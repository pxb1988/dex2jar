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

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
/* default */interface DexInternalOpcode {
    int OP_MOVE_FROM16 = 0x000002;
    int OP_MOVE_16 = 0x000003;
    int OP_MOVE_WIDE_FROM16 = 0x000005;
    int OP_MOVE_WIDE_16 = 0x000006;
    int OP_MOVE_OBJECT_FROM16 = 0x000008;
    int OP_MOVE_OBJECT_16 = 0x000009;
    int OP_CONST_4 = 0x000012;
    int OP_CONST_16 = 0x000013;
    int OP_CONST_HIGH16 = 0x000015;
    int OP_CONST_WIDE_16 = 0x000016;
    int OP_CONST_WIDE_32 = 0x000017;
    int OP_CONST_WIDE_HIGH16 = 0x000019;
    int OP_CONST_STRING_JUMBO = 0x00001b;
    int OP_GOTO_16 = 0x000029;
    int OP_GOTO_32 = 0x00002a;
    int OP_INVOKE_VIRTUAL_RANGE = 0x000074;
    int OP_INVOKE_SUPER_RANGE = 0x000075;
    int OP_INVOKE_DIRECT_RANGE = 0x000076;
    int OP_INVOKE_STATIC_RANGE = 0x000077;
    int OP_INVOKE_INTERFACE_RANGE = 0x000078;
    int OP_ADD_INT_2ADDR = 0x0000b0;
    int OP_SUB_INT_2ADDR = 0x0000b1;
    int OP_MUL_INT_2ADDR = 0x0000b2;
    int OP_DIV_INT_2ADDR = 0x0000b3;
    int OP_REM_INT_2ADDR = 0x0000b4;
    int OP_AND_INT_2ADDR = 0x0000b5;
    int OP_OR_INT_2ADDR = 0x0000b6;
    int OP_XOR_INT_2ADDR = 0x0000b7;
    int OP_SHL_INT_2ADDR = 0x0000b8;
    int OP_SHR_INT_2ADDR = 0x0000b9;
    int OP_USHR_INT_2ADDR = 0x0000ba;
    int OP_ADD_LONG_2ADDR = 0x0000bb;
    int OP_SUB_LONG_2ADDR = 0x0000bc;
    int OP_MUL_LONG_2ADDR = 0x0000bd;
    int OP_DIV_LONG_2ADDR = 0x0000be;
    int OP_REM_LONG_2ADDR = 0x0000bf;
    int OP_AND_LONG_2ADDR = 0x0000c0;
    int OP_OR_LONG_2ADDR = 0x0000c1;
    int OP_XOR_LONG_2ADDR = 0x0000c2;
    int OP_SHL_LONG_2ADDR = 0x0000c3;
    int OP_SHR_LONG_2ADDR = 0x0000c4;
    int OP_USHR_LONG_2ADDR = 0x0000c5;
    int OP_ADD_FLOAT_2ADDR = 0x0000c6;
    int OP_SUB_FLOAT_2ADDR = 0x0000c7;
    int OP_MUL_FLOAT_2ADDR = 0x0000c8;
    int OP_DIV_FLOAT_2ADDR = 0x0000c9;
    int OP_REM_FLOAT_2ADDR = 0x0000ca;
    int OP_ADD_DOUBLE_2ADDR = 0x0000cb;
    int OP_SUB_DOUBLE_2ADDR = 0x0000cc;
    int OP_MUL_DOUBLE_2ADDR = 0x0000cd;
    int OP_DIV_DOUBLE_2ADDR = 0x0000ce;
    int OP_REM_DOUBLE_2ADDR = 0x0000cf;
    int OP_ADD_INT_LIT16 = 0x0000d0;
    int OP_RSUB_INT = 0x0000d1;
    int OP_MUL_INT_LIT16 = 0x0000d2;
    int OP_DIV_INT_LIT16 = 0x0000d3;
    int OP_REM_INT_LIT16 = 0x0000d4;
    int OP_AND_INT_LIT16 = 0x0000d5;
    int OP_OR_INT_LIT16 = 0x0000d6;
    int OP_XOR_INT_LIT16 = 0x0000d7;
    int OP_ADD_INT_LIT8 = 0x0000d8;
    int OP_RSUB_INT_LIT8 = 0x0000d9;
    int OP_MUL_INT_LIT8 = 0x0000da;
    int OP_DIV_INT_LIT8 = 0x0000db;
    int OP_REM_INT_LIT8 = 0x0000dc;
    int OP_AND_INT_LIT8 = 0x0000dd;
    int OP_OR_INT_LIT8 = 0x0000de;
    int OP_XOR_INT_LIT8 = 0x0000df;
    int OP_SHL_INT_LIT8 = 0x0000e0;
    int OP_SHR_INT_LIT8 = 0x0000e1;
    int OP_USHR_INT_LIT8 = 0x0000e2;
    int OP_IGET_VOLATILE = 0x0000e3;
    int OP_IPUT_VOLATILE = 0x0000e4;
    int OP_SGET_VOLATILE = 0x0000e5;
    int OP_SPUT_VOLATILE = 0x0000e6;
    int OP_IGET_OBJECT_VOLATILE = 0x0000e7;
    int OP_IGET_WIDE_VOLATILE = 0x0000e8;
    int OP_IPUT_WIDE_VOLATILE = 0x0000e9;
    int OP_SGET_WIDE_VOLATILE = 0x0000ea;
    int OP_SPUT_WIDE_VOLATILE = 0x0000eb;
    int OP_EXECUTE_INLINE_RANGE = 0x0000ef;
    int OP_INVOKE_DIRECT_EMPTY = 0x0000f0;//
    int OP_INVOKE_OBJECT_INIT_RANGE = 0x0000f0;//
    int OP_RETURN_VOID_BARRIER = 0x0000f1;
    int OP_INVOKE_VIRTUAL_QUICK_RANGE = 0x0000f9;
    int OP_INVOKE_SUPER_QUICK_RANGE = 0x0000fb;
    int OP_IPUT_OBJECT_VOLATILE = 0x0000fc;
    int OP_SGET_OBJECT_VOLATILE = 0x0000fd;
    int OP_SPUT_OBJECT_VOLATILE = 0x0000fe;
    int OP_CONST_CLASS_JUMBO = 0x00ff00;
    int OP_CHECK_CAST_JUMBO = 0x00ff01;
    int OP_INSTANCE_OF_JUMBO = 0x00ff02;
    int OP_NEW_INSTANCE_JUMBO = 0x00ff03;
    int OP_NEW_ARRAY_JUMBO = 0x00ff04;
    int OP_FILLED_NEW_ARRAY_JUMBO = 0x00ff05;
    int OP_IGET_JUMBO = 0x00ff06;
    int OP_IGET_WIDE_JUMBO = 0x00ff07;
    int OP_IGET_OBJECT_JUMBO = 0x00ff08;
    int OP_IGET_BOOLEAN_JUMBO = 0x00ff09;
    int OP_IGET_BYTE_JUMBO = 0x00ff0a;
    int OP_IGET_CHAR_JUMBO = 0x00ff0b;
    int OP_IGET_SHORT_JUMBO = 0x00ff0c;
    int OP_IPUT_JUMBO = 0x00ff0d;
    int OP_IPUT_WIDE_JUMBO = 0x00ff0e;
    int OP_IPUT_OBJECT_JUMBO = 0x00ff0f;
    int OP_IPUT_BOOLEAN_JUMBO = 0x00ff10;
    int OP_IPUT_BYTE_JUMBO = 0x00ff11;
    int OP_IPUT_CHAR_JUMBO = 0x00ff12;
    int OP_IPUT_SHORT_JUMBO = 0x00ff13;
    int OP_SGET_JUMBO = 0x00ff14;
    int OP_SGET_WIDE_JUMBO = 0x00ff15;
    int OP_SGET_OBJECT_JUMBO = 0x00ff16;
    int OP_SGET_BOOLEAN_JUMBO = 0x00ff17;
    int OP_SGET_BYTE_JUMBO = 0x00ff18;
    int OP_SGET_CHAR_JUMBO = 0x00ff19;
    int OP_SGET_SHORT_JUMBO = 0x00ff1a;
    int OP_SPUT_JUMBO = 0x00ff1b;
    int OP_SPUT_WIDE_JUMBO = 0x00ff1c;
    int OP_SPUT_OBJECT_JUMBO = 0x00ff1d;
    int OP_SPUT_BOOLEAN_JUMBO = 0x00ff1e;
    int OP_SPUT_BYTE_JUMBO = 0x00ff1f;
    int OP_SPUT_CHAR_JUMBO = 0x00ff20;
    int OP_SPUT_SHORT_JUMBO = 0x00ff21;
    int OP_INVOKE_VIRTUAL_JUMBO = 0x00ff22;
    int OP_INVOKE_SUPER_JUMBO = 0x00ff23;
    int OP_INVOKE_DIRECT_JUMBO = 0x00ff24;
    int OP_INVOKE_STATIC_JUMBO = 0x00ff25;
    int OP_INVOKE_INTERFACE_JUMBO = 0x00ff26;
    int OP_INVOKE_OBJECT_INIT_JUMBO = 0x00fff2;
    int OP_IGET_VOLATILE_JUMBO = 0x00fff3;
    int OP_IGET_WIDE_VOLATILE_JUMBO = 0x00fff4;
    int OP_IGET_OBJECT_VOLATILE_JUMBO = 0x00fff5;
    int OP_IPUT_VOLATILE_JUMBO = 0x00fff6;
    int OP_IPUT_WIDE_VOLATILE_JUMBO = 0x00fff7;
    int OP_IPUT_OBJECT_VOLATILE_JUMBO = 0x00fff8;
    int OP_SGET_VOLATILE_JUMBO = 0x00fff9;
    int OP_SGET_WIDE_VOLATILE_JUMBO = 0x00fffa;
    int OP_SGET_OBJECT_VOLATILE_JUMBO = 0x00fffb;
    int OP_SPUT_VOLATILE_JUMBO = 0x00fffc;
    int OP_SPUT_WIDE_VOLATILE_JUMBO = 0x00fffd;
    int OP_SPUT_OBJECT_VOLATILE_JUMBO = 0x00fffe;
}
