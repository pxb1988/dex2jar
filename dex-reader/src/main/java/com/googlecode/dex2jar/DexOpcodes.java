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
package com.googlecode.dex2jar;

/**
 * dex2jar dex instruction set. This is different from the <b>dalvik instruction</b>
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
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

    int TYPE_SINGLE = 0;
    int TYPE_WIDE = 1;
    int TYPE_OBJECT = 2;
    int TYPE_BOOLEAN = 3;
    int TYPE_BYTE = 4;
    int TYPE_CHAR = 5;
    int TYPE_SHORT = 6;
    int TYPE_INT = 7;
    int TYPE_FLOAT = 8;
    int TYPE_LONG = 9;
    int TYPE_DOUBLE = 10;

    int OP_NOP = 0x000000;
    int OP_MOVE = 0x000001;
    int OP_MOVE_RESULT = 0x00000a;
    int OP_MOVE_EXCEPTION = 0x00000d;
    int OP_RETURN_VOID = 0x00000e;
    int OP_RETURN = 0x00000f;
    int OP_CONST = 0x000014;
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
    int OP_FILL_ARRAY_DATA = 0x000026;
    int OP_THROW = 0x000027;
    int OP_GOTO = 0x000028;
    int OP_PACKED_SWITCH = 0x00002b;
    int OP_SPARSE_SWITCH = 0x00002c;
    int OP_CMPL = 0xff002f;
    int OP_CMPG = 0xff0030;
    int OP_CMP = 0xff0031;
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
    int OP_NEG = 0xff007b;
    int OP_NOT = 0xff007c;
    int OP_X_TO_Y = 0xff0081;
    int OP_ADD = 0xff0090;
    int OP_SUB = 0xff0091;
    int OP_MUL = 0xff0092;
    int OP_DIV = 0xff0093;
    int OP_REM = 0xff0094;
    int OP_AND = 0xff0095;
    int OP_OR = 0xff0096;
    int OP_XOR = 0xff0097;
    int OP_SHL = 0xff0098;
    int OP_SHR = 0xff0099;
    int OP_USHR = 0xff009a;
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