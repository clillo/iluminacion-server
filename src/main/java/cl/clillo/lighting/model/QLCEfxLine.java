package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.utils.ScreenPoint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
public class QLCEfxLine extends QLCEfx{

    private double originX;
    private double originY;
    private double destinyX;
    private double destinyY;

    private int nodePos = 0;
    private int nodeDelta = 1;

    public QLCEfxLine(final int id, final String type, final String name, final String path,
                      final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                      final QLCScene boundScene, List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);
        nodePos = 0;
    }

    public void updateParameters(final double originX, final double originY, final double destinyX, final double destinyY){
        this.originX = originX;
        this.originY = originY;
        this.destinyX = destinyX;
        this.destinyY = destinyY;

        setNodes(buildNodes());
    }

    protected List<QLCExecutionNode> buildNodes(){
        final List<QLCExecutionNode> nodes = new ArrayList<>();

        for (double time=0; time<=1; time+=0.01) {
            final List<int[]> channels = new ArrayList<>();
            final List<int[]> data = new ArrayList<>();
            final ScreenPoint[] screenPoints = new ScreenPoint[getFixtureList().size()];

            int index = 0;

            for (QLCEfxFixtureData fixtureData: getFixtureList()) {
                final QLCRoboticFixture fixture = fixtureData.getFixture();

                double fixtureTime = (fixtureData.isReverse()?1.0-time:time);

                double x = originX + (int) (fixtureTime * (destinyX - originX));
                double y = originY + (int) (fixtureTime * (destinyY - originY));

                screenPoints[index] = new ScreenPoint(x, y);

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
                    .screenPoints(screenPoints)
                    .holdTime(50)
                    .build();

            node.setId(nodes.size());
            nodes.add(node);
        }

        return nodes;
    }

    @Override
    public QLCExecutionNode nextNode() {
        nodePos+=nodeDelta;
        if (nodePos<0){
            nodePos=1;
            nodeDelta=1;
        }
        if (nodePos>=nodes.size()){
            nodePos=nodes.size()-2;
            nodeDelta=-1;
        }
        return nodes.get(nodePos);
    }
}
