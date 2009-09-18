/**
 * 
 */
package pxb.android.dex2jar.v2;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodeUtil;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.asm.PDescMethodVisitor;
import pxb.android.dex2jar.optimize.LdcOptimizeAdapter;
import pxb.android.dex2jar.optimize.NewOptimizeAdapter;
import pxb.android.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexCodeAdapter implements DexCodeVisitor, Opcodes, DexOpcodes {
	PDescMethodVisitor mv;
	private int map[];
	boolean isStatic;
	Type[] parameters;

	/**
	 * @param mv2
	 */
	public ToAsmDexCodeAdapter(MethodVisitor mv, String owner, Method method) {
		mv = new LdcOptimizeAdapter(mv);// 优化Ldc
		mv = new NewOptimizeAdapter(mv);// 优化New
		this.mv = new PDescMethodVisitor(mv);
		isStatic = 0 != (method.getAccessFlags() & ACC_STATIC);
		this.mv.visit(owner, method.getType().getDesc(), isStatic);
		parameters = Type.getArgumentTypes(method.getType().getDesc());
	}

	/**
	 * <pre>
	 * 012 34 567 
	 * 567 34 012 
	 * reg=8 
	 * m=3 
	 * n=5
	 * 012
	 * reg=3
	 * mSize=2;
	 * n=1;
	 * 012345
	 * </pre>
	 * 
	 * @param total
	 * @param m
	 * @return
	 */
	public static int[] RegisterMapGenerator(int total, int mSize) {
		int[] map = new int[total];
		if (total == 1)
			return map;
		int n = total - mSize;
		for (int i = 1; i < mSize; i++) {
			map[n + i] = i;
		}
		for (int i = 0; i < n; i++) {
			map[i] = mSize + i;
		}
		return map;
	}

	int now_offset;

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLabel(int)
	 */
	public void visitLabel(int index) {
		mv.visitLabel(labels[index]);
		this.now_offset = index;
	}

	Label[] labels;

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectLoadOpcode(int reg) {
		Type type = mv.getLocal(map[reg]);
		return type.getOpcode(ILOAD);
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectAAStoreOpcode(int reg) {
		Type type = mv.getLocal(map[reg]);
		return type.getOpcode(IASTORE);
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectStoreOpcode() {
		return mv.getStack(1).getOpcode(ISTORE);
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectReturnOpcode() {
		return mv.getStack(1).getOpcode(IRETURN);
	}

	int detectStoreOpcode(String type) {
		return Type.getType(type).getOpcode(ISTORE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTryCatch(int, int,
	 * int, java.lang.String)
	 */
	public void visitTryCatch(int start, int offset, int handler, String type) {
		mv.visitTryCatchBlock(labels[start], labels[start + offset], labels[handler], type);
	}

	private void load(int reg) {
		mv.visitVarInsn(this.detectLoadOpcode(reg), map[reg]);
	}

	private void store(int reg) {
		mv.visitVarInsn(this.detectStoreOpcode(), map[reg]);
	}

	public void loadArgument(Method method, int[] registers, boolean isStatic) {
		int i = 0;
		if (!isStatic) {
			this.load(registers[i++]);
		}
		while (i < registers.length) {
			this.load(registers[i++]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitMethodInsn(int,
	 * pxb.android.dex2jar.Method, int[])
	 */
	public void visitMethodInsn(int opcode, Method method, int[] registers) {
		// ==========
		// <init>方法特殊处理
		// if (method.getName().equals("<init>")) {
		// int typeReg = registers[0];
		// String type = newInsTypes[typeReg];
		// if (type != null) {
		// mv.visitTypeInsn(NEW, type);
		// mv.visitInsn(DUP);
		// int i = 1;
		// for (String x : method.getType().getParameterTypes()) {
		// this.load(registers[i++]);
		// if ("D".equals(x) || "J".equals(x)) {
		// i++;
		// }
		// }
		// mv.visitMethodInsn(INVOKESPECIAL, method.getOwner(),
		// method.getName(), method.getType().getDesc());
		// this.store(typeReg);
		// newInsTypes[typeReg] = null;
		// } else {
		// loadArgument(method, registers, false);
		// mv.visitMethodInsn(INVOKESPECIAL, method.getOwner(),
		// method.getName(), method.getType().getDesc());
		// }
		// return;
		// }
		// ==========
		switch (opcode) {
		case OP_INVOKE_STATIC:
		case OP_INVOKE_STATIC_RANGE: {
			loadArgument(method, registers, true);
			mv.visitMethodInsn(INVOKESTATIC, method.getOwner(), method.getName(), method.getType().getDesc());
		}
			break;
		default:
			loadArgument(method, registers, false);
			switch (opcode) {
			case OP_INVOKE_DIRECT:
			case OP_INVOKE_SUPER:
			case OP_INVOKE_DIRECT_RANGE:
			case OP_INVOKE_SUPER_RANGE: {
				mv.visitMethodInsn(INVOKESPECIAL, method.getOwner(), method.getName(), method.getType().getDesc());
			}
				break;
			case OP_INVOKE_VIRTUAL:
			case OP_INVOKE_VIRTUAL_RANGE: {
				mv.visitMethodInsn(INVOKEVIRTUAL, method.getOwner(), method.getName(), method.getType().getDesc());
			}
				break;
			case OP_INVOKE_INTERFACE:
			case OP_INVOKE_INTERFACE_RANGE: {
				mv.visitMethodInsn(INVOKEINTERFACE, method.getOwner(), method.getName(), method.getType().getDesc());
			}
				break;

			default:
				throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
						.dump(opcode)));
			}
		}
	}

	String[] newInsTypes;

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int,
	 * java.lang.String, int)
	 */
	public void visitTypeInsn(int opcode, String type, int reg, int fromReg) {
		switch (opcode) {
		case OP_INSTANCE_OF: {
			this.load(fromReg);
			mv.visitTypeInsn(INSTANCEOF, type);
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, int,
	 * int, int)
	 */
	public void visitArrayInsn(int opcode, int valueReg, int arrayReg, int indexReg) {
		switch (opcode) {
		case OP_APUT:
		case OP_APUT_BOOLEAN:
		case OP_APUT_BYTE:
		case OP_APUT_CHAR:
		case OP_APUT_OBJECT:
		case OP_APUT_SHORT:
		case OP_APUT_WIDE: {
			load(arrayReg);
			load(indexReg);
			load(valueReg);
			switch (opcode) {
			case OP_APUT:
				mv.visitInsn(IASTORE);
				break;
			case OP_APUT_BOOLEAN:
				mv.visitInsn(BASTORE);
				break;
			case OP_APUT_BYTE:
				mv.visitInsn(BASTORE);
				break;
			case OP_APUT_CHAR:
				mv.visitInsn(CASTORE);
				break;
			case OP_APUT_OBJECT:
				mv.visitInsn(AASTORE);
				break;
			case OP_APUT_SHORT:
				mv.visitInsn(SASTORE);
				break;
			case OP_APUT_WIDE:
				mv.visitInsn(AASTORE);
				break;
			}
		}
			break;
		case OP_AGET:
		case OP_AGET_BOOLEAN:
		case OP_AGET_BYTE:
		case OP_AGET_CHAR:
		case OP_AGET_OBJECT:
		case OP_AGET_SHORT:
		case OP_AGET_WIDE: {
			load(arrayReg);
			load(indexReg);
			switch (opcode) {
			case OP_AGET:
				mv.visitInsn(IALOAD);
				break;
			case OP_AGET_BOOLEAN:
				mv.visitInsn(BALOAD);
				break;
			case OP_AGET_BYTE:
				mv.visitInsn(BALOAD);
				break;
			case OP_AGET_CHAR:
				mv.visitInsn(CALOAD);
				break;
			case OP_AGET_OBJECT:
				mv.visitInsn(AALOAD);
				break;
			case OP_AGET_SHORT:
				mv.visitInsn(SALOAD);
				break;
			case OP_AGET_WIDE:
				mv.visitInsn(AALOAD);
				break;
			}
			store(valueReg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int,
	 * pxb.android.dex2jar.Field, int, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int value_reg, int owner_reg) {
		switch (opcode) {
		case OP_IGET:
		case OP_IGET_WIDE:
		case OP_IGET_OBJECT:
		case OP_IGET_BOOLEAN:
		case OP_IGET_BYTE:
		case OP_IGET_SHORT:
			//
		{
			load(owner_reg);
			mv.visitFieldInsn(GETFIELD, field.getOwner(), field.getName(), field.getType());
			store(value_reg);
		}
			break;

		case OP_IPUT:
		case OP_IPUT_WIDE:
		case OP_IPUT_OBJECT:
		case OP_IPUT_BOOLEAN:
		case OP_IPUT_BYTE:
		case OP_IPUT_SHORT:
			//
		{
			this.load(owner_reg);
			this.load(value_reg);
			mv.visitFieldInsn(PUTFIELD, field.getOwner(), field.getName(), field.getType());
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInsn(int)
	 */
	public void visitInsn(int opcode) {
		switch (opcode) {
		case OP_RETURN_VOID: {
			mv.visitInsn(RETURN);
		}
			break;
		case OP_CONST_4:
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitIntInsn(int, int,
	 * int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg, int valueOrReg) {
		switch (opcode) {
		case OP_USHR_INT_LIT8:
		case OP_SHR_INT_LIT8:
		case OP_SHL_INT_LIT8:
		case OP_ADD_INT_LIT8:
		case OP_REM_INT_LIT8:
		case OP_AND_INT_LIT8:
		case OP_OR_INT_LIT8:
		case OP_XOR_INT_LIT8:
		case OP_DIV_INT_LIT8:
		case OP_MUL_INT_LIT8: {
			this.load(opReg);
			mv.visitLdcInsn(valueOrReg);
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			this.store(saveToReg);
		}
			break;

		case OP_AND_LONG:
		case OP_AND_INT:
		case OP_OR_LONG:
		case OP_OR_INT:
		case OP_XOR_LONG:
		case OP_XOR_INT:

		case OP_ADD_INT:
		case OP_ADD_LONG:
		case OP_ADD_FLOAT:
		case OP_ADD_DOUBLE:
		case OP_SUB_FLOAT:
		case OP_SUB_DOUBLE:
		case OP_SUB_INT:
		case OP_SUB_LONG:
		case OP_DIV_INT:
		case OP_DIV_LONG:
		case OP_DIV_FLOAT:
		case OP_DIV_DOUBLE:
		case OP_MUL_INT:
		case OP_MUL_LONG:
		case OP_MUL_FLOAT:
		case OP_MUL_DOUBLE:
		case OP_CMP_LONG:
		case OP_REM_LONG:
		case OP_REM_INT:
		case OP_REM_FLOAT:
		case OP_REM_DOUBLE:
		case OP_CMPL_DOUBLE:
		case OP_CMPL_FLOAT: {
			this.load(opReg);
			this.load(valueOrReg);
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			this.store(saveToReg);
		}
			break;

		case OP_MUL_INT_LIT16:
		case OP_DIV_INT_LIT16:
		case OP_REM_INT_LIT16:
		case OP_ADD_INT_LIT16:
		case OP_AND_INT_LIT16:
		case OP_OR_INT_LIT16:
		case OP_XOR_INT_LIT16: {
			this.load(opReg);
			mv.visitLdcInsn(valueOrReg);
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			this.store(saveToReg);
		}
			break;

		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}

	}

	public Label getLabel(int offset) {
		return labels[now_offset + offset];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int,
	 * int)
	 */
	public void visitJumpInsn(int opcode, int offset, int reg) {
		Label label = getLabel(offset);
		this.load(reg);
		switch (opcode) {
		case OP_IF_NEZ: {
			if (this.detectLoadOpcode(reg) == ALOAD) {
				mv.visitJumpInsn(IFNONNULL, label);
			} else {
				mv.visitJumpInsn(IFNE, label);
			}
		}
			break;
		case OP_IF_EQZ: {
			if (this.detectLoadOpcode(reg) == ALOAD) {
				mv.visitJumpInsn(IFNULL, label);
			} else {
				mv.visitJumpInsn(IFEQ, label);
			}
		}
			break;
		case OP_IF_GTZ: {
			mv.visitJumpInsn(IFGT, label);
		}
			break;
		case OP_IF_GEZ: {
			mv.visitJumpInsn(IFGE, label);
		}
			break;
		case OP_IF_LEZ: {
			mv.visitJumpInsn(IFLE, label);
		}
			break;
		case OP_IF_LTZ: {
			mv.visitJumpInsn(IFLT, label);
		}
			break;

		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int,
	 * int, int)
	 */
	public void visitJumpInsn(int opcode, int offset, int reg1, int reg2) {
		Label label = getLabel(offset);
		this.load(reg2);
		this.load(reg1);
		mv.visitJumpInsn(DexOpcodeUtil.mapOpcode(opcode), label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLdcInsn(int,
	 * java.lang.Object, int)
	 */
	public void visitLdcInsn(int opcode, Object value, int reg) {
		switch (opcode) {
		case OP_CONST_STRING:// const-string
		{
			mv.visitLdcInsn(value);
			this.store(reg);
		}
			break;
		case OP_CONST_4:
		case OP_CONST_16:
		case OP_CONST_HIGH16:
		case OP_CONST_WIDE_16:
		case OP_CONST_WIDE_32:
		case OP_CONST_WIDE_HIGH16:
		case OP_CONST_WIDE:
		case OP_CONST: {
			mv.visitLdcInsn(value);
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitStaticFieldInsn(int,
	 * pxb.android.dex2jar.Field, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int reg) {
		switch (opcode) {
		case OP_SPUT_OBJECT:
		case OP_SPUT:
		case OP_SPUT_WIDE:
		case OP_SPUT_BOOLEAN:
		case OP_SPUT_BYTE:
		case OP_SPUT_CHAR:
		case OP_SPUT_SHORT: {
			this.load(reg);
			mv.visitFieldInsn(PUTSTATIC, field.getOwner(), field.getName(), field.getType());
		}
			break;
		case OP_SGET_OBJECT:// sget-object
		case OP_SGET:
		case OP_SGET_WIDE:
		case OP_SGET_BOOLEAN:
		case OP_SGET_BYTE:
		case OP_SGET_CHAR:
		case OP_SGET_SHORT: {
			mv.visitFieldInsn(GETSTATIC, field.getOwner(), field.getName(), field.getType());
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitVarInsn(int, int)
	 */
	public void visitVarInsn(int opcode, int reg) {
		switch (opcode) {
		case OP_MOVE_RESULT_OBJECT:// move-result-object
		case OP_MOVE_RESULT:
		case OP_MOVE_EXCEPTION:
		case OP_MOVE_RESULT_WIDE:
			//
		{
			this.store(reg);
		}
			break;
		case OP_THROW:// throw
		{
			this.load(reg);
			mv.visitInsn(ATHROW);
		}
			break;
		case OP_RETURN:
		case OP_RETURN_OBJECT:
		case OP_RETURN_WIDE:
			//
		{
			this.load(reg);
			mv.visitInsn(this.detectReturnOpcode());
		}
			break;
		case OP_MONITOR_ENTER: {
			this.load(reg);
			mv.visitInsn(MONITORENTER);
		}
			break;
		case OP_MONITOR_EXIT: {
			this.load(reg);
			mv.visitInsn(MONITOREXIT);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitEnd()
	 */
	public void visitEnd() {
		mv.visitLabel(labels[labels.length - 1]);
		mv.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visit(int, int, int)
	 */
	public void visit(int total_registers_size, int in_register_size, int instruction_size) {
		labels = new Label[instruction_size + 1];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new Label();
		}
		int[] map = new int[total_registers_size];
		this.map = map;
		if (total_registers_size > 1) {
			int n = total_registers_size - in_register_size;
			int p = n;
			int q = 0;
			if (!isStatic) {
				map[p++] = q++;
			}
			for (Type arg : parameters) {
				map[p++] = q++;
				if (Type.LONG_TYPE.equals(arg) || Type.DOUBLE_TYPE.equals(arg)) {
					map[p++] = -1;
				}
			}

			for (int i = 0; i < n; i++) {
				map[i] = q++;
			}
		}
		newInsTypes = new String[total_registers_size];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int,
	 * java.lang.String, int, int)
	 */
	public void visitArrayInsn(int opcode, String type, int saveToReg, int demReg) {
		switch (opcode) {
		case OP_NEW_ARRAY: {
			this.load(demReg);
			int shortType = Type.getType(type).getElementType().getSort();
			switch (shortType) {
			case Type.BOOLEAN:
				mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
				break;
			case Type.BYTE:
				mv.visitIntInsn(NEWARRAY, T_BYTE);
				break;
			case Type.CHAR:
				mv.visitIntInsn(NEWARRAY, T_CHAR);
				break;
			case Type.DOUBLE:
				mv.visitIntInsn(NEWARRAY, T_DOUBLE);
				break;
			case Type.FLOAT:
				mv.visitIntInsn(NEWARRAY, T_FLOAT);
				break;
			case Type.INT:
				mv.visitIntInsn(NEWARRAY, T_INT);
				break;
			case Type.OBJECT:
				mv.visitTypeInsn(ANEWARRAY, type);
				break;
			}
			this.store(saveToReg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitSwitchInsn(int,
	 * int[], int[])
	 */
	public void visitLookupSwitchInsn(int opcode, int reg, int defaultHandler, int[] cases, int[] labels) {

		switch (opcode) {
		case OP_SPARSE_SWITCH:
			Label[] ls = new Label[labels.length];
			for (int i = 0; i < labels.length; i++) {
				ls[i] = this.labels[this.now_offset + labels[i]];
			}
			this.load(reg);
			mv.visitLookupSwitchInsn(this.labels[this.now_offset + defaultHandler], cases, ls);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitLocalVariable(java.lang
	 * .String, java.lang.String, java.lang.String, int, int, int)
	 */
	public void visitLocalVariable(String name, String type, String signature, int start, int end, int reg) {
		mv.visitLocalVariable(name, type, signature, labels[start], labels[end], map[reg]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLineNumber(int,
	 * int)
	 */
	public void visitLineNumber(int line, int label) {
		mv.visitLineNumber(line, labels[label]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitTableSwitchInsn(int,
	 * int, int, int[])
	 */
	public void visitTableSwitchInsn(int opcode, int reg, int first_case, int last_case, int label, int[] labels) {
		switch (opcode) {
		case OP_PACKED_SWITCH:
			Label ls[] = new Label[labels.length];
			for (int i = 0; i < labels.length; i++) {
				ls[i] = this.labels[this.now_offset + labels[i]];
			}
			this.load(reg);
			mv.visitTableSwitchInsn(first_case, last_case, this.labels[this.now_offset + label], ls);
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFillArrayInsn(int,
	 * int, int, int, java.lang.Object[])
	 */
	public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {
		int op = detectAAStoreOpcode(reg);
		for (int i = 0; i < initLength; i++) {
			load(reg);
			mv.visitLdcInsn(i);
			mv.visitLdcInsn(values[i]);
			mv.visitInsn(op);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitIntInsn(int, int,
	 * int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg) {
		switch (opcode) {
		case OP_AND_INT_2ADDR:
		case OP_AND_LONG_2ADDR:
		case OP_OR_INT_2ADDR:
		case OP_OR_LONG_2ADDR:
		case OP_XOR_INT_2ADDR:
		case OP_XOR_LONG_2ADDR:

		case OP_SUB_DOUBLE_2ADDR:
		case OP_SUB_FLOAT_2ADDR:
		case OP_SUB_INT_2ADDR:
		case OP_SUB_LONG_2ADDR:
		case OP_MUL_LONG_2ADDR:
		case OP_MUL_INT_2ADDR:
		case OP_REM_LONG_2ADDR:
		case OP_REM_INT_2ADDR:
		case OP_DIV_INT_2ADDR:
		case OP_DIV_LONG_2ADDR:
		case OP_DIV_FLOAT_2ADDR:
		case OP_DIV_DOUBLE_2ADDR:
		case OP_ADD_INT_2ADDR:
		case OP_ADD_LONG_2ADDR:
		case OP_ADD_FLOAT_2ADDR:
		case OP_ADD_DOUBLE_2ADDR: {
			this.load(saveToReg);
			this.load(opReg);
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			this.store(saveToReg);
		}
			break;
		case OP_NEG_INT:
		case OP_NEG_DOUBLE:
		case OP_NEG_FLOAT:
		case OP_NEG_LONG:

		case OP_INT_TO_BYTE:
		case OP_INT_TO_SHORT:
		case OP_INT_TO_CHAR:
		case OP_INT_TO_FLOAT:
		case OP_INT_TO_DOUBLE:
		case OP_LONG_TO_DOUBLE:
		case OP_LONG_TO_FLOAT:
		case OP_LONG_TO_INT:
		case OP_DOUBLE_TO_FLOAT:
		case OP_DOUBLE_TO_INT:
		case OP_DOUBLE_TO_LONG:
		case OP_FLOAT_TO_INT:
		case OP_FLOAT_TO_LONG:
		case OP_FLOAT_TO_DOUBLE:
		case OP_ARRAY_LENGTH: {
			this.load(opReg);
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			this.store(saveToReg);
		}
			break;
		case OP_MOVE_OBJECT:
		case OP_MOVE_OBJECT_FROM16:
		case OP_MOVE:
		case OP_MOVE_WIDE:
		case OP_MOVE_FROM16:
		case OP_MOVE_WIDE_FROM16: {
			this.load(opReg);
			this.store(saveToReg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int)
	 */
	public void visitJumpInsn(int opcode, int offset) {
		Label label = getLabel(offset);
		switch (opcode) {
		case OP_GOTO:
		case OP_GOTO_16: {
			mv.visitJumpInsn(GOTO, label);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int,
	 * java.lang.String, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg) {
		switch (opcode) {
		case OP_NEW_INSTANCE:// new-instance
		{
			mv.visitTypeInsn(NEW, type);
			this.store(toReg);
			// newInsTypes[reg] = type;
		}
			break;
		case OP_CONST_CLASS:// const-class
		{
			mv.visitLdcInsn(Type.getType(type));
			this.store(toReg);
		}
			break;
		case OP_CHECK_CAST: {
			this.load(toReg);
			mv.visitTypeInsn(CHECKCAST, type);
			this.store(toReg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitFilledNewArrayIns(int,
	 * java.lang.String, int[])
	 */
	public void visitFilledNewArrayIns(int opcode, String type, int[] regs) {
		Type elem = Type.getType(type).getElementType();
		int shortType = elem.getSort();
		mv.visitLdcInsn(regs.length);
		switch (shortType) {
		case Type.BOOLEAN:
			mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
			break;
		case Type.BYTE:
			mv.visitIntInsn(NEWARRAY, T_BYTE);
			break;
		case Type.CHAR:
			mv.visitIntInsn(NEWARRAY, T_CHAR);
			break;
		case Type.DOUBLE:
			mv.visitIntInsn(NEWARRAY, T_DOUBLE);
			break;
		case Type.FLOAT:
			mv.visitIntInsn(NEWARRAY, T_FLOAT);
			break;
		case Type.INT:
			mv.visitIntInsn(NEWARRAY, T_INT);
			break;
		case Type.OBJECT:
			mv.visitTypeInsn(ANEWARRAY, type);
			break;
		}
		int store = elem.getOpcode(IASTORE);
		for (int i = 0; i < regs.length; i++) {
			mv.visitInsn(DUP);
			mv.visitLdcInsn(i);
			this.load(regs[i]);
			mv.visitInsn(store);
		}
	}
}
