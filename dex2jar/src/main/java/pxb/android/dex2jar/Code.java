/**
 * 
 */
package pxb.android.dex2jar;

import org.objectweb.asm.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Code implements DexOpcodes {
	DexFile dexFile;
	DataIn in;

	/**
	 * @param dexFile
	 * @param in
	 * @param method
	 * @param isStatic
	 */
	public Code(DexFile dexFile, DataIn in) {
		this.dexFile = dexFile;
		this.in = in;
	}

	private static int[] buildMethodRegister(int size, int value) {
		int[] a = new int[size];
		for (int i = 0; i < size; i++) {
			a[i] = value & 0xf;
			value >>= 4;
		}
		return a;
	}

	public String dumpOpcode(int opcode) {
		try {
			java.lang.reflect.Field fs[] = DexOpcodes.class.getDeclaredFields();
			for (java.lang.reflect.Field f : fs) {
				int value = f.getInt(null);
				if (value == opcode)
					return f.getName();
			}
		} catch (Exception e) {
		}
		return "UNKNOW";
	}

	private static final Logger log = LoggerFactory.getLogger(Code.class);

	/**
	 * @param mv
	 */
	public void accept(DexMethodVisitor mv) {
		DataIn in = this.in;
		int registers_size = in.readShortx();
		// int ins_size =
		in.readShortx();
		// int outs_size =
		in.readShortx();
		mv.visit(registers_size);
		int tries_size = in.readShortx();
		// int debug_off =
		in.readIntx();
		int insns_size = in.readIntx();
		Label labels[] = new Label[insns_size];
		for (int i = 0; i < insns_size; i++) {
			labels[i] = new Label();
		}
		{// try catch
			in.push();
			in.skip(insns_size * 2);
			for (int i = 0; i < tries_size; i++) {
				int start = in.readIntx();
				int offset = in.readShortx();
				// TODO
				in.readShortx();
				in.readByte();
				int size = in.readByte();
				for (int j = 0; j < size; j++) {
					int type_id = in.readByte();
					int handler = in.readByte();
					String type = dexFile.getTypeItem(type_id);
					mv.visitTryCatchBlock(labels[start], labels[offset], labels[handler], type);
				}
			}
			in.pop();
		}
		for (int i = 0; i < insns_size;) {
			mv.visitLabel(labels[i]);

			int opcode = in.readByte() & 0xff;
			log.debug("Opcode:0x{}={}", Integer.toHexString(opcode), dumpOpcode(opcode));
			switch (opcode) {
			case DexOpcodes.OP_INVOKE_VIRTUAL:// invoke-virtual
			case DexOpcodes.OP_INVOKE_DIRECT:// invoke-direct
			case DexOpcodes.OP_INVOKE_INTERFACE:// invoke-interface
			case DexOpcodes.OP_INVOKE_SUPER: {
				int size = (in.readByte() >> 4) & 0xf;
				int method_idx = in.readShortx();
				int params = in.readShortx();
				mv.visitMethodIns(opcode, dexFile.getMethod(method_idx), buildMethodRegister(size, params));
				i += 3;
			}
				break;
			case DexOpcodes.OP_RETURN_VOID:// return-void
			{
				in.readByte();
				mv.visitInsn(opcode);
				i += 1;
			}
				break;
			case DexOpcodes.OP_NEW_INSTANCE: // new-instance
			case DexOpcodes.OP_CONST_CLASS:// const-class\
			case DexOpcodes.OP_CHECK_CAST: {
				int reg = in.readByte();
				int type_idx = in.readShortx();
				String type = dexFile.getTypeItem(type_idx);
				mv.visitTypeInsn(opcode, type, reg);
				i += 2;
			}
				break;
			case DexOpcodes.OP_CONST_STRING:// const-string
			{
				int reg = in.readByte();
				int string_idx = in.readShortx();
				String string = dexFile.getStringItem(string_idx);
				mv.visitLdcInsn(opcode, string, reg);
				i += 2;
			}
				break;
			case OP_MOVE_RESULT_OBJECT:// move-result-object
			case OP_MOVE_RESULT:
			case OP_MOVE_EXCEPTION:
			case OP_THROW:// throw
			case OP_RETURN_OBJECT:// return-object
			case OP_RETURN: {
				int reg = in.readByte();
				mv.visitVarInsn(opcode, reg);
				i += 1;
			}
				break;
			case OP_CONST_4: {
				int b = in.readByte();
				int reg = b & 0xf;
				int value = (b >> 4) & 0xf;
				if (0 != (value & 0x8)) {
					value = -((Integer.reverse(value) & 0x7) + 1);
				}
				mv.visitLdcInsn(opcode, value, reg);
				i += 1;
			}
				break;
			case OP_CONST_16: {
				int to = in.readByte();
				int value = (short) in.readShortx();
				mv.visitLdcInsn(opcode, value, to);
				i += 2;
			}
				break;
			case OP_SPUT_OBJECT:// sput-object
			case OP_SGET_OBJECT:// sget-object
			{
				int reg = in.readByte();
				int field_idx = in.readShortx();
				Field field = dexFile.getField(field_idx);
				mv.visitStaticFieldInsn(opcode, field, reg);
				i += 2;
			}
				break;

			case OP_IF_EQZ:
			case OP_IF_NEZ:
			case OP_IF_LTZ:
			case OP_IF_GEZ:
			case OP_IF_GTZ:
			case OP_IF_LEZ: //
			{
				int reg = in.readByte();
				int offset = in.readShortx();
				mv.visitJumpInsn(opcode, labels[i + ((short) offset)], reg);
				i += 2;
			}
				break;
			case OP_IF_EQ:
			case OP_IF_NE:
			case OP_IF_LT:
			case OP_IF_GE:
			case OP_IF_GT:
			case OP_IF_LE: {
				int b = in.readByte();
				int reg1 = b & 0xf;
				int reg2 = (b >> 4) & 0xf;
				int offset = in.readShortx();
				mv.visitJumpInsn(opcode, labels[i + ((short) offset)], reg1, reg2);
				i += 2;
			}
				break;
			case OP_IGET_OBJECT:
			case OP_IPUT_OBJECT:
			case OP_IPUT:
			case OP_IGET://
			{
				int b = in.readByte();
				int value_reg = b & 0xf;
				int owner_reg = (b >> 4) & 0xf;
				int field_idx = in.readShortx();
				Field field = dexFile.getField(field_idx);
				mv.visitFieldInsn(opcode, field, owner_reg, value_reg);
				i += 2;
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
				int b = in.readByte();
				int to = b & 0xf;
				int from = (b >> 4) & 0xf;
				mv.visitMoveInsn(opcode, from, to);
				i += 1;
			}
				break;
			case OP_GOTO: {
				int offset = in.readByte();
				mv.visitGotoInsn(opcode, labels[i + ((byte) offset)]);
				i += 1;
			}
				break;
			case OP_ADD_INT_LIT8: {
				int reg1 = in.readByte();
				int reg2 = in.readByte();
				int value = (byte) in.readByte();
				mv.visitAdd(opcode, reg1, reg2, value);
				i += 2;
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
				int value = in.readByte();
				int array = in.readByte();
				int index = in.readByte();
				mv.visitArrayInsn(opcode, array, index, value);
				i += 2;
			}
				break;
			default:
				throw new RuntimeException("Not support Opcode:[0x" + Integer.toHexString(opcode) + "] yet!");
			}
		}
		mv.visitEnd();
	}
}
