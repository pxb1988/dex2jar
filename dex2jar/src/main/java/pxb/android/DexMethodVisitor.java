/**
 * 
 */
package pxb.android;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexMethodVisitor implements Opcodes, DexOpcodes {
	MethodVisitor mv;
	int map[];

	/**
	 * @param mv
	 */
	public DexMethodVisitor(MethodVisitor mv) {
		super();
		this.mv = mv;
	}

	void visitEnd() {
	}

	/**
	 * <pre>
	 * 01234 567 
	 * 567 01234 
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

		for (int i = 0; i < n; i++) {
			map[i] = m + i;
		}
		for (int i = 1; i < m; i++) {
			map[i + n] = i;
		}
		return map;
	}

	/**
	 * 
	 * 
	 * @param reg_size
	 * @param ins_size
	 */
	void visit(boolean isStatic, int reg_size, int parameterSize) {
		if (isStatic) {
			map = RegisterMapGenerator(reg_size, reg_size - parameterSize);
		} else {
			map = RegisterMapGenerator(reg_size, reg_size - parameterSize - 1);
		}
	}

	int detectLoadOpcode(String type) {
		return Type.getType(type).getOpcode(ILOAD);
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectLoadOpcode(int reg) {
		return 0;
	}

	/**
	 * 
	 * @param reg
	 *            没有转换
	 * @return
	 */
	int detectStoreOpcode(int reg) {
		return 0;
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
		{
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
		default:
			throw new RuntimeException("Not support yet!");
		}
	}

	void visitInsn(int opcode) {
		switch (opcode) {
		case OP_RETURN_VOID: {
			mv.visitInsn(RETURN);
		}
			break;
		default:
			throw new RuntimeException("Not support yet!");
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
			mv.visitIntInsn(ASTORE, map[reg]);
		}
			break;
		case OP_CONST_CLASS:// const-class
		{
			mv.visitLdcInsn(ClassNameAdapter.x(type).replace('/', '.'));
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
			mv.visitVarInsn(ASTORE, map[reg]);
		}
			break;
		default:
			throw new RuntimeException("Not support yet!");
		}
	}

	/**
	 * @param opcode
	 * @param string
	 * @param reg
	 */
	public void visitLdcInsn(int opcode, String string, int reg) {
		switch (opcode) {
		case OP_CONST_STRING:// const-string
		{
			mv.visitLdcInsn(string);
			mv.visitVarInsn(ASTORE, map[reg]);
		}
			break;
		default:
			throw new RuntimeException("Not support yet!");
		}
	}

	/**
	 * @param opcode
	 * @param reg
	 */
	public void visitVarInsn(int opcode, int reg) {
		switch (opcode) {
		case OP_MOVE_RESULT_OBJECT:// move-result-object
		case OP_MOVE_EXCEPTION: {
			mv.visitVarInsn(ASTORE, map[reg]);
		}
			break;
		case OP_THROW:// throw
		{
			mv.visitVarInsn(ALOAD, map[reg]);
			mv.visitInsn(ATHROW);
		}
			break;
		case OP_RETURN_OBJECT:// return-object
		{
			mv.visitVarInsn(ALOAD, map[reg]);
			mv.visitInsn(ARETURN);
		}
			break;
		default:
			throw new RuntimeException("Not support yet!");
		}
	}

	void visitLabel(Label label) {
		mv.visitLabel(label);

	}

	private void load(int reg) {
		mv.visitVarInsn(this.detectLoadOpcode(reg), map[reg]);
	}

	private void store(int reg) {
		mv.visitVarInsn(this.detectStoreOpcode(reg), map[reg]);
	}

	void visitJumpInsn(int opcode, Label label, int reg) {
		switch (opcode) {
		case OP_IF_NEZ: {
			load(reg);
			if (this.detectLoadOpcode(reg) == ALOAD) {
				mv.visitJumpInsn(IFNONNULL, label);
			} else {
				mv.visitJumpInsn(IFNE, label);
			}
		}
			break;
		default:
			throw new RuntimeException("Not support yet!");
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
			throw new RuntimeException("Not support yet!");
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
			throw new RuntimeException("Not support yet!");
		}
	}

	/**
	 * @param opcode
	 * @param from
	 * @param to
	 */
	public void visitMoveObject(int opcode, int from, int to) {
		this.load(from);
		this.store(to);
	}

	/**
	 * @param opcode
	 * @param label
	 */
	public void visitGotoInsn(int opcode, Label label) {
		mv.visitJumpInsn(GOTO, label);
	}
}
