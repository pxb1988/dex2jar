/**
 * 
 */
package pxb.android.dex2jar.dump;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexCodeAdapter;
import pxb.android.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DumpDexCodeAdapter extends DexCodeAdapter implements DexOpcodes {
	private static final Logger log = LoggerFactory.getLogger(DumpDexCodeAdapter.class);

	int _index;

	/**
	 * @param dcv
	 */
	public DumpDexCodeAdapter(DexCodeVisitor dcv) {
		super(dcv);
	}

	protected void info(int opcode, String format, Object... args) {
		String s = String.format(format, args);
		log.info(String.format("%04x|%02x|%-20s|%s", this._index, opcode, DexOpcodeDump.dump(opcode), s));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visit(int, int, int)
	 */
	@Override
	public void visit(int total_registers_size, int in_register_size, int instruction_size) {
		log.info(String.format("%20s:%d,", "reg_size", total_registers_size));
		log.info(String.format("%20s:%d,", "in_reg_size", in_register_size));
		log.info(String.format("%20s:%d,", "ins_size", instruction_size));
		super.visit(total_registers_size, in_register_size, instruction_size);
	}

	private static String c(String type) {
		return Type.getType(type).getClassName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitArrayInsn(int, int,
	 * int, int)
	 */
	@Override
	public void visitArrayInsn(int opcode, int value, int array, int index) {
		switch (opcode) {
		case OP_APUT:
		case OP_APUT_BOOLEAN:
		case OP_APUT_BYTE:
		case OP_APUT_CHAR:
		case OP_APUT_OBJECT:
		case OP_APUT_SHORT:
		case OP_APUT_WIDE:
			info(opcode, "v%d[v%d]=v%d", array, index, value);
			break;
		case OP_AGET:
		case OP_AGET_BOOLEAN:
		case OP_AGET_BYTE:
		case OP_AGET_CHAR:
		case OP_AGET_OBJECT:
		case OP_AGET_SHORT:
		case OP_AGET_WIDE:
			info(opcode, "v%d=v%d[v%d]", value, array, index);
			break;
		}
		super.visitArrayInsn(opcode, value, array, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitArrayInsn(int,
	 * java.lang.String, int, int)
	 */
	@Override
	public void visitArrayInsn(int opcode, String type, int saveToReg, int demReg) {
		String type_show = Type.getType(type).getElementType().getClassName();
		info(opcode, "v%d=new %s[v%d]", saveToReg, type_show, demReg);
		super.visitArrayInsn(opcode, type, saveToReg, demReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitEnd()
	 */
	@Override
	public void visitEnd() {
		super.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitFieldInsn(int,
	 * pxb.android.dex2jar.Field, int)
	 */
	@Override
	public void visitFieldInsn(int opcode, Field field, int reg) {
		switch (opcode) {
		case OP_SPUT_OBJECT:
		case OP_SPUT_BOOLEAN:
		case OP_SPUT_BYTE:
		case OP_SPUT_CHAR:
		case OP_SPUT_SHORT:
		case OP_SPUT_WIDE:
		case OP_SPUT:
			info(opcode, "%s.%s=v%d  //%s", c(field.getOwner()), field.getName(), reg, field);
			break;
		case OP_SGET_OBJECT:
		case OP_SGET_BOOLEAN:
		case OP_SGET_BYTE:
		case OP_SGET_CHAR:
		case OP_SGET_SHORT:
		case OP_SGET_WIDE:
		case OP_SGET:
			info(opcode, "v%d=%s.%s  //%s", reg, c(field.getOwner()), field.getName(), field);
			break;
		}
		super.visitFieldInsn(opcode, field, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitFieldInsn(int,
	 * pxb.android.dex2jar.Field, int, int)
	 */
	@Override
	public void visitFieldInsn(int opcode, Field field, int value_reg, int owner_reg) {
		switch (opcode) {
		case OP_IGET_OBJECT:
		case OP_IGET_BOOLEAN:
		case OP_IGET_BYTE:
		case OP_IGET_SHORT:
		case OP_IGET:
		case OP_IGET_WIDE:
			info(opcode, "v%d=v%d.%s  //%s", value_reg, owner_reg, field.getName(), field);
			break;
		case OP_IPUT_OBJECT:
		case OP_IPUT_BOOLEAN:
		case OP_IPUT_BYTE:
		case OP_IPUT_SHORT:
		case OP_IPUT:
		case OP_IPUT_WIDE:
			info(opcode, "v%d.%s=v%d  //%s", owner_reg, field.getName(), value_reg, field);
			break;
		}

		super.visitFieldInsn(opcode, field, value_reg, owner_reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitFillArrayInsn(int,
	 * int, int, int, java.lang.Object[])
	 */
	@Override
	public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {

		// switch (elemWidth) {
		// case 1:
		// info(opcode, "v%d=new byte[%d]", reg, initLength);
		// break;
		// case 2:
		// info(opcode, "v%d=new short[%d]", reg, initLength);
		// break;
		// case 4:
		// info(opcode, "v%d=new int[%d]", reg, initLength);
		// break;
		// case 8:
		// info(opcode, "v%d=new long[%d]", reg, initLength);
		// break;
		// }
		for (int j = 0; j < initLength; j++) {
			info(opcode, "v%d[%d]=%d", reg, j, values[j]);
		}
		super.visitFillArrayInsn(opcode, reg, elemWidth, initLength, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitInInsn(int, int,
	 * int)
	 */
	@Override
	public void visitInInsn(int opcode, int saveToReg, int opReg) {
		switch (opcode) {
		case OP_AND_INT_2ADDR:
		case OP_AND_LONG_2ADDR:
			info(opcode, "v%d &= v%d", saveToReg, opReg);
			break;
		case OP_OR_INT_2ADDR:
		case OP_OR_LONG_2ADDR:
			info(opcode, "v%d |= v%d", saveToReg, opReg);
			break;
		case OP_XOR_INT_2ADDR:
		case OP_XOR_LONG_2ADDR:
			info(opcode, "v%d ^= v%d", saveToReg, opReg);
			break;
		case OP_MUL_LONG_2ADDR:
		case OP_MUL_INT_2ADDR:
		case OP_MUL_FLOAT_2ADDR:
		case OP_MUL_DOUBLE_2ADDR:
			info(opcode, "v%d *= v%d", saveToReg, opReg);
			break;
		case OP_SUB_INT_2ADDR:
		case OP_SUB_LONG_2ADDR:
		case OP_SUB_FLOAT_2ADDR:
		case OP_SUB_DOUBLE_2ADDR:
			info(opcode, "v%d -= v%d", saveToReg, opReg);
			break;
		case OP_REM_INT_2ADDR:
		case OP_REM_LONG_2ADDR:
			info(opcode, "v%d %%= v%d", saveToReg, opReg);
			break;
		case OP_DIV_INT_2ADDR:
		case OP_DIV_LONG_2ADDR:
		case OP_DIV_FLOAT_2ADDR:
		case OP_DIV_DOUBLE_2ADDR:
			info(opcode, "v%d /= v%d", saveToReg, opReg);
			break;
		case OP_ADD_INT_2ADDR:
		case OP_ADD_LONG_2ADDR:
		case OP_ADD_FLOAT_2ADDR:
		case OP_ADD_DOUBLE_2ADDR:
			info(opcode, "v%d += v%d", saveToReg, opReg);
			break;
		case OP_NEG_INT:
		case OP_NEG_DOUBLE:
		case OP_NEG_FLOAT:
		case OP_NEG_LONG:
			info(opcode, "v%d = ~v%d", saveToReg, opReg);
			break;
		case OP_MOVE_OBJECT:
		case OP_MOVE:
		case OP_MOVE_WIDE:
		case OP_MOVE_OBJECT_FROM16:
		case OP_MOVE_FROM16:
		case OP_MOVE_WIDE_FROM16:
			info(opcode, "v%d = v%d", saveToReg, opReg);
			break;
		case OP_INT_TO_BYTE:
			info(opcode, "v%d = (byte)v%d", saveToReg, opReg);
			break;
		case OP_INT_TO_CHAR:
			info(opcode, "v%d = (char)v%d", saveToReg, opReg);
			break;
		case OP_INT_TO_DOUBLE:
		case OP_INT_TO_FLOAT:
		case OP_INT_TO_LONG:
			info(opcode, "v%d = v%d", saveToReg, opReg);
			break;
		case OP_INT_TO_SHORT:
			info(opcode, "v%d = (short)v%d", saveToReg, opReg);
			break;
		case OP_LONG_TO_DOUBLE:
		case OP_LONG_TO_FLOAT:
			info(opcode, "v%d = v%d", saveToReg, opReg);
			break;
		case OP_LONG_TO_INT:
			info(opcode, "v%d = (int)v%d", saveToReg, opReg);
			break;
		case OP_DOUBLE_TO_FLOAT:
			info(opcode, "v%d = (float)v%d", saveToReg, opReg);
			break;
		case OP_DOUBLE_TO_INT:
			info(opcode, "v%d = (int)v%d", saveToReg, opReg);
			break;
		case OP_DOUBLE_TO_LONG:
			info(opcode, "v%d = (long)v%d", saveToReg, opReg);
			break;
		case OP_FLOAT_TO_INT:
			info(opcode, "v%d = (int)v%d", saveToReg, opReg);
			break;
		case OP_FLOAT_TO_DOUBLE:
			info(opcode, "v%d = v%d", saveToReg, opReg);
			break;
		case OP_FLOAT_TO_LONG:
			info(opcode, "v%d = (long)v%d", saveToReg, opReg);
			break;

		case OP_ARRAY_LENGTH:
			info(opcode, "v%d = v%d.length", saveToReg, opReg);
			break;
		}
		super.visitInInsn(opcode, saveToReg, opReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitInInsn(int, int,
	 * int, int)
	 */
	@Override
	public void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg) {
		switch (opcode) {
		case OP_AND_INT:
		case OP_AND_LONG:
			info(opcode, "v%d = v%d & v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_OR_INT:
		case OP_OR_LONG:
			info(opcode, "v%d = v%d | v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_XOR_INT:
		case OP_XOR_LONG:
			info(opcode, "v%d = v%d ^ v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_CMP_LONG:
			info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_MUL_INT:
		case OP_MUL_LONG:
		case OP_MUL_FLOAT:
		case OP_MUL_DOUBLE:
			info(opcode, "v%d = v%d * v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_DIV_INT:
		case OP_DIV_LONG:
		case OP_DIV_FLOAT:
		case OP_DIV_DOUBLE:
			info(opcode, "v%d = v%d / v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_ADD_INT:
		case OP_ADD_LONG:
		case OP_ADD_FLOAT:
		case OP_ADD_DOUBLE:
			info(opcode, "v%d = v%d + v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_SUB_INT:
		case OP_SUB_DOUBLE:
		case OP_SUB_FLOAT:
		case OP_SUB_LONG:
			info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_REM_LONG:
		case OP_REM_INT:
		case OP_REM_FLOAT:
		case OP_REM_DOUBLE:
			info(opcode, "v%d = v%d %% v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_CMPL_DOUBLE:
		case OP_CMPL_FLOAT:
			info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_MUL_INT_LIT16:
			info(opcode, "v%d = v%d * %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_DIV_INT_LIT16:
			info(opcode, "v%d = v%d / %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_REM_INT_LIT16:
			info(opcode, "v%d = v%d %% %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_ADD_INT_LIT16:
			info(opcode, "v%d = v%d + %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_AND_INT_LIT16:
			info(opcode, "v%d = v%d & %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_OR_INT_LIT16:
			info(opcode, "v%d = v%d | %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_XOR_INT_LIT16:
			info(opcode, "v%d = v%d ^ %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_AND_INT_LIT8:
			info(opcode, "v%d = v%d & %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_ADD_INT_LIT8:
			info(opcode, "v%d = v%d + %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_REM_INT_LIT8:
			info(opcode, "v%d = v%d %% %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_DIV_INT_LIT8:
			info(opcode, "v%d = v%d / %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_MUL_INT_LIT8:
			info(opcode, "v%d = v%d * %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_SHR_INT_LIT8:
			info(opcode, "v%d = v%d >> %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_SHL_INT_LIT8:
			info(opcode, "v%d = v%d << %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_USHR_INT_LIT8:
			info(opcode, "v%d = v%d >>> %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_OR_INT_LIT8:
			info(opcode, "v%d = v%d | %d", saveToReg, opReg, opValueOrReg);
			break;
		case OP_XOR_INT_LIT8:
			info(opcode, "v%d = v%d ^ %d", saveToReg, opReg, opValueOrReg);
			break;
		}
		super.visitInInsn(opcode, saveToReg, opReg, opValueOrReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitInsn(int)
	 */
	@Override
	public void visitInsn(int opcode) {
		switch (opcode) {
		case OP_RETURN_VOID:
			info(opcode, "return");
			break;
		}
		super.visitInsn(opcode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitJumpInsn(int, int)
	 */
	@Override
	public void visitJumpInsn(int opcode, int offset) {
		switch (opcode) {
		case OP_GOTO:
		case OP_GOTO_16:
			info(opcode, "goto %04x  //%c%04x", this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		}
		super.visitJumpInsn(opcode, offset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitJumpInsn(int, int,
	 * int)
	 */
	@Override
	public void visitJumpInsn(int opcode, int offset, int reg) {
		switch (opcode) {
		case OP_IF_EQZ:
			info(opcode, "if v%d == 0 goto %04x  //%c%04x", reg, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_NEZ:
			info(opcode, "if v%d != 0 goto %04x  //%c%04x", reg, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_LTZ:
			info(opcode, "if v%d <  0 goto %04x  //%c%04x", reg, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_GEZ:
			info(opcode, "if v%d >= 0 goto %04x  //%c%04x", reg, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_GTZ:
			info(opcode, "if v%d >  0 goto %04x  //%c%04x", reg, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_LEZ:
			info(opcode, "if v%d <= 0 goto %04x  //%c%04x", reg, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		}
		super.visitJumpInsn(opcode, offset, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitJumpInsn(int, int,
	 * int, int)
	 */
	@Override
	public void visitJumpInsn(int opcode, int offset, int reg1, int reg2) {
		switch (opcode) {
		case OP_IF_EQ:
			info(opcode, "if v%d == v%d goto %04x  //%c%04x", reg1, reg2, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_NE:
			info(opcode, "if v%d != v%d goto %04x  //%c%04x", reg1, reg2, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_LT:
			info(opcode, "if v%d <  v%d goto %04x  //%c%04x", reg1, reg2, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_GE:
			info(opcode, "if v%d >= v%d goto %04x  //%c%04x", reg1, reg2, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_GT:
			info(opcode, "if v%d >  v%d goto %04x  //%c%04x", reg1, reg2, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		case OP_IF_LE:
			info(opcode, "if v%d <= v%d goto %04x  //%c%04x", reg1, reg2, this._index + offset, offset >= 0 ? '+' : '-', offset >= 0 ? offset : -offset);
			break;
		}
		super.visitJumpInsn(opcode, offset, reg1, reg2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLabel(int)
	 */
	@Override
	public void visitLabel(int index) {
		this._index = index;
		super.visitLabel(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLdcInsn(int,
	 * java.lang.Object, int)
	 */
	@Override
	public void visitLdcInsn(int opcode, Object value, int reg) {
		if (value instanceof String)
			info(opcode, "v%d=\"%s\"", reg, value);
		else if (value instanceof Type) {
			info(opcode, "v%d=%s.class", reg, ((Type) value).getClassName());
		} else {
			info(opcode, "v%d=%s  //", reg, value);
		}
		super.visitLdcInsn(opcode, value, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLineNumber(int,
	 * int)
	 */
	@Override
	public void visitLineNumber(int line, int label) {
		super.visitLineNumber(line, label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeAdapter#visitLocalVariable(java.lang
	 * .String, java.lang.String, java.lang.String, int, int, int)
	 */
	@Override
	public void visitLocalVariable(String name, String type, String signature, int start, int end, int reg) {
		super.visitLocalVariable(name, type, signature, start, end, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeAdapter#visitLookupSwitchInsn(int,
	 * int, int, int[], int[])
	 */
	@Override
	public void visitLookupSwitchInsn(int opcode, int reg, int label, int[] cases, int[] label2) {
		info(opcode, "switch(v%d)", reg);
		for (int i = 0; i < cases.length; i++) {
			info(opcode, "case %d: goto %04x  //%c%04x", cases[i], label2[i] + this._index, label2[i] >= 0 ? '+' : '-', label2[i] >= 0 ? label2[i] : -label2[i]);
		}
		info(opcode, "default: goto %04x  //%c%04x", label + this._index, label >= 0 ? '+' : '-', label >= 0 ? label : -label);
		super.visitLookupSwitchInsn(opcode, reg, label, cases, label2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitMethodInsn(int,
	 * pxb.android.dex2jar.Method, int[])
	 */
	@Override
	public void visitMethodInsn(int opcode, Method method, int[] regs) {

		switch (opcode) {
		case OP_INVOKE_STATIC:
		case OP_INVOKE_STATIC_RANGE: {
			int i = 0;
			StringBuilder sb = new StringBuilder();
			for (String type : method.getType().getParameterTypes()) {
				sb.append('v').append(regs[i++]).append(',');
				if ("D".equals(type) || "J".equals(type)) {
					i++;
				}
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			if (method.getType().getReturnType().equals("V")) {
				info(opcode, "%s.%s(%s)  //%s", c(method.getOwner()), method.getName(), sb.toString(), method.toString());
			} else {
				info(opcode, "XXX=%s.%s(%s)  //%s", c(method.getOwner()), method.getName(), sb.toString(), method.toString());

			}
		}
			break;
		case OP_INVOKE_DIRECT_RANGE:
		case OP_INVOKE_INTERFACE_RANGE:
		case OP_INVOKE_SUPER_RANGE:
		case OP_INVOKE_VIRTUAL_RANGE:
		case OP_INVOKE_VIRTUAL:
		case OP_INVOKE_DIRECT:
		case OP_INVOKE_INTERFACE:
		case OP_INVOKE_SUPER: {
			int i = 1;
			StringBuilder sb = new StringBuilder();
			for (String type : method.getType().getParameterTypes()) {
				sb.append(',').append('v').append(regs[i++]);
				if ("D".equals(type) || "J".equals(type)) {
					i++;
				}
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(0);
			}
			if (method.getType().getReturnType().equals("V")) {
				info(opcode, "v%d.%s(%s)  //%s", regs[0], method.getName(), sb.toString(), method.toString());
			} else {
				info(opcode, "XXX=v%d.%s(%s)  //%s", regs[0], method.getName(), sb.toString(), method.toString());

			}
		}
			break;
		}
		super.visitMethodInsn(opcode, method, regs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeAdapter#visitTableSwitchInsn(int,
	 * int, int, int, int, int[])
	 */
	@Override
	public void visitTableSwitchInsn(int opcode, int reg, int first_case, int last_case, int label, int[] labels) {
		info(opcode, "switch(v%d)", reg);
		for (int i = 0; i < labels.length; i++) {
			info(opcode, "case %d: goto %04x  //%c%04x", first_case + i, labels[i] + this._index, labels[i] >= 0 ? '+' : '-', labels[i] >= 0 ? labels[i] : -labels[i]);
		}
		info(opcode, "default: goto %04x  //%c%04x", label + this._index, label >= 0 ? '+' : '-', label >= 0 ? label : -label);

		super.visitTableSwitchInsn(opcode, reg, first_case, last_case, label, labels);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitTryCatch(int, int,
	 * int, java.lang.String)
	 */
	@Override
	public void visitTryCatch(int start, int offset, int handler, String type) {
		log.info(String.format("%04x ~ %04x > %04x %s", start, start + offset, handler, type));
		super.visitTryCatch(start, offset, handler, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitTypeInsn(int,
	 * java.lang.String, int)
	 */
	@Override
	public void visitTypeInsn(int opcode, String type, int toReg) {
		switch (opcode) {
		case OP_NEW_INSTANCE:
			info(opcode, "v%d=NEW %s", toReg, type);
			break;
		case OP_CONST_CLASS:
			info(opcode, "v%d=%s.class", toReg, Type.getType(type).getClassName());
			break;
		case OP_CHECK_CAST:
			info(opcode, "v%d=(%s) v%d", toReg, Type.getType(type).getClassName(), toReg);
			break;
		}
		super.visitTypeInsn(opcode, type, toReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitTypeInsn(int,
	 * java.lang.String, int, int)
	 */
	@Override
	public void visitTypeInsn(int opcode, String type, int toReg, int fromReg) {
		switch (opcode) {
		case OP_INSTANCE_OF:
			info(opcode, "v%d=v%d instanceof %s", toReg, fromReg, Type.getType(type).getClassName());
			break;
		}
		super.visitTypeInsn(opcode, type, toReg, fromReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitVarInsn(int, int)
	 */
	@Override
	public void visitVarInsn(int opcode, int reg) {
		switch (opcode) {
		case OP_MOVE_RESULT_OBJECT:
		case OP_MOVE_RESULT:
		case OP_MOVE_RESULT_WIDE:
		case OP_MOVE_EXCEPTION:
			info(opcode, "v%d=XXX", reg);
			break;
		case OP_THROW:
			info(opcode, "throw v%d", reg);
			break;
		case OP_RETURN_OBJECT:
		case OP_RETURN:
		case OP_RETURN_WIDE:
			info(opcode, "return v%d", reg);
			break;
		case OP_MONITOR_ENTER:
			info(opcode, "lock v%d", reg);
			break;
		case OP_MONITOR_EXIT:
			info(opcode, "unlock v%d", reg);
			break;
		}
		super.visitVarInsn(opcode, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeAdapter#visitFilledNewArrayIns(int,
	 * java.lang.String, int[])
	 */
	@Override
	public void visitFilledNewArrayIns(int opcode, String type, int[] regs) {
		info(opcode, "XXX=new %s[%d]", Type.getType(type).getElementType().getClassName(), regs.length);
		for (int i = 0; i < regs.length; i++) {
			info(opcode, "XXX[%d]=v%d", i, regs[i]);
		}
		super.visitFilledNewArrayIns(opcode, type, regs);
	}

}
