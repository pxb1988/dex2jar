/**
 * 
 */
package pxb.android.dex2jar.reader;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.DataIn;
import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodeUtil;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexCodeReader implements DexOpcodes {
	private Dex dex;
	private DataIn in;
	private Method method;
	private static final Logger log = LoggerFactory.getLogger(DexCodeReader.class);

	/**
	 * @param dexReader
	 * @param in
	 */
	public DexCodeReader(Dex dex, DataIn in, Method method) {
		this.dex = dex;
		this.in = in;
		this.method = method;
	}

	/**
	 * @param dcv
	 */
	public void accept(DexCodeVisitor dcv) {
		DataIn in = this.in;
		DexOpcodeAdapter tadoa = new DexOpcodeAdapter(dex, dcv);
		int total_registers_size = in.readShortx();
		int in_register_size = in.readShortx();
		// int outs_size =
		in.readShortx();
		int tries_size = in.readShortx();
		int debug_off = in.readIntx();
		int instruction_size = in.readIntx();
//		// for v2
//		dcv.visit(total_registers_size, in_register_size, instruction_size);
		// for v3
		{
			int args[];
			int args_index;
			int i = total_registers_size - in_register_size;
			if ((method.getAccessFlags() & Opcodes.ACC_STATIC) == 0) {
				args = new int[method.getType().getParameterTypes().length + 1];
				args[0] = i++;
				args_index = 1;
			} else {
				args = new int[method.getType().getParameterTypes().length];
				args_index = 0;
			}
			for (String type : method.getType().getParameterTypes()) {
				args[args_index++] = i++;
				if ("D".equals(type) || "J".equals(type)) {
					i++;
				}
			}
			dcv.visitInitLocal(args);
		}
		if (tries_size > 0) {
			in.push();
			in.skip(instruction_size * 2);
			if (in.needPadding()) {
				in.skip(2);
			}
			for (int i = 0; i < tries_size; i++) {
				int start = in.readIntx();
				int offset = in.readShortx();
				int handlers = in.readShortx();
				in.push();
				in.skip((tries_size - i - 1) * 8 + handlers);
				boolean catchAll = false;
				int listSize = (int) in.readSignedLeb128();
				if (listSize <= 0) {
					listSize = -listSize;
					catchAll = true;
				}
				for (int k = 0; k < listSize; k++) {
					int type_id = (int) in.readUnsignedLeb128();
					int handler = (int) in.readUnsignedLeb128();
					String type = dex.getType(type_id);
					dcv.visitTryCatch(start, offset, handler, type);
				}
				if (catchAll) {
					int handler = (int) in.readUnsignedLeb128();
					dcv.visitTryCatch(start, offset, handler, null);
				}
				in.pop();
			}
			in.pop();
		}
		if (debug_off != 0) {
			// in.pushMove(debug_off);
			// new DexDebugInfoReader(in, dex,total_registers_size).accept(dcv);
			// in.pop();
		}

		for (int i = 0; i < instruction_size;) {
			int opcode = in.readByte() & 0xff;
			dcv.visitLabel(i);
			int size = DexOpcodeUtil.getSize(opcode);
			switch (size) {
			case 1: {
				int a = in.readByte();
				log.debug(String.format("%04x| %02x%02x           %s", i, opcode, a, DexOpcodeDump.dump(opcode)));
				tadoa.visit(opcode, a);
				i += 1;
				break;
			}
			case 2: {
				int a = in.readByte();
				short b = in.readShortx();
				log.debug(String.format("%04x| %02x%02x %04x      %s", i, opcode, a, Short.reverseBytes(b),
						DexOpcodeDump.dump(opcode)));
				tadoa.visit(opcode, a, b);
				i += 2;
				break;
			}
			case 3: {
				int a = in.readByte();
				short b = in.readShortx();
				short c = in.readShortx();
				log.debug(String.format("%04x| %02x%02x %04x %04x %s", i, opcode, a, Short.reverseBytes(b), Short
						.reverseBytes(c), DexOpcodeDump.dump(opcode)));
				tadoa.visit(opcode, a, b, c);
				i += 3;
				break;
			}
			case 0:// OP_NOP
				i = instruction_size;
				break;
			case -1: {
				int reg = in.readByte();
				int offset = in.readIntx();
				in.push();
				in.skip((offset - 3) * 2);
				switch (opcode) {
				case OP_SPARSE_SWITCH: {
					{
						in.readShortx();
						int switch_size = in.readShortx();
						int cases[] = new int[switch_size];
						int label[] = new int[switch_size];
						for (int j = 0; j < switch_size; j++) {
							cases[j] = in.readIntx();
						}
						for (int j = 0; j < switch_size; j++) {
							label[j] = in.readIntx();
						}
						dcv.visitLookupSwitchInsn(opcode, reg, 3, cases, label);
					}
				}
					break;
				case OP_PACKED_SWITCH: {
					{
						in.readShortx();
						int switch_size = in.readShortx();
						int first_case = in.readIntx();
						int last_case = first_case - 1 + switch_size;
						int labels[] = new int[switch_size];
						for (int j = 0; j < switch_size; j++) {
							int targetOffset = in.readIntx();
							labels[j] = targetOffset;
						}
						dcv.visitTableSwitchInsn(opcode, reg, first_case, last_case, 3, labels);
					}

				}
					break;
				case OP_FILL_ARRAY_DATA: {
					{
						in.readShortx();
						int elemWidth = in.readShortx();
						int initLength = in.readIntx();
						Object[] values = new Object[initLength];

						switch (elemWidth) {
						case 1:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readByte();
							}
							break;
						case 2:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readShortx();
							}
							break;
						case 4:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readIntx();
							}
							break;
						case 8:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readLongx();
							}
							break;
						}

						dcv.visitFillArrayInsn(opcode, reg, elemWidth, initLength, values);
					}

				}
					break;
				}
				in.pop();
				i += 3;
			}
				break;
			case -2: {
				int reg = in.readByte();
				long value = in.readLongx();
				dcv.visitLdcInsn(opcode, value, reg);
				i += 4;
			}
				break;
			default:
				throw new RuntimeException(String.format("Not support Opcode :0x%02x=%s @[0x%04x]", opcode,
						DexOpcodeDump.dump(opcode), i));
			}
		}
		dcv.visitEnd();
	}
}
