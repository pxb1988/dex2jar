/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.d2j.smali;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;

public class Baksmali {
    private Baksmali() {
    }

    public static Baksmali from(byte[] in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Baksmali from(ByteBuffer in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Baksmali from(DexFileReader reader) {
        return new Baksmali(reader);
    }

    public static Baksmali from(File in) throws IOException {
        return from(ZipUtil.readDex(in));
    }

    public static Baksmali from(Path in) throws IOException {
        return from(ZipUtil.readDex(in));
    }

    public static Baksmali from(InputStream in) throws IOException {
        return from(ZipUtil.readDex(in));
    }

    public static Baksmali from(String in) throws IOException {
        return from(new File(in));
    }

    boolean noDebug = false;
    boolean parameterRegisters = true;
    DexFileReader reader;
    boolean useLocals = false;

    private Baksmali(DexFileReader reader) {
        this.reader = reader;
    }

    /**
     * <pre>
     * -b,--no-debug-info don't write out debug info (.local, .param, .line, etc.)
     * </pre>
     * 
     * @return
     */
    public Baksmali noDebug() {
        this.noDebug = true;
        return this;
    }

    /**
     * <pre>
     *  -p,--no-parameter-registers use the v<n> syntax instead of the p<n> syntax for registers mapped to method parameters
     * </pre>
     * 
     * @return
     */
    public Baksmali noParameterRegisters() {
        this.parameterRegisters = false;
        return this;
    }

    public void to(final File dir) {
        to(dir.toPath());
    }

    public void to(final Path base) {
        final BaksmaliDumper bs = new BaksmaliDumper(parameterRegisters, useLocals);
        reader.accept(new BaksmaliDexFileVisitor(base, bs), this.noDebug ? DexFileReader.SKIP_CODE : 0);
    }

    /**
     * <pre>
     *  -l,--use-locals output the .locals directive with the number of non-parameter registers, rather than the .register
     * </pre>
     * 
     * @return
     */
    public Baksmali useLocals() {
        this.useLocals = true;
        return this;
    }

}
