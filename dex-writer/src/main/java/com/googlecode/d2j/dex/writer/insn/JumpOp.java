package com.googlecode.d2j.dex.writer.insn;

import java.nio.ByteBuffer;

import com.googlecode.d2j.dex.writer.CodeWriter;
import com.googlecode.d2j.reader.Op;

public class JumpOp extends OpInsn {
    final int a;
    final int b;
    final Label label;

    public JumpOp(Op op, int a, int b, Label label) {
        super(op);
        switch (op.format) {
            case kFmt31t:
            case kFmt21t:
                CodeWriter.checkRegAA(op, "vAA", a);
                break;
            case kFmt22t:
                CodeWriter.checkRegA(op, "vA", a);
                CodeWriter.checkRegA(op, "vB", b);
                break;
            default:
        }
        this.label = label;
        this.a = a;
        this.b = b;
    }

    @Override
    public void write(ByteBuffer out) {
        out.put((byte) op.opcode);
        int offset = label.offset - this.offset;
        switch (op.format) {
            case kFmt10t: // AA|op
                CodeWriter.checkContentByte(op, "+AA", offset);
                out.put((byte) offset);
                break;
            case kFmt20t: // ØØ|op AAAA
                CodeWriter.checkContentShort(op, "+AAAA", offset);
                out.put((byte) 0).putShort((short) offset);
                break;
            case kFmt30t: // ØØ|op AAAAlo AAAAhi
                out.put((byte) 0).putInt(offset);
                break;
            case kFmt31t: // AA|op BBBBlo BBBBhi
                out.put((byte) a).putInt(offset);
                break;
            case kFmt22t: // B|A|op CCCC
                CodeWriter.checkContentShort(op, "+CCCC", offset);
                out.put((byte) ((a & 0xF) | (b << 4))).putShort((short) offset);
                break;
            case kFmt21t: // AA|op BBBB
                CodeWriter.checkContentShort(op, "+BBBB", offset);
                out.put((byte) a).putShort((short) offset);
                break;
            default:
                throw new RuntimeException("not support");
        }
    }
}