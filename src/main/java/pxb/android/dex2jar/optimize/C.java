/**
 * 
 */
package pxb.android.dex2jar.optimize;

import static pxb.android.dex2jar.optimize.Util.isRead;
import static pxb.android.dex2jar.optimize.Util.isWrite;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.optimize.c.CAnalyzer;
import pxb.android.dex2jar.optimize.c.CBasicValue;
import pxb.android.dex2jar.optimize.c.CFrame;
import pxb.android.dex2jar.optimize.c.DexInterpreter;

/**
 * @author Panxiaobo
 * 
 */
public class C implements MethodTransformer, Opcodes {
    final private Method m;

    public void transform(final MethodNode method) {

        // dump(method.instructions);
        DexInterpreter dx = new DexInterpreter();
        // 分析出每条指令的Loacl及Stack内数据的类型
        Analyzer a = new CAnalyzer(dx);
        try {
            a.analyze(Type.getType(m.getOwner()).getInternalName(), method);
        } catch (AnalyzerException e) {
            throw new RuntimeException("fail on " + m, e);
        }
        final Frame[] fs = a.getFrames();

        Map<Integer, Integer> remap = new HashMap<Integer, Integer>();
        int nIndex = 0;// new index
        int oIndex = 0;// old index

        if ((method.access & ACC_STATIC) == 0) {// not static
            remap.put(oIndex++, nIndex++); // this
        }
        for (Type t : Type.getArgumentTypes(method.desc)) {
            remap.put(oIndex++, nIndex++); // the arg
            if (t.getSize() > 1) { // long
                nIndex++;
            }
        }

        // 根据当前Stack或者之后的Stack类型值推测当前指令的内容
        for (int i = 0; i < fs.length; i++) {
            AbstractInsnNode node = method.instructions.get(i);
            if (fs[i] == null) {// should remove dead code?
                continue;
            }
            if (isRead(node)) {// XLOAD
                CFrame f = (CFrame) fs[i + 1];
                CBasicValue v = (CBasicValue) f.peek();
                Type t = v.getType();
                if (t != null) {
                    ((VarInsnNode) node).setOpcode(t.getOpcode(ILOAD));
                }
                Integer oldVar = Util.var(node);
                Integer nVar = remap.get(oldVar);
                if (nVar == null) {
                    nVar = nIndex++;
                    remap.put(oldVar, nVar);
                    if (t != null && t.getSize() > 1) {
                        nIndex++;
                    }
                }
                Util.var(node, nVar);
            } else if (isWrite(node)) {
                CFrame f = (CFrame) fs[i];// XSTORE
                CBasicValue v = (CBasicValue) f.peek();
                Type t = v.getType();
                if (t != null) {
                    ((VarInsnNode) node).setOpcode(t.getOpcode(ISTORE));
                }
                Integer oldVar = Util.var(node);
                Integer nVar = remap.get(oldVar);
                if (nVar == null) {
                    nVar = nIndex++;
                    remap.put(oldVar, nVar);
                    if (t != null && t.getSize() > 1) {
                        nIndex++;
                    }
                }
                Util.var(node, nVar);
            } else if (node.getOpcode() == LDC) { // LDC
                CFrame f = (CFrame) fs[i + 1];
                CBasicValue v = (CBasicValue) f.peek();
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
            } else if (node.getOpcode() == IFNE) { // IFNE
                CFrame f = (CFrame) fs[i];
                CBasicValue v = (CBasicValue) f.peek();
                Type t = v.getType();
                if (t != null && (t.getSort() == Type.ARRAY || t.getSort() == Type.OBJECT)) {
                    ((JumpInsnNode) node).setOpcode(IFNONNULL);
                }
            } else if (node.getOpcode() == IFEQ) { // IFEQ
                CFrame f = (CFrame) fs[i];
                CBasicValue v = (CBasicValue) f.peek();
                Type t = v.getType();
                if (t != null && (t.getSort() == Type.ARRAY || t.getSort() == Type.OBJECT)) {
                    ((JumpInsnNode) node).setOpcode(IFNULL);
                }
            }
        }
        // // remove dead code
        // AbstractInsnNode node = method.instructions.getFirst();
        // for (int i = 0; i < fs.length; i++) {
        // if (fs[i] == null) {
        // AbstractInsnNode p = node;
        // node = node.getNext();
        // method.instructions.remove(p);
        // } else {
        // node = node.getNext();
        // }
        // }
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
}
