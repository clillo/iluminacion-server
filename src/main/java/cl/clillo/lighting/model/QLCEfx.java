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
public abstract class QLCEfx extends QLCFunction{

    private final QLCDirection direction;
    private final QLCRunOrder runOrder;
    private final List<QLCStep> qlcStepList;
    private final QLCScene boundScene;
    private final List<QLCRoboticFixture> fixtureList;
    protected List<QLCExecutionNode> nodes;

    public QLCEfx(final int id, final String type, final String name, final String path,
                  final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                  final QLCScene boundScene, List<QLCRoboticFixture> fixtureList) {
        super(id, type, name, path);
        this.qlcStepList = qlcStepList;
        this.direction = direction;
        this.runOrder = runOrder;
        this.boundScene = boundScene;
        this.fixtureList = fixtureList;

        if (boundScene!=null)
            for (QLCPoint boundPoint: boundScene.getQlcPointList()){
                final String operationalBoundId = boundPoint.getOperationalId();
                for (QLCStep step: qlcStepList){
                    boolean found = false;
                    for (QLCPoint stepPoint: step.getPointList()) {
                          if (stepPoint.getOperationalId().equals(operationalBoundId))
                              found = true;
                    }
                    if (!found)
                        step.getPointList().add(boundPoint);

                }
            }

        setNodes(buildNodes());
    }

    protected void setNodes(List<QLCExecutionNode> nodes) {
        this.nodes = nodes;
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());
        sb.append(direction).append('\t');
        sb.append(runOrder).append('\t');

        for (QLCStep step: qlcStepList) {
            sb.append('[').append('{')
                    .append(step.getFadeIn()).append(',')
                    .append(step.getHold()).append(',')
                    .append(step.getFadeOut()).append(',')
                    .append('}')
                    .append('{');

            for (QLCPoint point: step.getPointList())
                sb.append('{')
                        .append(point.getDmxChannel()).append(',')
                        .append(point.getData())
                        .append('}');

            sb.append('}').append(']');
        }

        return sb.toString();
    }

    public abstract List<ScreenPoint> buildScreenPoint();

    private List<QLCExecutionNode> buildNodes(){
        final List<QLCExecutionNode> nodes = new ArrayList<>();
        for (double time=0; time<=360; time+=1) {
            for (QLCRoboticFixture fixture : fixtureList) {
            }
        }
        return nodes;
    }

    public abstract QLCExecutionNode nextNode();

}
