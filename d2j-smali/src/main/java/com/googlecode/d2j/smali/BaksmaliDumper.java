/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.d2j.smali;

import java.io.BufferedWriter;
import java.util.*;

import com.googlecode.d2j.*;
import com.googlecode.d2j.node.*;
import com.googlecode.d2j.node.DexAnnotationNode.Item;
import com.googlecode.d2j.node.insn.DexLabelStmtNode;
import com.googlecode.d2j.node.insn.DexStmtNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.util.Out;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;

public class BaksmaliDumper implements DexConstants {
    private static final int ACCESS_FIELD = 1 << 31;
    private final static String[] accessWords = new String[]{"public", "private", "protected", "static", "final",
            "synchronized", "bridge", "varargs", "native", "abstract", "strictfp", "synthetic", "constructor",
            "interface", "enum", "annotation", "volatile", "transient"};

    static {
        Arrays.sort(accessWords);
    }

    private final StringBuilder buff = new StringBuilder();
    private boolean useParameterRegisters = true;
    private boolean useLocals = false;

    public BaksmaliDumper() {
    }

    public BaksmaliDumper(boolean useParameterRegisters, boolean useLocals) {
        this.useParameterRegisters = useParameterRegisters;
        this.useLocals = useLocals;
    }

    private static boolean isAccessWords(String name) {
        return Arrays.binarySearch(accessWords, name) >= 0;
    }

    static void escape0(final StringBuilder buf, char c) {
        if (c == '\n') {
            buf.append("\\n");
        } else if (c == '\r') {
            buf.append("\\r");
        } else if (c == '\t') {
            buf.append("\\t");
        } else if (c == '\\') {
            buf.append("\\\\");
        } else if (c == '"') {
            buf.append("\\\"");
        } else if (c < 0x20 || c > 0x7f) {
            buf.append("\\u");
            if (c < 0x10) {
                buf.append("000");
            } else if (c < 0x100) {
                buf.append("00");
            } else if (c < 0x1000) {
                buf.append('0');
            }
            buf.append(Integer.toString(c, 16));
        } else {
            buf.append(c);
        }
    }

    static String escapeType(String id) {
        StringBuilder escapeBuff = new StringBuilder();
        escapeType0(escapeBuff, id);
        return escapeBuff.toString();
    }

    static void escapeId0(StringBuilder sb, String id) {
        for (int i = 0; i < id.length(); ++i) {
            char c = id.charAt(i);
            escape1(sb, c);
        }
    }

    static String escapeId(String id) {
        StringBuilder escapeBuff = new StringBuilder();
        escapeId0(escapeBuff, id);
        return escapeBuff.toString();
    }

    static void escape1(final StringBuilder buf, char c) {
        if (c == ' ' || c == '-' || c == ':' | c == '=' || c == ',' || c == '{' || c == '}' || c == '(' || c == ')') {
            buf.append(String.format("\\u%04x", (int) c));
        } else {
            escape0(buf, c);
        }
    }

    static void escapeType0(StringBuilder sb, String id) {
        for (int i = 0; i < id.length(); ++i) {
            char c = id.charAt(i);
            if (c == '-') {
                sb.append(c);
            } else {
                escape1(sb, c);
            }
        }
    }

    static String escapeMethodDesc(Method m) {
        StringBuilder escapeBuff = new StringBuilder();
        escapeBuff.append("(");
        for (String t : m.getParameterTypes()) {
            escapeType0(escapeBuff, t);
        }
        escapeBuff.append(")");
        escapeType0(escapeBuff, m.getReturnType());
        return escapeBuff.toString();
    }

    static void appendAccess(final int access, final StringBuilder sb) {
        if ((access & ACC_PUBLIC) != 0) {
            sb.append("public ");
        }
        if ((access & ACC_PRIVATE) != 0) {
            sb.append("private ");
        }
        if ((access & ACC_PROTECTED) != 0) {
            sb.append("protected ");
        }
        if ((access & ACC_FINAL) != 0) {
            sb.append("final ");
        }
        if ((access & ACC_STATIC) != 0) {
            sb.append("static ");
        }
        if ((access & ACC_VOLATILE) != 0) {
            if ((access & ACCESS_FIELD) == 0) {
                sb.append("bridge ");
            } else {
                sb.append("volatile ");
            }
        }
        if ((access & ACC_TRANSIENT) != 0) {
            if ((access & ACCESS_FIELD) == 0) {
                sb.append("varargs ");
            } else {
                sb.append("transient ");
            }
        }
        if ((access & ACC_NATIVE) != 0) {
            sb.append("native ");
        }
        if ((access & ACC_STRICT) != 0) {
            sb.append("strict ");
        }
        if ((access & ACC_INTERFACE) != 0) {
            sb.append("interface ");
        }
        if ((access & ACC_ABSTRACT) != 0) {
            sb.append("abstract ");
        }
        if ((access & ACC_SYNTHETIC) != 0) {
            sb.append("synthetic ");
        }
        if ((access & ACC_ANNOTATION) != 0) {
            sb.append("annotation ");
        }
        if ((access & ACC_ENUM) != 0) {
            sb.append("enum ");
        }
        if ((access & ACC_DECLARED_SYNCHRONIZED) != 0) {
            sb.append("declared-synchronized ");
        }
        if ((access & ACC_CONSTRUCTOR) != 0) {
            sb.append("constructor ");
        }
    }

    static void escape(final StringBuilder buf, final String s) {
        buf.append("\"");
        for (int i = 0; i < s.length(); ++i) {
            escape0(buf, s.charAt(i));
        }
        buf.append("\"");
    }

    static String escapeValue(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof String) {
            StringBuilder buf = new StringBuilder();
            escape(buf, (String) obj);
            return buf.toString();
        }

        if (obj instanceof DexType) {
            return escapeType(((DexType) obj).desc);
        }

        // if (obj instanceof Method) {
        // return v((Method) obj);
        // }
        if (obj instanceof Field) {
            Field f = ((Field) obj);
            String owner = f.getOwner();
            if (owner == null) {
                owner = f.getType();
            }
            return ".enum " + escapeType(owner) + "->" + f.getName() + ":" + escapeType(f.getType());
        }

        if (obj instanceof Integer) {
            int i = ((Integer) obj);
            if (i == Integer.MIN_VALUE) {
                return "0x" + Integer.toHexString(i);
            }
            return obj.toString();
        }
        if (obj instanceof Long) {
            Long v = ((Long) obj);
            if (v == Long.MIN_VALUE) {
                return "0x" + Long.toHexString(v) + "L";
            } else {
                return ((Long) obj).toString() + "L";
            }
        }
        if (obj instanceof Float) {
            return ((Float) obj).toString() + "F";
        }
        if (obj instanceof Double) {
            return ((Double) obj).toString() + "D";
        }
        if (obj instanceof Short) {
            return ((Short) obj).toString() + "S";
        }
        if (obj instanceof Byte) {
            return ((Byte) obj).toString() + 't';
        }
        if (obj instanceof Character) {
            StringBuilder buf = new StringBuilder();
            buf.append("\'");
            escape0(buf, ((Character) obj).charValue());
            buf.append("\'");
            return buf.toString();
        }
        if (obj instanceof Boolean) {
            return ((Boolean) obj).toString();
        }
        if (obj instanceof Method) {
            Method m = (Method) obj;
            StringBuilder buf = new StringBuilder();
            escapeType0(buf, m.getOwner());
            buf.append("->");
            escapeId0(buf, m.getName());
            buf.append("(");
            for (String a : m.getParameterTypes()) {
                escapeType0(buf, a);
            }
            buf.append(")");
            escapeType0(buf, m.getReturnType());
            return buf.toString();
        }
        return null;
    }

    private static void dumpAnns(List<DexAnnotationNode> anns, Out out) {
        for (DexAnnotationNode ann : anns) {
            dumpAnn(ann, out);
        }
    }

    private static void dumpItem(String name, Object o, Out out, boolean array) {

        if (o instanceof Object[]) {
            Object[] vs = (Object[]) o;
            if (name != null) {
                out.s(escapeId(name) + " = {");
            } else {
                out.s("{");
            }
            out.push();
            for (int i = 0; i < vs.length; i++) {
                Object v = vs[i];
                dumpItem(null, v, out, i != vs.length - 1);
            }
            out.pop();
            if (array) {
                out.s("},");
            } else {
                out.s("}");
            }
        } else if (o instanceof DexAnnotationNode) {
            DexAnnotationNode dexAnnotationNode = (DexAnnotationNode) o;
            if (name != null) {
                out.s(escapeId(name) + " = .subannotation " + escapeType(dexAnnotationNode.type));
            } else {
                out.s(".subannotation " + escapeType(dexAnnotationNode.type));
            }
            out.push();
            for (Item item : dexAnnotationNode.items) {
                dumpItem(item.name, item.value, out, false);
            }
            out.pop();
            if (array) {
                out.s(".end subannotation,");
            } else {
                out.s(".end subannotation");
            }
        } else {
            StringBuilder sb = new StringBuilder();
            if (name != null) {
                sb.append(escapeId(name)).append(" = ");
            }
            sb.append(escapeValue(o));
            if (array) {
                sb.append(",");
            }
            out.s(sb.toString());
        }
    }

    private static void dumpAnn(DexAnnotationNode ann, Out out) {
        out.s(".annotation %s %s", ann.visibility.displayName(), escapeType(ann.type));
        out.push();
        for (Item item : ann.items) {
            dumpItem(item.name, item.value, out, false);
        }
        out.pop();
        out.s(".end annotation");
    }

    public void baksmaliClass(DexClassNode n, BufferedWriter writer) {
        baksmaliClass(n, new BaksmaliDumpOut(writer));
    }

    public void baksmaliClass(DexClassNode n, Out out) {

        buff.setLength(0);
        buff.append(".class ");
        appendAccess(n.access, buff);
        buff.append(escapeType(n.className));
        out.s(buff.toString());
        if (n.superClass != null) {
            out.s(".super %s", escapeType(n.superClass));
        }
        if (n.interfaceNames != null && n.interfaceNames.length > 0) {
            for (String itf : n.interfaceNames) {
                out.s(".implements %s", escapeType(itf));
            }
        }
        if (n.source != null) {
            out.s(".source " + escapeValue(n.source));
        }
        if (n.anns != null) {
            out.s("");
            dumpAnns(n.anns, out);
        }
        if (n.fields != null) {
            for (DexFieldNode f : n.fields) {
                out.s("");
                buff.setLength(0);
                buff.append(".field ");
                appendAccess(f.access | ACCESS_FIELD, buff);
                Field field = f.field;
                buff.append(escapeId(f.field.getName())).append(":").append(escapeType(field.getType()));
                if (f.cst != null) {
                    buff.append(" = ");
                    buff.append(escapeValue(f.cst));
                }
                out.s(buff.toString());

                if (f.anns != null) {
                    out.push();
                    dumpAnns(f.anns, out);
                    out.pop();
                    out.s(".end field");
                }
            }
        }
        if (n.methods != null) {
            for (DexMethodNode m : n.methods) {
                baksmaliMethod(m, out);
            }
        }
    }

    public void baksmaliMethod(DexMethodNode m, BufferedWriter writer) {
        baksmaliMethod(m, new BaksmaliDumpOut(writer));
    }

    public void baksmaliMethod(DexMethodNode m, Out out) {
        out.s("");
        buff.setLength(0);
        buff.append(".method ");
        Method method = m.method;
        appendAccess(m.access, buff);
        buff.append(escapeId(method.getName())).append(escapeMethodDesc(method));
        out.s(buff.toString());
        out.push();
        if (m.anns != null) {
            dumpAnns(m.anns, out);
        }

        int paramMax = 0;
        List<String> parameterNames = null;
        if (m.codeNode != null && m.codeNode.debugNode != null) {
            parameterNames = m.codeNode.debugNode.parameterNames;
            if (parameterNames != null) {
                paramMax = parameterNames.size();
            }
        }
        int annoMax = 0;
        if (m.parameterAnns != null) {
            for (int i = 0; i < m.parameterAnns.length; i++) {
                List<DexAnnotationNode> ps = m.parameterAnns[i];
                if (ps != null && ps.size() > 0) {
                    annoMax = i + 1;
                }
            }
        }

        int max = Math.max(paramMax, annoMax);
        for (int i = 0; i < max; i++) {
            String type = method.getParameterTypes()[i];
            String debugName = parameterNames == null ? null : i < parameterNames.size() ? parameterNames.get(i) : null;
            if (debugName != null) {
                out.s(".parameter \"" + escapeId(debugName) + "\" # " + type);
            } else {
                out.s(".parameter # " + type);
            }
            List<DexAnnotationNode> ps = m.parameterAnns == null ? null : m.parameterAnns[i];
            if (ps != null && ps.size() != 0) {
                out.push();
                dumpAnns(ps, out);
                out.pop();
                out.s(".end parameter");
            }
            // FIXME support '.param' REGISTER, STRING
        }

        if (m.codeNode != null) {
            baksmaliCode(m, m.codeNode, out);
        }

        out.pop();
        out.s(".end method");
    }

    public void baksmaliCode(DexMethodNode methodNode, DexCodeNode codeNode, Out out) {

        final List<DexLabel> allLabel = new ArrayList<>();
        final Set<DexLabel> usedLabel = new HashSet<>();
        codeNode.accept(new DexCodeVisitor() {
            @Override
            public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
                usedLabel.add(label);
            }

            @Override
            public void visitPackedSwitchStmt(Op op, int aA, int first_case, DexLabel[] labels) {
                usedLabel.addAll(Arrays.asList(labels));
            }

            @Override
            public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handler, String[] type) {
                usedLabel.add(start);
                usedLabel.add(end);
                usedLabel.addAll(Arrays.asList(handler));
            }

            @Override
            public void visitLabel(DexLabel label) {
                allLabel.add(label);
            }

            @Override
            public void visitSparseSwitchStmt(Op op, int ra, int[] cases, DexLabel[] labels) {
                usedLabel.addAll(Arrays.asList(labels));
            }
        });
        Map<DexLabel, List<DexDebugNode.DexDebugOpNode>> debugLabelMap = new HashMap<>();
        if (codeNode.debugNode != null) {
            DexDebugNode debugNode = codeNode.debugNode;
            for (DexDebugNode.DexDebugOpNode opNode : debugNode.debugNodes) {
                List<DexDebugNode.DexDebugOpNode> list = debugLabelMap.get(opNode.label);
                if (list == null) {
                    list = new ArrayList<>(3);
                    debugLabelMap.put(opNode.label, list);
                }
                list.add(opNode);
            }
        }
        int nextLabelNumber = 0;
        for (DexLabel label : allLabel) {
            if (usedLabel.contains(label)) {
                label.displayName = "L" + nextLabelNumber++;
            }
        }

        int inRegs = Utils.methodIns(methodNode.method, (methodNode.access & ACC_STATIC) != 0);

        DexCodeVisitor dexCodeVisitor = new BaksmaliCodeDumper(out, useParameterRegisters, useLocals, nextLabelNumber,
                codeNode.totalRegister - inRegs, usedLabel, debugLabelMap);
        accept(out, codeNode, dexCodeVisitor);
        dexCodeVisitor.visitEnd();
    }

    void accept(Out out, DexCodeNode code, DexCodeVisitor v) {
        if (code.tryStmts != null) {
            for (TryCatchNode n : code.tryStmts) {
                n.accept(v);
            }
        }
        if (code.debugNode != null) {
            DexDebugVisitor ddv = v.visitDebug();
            if (ddv != null) {
                code.debugNode.accept(ddv);
                ddv.visitEnd();
            }
        }
        if (code.totalRegister >= 0 && code.stmts.size() > 0) {
            v.visitRegister(code.totalRegister);
        }
        for (DexStmtNode n : code.stmts) {
            if (n instanceof DexLabelStmtNode) {
                n.accept(v);
            } else {
                out.push();
                n.accept(v);
                out.pop();
            }

        }
    }
}
