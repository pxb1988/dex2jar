/*
 * Copyright (c) 2009-2012 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.d2j.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;

/**
 * similar with org.objectweb.asm.util.ASMifierClassVisitor
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class ASMifierFileV extends DexFileVisitor {

    String pkgName = "dex2jar.gen";
    Path dir;
    ArrayOut file = new ArrayOut();
    int i = 0;

    public static void doData(byte[] data, Path destdir) throws IOException {
        new DexFileReader(data).accept(new ASMifierFileV(destdir, null));
    }

    public static void doFile(Path srcDex) throws IOException {
        String distName = srcDex.getFileName() + "_asmifier";
        doFile(srcDex, srcDex.resolveSibling(distName));
    }

    public static void doFile(Path srcDex, Path dest) throws IOException {
        doData(ZipUtil.readDex(srcDex), dest);
    }

    public static void main(String... args) throws IOException {
        if (args.length < 1) {
            System.out.println("ASMifier 1.dex 2.dex ... n.dex");
            return;
        }
        for (String s : args) {
            System.out.println("asmifier " + s);
            doFile(new File(s).toPath());
        }
    }

    public ASMifierFileV(Path dir, String pkgName) {
        super();
        if (dir == null) {
            this.dir = new File(".").toPath();
        } else {
            this.dir = dir;
        }
        if (pkgName != null) {
            this.pkgName = pkgName;
        }
        file.s("package %s;", this.pkgName);
        file.s("import com.googlecode.d2j.*;");
        file.s("import com.googlecode.dj2.visitors.*;");
        file.s("public class Main {");
        file.push();
        file.s("public static void accept(DexFileVisitor v) {");
        file.push();

    }

    static void write(ArrayOut out, Path file) {
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<String>(out.array.size());
        for (int i = 0; i < out.array.size(); i++) {
            sb.setLength(0);
            int p = out.is.get(i);
            for (int j = 0; j < p; j++) {
                sb.append("    ");
            }
            sb.append(out.array.get(i));
            list.add(sb.toString());
        }
        try {
            Path parent = file.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.write(file, list, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
        final String n = String.format("C%04d_", i++)
                + className.substring(1, className.length() - 1).replace('/', '_').replace('$', '_');
        file.s("%s.accept(v);", n);
        return new ASMifierClassV(pkgName, n, access_flags, className, superClass, interfaceNames) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                write(out, dir.resolve(pkgName.replace('.', '/') + '/' + n + ".java"));
            }

        };
    }

    @Override
    public void visitEnd() {
        file.pop();
        file.s("}");
        file.pop();
        file.s("}");
        write(file, dir.resolve(pkgName.replace('.', '/') + "/Main.java"));
    }
}
