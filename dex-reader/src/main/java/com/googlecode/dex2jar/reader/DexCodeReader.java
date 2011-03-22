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

import static com.googlecode.dex2jar.reader.OpcodeFormat.F10t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F10x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F11n;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F11x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F12x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F20t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21h;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21s;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22b;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22s;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F23x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F30t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F31c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F31i;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F31t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F32x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F35c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F3rc;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F51l;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import com.googlecode.dex2jar.DataIn;
import com.googlecode.dex2jar.Dex;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;

/**
 * 用于读取方法的指令
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DexCodeReader implements DexOpcodes {
    /**
     * dex文件
     */
    private Dex dex;
    /**
     * 输入流
     */
    private DataIn in;
    /**
     * 标签映射,指令位置->指令编号
     */
    private Map<Integer, Label> labels = new HashMap<Integer, Label>();

    /**
     * 方法的描述
     */
    private Method method;

    /**
     * @param dex
     *            dex文件
     * @param in
     *            输入流
     * @param method
     *            方法的描述
     */
    public DexCodeReader(Dex dex, DataIn in, Method method) {
        this.dex = dex;
        this.in = in;
        this.method = method;
    }

    private void findLabels(DataIn in, int instruction_size) {
        for (int baseOffset = in.getCurrentPosition(), currentOffset = 0; currentOffset < instruction_size * 2; currentOffset = in
                .getCurrentPosition() - baseOffset) {
            int opcode = in.readByte() & 0xff;
            switch (opcode) {
            case OP_GOTO:// 10t
                order(currentOffset + ((byte) (in.readByte() & 0xFF)) * 2);
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
                order(currentOffset + ((short) (in.readShortx() & 0xFFFF)) * 2);
                break;
            case DexInternalOpcode.OP_GOTO_32:// 30t;
                in.skip(1);
                order(currentOffset + in.readIntx() * 2);
                break;

            case OP_SPARSE_SWITCH:
            case OP_PACKED_SWITCH: {

                in.skip(1);
                int offset = in.readIntx();
                in.push();
                try {
                    in.move((offset - 3) * 2);
                    switch (opcode) {
                    case OP_SPARSE_SWITCH: {
                        in.skip(2);
                        int switch_size = in.readShortx();
                        in.skip(4 * switch_size);// skip keys
                        for (int j = 0; j < switch_size; j++) {
                            order(currentOffset + in.readIntx() * 2);
                        }
                        order(currentOffset + 3 * 2);
                    }
                        break;
                    case OP_PACKED_SWITCH: {

                        in.skip(2);
                        int switch_size = in.readShortx();
                        in.skip(4);
                        for (int j = 0; j < switch_size; j++) {
                            int targetOffset = in.readIntx();
                            order(currentOffset + targetOffset * 2);
                        }
                        order(currentOffset + 3 * 2);
                    }
                        break;
                    }
                } finally {
                    in.pop();
                }
            }

            case OP_NOP:// OP_NOP
                int x = in.readByte();
                switch (x) {
                case 0: // 0000 //spacer
                    break;
                case 1: // packed-switch-data
                {
                    int switch_size = in.readShortx(); // switch_size
                    in.skip(4);// first_case
                    in.skip(switch_size * 4);
                    break;
                }
                case 2:// sparse-switch-data
                {
                    int switch_size = in.readShortx();
                    in.skip(switch_size * 8);
                    break;
                }
                case 3: {
                    int elemWidth = in.readShortx();
                    int initLength = in.readIntx();
                    in.skip(elemWidth * initLength);
                    if (elemWidth == 1 && initLength % 2 != 0) {
                        in.skip(1);
                    }
                    break;
                }
                }
                break;
            default: {
                int size = DexOpcodeUtil.length(opcode);
                in.skip(size * 2 + 1);
                break;
            }

            }
        }
    }

    private void findTryCatch(DataIn in, DexCodeVisitor dcv, int tries_size) {
        if (in.needPadding()) {
            in.skip(2);
        }
        for (int i = 0; i < tries_size; i++) {
            int start = in.readIntx() * 2;
            int offset = in.readShortx() * 2;
            int handlers = in.readShortx();
            in.push();
            try {
                in.skip((tries_size - i - 1) * 8 + handlers);
                boolean catchAll = false;
                int listSize = (int) in.readSignedLeb128();
                if (listSize <= 0) {
                    listSize = -listSize;
                    catchAll = true;
                }
                for (int k = 0; k < listSize; k++) {
                    int type_id = (int) in.readUnsignedLeb128();
                    int handler = (int) in.readUnsignedLeb128() * 2;
                    order(start);
                    int end = 0;
                    if (handler > start && handler < start + offset) {
                        end = handler;
                        order(handler);
                    } else {
                        end = start + offset;
                        order(start + offset);
                        order(handler);
                    }
                    String type = dex.getType(type_id);
                    dcv.visitTryCatch(this.labels.get(start), this.labels.get(end), this.labels.get(handler), type);
                }
                if (catchAll) {
                    int handler = (int) in.readUnsignedLeb128() * 2;
                    order(start);
                    int end = 0;
                    if (handler > start && handler < start + offset) {
                        end = handler;
                        order(handler);
                    } else {
                        end = start + offset;
                        order(start + offset);
                        order(handler);
                    }
                    dcv.visitTryCatch(this.labels.get(start), this.labels.get(end), this.labels.get(handler), null);
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
    public void accept(DexCodeVisitor dcv) {

        DataIn in = this.in;
        int total_registers_size = in.readShortx();
        int in_register_size = in.readShortx();
        in.skip(2);// outs_size
        int tries_size = in.readShortx();
        int debug_off = in.readIntx();
        int instruction_size = in.readIntx();
        // 处理方法的参数
        {
            int args[];
            int args_index;
            int i = total_registers_size - in_register_size;
            if ((method.getAccessFlags() & Opcodes.ACC_STATIC) == 0) {
                args = new int[method.getType().getParameterTypes().length + 1];
                args[0] = i++;
                args_index = 1;
            } else {
                args = new int[method.getType().getParameterTypes().length];
                args_index = 0;
            }
            for (String type : method.getType().getParameterTypes()) {
                args[args_index++] = i++;
                if ("D".equals(type) || "J".equals(type)) {// 为Double/Long型特殊处理
                    i++;
                }
            }
            dcv.visitArguments(args);
        }

        // 处理异常处理
        if (tries_size > 0) {
            in.push();
            try {
                in.skip(instruction_size * 2);
                findTryCatch(in, dcv, instruction_size);
            } finally {
                in.pop();
            }
        }
        // 处理debug信息
        if (debug_off != 0) {
            // in.pushMove(debug_off);
            // new DexDebugInfoReader(in, dex,total_registers_size).accept(dcv);
            // in.pop();
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
        int currentOffset = 0;
        for (int baseOffset = in.getCurrentPosition(); currentOffset < instruction_size * 2; currentOffset = in
                .getCurrentPosition() - baseOffset) {
            int opcode = in.readByte() & 0xff;
            n.offset(currentOffset);

            switch (DexOpcodeUtil.format(opcode)) {
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
                        int switch_size = in.readShortx(); // switch_size
                        in.skip(4);// first_case
                        in.skip(switch_size * 4);
                        break;
                    }
                    case 2:// sparse-switch-data
                    {
                        int switch_size = in.readShortx();
                        in.skip(switch_size * 8);
                        break;
                    }
                    case 3: {
                        int elemWidth = in.readShortx();
                        int initLength = in.readIntx();
                        in.skip(elemWidth * initLength);
                        if (elemWidth == 1 && initLength % 2 != 0) {
                            in.skip(1);
                        }
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
                int B = (VV >>> 4) & 0xF;
                if (0 != (B & 0x8)) {
                    B = -((Integer.reverse(B) & 0x7) + 1);
                }
                n.x1n(opcode, VV & 0xF, B);
                break;
            }
            case F11x:
                n.x1x(opcode, in.readByte() & 0xFF);
                break;
            case F12x: {
                int VV = in.readByte();
                n.x2x(opcode, VV & 0xF, (VV >>> 4) & 0xF);
                break;
            }
            case F20t:
                in.skip(1);
                n.x0t(opcode, (short) (in.readShortx() & 0xFFFF));
                break;
            case F21c: {
                int AA = in.readByte() & 0xFF;
                int BBBB = in.readShortx() & 0xFFFF;
                n.x1c(opcode, AA, BBBB);
                break;
            }
            case F21h: {
                int A = in.readByte() & 0xFF;
                int B = (short) (in.readShortx() & 0xFFFF);
                n.x1h(opcode, A, B);
                break;
            }
            case F21s: {
                int AA = in.readByte() & 0xFF;
                int BBBBB = (short) (in.readShortx() & 0xFFFF);
                n.x1s(opcode, AA, BBBBB);
                break;
            }
            case F21t: {
                int AA = in.readByte() & 0xFF;
                int BBBB = (short) (in.readShortx() & 0xFFFF);
                n.x1t(opcode, AA, BBBB);
                break;
            }
            case F22b: {
                int AA = in.readByte() & 0xFF;
                int BB = in.readByte() & 0xFF;
                int CC = (byte) (in.readByte() & 0xFF);
                n.x2b(opcode, AA, BB, CC);
                break;
            }
            case F22c: {
                int VV = in.readByte();
                int A = VV & 0xF;
                int B = (VV >>> 4) & 0xF;
                int CCCC = in.readShortx() & 0xFFFF;
                n.x2c(opcode, A, B, CCCC);
                break;
            }
            case F22s: {
                int VV = in.readByte();
                int A = VV & 0xF;
                int B = (VV >>> 4) & 0xF;
                int CCCC = (short) (in.readShortx() & 0xFFFF);
                n.x2s(opcode, A, B, CCCC);
                break;
            }
            case F22t: {
                int VV = in.readByte();
                int A = VV & 0xF;
                int B = (VV >>> 4) & 0xF;
                int CCCC = (short) (in.readShortx() & 0xFFFF);
                n.x2t(opcode, A, B, CCCC);
                break;
            }
            case F22x: {
                int AA = in.readByte() & 0xFF;
                int BBBB = in.readShortx() & 0xFFFF;
                n.x2x(opcode, AA, BBBB);
                break;
            }
            case F23x: {
                int AA = in.readByte() & 0xFF;
                int BB = in.readByte() & 0xFF;
                int CC = in.readByte() & 0xFF;
                n.x3x(opcode, AA, BB, CC);
                break;
            }
            case F30t:
                in.skip(1);
                n.x0t(opcode, in.readIntx());
                break;
            case F31c: {
                int AA = in.readByte() & 0xFF;
                int BBBBBBBB = in.readIntx();
                n.x1c(opcode, AA, BBBBBBBB);
                break;
            }
            case F31i: {
                int AA = in.readByte() & 0xFF;
                int BBBBBBBB = in.readIntx();
                n.x1i(opcode, AA, BBBBBBBB);
                break;
            }
            case F31t: {
                int AA = in.readByte() & 0xFF;
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

                            in.readShortx();
                            int switch_size = in.readShortx();
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
                            int switch_size = in.readShortx();
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
                            int elemWidth = in.readShortx();
                            int initLength = in.readIntx();
                            Object[] values = new Object[initLength];

                            switch (elemWidth) {
                            case 1:
                                for (int j = 0; j < initLength; j++) {
                                    values[j] = in.readByte();
                                }
                                break;
                            case 2:
                                for (int j = 0; j < initLength; j++) {
                                    values[j] = in.readShortx();
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
            case F32x: {
                in.skip(1);
                int AAAA = in.readShortx() & 0xFFFF;
                int BBBB = in.readShortx() & 0xFFFF;
                n.x2x(opcode, AAAA, BBBB);
                break;
            }

            case F35c: {
                int V = in.readByte() & 0xF;
                int A = V & 0xF;
                int B = (V >>> 4) & 0xF;
                int CCCC = in.readShortx() & 0xFFFF;
                V = in.readShortx();
                int D = V & 0xF;
                int E = (V >> 4) & 0xF;
                int F = (V >> 8) & 0xF;
                int G = (V >>> 12) & 0xF;
                n.x5c(opcode, B, D, E, F, G, A, CCCC);
                break;
            }
            case F3rc: {
                int AA = in.readByte() & 0xFF;
                int BBBB = in.readShortx() & 0xFFFF;
                int CCCC = in.readShortx() & 0xFFFF;
                n.xrc(opcode, CCCC, AA, BBBB);
                break;
            }
            case F51l:
                int AA = in.readByte() & 0xFF;
                long BBBBBBBB_BBBBBBBB = in.readLongx();
                n.x1l(opcode, AA, BBBBBBBB_BBBBBBBB);
                break;
            }
        }
        n.offset(currentOffset);
    }

    /**
     * 预定一个标签位置
     * 
     * @param offset
     *            指令位置
     */
    private void order(int offset) {
        if (!labels.containsKey(offset)) {
            labels.put(offset, new Label());
        }
    }
}
