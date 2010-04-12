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

import org.objectweb.asm.Opcodes;

/**
 * 指令的工具类
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public final class DexOpcodeUtil implements DexOpcodes, Opcodes, DexInternalOpcode {
	/**
	 * 获取指令的长度
	 * 
	 * @param opcode
	 *            指令
	 * @return
	 */
	public static int getSize(int opcode) {
		switch (opcode) {

		case OP_MOVE_RESULT_OBJECT:
		case OP_MOVE_EXCEPTION:
		case OP_MOVE_OBJECT:
		case OP_MOVE_WIDE:
		case OP_MOVE_RESULT:
		case OP_MOVE:
		case OP_MOVE_RESULT_WIDE:
		case OP_GOTO:

		case OP_RETURN_VOID:
		case OP_RETURN:
		case OP_RETURN_OBJECT:
		case OP_RETURN_WIDE:
		case OP_THROW:

		case OP_CONST_4:

		case OP_ARRAY_LENGTH:

		case OP_INT_TO_BYTE:
		case OP_INT_TO_CHAR:
		case OP_INT_TO_DOUBLE:
		case OP_INT_TO_FLOAT:
		case OP_INT_TO_LONG:
		case OP_INT_TO_SHORT:
		case OP_LONG_TO_DOUBLE:
		case OP_LONG_TO_FLOAT:
		case OP_LONG_TO_INT:
		case OP_DOUBLE_TO_FLOAT:
		case OP_DOUBLE_TO_INT:
		case OP_DOUBLE_TO_LONG:
		case OP_FLOAT_TO_INT:
		case OP_FLOAT_TO_DOUBLE:
		case OP_FLOAT_TO_LONG:

		case OP_ADD_INT_2ADDR:
		case OP_SUB_INT_2ADDR:
		case OP_MUL_INT_2ADDR:
		case OP_DIV_INT_2ADDR:
		case OP_REM_INT_2ADDR:
		case OP_AND_INT_2ADDR:
		case OP_OR_INT_2ADDR:
		case OP_XOR_INT_2ADDR:
		case OP_SHL_INT_2ADDR:
		case OP_SHR_INT_2ADDR:
		case OP_USHR_INT_2ADDR:

		case OP_ADD_LONG_2ADDR:
		case OP_SUB_LONG_2ADDR:
		case OP_MUL_LONG_2ADDR:
		case OP_DIV_LONG_2ADDR:
		case OP_REM_LONG_2ADDR:
		case OP_AND_LONG_2ADDR:
		case OP_OR_LONG_2ADDR:
		case OP_XOR_LONG_2ADDR:
		case OP_SHL_LONG_2ADDR:
		case OP_SHR_LONG_2ADDR:
		case OP_USHR_LONG_2ADDR:

		case OP_ADD_FLOAT_2ADDR:
		case OP_SUB_FLOAT_2ADDR:
		case OP_MUL_FLOAT_2ADDR:
		case OP_DIV_FLOAT_2ADDR:
		case OP_REM_FLOAT_2ADDR:

		case OP_SUB_DOUBLE_2ADDR:
		case OP_DIV_DOUBLE_2ADDR:
		case OP_MUL_DOUBLE_2ADDR:
		case OP_ADD_DOUBLE_2ADDR:
		case OP_REM_DOUBLE_2ADDR:

		case OP_NEG_INT:
		case OP_NOT_INT:
		case OP_NOT_LONG:
		case OP_NEG_LONG:
		case OP_NEG_DOUBLE:
		case OP_NEG_FLOAT:

		case OP_MONITOR_ENTER:
		case OP_MONITOR_EXIT:
			//
		{
			return 1;
		}
		case OP_NEW_INSTANCE:
		case OP_NEW_ARRAY:
		case OP_MOVE_OBJECT_FROM16:
		case OP_MOVE_FROM16:
		case OP_MOVE_WIDE_FROM16:
		case OP_CONST_STRING:
		case OP_CONST_CLASS:
		case OP_CONST_WIDE_16:
		case OP_CONST_HIGH16:
		case OP_CONST_WIDE_HIGH16:

		case OP_CONST_16:

		case OP_GOTO_16:
		case OP_CHECK_CAST:
		case OP_INSTANCE_OF:

		case OP_IF_EQZ:
		case OP_IF_NEZ:
		case OP_IF_LTZ:
		case OP_IF_GEZ:
		case OP_IF_GTZ:
		case OP_IF_LEZ:
		case OP_IF_EQ:
		case OP_IF_NE:
		case OP_IF_LT:
		case OP_IF_GE:
		case OP_IF_GT:
		case OP_IF_LE:

		case OP_APUT:
		case OP_APUT_WIDE:
		case OP_APUT_OBJECT:
		case OP_APUT_BOOLEAN:
		case OP_APUT_BYTE:
		case OP_APUT_CHAR:
		case OP_APUT_SHORT:

		case OP_AGET:
		case OP_AGET_WIDE:
		case OP_AGET_OBJECT:
		case OP_AGET_BOOLEAN:
		case OP_AGET_BYTE:
		case OP_AGET_CHAR:
		case OP_AGET_SHORT:

		case OP_IGET:
		case OP_IGET_WIDE:
		case OP_IGET_OBJECT:
		case OP_IGET_BOOLEAN:
		case OP_IGET_BYTE:
		case OP_IGET_SHORT:
		case OP_IGET_CHAR:

		case OP_IPUT:
		case OP_IPUT_WIDE:
		case OP_IPUT_OBJECT:
		case OP_IPUT_BOOLEAN:
		case OP_IPUT_BYTE:
		case OP_IPUT_SHORT:
		case OP_IPUT_CHAR:

		case OP_SPUT:
		case OP_SPUT_WIDE:
		case OP_SPUT_OBJECT:
		case OP_SPUT_BOOLEAN:
		case OP_SPUT_BYTE:
		case OP_SPUT_CHAR:
		case OP_SPUT_SHORT:

		case OP_SGET:
		case OP_SGET_WIDE:
		case OP_SGET_OBJECT:
		case OP_SGET_BOOLEAN:
		case OP_SGET_BYTE:
		case OP_SGET_CHAR:
		case OP_SGET_SHORT:

		case OP_ADD_INT_LIT8:
		case OP_RSUB_INT_LIT8:
		case OP_MUL_INT_LIT8:
		case OP_DIV_INT_LIT8:
		case OP_REM_INT_LIT8:
		case OP_AND_INT_LIT8:
		case OP_OR_INT_LIT8:
		case OP_XOR_INT_LIT8:
		case OP_SHL_INT_LIT8:
		case OP_SHR_INT_LIT8:
		case OP_USHR_INT_LIT8:

		case OP_MUL_INT_LIT16:
		case OP_DIV_INT_LIT16:
		case OP_REM_INT_LIT16:
		case OP_ADD_INT_LIT16:
		case OP_AND_INT_LIT16:
		case OP_OR_INT_LIT16:
		case OP_XOR_INT_LIT16:

		case OP_ADD_INT:
		case OP_SUB_INT:
		case OP_MUL_INT:
		case OP_DIV_INT:
		case OP_REM_INT:
		case OP_AND_INT:
		case OP_OR_INT:
		case OP_XOR_INT:
		case OP_SHR_INT:
		case OP_SHL_INT:
		case OP_USHR_INT:

		case OP_ADD_LONG:
		case OP_SUB_LONG:
		case OP_MUL_LONG:
		case OP_DIV_LONG:
		case OP_REM_LONG:
		case OP_AND_LONG:
		case OP_OR_LONG:
		case OP_XOR_LONG:
		case OP_SHL_LONG:
		case OP_SHR_LONG:
		case OP_USHR_LONG:

		case OP_ADD_FLOAT:
		case OP_SUB_FLOAT:
		case OP_MUL_FLOAT:
		case OP_DIV_FLOAT:
		case OP_REM_FLOAT:
		case OP_ADD_DOUBLE:
		case OP_SUB_DOUBLE:
		case OP_MUL_DOUBLE:
		case OP_DIV_DOUBLE:
		case OP_REM_DOUBLE:

		case OP_CMP_LONG:
		case OP_CMPL_DOUBLE:
		case OP_CMPG_DOUBLE:
		case OP_CMPL_FLOAT:
		case OP_CMPG_FLOAT:
			//
		{
			return 2;
		}
		case OP_INVOKE_VIRTUAL:
		case OP_INVOKE_DIRECT:
		case OP_INVOKE_STATIC:
		case OP_INVOKE_INTERFACE:
		case OP_INVOKE_SUPER:
		case OP_INVOKE_DIRECT_RANGE:
		case OP_INVOKE_INTERFACE_RANGE:
		case OP_INVOKE_STATIC_RANGE:
		case OP_INVOKE_SUPER_RANGE:
		case OP_INVOKE_VIRTUAL_RANGE:
		case OP_CONST:

		case OP_FILLED_NEW_ARRAY:
		case OP_FILLED_NEW_ARRAY_RANGE:
		case OP_CONST_WIDE_32:
			//
		{
			return 3;
		}

			// case OP_CONST_WIDE: {
			// i += 5;
			// in.skip(9);
			// }
			// break;
		case OP_NOP:
			return 0;
		case OP_FILL_ARRAY_DATA:
		case OP_SPARSE_SWITCH:
		case OP_PACKED_SWITCH: {
			return -1;
		}
		case OP_CONST_WIDE:
			return 5;
		default:
			return Integer.MAX_VALUE;
			// throw new
			// RuntimeException(String.format("Not support Opcode :[0x%04x]",
			// opcode));

		}
	}

	/**
	 * 映射dex指令到jvm指令
	 * 
	 * @param dexOpcode
	 *            dex指令
	 * @return
	 */
	public static int mapOpcode(int dexOpcode) {
		switch (dexOpcode) {
		case OP_ADD_INT_LIT8:
			return IADD;
		case OP_MUL_INT_LIT8:
			return IMUL;
		case OP_DIV_INT_LIT8:
			return IDIV;
		case OP_REM_INT_LIT8:
			return IREM;
		case OP_AND_INT_LIT8:
			return IAND;
		case OP_OR_INT_LIT8:
			return IOR;
		case OP_XOR_INT_LIT8:
			return IXOR;
		case OP_SHR_INT_LIT8:
			return ISHR;
		case OP_USHR_INT_LIT8:
			return IUSHR;
		case OP_SHL_INT_LIT8:
			return ISHL;

			// &
		case OP_AND_INT_2ADDR:
			return IAND;
		case OP_AND_LONG_2ADDR:
			return LAND;
			// |
		case OP_OR_INT_2ADDR:
			return IOR;
		case OP_OR_LONG_2ADDR:
			return LOR;
			// ^
		case OP_XOR_INT_2ADDR:
			return IXOR;
		case OP_XOR_LONG_2ADDR:
			return LXOR;

			// +
		case OP_ADD_INT_2ADDR:
			return IADD;
		case OP_ADD_LONG_2ADDR:
			return LADD;
		case OP_ADD_FLOAT_2ADDR:
			return FADD;
		case OP_ADD_DOUBLE_2ADDR:
			return DADD;

			// -
		case OP_SUB_INT_2ADDR:
			return ISUB;
		case OP_SUB_LONG_2ADDR:
			return LSUB;
		case OP_SUB_FLOAT_2ADDR:
			return FSUB;
		case OP_SUB_DOUBLE_2ADDR:
			return DSUB;

			// *
		case OP_MUL_INT_2ADDR:
			return IMUL;
		case OP_MUL_LONG_2ADDR:
			return LMUL;
		case OP_MUL_FLOAT_2ADDR:
			return FMUL;
		case OP_MUL_DOUBLE_2ADDR:
			return DMUL;

			// /
		case OP_DIV_INT_2ADDR:
			return IDIV;
		case OP_DIV_LONG_2ADDR:
			return LDIV;
		case OP_DIV_FLOAT_2ADDR:
			return FDIV;
		case OP_DIV_DOUBLE_2ADDR:
			return DDIV;

		case OP_REM_LONG_2ADDR:
			return LREM;
		case OP_REM_INT_2ADDR:
			return IREM;

			// shr
		case OP_SHR_INT_2ADDR:
			return ISHR;
		case OP_SHR_LONG_2ADDR:
			return LSHR;

			// ushr
		case OP_USHR_INT_2ADDR:
			return IUSHR;
		case OP_USHR_LONG_2ADDR:
			return LUSHR;

			// shl
		case OP_SHL_INT_2ADDR:
			return ISHL;
		case OP_SHL_LONG_2ADDR:
			return LSHL;

		case OP_AND_LONG:
			return LAND;
		case OP_AND_INT:
			return IAND;
		case OP_OR_LONG:
			return LOR;
		case OP_OR_INT:
			return IOR;
		case OP_XOR_LONG:
			return LXOR;
		case OP_XOR_INT:
			return IXOR;
		case OP_SHR_INT:
			return ISHR;
		case OP_SHL_INT:
			return ISHL;
		case OP_USHR_INT:
			return IUSHR;
		case OP_SHR_LONG:
			return LSHR;
		case OP_SHL_LONG:
			return LSHL;
		case OP_USHR_LONG:
			return LUSHR;
		case OP_ADD_INT:
			return IADD;
		case OP_ADD_LONG:
			return LADD;
		case OP_ADD_FLOAT:
			return FADD;
		case OP_ADD_DOUBLE:
			return DADD;
		case OP_SUB_FLOAT:
			return FSUB;
		case OP_SUB_DOUBLE:
			return DSUB;
		case OP_SUB_INT:
			return ISUB;
		case OP_SUB_LONG:
			return LSUB;
		case OP_DIV_INT:
			return IDIV;
		case OP_DIV_LONG:
			return LDIV;
		case OP_DIV_FLOAT:
			return FDIV;
		case OP_DIV_DOUBLE:
			return DDIV;
		case OP_MUL_INT:
			return IMUL;
		case OP_MUL_LONG:
			return LMUL;
		case OP_MUL_FLOAT:
			return FMUL;
		case OP_MUL_DOUBLE:
			return FMUL;
		case OP_CMP_LONG:
			return LCMP;
		case OP_REM_LONG:
			return LREM;
		case OP_REM_INT:
			return IREM;
		case OP_REM_FLOAT:
			return FREM;
		case OP_REM_DOUBLE:
			return DREM;
		case OP_CMPL_DOUBLE:
			return DCMPL;
		case OP_CMPL_FLOAT:
			return FCMPL;
		case OP_CMPG_DOUBLE:
			return DCMPG;
		case OP_CMPG_FLOAT:
			return FCMPG;

		case OP_MUL_INT_LIT16:
			return IMUL;
		case OP_DIV_INT_LIT16:
			return IDIV;
		case OP_REM_INT_LIT16:
			return IREM;
		case OP_ADD_INT_LIT16:
			return IADD;
		case OP_AND_INT_LIT16:
			return IAND;
		case OP_OR_INT_LIT16:
			return IOR;
		case OP_XOR_INT_LIT16:
			return IXOR;

		case OP_NEG_INT:
			return INEG;
		case OP_NEG_DOUBLE:
			return DNEG;
		case OP_NEG_FLOAT:
			return FNEG;
		case OP_NEG_LONG:
			return LNEG;

		case OP_IF_EQ:
			return IF_ICMPEQ;
		case OP_IF_NE:
			return IF_ICMPNE;
		case OP_IF_GE:
			return IF_ICMPGE;
		case OP_IF_LE:
			return IF_ICMPLE;
		case OP_IF_LT:
			return IF_ICMPLT;
		case OP_IF_GT:
			return IF_ICMPGT;

		case OP_INT_TO_BYTE:
			return I2B;
		case OP_INT_TO_SHORT:
			return I2S;
		case OP_INT_TO_CHAR:
			return I2C;
		case OP_INT_TO_FLOAT:
			return I2F;
		case OP_INT_TO_LONG:
			return I2L;
		case OP_INT_TO_DOUBLE:
			return I2D;
		case OP_LONG_TO_DOUBLE:
			return L2D;
		case OP_LONG_TO_FLOAT:
			return L2F;
		case OP_LONG_TO_INT:
			return L2I;
		case OP_DOUBLE_TO_FLOAT:
			return D2F;
		case OP_DOUBLE_TO_INT:
			return D2I;
		case OP_DOUBLE_TO_LONG:
			return D2L;
		case OP_FLOAT_TO_INT:
			return F2I;
		case OP_FLOAT_TO_LONG:
			return F2L;
		case OP_FLOAT_TO_DOUBLE:
			return F2D;
		case OP_ARRAY_LENGTH:
			return ARRAYLENGTH;
		case OP_APUT: // int
			return IASTORE;
		case OP_APUT_BOOLEAN:
		case OP_APUT_BYTE:
			return BASTORE;
		case OP_APUT_CHAR:
			return CASTORE;
		case OP_APUT_OBJECT:
			return AASTORE;
		case OP_APUT_SHORT:
			return SASTORE;
		case OP_APUT_WIDE: // long
			return LASTORE;
		case OP_AGET:
			return IALOAD;
		case OP_AGET_BOOLEAN:
		case OP_AGET_BYTE:
			return BALOAD;
		case OP_AGET_CHAR:
			return CALOAD;
		case OP_AGET_SHORT:
			return SALOAD;
		case OP_AGET_WIDE:
			return LALOAD;
		default:
			throw new RuntimeException("Not support " + DexOpcodeDump.dump(dexOpcode));
		}

	}
}
