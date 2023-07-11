package cl.clillo.lighting.model;

import cl.clillo.lighting.utils.ScreenPoint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
public class QLCEfxCircle extends QLCEfx{

    private double centerX;
    private double centerY;
    private double width;
    private double height;

    private int nodePos = 0;

    public QLCEfxCircle(final int id, final String type, final String name, final String path,
                        final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                        final QLCScene boundScene, final List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);

    }

    public List<ScreenPoint> buildScreenPoint(){
        final List<ScreenPoint> nodes = new ArrayList<>();
        for (double time=0; time<=360; time+=1) {
            double x = centerX + (int) (Math.cos(Math.toRadians(time)) * width);
            double y = centerY + (int) (Math.sin(Math.toRadians(time)) * height);
            nodes.add(new ScreenPoint(x, y));
        }
        return nodes;
    }

    public List<ScreenPoint> updateParameters(final double centerX, final double centerY, final double width, final double height){
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;

        setNodes(buildNodes());
        return buildScreenPoint();
    }

    private List<QLCExecutionNode> buildNodes(){
        final List<QLCExecutionNode> nodes = new ArrayList<>();
        for (double time=0; time<=360; time+=1) {
            final List<int[]> channels = new ArrayList<>();
            final List<int[]> data = new ArrayList<>();
            final double[] timePos = new double[getFixtureList().size()];
            int index = 0;
            for (QLCEfxFixtureData fixtureData: getFixtureList()) {
                final QLCRoboticFixture fixture = fixtureData.getFixture();

                double fixtureTime = ((fixtureData.isReverse()?360-time:time)+ fixtureData.getStartOffset())%360;
                timePos[index] = fixtureTime;

                double x = centerX + (int) (Math.cos(Math.toRadians(fixtureTime)) * width);
                double y = centerY + (int) (Math.sin(Math.toRadians(fixtureTime)) * height);

                double vPan = x / 256;
                double vPanFine = x % 256;

                double vTilt = y / 256;
                double vTiltFine = y % 256;

                channels.add(new int[]{fixture.getPanDmxChannel(), fixture.getTiltDmxChannel(),
                        fixture.getPanFineDmxChannel(), fixture.getTiltFineDmxChannel()});
                data.add(new int[] {(int) vPan, (int) vTilt, (int) vPanFine, (int) vTiltFine});
                index++;
            }

            final QLCExecutionNode node = QLCExecutionNode.builder()
                    .channel(channels)
                    .data(data)
                    .holdTime(50)
                    .timePos(timePos)
                    .build();

            node.setId(nodes.size());
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public QLCExecutionNode nextNode() {
        nodePos++;
        if (nodePos>=nodes.size()){
            nodePos=0;
        }
        return nodes.get(nodePos);
    }
}
