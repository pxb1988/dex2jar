/**
 * 
 */
package pxb.android.dex2jar;

import org.objectweb.asm.Label;

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
		int debug_off = in.readIntx();
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
				in.readShortx();
				int type_id = in.readByte();
				int handler = in.readByte();
				String type = dexFile.getTypeItem(type_id);
				mv.visitTryCatchBlock(labels[start], labels[offset], labels[handler], type);
			}
			in.pop();
		}
		for (int i = 0; i < insns_size;) {
			mv.visitLabel(labels[i]);

			int opcode = in.readByte() & 0xff;
			switch (opcode) {
			case DexOpcodes.OP_INVOKE_VIRTUAL:// invoke-virtual
			case DexOpcodes.OP_INVOKE_DIRECT:// invoke-direct
			case DexOpcodes.OP_INVOKE_INTERFACE:// invoke-interface
			{
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
			case DexOpcodes.OP_CONST_CLASS:// const-class
			{
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
			case OP_MOVE_EXCEPTION:
			case OP_THROW:// throw
			case OP_RETURN_OBJECT:// return-object
			{
				int reg = in.readByte();
				mv.visitVarInsn(opcode, reg);
				i += 1;
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
			case OP_IF_EQ:
			case OP_IF_NE:
			case OP_IF_LT:
			case OP_IF_GE:
			case OP_IF_GT:
			case OP_IF_LE:
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
			case OP_MOVE_OBJECT: {
				int b = in.readByte();
				int to = b & 0xf;
				int from = (b >> 4) & 0xf;
				mv.visitMoveObject(opcode, from, to);
				i += 1;
			}
				break;
			case OP_GOTO: {
				int offset = in.readByte();
				mv.visitGotoInsn(opcode, labels[i + ((byte) offset)]);
				i += 1;
			}
				break;
			default:
				throw new RuntimeException("Not support yet!");
			}
		}
	}
}
