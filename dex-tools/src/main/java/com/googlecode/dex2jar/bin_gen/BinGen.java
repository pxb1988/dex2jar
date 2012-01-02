package com.googlecode.dex2jar.bin_gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class BinGen {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Properties p = new Properties();
        p.load(BinGen.class.getResourceAsStream("class.cfg"));
        String bat = IOUtils.toString(BinGen.class.getResourceAsStream("bat_template"), "UTF-8");
        String sh = IOUtils.toString(BinGen.class.getResourceAsStream("sh_template"), "UTF-8");

        File binDir = new File("src/main/bin");
        FileOutputStream fos = FileUtils.openOutputStream(new File(binDir, "setclasspath.bat"));
        IOUtils.copy(BinGen.class.getResourceAsStream("setclasspath.bat"), fos);
        IOUtils.closeQuietly(fos);
        for (Object key : p.keySet()) {
            String name = key.toString();
            FileUtils.writeStringToFile(new File(binDir, key.toString() + ".sh"),
                    sh.replaceAll("__@class_name@__", p.getProperty(name)), "UTF-8");
            FileUtils.writeStringToFile(new File(binDir, key.toString() + ".bat"),
                    bat.replaceAll("__@class_name@__", p.getProperty(name)), "UTF-8");
        }
        System.out.println("Done.");
    }
}
