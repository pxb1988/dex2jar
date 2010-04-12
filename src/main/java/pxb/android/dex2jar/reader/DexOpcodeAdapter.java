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
package pxb.android.dex2jar.reader;

import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.DexInternalOpcode;
import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class DexOpcodeAdapter implements DexOpcodes, DexInternalOpcode {
	private DexCodeVisitor dcv;
	private Dex dex;
	Map<Integer, Label> labels;

	/**
	 * @param dex
	 * @param dcv2
	 * @param labels
	 */
	public DexOpcodeAdapter(Dex dex, DexCodeVisitor dcv2, Map<Integer, Label> labels) {
		this.dex = dex;
		this.dcv = dcv2;
		this.labels = labels;
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

		case OP_MOVE_OBJECT:
		case OP_MOVE:
		case OP_MOVE_WIDE:

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

		case OP_ARRAY_LENGTH:
			//
		{
			int to = arg1 & 0xf;
			int from = (arg1 >> 4) & 0xf;
			dcv.visitInInsn(opcode, to, from);
		}
			break;
		case OP_GOTO: {
			dcv.visitJumpInsn(opcode, this.labels.get(offset + ((byte) arg1)));
		}
			break;
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
		case OP_MONITOR_ENTER:
		case OP_MONITOR_EXIT:
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
		case OP_NEW_INSTANCE:
		case OP_CHECK_CAST: {
			String type = dex.getType(arg2);
			dcv.visitTypeInsn(opcode, type, arg1);
		}
			break;
		case OP_INSTANCE_OF: {
			String type = dex.getType(arg2);
			dcv.visitTypeInsn(opcode, type, arg1 & 0xf, (arg1 >> 4) & 0xf);
		}
			break;
		case OP_CONST_CLASS: {
			String type = dex.getType(arg2);
			dcv.visitLdcInsn(opcode, Type.getType(type), arg1);
		}
			break;
		case OP_CONST_STRING: {
			String string = dex.getString(arg2);
			dcv.visitLdcInsn(opcode, string, arg1);
		}
			break;
		case OP_CONST_HIGH16: {
			// issue 13
			int intV = arg2 << 16;
			// float floatV = Float.intBitsToFloat(intV);
			dcv.visitLdcInsn(opcode, intV, arg1);
		}
			break;
		case OP_CONST_16: {
			// issue 13
			int intV = arg2 & 0x0000FFFF;
			// float floatV = Float.intBitsToFloat(intV);
			dcv.visitLdcInsn(opcode, intV, arg1);
		}
			break;
		case OP_CONST_WIDE_16: {
			// issue 13
			long longV = arg2 & 0x000000000000FFFFL;
			// double doubleV = Double.longBitsToDouble(longV);
			dcv.visitLdcInsn(opcode, longV, arg1);
		}
			break;
		case OP_CONST_WIDE_HIGH16: {
			// issue 13
			long longV = ((long) arg2) << 48;
			// double doubleV = Double.longBitsToDouble(longV);
			dcv.visitLdcInsn(opcode, longV, arg1);
		}
			break;

		case OP_IF_EQZ:
		case OP_IF_NEZ:
		case OP_IF_LTZ:
		case OP_IF_GEZ:
		case OP_IF_GTZ:
		case OP_IF_LEZ: //
		{
			dcv.visitJumpInsn(opcode, this.labels.get(offset + arg2), arg1);
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
			dcv.visitJumpInsn(opcode, this.labels.get(offset + arg2), reg1, reg2);
		}
			break;
		case OP_IGET_BOOLEAN:
		case OP_IGET_BYTE:
		case OP_IGET_CHAR:
		case OP_IGET_SHORT:
		case OP_IPUT_BOOLEAN:
		case OP_IPUT_BYTE:
		case OP_IPUT_CHAR:
		case OP_IPUT_SHORT:
		case OP_IGET:
		case OP_IGET_WIDE:
		case OP_IGET_OBJECT:
		case OP_IPUT:
		case OP_IPUT_WIDE:
		case OP_IPUT_OBJECT: {
			Field field = dex.getField(arg2);
			dcv.visitFieldInsn(opcode, field, arg1 & 0xf, (arg1 >> 4) & 0xf);
		}
			break;
		case OP_SPUT:
		case OP_SPUT_WIDE:
		case OP_SPUT_OBJECT:
		case OP_SGET:
		case OP_SGET_WIDE:
		case OP_SGET_OBJECT:
		case OP_SPUT_BOOLEAN:
		case OP_SPUT_BYTE:
		case OP_SPUT_CHAR:
		case OP_SPUT_SHORT:
		case OP_SGET_BOOLEAN:
		case OP_SGET_BYTE:
		case OP_SGET_CHAR:
		case OP_SGET_SHORT: {
			Field field = dex.getField(arg2);
			dcv.visitFieldInsn(opcode, field, arg1, -1);
		}
			break;
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
		case OP_AGET_SHORT: {
			int value = arg1;
			int index = (arg2 >> 8) & 0xff;
			int array = arg2 & 0xff;
			dcv.visitArrayInsn(opcode, value, array, index);
		}
			break;
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
		case OP_USHR_INT_LIT8: {
			dcv.visitInInsn(opcode, arg1, arg2 & 0xff, (byte) ((arg2 >> 8) & 0xff));
		}
			break;
		case OP_MUL_INT_LIT16:
		case OP_DIV_INT_LIT16:
		case OP_REM_INT_LIT16:
		case OP_ADD_INT_LIT16:
		case OP_AND_INT_LIT16:
		case OP_OR_INT_LIT16:
		case OP_XOR_INT_LIT16: {
			dcv.visitInInsn(opcode, arg1 & 0xf, (arg1 >> 4) & 0xf, (short) (arg2));
		}
			break;

		case OP_NEW_ARRAY: {
			int a = arg1 & 0xf;
			int dem = (arg1 >> 4) & 0xf;
			String type = dex.getType(arg2);
			dcv.visitArrayInsn(opcode, type, a, dem);
		}
			break;
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
		case OP_CMPG_FLOAT: {
			dcv.visitInInsn(opcode, arg1, (arg2 >> 8) & 0xff, arg2 & 0xff);
		}
			break;
		case OP_GOTO_16: {
			dcv.visitJumpInsn(opcode, this.labels.get(offset + arg2));
		}
			break;
		case OP_MOVE_OBJECT_FROM16:
		case OP_MOVE_FROM16:
		case OP_MOVE_WIDE_FROM16: {
			dcv.visitInInsn(opcode, arg1, arg2 & 0xff);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int, int, int)
	 */
	public void visit(int opcode, int arg1, int arg2, int arg3) {
		switch (opcode) {
		case OP_INVOKE_VIRTUAL:
		case OP_INVOKE_SUPER:
		case OP_INVOKE_DIRECT:
		case OP_INVOKE_STATIC:
		case OP_INVOKE_INTERFACE: {
			Method m = dex.getMethod(arg2);
			int args[] = getValues(arg1, arg3);
			args = filter(m, args);
			dcv.visitMethodInsn(opcode, m, args);
		}
			break;
		case OP_INVOKE_VIRTUAL_RANGE:
		case OP_INVOKE_SUPER_RANGE:
		case OP_INVOKE_DIRECT_RANGE:
		case OP_INVOKE_STATIC_RANGE:
		case OP_INVOKE_INTERFACE_RANGE: {
			int args[] = new int[arg1];
			for (int i = 0; i < arg1; i++) {
				args[i] = arg3 + i;
			}
			Method m = dex.getMethod(arg2);
			args = filter(m, args);
			// 转换成非Range的指令
			dcv.visitMethodInsn(opcode - 6, m, args);
		}
			break;
		case OP_CONST: {
			// issue 13
			int intV = (arg2 & 0xFFFF) | ((arg3 << 16) & 0xFFFF0000);
			// float floatV = Float.intBitsToFloat(intV);
			dcv.visitLdcInsn(opcode, intV, arg1);
		}
			break;
		case OP_CONST_WIDE_32: {
			long longV = (arg3 << 16) | arg2;
			dcv.visitLdcInsn(opcode, longV, arg1);
		}
			break;

		case OP_FILLED_NEW_ARRAY: {
			dcv.visitFilledNewArrayIns(opcode, dex.getType(arg2), getValues(arg1, arg3));
		}
			break;
		case OP_FILLED_NEW_ARRAY_RANGE: {
			int args[] = new int[arg1];
			for (int i = 0; i < arg1; i++) {
				args[i] = arg3 + i;
			}
			dcv.visitFilledNewArrayIns(OP_FILLED_NEW_ARRAY, dex.getType(arg2), args);

		}
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	private int[] filter(Method m, int[] args) {

		String types[] = m.getType().getParameterTypes();
		if (types.length == 0 || types.length == args.length)
			return args;
		int index = args.length - 1;
		int i = args.length - 1;
		for (int j = types.length - 1; j >= 0; j--) {
			String type = types[j];
			if ("J".equals(type) || "D".equals(type)) {
				i--;
			}
			args[index--] = args[i--];
		}
		switch (i) {
		case -1:
			break;
		case 0:
			args[index--] = args[i--];
			break;
		default:
			throw new RuntimeException("Should never happen.");
		}
		int start = index + 1;
		if (start == 0)
			return args;
		int length = args.length - start;
		int[] nArgs = new int[length];
		System.arraycopy(args, start, nArgs, 0, length);
		return nArgs;
	}

	private static int[] getValues(int arg1, int value) {
		int size = (arg1 >> 4) & 0xf;
		int[] a = new int[size];
		for (int i = 0; i < size && i < 4; i++) {
			a[i] = value & 0xf;
			value >>= 4;
		}
		if (size == 5) {
			a[4] = arg1 & 0xf;
		}
		return a;
	}

	int offset;

	public void visitOffset(int i) {
		this.offset = i;
	}
}
