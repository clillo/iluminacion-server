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
public class QLCEfxLine extends QLCEfx{

    private double originX;
    private double originY;
    private double destinyX;
    private double destinyY;

    public QLCEfxLine(final int id, final String type, final String name, final String path,
                      final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                      final QLCScene boundScene, List<QLCRoboticFixture> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);

    }

    public List<ScreenPoint> buildScreenPoint(){
        final List<ScreenPoint> nodes = new ArrayList<>();
        for (double time=0; time<=1; time+=0.01) {
            double x = originX + (int) (time * (destinyX - originX));
            double y = originY + (int) (time * (destinyY - originY));
            nodes.add(new ScreenPoint(x, y));
        }
        return nodes;
    }

    public List<ScreenPoint> updateParameters(final double originX, final double originY, final double destinyX, final double destinyY){
        this.originX = originX;
        this.originY = originY;
        this.destinyX = destinyX;
        this.destinyY = destinyY;

        setNodes(buildNodes());
        return buildScreenPoint();
    }

    private List<QLCExecutionNode> buildNodes(){
        final List<QLCExecutionNode> nodes = new ArrayList<>();
        for (double time=0; time<=1; time+=0.01) {
            for (QLCRoboticFixture fixture : getFixtureList()) {
                double x = originX + (int) (time * (destinyX - originX));
                double y = originY + (int) (time * (destinyY - originY));

                double vPan = x / 256;
                double vPanFine = x % 256;

                double vTilt = y / 256;
                double vTiltFine = y % 256;

                final int[] channels = {fixture.getPanDmxChannel(), fixture.getTiltDmxChannel(),
                        fixture.getPanFineDmxChannel(), fixture.getTiltFineDmxChannel()};

                final int[] data = {(int) vPan, (int) vTilt, (int) vPanFine, (int) vTiltFine};

                nodes.add(QLCExecutionNode.builder().channel(channels).data(data).holdTime(50).build());

            }
        }

        return nodes;
    }

}
