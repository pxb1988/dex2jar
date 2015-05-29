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
package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.insn.Label;
import com.googlecode.d2j.dex.writer.io.DataOut;

import java.util.ArrayList;
import java.util.List;

public class DebugInfoItem extends BaseItem {
    public List<DNode> debugNodes = new ArrayList<>();
    public StringIdItem parameterNames[];
    public int firstLine;
    public StringIdItem fileName;

    public static class DNode {
        public int op;
        public int reg;
        public int line;
        public Label label;
        StringIdItem name;
        TypeIdItem type;
        StringIdItem sig;

        public static DNode startLocal(int reg, Label label, StringIdItem name, TypeIdItem type) {
            DNode node = new DNode();
            node.reg = reg;
            node.label = label;
            node.name = name;
            node.type = type;
            node.op = DBG_START_LOCAL;
            return node;
        }

        public static DNode line(int line, Label label) {
            DNode node = new DNode();
            node.line = line;
            node.label = label;
            node.op = 99999;
            return node;
        }

        public static DNode startLocalEx(int reg, Label label, StringIdItem name, TypeIdItem type, StringIdItem sig) {
            DNode node = new DNode();
            node.reg = reg;
            node.label = label;
            node.name = name;
            node.type = type;
            node.sig = sig;
            node.op = DBG_START_LOCAL_EXTENDED;
            return node;
        }

        public static DNode endLocal(int reg, Label label) {
            DNode node = new DNode();
            node.reg = reg;
            node.label = label;
            node.op = DBG_END_LOCAL;
            return node;
        }

        public static DNode restartLocal(int reg, Label label) {
            DNode node = new DNode();
            node.reg = reg;
            node.label = label;
            node.op = DBG_RESTART_LOCAL;
            return node;
        }

        public static DNode epiogue(Label label) {
            DNode node = new DNode();
            node.label = label;
            node.op = DBG_SET_EPILOGUE_BEGIN;
            return node;
        }

        public static DNode prologue(Label label) {
            DNode node = new DNode();
            node.label = label;
            node.op = DBG_SET_PROLOGUE_END;
            return node;
        }
    }

    static final int DBG_END_SEQUENCE = 0x00;
    static final int DBG_ADVANCE_PC = 0x01;
    static final int DBG_ADVANCE_LINE = 0x02;
    static final int DBG_START_LOCAL = 0x03;
    static final int DBG_START_LOCAL_EXTENDED = 0x04;
    static final int DBG_END_LOCAL = 0x05;
    static final int DBG_RESTART_LOCAL = 0x06;
    static final int DBG_SET_PROLOGUE_END = 0x07;
    static final int DBG_SET_EPILOGUE_BEGIN = 0x08;
    static final int DBG_SET_FILE = 0x09;
    static final int DBG_FIRST_SPECIAL = 0x0a;
    static final int DBG_LINE_BASE = -4;
    static final int DBG_LINE_RANGE = 15;

    @Override
    public int place(int offset) {
        offset += lengthOfUleb128(firstLine);
        if (parameterNames == null) {
            offset += lengthOfUleb128(0);
        } else {
            offset += lengthOfUleb128(parameterNames.length);
            for (StringIdItem s : parameterNames) {
                offset += lengthOfUleb128(1 + (s == null ? -1 : s.index));
            }

        }
        int line = firstLine;
        int addr = 0;

        if (fileName != null) {
            offset += 1;
            offset += lengthOfUleb128(fileName.index + 1);
        }
        for (DNode opNode : debugNodes) {
            switch (opNode.op) {
            case DBG_START_LOCAL_EXTENDED:
                offset += lengthOfUleb128(opNode.sig.index + 1);
                // through;
            case DBG_START_LOCAL: {
                int pcData = opNode.label.offset - addr;
                if (pcData < 0) {
                    throw new RuntimeException();
                } else if (pcData > 0) {
                    // add an addvance_PC
                    offset += 1;
                    offset += lengthOfUleb128(pcData);
                }
                addr = opNode.label.offset;
            }
                offset += 1;// op;
                offset += lengthOfUleb128(opNode.reg);
                offset += lengthOfUleb128(opNode.name.index + 1);
                offset += lengthOfUleb128(opNode.type.index + 1);
                break;
            case DBG_RESTART_LOCAL:
            case DBG_END_LOCAL: {
                int pcData = opNode.label.offset - addr;
                if (pcData < 0) {
                    throw new RuntimeException();
                } else if (pcData > 0) {
                    // add an addvance_PC
                    offset += 1;
                    offset += lengthOfUleb128(pcData);
                }
                addr = opNode.label.offset;
            }
                offset += 1;// op;
                offset += lengthOfUleb128(opNode.reg);
                break;
            case DBG_SET_EPILOGUE_BEGIN:
            case DBG_SET_PROLOGUE_END:
                offset += 1;
                break;
            case DBG_SET_FILE:
                throw new RuntimeException();
            default:
                int lineDelta = opNode.line - line;
                int addrDelta = opNode.label.offset - addr;
                if (addrDelta < 0) {
                    throw new RuntimeException();
                }
                if (opNode.label.offset == 0 && lineDelta == 0 && addrDelta == 0) { // first line;
                    break;
                }
                if ((lineDelta >= -4 && lineDelta <= 10) && addrDelta <= 15) {
                    // do nothing
                } else {
                    if (addrDelta > 15) { // pc not ok, add addvance_PC
                        offset += 1;
                        offset += lengthOfUleb128(addrDelta);
                        addrDelta = 0;
                    }
                    if (lineDelta < -4 || lineDelta > 10) { // line not ok, add DBG_ADVANCE_LINE
                        offset += 1;
                        offset += lengthOfSleb128(lineDelta);
                        lineDelta = 0;
                    }
                }
                // int op = lineDelta + 4 + addrDelta * DBG_LINE_RANGE + DBG_FIRST_SPECIAL;
                offset += 1;
                line = opNode.line;
                addr = opNode.label.offset;
                break;
            }

        }
        offset += 1;// end sequence;

        return offset;
    }

    @Override
    public void write(DataOut out) {
        out.uleb128("startline", firstLine);
        if (parameterNames == null) {
            out.uleb128("szParams", 0);
        } else {
            out.uleb128("szParams", parameterNames.length);
            for (StringIdItem s : parameterNames) {
                out.uleb128p1("param_name_index", s == null ? -1 : s.index);
            }
        }
        int line = firstLine;
        int addr = 0;

        if (fileName != null) {
            out.sbyte("DBG_SET_FILE", DBG_SET_FILE);
            out.uleb128p1("filename", fileName.index);
        }
        for (DNode opNode : debugNodes) {
            switch (opNode.op) {
            case DBG_START_LOCAL_EXTENDED: {
                int pcDelta = opNode.label.offset - addr;
                if (pcDelta < 0) {
                    throw new RuntimeException();
                } else if (pcDelta > 0) {
                    addAdvancePC(out, pcDelta);
                }
                addr = opNode.label.offset;
            }
                out.sbyte("DBG_START_LOCAL_EXTENDED", DBG_START_LOCAL_EXTENDED);
                out.uleb128("reg", opNode.reg);
                out.uleb128p1("name", opNode.name.index);
                out.uleb128p1("type", opNode.type.index);
                out.uleb128p1("sig", opNode.sig.index);
                break;
            case DBG_START_LOCAL: {
                int pcDelta = opNode.label.offset - addr;
                if (pcDelta < 0) {
                    throw new RuntimeException();
                } else if (pcDelta > 0) {
                    addAdvancePC(out, pcDelta);
                }
                addr = opNode.label.offset;
            }
                out.sbyte("DBG_START_LOCAL", DBG_START_LOCAL);
                out.uleb128("reg", opNode.reg);
                out.uleb128p1("name", opNode.name.index);
                out.uleb128p1("type", opNode.type.index);

                break;
            case DBG_RESTART_LOCAL: {
                int pcDelta = opNode.label.offset - addr;
                if (pcDelta < 0) {
                    throw new RuntimeException();
                } else if (pcDelta > 0) {
                    addAdvancePC(out, pcDelta);
                }
                addr = opNode.label.offset;
            }

                out.sbyte("DBG_RESTART_LOCAL", DBG_RESTART_LOCAL);
                out.uleb128("reg", opNode.reg);
                break;
            case DBG_END_LOCAL: {
                int pcDelta = opNode.label.offset - addr;
                if (pcDelta < 0) {
                    throw new RuntimeException();
                } else if (pcDelta > 0) {
                    addAdvancePC(out, pcDelta);
                }
                addr = opNode.label.offset;
            }

                out.sbyte("DBG_END_LOCAL", DBG_END_LOCAL);
                out.uleb128("reg", opNode.reg);
                break;
            case DBG_SET_EPILOGUE_BEGIN:
                out.sbyte("DBG_SET_EPILOGUE_BEGIN", DBG_SET_EPILOGUE_BEGIN);
                break;
            case DBG_SET_PROLOGUE_END:
                out.sbyte("DBG_SET_PROLOGUE_END", DBG_SET_PROLOGUE_END);
                break;
            case DBG_SET_FILE:
                throw new RuntimeException();
            default:
                int lineDelta = opNode.line - line;
                int addrDelta = opNode.label.offset - addr;
                if (addrDelta < 0) {
                    throw new RuntimeException();
                }
                if (opNode.label.offset == 0 && lineDelta == 0 && addrDelta == 0) { // first line;
                    break;
                }
                if ((lineDelta >= -4 && lineDelta <= 10) && addrDelta <= 15) {
                    // do nothing
                } else {
                    if (addrDelta > 15) { // pc not ok, add addvance_PC
                        addAdvancePC(out, addrDelta);
                        addrDelta = 0;
                    }
                    if (lineDelta < -4 || lineDelta > 10) { // line not ok, add DBG_ADVANCE_LINE
                        addAdvanceLine(out, lineDelta);
                        lineDelta = 0;
                    }
                }
                int op = lineDelta + 4 + addrDelta * DBG_LINE_RANGE + DBG_FIRST_SPECIAL;
                out.sbyte("DEBUG_OP_X", op);
                line = opNode.line;
                addr = opNode.label.offset;
                break;
            }
        }
        out.sbyte("DBG_END_SEQUENCE", DBG_END_SEQUENCE);
    }

    private void addAdvanceLine(DataOut out, int lineDelta) {
        out.sbyte("DBG_ADVANCE_LINE", DBG_ADVANCE_LINE);
        out.sleb128("offset", lineDelta);
    }

    private void addAdvancePC(DataOut out, int delta) {
        out.sbyte("DBG_ADVANCE_PC", DBG_ADVANCE_PC);
        out.uleb128("offset", delta);
    }

}
