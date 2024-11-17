package io.github.imhmg.tokyo.commons;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class FileReader {

    public static String readFile(String path) {
        if (path == null) {
            throw new RuntimeException("File name cannot be null");
        }
        String absPath = getAbsPathIfExists(path);
        if (absPath != null) {
            return new String(readFiles(absPath));
        }
        return new String(readClassPath(path));
    }

    private static byte[] readClassPath(String path) {
        try {
            URL resource = FileReader.class.getClassLoader().getResource(path);
            byte[] bytes = FileUtils.readFileToByteArray(new File(resource.toURI()));
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read file = " + path, e);
        }
    }

    private static byte[] readFiles(String path) {
        try {
            byte[] bytes = FileUtils.readFileToByteArray(new File(path));
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read file = " + path, e);
        }
    }

    private static String getAbsPathIfExists(String path) {
        if (new File(path).exists()) {
            return new File(path).getAbsolutePath();
        }
        String TKY_BASE_DIR = System.getenv("TKY_BASE_DIR");
        if (TKY_BASE_DIR == null) {
            return null;
        }
        if(FileUtils.getFile(TKY_BASE_DIR, path).exists()) {
            return FileUtils.getFile(TKY_BASE_DIR, path).getAbsolutePath();
        }
        return null;
    }

}
