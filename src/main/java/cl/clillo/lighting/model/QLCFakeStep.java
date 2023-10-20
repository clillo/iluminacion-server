package cl.clillo.lighting.model;

import java.util.List;

public class QLCFakeStep extends QLCStep{

    QLCFakeStep(int id, int fadeIn, int hold, int fadeOut, List<QLCPoint> pointList) {
        super(id, fadeIn, hold, fadeOut, pointList);
    }
}
