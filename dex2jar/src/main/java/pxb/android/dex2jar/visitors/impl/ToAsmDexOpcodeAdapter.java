/**
 * 
 */
package pxb.android.dex2jar.visitors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexOpcodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexOpcodeAdapter implements DexOpcodeVisitor, DexOpcodes {
	DexCodeVisitor dcv;
	Dex dex;

	/**
	 * @param dex
	 * @param dcv2
	 */
	public ToAsmDexOpcodeAdapter(Dex dex, DexCodeVisitor dcv2) {
		this.dex = dex;
		this.dcv = dcv2;
	}

	private static final Logger log = LoggerFactory.getLogger(ToAsmDexOpcodeAdapter.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int)
	 */
	public void visit(int opcode, int arg1) {
		log.info(String.format("%02x%02x", opcode, arg1));
		switch (opcode) {
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int, int)
	 */
	public void visit(int opcode, int arg1, int arg2) {
		log.info(String.format("%02x%02x %04x", opcode, arg1, arg2));
		switch (opcode) {
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int, int,
	 * int)
	 */
	public void visit(int opcode, int arg1, int arg2, int arg3) {
		log.info(String.format("%02x%02x %04x %04x", opcode, arg1, arg2, arg3));
		switch (opcode) {
		case OP_INVOKE_VIRTUAL:// invoke-virtual
		case OP_INVOKE_DIRECT:// invoke-direct
		case OP_INVOKE_STATIC:
		case OP_INVOKE_INTERFACE:// invoke-interface
		case OP_INVOKE_SUPER: {
			int size = (arg1 >> 4) & 0xf;
			int method_idx = arg2;
			int params = arg3;
			dcv.visitMethodInsn(opcode, dex.getMethod(method_idx), buildMethodRegister(size, params));
		}
			break;
		default:
			throw new RuntimeException(String.format("Not support Opcode :[0x%04x] = %s", opcode, DexOpcodeDump.dump(opcode)));
		}
	}

	private static int[] buildMethodRegister(int size, int value) {
		int[] a = new int[size];
		for (int i = 0; i < size; i++) {
			a[i] = value & 0xf;
			value >>= 4;
		}
		return a;
	}
}
