/**
 * 
 */
package pxb.android.dex2jar.asm;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
@SuppressWarnings("serial")
public class PDescMethodVisitor extends MethodAdapter implements Opcodes {

	private Map<Label, Type> handlers = new HashMap<Label, Type>();
	private Map<Integer, Type> _local = new HashMap<Integer, Type>();
	private Stack<Type> stack = new Stack<Type>() {

		@Override
		public Type push(Type item) {
			if (this.size() + 1 > maxStack) {
				maxStack = this.size() + 1;
			}
			return super.push(item);
		}
	};
	int maxLocalId = 0;
	int maxStack = 0;

	public void putLocal(int id, Type t) {
		if (id > maxLocalId)
			maxLocalId = id;
		_local.put(id, t);
	}

	public PDescMethodVisitor(MethodVisitor mv) {
		super(mv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.asm.MethodAdapter#visitTryCatchBlock(org.objectweb.asm.
	 * Label, org.objectweb.asm.Label, org.objectweb.asm.Label,
	 * java.lang.String)
	 */
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		super.visitTryCatchBlock(start, end, handler, type);
		this.handlers.put(handler, type == null ? Type.getType("Ljava/lang/Throwable;") : Type.getType(type));
	}

	public void visit(String owner, String des, boolean isStatic) {
		Type args[] = Type.getArgumentTypes(des);
		if (isStatic) {
			for (int i = 0; i < args.length; i++) {
				putLocal(i, (args[i]));
			}
		} else {
			putLocal(0, Type.getType(owner));
			for (int i = 1; i <= args.length; i++) {
				putLocal(i, (args[i - 1]));
			}
		}
	}

	private static final Logger log = LoggerFactory.getLogger(PDescMethodVisitor.class);

	private void e(Type actual, Type expect) {
		if (expect != null && expect.equals(actual)) {
			//
		} else {
			log.warn("Expect :{} but :{}", expect, actual);
		}

	}

	public Type getLocal(int i) {
		return _local.get(i);
	}

	/**
	 *1开始
	 * 
	 * @param i
	 * @return
	 */
	public Type getStack(int i) {
		return stack.get(stack.size() - i);
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		super.visitFieldInsn(opcode, owner, name, desc);
		switch (opcode) {
		case Opcodes.GETFIELD:
			e((Type) stack.pop(), Type.getType(owner));
			stack.push(Type.getType(desc));
			break;
		case Opcodes.PUTFIELD:
			e((Type) stack.pop(), Type.getType(desc));
			e((Type) stack.pop(), Type.getType(owner));
			break;
		case Opcodes.GETSTATIC:
			stack.push(Type.getType(desc));
			break;
		case Opcodes.PUTSTATIC:
			e((Type) stack.pop(), Type.getType(desc));
			break;
		}
	}

	public void visitInsn(int opcode) {
		super.visitInsn(opcode);
		switch (opcode) {
		case ACONST_NULL:
			stack.push(Type.VOID_TYPE);
			break;
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
		case LCONST_0:
			stack.push(Type.INT_TYPE);
			break;
		case LCONST_1:
			stack.push(Type.LONG_TYPE);
			break;
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			stack.push(Type.FLOAT_TYPE);
			break;
		case DCONST_0:
		case DCONST_1:
			stack.push(Type.DOUBLE_TYPE);
			break;
		case IALOAD:
		case LALOAD:
		case FALOAD:
		case DALOAD:
		case BALOAD:
		case CALOAD:
		case AALOAD:
		case SALOAD:
			stack.pop();
			Type base = (Type) stack.pop();
			stack.push(base.getElementType());
			break;
		case IASTORE:
		case LASTORE:
		case FASTORE:
		case DASTORE:
		case AASTORE:
		case BASTORE:
		case CASTORE:
		case SASTORE:

			stack.pop();
			stack.pop();
			stack.pop();
			break;
		case POP:
			stack.pop();
			break;
		case DUP:
			stack.push(stack.peek());
			break;
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
		case ARETURN:
		case ATHROW:
			stack.pop();
			break;
		case ARRAYLENGTH:
			stack.pop();
			stack.push(Type.INT_TYPE);
			break;
		case IADD:
		case LADD:
		case FADD:
		case DADD:
		case ISUB:
		case LSUB:
		case FSUB:
		case DSUB:
		case IMUL:
		case LMUL:
		case FMUL:
		case DMUL:
		case IDIV:
		case LDIV:
		case FDIV:
		case DDIV:
		case IREM:
		case LREM:
		case FREM:
		case DREM:
		case ISHL:
		case LSHL:
		case ISHR:
		case LSHR:
		case IUSHR:
		case LUSHR:
		case IAND:
		case LAND:
		case IOR:
		case LOR:
		case IXOR:
		case LXOR:
		case MONITORENTER:
		case MONITOREXIT:
		case LCMP:
		case FCMPL:
		case FCMPG:
		case DCMPL:
		case DCMPG:
			stack.pop();
			break;

		case DUP_X1:
			Type a = stack.pop();
			Type b = stack.pop();
			stack.push(a);
			stack.push(b);
			stack.push(a);
			break;

		case I2L:
		case I2F:
		case I2D:
		case L2I:
		case L2F:
		case L2D:
		case F2I:
		case F2L:
		case F2D:
		case D2I:
		case D2L:
		case D2F:
		case I2B:
		case I2C:
		case I2S:
		case INEG:
		case LNEG:
		case FNEG:
		case DNEG:
		case RETURN:

			break;
		case POP2:
		case DUP_X2:
		case DUP2:
		case DUP2_X1:
		case DUP2_X2:
		case SWAP:
		default:
			throw new RuntimeException("");
		}
	}

	public void visitIntInsn(int opcode, int operand) {
		super.visitIntInsn(opcode, operand);
		switch (opcode) {
		case BIPUSH:
			stack.push(Type.BYTE_TYPE);
			break;
		case SIPUSH:
			stack.push(Type.SHORT_TYPE);
			break;
		case NEWARRAY:
			stack.pop();
			switch (operand) {
			case Opcodes.T_BOOLEAN:
				stack.push(Type.getType("[Z"));
				break;
			case Opcodes.T_BYTE:
				stack.push(Type.getType("[B"));
				break;
			case Opcodes.T_CHAR:
				stack.push(Type.getType("[C"));
				break;
			case Opcodes.T_DOUBLE:
				stack.push(Type.getType("[D"));
				break;
			case Opcodes.T_FLOAT:
				stack.push(Type.getType("[F"));
				break;
			case Opcodes.T_INT:
				stack.push(Type.getType("[I"));
				break;
			case Opcodes.T_LONG:
				stack.push(Type.getType("[J"));
				break;
			case Opcodes.T_SHORT:
				stack.push(Type.getType("[Z"));
				break;
			}
			break;
		}
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		super.visitJumpInsn(opcode, label);
		switch (opcode) {
		case IFEQ:
		case IFNE:
		case IFLT:
		case IFGE:
		case IFGT:
		case IFLE:
		case IFNONNULL:
		case IFNULL:
			stack.pop();
			break;
		case IF_ICMPEQ:
		case IF_ICMPNE:
		case IF_ICMPLT:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
		case IF_ACMPEQ:
		case IF_ACMPNE:
			stack.pop();
			stack.pop();
			break;
		case GOTO:
		case JSR:
		}
	}

	@Override
	public void visitLabel(Label label) {
		super.visitLabel(label);
		Type type = handlers.get(label);
		if (type != null) {
			stack.push(type);
		}
	}

	public void visitLdcInsn(Object cst) {
		if (cst instanceof String) {
			stack.push(Type.getType(String.class));
		} else if (cst instanceof Integer) {
			stack.push(Type.INT_TYPE);
		} else if (cst instanceof Float) {
			stack.push(Type.FLOAT_TYPE);
		} else if (cst instanceof Long) {
			stack.push(Type.LONG_TYPE);
		} else if (cst instanceof Double) {
			stack.push(Type.DOUBLE_TYPE);
		} else if (cst instanceof Type) {
			stack.push(Type.getType(Class.class));
		}
		super.visitLdcInsn(cst);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		super.visitLookupSwitchInsn(dflt, keys, labels);
		stack.pop();
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {

		Type args[] = Type.getArgumentTypes(desc);
		for (int i = args.length - 1; i >= 0; i--) {
			Type t = (Type) stack.pop();
			e(args[i], t);
		}
		if (opcode != Opcodes.INVOKESTATIC) {
			Type o = Type.getType(owner);
			Type p = (Type) stack.pop();
			e(p, o);
		}

		Type ret = Type.getReturnType(desc);
		if (!ret.equals(Type.VOID_TYPE)) {
			stack.push(ret);
		}
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		super.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		super.visitTableSwitchInsn(min, max, dflt, labels);
		stack.pop();
	}

	public void visitTypeInsn(int opcode, String type) {
		super.visitTypeInsn(opcode, type);
		switch (opcode) {
		case NEW:
			stack.push(Type.getType(type));
			break;
		case CHECKCAST:
			stack.pop();
			stack.push(Type.getType(type));
			break;
		case INSTANCEOF:
			stack.pop();
			stack.push(Type.BOOLEAN_TYPE);
			break;
		case ANEWARRAY:
			stack.pop();
			stack.push(Type.getType(type));
			// stack.push(Type.getObjectType("[L" + type + ";"));
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.MethodAdapter#visitMaxs(int, int)
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(this.maxStack, this.maxLocalId + 1);
	}

	public void visitVarInsn(int opcode, int var) {
		super.visitVarInsn(opcode, var);
		switch (opcode) {
		case ILOAD:
		case LLOAD:
		case FLOAD:
		case DLOAD:
		case ALOAD:
			stack.push(getLocal(var));
			break;
		case ISTORE:
		case LSTORE:
		case FSTORE:
		case DSTORE:
		case ASTORE:
			Type p = stack.pop();
			// if (local[var] == null)
			putLocal(var, p);
			break;
		case RET:
		}
	}
}
