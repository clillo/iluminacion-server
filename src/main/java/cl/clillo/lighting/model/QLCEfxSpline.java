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
public class QLCEfxSpline extends QLCEfx{

    private RealPoint leftUp;
    private RealPoint rightDown;
    private Random random = new Random();

    private List<RealPoint> realPoints;
    private int nodePos = 0;
    private int nodeDelta = 1;

    public QLCEfxSpline(final int id, final String type, final String name, final String path,
                        final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                        final QLCScene boundScene, List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);

    }

    public void updateParameters(final List<RealPoint> realPoints){
        this.realPoints = realPoints;
        setNodes(buildNodes());
    }

    protected List<QLCEfxPosition> buildPositions(){
        if (realPoints==null)
            return List.of();

        final List<QLCEfxPosition> positions = new ArrayList<>();

        double[] x = new double[realPoints.size()+1];
        double[] y = new double[realPoints.size()+1];

        for(int i=0; i<realPoints.size(); i++) {
            x[i] = realPoints.get(i).getX();
            y[i] = realPoints.get(i).getY();
        }

        x[realPoints.size()] = realPoints.get(0).getX();
        y[realPoints.size()] = realPoints.get(0).getY();

        final Spline2D spline2D = new Spline2D(x, y);

        int index=0;
        for (double time=0; time<1; time+=0.01)
            positions.add(QLCEfxPosition.builder()
                    .index(index++)
                    .x(spline2D.getPoint(time)[0])
                    .y(spline2D.getPoint(time)[1])
                    .build());

        return positions;
    }

    @Override
    public QLCExecutionNode nextNode() {
        if (nodes.isEmpty())
            return null;
        nodePos++;
        if (nodePos>=nodes.size()){
            nodePos=0;
        }
        return nodes.get(nodePos);
    }
}
