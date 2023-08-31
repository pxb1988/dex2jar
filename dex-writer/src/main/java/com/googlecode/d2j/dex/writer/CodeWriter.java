package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.dex.writer.insn.Insn;
import com.googlecode.d2j.dex.writer.insn.JumpOp;
import com.googlecode.d2j.dex.writer.insn.Label;
import com.googlecode.d2j.dex.writer.insn.OpInsn;
import com.googlecode.d2j.dex.writer.insn.PreBuildInsn;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.CodeItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.dex.writer.item.DebugInfoItem;
import com.googlecode.d2j.dex.writer.item.StringIdItem;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.d2j.reader.InstructionFormat.kFmt10x;
import static com.googlecode.d2j.reader.InstructionFormat.kFmt11x;
import static com.googlecode.d2j.reader.InstructionFormat.kFmt22b;
import static com.googlecode.d2j.reader.InstructionFormat.kFmt22s;
import static com.googlecode.d2j.reader.InstructionFormat.kFmt23x;
import static com.googlecode.d2j.reader.InstructionFormat.kFmt35c;
import static com.googlecode.d2j.reader.InstructionFormat.kFmt3rc;
import static com.googlecode.d2j.reader.Op.BAD_OP;
import static com.googlecode.d2j.reader.Op.CONST;
import static com.googlecode.d2j.reader.Op.CONST_16;
import static com.googlecode.d2j.reader.Op.CONST_HIGH16;
import static com.googlecode.d2j.reader.Op.CONST_STRING;
import static com.googlecode.d2j.reader.Op.CONST_STRING_JUMBO;
import static com.googlecode.d2j.reader.Op.CONST_WIDE_32;

public class CodeWriter extends DexCodeVisitor {

    final CodeItem codeItem;

    final ConstPool cp;

    ByteBuffer b = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);

    int inRegSize;

    int maxOutRegSize = 0;

    List<Insn> ops = new ArrayList<>();

    List<Insn> tailOps = new ArrayList<>();

    int totalReg;

    List<CodeItem.TryItem> tryItems = new ArrayList<>();

    Method owner;

    Map<DexLabel, Label> labelMap = new HashMap<>();

    ClassDataItem.EncodedMethod encodedMethod;

    public CodeWriter(ClassDataItem.EncodedMethod encodedMethod, CodeItem codeItem, Method owner, boolean isStatic,
                      ConstPool cp) {
        this.encodedMethod = encodedMethod;
        this.codeItem = codeItem;
        this.owner = owner;
        int inRegSize = 0;
        if (!isStatic) {
            inRegSize++;
        }
        for (String s : owner.getParameterTypes()) {
            switch (s.charAt(0)) {
            case 'J':
            case 'D':
                inRegSize += 2;
                break;
            default:
                inRegSize++;
                break;
            }
        }
        this.inRegSize = inRegSize;
        this.cp = cp;
    }

    public static void checkContentByte(Op op, String cc, int v) {
        if (v > Byte.MAX_VALUE || v < Byte.MIN_VALUE) {
            throw new CantNotFixContentException(op, cc, v);
        }
    }

    public static void checkContentS4bit(Op op, String name, int v) {
        if (v > 7 || v < -8) { // TODO check
            throw new CantNotFixContentException(op, name, v);
        }
    }

    public static void checkContentShort(Op op, String cccc, int v) {
        if (v > Short.MAX_VALUE || v < Short.MIN_VALUE) {
            throw new CantNotFixContentException(op, cccc, v);
        }
    }

    public static void checkContentU4bit(Op op, String name, int v) {
        if (v > 15 || v < 0) {
            throw new CantNotFixContentException(op, name, v);
        }
    }

    public static void checkContentUByte(Op op, String cc, int v) {
        if (v > 0xFF || v < 0) {
            throw new CantNotFixContentException(op, cc, v);
        }
    }

    public static void checkContentUShort(Op op, String cccc, int v) {
        if (v > 0xFFFF || v < 0) {
            throw new CantNotFixContentException(op, cccc, v);
        }
    }

    public static void checkRegA(Op op, String s, int reg) {
        if (reg > 0xF || reg < 0) {
            throw new CantNotFixContentException(op, s, reg);
        }
    }

    public static void checkRegAA(Op op, String s, int reg) {
        if (reg > 0xFF || reg < 0) {
            throw new CantNotFixContentException(op, s, reg);
        }
    }

    static void checkRegAAAA(Op op, String s, int reg) {
        if (reg > 0xFFFF || reg < 0) {
            throw new CantNotFixContentException(op, s, reg);
        }
    }

    static byte[] copy(ByteBuffer b) {
        int size = b.position();
        byte[] data = new byte[size];
        System.arraycopy(b.array(), 0, data, 0, size);
        return data;
    }

    public void add(Insn insn) {
        ops.add(insn);
    }

    private byte[] build10x(Op op) {
        b.position(0);
        b.put((byte) op.opcode).put((byte) 0);

        return copy(b);
    }

    // B|A|op
    private byte[] build11n(Op op, int vA, int b) {
        checkRegA(op, "vA", vA);
        checkContentS4bit(op, "#+B", b);
        this.b.position(0);
        this.b.put((byte) op.opcode).put((byte) ((vA & 0xF) | (b << 4)));
        return copy(this.b);
    }

    // AA|op
    private byte[] build11x(Op op, int vAA) {
        checkRegAA(op, "vAA", vAA);
        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA);

        return copy(b);
    }

    // B|A|op
    private byte[] build12x(Op op, int vA, int vB) {
        checkRegA(op, "vA", vA);
        checkRegA(op, "vB", vB);
        b.position(0);
        b.put((byte) op.opcode).put((byte) ((vA & 0xF) | (vB << 4)));

        return copy(b);
    }

    // AA|op BBBB
    private byte[] build21h(Op op, int vAA, Number value) {
        checkRegAA(op, "vAA", vAA);
        int realV;
        if (op == CONST_HIGH16) { // op vAA, #+BBBB0000
            int v = value.intValue();
            if ((v & 0xFFFF) != 0) {
                throw new CantNotFixContentException(op, "#+BBBB0000", v);
            }
            realV = v >> 16;

        } else { // CONST_WIDE_HIGH16 //op vAA, #+BBBB000000000000
            long v = value.longValue();
            if ((v & 0x0000FFFFffffFFFFL) != 0) {
                throw new CantNotFixContentException(op, "#+BBBB000000000000", v);
            }
            realV = (int) (v >> 48);
        }
        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA).putShort((short) realV);
        return copy(b);
    }

    // AA|op BBBB
    private byte[] build21s(Op op, int vAA, Number value) {
        checkRegAA(op, "vAA", vAA);
        int realV;
        if (op == CONST_16) {
            realV = value.intValue();
            checkContentShort(op, "#+BBBB", realV);
        } else { // CONST_WIDE_16
            long v = value.longValue();
            if (v > Short.MAX_VALUE || v < Short.MIN_VALUE) {
                throw new CantNotFixContentException(op, "#+BBBB", v);
            }
            realV = (int) v;
        }
        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA).putShort((short) realV);
        return copy(b);
    }

    // AA|op CC|BB
    private byte[] build22b(Op op, int vAA, int vBB, int cc) {
        checkRegAA(op, "vAA", vAA);
        checkRegAA(op, "vBB", vBB);
        checkContentByte(op, "#+CC", cc);

        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA).put((byte) vBB).put((byte) cc);
        return copy(b);
    }

    // B|A|op CCCC
    private byte[] build22s(Op op, int a, int b, int cccc) {
        checkRegA(op, "vA", a);
        checkRegA(op, "vB", b);
        checkContentShort(op, "+CCCC", cccc);

        this.b.position(0);
        this.b.put((byte) op.opcode).put((byte) ((a & 0xF) | (b << 4))).putShort((short) cccc);
        return copy(this.b);
    }

    // AA|op BBBB
    private byte[] build22x(Op op, int vAA, int vBBBB) {
        checkRegAA(op, "vAA", vAA);
        checkRegAAAA(op, "vBBBB", vBBBB);
        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA).putShort((short) vBBBB);
        return copy(b);
    }

    // AA|op CC|BB
    private byte[] build23x(Op op, int vAA, int vBB, int vCC) {
        checkRegAA(op, "vAA", vAA);
        checkRegAA(op, "vBB", vBB);
        checkRegAA(op, "vCC", vCC);
        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA).put((byte) vBB).put((byte) vCC);
        return copy(b);
    }

    // AA|op BBBBlo BBBBhi
    private byte[] build31i(Op op, int vAA, Number value) {
        checkRegAA(op, "vAA", vAA);
        int realV;
        if (op == CONST) {
            realV = value.intValue();
        } else if (op == CONST_WIDE_32) {
            long v = value.longValue();
            if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
                throw new CantNotFixContentException(op, "#+BBBBBBBB", v);
            }
            realV = (int) v;
        } else {
            throw new RuntimeException();
        }
        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA).putInt(realV);
        return copy(b);
    }

    // ØØ|op AAAA BBBB
    private byte[] build32x(Op op, int vAAAA, int vBBBB) {
        checkRegAAAA(op, "vAAAA", vAAAA);
        checkRegAAAA(op, "vBBBB", vBBBB);
        b.position(0);
        b.put((byte) op.opcode).put((byte) 0).putShort((short) vAAAA).putShort((short) vBBBB);
        return copy(b);
    }

    // AA|op BBBBlo BBBB BBBB BBBBhi
    private byte[] build51l(Op op, int vAA, Number value) {
        checkRegAA(op, "vAA", vAA);

        b.position(0);
        b.put((byte) op.opcode).put((byte) vAA).putLong(value.longValue());
        return copy(b);

    }

    Label getLabel(DexLabel label) {
        Label mapped = labelMap.get(label);
        if (mapped == null) {
            mapped = new Label();
            labelMap.put(label, mapped);
        }
        return mapped;
    }

    @Override
    public void visitFillArrayDataStmt(Op op, int ra, Object value) {

        ByteBuffer b;

        if (value instanceof byte[]) {
            byte[] data = (byte[]) value;
            int size = data.length;
            int elementWidth = 1;
            b = ByteBuffer.allocate(((size * elementWidth + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short) 0x0300);
            b.putShort((short) elementWidth);
            b.putInt(size);
            b.put(data);
        } else if (value instanceof short[]) {
            short[] data = (short[]) value;
            int size = data.length;
            int elementWidth = 2;
            b = ByteBuffer.allocate(((size * elementWidth + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short) 0x0300);
            b.putShort((short) elementWidth);
            b.putInt(size);
            for (short s : data) {
                b.putShort(s);
            }
        } else if (value instanceof int[]) {
            int[] data = (int[]) value;
            int size = data.length;
            int elementWidth = 4;
            b = ByteBuffer.allocate(((size * elementWidth + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short) 0x0300);
            b.putShort((short) elementWidth);
            b.putInt(size);
            for (int s : data) {
                b.putInt(s);
            }
        } else if (value instanceof float[]) {
            float[] data = (float[]) value;
            int size = data.length;
            int elementWidth = 4;
            b = ByteBuffer.allocate(((size * elementWidth + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short) 0x0300);
            b.putShort((short) elementWidth);
            b.putInt(size);
            for (float s : data) {
                b.putInt(Float.floatToIntBits(s));
            }
        } else if (value instanceof long[]) {
            long[] data = (long[]) value;
            int size = data.length;
            int elementWidth = 8;
            b = ByteBuffer.allocate(((size * elementWidth + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short) 0x0300);
            b.putShort((short) elementWidth);
            b.putInt(size);
            for (long s : data) {
                b.putLong(s);
            }
        } else if (value instanceof double[]) {
            double[] data = (double[]) value;
            int size = data.length;
            int elementWidth = 8;
            b = ByteBuffer.allocate(((size * elementWidth + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short) 0x0300);
            b.putShort((short) elementWidth);
            b.putInt(size);
            for (double s : data) {
                b.putLong(Double.doubleToLongBits(s));
            }
        } else {
            throw new RuntimeException();
        }
        Label d = new Label();
        ops.add(new JumpOp(op, ra, 0, d));

        tailOps.add(d);
        tailOps.add(new PreBuildInsn(b.array()));

    }

    /**
     * kFmt21c,kFmt31c,kFmt11n,kFmt21h,kFmt21s,kFmt31i,kFmt51l
     */
    @Override
    public void visitConstStmt(Op op, int ra, Object value) {
        switch (op.format) {
        case kFmt21c:// value is field,type,string,method_handle,proto
        case kFmt31c:// value is string,
            value = cp.wrapEncodedItem(value);
            ops.add(new CodeWriter.IndexedInsn(op, ra, 0, (BaseItem) value));
            break;
        case kFmt11n:
            ops.add(new PreBuildInsn(build11n(op, ra, ((Number) value).intValue())));
            break;
        case kFmt21h:
            ops.add(new PreBuildInsn(build21h(op, ra, ((Number) value))));
            break;
        case kFmt21s:
            ops.add(new PreBuildInsn(build21s(op, ra, ((Number) value))));
            break;
        case kFmt31i:
            ops.add(new PreBuildInsn(build31i(op, ra, ((Number) value))));
            break;
        case kFmt51l:
            ops.add(new PreBuildInsn(build51l(op, ra, ((Number) value))));
            break;
        default:
            break;
        }
    }

    @Override
    public void visitEnd() {
        if (ops.isEmpty() && tailOps.isEmpty()) {
            encodedMethod.code = null;
            return;
        }
        cp.addCodeItem(codeItem);

        codeItem.registersSize = this.totalReg;
        codeItem.outsSize = maxOutRegSize;
        codeItem.insSize = inRegSize;

        codeItem.init(ops, tailOps, tryItems);

        if (codeItem.debugInfo != null) {
            cp.addDebugInfoItem(codeItem.debugInfo);
            List<DebugInfoItem.DNode> debugNodes = codeItem.debugInfo.debugNodes;
            debugNodes.sort((o1, o2) -> {
                int x = o1.label.offset - o2.label.offset;
                // if (x == 0) {
                // if (o1.op == o2.op) {
                // x = o1.reg - o2.reg;
                // if (x == 0) {
                // x = o1.line - o2.line;
                // }
                // } else {
                // //
                // }
                // }
                return x;
            });
        }

        ops = null;
        tailOps = null;
        tryItems = null;

    }

    @Override
    public void visitFieldStmt(Op op, int a, int b, Field field) {
        ops.add(new CodeWriter.IndexedInsn(op, a, b, cp.uniqField(field)));
    }

    @Override
    public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
        if (op.format == kFmt35c) {
            ops.add(new CodeWriter.OP35c(op, args, cp.uniqType(type)));
        } else {
            ops.add(new CodeWriter.OP3rc(op, args, cp.uniqType(type)));
        }
    }

    @Override
    public void visitJumpStmt(Op op, int a, int b, DexLabel label) {

        ops.add(new JumpOp(op, a, b, getLabel(label)));
    }

    @Override
    public void visitLabel(DexLabel label) {
        ops.add(getLabel(label));
    }

    @Override
    public void visitMethodStmt(Op op, int[] args, Method method) {
        if (op.format == kFmt3rc) {
            ops.add(new CodeWriter.OP3rc(op, args, cp.uniqMethod(method)));
        } else if (op.format == kFmt35c) {
            ops.add(new CodeWriter.OP35c(op, args, cp.uniqMethod(method)));
        }
        if (args.length > maxOutRegSize) {
            maxOutRegSize = args.length;
        }
    }

    @Override
    public void visitPackedSwitchStmt(Op op, int aA, final int firstCase, final DexLabel[] labels) {
        Label switchDataLocation = new Label();
        final JumpOp jumpOp = new JumpOp(op, aA, 0, switchDataLocation);
        ops.add(jumpOp);

        tailOps.add(switchDataLocation);
        tailOps.add(new Insn() {

            @Override
            public int getCodeUnitSize() {
                return (labels.length * 2) + 4;
            }

            @Override
            public void write(ByteBuffer out) {
                out.putShort((short) 0x0100).putShort((short) labels.length).putInt(firstCase);

                for (DexLabel label : labels) {
                    out.putInt(getLabel(label).offset - jumpOp.offset);
                }
            }
        });
    }

    @Override
    public void visitRegister(int total) {
        this.totalReg = total;
    }

    @Override
    public void visitSparseSwitchStmt(Op op, int ra, final int[] cases, final DexLabel[] labels) {
        Label switchDataLocation = new Label();
        final JumpOp jumpOp = new JumpOp(op, ra, 0, switchDataLocation);
        ops.add(jumpOp);

        tailOps.add(switchDataLocation);
        tailOps.add(new Insn() {

            @Override
            public int getCodeUnitSize() {
                return (cases.length * 4) + 2;
            }

            @Override
            public void write(ByteBuffer out) {
                out.putShort((short) 0x0200).putShort((short) cases.length);
                for (int aCase : cases) {
                    out.putInt(aCase);
                }
                for (int i = 0; i < cases.length; i++) {
                    out.putInt(getLabel(labels[i]).offset - jumpOp.offset);
                }
            }
        });

    }

    @Override
    public void visitStmt0R(Op op) {
        // CHECKSTYLE:OFF
        if (op == BAD_OP) {
            // TODO check
        } else {
            if (op.format == kFmt10x) {
                ops.add(new PreBuildInsn(build10x(op)));
            } else {
                // FIXME error
            }
        }
        // CHECKSTYLE:ON
    }

    /**
     * kFmt11x
     */
    @Override
    public void visitStmt1R(Op op, int reg) {
        if (op.format == kFmt11x) {
            ops.add(new PreBuildInsn(build11x(op, reg)));
        }
    }

    /**
     * kFmt12x,kFmt22x,kFmt32x
     */
    @Override
    public void visitStmt2R(Op op, int a, int b) {
        switch (op.format) {
        case kFmt12x:
            ops.add(new PreBuildInsn(build12x(op, a, b)));
            break;
        case kFmt22x:
            ops.add(new PreBuildInsn(build22x(op, a, b)));
            break;
        case kFmt32x:
            ops.add(new PreBuildInsn(build32x(op, a, b)));
            break;
        default:
            break;
        }
    }

    /**
     * Only kFmt22s, kFmt22b
     */
    @Override
    public void visitStmt2R1N(Op op, int distReg, int srcReg, int content) {
        if (op.format == kFmt22s) {
            ops.add(new PreBuildInsn(build22s(op, distReg, srcReg, content)));
        } else if (op.format == kFmt22b) {
            ops.add(new PreBuildInsn(build22b(op, distReg, srcReg, content)));
        }
    }

    /**
     * kFmt23x
     */
    @Override
    public void visitStmt3R(Op op, int a, int b, int c) {
        if (op.format == kFmt23x) {
            ops.add(new PreBuildInsn(build23x(op, a, b, c)));
        }
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handlers, String[] types) {
        CodeItem.TryItem tryItem = new CodeItem.TryItem();
        tryItem.start = getLabel(start);
        tryItem.end = getLabel(end);
        CodeItem.EncodedCatchHandler ech = new CodeItem.EncodedCatchHandler();
        tryItem.handler = ech;
        tryItems.add(tryItem);
        ech.addPairs = new ArrayList<>(types.length);
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            Label label = getLabel(handlers[i]);
            if (type == null) {
                ech.catchAll = label;
            } else {
                ech.addPairs.add(new CodeItem.EncodedCatchHandler.AddrPair(cp.uniqType(type), label));
            }
        }
    }

    @Override
    public void visitTypeStmt(Op op, int a, int b, String type) {
        ops.add(new CodeWriter.IndexedInsn(op, a, b, cp.uniqType(type)));
    }

    public static class IndexedInsn extends OpInsn {

        final int a;

        final int b;

        final BaseItem idxItem;

        public IndexedInsn(Op op, int a, int b, BaseItem idxItem) {
            super(op);
            switch (op.format) {
            case kFmt21c:
            case kFmt31c:
                checkRegAA(op, "vAA", a);
                break;
            case kFmt22c:
                checkContentU4bit(op, "A", a);
                checkContentU4bit(op, "B", b);
                break;
            default:
                break;
            }

            this.a = a;
            this.b = b;
            this.idxItem = idxItem;
        }

        // 21c AA|op BBBB
        // 31c AA|op BBBBlo BBBBhi
        // 22c B|A|op CCCC
        @Override
        public void write(ByteBuffer out) {
            out.put((byte) op.opcode);
            switch (op.format) {
            case kFmt21c:
                checkContentUShort(op, "?@BBBB", idxItem.index);
                out.put((byte) a).putShort((short) idxItem.index);
                break;
            case kFmt31c:
                out.put((byte) a).putInt(idxItem.index);
                break;
            case kFmt22c: // B|A|op CCCC
                checkContentUShort(op, "?@CCCC", idxItem.index);
                out.put((byte) ((a & 0xF) | (b << 4))).putShort((short) idxItem.index);
                break;
            default:
                break;
            }
        }

        public void fit() {
            if (op == CONST_STRING && (idxItem.index > 0xFFFF || idxItem.index < 0)) {
                op = CONST_STRING_JUMBO;
            }
        }
    }

    public static class OP35c extends OpInsn {

        final BaseItem item;

        int a;

        int c;

        int d;

        int e;

        int f;

        int g;

        public OP35c(Op op, int[] args, BaseItem item) {
            super(op);
            int a = args.length;
            if (a > 5) {
                throw new CantNotFixContentException(op, "A", a);
            }
            this.a = a;
            switch (a) { // [A=5] op {vC, vD, vE, vF, vG},
            case 5:
                g = args[4];
                checkContentU4bit(op, "vG", g);
            case 4:
                f = args[3];
                checkContentU4bit(op, "vF", f);
            case 3:
                e = args[2];
                checkContentU4bit(op, "vE", e);
            case 2:
                d = args[1];
                checkContentU4bit(op, "vD", d);
            case 1:
                c = args[0];
                checkContentU4bit(op, "vC", c);
                break;
            default:
                break;
            }
            this.item = item;
        }

        @Override
        public void write(ByteBuffer out) { // A|G|op BBBB F|E|D|C
            checkContentUShort(op, "@BBBB", item.index);
            out.put((byte) op.opcode).put((byte) ((a << 4) | (g & 0xF))).putShort((short) item.index)
                    .put((byte) ((d << 4) | (c & 0xF))).put((byte) ((f << 4) | (e & 0xF)));
        }
    }

    // AA|op BBBB CCCC
    public static class OP3rc extends OpInsn {

        final BaseItem item;

        final int length;

        final int start;

        public OP3rc(Op op, int[] args, BaseItem item) {
            super(op);
            this.item = item;
            length = args.length;
            checkContentUByte(op, "AA", length);
            if (length > 0) {
                start = args[0];
                checkContentUShort(op, "CCCC", start);
                for (int i = 1; i < args.length; i++) {
                    if (start + i != args[i]) {
                        throw new CantNotFixContentException(op, "a", args[i]);
                    }
                }
            } else {
                start = 0;
            }

        }

        @Override
        public void write(ByteBuffer out) {
            checkContentUShort(op, "@BBBB", item.index);
            out.put((byte) op.opcode).put((byte) length).putShort((short) item.index).putShort((short) start);
        }

    }

    @Override
    public DexDebugVisitor visitDebug() {
        if (codeItem.debugInfo == null) {
            codeItem.debugInfo = new DebugInfoItem();
            codeItem.debugInfo.parameterNames = new StringIdItem[owner.getParameterTypes().length];
        }
        final DebugInfoItem debugInfoItem = codeItem.debugInfo;
        return new DexDebugVisitor() {

            @Override
            public void visitParameterName(int parameterIndex, String name) {
                if (name == null) {
                    return;
                }
                if (parameterIndex >= debugInfoItem.parameterNames.length) {
                    return;
                }
                debugInfoItem.parameterNames[parameterIndex] = cp.uniqString(name);
            }

            @Override
            public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
                if (signature == null) {
                    debugInfoItem.debugNodes.add(DebugInfoItem.DNode.startLocal(reg, getLabel(label),
                            cp.uniqString(name), cp.uniqType(type)));
                } else {
                    debugInfoItem.debugNodes.add(DebugInfoItem.DNode.startLocalEx(reg, getLabel(label),
                            cp.uniqString(name), cp.uniqType(type), cp.uniqString(signature)));
                }
            }

            int miniLine = 0;

            @Override
            public void visitLineNumber(int line, DexLabel label) {
                if ((0x00000000FFFFffffL & line) < miniLine) {
                    miniLine = line;
                }
                debugInfoItem.debugNodes.add(DebugInfoItem.DNode.line(line, getLabel(label)));
            }

            @Override
            public void visitPrologue(DexLabel dexLabel) {
                debugInfoItem.debugNodes.add(DebugInfoItem.DNode.prologue(getLabel(dexLabel)));
            }

            @Override
            public void visitEpiogue(DexLabel dexLabel) {
                debugInfoItem.debugNodes.add(DebugInfoItem.DNode.epiogue(getLabel(dexLabel)));
            }

            @Override
            public void visitEndLocal(int reg, DexLabel label) {
                debugInfoItem.debugNodes.add(DebugInfoItem.DNode.endLocal(reg, getLabel(label)));
            }

            @Override
            public void visitSetFile(String file) {
                debugInfoItem.fileName = cp.uniqString(file);
            }

            @Override
            public void visitRestartLocal(int reg, DexLabel label) {
                debugInfoItem.debugNodes.add(DebugInfoItem.DNode.restartLocal(reg, getLabel(label)));
            }

            @Override
            public void visitEnd() {
                debugInfoItem.firstLine = miniLine;
            }
        };
    }

}
