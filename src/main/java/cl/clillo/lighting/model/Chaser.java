package cl.clillo.lighting.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ToString
public class Chaser extends QLCFunction implements Sequenceable{

    private final List<ChaserStep> chaserSteps;
    private QLCDirection direction;
    private final QLCRunOrder runOrder;
    private int qlcSpeed;
    private Show blackoutShow;

    public Chaser(final int id, final String type, final String name, final String path, final QLCDirection direction,
                  final QLCRunOrder runOrder, final int qlcSpeed, final List<ChaserStep> chaserSteps) {
        super(id, type, name, path);
        this.direction = direction;
        this.runOrder = runOrder;
        this.qlcSpeed = qlcSpeed;
        this.chaserSteps = chaserSteps;
    }

    public String toSmallString() {
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (ChaserStep scene : chaserSteps)
            sb.append('{').append(scene.getId()).append('}');
        return sb.toString();
    }

    public static Chaser readFromJson(final ShowCollection collection, final File file) throws IOException {
        final JsonNode root = ConfigJsonIO.readTree(file);
        final List<ChaserStep> steps = new ArrayList<>();
        final JsonNode showsNode = root.get("shows");
        if (showsNode != null && showsNode.isArray()) {
            for (JsonNode showNode : showsNode) {
                final int showId = showNode.path("id").asInt(-1);
                final Show show = collection.getShow(showId);
                if (show == null) {
                    System.out.println("Show no existe: " + showId);
                    System.exit(0);
                }
                steps.add(ChaserStep.builder()
                        .id(showId)
                        .fadeIn(showNode.path("fadeIn").asInt(0))
                        .hold(showNode.path("hold").asInt(0))
                        .fadeOut(showNode.path("fadeOut").asInt(0))
                        .show(show)
                        .build());
            }
        }

        final JsonNode properties = root.path("properties");
        final QLCDirection direction = QLCDirection.valueOf(
                ConfigJsonIO.text(properties, "direction", "FORWARD"));
        final QLCRunOrder runOrder = QLCRunOrder.valueOf(
                ConfigJsonIO.text(properties, "runOrder", "RANDOM"));
        final int speed = properties.path("speed").asInt(1);

        final Chaser chaser = new Chaser(
                root.path("id").asInt(),
                ConfigJsonIO.text(root, "type", "Chaser"),
                ConfigJsonIO.text(root, "name", ""),
                ConfigJsonIO.text(root, "path", ""),
                direction,
                runOrder,
                speed,
                steps);
        if (root.has("blackoutShowId"))
            chaser.setBlackoutShow(collection.getShow(root.path("blackoutShowId").asInt(-1)));
        return chaser;
    }

    @Override
    protected void writeJson(final ObjectNode root) {
        super.writeJson(root);
        if (blackoutShow != null)
            root.put("blackoutShowId", blackoutShow.getId());
        final ObjectNode properties = ConfigJsonIO.createObject();
        properties.put("runOrder", String.valueOf(runOrder));
        properties.put("direction", String.valueOf(direction));
        properties.put("speed", String.valueOf(qlcSpeed));
        root.set("properties", properties);
        final ArrayNode shows = ConfigJsonIO.createArray();
        for (ChaserStep step : chaserSteps) {
            if (step.getShow() == null)
                continue;
            final ObjectNode showNode = ConfigJsonIO.createObject();
            showNode.put("id", step.getShow().getId());
            showNode.put("fadeIn", step.getFadeIn());
            showNode.put("hold", step.getHold());
            showNode.put("fadeOut", step.getFadeOut());
            shows.add(showNode);
        }
        root.set("shows", shows);
    }

    public List<ChaserStep> getChaserSteps() {
        return chaserSteps;
    }

    @Override
    public QLCDirection getDirection() {
        return direction;
    }

    @Override
    public QLCRunOrder getRunOrder() {
        return runOrder;
    }

    @Override
    public void setDirection(QLCDirection direction) {
        this.direction = direction;
    }

    @Override
    public int getSpeed() {
        return qlcSpeed;
    }

    @Override
    public void setSpeed(int speed) {
        this.qlcSpeed = speed;
    }

    public void setBlackoutShow(Show blackoutShow) {
        this.blackoutShow = blackoutShow;
    }

    public Show getBlackoutShow() {
        return blackoutShow;
    }
}