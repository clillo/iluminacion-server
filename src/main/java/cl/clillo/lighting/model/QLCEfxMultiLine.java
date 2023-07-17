package cl.clillo.lighting.model;

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

    public void updateParameters(final List<RealPoint> realPoints){
        this.realPoints = realPoints;
        setNodes(buildNodes());
    }


    public void updateParameters(final RealPoint leftUp, RealPoint rightDown){
        this.leftUp = leftUp;
        this.rightDown = rightDown;
        this.realPoints = new ArrayList<>();

        for (int i=0; i<3; i++){
            realPoints.add(randomA());
            realPoints.add(randomB());
            realPoints.add(randomC());
            realPoints.add(randomD());
        }

        setNodes(buildNodes());
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

    protected List<QLCEfxPosition> buildPositions(){
        if (realPoints==null)
            return List.of();

        final List<QLCEfxPosition> positions = new ArrayList<>();

        RealPoint p1 = realPoints.get(0);
        RealPoint initialPoint = p1;

        for(int i=1; i<realPoints.size(); i++) {
            positions.addAll(buildPositions(p1, realPoints.get(i), nodes));
            p1 = realPoints.get(i);
        }

        positions.addAll(buildPositions(p1, initialPoint, nodes));

        return positions;
    }

    private List<QLCEfxPosition> buildPositions(final RealPoint a, final RealPoint b, final List<QLCExecutionNode> nodes){
        final List<QLCEfxPosition> positions = new ArrayList<>();

        int index=0;
        for (double time=0; time<=1; time+=0.01)
            positions.add(QLCEfxPosition.builder()
                    .index(index++)
                    .x(a.getX() + (int) (time * (b.getX() - a.getX())))
                    .y(a.getY() + (int) (time * (b.getY() - a.getY())))
                    .build());

        return positions;
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
