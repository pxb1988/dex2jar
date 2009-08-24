/**
 * 
 */
package pxb.android.dex2jar.visitors.impl;

import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexOpcodeVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexOpcodeAdapter implements DexOpcodeVisitor {
	DexCodeVisitor dcv;

	/**
	 * @param dex
	 * @param dcv2
	 */
	public ToAsmDexOpcodeAdapter(Dex dex, DexCodeVisitor dcv2) {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int)
	 */
	public void visit(int opcode, int arg1) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int, int)
	 */
	public void visit(int opcode, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexOpcodeVisitor#visit(int, int, int,
	 * int)
	 */
	public void visit(int opcode, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

}
