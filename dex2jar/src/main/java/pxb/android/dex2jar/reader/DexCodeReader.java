/**
 * 
 */
package pxb.android.dex2jar.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.DataIn;
import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodeUtil;
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
		ToAsmDexOpcodeAdapter tadoa = new ToAsmDexOpcodeAdapter(dex, dcv);
		int total_registers_size = in.readShortx();
		int in_register_size = in.readShortx();
		// int outs_size =
		in.readShortx();

		int tries_size = in.readShortx();
		// int debug_off =
		in.readIntx();
		int instruction_size = in.readIntx();
		dcv.visit(total_registers_size, in_register_size, instruction_size);
		{// try catch
			in.push();
			in.skip(instruction_size * 2);
			for (int i = 0; i < tries_size; i++) {
				int start = in.readIntx();
				int offset = in.readShortx();
				// TODO
				int x = in.readShortx();
				int y = in.readByte();
				log.debug("Unknow x:{},y:{}", x, y);
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
				log.debug(String.format("%04x| %02x%02x %04x      %s", i, opcode, a, Short.reverseBytes(b), DexOpcodeDump.dump(opcode)));
				tadoa.visit(opcode, a, b);
				i += 2;
				break;
			}
			case 3: {
				int a = in.readByte();
				short b = in.readShortx();
				short c = in.readShortx();
				log.debug(String.format("%04x| %02x%02x %04x %04x %s", i, opcode, a, Short.reverseBytes(b), Short.reverseBytes(c), DexOpcodeDump.dump(opcode)));
				tadoa.visit(opcode, a, b, c);
				i += 3;
				break;
			}
			default:
				throw new RuntimeException(String.format("Not support Opcode :[0x%02x] @[0x%04x]", opcode, i));
			}
		}
		dcv.visitEnd();
	}
}
