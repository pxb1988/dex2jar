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
package com.googlecode.dex2jar;

/**
 * dex的指令
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public abstract interface DexOpcodes {

    int ACC_PUBLIC = 0x0001; // class, field, method
    int ACC_PRIVATE = 0x0002; // class, field, method
    int ACC_PROTECTED = 0x0004; // class, field, method
    int ACC_STATIC = 0x0008; // field, method
    int ACC_FINAL = 0x0010; // class, field, method
    // int ACC_SUPER = 0x0020; // class
    int ACC_SYNCHRONIZED = 0x0020; // method
    int ACC_VOLATILE = 0x0040; // field
    int ACC_BRIDGE = 0x0040; // method
    int ACC_VARARGS = 0x0080; // method
    int ACC_TRANSIENT = 0x0080; // field
    int ACC_NATIVE = 0x0100; // method
    int ACC_INTERFACE = 0x0200; // class
    int ACC_ABSTRACT = 0x0400; // class, method
    int ACC_STRICT = 0x0800; // method
    int ACC_SYNTHETIC = 0x1000; // class, field, method
    int ACC_ANNOTATION = 0x2000; // class
    int ACC_ENUM = 0x4000; // class(?) field inner
    int ACC_CONSTRUCTOR = 0x10000;// constructor method (class or instance initializer)
    int ACC_DECLARED_SYNCHRONIZED = 0x20000;

    public static final int OP_NOP = 0;
    public static final int OP_MOVE = 1;
    public static final int OP_MOVE_WIDE = 4;
    public static final int OP_MOVE_OBJECT = 7;
    public static final int OP_MOVE_RESULT = 10;
    public static final int OP_MOVE_RESULT_WIDE = 11;
    public static final int OP_MOVE_RESULT_OBJECT = 12;
    public static final int OP_MOVE_EXCEPTION = 13;
    public static final int OP_RETURN_VOID = 14;
    public static final int OP_RETURN = 15;

    public static final int OP_CONST = 20;
    public static final int OP_CONST_WIDE = 24;
    public static final int OP_CONST_STRING = 26;
    public static final int OP_CONST_CLASS = 28;
    public static final int OP_MONITOR_ENTER = 29;
    public static final int OP_MONITOR_EXIT = 30;
    public static final int OP_CHECK_CAST = 31;
    public static final int OP_INSTANCE_OF = 32;
    public static final int OP_ARRAY_LENGTH = 33;
    public static final int OP_NEW_INSTANCE = 34;
    public static final int OP_NEW_ARRAY = 35;
    public static final int OP_FILLED_NEW_ARRAY = 36;
    public static final int OP_FILLED_NEW_ARRAY_RANGE = 37;
    public static final int OP_FILL_ARRAY_DATA = 38;
    public static final int OP_THROW = 39;
    public static final int OP_GOTO = 40;

    public static final int OP_PACKED_SWITCH = 43;
    public static final int OP_SPARSE_SWITCH = 44;
    public static final int OP_CMPL_FLOAT = 45;
    public static final int OP_CMPG_FLOAT = 46;
    public static final int OP_CMPL_DOUBLE = 47;
    public static final int OP_CMPG_DOUBLE = 48;
    public static final int OP_CMP_LONG = 49;
    public static final int OP_IF_EQ = 50;
    public static final int OP_IF_NE = 51;
    public static final int OP_IF_LT = 52;
    public static final int OP_IF_GE = 53;
    public static final int OP_IF_GT = 54;
    public static final int OP_IF_LE = 55;
    public static final int OP_IF_EQZ = 56;
    public static final int OP_IF_NEZ = 57;
    public static final int OP_IF_LTZ = 58;
    public static final int OP_IF_GEZ = 59;
    public static final int OP_IF_GTZ = 60;
    public static final int OP_IF_LEZ = 61;

    public static final int OP_AGET = 68;

    public static final int OP_APUT = 75;

    public static final int OP_IGET = 82;

    public static final int OP_IPUT = 89;

    public static final int OP_SGET = 96;

    public static final int OP_SPUT = 103;

    public static final int OP_INVOKE_VIRTUAL = 110;
    public static final int OP_INVOKE_SUPER = 111;
    public static final int OP_INVOKE_DIRECT = 112;
    public static final int OP_INVOKE_STATIC = 113;
    public static final int OP_INVOKE_INTERFACE = 114;

    public static final int OP_NEG_INT = 123;
    public static final int OP_NOT_INT = 124;
    public static final int OP_NEG_LONG = 125;
    public static final int OP_NOT_LONG = 126;
    public static final int OP_NEG_FLOAT = 127;
    public static final int OP_NEG_DOUBLE = 128;
    public static final int OP_INT_TO_LONG = 129;
    public static final int OP_INT_TO_FLOAT = 130;
    public static final int OP_INT_TO_DOUBLE = 131;
    public static final int OP_LONG_TO_INT = 132;
    public static final int OP_LONG_TO_FLOAT = 133;
    public static final int OP_LONG_TO_DOUBLE = 134;
    public static final int OP_FLOAT_TO_INT = 135;
    public static final int OP_FLOAT_TO_LONG = 136;
    public static final int OP_FLOAT_TO_DOUBLE = 137;
    public static final int OP_DOUBLE_TO_INT = 138;
    public static final int OP_DOUBLE_TO_LONG = 139;
    public static final int OP_DOUBLE_TO_FLOAT = 140;
    public static final int OP_INT_TO_BYTE = 141;
    public static final int OP_INT_TO_CHAR = 142;
    public static final int OP_INT_TO_SHORT = 143;
    public static final int OP_ADD_INT = 144;
    public static final int OP_SUB_INT = 145;
    public static final int OP_MUL_INT = 146;
    public static final int OP_DIV_INT = 147;
    public static final int OP_REM_INT = 148;
    public static final int OP_AND_INT = 149;
    public static final int OP_OR_INT = 150;
    public static final int OP_XOR_INT = 151;
    public static final int OP_SHL_INT = 152;
    public static final int OP_SHR_INT = 153;
    public static final int OP_USHR_INT = 154;
    public static final int OP_ADD_LONG = 155;
    public static final int OP_SUB_LONG = 156;
    public static final int OP_MUL_LONG = 157;
    public static final int OP_DIV_LONG = 158;
    public static final int OP_REM_LONG = 159;
    public static final int OP_AND_LONG = 160;
    public static final int OP_OR_LONG = 161;
    public static final int OP_XOR_LONG = 162;
    public static final int OP_SHL_LONG = 163;
    public static final int OP_SHR_LONG = 164;
    public static final int OP_USHR_LONG = 165;
    public static final int OP_ADD_FLOAT = 166;
    public static final int OP_SUB_FLOAT = 167;
    public static final int OP_MUL_FLOAT = 168;
    public static final int OP_DIV_FLOAT = 169;
    public static final int OP_REM_FLOAT = 170;
    public static final int OP_ADD_DOUBLE = 171;
    public static final int OP_SUB_DOUBLE = 172;
    public static final int OP_MUL_DOUBLE = 173;
    public static final int OP_DIV_DOUBLE = 174;
    public static final int OP_REM_DOUBLE = 175;

    public static final int OP_ADD_INT_LIT_X = 0x00FF0000 | 216;
    public static final int OP_RSUB_INT_LIT_X = 0x00FF0000 | 217;
    public static final int OP_MUL_INT_LIT_X = 0x00FF0000 | 218;
    public static final int OP_DIV_INT_LIT_X = 0x00FF0000 | 219;
    public static final int OP_REM_INT_LIT_X = 0x00FF0000 | 220;
    public static final int OP_AND_INT_LIT_X = 0x00FF0000 | 221;
    public static final int OP_OR_INT_LIT_X = 0x00FF0000 | 222;
    public static final int OP_XOR_INT_LIT_X = 0x00FF0000 | 223;
    public static final int OP_SHL_INT_LIT_X = 0x00FF0000 | 224;
    public static final int OP_SHR_INT_LIT_X = 0x00FF0000 | 225;
    public static final int OP_USHR_INT_LIT_X = 0x00FF0000 | 226;
}