package com.googlecode.dex2jar.xir;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.DexOpcodeDump;
import com.googlecode.dex2jar.util.AbstractDumpDexCodeAdapter;

public class NodeDump extends AbstractDumpDexCodeAdapter {
    StringBuilder sb = new StringBuilder();

    @Override
    protected void info(int opcode, String format, Object... args) {
        String s = String.format(format, args);
        if (opcode < 0) {
            sb.append(String.format("%-20s|%5s|%s\n", "", "", s));
        } else {
            sb.append(String.format("%-20s|%5s|%s\n", DexOpcodeDump.dump(opcode), "", s));
        }
    }

    @Override
    public void visitLabel(DexLabel label) {
        sb.append(String.format("%-20s|%5s:\n", "LABEL", labelToString(label)));
    }

    @Override
    protected String labelToString(DexLabel label) {
        return label.toString();
    }

    public String toString() {
        return sb.toString();
    }
}
