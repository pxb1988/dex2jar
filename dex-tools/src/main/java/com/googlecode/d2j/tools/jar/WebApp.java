package com.googlecode.d2j.tools.jar;

import com.googlecode.dex2jar.tools.BaseCmd;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

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
        Path jarIgnore = args.length > 2 ? new File(args[2]).toPath() : null;

        Path clz = new File(webApp, "WEB-INF/classes").toPath();
        Path tmpClz = new File(webApp, "WEB-INF/tmp-classes").toPath();
        final InvocationWeaver ro = (InvocationWeaver) new InvocationWeaver().withConfig(config.toPath());
        Files.deleteIfExists(tmpClz);
        copyDirectory(clz, tmpClz);

        System.out.println("InvocationWeaver from [" + tmpClz + "] to [" + clz + "]");
        ro.wave(tmpClz, clz);
        Files.deleteIfExists(tmpClz);

        final File lib = new File(webApp, "WEB-INF/lib");
        Path tmpLib = new File(webApp, "WEB-INF/Nlib").toPath();

        final Set<String> ignores = new HashSet<String>();
        if (jarIgnore != null && Files.exists(jarIgnore)) {
            ignores.addAll(Files.readAllLines(jarIgnore, StandardCharsets.UTF_8));
        } else {
            System.out.println("ignoreJarConfig ignored");
        }

        Files.deleteIfExists(tmpLib);
        copyDirectory(lib.toPath(), tmpLib);

        Files.walkFileTree(tmpLib, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".jar")) {
                    final String s = file.getFileName().toString();
                    boolean ignore = false;
                    for (String i : ignores) {
                        if (s.startsWith(i)) {
                            ignore = true;
                            break;
                        }
                    }
                    if (!ignore) {
                        Path nJar = new File(lib, s).toPath();
                        System.out.println("InvocationWeaver from [" + file + "] to [" + nJar + "]");
                        ro.wave(file, nJar);
                    }
                }
                return super.visitFile(file, attrs);
            }
        });
        Files.deleteIfExists(tmpLib);
    }

    private static void copyDirectory(final Path clz, final Path tmpClz) throws IOException {
        Files.walkFileTree(clz, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path n = clz.relativize(file);
                Path target = tmpClz.resolve(n);
                BaseCmd.createParentDirectories(target);
                Files.copy(file, target);
                return super.visitFile(file, attrs);
            }
        });
    }
}
