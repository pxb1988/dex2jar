/**
 * 
 */
package pxb.android.dex2jar.optimize;

import static pxb.android.dex2jar.optimize.Util.isRead;
import static pxb.android.dex2jar.optimize.Util.isWrite;

import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceMethodVisitor;

import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.InsnList;
import pxb.android.dex2jar.org.objectweb.asm.tree.JumpInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.LdcInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.MethodNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.VarInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Analyzer;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.AnalyzerException;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicValue;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Frame;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value;

/**
 * @author Panxiaobo
 * 
 */
public class C implements MethodTransformer, Opcodes {
	Method m;

	public void transform(final MethodNode method) {

		//dump(method.instructions);
		DexInterpreter dx = new DexInterpreter();
		//分析出每条指令的Loacl及Stack内数据的类型
		Analyzer a = new Analyzer(dx);
		try {
			a.analyze(Type.getType(m.getOwner()).getInternalName(), method);
		} catch (AnalyzerException e) {
			throw new RuntimeException("fail on " + m, e);
		}
		final Frame[] fs = a.getFrames();

		// 根据当前Stack或者之后的Stack类型值推测当前指令的内容
		for (int i = 0; i < fs.length; i++) {
			AbstractInsnNode node = method.instructions.get(i);
			if (isRead(node)) {//XLOAD
				Frame f = fs[i + 1];
				BasicValue v = (BasicValue) f.peek();
				Type t = v.getType();
				if (t != null) {
					((VarInsnNode) node).setOpcode(t.getOpcode(ILOAD));
				}
			} else if (isWrite(node)) {
				Frame f = fs[i];//XSTORE
				BasicValue v = (BasicValue) f.peek();
				Type t = v.getType();
				if (t != null) {
					((VarInsnNode) node).setOpcode(t.getOpcode(ISTORE));
				}
			} else if (node.getOpcode() == LDC) { //LDC
				Frame f = fs[i + 1];
				BasicValue v = (BasicValue) f.peek();
				Type t = v.getType();
				if (t != null) {
					LdcInsnNode ldcInsnNode = ((LdcInsnNode) node);

					if (ldcInsnNode.cst instanceof Number) {
						switch (t.getSort()) {
						case Type.VOID:
							break;
						case Type.BOOLEAN:
						case Type.BYTE: {
							int iValue = ((Number) ldcInsnNode.cst).intValue();
							ldcInsnNode.cst = (short) iValue;
						}
							break;
						case Type.CHAR: {
							int iValue = ((Number) ldcInsnNode.cst).intValue();
							ldcInsnNode.cst = (char) iValue;
						}
							break;
						case Type.SHORT: {
							int iValue = ((Number) ldcInsnNode.cst).intValue();
							ldcInsnNode.cst = (short) iValue;
						}
							break;
						case Type.INT:
						case Type.LONG:
							break;
						case Type.FLOAT: {
							int iValue = ((Number) ldcInsnNode.cst).intValue();
							ldcInsnNode.cst = Float.intBitsToFloat(iValue);
						}
							break;

						case Type.DOUBLE: {
							long iValue = ((Number) ldcInsnNode.cst).longValue();
							ldcInsnNode.cst = Double.longBitsToDouble(iValue);
						}
							break;
						default:
							ldcInsnNode.cst = null;
						}
					}
				}
			} else if (node.getOpcode() == IFNE) { //IFNE
				Frame f = fs[i];
				BasicValue v = (BasicValue) f.peek();
				Type t = v.getType();
				if (t != null && (t.getSort() == Type.ARRAY || t.getSort() == Type.OBJECT)) {
					((JumpInsnNode) node).setOpcode(IFNONNULL);
				}
			}else if (node.getOpcode() == IFEQ) { //IFEQ
				Frame f = fs[i];
				BasicValue v = (BasicValue) f.peek();
				Type t = v.getType();
				if (t != null && (t.getSort() == Type.ARRAY || t.getSort() == Type.OBJECT)) {
					((JumpInsnNode) node).setOpcode(IFNULL);
				}
			}
		}

		// Object o = new Object() {
		// public String toString() {
		// StringBuilder sb = new StringBuilder();
		// InsnList il = method.instructions;
		// int i = 0;
		// int max = 0;
		// boolean contain = false;
		// for (AbstractInsnNode p = il.getFirst(); p != il.getLast(); p =
		// p.getNext()) {
		// String s = fs[i++].toString();
		// int x = s.length();
		// if (x > max) {
		// max = x;
		// }
		// if (!contain && s.contains("X")) {
		// contain = true;
		// }
		// }
		//
		// TraceMethodVisitor tr = new TraceMethodVisitor();
		// tr.text.clear();
		// il.accept(tr);
		// i = 0;
		// for (AbstractInsnNode p = il.getFirst(); p != il.getLast(); p =
		// p.getNext()) {
		// sb.append(String.format("%04d |%-" + max + "s|%s", i,
		// fs[i].toString(max), tr.text.get(i++)));
		// }
		// if (contain) {
		// return "!!!!!!!\n" + sb;
		// } else {
		// return sb.toString();
		// }
		// }
		// };
	}

	/**
	 * @param tr
	 */
	public C(Method m) {
		this.m = m;
	}

	public void dump(InsnList insnList) {
		TraceMethodVisitor tr = new TraceMethodVisitor();
		tr.text.clear();
		insnList.accept(tr);
		int i = 0;
		for (Object o : tr.text) {
			System.out.print((i++) + " " + o);
		}
		tr.text.clear();
	}

	public static void replace(AbstractInsnNode node) {
		if (isRead(node)) {
			((VarInsnNode) node).setOpcode(ALOAD);
		} else if (isWrite(node)) {
			((VarInsnNode) node).setOpcode(ASTORE);
		} else if (node.getOpcode() == IFEQ) {
			((JumpInsnNode) node).setOpcode(IFNULL);
		} else if (node.getOpcode() == IFNE) {
			((JumpInsnNode) node).setOpcode(IFNONNULL);
		} else if (node.getOpcode() == LDC) {
			((LdcInsnNode) node).cst = null;
		}
	}

}
