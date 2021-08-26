package com.googlecode.dex2jar.bin_gen;

import com.googlecode.dex2jar.tools.BaseCmd;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;

public final class BinGen {

    private BinGen() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public static void main(String... args) throws IOException {
        if (args.length < 2) {
            System.err.println("bin-gen cfg-dir out-dir");
            return;
        }
        final Path cfg = new File(args[0]).toPath();
        final Path out = new File(args[1]).toPath();
        Properties p = new Properties();
        try (InputStream is = Files.newInputStream(cfg.resolve("class.cfg"))) {
            p.load(is);
        }

        String bat = new String(Files.readAllBytes(cfg.resolve("bat_template")), StandardCharsets.UTF_8);

        String sh = new String(Files.readAllBytes(cfg.resolve("sh_template")), StandardCharsets.UTF_8);

        Files.walkFileTree(cfg, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".sh") || fileName.endsWith(".bat")) {
                    Path f = out.resolve(cfg.relativize(file));
                    BaseCmd.createParentDirectories(f);
                    Files.copy(file, f, StandardCopyOption.REPLACE_EXISTING);
                    if (fileName.endsWith(".sh")) {
                        setExec(f);
                    }
                }
                return super.visitFile(file, attrs);
            }
        });

        for (Object key : p.keySet()) {
            String name = key.toString();
            Path path = out.resolve(key + ".sh");
            BaseCmd.createParentDirectories(path);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                String s = sh.replaceAll("__@class_name@__", p.getProperty(name));
                bw.append(s);
            }

            setExec(path);

            path = out.resolve(key + ".bat");
            BaseCmd.createParentDirectories(path);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                String s = bat.replaceAll("__@class_name@__", p.getProperty(name));
                bw.append(s);
            }
        }
    }

    private static void setExec(Path path) {
        try {
            path.toFile().setExecutable(true);
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (Exception ex) {
            // ignored
        }
    }

}
