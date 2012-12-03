package p.rn.name;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RenamerMain {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File srcJar = new File("org.jar");
        File configDir = new File(".");
        File distDir = new File(".");
        Set<String> config = new TreeSet<String>();
        for (File f : configDir.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".txt")) {
                config.add(f.getName().substring(0, f.getName().length() - ".txt".length()));
            }
        }
        config.remove("init");
        config.remove("org");
        File lastJar = srcJar;
        List<String> s = new ArrayList<String>(config.size() + 1);
        s.add("init");
        s.addAll(config);
        for (String f : s) {
            File thisJarFile = new File(distDir, f + ".jar");
            new Renamer().from(lastJar).withConfig(new File(configDir, f + ".txt")).to(thisJarFile);
            System.out.println(thisJarFile.getCanonicalPath());
            lastJar = thisJarFile;
        }
    }
}
