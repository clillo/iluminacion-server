package cl.clillo.lighting.utils;

public class SystemUtils {

    public enum SO { WINDOWS, MAC_OS, UBUNTU }

    public static SO getSO(){
        final String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("windows"))
            return SO.WINDOWS;

        if (os.toLowerCase().startsWith("mac"))
            return SO.MAC_OS;

        return SO.UBUNTU;
    }
}
