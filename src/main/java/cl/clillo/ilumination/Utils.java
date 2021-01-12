package cl.clillo.ilumination;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static List<File> getDirectories(final String baseName){
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(Objects.requireNonNull(classLoader.getResource(baseName)).getFile());

        final File[] listFiles = Arrays.stream(file.listFiles()).filter(f -> f.isDirectory()).toArray(File[]::new);
        return Lists.newArrayList(listFiles);
    }


    public static List<File> getFiles(final String baseName, final String prefix){
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(Objects.requireNonNull(classLoader.getResource(baseName)).getFile());

        final File[] listFiles = Arrays.stream(file.listFiles()).filter(f -> StringUtils.isNoneEmpty(StringUtils.substringBetween(f.getName(), prefix, ".yml"))).toArray(File[]::new);
        return Lists.newArrayList(listFiles);
    }
}
