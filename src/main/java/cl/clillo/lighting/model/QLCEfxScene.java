package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.gui.ScreenPoint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@Setter
public class QLCEfxScene extends QLCEfx{

    private final List<QLCPoint> qlcPointList;
    private int nodePos = 0;

    public QLCEfxScene(final int id, final String type, final String name, final String path,
                       List<QLCPoint> qlcPointList, List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, QLCDirection.FORWARD, QLCRunOrder.LOOP, null, null, fixtureList);
        this.qlcPointList = qlcPointList;
    }

    public void updateParameters(final int fixtureId, final double newX, final double newY){
        if (fixtureId>0){
            QLCEfxPosition position = new QLCEfxPosition(0, newX, newY);
            for (QLCPoint qlcPoint : qlcPointList){
                if (qlcPoint.getFixture().getId()!=fixtureId)
                    continue;
                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.PAN)
                    qlcPoint.setData((int)position.getVPan());
                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.PAN_FINE)
                    qlcPoint.setData((int)position.getVPanFine());
                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.TILT)
                    qlcPoint.setData((int)position.getVTilt());
                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.TILT_FINE)
                    qlcPoint.setData((int)position.getVTiltFine());
            }
        }

        setNodes(buildNodes());
    }

    protected List<QLCEfxPosition> buildPositions(){
        final List<QLCEfxPosition> positions = new ArrayList<>();

        if (qlcPointList!=null) {
            final List<QLCEfxFixtureData> fixtureList = getFixtureList();
            final Map<Integer, int[]> fixturePoints = new HashMap<>();

            for (QLCEfxFixtureData qlcEfxFixtureData: fixtureList)
                fixturePoints.put(qlcEfxFixtureData.getFixture().getId(), new int[]{-1, -1, -1, -1});

            for (QLCPoint qlcPoint : qlcPointList){

                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.PAN)
                    fixturePoints.get(qlcPoint.getFixture().getId())[0] =qlcPoint.getData();
                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.PAN_FINE)
                    fixturePoints.get(qlcPoint.getFixture().getId())[1] =qlcPoint.getData();
                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.TILT)
                    fixturePoints.get(qlcPoint.getFixture().getId())[2] =qlcPoint.getData();
                if (qlcPoint.getChannelType() == QLCFixture.ChannelType.TILT_FINE)
                    fixturePoints.get(qlcPoint.getFixture().getId())[3] =qlcPoint.getData();
            }

            for (Map.Entry<Integer, int[]> entry: fixturePoints.entrySet())
                positions.add(QLCEfxPosition.builder()
                        .index(entry.getKey())
                        .pan(entry.getValue()[0], entry.getValue()[1])
                        .tilt(entry.getValue()[2], entry.getValue()[3])
                        .build());
        }

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
