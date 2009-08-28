/**
 * 
 */
package pxb.android.dex2jar.visitors.impl;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.optimize.LdcOptimizeAdapter;
import pxb.android.dex2jar.optimize.NewOptimizeAdapter;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.asm.PDescMethodVisitor;

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
	public ToAsmDexCodeAdapter(MethodVisitor mv, int access_flags, String owner, String name, String desc) {
		mv = new LdcOptimizeAdapter(mv);// 优化Ldc
		mv = new NewOptimizeAdapter(mv);// 优化New
		this.mv = new PDescMethodVisitor(mv);
		isStatic = 0 != (access_flags & ACC_STATIC);
		this.mv.visit(owner, desc, isStatic);
		parameters = Type.getArgumentTypes(desc);
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
		mv.visitTryCatchBlock(labels[start], labels[offset], labels[handler], type);
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
		for (String type : method.getType().getParameterTypes()) {
			this.load(registers[i++]);
			if ("D".equals(type) || "J".equals(type)) {
				i++;
			}
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
		if (method.getName().equals("<init>")) {
			int typeReg = registers[0];
			String type = newInsTypes[typeReg];
			if (type != null) {
				mv.visitTypeInsn(NEW, type);
				mv.visitInsn(DUP);
				int i = 1;
				for (String x : method.getType().getParameterTypes()) {
					this.load(registers[i++]);
					if ("D".equals(x) || "J".equals(x)) {
						i++;
					}
				}
				mv.visitMethodInsn(INVOKESPECIAL, method.getOwner(), method.getName(), method.getType().getDesc());
				this.store(typeReg);
				newInsTypes[typeReg] = null;
			} else {
				loadArgument(method, registers, false);
				mv.visitMethodInsn(INVOKESPECIAL, method.getOwner(), method.getName(), method.getType().getDesc());
			}
			return;
		}
		// ==========
		switch (opcode) {
		case OP_INVOKE_STATIC: {
			loadArgument(method, registers, true);
			mv.visitMethodInsn(INVOKESTATIC, method.getOwner(), method.getName(), method.getType().getDesc());
		}
			break;
		default:
			loadArgument(method, registers, false);
			switch (opcode) {
			case OP_INVOKE_DIRECT:
			case OP_INVOKE_SUPER: {
				mv.visitMethodInsn(INVOKESPECIAL, method.getOwner(), method.getName(), method.getType().getDesc());
			}
				break;
			case OP_INVOKE_VIRTUAL: {
				mv.visitMethodInsn(INVOKEVIRTUAL, method.getOwner(), method.getName(), method.getType().getDesc());
			}
				break;
			case OP_INVOKE_INTERFACE: {
				mv.visitMethodInsn(INVOKEINTERFACE, method.getOwner(), method.getName(), method.getType().getDesc());
			}
				break;

			default:
				throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));
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
	public void visitTypeInsn(int opcode, String type, int reg) {
		switch (opcode) {
		case OP_NEW_INSTANCE:// new-instance
		{
			mv.visitTypeInsn(NEW, type);
			this.store(reg);
			// newInsTypes[reg] = type;
		}
			break;
		case OP_CONST_CLASS:// const-class
		{
			mv.visitLdcInsn(Type.getType(type));
			this.store(reg);
		}
			break;
		case OP_CHECK_CAST: {
			this.load(reg);
			mv.visitTypeInsn(CHECKCAST, type);
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, int,
	 * int, int)
	 */
	public void visitArrayInsn(int opcode, int arrayReg, int indexReg, int valueReg) {
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
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int,
	 * pxb.android.dex2jar.Field, int, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int owner_reg, int value_reg) {
		switch (opcode) {
		case OP_IGET:
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
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

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
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitIntInsn(int, int,
	 * int, int)
	 */
	public void visitIntInsn(int opcode, int reg1, int reg2, int value) {
		switch (opcode) {
		case OP_ADD_INT_LIT8: {
			this.load(reg2);
			mv.visitLdcInsn(value);
			mv.visitInsn(IADD);
			this.store(reg1);
		}
			break;

		case OP_AND_INT_2ADDR: {
			this.load(reg1);
			this.load(reg2);
			mv.visitInsn(IAND);
			this.store(reg1);
		}
			break;
		case OP_AND_LONG_2ADDR: {
			this.load(reg1);
			this.load(reg2);
			mv.visitInsn(LAND);
			this.store(reg1);
		}
			break;
		case OP_ADD_LONG_2ADDR: {
			this.load(reg1);
			this.load(reg2);
			mv.visitInsn(LADD);
			this.store(reg1);
		}
			break;
		case OP_ADD_INT_2ADDR: {
			this.load(reg1);
			this.load(reg2);
			mv.visitInsn(IADD);
			this.store(reg1);
		}
			break;
		case OP_MUL_LONG_2ADDR: {
			this.load(reg1);
			this.load(reg2);
			mv.visitInsn(LMUL);
			this.store(reg1);
		}
			break;
		case OP_CMP_LONG: {
			this.load(reg1);
			this.load(reg2);
			mv.visitInsn(LSUB);
			this.store(value);
		}
			break;
		case OP_DIV_LONG: {
			this.load(reg1);
			this.load(reg2);
			mv.visitInsn(LDIV);
			this.store(value);
		}
			break;

		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

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
		if (reg > -1)
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
		case OP_GOTO:
		case OP_GOTO_16: {
			mv.visitJumpInsn(GOTO, label);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));
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
		switch (opcode) {
		case OP_IF_EQ: {
			mv.visitJumpInsn(IF_ICMPEQ, label);
		}
			break;
		case OP_IF_NE: {
			mv.visitJumpInsn(IF_ICMPNE, label);
		}
			break;
		case OP_IF_GE: {
			mv.visitJumpInsn(IF_ICMPGE, label);
		}
			break;
		case OP_IF_LE: {
			mv.visitJumpInsn(IF_ICMPLE, label);
		}
			break;
		case OP_IF_LT: {
			mv.visitJumpInsn(IF_ICMPLT, label);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));
		}
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
		case OP_CONST_WIDE_16:
		case OP_CONST: {
			mv.visitLdcInsn(value);
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitMoveInsn(int, int,
	 * int)
	 */
	public void visitMoveInsn(int opcode, int from, int to) {
		this.load(from);
		switch (opcode) {
		case OP_MOVE_OBJECT:
		case OP_MOVE: {
			// do nothing
		}
			break;
		case OP_INT_TO_BYTE: {
			mv.visitInsn(I2B);
		}
			break;
		case OP_INT_TO_SHORT: {
			mv.visitInsn(I2S);
		}
			break;
		case OP_INT_TO_CHAR: {
			mv.visitInsn(I2C);
		}
			break;
		case OP_INT_TO_FLOAT: {
			mv.visitInsn(I2F);
		}
			break;
		case OP_INT_TO_LONG: {
			mv.visitInsn(I2L);
		}
			break;
		case OP_INT_TO_DOUBLE: {
			mv.visitInsn(I2D);
		}
			break;
		case OP_LONG_TO_DOUBLE: {
			mv.visitInsn(L2D);
		}
			break;
		case OP_LONG_TO_FLOAT: {
			mv.visitInsn(L2F);
		}
			break;
		case OP_LONG_TO_INT: {
			mv.visitInsn(L2I);
		}
			break;
		case OP_DOUBLE_TO_FLOAT: {
			mv.visitInsn(D2F);
		}
			break;
		case OP_DOUBLE_TO_INT: {
			mv.visitInsn(D2I);
		}
			break;
		case OP_DOUBLE_TO_LONG: {
			mv.visitInsn(D2L);
		}
			break;
		case OP_FLOAT_TO_INT: {
			mv.visitInsn(F2I);
		}
			break;
		case OP_FLOAT_TO_LONG: {
			mv.visitInsn(F2L);
		}
			break;
		case OP_FLOAT_TO_DOUBLE: {
			mv.visitInsn(F2D);
		}
			break;
		case OP_ARRAY_LENGTH: {
			mv.visitInsn(ARRAYLENGTH);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

		}
		this.store(to);
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
		case OP_SPUT_OBJECT:// sput-object
		{
			this.load(reg);
			mv.visitFieldInsn(PUTSTATIC, field.getOwner(), field.getName(), field.getType());
		}
			break;
		case OP_SGET_OBJECT:// sget-object
		{
			mv.visitFieldInsn(GETSTATIC, field.getOwner(), field.getName(), field.getType());
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

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
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitEnd()
	 */
	public void visitEnd() {
		mv.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visit(int, int, int)
	 */
	public void visit(int total_registers_size, int in_register_size, int instruction_size) {
		labels = new Label[instruction_size];
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
			mv.visitTypeInsn(ANEWARRAY, type);
			this.store(saveToReg);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump.dump(opcode)));

		}
	}
}
