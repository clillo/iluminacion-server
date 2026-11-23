package cl.clillo.lighting.repository;

import cl.clillo.lighting.config.QLCReader;
import cl.clillo.lighting.model.ConfigJsonIO;
import cl.clillo.lighting.model.Point;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateRepository {

    private static final String FILENAME = QLCReader.repoBase + "/general.state.json";

    private List<Point> limitMasterDimmer = new ArrayList<>();
    private final Map<Integer, Integer> events = new HashMap<>();
    private final Map<String, Integer> commands = new HashMap<>();
    private int rgbwMasterDimmer;
    private int movingHeadSpotBeamMasterDimmer;
    private int movingHeadSpotMasterDimmer;
    private int movingHeadBeamMasterDimmer;

    private HashMap<Integer, Integer> maxDimmerValues;

    private static final class InstanceHolder {
        private static StateRepository instance;

        public static StateRepository getInstance() {
            if (instance==null){
                instance = new StateRepository();
                instance.read();
            }
            return instance;
        }
    }

    public static StateRepository getInstance() {
        return InstanceHolder.getInstance();
    }

    public void setLimitMasterDimmer(List<Point> limitMasterDimmer) {
        this.limitMasterDimmer = limitMasterDimmer;
    }

    public List<Point> getLimitMasterDimmer() {
        return limitMasterDimmer;
    }

    public void setRgbwMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.rgbwMasterDimmer = value;
    }

    public int getRgbwMasterDimmer() {
        return rgbwMasterDimmer;
    }

    public int getMovingHeadSpotBeamMasterDimmer() {
        return movingHeadSpotBeamMasterDimmer;
    }

    public int getMovingHeadSpotMasterDimmer() {
        return movingHeadSpotMasterDimmer;
    }

    public int getMovingHeadBeamMasterDimmer() {
        return movingHeadBeamMasterDimmer;
    }

    public void setMovingHeadBeamMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.movingHeadBeamMasterDimmer = value;
    }

    public void setMovingHeadSpotBeamMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.movingHeadSpotBeamMasterDimmer = value;
    }

    public void setMovingHeadSpotMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.movingHeadSpotMasterDimmer = value;
    }

    public int getMaxValue(final int universe, int channel){
        if (maxDimmerValues.containsKey(universe*512+ channel))
            return maxDimmerValues.get(channel);

        return -1;
    }

    public void write(){
        try {
            final ObjectNode root = ConfigJsonIO.createObject();
            final ObjectNode masterDimmer = ConfigJsonIO.createObject();
            masterDimmer.put("rgbw", rgbwMasterDimmer);
            masterDimmer.put("movingHeadSpotBeam", movingHeadSpotBeamMasterDimmer);
            masterDimmer.put("movingHeadSpot", movingHeadSpotMasterDimmer);
            masterDimmer.put("movingHeadBeam", movingHeadBeamMasterDimmer);
            root.set("masterDimmer", masterDimmer);

            final ArrayNode eventsNode = ConfigJsonIO.createArray();
            for (Map.Entry<Integer, Integer> entry : events.entrySet()) {
                final ObjectNode event = ConfigJsonIO.createObject();
                event.put("id", entry.getKey());
                event.put("show", entry.getValue());
                eventsNode.add(event);
            }
            root.set("virtualDJEvents", eventsNode);

            final ArrayNode commandsNode = ConfigJsonIO.createArray();
            for (Map.Entry<String, Integer> entry : commands.entrySet()) {
                final ObjectNode command = ConfigJsonIO.createObject();
                command.put("name", entry.getKey());
                command.put("show", entry.getValue());
                commandsNode.add(command);
            }
            root.set("virtualDJCommands", commandsNode);

            ConfigJsonIO.write(new File(FILENAME), root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void read(){
        try {
            maxDimmerValues = new HashMap<>();
            final JsonNode root = ConfigJsonIO.readTree(new File(FILENAME));
            final JsonNode masterDimmer = root.path("masterDimmer");
            rgbwMasterDimmer = masterDimmer.path("rgbw").asInt(0);
            movingHeadSpotBeamMasterDimmer = masterDimmer.path("movingHeadSpotBeam").asInt(0);
            movingHeadSpotMasterDimmer = masterDimmer.path("movingHeadSpot").asInt(0);
            movingHeadBeamMasterDimmer = masterDimmer.path("movingHeadBeam").asInt(0);

            final JsonNode eventsNode = root.get("virtualDJEvents");
            if (eventsNode != null && eventsNode.isArray()) {
                for (JsonNode event : eventsNode)
                    this.events.put(event.path("id").asInt(), event.path("show").asInt());
            }

            final JsonNode commandsNode = root.get("virtualDJCommands");
            if (commandsNode != null && commandsNode.isArray()) {
                for (JsonNode command : commandsNode)
                    this.commands.put(command.path("name").asText(), command.path("show").asInt());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Show getEventShow(int eventId){
        if (!events.containsKey(eventId))
            return null;

        return ShowCollection.getInstance().getShow(events.get(eventId));
    }

    public Show getCommkandShow(String command){
        if (!commands.containsKey(command))
            return null;

        return ShowCollection.getInstance().getShow(commands.get(command));
    }
}

