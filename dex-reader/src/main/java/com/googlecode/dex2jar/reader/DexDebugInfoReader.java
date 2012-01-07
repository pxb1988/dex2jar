/*
 * Copyright (c) 2009-2012 Panxiaobo
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

import com.googlecode.dex2jar.reader.io.DataIn;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;

/**
 * 读取debug信息
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
/* default */class DexDebugInfoReader {
    public static final int DBG_END_SEQUENCE = 0;
    public static final int DBG_ADVANCE_PC = 1;
    public static final int DBG_ADVANCE_LINE = 2;
    public static final int DBG_START_LOCAL = 3;
    public static final int DBG_START_LOCAL_EXTENDED = 4;
    public static final int DBG_END_LOCAL = 5;
    public static final int DBG_RESTART_LOCAL = 6;
    public static final int DBG_SET_PROLOGUE_END = 7;
    public static final int DBG_SET_EPILOGUE_BEGIN = 8;
    public static final int DBG_SET_FILE = 9;
    public static final int DBG_FIRST_SPECIAL = 10;
    public static final int DBG_LINE_BASE = -4;
    public static final int DBG_LINE_RANGE = 15;
    private DataIn in;
    private DexFileReader dex;

    private int instruction_size;
    private DexCodeReader codeReader;
    LocalVariable variableList[];
    int args[];

    /**
     * @param in
     * @param dex
     */
    public DexDebugInfoReader(DataIn in, DexFileReader dex, int instruction_size, DexCodeReader codeReader,
            LocalVariable localVariables[], int args[]) {
        super();
        this.in = in;
        this.dex = dex;
        this.instruction_size = instruction_size;
        this.codeReader = codeReader;
        this.variableList = localVariables;
        this.args = args;
    }

    public static class LocalVariable {
        public int start;
        public int end;
        public String name;
        public String type;
        public String signature;
        public int reg;

        /**
         * @param start
         * @param end
         * @param name
         * @param type
         * @param signature
         */
        public LocalVariable(int reg, int start, int end, String name, String type, String signature) {
            super();
            this.reg = reg;
            this.start = start;
            this.end = end;
            this.name = name;
            this.type = type;
            this.signature = signature;
        }
    }

    /**
     * 处理
     * 
     * @param dcv
     */
    public void accept(DexCodeVisitor dcv) {
        // System.out.println("==");
        DataIn in = this.in;
        int line = (int) in.readULeb128();
        {
            int szParams = (int) in.readULeb128();
            int offset = szParams == this.args.length ? 0 : 1;
            for (int i = 0; i < szParams; i++) {
                int string_offset = (int) in.readULeb128() - 1;
                if (string_offset < 0) {// NO_INDEX
                    this.variableList[this.args[i + offset]] = null;// remove the variable
                } else {
                    String psName = dex.getString(string_offset);
                    this.variableList[this.args[i + offset]].name = psName;
                }
            }
        }
        int pcOffset = 0;
        {// init line
            codeReader.order(pcOffset);
            dcv.visitLineNumber(line, codeReader.labels.get(pcOffset));
        }

        l1: while (true) {
            int opcode = in.readUByte();
            switch (opcode) {
            case DBG_END_SEQUENCE:
                break l1;
            case DBG_ADVANCE_PC: {
                int offset = (int) in.readULeb128();
                pcOffset += offset;
            }
                break;
            case DBG_ADVANCE_LINE: {
                int offset = (int) in.readULeb128();
                line += offset;
            }
                break;
            case DBG_START_LOCAL: {
                int regNum = (int) in.readULeb128();
                int nameIdx = (int) in.readULeb128() - 1;
                int typeIdx = (int) in.readULeb128() - 1;
                if ((nameIdx >= 0) && (typeIdx >= 0)) {
                    codeReader.order(pcOffset);
                    LocalVariable localVariable = new LocalVariable(regNum, pcOffset, -1, dex.getString(nameIdx),
                            dex.getType(typeIdx), null);
                    variableList[regNum] = localVariable;
                }
            }
                break;
            case DBG_START_LOCAL_EXTENDED: {
                int regNum = (int) in.readULeb128();
                int nameIdx = (int) in.readULeb128() - 1;
                int typeIdx = (int) in.readULeb128() - 1;
                int sigIdx = (int) in.readULeb128() - 1;
                if ((nameIdx >= 0) && (typeIdx >= 0)) {
                    codeReader.order(pcOffset);
                    LocalVariable localVariable = new LocalVariable(regNum, pcOffset, -1, dex.getString(nameIdx),
                            dex.getType(typeIdx), dex.getString(sigIdx));
                    variableList[regNum] = localVariable;
                }
            }
                break;
            case DBG_END_LOCAL: {
                int regNum = (int) in.readULeb128();
                LocalVariable v = variableList[regNum];
                codeReader.order(pcOffset);
                v.end = pcOffset;
                // System.out.println(v.start + " - " + pcOffset);
                dcv.visitLocalVariable(v.name, v.type, v.signature, codeReader.labels.get(v.start),
                        codeReader.labels.get(pcOffset), v.reg);

            }
                break;
            case DBG_RESTART_LOCAL: {
                int regNum = (int) in.readULeb128();
                LocalVariable v = variableList[regNum];
                v.start = pcOffset;
                v.end = -1;
                codeReader.order(pcOffset);
            }
                break;
            case DBG_SET_PROLOGUE_END: {
            }
                break;

            case DBG_SET_EPILOGUE_BEGIN: {
            }
                break;

            case DBG_SET_FILE: {
                in.readULeb128();// skip source file in debug
                // int sourceFileIdx = (int) in.readULeb128() - 1;
            }
                break;
            default: {
                int adjustedOpcode = opcode - DBG_FIRST_SPECIAL;
                line += DBG_LINE_BASE + (adjustedOpcode % DBG_LINE_RANGE);
                if (adjustedOpcode / DBG_LINE_RANGE != 0) {
                    pcOffset += (adjustedOpcode / DBG_LINE_RANGE);
                    codeReader.order(pcOffset);
                    dcv.visitLineNumber(line, codeReader.labels.get(pcOffset));
                }
            }
            }
        }
        for (LocalVariable v : variableList) {
            if (v != null && v.end < 0) {
                codeReader.order(this.instruction_size);
                // System.out.println(v.start + " - " + instruction_size);
                dcv.visitLocalVariable(v.name, v.type, v.signature, codeReader.labels.get(v.start),
                        codeReader.labels.get(instruction_size), v.reg);
            }
        }
    }
}
