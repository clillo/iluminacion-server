package cl.clillo.ilumination.model;

import lombok.ToString;

import java.util.List;

@ToString
public class QLCCollection extends QLCFunction{

    private List<QLCFunction> qlcFunctionList;

    public QLCCollection(final int id, final String type, final String name, final String path, final List<QLCFunction> qlcFunctionList) {
        super(id, type, name, path);
        this.qlcFunctionList = qlcFunctionList;
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (QLCFunction scene: qlcFunctionList)
            sb.append('{').append(scene.getId()).append('}');
        return sb.toString();
    }
}
