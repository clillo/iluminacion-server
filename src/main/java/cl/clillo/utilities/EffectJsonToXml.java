package cl.clillo.utilities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilitario para convertir efectos JSON (definidos en qlc/effects/*.json)
 * al formato XML antiguo de QLCSequence.* usado por la app.
 *
 * Uso:
 *   java cl.clillo.utilities.EffectJsonToXml <effect.json> [<output.xml>] [<colors.json>]
 *
 * Si no se especifica output, se genera un archivo .xml junto al .json de entrada.
 * Si no se especifica colors.json, se usa: src/main/resources/qlc/effects/colors.json
 */
@Slf4j
public class EffectJsonToXml {

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Uso: java cl.clillo.utilities.EffectJsonToXml <effect.json> [<output.xml>] [<colors.json>]");
			return;
		}
		final Path effectPath = Paths.get(args[0]);
		final ObjectMapper mapper = new ObjectMapper();
		final Effect effect = mapper.readValue(effectPath.toFile(), Effect.class);

		final Path outputPath = args.length >= 2
				? Paths.get(args[1])
				: defaultOutputPath(effect);
		final Path colorsPath = args.length >= 3
				? Paths.get(args[2])
				: Paths.get("src/main/resources/qlc/effects/colors.json");

		final ColorsFile colors = mapper.readValue(colorsPath.toFile(), ColorsFile.class);
		final Map<String, RGB> palette = colors.colors != null ? colors.colors : new HashMap<>();

		final String xml = render(effect, palette);
		Files.createDirectories(outputPath.getParent());
		Files.write(outputPath, xml.getBytes(StandardCharsets.UTF_8));
		System.out.println("Generado: " + outputPath.toAbsolutePath());
	}

	private static Path defaultOutputPath(Effect effect) {
		final String typePrefix = isScene(effect) ? "QLCScene" : "QLCSequence";
		final String pathName = (effect.path == null || effect.path.isBlank())
				? (isScene(effect) ? "Generated Scene" : "Generated Sequence")
				: effect.path;
		final String dir = "src/main/resources/qlc/" + typePrefix + "." + pathName;
		final String file = typePrefix + "." + pathName + "." + effect.id + ".xml";
		return Paths.get(dir, file);
	}

	private static boolean isScene(Effect effect) {
		return effect != null && effect.type != null && effect.type.equalsIgnoreCase("Scene");
	}

	private static String render(Effect effect, Map<String, RGB> palette) {
		return isScene(effect) ? renderSceneXml(effect, palette) : renderSequenceXml(effect, palette);
	}

	private static String renderSequenceXml(Effect effect, Map<String, RGB> palette) {
		StringBuilder sb = new StringBuilder();
		sb.append("<doc>\n");
		final String pathName = (effect.path == null || effect.path.isBlank())
				? "Moving Head Bee Eye Sequence" : effect.path;
		sb.append("   <title>").append("QLCSequence.").append(pathName).append(".").append(effect.id).append("</title>\n");
		sb.append("   <common>\n");
		sb.append("      <id>").append(effect.id).append("</id>\n");
		sb.append("      <type>Sequence</type>\n");
		sb.append("      <path>").append(escape(pathName)).append("</path>\n");
		sb.append("      <name>").append(escape(effect.name)).append("</name>\n");
		sb.append("   </common>\n");
		sb.append("   <behaviour order=\"").append(escape(effect.order)).append("\" direction=\"").append(escape(effect.direction)).append("\" vdjType=\"BEAT_X_1\"/>\n");
		sb.append("   <steps>\n");

		final List<Integer> fixtures = effect.applyToFixtures != null ? effect.applyToFixtures : List.of();
		final int ledCount = effect.ledCount > 0 ? effect.ledCount : 6; // por defecto 6 (anillo), ignorar centro

		for (EffectStep step : effect.steps) {
			sb.append("      <step id=\"").append(step.index).append("\" fadeIn=\"0\" hold=\"").append(Math.max(0, effect.holdMs)).append("\" fadeOut=\"0\">\n");
			sb.append("         <points>\n");
			// Mapa ledIndex -> RGB
			Map<Integer, RGB> perLed = buildLedColorMap(step, palette);

			for (Integer fixtureId : fixtures) {
				// Por cada LED del anillo
				for (int led = 0; led < ledCount; led++) {
					RGB c = perLed.getOrDefault(led, new RGB(0, 0, 0));
					// Base channel para LED k
					int base = 21 + 4 * led;
					// R, G, B
					appendPoint(sb, fixtureId, base + 0, c.r);
					appendPoint(sb, fixtureId, base + 1, c.g);
					appendPoint(sb, fixtureId, base + 2, c.b);
					appendPoint(sb, fixtureId, base + 3, c.w);
				}
			}
			sb.append("         </points>\n");
			sb.append("      </step>\n");
		}

		sb.append("   </steps>\n");
		sb.append("</doc>\n");
		return sb.toString();
	}

	private static String renderSceneXml(Effect effect, Map<String, RGB> palette) {
		StringBuilder sb = new StringBuilder();
		sb.append("<doc>\n");
		final String pathName = (effect.path == null || effect.path.isBlank())
				? "Generated Scene" : effect.path;
		sb.append("   <title>").append("QLCScene.").append(pathName).append(".").append(effect.id).append("</title>\n");
		sb.append("   <common>\n");
		sb.append("      <id>").append(effect.id).append("</id>\n");
		sb.append("      <type>Scene</type>\n");
		sb.append("      <path>").append(escape(pathName)).append("</path>\n");
		sb.append("      <name>").append(escape(effect.name)).append("</name>\n");
		sb.append("   </common>\n");
		sb.append("   <points>\n");

		final List<Integer> fixtures = effect.applyToFixtures != null ? effect.applyToFixtures : List.of();
		final int ledCount = effect.ledCount > 0 ? effect.ledCount : 6;
		// Usar el primer paso si existe; si no, vacío
		final EffectStep step = (effect.steps != null && !effect.steps.isEmpty()) ? effect.steps.get(0) : new EffectStep();
		final Map<Integer, RGB> perLed = buildLedColorMap(step, palette);

		for (Integer fixtureId : fixtures) {
			for (int led = 0; led < ledCount; led++) {
				RGB c = perLed.getOrDefault(led, new RGB(0, 0, 0));
				int base = 21 + 4 * led;
				appendPoint(sb, fixtureId, base + 0, c.r);
				appendPoint(sb, fixtureId, base + 1, c.g);
				appendPoint(sb, fixtureId, base + 2, c.b);
				appendPoint(sb, fixtureId, base + 3, c.w);
			}
		}

		sb.append("   </points>\n");
		sb.append("</doc>\n");
		return sb.toString();
	}

	private static Map<Integer, RGB> buildLedColorMap(EffectStep step, Map<String, RGB> palette) {
		Map<Integer, RGB> map = new HashMap<>();
		if (step.leds == null) return map;
		for (LedSpec ledSpec : step.leds) {
			if (ledSpec == null) continue;
			int idx = Math.max(0, ledSpec.led);
			RGB rgb;
			if (ledSpec.color != null && !ledSpec.color.isBlank()) {
				rgb = palette.get(ledSpec.color);
				if (rgb == null) {
					log.warn("Color '{}' not found in palette; defaulting to off at LED {}", ledSpec.color, idx);
					rgb = new RGB(0, 0, 0, 0);
				}
			} else {
				rgb = new RGB(safe(ledSpec.r), safe(ledSpec.g), safe(ledSpec.b), safe(ledSpec.w));
			}
			map.put(idx, rgb);
		}
		// También procesar LedPoint (si viene en JSON)
		if (step.ledPoints != null) {
			for (cl.clillo.lighting.model.LedPoint p : step.ledPoints) {
				if (p == null) continue;
				int idx = Math.max(0, p.getId());
				map.put(idx, new RGB(
						safe(p.getR()),
						safe(p.getG()),
						safe(p.getB()),
						safe(p.getW())
				));
			}
		}
		return map;
	}

	private static int safe(Integer v) {
		return v == null ? 0 : Math.max(0, Math.min(255, v));
	}

	private static void appendPoint(StringBuilder sb, int fixtureId, int channel, int value) {
		sb.append("            <point fixture-robotic=\"false\" fixture=\"")
				.append(fixtureId).append("\" channel=\"").append(channel).append("\" value=\"")
				.append(value).append("\"/>\n");
	}

	private static String escape(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class Effect {
		public int id;
		public String name;
		public String type = "Sequence"; // "Sequence" o "Scene"
		public String path;              // directorio lógico para XML de salida
		public String order = "LOOP";
		public String direction = "FORWARD";
		public int holdMs = 1000;
		public List<Integer> applyToFixtures;
		public int ledCount = 6;
		public List<EffectStep> steps = new ArrayList<>();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class EffectStep {
		public int index;
		public List<LedSpec> leds;
		public List<cl.clillo.lighting.model.LedPoint> ledPoints;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class LedSpec {
		public int led;
		public String color; // referencia a colors.json
		public Integer r;
		public Integer g;
		public Integer b;
		public Integer w;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class ColorsFile {
		public Map<String, RGB> colors = new HashMap<>();
	}

	static class RGB {
		public int r;
		public int g;
		public int b;
		public int w;

		public RGB() {}
		public RGB(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		public RGB(int r, int g, int b, int w) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.w = w;
		}
	}
}


