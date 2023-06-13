package cl.clillo.lighting.model;

import lombok.ToString;

import java.util.List;

@ToString
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
                    .append(point.getValue()).append('}');
        return sb.toString();
    }
}
