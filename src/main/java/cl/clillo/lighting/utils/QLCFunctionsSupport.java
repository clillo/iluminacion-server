package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.ShowCollection;

import java.util.List;

public class QLCFunctionsSupport {


    public void readFunction(final String type){
        final List<QLCFunction> list = ShowCollection.getInstance().getOriginalFunctionList(type);
        for (QLCFunction function: list){
            System.out.println(function.getPath()+"\t"+function.getId()+"\t"+function.getName());
           // function.writeToConfigFile("src/main/resources/qlc");
        }

    }

    public void readFunction(final String type, final String path){
        final List<QLCFunction> list = ShowCollection.getInstance().getOriginalFunctionList(type, path);
        for (QLCFunction function: list){
            System.out.println(function.getPath()+"\t"+function.getId()+"\t"+function.getName());
             function.writeToConfigFile("src/main/resources/qlc");
        }

    }

}
