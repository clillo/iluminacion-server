package cl.clillo.lighting.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileUtils {

    public static List<File> getDirectories(final String baseName){
        final File file = new File(baseName);

        final File[] listFiles = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(File::isDirectory).toArray(File[]::new);
        return Lists.newArrayList(listFiles);
    }

    public static List<File> getFiles(final String baseName, final String prefix, final String suffix){
        final File file = new File(baseName);

        final File[] listFiles = Arrays.stream(
                Objects
                        .requireNonNull(file.listFiles()))
                .filter(f ->
                        StringUtils.isNoneEmpty(
                                StringUtils.substringBetween(f.getName(), prefix, suffix))).toArray(File[]::new);
        return Lists.newArrayList(listFiles);
    }

    public static File getFile(final String baseName, final String name){
        final File dir = new File(baseName);

        final File file = new File(dir.getAbsolutePath()+"/"+name);
        if (file.exists())
            return file;

        try {
            file.createNewFile();
        }catch (Exception e){

        }
        return new File(dir.getAbsolutePath()+"/"+name);
    }
}