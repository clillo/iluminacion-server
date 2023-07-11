package cl.clillo.lighting.model;

import cl.clillo.lighting.utils.ScreenPoint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ToString
@Getter
@Setter
public class QLCEfxMultiLine extends QLCEfx{

    private RealPoint leftUp;
    private RealPoint rightDown;
    private Random random = new Random();

    private List<RealPoint> realPoints;
    private int nodePos = 0;
    private int nodeDelta = 1;

    public QLCEfxMultiLine(final int id, final String type, final String name, final String path,
                           final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                           final QLCScene boundScene, List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);

    }

    public List<ScreenPoint> buildScreenPoint(){
        final List<ScreenPoint> nodes = new ArrayList<>();
        RealPoint p1 = realPoints.get(0);
        RealPoint initialPoint = p1;

        for(int i=1; i<realPoints.size(); i++) {
            nodes.addAll(buildScreenPoint(p1, realPoints.get(i)));
            p1 = realPoints.get(i);
        }

        nodes.addAll(buildScreenPoint(p1, initialPoint));

        return nodes;
    }

    private List<ScreenPoint> buildScreenPoint(final RealPoint a, final RealPoint b){
        final List<ScreenPoint> nodes = new ArrayList<>();
        for (double time=0; time<=1; time+=0.01) {
            double x = a.getX() + (int) (time * (b.getX() - a.getX()));
            double y = a.getY() + (int) (time * (b.getY() - a.getY()));
            nodes.add(new ScreenPoint(x, y));
        }
        return nodes;
    }

    public List<ScreenPoint> updateParameters(final List<RealPoint> realPoints){
        this.realPoints = realPoints;
        setNodes(buildNodes());
        return buildScreenPoint();
    }


    public List<ScreenPoint> updateParameters(final RealPoint leftUp, RealPoint rightDown){
        this.leftUp = leftUp;
        this.rightDown = rightDown;
        this.realPoints = new ArrayList<>();

        for (int i=0; i<4; i++){
            realPoints.add(randomA());
            realPoints.add(randomB());
            realPoints.add(randomC());
            realPoints.add(randomD());
        }

        setNodes(buildNodes());
        return buildScreenPoint();
    }

    private RealPoint randomA(){
        return RealPoint.builder().x(leftUp.getX()).y(randomY()).build();
    }

    private RealPoint randomB(){
        return RealPoint.builder().x(randomX()).y(leftUp.getY()).build();
    }

    private RealPoint randomC(){
        return RealPoint.builder().x(rightDown.getX()).y(randomY()).build();
    }

    private RealPoint randomD(){
        return RealPoint.builder().x(randomX()).y(rightDown.getY()).build();
    }

    private double randomY(){
        return leftUp.getY() + (rightDown.getY() - leftUp.getY())*Math.random();
    }

    private double randomX(){
        return leftUp.getX() + (rightDown.getX() - leftUp.getX())*Math.random();
    }

    private List<QLCExecutionNode> buildNodes(){
        final List<QLCExecutionNode> nodes = new ArrayList<>();
        RealPoint p1 = realPoints.get(0);
        RealPoint initialPoint = p1;

        for(int i=1; i<realPoints.size(); i++) {
            buildNodes(p1, realPoints.get(i), nodes);
            p1 = realPoints.get(i);
        }

        buildNodes(p1, initialPoint, nodes);
        return nodes;
    }

    private void buildNodes(final RealPoint a, final RealPoint b, final List<QLCExecutionNode> nodes){

        for (double time=0; time<=1; time+=0.01) {
            final List<int[]> channels = new ArrayList<>();
            final List<int[]> data = new ArrayList<>();
            for (QLCEfxFixtureData fixtureData: getFixtureList()) {
                final QLCRoboticFixture fixture = fixtureData.getFixture();

                double x = a.getX() + (int) (time * (b.getX() - a.getX()));
                double y = a.getY() + (int) (time * (b.getY() - a.getY()));

                double vPan = x / 256;
                double vPanFine = x % 256;

                double vTilt = y / 256;
                double vTiltFine = y % 256;

                channels.add(new int[]{fixture.getPanDmxChannel(), fixture.getTiltDmxChannel(),
                        fixture.getPanFineDmxChannel(), fixture.getTiltFineDmxChannel()});
                data.add(new int[] {(int) vPan, (int) vTilt, (int) vPanFine, (int) vTiltFine});

            }
            final QLCExecutionNode node = QLCExecutionNode.builder().channel(channels).data(data).holdTime(50).build();
            node.setId(nodes.size());
            nodes.add(node);
        }
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
