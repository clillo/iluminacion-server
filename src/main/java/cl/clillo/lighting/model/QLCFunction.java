package cl.clillo.lighting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class QLCFunction {

    private int id;
    private String type;
    private String name;
    private String path;

    QLCFunction(int id, String type, String name, String path) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.path = path;
    }

    public String toSmallString(){
        return String.valueOf(this.getId()) + '\t' +
                this.getPath() + '.' + this.getName() + '\t';
    }

    public static QLCFunctionBuilder builder() {
        return new QLCFunctionBuilder();
    }

    public static class QLCFunctionBuilder {
        private int id;
        private String type;
        private String name;
        private String path;
        private QLCDirection direction;
        private QLCRunOrder runOrder;
        private QLCScene boundScene;
        private final List<QLCPoint> qlcPointList = new ArrayList<>();
        private final List<QLCFunction> qlcFunctionList = new ArrayList<>();
        private final List<QLCStep> qlcStepList = new ArrayList<>();

        QLCFunctionBuilder() {
        }

        public QLCFunctionBuilder id(final int id) {
            this.id = id;
            return this;
        }

        public QLCFunctionBuilder type(final String type) {
            this.type = type;
            return this;
        }

        public QLCFunctionBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public QLCFunctionBuilder path(final String path) {
            this.path = path;
            return this;
        }

        public QLCFunctionBuilder boundScene(final QLCScene boundScene) {
            this.boundScene = boundScene;
            return this;
        }

        public void direction(final QLCDirection direction) {
            this.direction = direction;
        }

        public void runOrder(final QLCRunOrder runOrder){
            this.runOrder = runOrder;
        }

        public void addFunctionList(final QLCFunction function) {
            this.qlcFunctionList.add(function);
        }

        public void addPointList(final QLCPoint fixture) {
            this.qlcPointList.add(fixture);
        }

        public QLCFunctionBuilder addStepList(final QLCStep step) {
            this.qlcStepList.add(step);
            return this;
        }

        public QLCFunction build() {
            if ("Scene".equalsIgnoreCase(type))
                return new QLCScene(this.id, this.type, this.name, this.path, this.qlcPointList);

            if ("Collection".equalsIgnoreCase(type))
                return new QLCCollection(this.id, this.type, this.name, this.path, this.qlcFunctionList);

            if ("Sequence".equalsIgnoreCase(type))
                return new QLCSequence(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                        this.qlcStepList, boundScene);

            return new QLCFunction(this.id, this.type, this.name, this.path);
        }

    }
}
