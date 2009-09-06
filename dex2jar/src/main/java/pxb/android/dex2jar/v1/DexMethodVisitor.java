/**
 * 
 */
package pxb.android.dex2jar.v1;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.asm.PMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexMethodVisitor implements Opcodes, DexOpcodes {
	private PMethodVisitor pmv;
	private MethodVisitor mv;
	private Method method;
	private int map[];
	private int method_access_flags;

	private boolean isStatic() {
		return 0 != (method_access_flags & ACC_STATIC);
	}

	/**
	 * @param mv
	 */
	public DexMethodVisitor(MethodVisitor mv, Method method, int method_access_flags) {
		super();
		this.method = method;
		this.method_access_flags = method_access_flags;
		// mv = new AnalyzerAdapter(x(method.getOwner()), method_access_flags,
		// method.getName(), method.getType().getDesc(), mv);
		pmv = new PMethodVisitor(mv);
		pmv.visit(Type.getType(method.getOwner()), method.getType().getDesc(), this.isStatic());
		this.mv = new MethodNameAdapter(pmv);
	}

	void visitEnd() {
		// PMethodVisitor will count
		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}

	/**
	 * <pre>
	 * 012 34 567 
	 * 567 34 012 
	 * reg=8 
	 * m=5 
	 * n=3
	 * </pre>
	 * 
	 * @param total
	 * @param m
	 * @return
	 */
	public static int[] RegisterMapGenerator(int total, int m) {
		int[] map = new int[total];
		int n = total - m;
		for (int i = n; i < m; i++) {
			map[i] = i;
		}
		for (int i = 0; i < n; i++) {
			map[m + i] = i;
			map[i] = m + i;
		}

		return map;
	}

	int reg_size;

	/**
	 * 
	 * 
	 * @param reg_size
	 * @param ins_size
	 */
	void visit(int reg_size) {
		int parameterSize = method.getType().getParameterTypes().length;
		if (isStatic()) {
			map = RegisterMapGenerator(reg_size, reg_size - parameterSize);
		} else {
			map = RegisterMapGenerator(reg_size, reg_size - parameterSize - 1);
		}
		this.reg_size = reg_size;
	}

	private int detectLoadOpcode(String type) {
		return Type.getType(type).getOpcode(ILOAD);
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectLoadOpcode(int reg) {
		Type type = pmv.getLocal(map[reg]);
		return type.getOpcode(ILOAD);
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectStoreOpcode() {
		return pmv.getStack(1).getOpcode(ISTORE);
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectReturnOpcode() {
		return pmv.getStack(1).getOpcode(IRETURN);
	}

	void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		mv.visitTryCatchBlock(start, end, handler, type);
	}

	int detectStoreOpcode(String type) {
		return Type.getType(type).getOpcode(ISTORE);
	}

	/**
	 * @param opcode
	 * @param method
	 * @param registers
	 */
	public void visitMethodIns(int opcode, Method method, int... registers) {
		String[] parms = method.getType().getParameterTypes();
		int i = 0;// register
		int j = 0;// parms
		if (registers.length == parms.length + 1) {
			mv.visitVarInsn(ALOAD, map[registers[0]]);
			i = 1;
		}
		for (; i < registers.length; i++, j++) {
			mv.visitVarInsn(this.detectLoadOpcode(parms[j]), map[registers[i]]);
		}
		switch (opcode) {
		case OP_INVOKE_DIRECT:// invoke-direct
		case OP_INVOKE_SUPER: {
			mv.visitMethodInsn(INVOKESPECIAL, method.getOwner(), method.getName(), method.getType().getDesc());
		}
			break;
		case OP_INVOKE_VIRTUAL:// invoke-virtual
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, method.getOwner(), method.getName(), method.getType().getDesc());
		}
			break;
		case OP_INVOKE_INTERFACE:// invoke-interface
		{
			mv.visitMethodInsn(INVOKEINTERFACE, method.getOwner(), method.getName(), method.getType().getDesc());
		}
			break;
		case OP_INVOKE_STATIC: {
			mv.visitMethodInsn(INVOKESTATIC, method.getOwner(), method.getName(), method.getType().getDesc());
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}
	}

	void visitInsn(int opcode) {
		switch (opcode) {
		case OP_RETURN_VOID: {
			mv.visitInsn(RETURN);
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}
	}

	/**
	 * @param opcode
	 * @param type
	 * @param reg
	 */
	public void visitTypeInsn(int opcode, String type, int reg) {
		switch (opcode) {
		case OP_NEW_INSTANCE:// new-instance
		{
			mv.visitTypeInsn(NEW, type);
			mv.visitVarInsn(ASTORE, map[reg]);
		}
			break;
		case OP_CONST_CLASS:// const-class
		{
			mv.visitLdcInsn(Type.getType(type));
			// mv.visitLdcInsn(ClassNameAdapter.x(type).replace('/', '.'));
			// mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName",
			// "(Ljava/lang/String;)Ljava/lang/Class;");
			//			
			mv.visitVarInsn(ASTORE, map[reg]);
		}
			break;
		case OP_CHECK_CAST: {
			this.load(reg);
			mv.visitTypeInsn(CHECKCAST, type);
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}
	}

	/**
	 * @param opcode
	 * @param string
	 * @param reg
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
		case OP_CONST_16: {
			mv.visitLdcInsn(value);
			this.store(reg);
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}
	}

	/**
	 * @param opcode
	 * @param reg
	 */
	public void visitVarInsn(int opcode, int reg) {
		switch (opcode) {
		case OP_MOVE_RESULT_OBJECT:// move-result-object
		case OP_MOVE_RESULT:
		case OP_MOVE_EXCEPTION: {
			this.store(reg);
		}
			break;
		case OP_THROW:// throw
		{
			mv.visitVarInsn(ALOAD, map[reg]);
			mv.visitInsn(ATHROW);
		}
			break;
		case OP_RETURN:
		case OP_RETURN_OBJECT:// return-object
		{
			this.load(reg);
			mv.visitInsn(this.detectReturnOpcode());
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}
	}

	void visitLabel(Label label) {
		mv.visitLabel(label);

	}

	private void load(int reg) {
		mv.visitVarInsn(this.detectLoadOpcode(reg), map[reg]);
	}

	private void store(int reg) {
		mv.visitVarInsn(this.detectStoreOpcode(), map[reg]);
	}

	void visitJumpInsn(int opcode, Label label, int reg) {
		load(reg);

		switch (opcode) {
		case OP_IF_NEZ: {
			if (this.detectLoadOpcode(reg) == ALOAD) {
				mv.visitJumpInsn(IFNONNULL, label);
			} else {
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(IF_ICMPEQ, label);
			}
		}
			break;
		case OP_IF_GTZ: {
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(IF_ICMPGT, label);
		}
			break;
		case OP_IF_LEZ: {
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(IF_ICMPLE, label);
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");
		}
	}

	void visitJumpInsn(int opcode, Label label, int reg1, int reg2) {
		this.load(reg1);
		this.load(reg2);
		switch (opcode) {
		case OP_IF_EQ: {
			mv.visitJumpInsn(IFEQ, label);
		}
			break;
		case OP_IF_NE: {
			mv.visitJumpInsn(IFNE, label);
		}
			break;
		case OP_IF_GE: {
			mv.visitJumpInsn(IFGE, label);
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");
		}
	}

	/**
	 * @param opcode
	 * @param field
	 * @param reg
	 */
	public void visitStaticFieldInsn(int opcode, Field field, int reg) {
		switch (opcode) {
		case OP_SPUT_OBJECT:// sput-object
		{
			mv.visitVarInsn(this.detectLoadOpcode(field.getType()), map[reg]);
			mv.visitFieldInsn(PUTSTATIC, field.getOwner(), field.getName(), field.getType());
		}
			break;
		case OP_SGET_OBJECT:// sget-object
		{
			mv.visitFieldInsn(GETSTATIC, field.getOwner(), field.getName(), field.getType());
			mv.visitVarInsn(this.detectStoreOpcode(field.getType()), map[reg]);
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}

	}

	/**
	 * @param opcode
	 * @param field
	 * @param owner_reg
	 * @param value_reg
	 */
	public void visitFieldInsn(int opcode, Field field, int owner_reg, int value_reg) {
		switch (opcode) {
		case OP_IGET:
		case OP_IGET_OBJECT: {
			mv.visitVarInsn(ALOAD, map[owner_reg]);
			mv.visitFieldInsn(GETFIELD, field.getOwner(), field.getName(), field.getType());
			mv.visitVarInsn(ISTORE, map[value_reg]);
		}
			break;

		case OP_IPUT:
		case OP_IPUT_OBJECT: {
			this.load(owner_reg);
			this.load(value_reg);
			mv.visitFieldInsn(PUTFIELD, field.getOwner(), field.getName(), field.getType());
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}
	}

	/**
	 * @param opcode
	 * @param fromReg
	 * @param toReg
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
		}
		this.store(to);
	}

	/**
	 * @param opcode
	 * @param label
	 */
	public void visitGotoInsn(int opcode, Label label) {
		mv.visitJumpInsn(GOTO, label);
	}

	/**
	 * [reg1]=[reg2]+value
	 * 
	 * @param opcode
	 * @param reg1
	 * @param reg2
	 * @param value
	 */
	public void visitAdd(int opcode, int reg1, int reg2, int value) {
		switch (opcode) {
		case OP_ADD_INT_LIT8: {
			this.load(reg2);
			mv.visitLdcInsn(value);
			mv.visitInsn(IADD);
			this.store(reg1);
		}
			break;
		default:
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");

		}
	}

	/**
	 * @param opcode
	 * @param arrayReg
	 * @param indexReg
	 * @param valueReg
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
			throw new RuntimeException("Not support Opcode:[" + Integer.toHexString(opcode) + "] yet!");
		}
	}
}
