package com.googlecode.d2j.dex.writer.insn;

import java.nio.ByteBuffer;

public class PreBuildInsn extends Insn {

    public final byte[] data;

    public PreBuildInsn(byte[] data) {

        this.data = data;
    }


    @Override
    public int getCodeUnitSize() {
        return data.length / 2;
    }

    @Override
    public void write(ByteBuffer out) {
        out.put(data);
    }

}
