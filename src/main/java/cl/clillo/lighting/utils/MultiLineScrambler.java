package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.RealPoint;

import java.util.ArrayList;
import java.util.List;

public class MultiLineScrambler {

    public enum Type{CROSS, TOP, BOTTOM, LEFT, RIGHT, EXTERNAL;

        public static Type of(String name){
            for (Type type: values())
                if(type.name().equals(name))
                    return type;
            return null;
        }
    };

    private final RealPoint leftUp;
    private final RealPoint rightDown;
    private final double widthMid;
    private final double heightMid;

    public MultiLineScrambler(final RealPoint leftUp, final RealPoint rightDown) {
        this.leftUp = leftUp;
        this.rightDown = rightDown;
        heightMid = (rightDown.getY() - leftUp.getY())/2.0;
        widthMid = (rightDown.getX() - leftUp.getX())/2.0;
    }

    public List<RealPoint> buildRealPoints(final int loops, final Type type){
        final List<RealPoint> realPoints = new ArrayList<>();
        for (int i=0; i<loops; i++){
            switch (type){
                case TOP:
                    buildTopLines(realPoints);
                    break;
                case CROSS:
                    buildCrossLines(realPoints);
                    break;
                case LEFT:
                    buildLeftLines(realPoints);
                    break;
                case BOTTOM:
                    buildBottomLines(realPoints);
                    break;
                case RIGHT:
                    buildRightLines(realPoints);
                    break;
                case EXTERNAL:
                    buildExternalLines(realPoints);
            }
        }
        return realPoints;
    }

    private void buildCrossLines(final List<RealPoint> realPoints){
        realPoints.add(randomA());
        realPoints.add(randomD());
        realPoints.add(randomF());
        realPoints.add(randomH());
        realPoints.add(randomB());
        realPoints.add(randomC());
        realPoints.add(randomE());
        realPoints.add(randomG());
    }

    private void buildExternalLines(final List<RealPoint> realPoints){
        realPoints.add(randomA());
        realPoints.add(randomC());
        realPoints.add(randomE());
        realPoints.add(randomG());
    }

    private void buildTopLines(final List<RealPoint> realPoints){
        realPoints.add(randomA());
        realPoints.add(randomF());

    }

    private void buildBottomLines(final List<RealPoint> realPoints){
        realPoints.add(randomB());
        realPoints.add(randomE());

    }

    private void buildLeftLines(final List<RealPoint> realPoints){
        realPoints.add(randomH());
        realPoints.add(randomC());

    }

    private void buildRightLines(final List<RealPoint> realPoints){
        realPoints.add(randomG());
        realPoints.add(randomD());

    }

    private RealPoint randomA(){
        return RealPoint.builder().x(leftUp.getX()).y(randomYA()).build();
    }

    private RealPoint randomB(){
        return RealPoint.builder().x(leftUp.getX()).y(randomYB()).build();
    }

    private RealPoint randomC(){
        return RealPoint.builder().x(randomXA()).y(rightDown.getY()).build();
    }

    private RealPoint randomD(){
        return RealPoint.builder().x(randomXB()).y(rightDown.getY()).build();
    }

    private RealPoint randomE(){
        return RealPoint.builder().x(rightDown.getX()).y(randomYB()).build();
    }

    private RealPoint randomF(){
        return RealPoint.builder().x(rightDown.getX()).y(randomYA()).build();
    }

    private RealPoint randomG(){
        return RealPoint.builder().x(randomXB()).y(leftUp.getY()).build();
    }

    private RealPoint randomH(){
        return RealPoint.builder().x(randomXA()).y(leftUp.getY()).build();
    }

    private double randomYA(){
        return leftUp.getY() + heightMid*Math.random();
    }

    private double randomYB(){
        return leftUp.getY() + heightMid + heightMid*Math.random();
    }

    private double randomXA(){
        return leftUp.getX() + widthMid*Math.random();
    }

    private double randomXB(){
        return leftUp.getX() + widthMid + widthMid*Math.random();
    }
}
