package cl.clillo.lighting.model;

import cl.clillo.lighting.gui.ScreenPoint;
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
    private final List<QLCEfxFixtureData> fixtureList;
    protected List<QLCExecutionNode> nodes;

    public QLCEfx(final int id, final String type, final String name, final String path,
                  final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                  final QLCScene boundScene, List<QLCEfxFixtureData> fixtureList) {
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

    protected final List<QLCExecutionNode> buildNodes(){

        final List<QLCEfxPosition> positions = buildPositions();
        final int positionsSize = positions.size();
        final List<QLCExecutionNode> nodes = new ArrayList<>();

        for (int positionIndex=0; positionIndex<positionsSize; positionIndex++) {

            final List<int[]> channels = new ArrayList<>();
            final List<int[]> data = new ArrayList<>();
            final ScreenPoint[] screenPoints = new ScreenPoint[getFixtureList().size()];

            for (int positionFixture=0; positionFixture<getFixtureList().size(); positionFixture++) {
                QLCEfxFixtureData fixtureData = getFixtureList().get(positionFixture);

                int fixtureIndexPosition = ((fixtureData.isReverse()?positionsSize-positionIndex:positionIndex)+ (int)fixtureData.getStartOffset())%positionsSize;
                final QLCEfxPosition position = positions.get(fixtureIndexPosition);

                screenPoints[positionFixture] = position.buildScreenPoint(fixtureData.getFixture().getId());
                channels.add(fixtureData.getChannels());
                data.add(position.buildDataArray());
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

    public abstract QLCExecutionNode nextNode();

    protected List<QLCEfxPosition> buildPositions(){
        return List.of();
    }
}
