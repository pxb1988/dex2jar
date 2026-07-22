package com.googlecode.d2j.smali;

import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public final class Baksmali {

    boolean noDebug = false;

    boolean parameterRegisters = true;

    DexFileReader reader;

    boolean useLocals = false;

    private Baksmali() {
    }

    private Baksmali(DexFileReader reader) {
        this.reader = reader;
    }

    /**
     * <pre>
     * -b,--no-debug-info don't write out debug info (.local, .param, .line, etc.)
     * </pre>
     */
    public Baksmali noDebug() {
        this.noDebug = true;
        return this;
    }

    /**
     * <pre>
     *  -p,--no-parameter-registers use the v&lt;n&gt; syntax instead of the p&lt;n&gt; syntax for registers
     *  mapped to method parameters
     * </pre>
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
     *  -l,--use-locals output the .locals directive with the number of non-parameter registers,
     *  rather than the .register
     * </pre>
     */
    public Baksmali useLocals() {
        this.useLocals = true;
        return this;
    }

    public static Baksmali from(byte[] in) {
        return from(new DexFileReader(in));
    }

    public static Baksmali from(ByteBuffer in) {
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

}
