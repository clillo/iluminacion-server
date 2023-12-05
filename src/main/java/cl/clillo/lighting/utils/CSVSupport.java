package cl.clillo.lighting.utils;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCSpeed;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVSupport {

    private void exportSequenceToCSVTransposed(final QLCSequence sequence){
        final List<Integer> fixtures = new ArrayList<>();
        final List<Integer> channelIds = new ArrayList<>();
        final Map<String, Integer> channels = new HashMap<>();
        final Map<Integer, String[]> channelDescription = new HashMap<>();
        final QLCFixtureBuilder fixtureModel = ShowCollection.getInstance().getQlcModel();

        int maxSteps = 0;
        for (QLCStep step: sequence.getQlcStepWithoutFade()) {
            if (maxSteps<step.getId()+1)
                maxSteps = step.getId()+1;
            for (QLCPoint point : step.getPointList()) {
                if (!fixtures.contains(point.getFixture().getId()))
                    fixtures.add(point.getFixture().getId());
                if (!channelIds.contains(point.getChannel()))
                    channelIds.add(point.getChannel());
                if (!channels.containsKey(point.getFixture().getId() + ";" + point.getChannel()))
                    channels.put(point.getFixture().getId() + ";" + point.getChannel(), channels.size() + 1);
            }
        }

        for (int fixtureId: fixtures)
            channelDescription.put(fixtureId, fixtureModel.getFixture(fixtureId).getFixtureModel().getChannels());

        final Map<Integer, List<Integer>> steps = new HashMap<>();
        for (Map.Entry<String, Integer> entry: channels.entrySet()) {
            List<Integer> list = new ArrayList<>(maxSteps);
            for (int i=0; i<maxSteps; i++)
                list.add(0);
            steps.put(entry.getValue(), list);

        }

        for (QLCStep step: sequence.getQlcStepWithoutFade()) {
            for (QLCPoint point : step.getPointList()) {
                Integer id = channels.get(point.getFixture().getId() + ";" + point.getChannel());
                List<Integer> list = steps.get(id);
                list.set(step.getId(), point.getData());
            }
        }

        Collections.sort(fixtures);
        Collections.sort(channelIds);

        final List<String> output = new ArrayList<>();
        final StringBuilder sbSequence = new StringBuilder();
        final StringBuilder sbFixtures = new StringBuilder();
        final StringBuilder sbChannels = new StringBuilder();
        final StringBuilder sbChannelDescription = new StringBuilder();
        sbSequence.append("SequenceId;");
        sbFixtures.append("FixtureId;");
        sbChannels.append("ChannelId;");
        sbChannelDescription.append("ChannelDescription;");
        for (int fixture : fixtures) {
            for (int channel : channelIds) {
                sbSequence.append(sequence.getId()).append(';');
                sbFixtures.append(fixture).append(';');
                sbChannels.append(channel).append(';');
                sbChannelDescription.append(channelDescription.get(fixture)[channel]).append(';');
            }
            sbSequence.append(';');
            sbFixtures.append(';');
            sbChannels.append(';');
            sbChannelDescription.append(';');
        }

        output.add(sbSequence.toString());
        output.add(sbFixtures.toString());
        output.add(sbChannels.toString());
        output.add(sbChannelDescription.toString());

        for (int step=0; step<maxSteps; step++){
            final StringBuilder sbValues = new StringBuilder();
            sbValues.append(step).append(';');
            for (int fixture : fixtures) {
                for (int channel : channelIds) {
                    Integer id = channels.get(fixture + ";" + channel);
                    List<Integer> list = steps.get(id);
                    sbValues.append(list.get(step)).append(';');
                }
                sbValues.append(';');

            }
            output.add(sbValues.toString());
        }

        for (String str: output)
            System.out.println(str);

        System.out.println();
    }


    private void exportSequenceToCSV(){
        for (Show show: ShowCollection.getInstance().getShowList())
            if (show.getFunction().getType().equals("Sequence") && show.getFunction().getPath().equals("RGBW")) {
                // System.out.println(show.getFunction().getId());
                //  if (show.getFunction().getId()==61)
                exportSequenceToCSVTransposed(show.getFunction());
                //    show.getFunction().writeToConfigFile();
            }

    }

    public void printFunctionIds(){
        final Set<Integer> functionIds = new HashSet<>();
        final HashMap<Integer, String> names = new HashMap<>();

        for (Show show: ShowCollection.getInstance().getShowList()) {
            functionIds.add(show.getFunction().getId());
            names.put(show.getFunction().getId(), show.getFunction().getPath()+ "."+ show.getFunction().getName());
        }

        final List<Integer> finalList = new ArrayList<>(functionIds);

        Collections.sort(finalList);
        for (int functionId: finalList)
            System.out.println(functionId + "\t" + names.get(functionId));

    }

    private void readCSVFunctionDefinition(final String fileName) {
        final List<Integer> fixtureIds = new ArrayList<>();
        final List<Integer> channelIds = new ArrayList<>();
        final List<List<Integer>> steps = new ArrayList<>();
        int sequenceId=-1;

        try (FileInputStream fstream = new FileInputStream(fileName);

             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));){

            String strLine;

            while ((strLine = br.readLine()) != null)   {
                String[] content = strLine.split(",");
                if ("ChannelDescription".equalsIgnoreCase(content[0])) {
                    continue;
                }
                if ("SequenceId".equalsIgnoreCase(content[0])) {
                    sequenceId = Integer.parseInt(content[1]);
                    continue;
                }
                if ("FixtureId".equalsIgnoreCase(content[0])) {
                    for (int i=1; i<content.length; i++)
                        try {
                            fixtureIds.add(Integer.parseInt(content[i]));
                        }catch (Exception e){

                        }

                    continue;
                }
                if ("ChannelId".equalsIgnoreCase(content[0])) {
                    for (int i=1; i<content.length; i++)
                        try {
                            channelIds.add(Integer.parseInt(content[i]));
                        }catch (Exception e){

                        }

                    continue;
                }

                final List<Integer> values = new ArrayList<>();
                for (int i=1; i<content.length; i++)
                    try {
                        values.add(Integer.parseInt(content[i]));
                    }catch (Exception e){

                    }
                steps.add(values);
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        final QLCFixtureBuilder fixtureModel = ShowCollection.getInstance().getQlcModel();
        final List<QLCStep> qlcStepList = new ArrayList<>();
        int i=0;
        for (List<Integer> step: steps){
            List<QLCPoint> pointList = new ArrayList<>();
            int valueIndex=0;
            for (int value: step){
                pointList.add(QLCPoint.builder()
                        .fixture(fixtureModel.getFixture(fixtureIds.get(valueIndex)))
                        .data(value)
                        .channel(channelIds.get(valueIndex))
                        .build());
                valueIndex++;
            }

            QLCStep qlcStep = QLCStep.builder()
                    .id(i)
                    .hold(1000)
                    .fadeIn(0)
                    .fadeOut(0)
                    .pointList(pointList)
                    .build();
            i++;

            qlcStepList.add(qlcStep);
        }

        QLCSequence sequence = new QLCSequence(sequenceId, "Sequence", "Bouncing pink", "RGBW",
                null, null, qlcStepList, null, QLCSpeed.builder().duration(1000).build());

        sequence.writeToConfigFile("/Users/carlos.lillo/IdeaProjects/iluminacion-server/src/main/resources/qlc/QLCSequence.RGBW/");
        // System.out.println(sequenceId);
        // System.out.println(fixtureIds);
        // System.out.println(channelIds);
        // System.out.println(steps);

    }
}
