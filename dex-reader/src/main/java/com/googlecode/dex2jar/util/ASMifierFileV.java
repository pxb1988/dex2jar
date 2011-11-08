/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;

/**
 * similar with org.objectweb.asm.util.ASMifierClassVisitor
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class ASMifierFileV implements DexFileVisitor {

    String pkgName = "dex2jar.gen";
    File dir;
    ArrayOut file = new ArrayOut();

    public static void doData(byte[] data, File destdir) throws IOException {
        new DexFileReader(data).accept(new ASMifierFileV(destdir, null));
    }

    public static void doFile(File srcDex) throws IOException {
        doFile(srcDex, new File(srcDex.getParentFile(), srcDex.getName() + "_asmifier"));
    }

    public static void doFile(File srcDex, File destDir) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(srcDex);
        // checkMagic
        if ("dex".equals(new String(data, 0, 3))) {// dex
            doData(data, destDir);
        } else if ("PK".equals(new String(data, 0, 2))) {// ZIP
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                if (entry.getName().equals("classes.dex")) {
                    data = IOUtils.toByteArray(zis);
                    doData(data, destDir);
                }
            }
        } else {
            throw new RuntimeException("the src file not a .dex file or a zip file");
        }
    }

    public static void main(String... args) throws IOException {
        if (args.length < 2) {
            System.out.println("ASMifier 1.dex 2.dex ... n.dex");
            return;
        }
        for (String s : args) {
            System.out.println("asmifier " + s);
            doFile(new File(s));
        }
    }

    public ASMifierFileV(File dir, String pkgName) {
        super();
        if (dir == null) {
            this.dir = new File(".");
        } else {
            this.dir = dir;
        }
        if (pkgName != null) {
            this.pkgName = pkgName;
        }
        file.s("package %s;", this.pkgName);
        file.s("import com.googlecode.dex2jar.*;");
        file.s("import com.googlecode.dex2jar.visitors.*;");
        file.s("public class Main {");
        file.push();
        file.s("public static void accept(DexFileVisitor v) {");
        file.push();

    }

    static void write(ArrayOut out, File file) {
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
            FileUtils.writeLines(file, "UTF-8", list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
        final String n = className.substring(1, className.length() - 1).replace('/', '_').replace('$', '_');
        file.s("%s.accept(v);", n);
        return new ASMifierClassV(pkgName, n, access_flags, className, superClass, interfaceNames) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                write(out, new File(dir, pkgName.replace('.', '/') + '/' + n + ".java"));
            }

        };
    }

    @Override
    public void visitEnd() {
        file.pop();
        file.s("}");
        file.pop();
        file.s("}");
        write(file, new File(dir, pkgName.replace('.', '/') + "/Main.java"));
    }

}
