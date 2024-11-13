package co.mahmm.tokyo.commons;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class FileReader {

    public static String readFile(String path) {
        if(path == null) {
            throw new RuntimeException("File name cannot be null");
        }
        if(path.startsWith("file://")) {
            return new String(readFiles(path));
        }
        return new String(readClassPath(path));
    }

    private static byte[] readClassPath(String path) {
        try {
            URL resource = FileReader.class.getClassLoader().getResource(path);
            byte[] bytes = FileUtils.readFileToByteArray(new File(resource.toURI()));
            return bytes;
        }catch (Exception e) {
            throw new RuntimeException("Unable to read file = " + path, e);
        }
    }

    private static byte[] readFiles(String path) {
        try {
            path = path.replace("file://", "");
            byte[] bytes = FileUtils.readFileToByteArray(new File(path));
            return bytes;
        }catch (Exception e) {
            throw new RuntimeException("Unable to read file = " + path, e);
        }
    }

}
