package cl.clillo.lighting.model;

import lombok.ToString;

import java.util.List;

@ToString
public class QLCSequence extends QLCFunction{

    private final QLCDirection direction;
    private final QLCRunOrder runOrder;
    private final List<QLCStep> qlcStepList;

    public QLCSequence(final int id, final String type, final String name, final String path,
                       final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList) {
        super(id, type, name, path);
        this.qlcStepList = qlcStepList;
        this.direction = direction;
        this.runOrder = runOrder;
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());
        sb.append(direction).append('\t');
        sb.append(runOrder).append('\t');

        for (QLCStep scene: qlcStepList)
            sb.append('{')
                    .append(scene.getFadeIn()).append(',')
                    .append(scene.getHold()).append(',')
                    .append(scene.getFadeOut()).append(',')
                    .append('}');

        return sb.toString();
    }
}
