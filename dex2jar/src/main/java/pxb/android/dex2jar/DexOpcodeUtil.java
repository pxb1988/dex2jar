/**
 * 
 */
package pxb.android.dex2jar;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public final class DexOpcodeUtil implements DexOpcodes {
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

		case OP_OR_INT_2ADDR:
		case OP_OR_LONG_2ADDR:
		case OP_AND_INT_2ADDR:
		case OP_AND_LONG_2ADDR:

		case OP_MUL_DOUBLE_2ADDR:
		case OP_MUL_LONG_2ADDR:
		case OP_MUL_INT_2ADDR:
		case OP_MUL_FLOAT_2ADDR:

		case OP_MONITOR_ENTER:
		case OP_MONITOR_EXIT:

		case OP_DIV_LONG_2ADDR:
		case OP_DIV_INT_2ADDR:
		case OP_DIV_FLOAT_2ADDR:
		case OP_DIV_DOUBLE_2ADDR:

		case OP_SUB_INT_2ADDR:
		case OP_SUB_FLOAT_2ADDR:
		case OP_SUB_DOUBLE_2ADDR:
		case OP_SUB_LONG_2ADDR:

		case OP_ADD_INT_2ADDR:
		case OP_ADD_FLOAT_2ADDR:
		case OP_ADD_LONG_2ADDR:
		case OP_ADD_DOUBLE_2ADDR:

		case OP_REM_INT_2ADDR:
		case OP_REM_LONG_2ADDR:

		case OP_XOR_INT_2ADDR:
		case OP_XOR_LONG_2ADDR:

		case OP_SHR_INT_2ADDR:
		case OP_SHR_LONG_2ADDR:
		case OP_SHL_INT_2ADDR:
		case OP_SHL_LONG_2ADDR:

		case OP_NEG_INT:
		case OP_NEG_LONG:
		case OP_NEG_FLOAT:
		case OP_NEG_DOUBLE:
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

		case OP_CMPL_FLOAT:
		case OP_CMPG_FLOAT:
		case OP_CMPL_DOUBLE:
		case OP_CMPG_DOUBLE:
		case OP_CMP_LONG:

		case OP_AGET:
		case OP_AGET_WIDE:
		case OP_AGET_OBJECT:
		case OP_AGET_BOOLEAN:
		case OP_AGET_BYTE:
		case OP_AGET_CHAR:
		case OP_AGET_SHORT:
		case OP_APUT:
		case OP_APUT_WIDE:
		case OP_APUT_OBJECT:
		case OP_APUT_BOOLEAN:
		case OP_APUT_BYTE:
		case OP_APUT_CHAR:
		case OP_APUT_SHORT:
		case OP_IGET:
		case OP_IGET_WIDE:
		case OP_IGET_OBJECT:
		case OP_IGET_BOOLEAN:
		case OP_IGET_BYTE:
		case OP_IGET_CHAR:
		case OP_IGET_SHORT:
		case OP_IPUT:
		case OP_IPUT_WIDE:
		case OP_IPUT_OBJECT:
		case OP_IPUT_BOOLEAN:
		case OP_IPUT_BYTE:
		case OP_IPUT_CHAR:
		case OP_IPUT_SHORT:
		case OP_SGET:
		case OP_SGET_WIDE:
		case OP_SGET_OBJECT:
		case OP_SGET_BOOLEAN:
		case OP_SGET_BYTE:
		case OP_SGET_CHAR:
		case OP_SGET_SHORT:
		case OP_SPUT:
		case OP_SPUT_WIDE:
		case OP_SPUT_OBJECT:
		case OP_SPUT_BOOLEAN:
		case OP_SPUT_BYTE:
		case OP_SPUT_CHAR:
		case OP_SPUT_SHORT:

		case OP_DIV_DOUBLE:
		case OP_DIV_INT:
		case OP_DIV_FLOAT:
		case OP_DIV_LONG:
		case OP_DIV_INT_LIT16:

		case OP_SUB_INT:
		case OP_SUB_FLOAT:
		case OP_SUB_DOUBLE:
		case OP_SUB_LONG:

		case OP_MUL_INT:
		case OP_MUL_LONG:
		case OP_MUL_FLOAT:
		case OP_MUL_DOUBLE:

		case OP_REM_LONG:
		case OP_REM_INT:

		case OP_MUL_INT_LIT16:
		case OP_REM_INT_LIT8:
		case OP_REM_INT_LIT16:
		case OP_DIV_INT_LIT8:
		case OP_MUL_INT_LIT8:

		case OP_OR_INT:
		case OP_XOR_INT:
		case OP_AND_INT:
		case OP_OR_LONG:
		case OP_XOR_LONG:
		case OP_AND_LONG:
		case OP_ADD_INT:
		case OP_ADD_LONG:
		case OP_ADD_FLOAT:
		case OP_ADD_DOUBLE:
		case OP_ADD_INT_LIT8:
		case OP_ADD_INT_LIT16:

		case OP_AND_INT_LIT16:
		case OP_OR_INT_LIT16:
		case OP_OR_INT_LIT8:
		case OP_AND_INT_LIT8:
		case OP_SHR_INT_LIT8:
		case OP_SHL_INT_LIT8:
		case OP_USHR_INT_LIT8:

		case OP_SHR_INT:
		case OP_SHR_LONG:
		case OP_SHL_INT:
		case OP_SHL_LONG:
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
		case OP_FILL_ARRAY_DATA:
		case OP_FILLED_NEW_ARRAY:
		case OP_CONST_WIDE_32:
			//
		{
			return 3;
		}
			// case OP_NOP: {
			// // TODO
			// // int b =
			// // in.readByte();
			// // for (int j = 0; j < 10; j++)
			// // in.readShortx();
			// // i = insns_size;
			// }
			// break;
			// case OP_CONST_WIDE: {
			// i += 5;
			// in.skip(9);
			// }
			// break;
		case OP_SPARSE_SWITCH:
		case OP_PACKED_SWITCH: {
			return -1;
		}
		default:
			return Integer.MAX_VALUE;
			// throw new
			// RuntimeException(String.format("Not support Opcode :[0x%04x]",
			// opcode));

		}
	}
}
