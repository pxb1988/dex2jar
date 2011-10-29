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

    public static final int OP_MOVE_FROM16 = 2;
    public static final int OP_MOVE_16 = 3;
    public static final int OP_MOVE_WIDE_FROM16 = 5;
    public static final int OP_MOVE_WIDE_16 = 6;
    public static final int OP_MOVE_OBJECT_FROM16 = 8;
    public static final int OP_MOVE_OBJECT_16 = 9;

    public static final int OP_RETURN_WIDE = 16;
    public static final int OP_RETURN_OBJECT = 17;

    public static final int OP_CONST_4 = 18;
    public static final int OP_CONST_16 = 19;
    public static final int OP_CONST_HIGH16 = 21;
    public static final int OP_CONST_WIDE_16 = 22;
    public static final int OP_CONST_WIDE_32 = 23;
    public static final int OP_CONST_WIDE_HIGH16 = 25;
    public static final int OP_CONST_STRING_JUMBO = 27;
    public static final int OP_UNUSED_3e = 62;
    public static final int OP_UNUSED_3f = 63;
    public static final int OP_UNUSED_40 = 64;
    public static final int OP_UNUSED_41 = 65;
    public static final int OP_UNUSED_42 = 66;
    public static final int OP_UNUSED_43 = 67;

    public static final int OP_AGET_WIDE = 69;
    public static final int OP_AGET_OBJECT = 70;
    public static final int OP_AGET_BOOLEAN = 71;
    public static final int OP_AGET_BYTE = 72;
    public static final int OP_AGET_CHAR = 73;
    public static final int OP_AGET_SHORT = 74;

    public static final int OP_APUT_WIDE = 76;
    public static final int OP_APUT_OBJECT = 77;
    public static final int OP_APUT_BOOLEAN = 78;
    public static final int OP_APUT_BYTE = 79;
    public static final int OP_APUT_CHAR = 80;
    public static final int OP_APUT_SHORT = 81;

    public static final int OP_IGET_WIDE = 83;
    public static final int OP_IGET_OBJECT = 84;
    public static final int OP_IGET_BOOLEAN = 85;
    public static final int OP_IGET_BYTE = 86;
    public static final int OP_IGET_CHAR = 87;
    public static final int OP_IGET_SHORT = 88;
    public static final int OP_IPUT_WIDE = 90;
    public static final int OP_IPUT_OBJECT = 91;
    public static final int OP_IPUT_BOOLEAN = 92;
    public static final int OP_IPUT_BYTE = 93;
    public static final int OP_IPUT_CHAR = 94;
    public static final int OP_IPUT_SHORT = 95;

    public static final int OP_SGET_WIDE = 97;
    public static final int OP_SGET_OBJECT = 98;
    public static final int OP_SGET_BOOLEAN = 99;
    public static final int OP_SGET_BYTE = 100;
    public static final int OP_SGET_CHAR = 101;
    public static final int OP_SGET_SHORT = 102;

    public static final int OP_SPUT_WIDE = 104;
    public static final int OP_SPUT_OBJECT = 105;
    public static final int OP_SPUT_BOOLEAN = 106;
    public static final int OP_SPUT_BYTE = 107;
    public static final int OP_SPUT_CHAR = 108;
    public static final int OP_SPUT_SHORT = 109;

    public static final int OP_UNUSED_73 = 115;
    public static final int OP_INVOKE_VIRTUAL_RANGE = 116;
    public static final int OP_INVOKE_SUPER_RANGE = 117;
    public static final int OP_INVOKE_DIRECT_RANGE = 118;
    public static final int OP_INVOKE_STATIC_RANGE = 119;
    public static final int OP_INVOKE_INTERFACE_RANGE = 120;
    public static final int OP_UNUSED_79 = 121;
    public static final int OP_UNUSED_7A = 122;

    public static final int OP_UNUSED_E3 = 227;
    public static final int OP_UNUSED_E4 = 228;
    public static final int OP_UNUSED_E5 = 229;
    public static final int OP_UNUSED_E6 = 230;
    public static final int OP_UNUSED_E7 = 231;
    public static final int OP_UNUSED_E8 = 232;
    public static final int OP_UNUSED_E9 = 233;
    public static final int OP_UNUSED_EA = 234;
    public static final int OP_UNUSED_EB = 235;
    public static final int OP_UNUSED_EC = 236;
    public static final int OP_UNUSED_ED = 237;

    public static final int OP_UNUSED_EF = 239;

    public static final int OP_UNUSED_F1 = 241;

    public static final int OP_UNUSED_FC = 252;
    public static final int OP_UNUSED_FD = 253;
    public static final int OP_UNUSED_FE = 254;
    public static final int OP_UNUSED_FF = 255;

    public static final int OP_ADD_INT_2ADDR = 176;
    public static final int OP_SUB_INT_2ADDR = 177;
    public static final int OP_MUL_INT_2ADDR = 178;
    public static final int OP_DIV_INT_2ADDR = 179;
    public static final int OP_REM_INT_2ADDR = 180;
    public static final int OP_AND_INT_2ADDR = 181;
    public static final int OP_OR_INT_2ADDR = 182;
    public static final int OP_XOR_INT_2ADDR = 183;
    public static final int OP_SHL_INT_2ADDR = 184;
    public static final int OP_SHR_INT_2ADDR = 185;
    public static final int OP_USHR_INT_2ADDR = 186;
    public static final int OP_ADD_LONG_2ADDR = 187;
    public static final int OP_SUB_LONG_2ADDR = 188;
    public static final int OP_MUL_LONG_2ADDR = 189;
    public static final int OP_DIV_LONG_2ADDR = 190;
    public static final int OP_REM_LONG_2ADDR = 191;
    public static final int OP_AND_LONG_2ADDR = 192;
    public static final int OP_OR_LONG_2ADDR = 193;
    public static final int OP_XOR_LONG_2ADDR = 194;
    public static final int OP_SHL_LONG_2ADDR = 195;
    public static final int OP_SHR_LONG_2ADDR = 196;
    public static final int OP_USHR_LONG_2ADDR = 197;
    public static final int OP_ADD_FLOAT_2ADDR = 198;
    public static final int OP_SUB_FLOAT_2ADDR = 199;
    public static final int OP_MUL_FLOAT_2ADDR = 200;
    public static final int OP_DIV_FLOAT_2ADDR = 201;
    public static final int OP_REM_FLOAT_2ADDR = 202;
    public static final int OP_ADD_DOUBLE_2ADDR = 203;
    public static final int OP_SUB_DOUBLE_2ADDR = 204;
    public static final int OP_MUL_DOUBLE_2ADDR = 205;
    public static final int OP_DIV_DOUBLE_2ADDR = 206;
    public static final int OP_REM_DOUBLE_2ADDR = 207;
    public static final int OP_ADD_INT_LIT16 = 208;
    public static final int OP_RSUB_INT = 209;
    public static final int OP_MUL_INT_LIT16 = 210;
    public static final int OP_DIV_INT_LIT16 = 211;
    public static final int OP_REM_INT_LIT16 = 212;
    public static final int OP_AND_INT_LIT16 = 213;
    public static final int OP_OR_INT_LIT16 = 214;
    public static final int OP_XOR_INT_LIT16 = 215;
    public static final int OP_ADD_INT_LIT8 = 216;
    public static final int OP_RSUB_INT_LIT8 = 217;
    public static final int OP_MUL_INT_LIT8 = 218;
    public static final int OP_DIV_INT_LIT8 = 219;
    public static final int OP_REM_INT_LIT8 = 220;
    public static final int OP_AND_INT_LIT8 = 221;
    public static final int OP_OR_INT_LIT8 = 222;
    public static final int OP_XOR_INT_LIT8 = 223;
    public static final int OP_SHL_INT_LIT8 = 224;
    public static final int OP_SHR_INT_LIT8 = 225;
    public static final int OP_USHR_INT_LIT8 = 226;

    public static final int OP_GOTO_16 = 41;
    public static final int OP_GOTO_32 = 42;
}
