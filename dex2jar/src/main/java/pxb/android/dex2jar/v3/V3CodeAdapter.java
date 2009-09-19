/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodeUtil;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3CodeAdapter implements DexCodeVisitor, Opcodes, DexOpcodes {
	protected Method method;
	protected MethodVisitor mv;
	int _regcount = 0;
	Map<Integer, Integer> map = new HashMap<Integer, Integer>();

	private int map(int reg) {
		Integer integer = map.get(reg);
		if (integer == null) {
			integer = _regcount++;
			map.put(reg, integer);
		}
		return integer;
	}

	/**
	 * @param method
	 * @param mv
	 */
	public V3CodeAdapter(Method method, MethodVisitor mv) {
		super();
		this.method = method;
		this.mv = mv;
	}

	public void visitInitLocal(int... args) {
		for (int i : args) {
			map(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, int,
	 * int, int)
	 */
	public void visitArrayInsn(int opcode, int regFromOrTo, int array, int index) {
		switch (opcode) {
		case OP_APUT: // int
		case OP_APUT_BOOLEAN:
		case OP_APUT_BYTE:
		case OP_APUT_CHAR:
		case OP_APUT_SHORT: {
			mv.visitVarInsn(ALOAD, map(array));
			mv.visitVarInsn(ILOAD, map(index));
			mv.visitVarInsn(ILOAD, map(regFromOrTo));
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
		}
			break;
		case OP_APUT_WIDE: // long
		{
			mv.visitVarInsn(ALOAD, map(array));
			mv.visitVarInsn(ILOAD, map(index));
			mv.visitVarInsn(LLOAD, map(regFromOrTo));
			mv.visitInsn(LASTORE);
		}
			break;
		case OP_APUT_OBJECT: {
			mv.visitVarInsn(ALOAD, map(array));
			mv.visitVarInsn(ILOAD, map(index));
			mv.visitVarInsn(ALOAD, map(regFromOrTo));
			mv.visitInsn(AASTORE);
		}
			break;
		case OP_AGET:
		case OP_AGET_BOOLEAN:
		case OP_AGET_BYTE:
		case OP_AGET_CHAR:
		case OP_AGET_SHORT: {
			mv.visitVarInsn(ALOAD, map(array));
			mv.visitVarInsn(ILOAD, map(index));
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			mv.visitVarInsn(ISTORE, map(regFromOrTo));
		}
			break;
		case OP_AGET_WIDE: {
			mv.visitVarInsn(ALOAD, map(array));
			mv.visitVarInsn(ILOAD, map(index));
			mv.visitInsn(LALOAD);
			mv.visitVarInsn(LSTORE, map(regFromOrTo));
		}
			break;
		case OP_AGET_OBJECT: {
			mv.visitVarInsn(ALOAD, map(array));
			mv.visitVarInsn(ILOAD, map(index));
			mv.visitInsn(AALOAD);
			mv.visitVarInsn(ASTORE, map(regFromOrTo));
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
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int,
	 * java.lang.String, int, int)
	 */
	public void visitArrayInsn(int opcode, String type, int saveToReg, int demReg) {
		switch (opcode) {
		case OP_NEW_ARRAY: {
			mv.visitVarInsn(ILOAD, map(demReg));
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
			mv.visitVarInsn(ASTORE, map(saveToReg));
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
		for (Map.Entry<Integer, Label> e : _labels.entrySet()) {
			if (e.getKey() > this.now_offset) {
				mv.visitLabel(e.getValue());
			}
		}
		mv.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int,
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
			switch (opcode) {
			case OP_SPUT_OBJECT:
				mv.visitVarInsn(ALOAD, map(reg));
				break;
			case OP_SPUT:
			case OP_SPUT_BOOLEAN:
			case OP_SPUT_BYTE:
			case OP_SPUT_CHAR:
			case OP_SPUT_SHORT:
				mv.visitVarInsn(ILOAD, map(reg));
				break;
			case OP_SPUT_WIDE:
				mv.visitVarInsn(LLOAD, map(reg));
				break;
			}
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
			switch (opcode) {
			case OP_SGET_OBJECT:// sget-object
				mv.visitVarInsn(ASTORE, map(reg));
				break;
			case OP_SGET_WIDE:
				mv.visitVarInsn(LSTORE, map(reg));
				break;
			case OP_SGET:
			case OP_SGET_BOOLEAN:
			case OP_SGET_BYTE:
			case OP_SGET_CHAR:
			case OP_SGET_SHORT:
				mv.visitVarInsn(ISTORE, map(reg));
				break;
			}
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
	public void visitFieldInsn(int opcode, Field field, int regFromOrTo, int ownerReg) {
		switch (opcode) {
		case OP_IGET:
		case OP_IGET_WIDE:
		case OP_IGET_OBJECT:
		case OP_IGET_BOOLEAN:
		case OP_IGET_BYTE:
		case OP_IGET_SHORT:
			//
		{
			mv.visitVarInsn(ALOAD, map(ownerReg));
			mv.visitFieldInsn(GETFIELD, field.getOwner(), field.getName(), field.getType());
			switch (opcode) {
			case OP_IGET:
			case OP_IGET_BOOLEAN:
			case OP_IGET_BYTE:
			case OP_IGET_SHORT:
				mv.visitVarInsn(ISTORE, map(regFromOrTo));
				break;
			case OP_IGET_WIDE:
				mv.visitVarInsn(LSTORE, map(regFromOrTo));
				break;
			case OP_IGET_OBJECT:
				mv.visitVarInsn(ASTORE, map(regFromOrTo));
				break;
			}
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
			mv.visitVarInsn(ALOAD, map(ownerReg));
			switch (opcode) {
			case OP_IPUT:
			case OP_IPUT_BOOLEAN:
			case OP_IPUT_BYTE:
			case OP_IPUT_SHORT:
				mv.visitVarInsn(ILOAD, map(regFromOrTo));
				break;
			case OP_IPUT_WIDE:
				mv.visitVarInsn(LLOAD, map(regFromOrTo));
				break;
			case OP_IPUT_OBJECT:
				mv.visitVarInsn(ALOAD, map(regFromOrTo));
				break;
			}
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
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFillArrayInsn(int,
	 * int, int, int, java.lang.Object[])
	 */
	public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {
		int op = 0;
		switch (elemWidth) {
		case 1:
			op = BASTORE;
			break;
		case 2:
			op = SASTORE;
			break;
		case 4:
			op = IASTORE;
			break;
		case 8:
			op = LASTORE;
			break;
		}

		for (int i = 0; i < initLength; i++) {
			mv.visitVarInsn(ALOAD, reg);
			mv.visitLdcInsn(i);
			mv.visitLdcInsn(values[i]);
			mv.visitInsn(op);
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
		int load = elem.getOpcode(ILOAD);
		for (int i = 0; i < regs.length; i++) {
			mv.visitInsn(DUP);
			mv.visitLdcInsn(i);
			mv.visitVarInsn(load, map(regs[i]));
			mv.visitInsn(store);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInInsn(int, int,
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
			int load = 0;
			int store = 0;
			switch (opcode) {
			case OP_AND_INT_2ADDR:
			case OP_SUB_INT_2ADDR:
			case OP_OR_INT_2ADDR:
			case OP_MUL_INT_2ADDR:
			case OP_XOR_INT_2ADDR:
			case OP_ADD_INT_2ADDR:
			case OP_REM_INT_2ADDR:
			case OP_DIV_INT_2ADDR: {
				load = ILOAD;
				store = ISTORE;
			}
				break;
			case OP_SUB_LONG_2ADDR:
			case OP_MUL_LONG_2ADDR:
			case OP_ADD_LONG_2ADDR:
			case OP_AND_LONG_2ADDR:
			case OP_OR_LONG_2ADDR:
			case OP_XOR_LONG_2ADDR:
			case OP_REM_LONG_2ADDR:
			case OP_DIV_LONG_2ADDR: {
				load = LLOAD;
				store = LSTORE;
			}
				break;
			case OP_SUB_FLOAT_2ADDR:
			case OP_ADD_FLOAT_2ADDR:
			case OP_DIV_FLOAT_2ADDR: {
				load = FLOAD;
				store = FSTORE;
			}
				break;
			case OP_DIV_DOUBLE_2ADDR:
			case OP_SUB_DOUBLE_2ADDR:
			case OP_ADD_DOUBLE_2ADDR: {
				load = DLOAD;
				store = DSTORE;
			}
				break;
			}
			mv.visitVarInsn(load, map(saveToReg));
			mv.visitVarInsn(load, map(opReg));
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			mv.visitVarInsn(store, map(saveToReg));
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
			int load = 0;
			int store = 0;
			switch (opcode) {
			case OP_NEG_INT: {
				load = ILOAD;
				store = ISTORE;
			}
				break;
			case OP_NEG_DOUBLE: {
				load = DLOAD;
				store = ISTORE;
			}
				break;
			case OP_NEG_FLOAT: {
				load = FLOAD;
				store = ISTORE;
			}
				break;
			case OP_NEG_LONG: {
				load = LLOAD;
				store = ISTORE;
			}
				break;
			case OP_INT_TO_BYTE: {
				load = ILOAD;
				store = ISTORE;
			}
				break;
			case OP_INT_TO_SHORT: {
				load = ILOAD;
				store = ISTORE;
			}
				break;
			case OP_INT_TO_CHAR: {
				load = ILOAD;
				store = ISTORE;
			}
				break;
			case OP_INT_TO_FLOAT: {
				load = ILOAD;
				store = FSTORE;
			}
				break;
			case OP_INT_TO_DOUBLE: {
				load = ILOAD;
				store = DSTORE;
			}
				break;
			case OP_LONG_TO_DOUBLE: {
				load = LLOAD;
				store = DSTORE;
			}
				break;
			case OP_LONG_TO_FLOAT: {
				load = LLOAD;
				store = FSTORE;
			}
				break;
			case OP_LONG_TO_INT: {
				load = LLOAD;
				store = ISTORE;
			}
				break;
			case OP_DOUBLE_TO_FLOAT: {
				load = DLOAD;
				store = FSTORE;
			}
				break;
			case OP_DOUBLE_TO_INT: {
				load = DLOAD;
				store = ISTORE;
			}
				break;
			case OP_DOUBLE_TO_LONG: {
				load = DLOAD;
				store = LSTORE;
			}
				break;
			case OP_FLOAT_TO_INT: {
				load = FLOAD;
				store = ISTORE;
			}
				break;
			case OP_FLOAT_TO_LONG: {
				load = FLOAD;
				store = LSTORE;
			}
				break;
			case OP_FLOAT_TO_DOUBLE: {
				load = FLOAD;
				store = DSTORE;
			}
				break;
			case OP_ARRAY_LENGTH: {
				load = ALOAD;
				store = ISTORE;
			}
				break;
			}
			mv.visitVarInsn(load, map(opReg));
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			mv.visitVarInsn(store, map(saveToReg));
		}
			break;
		case OP_MOVE_OBJECT:
		case OP_MOVE_OBJECT_FROM16:
		case OP_MOVE:
		case OP_MOVE_WIDE:
		case OP_MOVE_FROM16:
		case OP_MOVE_WIDE_FROM16: {
			int load = 0;
			int store = 0;
			switch (opcode) {
			case OP_MOVE_OBJECT:
			case OP_MOVE_OBJECT_FROM16: {
				load = ALOAD;
				store = ASTORE;
			}
				break;
			case OP_MOVE:
			case OP_MOVE_FROM16: {
				load = ILOAD;
				store = ISTORE;
			}
				break;
			case OP_MOVE_WIDE:
			case OP_MOVE_WIDE_FROM16: {
				load = LLOAD;
				store = LSTORE;
			}
				break;
			}
			mv.visitVarInsn(load, map(opReg));
			mv.visitVarInsn(store, map(saveToReg));
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
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInInsn(int, int,
	 * int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg) {
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
			mv.visitVarInsn(ILOAD, map(opReg));
			mv.visitLdcInsn(opValueOrReg);
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			mv.visitVarInsn(ISTORE, map(saveToReg));
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
			int load = 0;
			int store = 0;
			switch (opcode) {

			case OP_AND_INT:
			case OP_OR_INT:
			case OP_XOR_INT:
			case OP_SUB_INT:
			case OP_DIV_INT:
			case OP_MUL_INT:
			case OP_REM_INT:
			case OP_ADD_INT: {
				load = ILOAD;
				store = ISTORE;
			}
				break;

			case OP_OR_LONG:
			case OP_AND_LONG:
			case OP_XOR_LONG:
			case OP_ADD_LONG:
			case OP_SUB_LONG:
			case OP_MUL_LONG:
			case OP_CMP_LONG:
			case OP_REM_LONG:
			case OP_DIV_LONG: {
				load = ILOAD;
				store = ISTORE;
			}
				break;
			case OP_ADD_FLOAT:
			case OP_SUB_FLOAT:
			case OP_DIV_FLOAT:
			case OP_MUL_FLOAT:
			case OP_REM_FLOAT:
			case OP_CMPL_FLOAT: {
				load = FLOAD;
				store = FSTORE;
			}
				break;
			case OP_ADD_DOUBLE:
			case OP_SUB_DOUBLE:
			case OP_DIV_DOUBLE:
			case OP_MUL_DOUBLE:
			case OP_REM_DOUBLE:
			case OP_CMPL_DOUBLE: {
				load = DLOAD;
				store = DSTORE;
			}
				break;

			}
			mv.visitVarInsn(load, map(opReg));
			mv.visitVarInsn(load, map(opValueOrReg));
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			mv.visitVarInsn(store, map(saveToReg));
		}
			break;

		case OP_MUL_INT_LIT16:
		case OP_DIV_INT_LIT16:
		case OP_REM_INT_LIT16:
		case OP_ADD_INT_LIT16:
		case OP_AND_INT_LIT16:
		case OP_OR_INT_LIT16:
		case OP_XOR_INT_LIT16: {
			mv.visitVarInsn(ILOAD, map(opReg));
			mv.visitLdcInsn(opValueOrReg);
			mv.visitInsn(DexOpcodeUtil.mapOpcode(opcode));
			mv.visitVarInsn(ISTORE, map(saveToReg));
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

	int now_offset;

	protected Label getLabel(int offset) {
		return labels(now_offset + offset);
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
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int,
	 * int)
	 */
	public void visitJumpInsn(int opcode, int offset, int reg) {
		Label label = getLabel(offset);
		mv.visitVarInsn(ILOAD, map(reg));
		switch (opcode) {
		case OP_IF_NEZ: {
			mv.visitJumpInsn(IFNE, label);
		}
			break;
		case OP_IF_EQZ: {
			mv.visitJumpInsn(IFEQ, label);
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
		mv.visitVarInsn(ILOAD, map(reg2));
		mv.visitVarInsn(ILOAD, map(reg1));
		mv.visitJumpInsn(DexOpcodeUtil.mapOpcode(opcode), label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLabel(int)
	 */
	public void visitLabel(int index) {
		labels(index);
		this.now_offset = index;
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
			mv.visitVarInsn(ASTORE, map(reg));
		}
			break;
		case OP_CONST:
		case OP_CONST_4:
		case OP_CONST_16:
		case OP_CONST_HIGH16: {
			mv.visitLdcInsn(value);
			mv.visitVarInsn(ISTORE, map(reg));
		}
			break;
		case OP_CONST_WIDE:
		case OP_CONST_WIDE_16:
		case OP_CONST_WIDE_32:
		case OP_CONST_WIDE_HIGH16: {
			mv.visitLdcInsn(value);
			mv.visitVarInsn(LSTORE, map(reg));
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
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLineNumber(int,
	 * int)
	 */
	public void visitLineNumber(int line, int label) {
		mv.visitLineNumber(line, labels(label));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitLocalVariable(java.lang
	 * .String, java.lang.String, java.lang.String, int, int, int)
	 */
	public void visitLocalVariable(String name, String type, String signature, int start, int end, int reg) {
		mv.visitLocalVariable(name, type, signature, labels(start), labels(end), map(reg));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitLookupSwitchInsn(int,
	 * int, int, int[], int[])
	 */
	public void visitLookupSwitchInsn(int opcode, int reg, int defaultOffset, int[] cases, int[] offsets) {
		switch (opcode) {
		case OP_SPARSE_SWITCH:
			Label[] ls = new Label[offsets.length];
			for (int i = 0; i < ls.length; i++) {
				ls[i] = getLabel(offsets[i]);
			}
			mv.visitVarInsn(ILOAD, map(reg));
			mv.visitLookupSwitchInsn(getLabel(defaultOffset), cases, ls);
			break;
		}
	}

	public void loadArgument(Method method, int[] registers, boolean isStatic) {
		int i = 0;
		if (!isStatic) {
			mv.visitVarInsn(ALOAD, map(registers[i++]));
		}
		for (String s : method.getType().getParameterTypes()) {
			mv.visitVarInsn(Type.getType(s).getOpcode(ILOAD), map(registers[i++]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitMethodInsn(int,
	 * pxb.android.dex2jar.Method, int[])
	 */
	public void visitMethodInsn(int opcode, Method method, int[] args) {
		switch (opcode) {
		case OP_INVOKE_STATIC:
		case OP_INVOKE_STATIC_RANGE: {
			loadArgument(method, args, true);
			mv.visitMethodInsn(INVOKESTATIC, method.getOwner(), method.getName(), method.getType().getDesc());
		}
			break;
		default:
			loadArgument(method, args, false);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitTableSwitchInsn(int,
	 * int, int, int, int, int[])
	 */
	public void visitTableSwitchInsn(int opcode, int reg, int first_case, int last_case, int label, int[] labels) {
		switch (opcode) {
		case OP_PACKED_SWITCH:
			Label ls[] = new Label[labels.length];
			for (int i = 0; i < labels.length; i++) {
				ls[i] = getLabel(labels[i]);
			}
			mv.visitVarInsn(ILOAD, map(reg));
			mv.visitTableSwitchInsn(first_case, last_case, this.getLabel(label), ls);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTryCatch(int, int,
	 * int, java.lang.String)
	 */
	public void visitTryCatch(int start, int offset, int handler, String type) {
		mv.visitTryCatchBlock(labels(start), labels(start + offset), labels(handler), type);
	}

	protected Label labels(int i) {
		Label label = _labels.get(i);
		if (label == null) {
			label = new Label();
			_labels.put(i, label);
		}
		return label;
	}

	Map<Integer, Label> _labels = new HashMap<Integer, Label>();

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
			mv.visitVarInsn(ASTORE, map(toReg));
		}
			break;
		case OP_CONST_CLASS:// const-class
		{
			mv.visitLdcInsn(Type.getType(type));
			mv.visitVarInsn(ASTORE, map(toReg));
		}
			break;
		case OP_CHECK_CAST: {
			mv.visitVarInsn(ALOAD, map(toReg));
			mv.visitTypeInsn(CHECKCAST, type);
			mv.visitVarInsn(ASTORE, map(toReg));
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
	 * java.lang.String, int, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg, int fromReg) {
		switch (opcode) {
		case OP_INSTANCE_OF: {
			mv.visitVarInsn(ALOAD, map(fromReg));
			mv.visitTypeInsn(INSTANCEOF, type);
			mv.visitVarInsn(ASTORE, map(toReg));
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
			mv.visitVarInsn(ASTORE, map(reg));
		}
			break;
		case OP_THROW:// throw
		{
			mv.visitVarInsn(ALOAD, map(reg));
			mv.visitInsn(ATHROW);
		}
			break;
		case OP_RETURN: {
			mv.visitVarInsn(ILOAD, map(reg));
			mv.visitInsn(IRETURN);
		}
			break;
		case OP_RETURN_OBJECT: {
			mv.visitVarInsn(ALOAD, map(reg));
			mv.visitInsn(ARETURN);
		}
			break;
		case OP_RETURN_WIDE:
			//
		{
			mv.visitVarInsn(LLOAD, map(reg));
			mv.visitInsn(LRETURN);
		}
			break;
		case OP_MONITOR_ENTER: {
			mv.visitVarInsn(ALOAD, map(reg));
			mv.visitInsn(MONITORENTER);
		}
			break;
		case OP_MONITOR_EXIT: {
			mv.visitVarInsn(ALOAD, map(reg));
			mv.visitInsn(MONITOREXIT);
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode:[0x%04x]=%s yet!", opcode, DexOpcodeDump
					.dump(opcode)));
		}
	}

}
