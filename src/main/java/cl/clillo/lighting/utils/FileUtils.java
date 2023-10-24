package cl.clillo.lighting.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class FileUtils {

    public static List<File> getDirectories(final String baseName){
        final List<File> dirs = new ArrayList<>();
        final File file = new File(baseName);
        Stack<File> toReview = new Stack<>();

        toReview.push(file);

        while (!toReview.isEmpty()){
            File actualFile = toReview.pop();
            if (actualFile.isDirectory())
                dirs.add(actualFile);

            if (actualFile.listFiles()==null)
                continue;

            for(File f: actualFile.listFiles()){
                if (f.isDirectory())
                    toReview.push(f);
            }
        }

        return dirs;
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

    public static File getDirectory(final String baseName){
        final File dir = new File(baseName);

        if (dir.exists())
            return dir;

        try {
            new File(baseName).mkdirs();
        }catch (Exception e){

        }
        return new File(baseName);
    }
}
