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
 * 输出指令信息
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id: DexOpcodeDump.java 90 2010-03-09 05:31:33Z pxb1988 $
 */
public final class DexOpcodeDump {
	/**
	 * 输出
	 * 
	 * @param 指令
	 */
	public static final String dump(int opcode) {
		return map[opcode & 0xff];
	}

	private static String[] map = new String[0x100];
	static {
		map[0] = "NOP";
		map[1] = "MOVE";
		map[2] = "MOVE_FROM16";
		map[3] = "MOVE_16";
		map[4] = "MOVE_WIDE";
		map[5] = "MOVE_WIDE_FROM16";
		map[6] = "MOVE_WIDE_16";
		map[7] = "MOVE_OBJECT";
		map[8] = "MOVE_OBJECT_FROM16";
		map[9] = "MOVE_OBJECT_16";
		map[10] = "MOVE_RESULT";
		map[11] = "MOVE_RESULT_WIDE";
		map[12] = "MOVE_RESULT_OBJECT";
		map[13] = "MOVE_EXCEPTION";
		map[14] = "RETURN_VOID";
		map[15] = "RETURN";
		map[16] = "RETURN_WIDE";
		map[17] = "RETURN_OBJECT";
		map[18] = "CONST_4";
		map[19] = "CONST_16";
		map[20] = "CONST";
		map[21] = "CONST_HIGH16";
		map[22] = "CONST_WIDE_16";
		map[23] = "CONST_WIDE_32";
		map[24] = "CONST_WIDE";
		map[25] = "CONST_WIDE_HIGH16";
		map[26] = "CONST_STRING";
		map[27] = "CONST_STRING_JUMBO";
		map[28] = "CONST_CLASS";
		map[29] = "MONITOR_ENTER";
		map[30] = "MONITOR_EXIT";
		map[31] = "CHECK_CAST";
		map[32] = "INSTANCE_OF";
		map[33] = "ARRAY_LENGTH";
		map[34] = "NEW_INSTANCE";
		map[35] = "NEW_ARRAY";
		map[36] = "FILLED_NEW_ARRAY";
		map[37] = "FILLED_NEW_ARRAY_RANGE";
		map[38] = "FILL_ARRAY_DATA";
		map[39] = "THROW";
		map[40] = "GOTO";
		map[41] = "GOTO_16";
		map[42] = "GOTO_32";
		map[43] = "PACKED_SWITCH";
		map[44] = "SPARSE_SWITCH";
		map[45] = "CMPL_FLOAT";
		map[46] = "CMPG_FLOAT";
		map[47] = "CMPL_DOUBLE";
		map[48] = "CMPG_DOUBLE";
		map[49] = "CMP_LONG";
		map[50] = "IF_EQ";
		map[51] = "IF_NE";
		map[52] = "IF_LT";
		map[53] = "IF_GE";
		map[54] = "IF_GT";
		map[55] = "IF_LE";
		map[56] = "IF_EQZ";
		map[57] = "IF_NEZ";
		map[58] = "IF_LTZ";
		map[59] = "IF_GEZ";
		map[60] = "IF_GTZ";
		map[61] = "IF_LEZ";
		map[62] = "UNUSED_3e";
		map[63] = "UNUSED_3f";
		map[64] = "UNUSED_40";
		map[65] = "UNUSED_41";
		map[66] = "UNUSED_42";
		map[67] = "UNUSED_43";
		map[68] = "AGET";
		map[69] = "AGET_WIDE";
		map[70] = "AGET_OBJECT";
		map[71] = "AGET_BOOLEAN";
		map[72] = "AGET_BYTE";
		map[73] = "AGET_CHAR";
		map[74] = "AGET_SHORT";
		map[75] = "APUT";
		map[76] = "APUT_WIDE";
		map[77] = "APUT_OBJECT";
		map[78] = "APUT_BOOLEAN";
		map[79] = "APUT_BYTE";
		map[80] = "APUT_CHAR";
		map[81] = "APUT_SHORT";
		map[82] = "IGET";
		map[83] = "IGET_WIDE";
		map[84] = "IGET_OBJECT";
		map[85] = "IGET_BOOLEAN";
		map[86] = "IGET_BYTE";
		map[87] = "IGET_CHAR";
		map[88] = "IGET_SHORT";
		map[89] = "IPUT";
		map[90] = "IPUT_WIDE";
		map[91] = "IPUT_OBJECT";
		map[92] = "IPUT_BOOLEAN";
		map[93] = "IPUT_BYTE";
		map[94] = "IPUT_CHAR";
		map[95] = "IPUT_SHORT";
		map[96] = "SGET";
		map[97] = "SGET_WIDE";
		map[98] = "SGET_OBJECT";
		map[99] = "SGET_BOOLEAN";
		map[100] = "SGET_BYTE";
		map[101] = "SGET_CHAR";
		map[102] = "SGET_SHORT";
		map[103] = "SPUT";
		map[104] = "SPUT_WIDE";
		map[105] = "SPUT_OBJECT";
		map[106] = "SPUT_BOOLEAN";
		map[107] = "SPUT_BYTE";
		map[108] = "SPUT_CHAR";
		map[109] = "SPUT_SHORT";
		map[110] = "INVOKE_VIRTUAL";
		map[111] = "INVOKE_SUPER";
		map[112] = "INVOKE_DIRECT";
		map[113] = "INVOKE_STATIC";
		map[114] = "INVOKE_INTERFACE";
		map[115] = "UNUSED_73";
		map[116] = "INVOKE_VIRTUAL_RANGE";
		map[117] = "INVOKE_SUPER_RANGE";
		map[118] = "INVOKE_DIRECT_RANGE";
		map[119] = "INVOKE_STATIC_RANGE";
		map[120] = "INVOKE_INTERFACE_RANGE";
		map[121] = "UNUSED_79";
		map[122] = "UNUSED_7A";
		map[123] = "NEG_INT";
		map[124] = "NOT_INT";
		map[125] = "NEG_LONG";
		map[126] = "NOT_LONG";
		map[127] = "NEG_FLOAT";
		map[128] = "NEG_DOUBLE";
		map[129] = "INT_TO_LONG";
		map[130] = "INT_TO_FLOAT";
		map[131] = "INT_TO_DOUBLE";
		map[132] = "LONG_TO_INT";
		map[133] = "LONG_TO_FLOAT";
		map[134] = "LONG_TO_DOUBLE";
		map[135] = "FLOAT_TO_INT";
		map[136] = "FLOAT_TO_LONG";
		map[137] = "FLOAT_TO_DOUBLE";
		map[138] = "DOUBLE_TO_INT";
		map[139] = "DOUBLE_TO_LONG";
		map[140] = "DOUBLE_TO_FLOAT";
		map[141] = "INT_TO_BYTE";
		map[142] = "INT_TO_CHAR";
		map[143] = "INT_TO_SHORT";
		map[144] = "ADD_INT";
		map[145] = "SUB_INT";
		map[146] = "MUL_INT";
		map[147] = "DIV_INT";
		map[148] = "REM_INT";
		map[149] = "AND_INT";
		map[150] = "OR_INT";
		map[151] = "XOR_INT";
		map[152] = "SHL_INT";
		map[153] = "SHR_INT";
		map[154] = "USHR_INT";
		map[155] = "ADD_LONG";
		map[156] = "SUB_LONG";
		map[157] = "MUL_LONG";
		map[158] = "DIV_LONG";
		map[159] = "REM_LONG";
		map[160] = "AND_LONG";
		map[161] = "OR_LONG";
		map[162] = "XOR_LONG";
		map[163] = "SHL_LONG";
		map[164] = "SHR_LONG";
		map[165] = "USHR_LONG";
		map[166] = "ADD_FLOAT";
		map[167] = "SUB_FLOAT";
		map[168] = "MUL_FLOAT";
		map[169] = "DIV_FLOAT";
		map[170] = "REM_FLOAT";
		map[171] = "ADD_DOUBLE";
		map[172] = "SUB_DOUBLE";
		map[173] = "MUL_DOUBLE";
		map[174] = "DIV_DOUBLE";
		map[175] = "REM_DOUBLE";
		map[176] = "ADD_INT_2ADDR";
		map[177] = "SUB_INT_2ADDR";
		map[178] = "MUL_INT_2ADDR";
		map[179] = "DIV_INT_2ADDR";
		map[180] = "REM_INT_2ADDR";
		map[181] = "AND_INT_2ADDR";
		map[182] = "OR_INT_2ADDR";
		map[183] = "XOR_INT_2ADDR";
		map[184] = "SHL_INT_2ADDR";
		map[185] = "SHR_INT_2ADDR";
		map[186] = "USHR_INT_2ADDR";
		map[187] = "ADD_LONG_2ADDR";
		map[188] = "SUB_LONG_2ADDR";
		map[189] = "MUL_LONG_2ADDR";
		map[190] = "DIV_LONG_2ADDR";
		map[191] = "REM_LONG_2ADDR";
		map[192] = "AND_LONG_2ADDR";
		map[193] = "OR_LONG_2ADDR";
		map[194] = "XOR_LONG_2ADDR";
		map[195] = "SHL_LONG_2ADDR";
		map[196] = "SHR_LONG_2ADDR";
		map[197] = "USHR_LONG_2ADDR";
		map[198] = "ADD_FLOAT_2ADDR";
		map[199] = "SUB_FLOAT_2ADDR";
		map[200] = "MUL_FLOAT_2ADDR";
		map[201] = "DIV_FLOAT_2ADDR";
		map[202] = "REM_FLOAT_2ADDR";
		map[203] = "ADD_DOUBLE_2ADDR";
		map[204] = "SUB_DOUBLE_2ADDR";
		map[205] = "MUL_DOUBLE_2ADDR";
		map[206] = "DIV_DOUBLE_2ADDR";
		map[207] = "REM_DOUBLE_2ADDR";
		map[208] = "ADD_INT_LIT16";
		map[209] = "RSUB_INT";
		map[210] = "MUL_INT_LIT16";
		map[211] = "DIV_INT_LIT16";
		map[212] = "REM_INT_LIT16";
		map[213] = "AND_INT_LIT16";
		map[214] = "OR_INT_LIT16";
		map[215] = "XOR_INT_LIT16";
		map[216] = "ADD_INT_LIT8";
		map[217] = "RSUB_INT_LIT8";
		map[218] = "MUL_INT_LIT8";
		map[219] = "DIV_INT_LIT8";
		map[220] = "REM_INT_LIT8";
		map[221] = "AND_INT_LIT8";
		map[222] = "OR_INT_LIT8";
		map[223] = "XOR_INT_LIT8";
		map[224] = "SHL_INT_LIT8";
		map[225] = "SHR_INT_LIT8";
		map[226] = "USHR_INT_LIT8";
		map[227] = "UNUSED_E3";
		map[228] = "UNUSED_E4";
		map[229] = "UNUSED_E5";
		map[230] = "UNUSED_E6";
		map[231] = "UNUSED_E7";
		map[232] = "UNUSED_E8";
		map[233] = "UNUSED_E9";
		map[234] = "UNUSED_EA";
		map[235] = "UNUSED_EB";
		map[236] = "UNUSED_EC";
		map[237] = "UNUSED_ED";
		map[238] = "EXECUTE_INLINE";
		map[239] = "UNUSED_EF";
		map[240] = "INVOKE_DIRECT_EMPTY";
		map[241] = "UNUSED_F1";
		map[242] = "IGET_QUICK";
		map[243] = "IGET_WIDE_QUICK";
		map[244] = "IGET_OBJECT_QUICK";
		map[245] = "IPUT_QUICK";
		map[246] = "IPUT_WIDE_QUICK";
		map[247] = "IPUT_OBJECT_QUICK";
		map[248] = "INVOKE_VIRTUAL_QUICK";
		map[249] = "INVOKE_VIRTUAL_QUICK_RANGE";
		map[250] = "INVOKE_SUPER_QUICK";
		map[251] = "INVOKE_SUPER_QUICK_RANGE";
		map[252] = "UNUSED_FC";
		map[253] = "UNUSED_FD";
		map[254] = "UNUSED_FE";
		map[255] = "UNUSED_FF";
	}
}
