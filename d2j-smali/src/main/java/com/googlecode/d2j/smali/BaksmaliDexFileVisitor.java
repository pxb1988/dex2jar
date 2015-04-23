package com.googlecode.d2j.smali;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class BaksmaliDexFileVisitor extends DexFileVisitor {
    private final Path dir;
    private final BaksmaliDumper bs;
    private Set<String> hases;
    private int i;

    public BaksmaliDexFileVisitor(Path dir, BaksmaliDumper bs) {
        this.dir = dir;
        this.bs = bs;
        hases = new HashSet<String>();
        i = 1;
    }

    protected String rebuildFileName(String s) {
        s = BaksmaliDumper.escapeId(s);
        s = s.replace('\\', '-');
        String low = s.toLowerCase();
        if (hases.contains(low)) {
            return s + "_d2j" + i++;
        } else {
            hases.add(low);
        }
        return s;
    }

    @Override
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
        return new DexClassNode(access_flags, className, superClass, interfaceNames) {

            @Override
            public void visitEnd() {
                super.visitEnd();

                Path smaliFile = dir
                        .resolve(rebuildFileName(className.substring(1, className.length() - 1)) + ".smali");

                try {
                    Path parent = smaliFile.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try (BufferedWriter writer = Files.newBufferedWriter(smaliFile, StandardCharsets.UTF_8)) {
                    BaksmaliDumpOut out = new BaksmaliDumpOut(writer);
                    bs.baksmaliClass(this, out);
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        };
    }
}
