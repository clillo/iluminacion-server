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

    protected List<QLCEfxPosition> buildPositions(){
        final List<QLCEfxPosition> positions = new ArrayList<>();

        int index=0;
        for (double time=0; time<=1; time+=0.01)
            positions.add(QLCEfxPosition.builder()
                    .index(index++)
                    .x(originX + (int) (time * (destinyX - originX)))
                    .y(originY + (int) (time * (destinyY - originY)))
                    .build());

        return positions;

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
