/*
 * dex2jar - Tools to work with android .dex and java .class files
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
package com.googlecode.dex2jar.v3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.reader.io.DataIn;
import com.googlecode.dex2jar.visitors.DexClassVisitor;

public class Dex2jar {
    public static Dex2jar from(byte[] in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Dex2jar from(DataIn in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Dex2jar from(DexFileReader reader) {
        return new Dex2jar(reader);
    }

    public static Dex2jar from(File in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Dex2jar from(InputStream in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Dex2jar from(String in) throws IOException {
        return from(new File(in));
    }

    private DexExceptionHandler exceptionHandler;

    final private DexFileReader reader;
    private int readerConfig;
    private boolean verbose = false;
    private int v3Config;

    private Dex2jar(DexFileReader reader) {
        super();
        this.reader = reader;
        readerConfig |= DexFileReader.SKIP_DEBUG;
    }

    private void doTranslate(final Object dist) throws IOException {
        if (reader.isOdex()) {
            throw new DexException("dex-translator not support translate an odex file,"
                    + " please refere smali http://code.google.com/p/smali/ to convert odex to dex");
        }

        V3InnerClzGather afa = new V3InnerClzGather();
        reader.accept(afa, DexFileReader.SKIP_CODE | DexFileReader.SKIP_DEBUG);
        try {
            reader.accept(new V3(afa.getClasses(), exceptionHandler, new ClassVisitorFactory() {
                @Override
                public ClassVisitor create(final String name) {
                    return new ClassWriter(ClassWriter.COMPUTE_MAXS) {
                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            try {
                                byte[] data = this.toByteArray();
                                saveTo(data, name, dist);
                            } catch (IOException e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    };
                }
            }, v3Config) {

                @Override
                public DexClassVisitor visit(int access_flags, String className, String superClass,
                        String[] interfaceNames) {
                    if (verbose) {
                        System.err.println("Processing " + className);
                    }
                    return super.visit(access_flags, className, superClass, interfaceNames);
                }

            }, readerConfig);
        } catch (Exception e) {
            if (exceptionHandler == null) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            } else {
                exceptionHandler.handleFileException(e);
            }
        }
    }

    public DexExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public DexFileReader getReader() {
        return reader;
    }

    public Dex2jar reUseReg(boolean b) {
        if (b) {
            this.v3Config |= V3.REUSE_REGISTER;
        } else {
            this.v3Config &= ~V3.REUSE_REGISTER;
        }
        return this;
    }

    public Dex2jar topoLogicalSort(boolean b) {
        if (b) {
            this.v3Config |= V3.TOPOLOGICAL_SORT;
        } else {
            this.v3Config &= ~V3.TOPOLOGICAL_SORT;
        }
        return this;
    }

    public Dex2jar verbose() {
        this.verbose = true;
        return this;
    }

    public Dex2jar verbose(boolean b) {
        this.verbose = b;
        return this;
    }

    public Dex2jar optimizeSynchronized(boolean b) {
        if (b) {
            this.v3Config |= V3.OPTIMIZE_SYNCHRONIZED;
        } else {
            this.v3Config &= ~V3.OPTIMIZE_SYNCHRONIZED;
        }
        return this;
    }

    public Dex2jar printIR(boolean b) {
        if (b) {
            this.v3Config |= V3.PRINT_IR;
        } else {
            this.v3Config &= ~V3.PRINT_IR;
        }
        return this;
    }

    public Dex2jar reUseReg() {
        this.v3Config |= V3.REUSE_REGISTER;
        return this;
    }

    public Dex2jar optimizeSynchronized() {
        this.v3Config |= V3.OPTIMIZE_SYNCHRONIZED;
        return this;
    }

    public Dex2jar printIR() {
        this.v3Config |= V3.PRINT_IR;
        return this;
    }

    public Dex2jar topoLogicalSort() {
        this.v3Config |= V3.TOPOLOGICAL_SORT;
        return this;
    }

    private Set<String> dirs = new HashSet<String>();

    private void check(String dir, ZipOutputStream zos) throws IOException {
        if (dirs.contains(dir)) {
            return;
        }
        dirs.add(dir);
        int i = dir.lastIndexOf('/');
        if (i > 0) {
            check(dir.substring(0, i), zos);
        }
        zos.putNextEntry(new ZipEntry(dir + "/"));
        zos.closeEntry();
    }

    private void saveTo(byte[] data, String name, Object dist) throws IOException {
        if (dist instanceof ZipOutputStream) {
            ZipOutputStream zos = (ZipOutputStream) dist;
            ZipEntry entry = new ZipEntry(name + ".class");
            int i = name.lastIndexOf('/');
            if (i > 0) {
                check(name.substring(0, i), zos);
            }
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
        } else {
            File dir = (File) dist;
            FileUtils.writeByteArrayToFile(new File(dir, name + ".class"), data);
        }
    }

    public void setExceptionHandler(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public Dex2jar skipDebug(boolean b) {
        if (b) {
            this.readerConfig |= DexFileReader.SKIP_DEBUG;
        } else {
            this.readerConfig &= ~DexFileReader.SKIP_DEBUG;
        }
        return this;
    }

    public Dex2jar skipDebug() {
        this.readerConfig |= DexFileReader.SKIP_DEBUG;
        return this;
    }

    public void to(File file) throws IOException {
        if (file.exists() && file.isDirectory()) {
            doTranslate(file);
        } else {
            OutputStream fos = FileUtils.openOutputStream(file);
            try {
                to(fos);
            } finally {
                IOUtils.closeQuietly(fos);
            }
        }
    }

    public void to(OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        doTranslate(zos);
        zos.finish();
    }

    public void to(String file) throws IOException {
        to(new File(file));
    }

    public Dex2jar withExceptionHandler(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }
}
