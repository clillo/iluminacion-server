package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixtureModel;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class QLCScene extends QLCFunction{

    private List<QLCPoint> qlcPointList;

    public QLCScene(final int id, final String type, final String name, final String path, final List<QLCPoint> qlcPointList) {
        super(id, type, name, path);
        this.qlcPointList = qlcPointList;
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (QLCPoint point: qlcPointList)
            sb.append('{').append(point.getFixture().getId()).append(',')
                    .append(point.getChannel()).append(',')
                    .append(point.getData()).append('}');
        return sb.toString();
    }

    public static QLCScene build(final int id, final List<QLCPoint> qlcPointList){
        String type = "type";
        String path = "path";

        return new QLCScene(id, type, "scene: "+id, path, qlcPointList);
    }
}
