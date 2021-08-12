package com.googlecode.d2j.smali;

import com.googlecode.d2j.util.Out;
import java.io.BufferedWriter;
import java.io.IOException;

public class BaksmaliDumpOut implements Out {

    private final BufferedWriter writer;

    int i;

    final String indent;

    public BaksmaliDumpOut(BufferedWriter writer) {
        this("  ", writer);
    }

    public BaksmaliDumpOut(String indent, BufferedWriter writer) {
        this.writer = writer;
        i = 0;
        this.indent = indent;
    }

    @Override
    public void pop() {
        i--;
    }

    @Override
    public void push() {
        i++;
    }

    @Override
    public void s(String s) {
        try {
            for (int i = 0; i < this.i; i++) {
                writer.append(indent);
            }
            writer.append(s);
            writer.newLine();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void s(String format, Object... arg) {
        try {
            for (int i = 0; i < this.i; i++) {
                writer.append(indent);
            }
            writer.append(String.format(format, arg));
            writer.newLine();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
