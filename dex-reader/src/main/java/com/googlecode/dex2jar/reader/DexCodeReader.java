/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.reader;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.reader.DexDebugInfoReader.LocalVariable;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;

/**
 * 用于读取方法的指令
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
/* default */class DexCodeReader implements DexOpcodes {

    /**
     * dex文件
     */
    private DexFileReader dex;
    /**
     * 输入流
     */
    private DataIn in;
    /**
     * 标签映射,指令位置->指令编号
     */
    /* default */Map<Integer, DexLabel> labels = new TreeMap<Integer, DexLabel>();

    private boolean isStatic;

    /**
     * 方法的描述
     */
    private Method method;

    /**
     * @param dex
     *            dex文件
     * @param in
     *            输入流
     * @param isStatic
     * @param method
     *            方法的描述
     */
    /* default */DexCodeReader(DexFileReader dex, DataIn in, boolean isStatic, Method method) {
        this.dex = dex;
        this.in = in;
        this.method = method;
        this.isStatic = isStatic;
    }

    private void findLabels(DataIn in, int instruction_size) {
        for (int baseOffset = in.getCurrentPosition(), currentOffset = 0; currentOffset < instruction_size; currentOffset = (in
                .getCurrentPosition() - baseOffset) / 2) {
            int opcode = in.readUByte();
            if (opcode == 0xFF) {
                opcode = 0xFF00 | in.readUByte();
            }
            OpcodeFormat format = OpcodeFormat.get(opcode);
            try {
                switch (opcode) {
                case OP_GOTO:// 10t
                    order(currentOffset + in.readByte());
                    break;
                case OP_IF_EQZ:// 21t
                case OP_IF_NEZ:
                case OP_IF_LTZ:
                case OP_IF_GEZ:
                case OP_IF_GTZ:
                case OP_IF_LEZ:
                case OP_IF_EQ:// 22t
                case OP_IF_NE:
                case OP_IF_LT:
                case OP_IF_GE:
                case OP_IF_GT:
                case OP_IF_LE:
                case DexInternalOpcode.OP_GOTO_16:// 20t;
                    in.skip(1);
                    order(currentOffset + in.readShortx());
                    break;
                case DexInternalOpcode.OP_GOTO_32:// 30t;
                    in.skip(1);
                    order(currentOffset + in.readIntx());
                    break;

                case OP_SPARSE_SWITCH:
                case OP_PACKED_SWITCH: {

                    in.skip(1);
                    int offset = in.readIntx();
                    in.push();
                    try {
                        in.skip((offset - 3) * 2);
                        switch (opcode) {
                        case OP_SPARSE_SWITCH: {
                            in.skip(2);
                            int switch_size = in.readUShortx();
                            in.skip(4 * switch_size);// skip keys
                            for (int j = 0; j < switch_size; j++) {
                                order(currentOffset + in.readIntx());
                            }
                            order(currentOffset + 3);
                        }
                            break;
                        case OP_PACKED_SWITCH: {

                            in.skip(2);
                            int switch_size = in.readUShortx();
                            in.skip(4);
                            for (int j = 0; j < switch_size; j++) {
                                int targetOffset = in.readIntx();
                                order(currentOffset + targetOffset);
                            }
                            order(currentOffset + 3);
                        }
                            break;
                        }
                    } finally {
                        in.pop();
                    }
                    break;
                }

                case OP_NOP:// OP_NOP
                    int x = in.readByte();
                    switch (x) {
                    case 0: // 0000 //spacer
                        break;
                    case 1: // packed-switch-data
                    {
                        int switch_size = in.readUShortx(); // switch_size
                        in.skip(switch_size * 4 + 4);
                        break;
                    }
                    case 2:// sparse-switch-data
                    {
                        int switch_size = in.readUShortx();
                        in.skip(switch_size * 8);
                        break;
                    }
                    case 3: {
                        int element_width = in.readUShortx();
                        int size = in.readUIntx();
                        // total byte of fill-array-data is ((size * element_width + 1) / 2 + 4) * 2;
                        in.skip((size * element_width + 1) & 0xFFFFFFFE);
                        break;
                    }
                    }
                    break;
                default: {
                    if (opcode >> 4 == 0xFF) {
                        in.skip(2 * format.getSize() - 2);
                    } else {
                        in.skip(2 * format.getSize() - 1);
                    }
                    break;
                }

                }
            } catch (Exception e) {
                throw new DexException(e, String.format("while scan for label, Posotion :%04x", currentOffset));
            }
        }
    }

    private void findTryCatch(DataIn in, DexCodeVisitor dcv, int tries_size) {
        int encoded_catch_handler_list = in.getCurrentPosition() + tries_size * 8;
        for (int i = 0; i < tries_size; i++) {
            int start_addr = in.readUIntx();
            int insn_count = in.readUShortx();
            int handler_offset = in.readUShortx();

            order(start_addr);
            int end = start_addr + insn_count;
            order(end);

            in.pushMove(encoded_catch_handler_list + handler_offset);// move to encoded_catch_handler
            try {
                boolean catchAll = false;
                int listSize = (int) in.readLeb128();
                if (listSize <= 0) {
                    listSize = -listSize;
                    catchAll = true;
                }
                for (int k = 0; k < listSize; k++) {
                    int type_id = (int) in.readULeb128();
                    int handler = (int) in.readULeb128();
                    order(handler);

                    String type = dex.getType(type_id);
                    dcv.visitTryCatch(this.labels.get(start_addr), this.labels.get(end), this.labels.get(handler), type);
                }
                if (catchAll) {
                    int handler = (int) in.readULeb128();
                    order(handler);
                    dcv.visitTryCatch(this.labels.get(start_addr), this.labels.get(end), this.labels.get(handler), null);
                }
            } finally {
                in.pop();
            }
        }
    }

    /**
     * 处理指令
     * 
     * @param dcv
     */
    public void accept(DexCodeVisitor dcv, int config) {

        DataIn in = this.in;
        int total_registers_size = in.readUShortx();
        int in_register_size = in.readUShortx();
        in.skip(2);// outs_size
        int tries_size = in.readUShortx();
        int debug_off = in.readUIntx();
        int instruction_size = in.readUIntx();

        LocalVariable localVariables[] = new LocalVariable[total_registers_size];
        int args[];
        // 处理方法的参数
        {
            int args_index;
            int i = total_registers_size - in_register_size;
            String[] parameterTypes = method.getParameterTypes();
            if (!isStatic) {
                args = new int[parameterTypes.length + 1];
                localVariables[i] = new LocalVariable(i, 0, -1, "this", method.getOwner(), null);
                args[0] = i++;
                args_index = 1;
            } else {
                args = new int[parameterTypes.length];
                args_index = 0;
            }
            for (String type : parameterTypes) {
                localVariables[i] = new LocalVariable(i, 0, -1, "arg" + args_index, type, null);
                args[args_index++] = i++;
                if ("D".equals(type) || "J".equals(type)) {// 为Double/Long型特殊处理
                    i++;
                }
            }
            dcv.visitArguments(total_registers_size, args);
        }

        // 处理异常处理
        if (tries_size > 0) {
            in.push();
            try {
                in.skip(instruction_size * 2);
                if ((instruction_size & 0x01) != 0) {// skip padding
                    in.skip(2);
                }
                findTryCatch(in, dcv, tries_size);
            } finally {
                in.pop();
            }
        }
        // 处理debug信息
        if (debug_off != 0 && (0 == (config & DexFileReader.SKIP_DEBUG))) {
            in.pushMove(debug_off);
            try {
                new DexDebugInfoReader(in, dex, instruction_size, this, localVariables, args).accept(dcv);
            } finally {
                in.pop();
            }
        }
        // 查找标签
        in.push();
        try {
            findLabels(in, instruction_size);
        } finally {
            in.pop();
        }
        DexOpcodeAdapter n = new DexOpcodeAdapter(this.dex, this.labels, dcv);
        acceptInsn(in, instruction_size, n);
        dcv.visitEnd();
    }

    private void acceptInsn(DataIn in, int instruction_size, DexOpcodeAdapter n) {
        // 处理指令
        Iterator<Integer> labelOffsetIterator = this.labels.keySet().iterator();
        Integer nextLabelOffset = labelOffsetIterator.hasNext() ? labelOffsetIterator.next() : null;
        for (int baseOffset = in.getCurrentPosition(), currentOffset = 0; currentOffset < instruction_size; currentOffset = (in
                .getCurrentPosition() - baseOffset) / 2) {
            boolean currentOffsetVisited = false;
            while (nextLabelOffset != null) {// issue 65, a label may `inside` an instruction
                int _intNextLabelOffset = nextLabelOffset;// autobox
                if (_intNextLabelOffset > currentOffset) {
                    break;
                } else if (_intNextLabelOffset == currentOffset) {
                    currentOffsetVisited = true;
                    n.offset(currentOffset);
                    nextLabelOffset = labelOffsetIterator.hasNext() ? labelOffsetIterator.next() : null;
                    break;
                } else {// _intNextLabelOffset < currentOffset
                    n.offset(_intNextLabelOffset);
                    nextLabelOffset = labelOffsetIterator.hasNext() ? labelOffsetIterator.next() : null;
                }
            }
            if (!currentOffsetVisited) {
                n.offset(currentOffset);
            }
            int opcode = in.readUByte();
            if (opcode == 0xFF) {
                opcode = 0xFF00 | in.readUByte();
            }
            OpcodeFormat format = OpcodeFormat.get(opcode);

            switch (format) {
            case F10t:
                n.x0t(opcode, (byte) (in.readByte() & 0xFF));
                break;
            case F10x: {
                int x = in.readByte();
                switch (opcode) {
                case OP_NOP:// OP_NOP
                    switch (x) {
                    case 0: // 0000 //spacer
                        break;
                    case 1: // packed-switch-data
                    {
                        int switch_size = in.readUShortx(); // switch_size
                        in.skip(switch_size * 4 + 4);
                        break;
                    }
                    case 2:// sparse-switch-data
                    {
                        int switch_size = in.readUShortx();
                        in.skip(switch_size * 8);
                        break;
                    }
                    case 3: {// fill-array-data
                        int element_width = in.readUShortx();
                        int size = in.readUIntx();
                        // total byte of fill-array-data is ((size * element_width + 1) / 2 + 4) * 2;
                        in.skip((size * element_width + 1) & 0xFFFFFFFE);
                        break;
                    }
                    }
                    break;
                }
                n.x0x(opcode);
                break;
            }
            case F11n: {
                int VV = in.readByte();
                int B = VV >> 4;
                n.x1n(opcode, VV & 0xF, B);
                break;
            }
            case F11x:
                n.x1x(opcode, in.readUByte());
                break;
            case F12x: {
                int VV = in.readUByte();
                n.x2x(opcode, VV & 0xF, VV >>> 4);
                break;
            }
            case F20bc: {
                int AA = in.readUByte();
                n.x0bc(opcode, AA, in.readUShortx());
                break;
            }
            case F20t:
                in.skip(1);
                n.x0t(opcode, in.readShortx());
                break;
            case F21c: {
                int AA = in.readUByte();
                int BBBB = in.readUShortx();
                n.x1c(opcode, AA, BBBB);
                break;
            }
            case F21h: {
                int A = in.readUByte();
                int B = in.readShortx();
                n.x1h(opcode, A, B);
                break;
            }
            case F21s: {
                int AA = in.readUByte();
                int BBBBB = in.readShortx();
                n.x1s(opcode, AA, BBBBB);
                break;
            }
            case F21t: {
                int AA = in.readUByte();
                int BBBB = in.readShortx();
                n.x1t(opcode, AA, BBBB);
                break;
            }
            case F22b: {
                int AA = in.readUByte();
                int BB = in.readUByte();
                int CC = in.readByte();
                n.x2b(opcode, AA, BB, CC);
                break;
            }
            case F22c: {
                int VV = in.readUByte();
                int A = VV & 0xF;
                int B = VV >>> 4;
                int CCCC = in.readUShortx();
                n.x2c(opcode, A, B, CCCC);
                break;
            }
            case F22cs: {
                int VV = in.readUByte();
                int A = VV & 0xF;
                int B = VV >>> 4;
                int CCCC = in.readUShortx();
                n.x2cs(opcode, A, B, CCCC);
                break;
            }
            case F22s: {
                int VV = in.readUByte();
                int A = VV & 0xF;
                int B = VV >>> 4;
                int CCCC = in.readShortx();
                n.x2s(opcode, A, B, CCCC);
                break;
            }
            case F22t: {
                int VV = in.readUByte();
                int A = VV & 0xF;
                int B = VV >>> 4;
                int CCCC = in.readShortx();
                n.x2t(opcode, A, B, CCCC);
                break;
            }
            case F22x: {
                int AA = in.readUByte();
                int BBBB = in.readUShortx();
                n.x2x(opcode, AA, BBBB);
                break;
            }
            case F23x: {
                int AA = in.readUByte();
                int BB = in.readUByte();
                int CC = in.readUByte();
                n.x3x(opcode, AA, BB, CC);
                break;
            }
            case F30t:
                in.skip(1);
                n.x0t(opcode, in.readIntx());
                break;
            case F31c: {
                int AA = in.readUByte();
                int BBBBBBBB = in.readUIntx();
                n.x1c(opcode, AA, BBBBBBBB);
                break;
            }
            case F31i: {
                int AA = in.readUByte();
                int BBBBBBBB = in.readIntx();
                n.x1i(opcode, AA, BBBBBBBB);
                break;
            }
            case F31t: {
                int AA = in.readUByte();
                int BBBBBBBB = in.readIntx();
                switch (opcode) {
                case OP_FILL_ARRAY_DATA:
                case OP_PACKED_SWITCH:
                case OP_SPARSE_SWITCH:
                    in.push();
                    try {
                        in.skip((BBBBBBBB - 3) * 2);
                        switch (opcode) {
                        case OP_SPARSE_SWITCH: {

                            in.skip(2);
                            int switch_size = in.readUShortx();
                            int cases[] = new int[switch_size];
                            int label[] = new int[switch_size];
                            for (int j = 0; j < switch_size; j++) {
                                cases[j] = in.readIntx();
                            }
                            for (int j = 0; j < switch_size; j++) {
                                label[j] = in.readIntx();
                            }
                            n.visitLookupSwitchStmt(opcode, AA, 3, cases, label);

                        }
                            break;
                        case OP_PACKED_SWITCH: {

                            in.skip(2);
                            int switch_size = in.readUShortx();
                            int first_case = in.readIntx();
                            int last_case = first_case - 1 + switch_size;
                            int _labels[] = new int[switch_size];
                            for (int j = 0; j < switch_size; j++) {
                                int targetOffset = in.readIntx();
                                _labels[j] = targetOffset;
                            }
                            n.visitTableSwitchStmt(opcode, AA, 3, first_case, last_case, _labels);

                        }
                            break;
                        case OP_FILL_ARRAY_DATA: {

                            in.skip(2);
                            int elemWidth = in.readUShortx();
                            int initLength = in.readUIntx();
                            Object[] values = new Object[initLength];

                            switch (elemWidth) {
                            case 1:
                                for (int j = 0; j < initLength; j++) {
                                    values[j] = (byte) in.readByte();
                                }
                                break;
                            case 2:
                                for (int j = 0; j < initLength; j++) {
                                    values[j] = (short) in.readShortx();
                                }
                                break;
                            case 4:
                                for (int j = 0; j < initLength; j++) {
                                    values[j] = in.readIntx();
                                }
                                break;
                            case 8:
                                for (int j = 0; j < initLength; j++) {
                                    values[j] = in.readLongx();
                                }
                                break;
                            }

                            n.visitFillArrayStmt(opcode, AA, elemWidth, initLength, values);
                        }
                        }
                    } finally {
                        in.pop();
                    }
                    break;
                default:
                    n.x1t(opcode, AA, BBBBBBBB);
                }
                break;
            }
            case F32s: {
                int aa = in.readUByte();
                int bb = in.readUByte();
                int cccc = in.readShortx();
                n.x2s(opcode, aa, bb, cccc);
                break;
            }
            case F32x: {
                in.skip(1);
                int AAAA = in.readUShortx();
                int BBBB = in.readUShortx();
                n.x2x(opcode, AAAA, BBBB);
                break;
            }
            case F33x: {
                int aa = in.readUByte();
                int bb = in.readUByte();
                int cccc = in.readUShortx();
                n.x3x(opcode, aa, bb, cccc);
                break;
            }
            case F35c: {
                int VV = in.readUByte();
                int g = VV & 0xF;
                int a = VV >>> 4;
                int bbbb = in.readUShortx();
                VV = in.readUShortx();
                int c = VV & 0xF;
                int d = (VV >> 4) & 0xF;
                int e = (VV >> 8) & 0xF;
                int f = VV >>> 12;
                n.x5c(opcode, a, c, d, e, f, g, bbbb);
                break;
            }
            case F35mi: {
                int VV = in.readUByte();
                int g = VV & 0xF;
                int a = VV >>> 4;
                int bbbb = in.readUShortx();
                VV = in.readUShortx();
                int c = VV & 0xF;
                int d = (VV >> 4) & 0xF;
                int e = (VV >> 8) & 0xF;
                int f = VV >>> 12;
                n.x5mi(opcode, a, c, d, e, f, g, bbbb);
                break;
            }
            case F35ms: {
                int VV = in.readUByte();
                int g = VV & 0xF;
                int a = VV >>> 4;
                int bbbb = in.readUShortx();
                VV = in.readUShortx();
                int c = VV & 0xF;
                int d = (VV >> 4) & 0xF;
                int e = (VV >> 8) & 0xF;
                int f = VV >>> 12;
                n.x5ms(opcode, a, c, d, e, f, g, bbbb);
                break;
            }
            case F3rc: {
                int aa = in.readUByte();
                int bbbb = in.readUShortx();
                int cccc = in.readUShortx();
                n.xrc(opcode, cccc, aa, bbbb);
                break;
            }
            case F3rmi: {
                int AA = in.readUByte();
                int BBBB = in.readUShortx();
                int CCCC = in.readUShortx();
                n.xrmi(opcode, CCCC, AA, BBBB);
                break;
            }
            case F3rms: {
                int AA = in.readUByte();
                int BBBB = in.readUShortx();
                int CCCC = in.readUShortx();
                n.xrms(opcode, CCCC, AA, BBBB);
                break;
            }
            case F40sc: {
                int bbbb_bbbb = in.readUIntx();
                int aaaa = in.readUShortx();
                n.x0sc(opcode, aaaa, bbbb_bbbb);
                break;
            }
            case F41c: {
                int bbbb_bbbb = in.readUIntx();
                int aaaa = in.readUShortx();
                n.x1c(opcode, aaaa, bbbb_bbbb);
                break;
            }
            case F51l: {
                int AA = in.readUByte();
                long BBBBBBBB_BBBBBBBB = in.readLongx();
                n.x1l(opcode, AA, BBBBBBBB_BBBBBBBB);
                break;
            }
            case F52c: {
                int cccc_cccc = in.readUIntx();
                int aaaa = in.readUShortx();
                int bbbb = in.readUShortx();
                n.x2c(opcode, aaaa, bbbb, cccc_cccc);
                break;
            }
            case F5rc: {
                int bbbb_bbbb = in.readUIntx();
                int aaaa = in.readUShortx();
                int cccc = in.readUShortx();
                n.xrc(opcode, cccc, aaaa, bbbb_bbbb);
                break;
            }
            }
        }
        while (nextLabelOffset != null) {
            n.offset(nextLabelOffset);
            if (labelOffsetIterator.hasNext()) {
                nextLabelOffset = labelOffsetIterator.next();
            } else {
                break;
            }
        }
    }

    /**
     * 预定一个标签位置
     * 
     * @param offset
     *            指令位置
     */
    void order(int offset) {
        if (!labels.containsKey(offset)) {
            labels.put(offset, new DexLabel());
        }
    }
}
