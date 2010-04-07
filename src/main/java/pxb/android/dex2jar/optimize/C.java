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
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Frame;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value;

/**
 * @author Panxiaobo
 * 
 */
public class C implements MethodTransformer, Opcodes {
	Method m;

	public void transform(final MethodNode method) {

		dump(method.instructions);
		DexInterpreter dx = new DexInterpreter();
		Analyzer a = new Analyzer(dx);
		try {
			a.analyze(Type.getType(m.getOwner()).getInternalName(), method);
		} catch (AnalyzerException e) {
			throw new RuntimeException("fail on " + m, e);
		}
		final Frame[] fs = a.getFrames();

		Object o = new Object() {
			public String toString() {
				StringBuilder sb = new StringBuilder();
				InsnList il = method.instructions;
				int i = 0;
				for (AbstractInsnNode p = il.getFirst(); p != il.getLast(); p = p.getNext()) {
					sb.append(String.format("%-20s  %s", fs[i++].toString(), p.toString()));
				}
				return sb.toString();
			}
		};

		for (Map.Entry<Value, Set<AbstractInsnNode>> entry : dx.getM().entrySet()) {
			Value value = entry.getKey();
			if (value instanceof DexInterpreter.MayObject) {
				if (!(Type.INT_TYPE.equals(((DexInterpreter.MayObject) value).type))) {
					for (AbstractInsnNode insnNode : entry.getValue()) {
						replace(insnNode);
					}
				}
			}
		}

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
