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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.reader.io.DataIn;

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

    private int flags = DexFileReader.SKIP_DEBUG;

    final private DexFileReader reader;

    private Dex2jar(DexFileReader reader) {
        super();
        this.reader = reader;
    }

    public Dex2jar withExceptionHandler(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public Dex2jar withFlags(int flags) {
        this.flags = flags;
        return this;
    }

    private void doTranslate(final Object dist) throws IOException {
        if (reader.isOdex()) {
            throw new DexException("dex-translator not support translate an odex file,"
                    + " please refere smali http://code.google.com/p/smali/ to convert odex to dex");
        }

        V3AccessFlagsAdapter afa = new V3AccessFlagsAdapter();
        reader.accept(afa, DexFileReader.SKIP_CODE | DexFileReader.SKIP_DEBUG);
        try {
            reader.accept(new V3(afa.getAccessFlagsMap(), afa.getInnerNameMap(), afa.getExtraMember(),
                    exceptionHandler, new ClassVisitorFactory() {
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
                    }), flags);
        } catch (Exception e) {
            if (exceptionHandler == null) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            } else {
                exceptionHandler.handleFileException(e);
            }
        }
    }

    private void saveTo(byte[] data, String name, Object dist) throws IOException {
        if (dist instanceof ZipOutputStream) {
            ZipOutputStream zos = (ZipOutputStream) dist;
            ZipEntry entry = new ZipEntry(name + ".class");
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
        } else {
            File dir = (File) dist;
            FileUtils.writeByteArrayToFile(new File(dir, name + ".class"), data);
        }
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

    public DexExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public DexFileReader getReader() {
        return reader;
    }
}
