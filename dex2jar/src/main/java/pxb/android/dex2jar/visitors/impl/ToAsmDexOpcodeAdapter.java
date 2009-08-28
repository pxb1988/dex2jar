/**
 * 
 */
package pxb.android.dex2jar.visitors.impl;

import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexOpcodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexOpcodeAdapter implements DexOpcodeVisitor, DexOpcodes {
	private DexCodeVisitor dcv;
	private Dex dex;

	/**
	 * @param dex
	 * @param dcv2
	 */
	public ToAsmDexOpcodeAdapter(Dex dex, DexCodeVisitor dcv2) {
		this.dex = dex;
		this.dcv = dcv2;
	}

	// private static final Logger log =
	// LoggerFactory.getLogger(ToAsmDexOpcodeAdapter.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int)
	 */
	public void visit(int opcode, int arg1) {

		switch (opcode) {
		case OP_RETURN_VOID: {
			dcv.visitInsn(opcode);
		}
			break;
		case OP_MOVE_RESULT_OBJECT:// move-result-object
		case OP_MOVE_RESULT:
		case OP_MOVE_RESULT_WIDE:
		case OP_MOVE_EXCEPTION:
		case OP_THROW:// throw
		case OP_RETURN_OBJECT:// return-object
		case OP_RETURN:
		case OP_RETURN_WIDE:
			//
		{
			dcv.visitVarInsn(opcode, arg1);
		}
			break;
		case OP_CONST_4: {
			int b = arg1;
			int reg = b & 0xf;
			int value = (b >> 4) & 0xf;
			if (0 != (value & 0x8)) {
				value = -((Integer.reverse(value) & 0x7) + 1);
			}
			dcv.visitLdcInsn(opcode, value, reg);
		}
			break;

		case OP_MOVE_OBJECT:
		case OP_MOVE:
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
			//
		{
			int b = arg1;
			int to = b & 0xf;
			int from = (b >> 4) & 0xf;
			dcv.visitMoveInsn(opcode, from, to);
		}
			break;
		case OP_GOTO: {
			dcv.visitJumpInsn(opcode, (byte) arg1, -1);
		}
			break;
		case OP_AND_INT_2ADDR:
		case OP_AND_LONG_2ADDR:
		case OP_ADD_INT_2ADDR:
		case OP_ADD_LONG_2ADDR:
		case OP_MUL_LONG_2ADDR:
		case OP_MUL_INT_2ADDR:
			//
		{

			int a = arg1 & 0xf;
			int b = (arg1 >> 4) & 0xf;
			dcv.visitIntInsn(opcode, a, b, -1);
		}
			break;
		case OP_ARRAY_LENGTH: {
			int a = arg1 & 0xf;
			int b = (arg1 >> 4) & 0xf;
			dcv.visitMoveInsn(opcode, b, a);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int, int)
	 */
	public void visit(int opcode, int arg1, int arg2) {
		switch (opcode) {
		case DexOpcodes.OP_NEW_INSTANCE:
		case DexOpcodes.OP_CONST_CLASS:
		case DexOpcodes.OP_CHECK_CAST: {
			String type = dex.getType(arg2);
			dcv.visitTypeInsn(opcode, type, arg1);
		}
			break;
		case OP_CONST_STRING: {
			String string = dex.getString(arg2);
			dcv.visitLdcInsn(opcode, string, arg1);
		}
			break;
		case OP_CONST_16:
		case OP_CONST_WIDE_16:
			//
		{
			dcv.visitLdcInsn(opcode, arg2, arg1);
		}
			break;
		case OP_SPUT_OBJECT:// sput-object
		case OP_SGET_OBJECT:// sget-object
		{
			Field field = dex.getField(arg2);
			dcv.visitFieldInsn(opcode, field, arg1);
		}
			break;
		case OP_IF_EQZ:
		case OP_IF_NEZ:
		case OP_IF_LTZ:
		case OP_IF_GEZ:
		case OP_IF_GTZ:
		case OP_IF_LEZ: //
		{
			dcv.visitJumpInsn(opcode, arg2, arg1);
		}
			break;
		case OP_IF_EQ:
		case OP_IF_NE:
		case OP_IF_LT:
		case OP_IF_GE:
		case OP_IF_GT:
		case OP_IF_LE: {
			int reg1 = arg1 & 0xf;
			int reg2 = (arg1 >> 4) & 0xf;
			dcv.visitJumpInsn(opcode, arg2, reg1, reg2);
		}
			break;
		case OP_IGET_OBJECT:
		case OP_IGET:
		case OP_IGET_BOOLEAN:
		case OP_IGET_BYTE:
		case OP_IGET_SHORT:
		case OP_IPUT_OBJECT:
		case OP_IPUT_BOOLEAN:
		case OP_IPUT_BYTE:
		case OP_IPUT_SHORT:
		case OP_IPUT:

			//
		{
			int value_reg = arg1 & 0xf;
			int owner_reg = (arg1 >> 4) & 0xf;
			Field field = dex.getField(arg2);
			dcv.visitFieldInsn(opcode, field, owner_reg, value_reg);
		}
			break;

		case OP_ADD_INT_LIT8: {
			int reg2 = (arg1 >> 4) & 0xf;
			int value = (byte) (arg1 & 0xf);
			dcv.visitIntInsn(opcode, arg1, reg2, value);
		}
			break;
		case OP_APUT:
		case OP_APUT_BOOLEAN:
		case OP_APUT_BYTE:
		case OP_APUT_CHAR:
		case OP_APUT_OBJECT:
		case OP_APUT_SHORT:
		case OP_APUT_WIDE:
		case OP_AGET:
		case OP_AGET_BOOLEAN:
		case OP_AGET_BYTE:
		case OP_AGET_CHAR:
		case OP_AGET_OBJECT:
		case OP_AGET_SHORT:
		case OP_AGET_WIDE:
			//
		{
			int value = arg1;
			int index = (arg2 >> 8) & 0xff;
			int array = arg2 & 0xff;
			dcv.visitArrayInsn(opcode, array, index, value);
		}
			break;
		case OP_NEW_ARRAY: {
			int a = arg1 & 0xf;
			int dem = (arg1 >> 4) & 0xf;
			String type = dex.getType(arg2);
			dcv.visitArrayInsn(opcode, type, a, dem);
		}
			break;
		case OP_CMP_LONG:
		case OP_DIV_LONG: {
			int a = (arg2 >> 8) & 0xff;
			int b = arg2 & 0xff;
			dcv.visitIntInsn(opcode, arg1, a, b);
		}
			break;
		case OP_GOTO_16: {
			dcv.visitJumpInsn(opcode, arg2, -1);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int, int,
	 * int)
	 */
	public void visit(int opcode, int arg1, int arg2, int arg3) {
		switch (opcode) {
		case OP_INVOKE_VIRTUAL:// invoke-virtual
		case OP_INVOKE_DIRECT:// invoke-direct
		case OP_INVOKE_STATIC:
		case OP_INVOKE_INTERFACE:// invoke-interface
		case OP_INVOKE_SUPER: {
			int size = (arg1 >> 4) & 0xf;
			int method_idx = arg2;
			int params = arg3;
			dcv.visitMethodInsn(opcode, dex.getMethod(method_idx), buildMethodRegister(size, params));
		}
			break;
		case OP_CONST: {
			dcv.visitLdcInsn(opcode, (arg2 << 16) | arg1, arg1);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	private static int[] buildMethodRegister(int size, int value) {
		int[] a = new int[size];
		for (int i = 0; i < size; i++) {
			a[i] = value & 0xf;
			value >>= 4;
		}
		return a;
	}
}
