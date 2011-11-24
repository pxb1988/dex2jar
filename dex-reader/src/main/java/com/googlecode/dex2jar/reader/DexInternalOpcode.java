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

    int OP_MOVE_FROM16 = 2;
    int OP_MOVE_16 = 3;
    int OP_MOVE_WIDE_FROM16 = 5;
    int OP_MOVE_WIDE_16 = 6;
    int OP_MOVE_OBJECT_FROM16 = 8;
    int OP_MOVE_OBJECT_16 = 9;

    int OP_RETURN_WIDE = 16;
    int OP_RETURN_OBJECT = 17;

    int OP_CONST_4 = 18;
    int OP_CONST_16 = 19;
    int OP_CONST_HIGH16 = 21;
    int OP_CONST_WIDE_16 = 22;
    int OP_CONST_WIDE_32 = 23;
    int OP_CONST_WIDE_HIGH16 = 25;
    int OP_CONST_STRING_JUMBO = 27;
    int OP_UNUSED_3e = 62;
    int OP_UNUSED_3f = 63;
    int OP_UNUSED_40 = 64;
    int OP_UNUSED_41 = 65;
    int OP_UNUSED_42 = 66;
    int OP_UNUSED_43 = 67;

    int OP_AGET_WIDE = 69;
    int OP_AGET_OBJECT = 70;
    int OP_AGET_BOOLEAN = 71;
    int OP_AGET_BYTE = 72;
    int OP_AGET_CHAR = 73;
    int OP_AGET_SHORT = 74;

    int OP_APUT_WIDE = 76;
    int OP_APUT_OBJECT = 77;
    int OP_APUT_BOOLEAN = 78;
    int OP_APUT_BYTE = 79;
    int OP_APUT_CHAR = 80;
    int OP_APUT_SHORT = 81;

    int OP_IGET_WIDE = 83;
    int OP_IGET_OBJECT = 84;
    int OP_IGET_BOOLEAN = 85;
    int OP_IGET_BYTE = 86;
    int OP_IGET_CHAR = 87;
    int OP_IGET_SHORT = 88;
    int OP_IPUT_WIDE = 90;
    int OP_IPUT_OBJECT = 91;
    int OP_IPUT_BOOLEAN = 92;
    int OP_IPUT_BYTE = 93;
    int OP_IPUT_CHAR = 94;
    int OP_IPUT_SHORT = 95;

    int OP_SGET_WIDE = 97;
    int OP_SGET_OBJECT = 98;
    int OP_SGET_BOOLEAN = 99;
    int OP_SGET_BYTE = 100;
    int OP_SGET_CHAR = 101;
    int OP_SGET_SHORT = 102;

    int OP_SPUT_WIDE = 104;
    int OP_SPUT_OBJECT = 105;
    int OP_SPUT_BOOLEAN = 106;
    int OP_SPUT_BYTE = 107;
    int OP_SPUT_CHAR = 108;
    int OP_SPUT_SHORT = 109;

    int OP_UNUSED_73 = 115;
    int OP_INVOKE_VIRTUAL_RANGE = 116;
    int OP_INVOKE_SUPER_RANGE = 117;
    int OP_INVOKE_DIRECT_RANGE = 118;
    int OP_INVOKE_STATIC_RANGE = 119;
    int OP_INVOKE_INTERFACE_RANGE = 120;
    int OP_UNUSED_79 = 121;
    int OP_UNUSED_7A = 122;

    int OP_UNUSED_E3 = 227;
    int OP_UNUSED_E4 = 228;
    int OP_UNUSED_E5 = 229;
    int OP_UNUSED_E6 = 230;
    int OP_UNUSED_E7 = 231;
    int OP_UNUSED_E8 = 232;
    int OP_UNUSED_E9 = 233;
    int OP_UNUSED_EA = 234;
    int OP_UNUSED_EB = 235;
    int OP_UNUSED_EC = 236;
    int OP_UNUSED_ED = 237;

    int OP_UNUSED_EF = 239;

    int OP_UNUSED_F1 = 241;

    int OP_UNUSED_FC = 252;
    int OP_UNUSED_FD = 253;
    int OP_UNUSED_FE = 254;
    int OP_UNUSED_FF = 255;

    int OP_ADD_INT_2ADDR = 176;
    int OP_SUB_INT_2ADDR = 177;
    int OP_MUL_INT_2ADDR = 178;
    int OP_DIV_INT_2ADDR = 179;
    int OP_REM_INT_2ADDR = 180;
    int OP_AND_INT_2ADDR = 181;
    int OP_OR_INT_2ADDR = 182;
    int OP_XOR_INT_2ADDR = 183;
    int OP_SHL_INT_2ADDR = 184;
    int OP_SHR_INT_2ADDR = 185;
    int OP_USHR_INT_2ADDR = 186;
    int OP_ADD_LONG_2ADDR = 187;
    int OP_SUB_LONG_2ADDR = 188;
    int OP_MUL_LONG_2ADDR = 189;
    int OP_DIV_LONG_2ADDR = 190;
    int OP_REM_LONG_2ADDR = 191;
    int OP_AND_LONG_2ADDR = 192;
    int OP_OR_LONG_2ADDR = 193;
    int OP_XOR_LONG_2ADDR = 194;
    int OP_SHL_LONG_2ADDR = 195;
    int OP_SHR_LONG_2ADDR = 196;
    int OP_USHR_LONG_2ADDR = 197;
    int OP_ADD_FLOAT_2ADDR = 198;
    int OP_SUB_FLOAT_2ADDR = 199;
    int OP_MUL_FLOAT_2ADDR = 200;
    int OP_DIV_FLOAT_2ADDR = 201;
    int OP_REM_FLOAT_2ADDR = 202;
    int OP_ADD_DOUBLE_2ADDR = 203;
    int OP_SUB_DOUBLE_2ADDR = 204;
    int OP_MUL_DOUBLE_2ADDR = 205;
    int OP_DIV_DOUBLE_2ADDR = 206;
    int OP_REM_DOUBLE_2ADDR = 207;
    int OP_ADD_INT_LIT16 = 208;
    int OP_RSUB_INT = 209;
    int OP_MUL_INT_LIT16 = 210;
    int OP_DIV_INT_LIT16 = 211;
    int OP_REM_INT_LIT16 = 212;
    int OP_AND_INT_LIT16 = 213;
    int OP_OR_INT_LIT16 = 214;
    int OP_XOR_INT_LIT16 = 215;
    int OP_ADD_INT_LIT8 = 216;
    int OP_RSUB_INT_LIT8 = 217;
    int OP_MUL_INT_LIT8 = 218;
    int OP_DIV_INT_LIT8 = 219;
    int OP_REM_INT_LIT8 = 220;
    int OP_AND_INT_LIT8 = 221;
    int OP_OR_INT_LIT8 = 222;
    int OP_XOR_INT_LIT8 = 223;
    int OP_SHL_INT_LIT8 = 224;
    int OP_SHR_INT_LIT8 = 225;
    int OP_USHR_INT_LIT8 = 226;

    int OP_GOTO_16 = 41;
    int OP_GOTO_32 = 42;

    int OP_EXECUTE_INLINE = 238;

    int OP_INVOKE_DIRECT_EMPTY = 240;

    int OP_IGET_QUICK = 242;
    int OP_IGET_WIDE_QUICK = 243;
    int OP_IGET_OBJECT_QUICK = 244;
    int OP_IPUT_QUICK = 245;
    int OP_IPUT_WIDE_QUICK = 246;
    int OP_IPUT_OBJECT_QUICK = 247;
    int OP_INVOKE_VIRTUAL_QUICK = 248;
    int OP_INVOKE_VIRTUAL_QUICK_RANGE = 249;
    int OP_INVOKE_SUPER_QUICK = 250;
    int OP_INVOKE_SUPER_QUICK_RANGE = 251;

    int OP_CONST_CLASS_JUMBO = 0xFF00;
    int OP_CHECK_CAST_JUMBO = 0xFF01;
    int OP_INSTANCE_OF_JUMBO = 0xFF02;
    int OP_NEW_INSTANCE_JUMBO = 0xFF03;
    int OP_NEW_ARRAY_JUMBO = 0xFF04;
    int OP_FILLED_NEW_ARRAY_JUMBO = 0xFF05;
    int OP_IGET_JUMBO = 0xFF06;
    int OP_IGET_WIDE_JUMBO = 0xFF07;
    int OP_IGET_OBJECT_JUMBO = 0xFF08;
    int OP_IGET_BOOLEAN_JUMBO = 0xFF09;
    int OP_IGET_BYTE_JUMBO = 0xFF0a;
    int OP_IGET_CHAR_JUMBO = 0xFF0b;
    int OP_IGET_SHORT_JUMBO = 0xFF0c;
    int OP_IPUT_JUMBO = 0xFF0d;
    int OP_IPUT_WIDE_JUMBO = 0xFF0e;
    int OP_IPUT_OBJECT_JUMBO = 0xFF0f;
    int OP_IPUT_BOOLEAN_JUMBO = 0xFF10;
    int OP_IPUT_BYTE_JUMBO = 0xFF11;
    int OP_IPUT_CHAR_JUMBO = 0xFF12;
    int OP_IPUT_SHORT_JUMBO = 0xFF13;
    int OP_SGET_JUMBO = 0xFF14;
    int OP_SGET_WIDE_JUMBO = 0xFF15;
    int OP_SGET_OBJECT_JUMBO = 0xFF16;
    int OP_SGET_BOOLEAN_JUMBO = 0xFF17;
    int OP_SGET_BYTE_JUMBO = 0xFF18;
    int OP_SGET_CHAR_JUMBO = 0xFF19;
    int OP_SGET_SHORT_JUMBO = 0xFF1a;
    int OP_SPUT_JUMBO = 0xFF1b;
    int OP_SPUT_WIDE_JUMBO = 0xFF1c;
    int OP_SPUT_OBJECT_JUMBO = 0xFF1d;
    int OP_SPUT_BOOLEAN_JUMBO = 0xFF1e;
    int OP_SPUT_BYTE_JUMBO = 0xFF1f;
    int OP_SPUT_CHAR_JUMBO = 0xFF20;
    int OP_SPUT_SHORT_JUMBO = 0xFF21;
    int OP_INVOKE_VIRTUAL_JUMBO = 0xFF22;
    int OP_INVOKE_SUPER_JUMBO = 0xFF23;
    int OP_INVOKE_DIRECT_JUMBO = 0xFF24;
    int OP_INVOKE_STATIC_JUMBO = 0xFF25;
    int OP_INVOKE_INTERFACE_JUMBO = 0xFF26;
}
