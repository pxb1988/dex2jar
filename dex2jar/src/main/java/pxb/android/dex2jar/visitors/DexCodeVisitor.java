/**
 * 
 */
package pxb.android.dex2jar.visitors;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexCodeVisitor {

	/**
	 * 
	 * @param args
	 */
	void visitInitLocal(int... args);

	/**
	 * <pre>
	 * 		case OP_APUT:
	 * 		case OP_APUT_BOOLEAN:
	 * 		case OP_APUT_BYTE:
	 * 		case OP_APUT_CHAR:
	 * 		case OP_APUT_OBJECT:
	 * 		case OP_APUT_SHORT:
	 * 		case OP_APUT_WIDE:
	 * 		case OP_AGET:
	 * 		case OP_AGET_BOOLEAN:
	 * 		case OP_AGET_BYTE:
	 * 		case OP_AGET_CHAR:
	 * 		case OP_AGET_OBJECT:
	 * 		case OP_AGET_SHORT:
	 * 		case OP_AGET_WIDE:
	 * </pre>
	 * 
	 * @param opcode
	 * @param regFromOrTo
	 * @param array
	 * @param index
	 */
	void visitArrayInsn(int opcode, int regFromOrTo, int array, int index);

	/**
	 * <pre>
	 * case OP_NEW_ARRAY:
	 * </pre>
	 * 
	 * @param opcode
	 * @param type
	 * @param saveToReg
	 * @param demReg
	 */
	void visitArrayInsn(int opcode, String type, int saveToReg, int demReg);

	void visitEnd();

	/**
	 * Static Field
	 * 
	 * <pre>
	 * 		case OP_SPUT_OBJECT:
	 * 		case OP_SGET_OBJECT:
	 * 		case OP_SPUT_BOOLEAN:
	 * 		case OP_SPUT_BYTE:
	 * 		case OP_SPUT_CHAR:
	 * 		case OP_SPUT_SHORT:
	 * 		case OP_SPUT_WIDE:
	 * 		case OP_SGET_BOOLEAN:
	 * 		case OP_SGET_BYTE:
	 * 		case OP_SGET_CHAR:
	 * 		case OP_SGET_SHORT:
	 * 		case OP_SGET_WIDE:
	 * 		case OP_SPUT:
	 * 		case OP_SGET:
	 * </pre>
	 * 
	 * @param opcode
	 * @param field
	 * @param reg
	 */
	void visitFieldInsn(int opcode, Field field, int reg);

	/**
	 * <pre>
	 * 		case OP_IGET_OBJECT:
	 * 		case OP_IGET_BOOLEAN:
	 * 		case OP_IGET_BYTE:
	 * 		case OP_IGET_SHORT:
	 * 		case OP_IGET:
	 * 		case OP_IGET_WIDE:
	 * 
	 * 		case OP_IPUT_OBJECT:
	 * 		case OP_IPUT_BOOLEAN:
	 * 		case OP_IPUT_BYTE:
	 * 		case OP_IPUT_SHORT:
	 * 		case OP_IPUT:
	 * 		case OP_IPUT_WIDE:
	 * </pre>
	 * 
	 * @param opcode
	 * @param field
	 * @param regFromOrTo
	 * @param ownerReg
	 */
	void visitFieldInsn(int opcode, Field field, int regFromOrTo, int ownerReg);

	/**
	 * @param opcode
	 * @param reg
	 * @param elemWidth
	 * @param initLength
	 * @param values
	 */
	void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values);

	/**
	 * <pre>
	 * 		case OP_AND_INT_2ADDR:
	 * 		case OP_AND_LONG_2ADDR:
	 * 		case OP_OR_INT_2ADDR:
	 * 		case OP_OR_LONG_2ADDR:
	 * 		case OP_XOR_INT_2ADDR:
	 * 		case OP_XOR_LONG_2ADDR:
	 * 		case OP_MUL_LONG_2ADDR:
	 * 		case OP_MUL_INT_2ADDR:
	 * 		case OP_MUL_FLOAT_2ADDR:
	 * 		case OP_MUL_DOUBLE_2ADDR:
	 * 		case OP_SUB_INT_2ADDR:
	 * 		case OP_SUB_LONG_2ADDR:
	 * 		case OP_SUB_FLOAT_2ADDR:
	 * 		case OP_SUB_DOUBLE_2ADDR:
	 * 		case OP_REM_INT_2ADDR:
	 * 		case OP_REM_LONG_2ADDR:
	 * 		case OP_DIV_INT_2ADDR:
	 * 		case OP_DIV_LONG_2ADDR:
	 * 		case OP_DIV_FLOAT_2ADDR:
	 * 		case OP_DIV_DOUBLE_2ADDR:
	 * 		case OP_ADD_INT_2ADDR:
	 * 		case OP_ADD_LONG_2ADDR:
	 * 		case OP_ADD_FLOAT_2ADDR:
	 * 		case OP_ADD_DOUBLE_2ADDR:
	 * 
	 * 		case OP_NEG_INT:
	 * 		case OP_NEG_DOUBLE:
	 * 		case OP_NEG_FLOAT:
	 * 		case OP_NEG_LONG:
	 * 
	 * 		case OP_MOVE_OBJECT:
	 * 		case OP_MOVE:
	 * 		case OP_MOVE_WIDE:
	 * 		case OP_INT_TO_BYTE:
	 * 		case OP_INT_TO_CHAR:
	 * 		case OP_INT_TO_DOUBLE:
	 * 		case OP_INT_TO_FLOAT:
	 * 		case OP_INT_TO_LONG:
	 * 		case OP_INT_TO_SHORT:
	 * 		case OP_LONG_TO_DOUBLE:
	 * 		case OP_LONG_TO_FLOAT:
	 * 		case OP_LONG_TO_INT:
	 * 		case OP_DOUBLE_TO_FLOAT:
	 * 		case OP_DOUBLE_TO_INT:
	 * 		case OP_DOUBLE_TO_LONG:
	 * 		case OP_FLOAT_TO_INT:
	 * 		case OP_FLOAT_TO_DOUBLE:
	 * 		case OP_FLOAT_TO_LONG:
	 * 
	 * 		case OP_MOVE_OBJECT_FROM16:
	 * 		case OP_MOVE_FROM16:
	 * 		case OP_MOVE_WIDE_FROM16:
	 * 
	 * 		case OP_ARRAY_LENGTH:
	 * </pre>
	 * 
	 * @param opcode
	 * @param saveToReg
	 * @param opReg
	 */
	void visitInInsn(int opcode, int saveToReg, int opReg);

	/**
	 * <pre>
	 * 		case OP_AND_INT:
	 * 		case OP_AND_LONG:
	 * 		case OP_OR_INT:
	 * 		case OP_OR_LONG:
	 * 		case OP_XOR_INT:
	 * 		case OP_XOR_LONG:
	 * 		case OP_CMP_LONG:
	 * 		case OP_MUL_INT:
	 * 		case OP_MUL_LONG:
	 * 		case OP_MUL_FLOAT:
	 * 		case OP_MUL_DOUBLE:
	 * 		case OP_DIV_INT:
	 * 		case OP_DIV_LONG:
	 * 		case OP_DIV_FLOAT:
	 * 		case OP_DIV_DOUBLE:
	 * 		case OP_ADD_INT:
	 * 		case OP_ADD_LONG:
	 * 		case OP_ADD_FLOAT:
	 * 		case OP_ADD_DOUBLE:
	 * 		case OP_SUB_INT:
	 * 		case OP_SUB_DOUBLE:
	 * 		case OP_SUB_FLOAT:
	 * 		case OP_SUB_LONG:
	 * 		case OP_REM_LONG:
	 * 		case OP_REM_INT:
	 * 		case OP_REM_FLOAT:
	 * 		case OP_REM_DOUBLE:
	 * 		case OP_CMPL_DOUBLE:
	 * 		case OP_CMPL_FLOAT:
	 * 		case OP_MUL_INT_LIT16:
	 * 		case OP_DIV_INT_LIT16:
	 * 		case OP_REM_INT_LIT16:
	 * 		case OP_ADD_INT_LIT16:
	 * 		case OP_AND_INT_LIT16:
	 * 		case OP_OR_INT_LIT16:
	 * 		case OP_XOR_INT_LIT16: 
	 * 		case OP_AND_INT_LIT8:
	 * 		case OP_ADD_INT_LIT8:
	 * 		case OP_REM_INT_LIT8:
	 * 		case OP_DIV_INT_LIT8:
	 * 		case OP_MUL_INT_LIT8:
	 * 		case OP_SHR_INT_LIT8:
	 * 		case OP_SHL_INT_LIT8:
	 * 		case OP_USHR_INT_LIT8:
	 * 		case OP_OR_INT_LIT8:
	 * 		case OP_XOR_INT_LIT8:
	 * </pre>
	 * 
	 * @param opcode
	 * @param saveToReg
	 * @param opReg
	 * @param opValueOrReg
	 */
	void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg);

	/**
	 * <pre>
	 * case OP_RETURN_VOID:
	 * </pre>
	 * 
	 * @param opcode
	 */
	void visitInsn(int opcode);

	/**
	 * <pre>
	 * case OP_GOTO:
	 * case OP_GOTO_16:
	 * </pre>
	 * 
	 * @param opcode
	 * @param offset
	 */
	void visitJumpInsn(int opcode, int offset);

	/**
	 * <pre>
	 * 		case OP_IF_EQZ:
	 * 		case OP_IF_NEZ:
	 * 		case OP_IF_LTZ:
	 * 		case OP_IF_GEZ:
	 * 		case OP_IF_GTZ:
	 * 		case OP_IF_LEZ:
	 * </pre>
	 * 
	 * @param opcode
	 * @param offset
	 * @param reg
	 */
	void visitJumpInsn(int opcode, int offset, int reg);

	/**
	 * <pre>
	 * 		case OP_IF_EQ:
	 * 		case OP_IF_NE:
	 * 		case OP_IF_LT:
	 * 		case OP_IF_GE:
	 * 		case OP_IF_GT:
	 * 		case OP_IF_LE:
	 * </pre>
	 * 
	 * @param opcode
	 * @param offset
	 * @param reg1
	 * @param reg2
	 */
	void visitJumpInsn(int opcode, int offset, int reg1, int reg2);

	/**
	 * 
	 * @param index
	 */
	public void visitLabel(int index);

	/**
	 * 
	 * @param opcode
	 * @param value
	 * @param reg
	 */
	void visitLdcInsn(int opcode, Object value, int reg);

	/**
	 * @param line
	 * @param label
	 */
	void visitLineNumber(int line, int label);

	/**
	 * @param name
	 * @param type
	 * @param signature
	 * @param start
	 * @param end
	 * @param reg
	 */
	void visitLocalVariable(String name, String type, String signature, int start, int end, int reg);

	/**
	 * 
	 * @param opcode
	 * @param reg
	 * @param defaultOffset
	 * @param cases
	 * @param offsets
	 */
	void visitLookupSwitchInsn(int opcode, int reg, int defaultOffset, int[] cases, int[] offsets);

	/**
	 * <pre>
	 * 		case OP_INVOKE_DIRECT_RANGE:
	 * 		case OP_INVOKE_STATIC:
	 * 		case OP_INVOKE_DIRECT_RANGE:
	 * 		case OP_INVOKE_DIRECT:
	 * 		case OP_INVOKE_INTERFACE_RANGE:
	 * 		case OP_INVOKE_SUPER_RANGE:
	 * 		case OP_INVOKE_VIRTUAL_RANGE: 
	 * 		case OP_INVOKE_VIRTUAL: 	 
	 * 		case OP_INVOKE_INTERFACE:
	 * 		case OP_INVOKE_SUPER:
	 * </pre>
	 * 
	 * @param opcode
	 * @param method
	 * @param regs
	 */
	void visitMethodInsn(int opcode, Method method, int[] args);

	/**
	 * 
	 * @param opcode
	 * @param reg
	 * @param first_case
	 * @param last_case
	 * @param _defaultLabel
	 * @param labels
	 */
	void visitTableSwitchInsn(int opcode, int reg, int first_case, int last_case, int _defaultLabel, int[] labels);

	/**
	 * 
	 * @param start
	 * @param offset
	 * @param handler
	 * @param type
	 */
	void visitTryCatch(int start, int offset, int handler, String type);

	/**
	 * <pre>
	 * 		case OP_NEW_INSTANCE:
	 * 		case OP_CONST_CLASS:
	 * 		case OP_CHECK_CAST:
	 * </pre>
	 * 
	 * @param opcode
	 * @param type
	 * @param toReg
	 */
	void visitTypeInsn(int opcode, String type, int toReg);

	/**
	 * <pre>
	 * case OP_INSTANCE_OF:
	 * </pre>
	 * 
	 * @param opcode
	 * @param type
	 * @param toReg
	 * @param fromReg
	 */
	void visitTypeInsn(int opcode, String type, int toReg, int fromReg);

	/**
	 * <pre>
	 * 		case OP_MOVE_RESULT_OBJECT:
	 * 		case OP_MOVE_RESULT:
	 * 		case OP_MOVE_RESULT_WIDE:
	 * 		case OP_MOVE_EXCEPTION:
	 * 		case OP_THROW:
	 * 		case OP_RETURN_OBJECT:
	 * 		case OP_RETURN:
	 * 		case OP_RETURN_WIDE:
	 * 		case OP_MONITOR_ENTER:
	 * 		case OP_MONITOR_EXIT:
	 * </pre>
	 * 
	 * @param opcode
	 * @param arg1
	 */
	void visitVarInsn(int opcode, int reg);

	void visitFilledNewArrayIns(int opcode, String type, int[] regs);
}
