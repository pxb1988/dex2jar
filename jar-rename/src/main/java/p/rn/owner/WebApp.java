package p.rn.owner;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class WebApp {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("webapp pathToWebApp config [ignoreJarConfig]");
            return;
        }

        File webApp = new File(args[0]);
        File config = new File(args[1]);
        File jarIgnore = args.length > 2 ? new File(args[2]) : null;

        File clz = new File(webApp, "WEB-INF/classes");
        File tmpClz = new File(webApp, "WEB-INF/tmp-classes");
        ReOwner ro = new ReOwner().withConfig(config);

        if (tmpClz.exists()) {
            FileUtils.deleteDirectory(tmpClz);
        }
        FileUtils.copyDirectory(clz, tmpClz);
        System.out.println("ReOwner from [" + tmpClz + "] to [" + clz + "]");
        ro.reOwner(tmpClz, clz);
        FileUtils.deleteDirectory(tmpClz);

        File lib = new File(webApp, "WEB-INF/lib");
        File tmpLib = new File(webApp, "WEB-INF/Nlib");

        Set<String> ignores = new HashSet<String>();
        if (jarIgnore != null && jarIgnore.exists()) {
            ignores.addAll(FileUtils.readLines(jarIgnore, "UTF-8"));
        } else {
            System.out.println("ignoreJarConfig ignored");
        }
        if (tmpLib.exists()) {
            FileUtils.deleteDirectory(tmpLib);
        }
        FileUtils.copyDirectory(lib, tmpLib);
        for (File jar : FileUtils.listFiles(tmpLib, new String[] { "jar" }, false)) {
            final String s = jar.getName();
            boolean ignore = false;
            for (String i : ignores) {
                if (s.startsWith(i)) {
                    ignore = true;
                    break;
                }
            }
            if (!ignore) {
                File nJar = new File(lib, s);
                System.out.println("ReOwner from [" + jar + "] to [" + nJar + "]");
                ro.reOwner(jar, nJar);
            }
        }
        FileUtils.deleteDirectory(tmpLib);
    }
}
