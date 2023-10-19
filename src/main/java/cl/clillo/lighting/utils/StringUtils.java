package cl.clillo.lighting.utils;

public class StringUtils {

    public static String buildString3Digits(int value){
        return "000".substring(0, 3-String.valueOf(value).length())+value;
    }
}
