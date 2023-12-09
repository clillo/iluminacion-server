package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.QLCSequence;

public class FunctionListWrapper {

    private final QLCFunction function;

    public FunctionListWrapper(QLCFunction function) {
        this.function = function;
    }


    public QLCFunction getFunction() {
        return function;
    }

    @Override
    public String toString() {
        if (function instanceof QLCScene)
            return function.getId()+"\t"+function.getName();

        if (function instanceof QLCCollection)
            return function.getId()+"\t"+function.getName()+"\t"+((QLCCollection)function).getQlcFunctionList().size();

        if (function instanceof QLCSequence)
            return function.getId()+"\t"+function.getName()+"\t("+((QLCSequence)function).getQlcStepList().size()+")";

        return function.getId()+"\t"+"N/A";
    }
}
