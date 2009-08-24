/**
 * 
 */
package pxb.android.dex2jar.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.DataIn;
import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.impl.ToAsmDexOpcodeAdapter;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexCodeReader implements DexOpcodes {
	private Dex dex;
	private DataIn in;
	private static final Logger log = LoggerFactory.getLogger(DexCodeReader.class);

	/**
	 * @param dexReader
	 * @param in
	 */
	public DexCodeReader(Dex dex, DataIn in) {
		this.dex = dex;
		this.in = in;
	}

	/**
	 * @param dcv
	 */
	public void accept(DexCodeVisitor dcv) {
		DataIn in = this.in;
		int registers_size = in.readShortx();
		// int ins_size =
		in.readShortx();
		// int outs_size =
		in.readShortx();
		dcv.visitRegister(registers_size);
		int tries_size = in.readShortx();
		// int debug_off =
		in.readIntx();
		int insns_size = in.readIntx();
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
					String type = dex.getType(type_id);
					dcv.visitTryCatch(start, offset, handler, type);
				}
			}
			in.pop();
		}
		ToAsmDexOpcodeAdapter tadoa = new ToAsmDexOpcodeAdapter(dex, dcv);
		for (int i = 0; i < insns_size;) {
			int opcode = in.readByte() & 0xff;
			dcv.visitLabel(i);
			switch (opcode) {

			case OP_MOVE_RESULT_OBJECT:
			case OP_MOVE_EXCEPTION:
			case OP_MOVE_OBJECT:
			case OP_MOVE_RESULT:
			case OP_MOVE:
			case OP_MOVE_RESULT_WIDE:
			case OP_GOTO:

			case OP_RETURN_VOID:
			case OP_RETURN:
			case OP_RETURN_OBJECT:
			case OP_RETURN_WIDE:
			case OP_THROW:

			case OP_CONST_4:

			case OP_ARRAY_LENGTH:

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

			case OP_AND_INT_2ADDR:
			case OP_ADD_INT_2ADDR:
			case OP_MUL_DOUBLE_2ADDR:
			case OP_MUL_LONG_2ADDR:
			case OP_MUL_INT_2ADDR:
			case OP_MUL_FLOAT_2ADDR:
				//
			{
				int b = in.readByte();
				tadoa.visit(opcode, b);
				i += 1;
				break;
			}
			case OP_NEW_INSTANCE:
			case OP_NEW_ARRAY:
			case OP_MOVE_OBJECT_FROM16:
			case OP_MOVE_FROM16:
			case OP_CONST_STRING:
			case OP_CONST_CLASS:
			case OP_CONST_WIDE_16:
			case OP_CHECK_CAST:
			case OP_CONST_16:
			case OP_ADD_INT_LIT8:
			case OP_GOTO_16:

			case OP_IF_EQZ:
			case OP_IF_NEZ:
			case OP_IF_LTZ:
			case OP_IF_GEZ:
			case OP_IF_GTZ:
			case OP_IF_LEZ:
			case OP_IF_EQ:
			case OP_IF_NE:
			case OP_IF_LT:
			case OP_IF_GE:
			case OP_IF_GT:
			case OP_IF_LE:

			case OP_CMPL_FLOAT:
			case OP_CMPG_FLOAT:
			case OP_CMPL_DOUBLE:
			case OP_CMPG_DOUBLE:
			case OP_CMP_LONG:

			case OP_AGET:
			case OP_AGET_WIDE:
			case OP_AGET_OBJECT:
			case OP_AGET_BOOLEAN:
			case OP_AGET_BYTE:
			case OP_AGET_CHAR:
			case OP_AGET_SHORT:
			case OP_APUT:
			case OP_APUT_WIDE:
			case OP_APUT_OBJECT:
			case OP_APUT_BOOLEAN:
			case OP_APUT_BYTE:
			case OP_APUT_CHAR:
			case OP_APUT_SHORT:
			case OP_IGET:
			case OP_IGET_WIDE:
			case OP_IGET_OBJECT:
			case OP_IGET_BOOLEAN:
			case OP_IGET_BYTE:
			case OP_IGET_CHAR:
			case OP_IGET_SHORT:
			case OP_IPUT:
			case OP_IPUT_WIDE:
			case OP_IPUT_OBJECT:
			case OP_IPUT_BOOLEAN:
			case OP_IPUT_BYTE:
			case OP_IPUT_CHAR:
			case OP_IPUT_SHORT:
			case OP_SGET:
			case OP_SGET_WIDE:
			case OP_SGET_OBJECT:
			case OP_SGET_BOOLEAN:
			case OP_SGET_BYTE:
			case OP_SGET_CHAR:
			case OP_SGET_SHORT:
			case OP_SPUT:
			case OP_SPUT_WIDE:
			case OP_SPUT_OBJECT:
			case OP_SPUT_BOOLEAN:
			case OP_SPUT_BYTE:
			case OP_SPUT_CHAR:
			case OP_SPUT_SHORT:

			case OP_DIV_DOUBLE:
			case OP_DIV_INT:
			case OP_DIV_FLOAT:
			case OP_DIV_LONG:

				//
			{
				int b = in.readByte();
				int c = in.readShortx();
				tadoa.visit(opcode, b, c);
				i += 2;
				break;
			}
			case OP_INVOKE_VIRTUAL:
			case OP_INVOKE_DIRECT:
			case OP_INVOKE_STATIC:
			case OP_INVOKE_INTERFACE:
			case OP_INVOKE_SUPER:
			case OP_CONST:
				//
			{
				int b = in.readByte();
				int c = in.readShortx();
				int d = in.readShortx();
				tadoa.visit(opcode, b, c, d);
				i += 3;
				break;
			}
			default:
				throw new RuntimeException("Not support Opcode :[0x" + Integer.toHexString(opcode) + "]");

			}
		}
	}
}
