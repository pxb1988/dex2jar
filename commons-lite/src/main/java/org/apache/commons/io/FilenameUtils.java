package org.apache.commons.io;

public class FilenameUtils {
    public static String getBaseName(String n) {
        n = n.replace('\\', '/');
        int i = n.lastIndexOf('/');
        if (i >= 0) {
            n = n.substring(i + 1);
        }
        i = n.lastIndexOf('.');
        if (i >= 0) {
            n = n.substring(0, i);
        }
        return n;
    }
}
