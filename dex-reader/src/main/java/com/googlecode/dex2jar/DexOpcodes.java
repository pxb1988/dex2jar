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

    int OP_NOP = 0x000000;
    int OP_MOVE = 0x000001;
    int OP_MOVE_WIDE = 0x000004;
    int OP_MOVE_OBJECT = 0x000007;
    int OP_MOVE_RESULT = 0x00000a;
    int OP_MOVE_RESULT_WIDE = 0x00000b;
    int OP_MOVE_RESULT_OBJECT = 0x00000c;
    int OP_MOVE_EXCEPTION = 0x00000d;
    int OP_RETURN_VOID = 0x00000e;
    int OP_RETURN = 0x00000f;
    int OP_CONST = 0x000014;
    int OP_CONST_WIDE = 0x000018;
    int OP_CONST_STRING = 0x00001a;
    int OP_CONST_CLASS = 0x00001c;
    int OP_MONITOR_ENTER = 0x00001d;
    int OP_MONITOR_EXIT = 0x00001e;
    int OP_CHECK_CAST = 0x00001f;
    int OP_INSTANCE_OF = 0x000020;
    int OP_ARRAY_LENGTH = 0x000021;
    int OP_NEW_INSTANCE = 0x000022;
    int OP_NEW_ARRAY = 0x000023;
    int OP_FILLED_NEW_ARRAY = 0x000024;
    int OP_FILLED_NEW_ARRAY_RANGE = 0x000025;
    int OP_FILL_ARRAY_DATA = 0x000026;
    int OP_THROW = 0x000027;
    int OP_GOTO = 0x000028;
    int OP_PACKED_SWITCH = 0x00002b;
    int OP_SPARSE_SWITCH = 0x00002c;
    int OP_CMPL_FLOAT = 0x00002d;
    int OP_CMPG_FLOAT = 0x00002e;
    int OP_CMPL_DOUBLE = 0x00002f;
    int OP_CMPG_DOUBLE = 0x000030;
    int OP_CMP_LONG = 0x000031;
    int OP_IF_EQ = 0x000032;
    int OP_IF_NE = 0x000033;
    int OP_IF_LT = 0x000034;
    int OP_IF_GE = 0x000035;
    int OP_IF_GT = 0x000036;
    int OP_IF_LE = 0x000037;
    int OP_IF_EQZ = 0x000038;
    int OP_IF_NEZ = 0x000039;
    int OP_IF_LTZ = 0x00003a;
    int OP_IF_GEZ = 0x00003b;
    int OP_IF_GTZ = 0x00003c;
    int OP_IF_LEZ = 0x00003d;
    int OP_AGET = 0x000044;
    int OP_APUT = 0x00004b;
    int OP_IGET = 0x000052;
    int OP_IPUT = 0x000059;
    int OP_SGET = 0x000060;
    int OP_SPUT = 0x000067;
    int OP_INVOKE_VIRTUAL = 0x00006e;
    int OP_INVOKE_SUPER = 0x00006f;
    int OP_INVOKE_DIRECT = 0x000070;
    int OP_INVOKE_STATIC = 0x000071;
    int OP_INVOKE_INTERFACE = 0x000072;
    int OP_NEG_INT = 0x00007b;
    int OP_NOT_INT = 0x00007c;
    int OP_NEG_LONG = 0x00007d;
    int OP_NOT_LONG = 0x00007e;
    int OP_NEG_FLOAT = 0x00007f;
    int OP_NEG_DOUBLE = 0x000080;
    int OP_INT_TO_LONG = 0x000081;
    int OP_INT_TO_FLOAT = 0x000082;
    int OP_INT_TO_DOUBLE = 0x000083;
    int OP_LONG_TO_INT = 0x000084;
    int OP_LONG_TO_FLOAT = 0x000085;
    int OP_LONG_TO_DOUBLE = 0x000086;
    int OP_FLOAT_TO_INT = 0x000087;
    int OP_FLOAT_TO_LONG = 0x000088;
    int OP_FLOAT_TO_DOUBLE = 0x000089;
    int OP_DOUBLE_TO_INT = 0x00008a;
    int OP_DOUBLE_TO_LONG = 0x00008b;
    int OP_DOUBLE_TO_FLOAT = 0x00008c;
    int OP_INT_TO_BYTE = 0x00008d;
    int OP_INT_TO_CHAR = 0x00008e;
    int OP_INT_TO_SHORT = 0x00008f;
    int OP_ADD_INT = 0x000090;
    int OP_SUB_INT = 0x000091;
    int OP_MUL_INT = 0x000092;
    int OP_DIV_INT = 0x000093;
    int OP_REM_INT = 0x000094;
    int OP_AND_INT = 0x000095;
    int OP_OR_INT = 0x000096;
    int OP_XOR_INT = 0x000097;
    int OP_SHL_INT = 0x000098;
    int OP_SHR_INT = 0x000099;
    int OP_USHR_INT = 0x00009a;
    int OP_ADD_LONG = 0x00009b;
    int OP_SUB_LONG = 0x00009c;
    int OP_MUL_LONG = 0x00009d;
    int OP_DIV_LONG = 0x00009e;
    int OP_REM_LONG = 0x00009f;
    int OP_AND_LONG = 0x0000a0;
    int OP_OR_LONG = 0x0000a1;
    int OP_XOR_LONG = 0x0000a2;
    int OP_SHL_LONG = 0x0000a3;
    int OP_SHR_LONG = 0x0000a4;
    int OP_USHR_LONG = 0x0000a5;
    int OP_ADD_FLOAT = 0x0000a6;
    int OP_SUB_FLOAT = 0x0000a7;
    int OP_MUL_FLOAT = 0x0000a8;
    int OP_DIV_FLOAT = 0x0000a9;
    int OP_REM_FLOAT = 0x0000aa;
    int OP_ADD_DOUBLE = 0x0000ab;
    int OP_SUB_DOUBLE = 0x0000ac;
    int OP_MUL_DOUBLE = 0x0000ad;
    int OP_DIV_DOUBLE = 0x0000ae;
    int OP_REM_DOUBLE = 0x0000af;
    int OP_ADD_INT_LIT_X = 0xff00d8;
    int OP_RSUB_INT_LIT_X = 0xff00d9;
    int OP_MUL_INT_LIT_X = 0xff00da;
    int OP_DIV_INT_LIT_X = 0xff00db;
    int OP_REM_INT_LIT_X = 0xff00dc;
    int OP_AND_INT_LIT_X = 0xff00dd;
    int OP_OR_INT_LIT_X = 0xff00de;
    int OP_XOR_INT_LIT_X = 0xff00df;
    int OP_SHL_INT_LIT_X = 0xff00e0;
    int OP_SHR_INT_LIT_X = 0xff00e1;
    int OP_USHR_INT_LIT_X = 0xff00e2;
}