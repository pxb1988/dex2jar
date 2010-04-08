/*
 * Copyright (c) 2009-2010 Panxiaobo
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
package pxb.android.dex2jar;

/**
 * dex的指令
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public abstract interface DexOpcodes {
	public static final int OP_NOP = 0;
	public static final int OP_MOVE = 1;
	public static final int OP_MOVE_FROM16 = 2;
	public static final int OP_MOVE_16 = 3;
	public static final int OP_MOVE_WIDE = 4;
	public static final int OP_MOVE_WIDE_FROM16 = 5;
	public static final int OP_MOVE_WIDE_16 = 6;
	public static final int OP_MOVE_OBJECT = 7;
	public static final int OP_MOVE_OBJECT_FROM16 = 8;
	public static final int OP_MOVE_OBJECT_16 = 9;
	public static final int OP_MOVE_RESULT = 10;
	public static final int OP_MOVE_RESULT_WIDE = 11;
	public static final int OP_MOVE_RESULT_OBJECT = 12;
	public static final int OP_MOVE_EXCEPTION = 13;
	public static final int OP_RETURN_VOID = 14;
	public static final int OP_RETURN = 15;
	public static final int OP_RETURN_WIDE = 16;
	public static final int OP_RETURN_OBJECT = 17;
	public static final int OP_CONST_4 = 18;
	public static final int OP_CONST_16 = 19;
	public static final int OP_CONST = 20;
	public static final int OP_CONST_HIGH16 = 21;
	public static final int OP_CONST_WIDE_16 = 22;
	public static final int OP_CONST_WIDE_32 = 23;
	public static final int OP_CONST_WIDE = 24;
	public static final int OP_CONST_WIDE_HIGH16 = 25;
	public static final int OP_CONST_STRING = 26;
	public static final int OP_CONST_STRING_JUMBO = 27;
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
	public static final int OP_GOTO_16 = 41;
	public static final int OP_GOTO_32 = 42;
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
	public static final int OP_AGET_WIDE = 69;
	public static final int OP_AGET_OBJECT = 70;
	public static final int OP_AGET_BOOLEAN = 71;
	public static final int OP_AGET_BYTE = 72;
	public static final int OP_AGET_CHAR = 73;
	public static final int OP_AGET_SHORT = 74;
	public static final int OP_APUT = 75;
	public static final int OP_APUT_WIDE = 76;
	public static final int OP_APUT_OBJECT = 77;
	public static final int OP_APUT_BOOLEAN = 78;
	public static final int OP_APUT_BYTE = 79;
	public static final int OP_APUT_CHAR = 80;
	public static final int OP_APUT_SHORT = 81;
	public static final int OP_IGET = 82;
	public static final int OP_IGET_WIDE = 83;
	public static final int OP_IGET_OBJECT = 84;
	public static final int OP_IGET_BOOLEAN = 85;
	public static final int OP_IGET_BYTE = 86;
	public static final int OP_IGET_CHAR = 87;
	public static final int OP_IGET_SHORT = 88;
	public static final int OP_IPUT = 89;
	public static final int OP_IPUT_WIDE = 90;
	public static final int OP_IPUT_OBJECT = 91;
	public static final int OP_IPUT_BOOLEAN = 92;
	public static final int OP_IPUT_BYTE = 93;
	public static final int OP_IPUT_CHAR = 94;
	public static final int OP_IPUT_SHORT = 95;
	public static final int OP_SGET = 96;
	public static final int OP_SGET_WIDE = 97;
	public static final int OP_SGET_OBJECT = 98;
	public static final int OP_SGET_BOOLEAN = 99;
	public static final int OP_SGET_BYTE = 100;
	public static final int OP_SGET_CHAR = 101;
	public static final int OP_SGET_SHORT = 102;
	public static final int OP_SPUT = 103;
	public static final int OP_SPUT_WIDE = 104;
	public static final int OP_SPUT_OBJECT = 105;
	public static final int OP_SPUT_BOOLEAN = 106;
	public static final int OP_SPUT_BYTE = 107;
	public static final int OP_SPUT_CHAR = 108;
	public static final int OP_SPUT_SHORT = 109;
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

	public static final int OP_EXECUTE_INLINE = 238;

	public static final int OP_INVOKE_DIRECT_EMPTY = 240;

	public static final int OP_IGET_QUICK = 242;
	public static final int OP_IGET_WIDE_QUICK = 243;
	public static final int OP_IGET_OBJECT_QUICK = 244;
	public static final int OP_IPUT_QUICK = 245;
	public static final int OP_IPUT_WIDE_QUICK = 246;
	public static final int OP_IPUT_OBJECT_QUICK = 247;
	public static final int OP_INVOKE_VIRTUAL_QUICK = 248;
	public static final int OP_INVOKE_VIRTUAL_QUICK_RANGE = 249;
	public static final int OP_INVOKE_SUPER_QUICK = 250;
	public static final int OP_INVOKE_SUPER_QUICK_RANGE = 251;

}