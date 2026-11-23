#!/usr/bin/env python3
"""Convert QLC show XML configs under src/main/resources/qlc to JSON."""

from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/qlc"


def text(el: ET.Element | None, default: str | None = None) -> str | None:
    if el is None or el.text is None:
        return default
    value = el.text.strip()
    return value if value != "" else default


def as_int(value: str | None, default: int | None = None) -> int | None:
    if value is None:
        return default
    try:
        return int(float(value))
    except ValueError:
        return default


def as_float(value: str | None, default: float | None = None) -> float | None:
    if value is None:
        return default
    try:
        return float(value)
    except ValueError:
        return default


def as_bool(value: str | None) -> bool:
    return value is not None and value.strip().lower() == "true"


def common_fields(common: ET.Element) -> dict:
    data = {
        "id": as_int(text(common.find("id"))),
        "type": text(common.find("type")),
        "name": text(common.find("name")),
        "path": text(common.find("path")),
    }
    if as_bool(text(common.find("blackout"))):
        data["blackout"] = True
    if text(common.find("system")) == "TotalBlackout":
        data["totalBlackout"] = True
    if as_bool(text(common.find("initTrigger"))):
        data["initTrigger"] = True
    sub_type = text(common.find("subType"))
    if sub_type:
        data["subType"] = sub_type
    return data


def parse_point(point: ET.Element) -> dict:
    item: dict = {
        "fixture": as_int(point.get("fixture")),
        "value": as_int(point.get("value"), as_int(point.get("data"), 0)),
    }
    if as_bool(point.get("fixture-robotic")):
        item["fixtureRobotic"] = True
    channel = point.get("channel")
    if channel is not None:
        item["channel"] = as_int(channel)
    point_type = point.get("type")
    if point_type:
        item["channelType"] = point_type
    return item


def parse_fixtures(fixtures_el: ET.Element | None) -> list[dict]:
    if fixtures_el is None:
        return []
    result = []
    for fixture in fixtures_el.findall("fixture"):
        result.append({
            "id": as_int(text(fixture.find("id"))),
            "offset": as_float(text(fixture.find("offset")), 0.0),
            "reverse": as_bool(text(fixture.find("reverse"))),
        })
    return result


def convert_scene(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    data["points"] = [parse_point(p) for p in root.findall("points/point")]
    return data


def convert_sequence(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    behaviour = root.find("behaviour")
    if behaviour is not None:
        if behaviour.get("order"):
            data["order"] = behaviour.get("order")
        if behaviour.get("direction"):
            data["direction"] = behaviour.get("direction")
        if behaviour.get("vdjType"):
            data["vdjType"] = behaviour.get("vdjType")
    steps = []
    for step in root.findall("steps/step"):
        steps.append({
            "id": as_int(step.get("id"), 0),
            "fadeIn": as_int(step.get("fadeIn"), 0),
            "hold": as_int(step.get("hold"), 0),
            "fadeOut": as_int(step.get("fadeOut"), 0),
            "points": [parse_point(p) for p in step.findall("points/point")],
        })
    data["steps"] = steps
    return data


def convert_collection(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    shows = []
    for show in root.findall("shows/show"):
        item = {"id": as_int(show.get("id"))}
        if show.get("name"):
            item["name"] = show.get("name")
        shows.append(item)
    data["shows"] = shows
    return data


def convert_chaser(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    blackout = root.find("blackout")
    if blackout is not None and blackout.get("id") is not None:
        data["blackoutShowId"] = as_int(blackout.get("id"))
    props = root.find("properties")
    if props is not None:
        properties = {}
        for child in props:
            properties[child.tag] = text(child)
        data["properties"] = properties
    shows = []
    for show in root.findall("shows/show"):
        shows.append({
            "id": as_int(show.get("id")),
            "fadeIn": as_int(show.get("fadeIn"), 0),
            "hold": as_int(show.get("hold"), 0),
            "fadeOut": as_int(show.get("fadeOut"), 0),
        })
    data["shows"] = shows
    return data


def convert_efx_circle(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    data["type"] = "QLCEfxCircle"
    data["fixtures"] = parse_fixtures(root.find("fixtures"))
    dimensions = root.find("dimensions")
    if dimensions is not None:
        data["dimensions"] = {
            "centerX": as_float(text(dimensions.find("centerX")), 0.0),
            "centerY": as_float(text(dimensions.find("centerY")), 0.0),
            "width": as_float(text(dimensions.find("width")), 0.0),
            "height": as_float(text(dimensions.find("height")), 0.0),
        }
    return data


def convert_efx_line(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    data["type"] = "QLCEfxLine"
    data["fixtures"] = parse_fixtures(root.find("fixtures"))
    points = root.find("points")
    if points is not None:
        origin = points.find("origin")
        destiny = points.find("destiny")
        data["line"] = {
            "originX": as_float(text(origin.find("x")) if origin is not None else None, 0.0),
            "originY": as_float(text(origin.find("y")) if origin is not None else None, 0.0),
            "destinyX": as_float(text(destiny.find("x")) if destiny is not None else None, 0.0),
            "destinyY": as_float(text(destiny.find("y")) if destiny is not None else None, 0.0),
        }
    return data


def convert_efx_multiline(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    data["type"] = "QLCEfxMultiLine"
    data["fixtures"] = parse_fixtures(root.find("fixtures"))
    dimensions = root.find("dimensions")
    if dimensions is not None:
        left_up = dimensions.find("leftUp")
        right_down = dimensions.find("rightDown")
        data["dimensions"] = {
            "leftUp": {
                "x": as_float(text(left_up.find("x")) if left_up is not None else None, 0.0),
                "y": as_float(text(left_up.find("y")) if left_up is not None else None, 0.0),
            },
            "rightDown": {
                "x": as_float(text(right_down.find("x")) if right_down is not None else None, 0.0),
                "y": as_float(text(right_down.find("y")) if right_down is not None else None, 0.0),
            },
        }
    config = root.find("config")
    if config is not None:
        data["config"] = {
            "loops": as_int(text(config.find("loops")), 1),
            "type": text(config.find("type")),
        }
    return data


def convert_efx_spline(root: ET.Element) -> dict:
    data = common_fields(root.find("common"))
    data["type"] = "QLCEfxSpline"
    data["fixtures"] = parse_fixtures(root.find("fixtures"))
    real_points = []
    for point in root.findall("realPoints/point"):
        real_points.append({
            "x": as_float(text(point.find("x")), 0.0),
            "y": as_float(text(point.find("y")), 0.0),
        })
    # some files may use <realPoint>
    if not real_points:
        for point in root.findall("realPoints/*"):
            if point.find("x") is not None:
                real_points.append({
                    "x": as_float(text(point.find("x")), 0.0),
                    "y": as_float(text(point.find("y")), 0.0),
                })
    data["realPoints"] = real_points
    return data


def convert_state(root: ET.Element) -> dict:
    master = root.find("masterDimmer") or ET.Element("masterDimmer")
    data = {
        "masterDimmer": {
            "rgbw": as_int(text(master.find("rgbw")), 0),
            "movingHeadSpotBeam": as_int(text(master.find("movingHeadSpotBeam")), 0),
            "movingHeadSpot": as_int(text(master.find("movingHeadSpot")), 0),
            "movingHeadBeam": as_int(text(master.find("movingHeadBeam")), 0),
        },
        "virtualDJEvents": [],
        "virtualDJCommands": [],
    }
    events_parent = root.find("virtualDJEvents")
    if events_parent is not None:
        for event in list(events_parent):
            if event.get("id") is None:
                continue
            data["virtualDJEvents"].append({
                "id": as_int(event.get("id")),
                "show": as_int(event.get("show")),
            })
    commands_parent = root.find("virtualDJCommands")
    if commands_parent is not None:
        for command in list(commands_parent):
            name = command.get("name")
            if name is None:
                continue
            data["virtualDJCommands"].append({
                "name": name,
                "show": as_int(command.get("show")),
            })
    return data


CONVERTERS = {
    "QLCScene": convert_scene,
    "QLCSequence": convert_sequence,
    "QLCCollection": convert_collection,
    "Chaser": convert_chaser,
    "QLCEfxCircle": convert_efx_circle,
    "QLCEfxLine": convert_efx_line,
    "QLCEfxMultiLine": convert_efx_multiline,
    "QLCEfxSpline": convert_efx_spline,
}


def detect_kind(path: Path, root: ET.Element) -> str | None:
    name = path.name
    for prefix in CONVERTERS:
        if name.startswith(prefix):
            return prefix
    if path.name == "general.state.xml":
        return "State"
    common = root.find("common")
    if common is not None:
        type_name = text(common.find("type"))
        mapping = {
            "Scene": "QLCScene",
            "Sequence": "QLCSequence",
            "Collection": "QLCCollection",
            "Chaser": "Chaser",
            "QLCEfxCircle": "QLCEfxCircle",
            "QLCEfxLine": "QLCEfxLine",
            "QLCEfxMultiLine": "QLCEfxMultiLine",
            "QLCEfxSpline": "QLCEfxSpline",
        }
        return mapping.get(type_name)
    return None


def convert_file(xml_path: Path, delete_xml: bool) -> Path | None:
    root = ET.parse(xml_path).getroot()
    kind = detect_kind(xml_path, root)
    if kind == "State":
        data = convert_state(root)
        out = xml_path.with_suffix(".json")
    elif kind in CONVERTERS:
        data = CONVERTERS[kind](root)
        out = xml_path.with_suffix(".json")
    else:
        print(f"SKIP unknown: {xml_path}", file=sys.stderr)
        return None

    out.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    if delete_xml:
        xml_path.unlink()
    return out


def main() -> int:
    delete_xml = "--delete-xml" in sys.argv
    dry_run = "--dry-run" in sys.argv
    converted = 0
    for xml_path in sorted(ROOT.rglob("*.xml")):
        if xml_path.parent.name == "Fixtures":
            continue
        if dry_run:
            print(xml_path)
            converted += 1
            continue
        out = convert_file(xml_path, delete_xml=delete_xml)
        if out is not None:
            converted += 1
            print(f"{xml_path.relative_to(ROOT)} -> {out.name}")
    print(f"converted={converted} delete_xml={delete_xml}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
