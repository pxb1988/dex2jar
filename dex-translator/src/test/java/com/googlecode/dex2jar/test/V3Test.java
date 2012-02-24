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
package com.googlecode.dex2jar.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.v3.ClassVisitorFactory;
import com.googlecode.dex2jar.v3.Main;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.v3.V3AccessFlagsAdapter;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
public class V3Test {

    @Test
    public void test() throws Exception {
        try {
            for (File f : TestUtils.listTestDexFiles()) {
                System.out.println("dex2jar file " + f);
                File distDir = new File(f.getParentFile(), FilenameUtils.getBaseName(f.getName()) + "_dex2jar");
                doData(DexFileReader.readDex(f), distDir);
            }
        } catch (Exception e) {
            Main.niceExceptionMessage(e, 0);
            throw e;
        }
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void doData(byte[] data, final File destDir) throws IOException {

        final int lineCount = 50;

        DexFileReader reader = new DexFileReader(data);
        V3AccessFlagsAdapter afa = new V3AccessFlagsAdapter();
        final List<String> exes = new ArrayList();
        reader.accept(afa, DexFileReader.SKIP_CODE | DexFileReader.SKIP_DEBUG);
        System.out.flush();
        System.out.write(String.format("%05d ", 0).getBytes(UTF8));
        reader.accept(new V3(afa.getAccessFlagsMap(), afa.getInnerNameMap(), afa.getExtraMember(), null,
                new ClassVisitorFactory() {
                    int count = 0;

                    @Override
                    public ClassVisitor create(final String name) {
                        return new ClassWriter(ClassWriter.COMPUTE_MAXS) {
                            @Override
                            public void visitEnd() {
                                count++;
                                try {
                                    super.visitEnd();
                                    byte[] data = this.toByteArray();
                                    FileUtils.writeByteArrayToFile(new File(destDir, name + ".class"), data);
                                    TestUtils.verify(new ClassReader(data));
                                    System.out.write('.');
                                } catch (Throwable e) {
                                    System.out.write('X');
                                    exes.add(String.format("%05d %s - %s", count - 1, name, e.getMessage()));
                                }
                                if (count % lineCount == 0) {
                                    try {
                                        System.out.write(String.format("\n%05d ", count).getBytes(UTF8));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                System.out.flush();
                            }
                        };
                    }
                }), 0);
        System.out.flush();
        System.out.println();
        if (exes.size() > 0) {
            StringBuilder sb = new StringBuilder("there are ").append(exes.size())
                    .append(" error(s) while translate\n");
            for (String ln : exes) {
                sb.append(ln).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
    }
}
