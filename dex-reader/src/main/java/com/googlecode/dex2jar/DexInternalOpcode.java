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
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public interface DexInternalOpcode {

    public static final int OP_UNUSED_3e = 62;
    public static final int OP_UNUSED_3f = 63;
    public static final int OP_UNUSED_40 = 64;
    public static final int OP_UNUSED_41 = 65;
    public static final int OP_UNUSED_42 = 66;
    public static final int OP_UNUSED_43 = 67;

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

}
