package com.googlecode.dex2jar.analysis.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.analysis.Analyzer;
import com.googlecode.dex2jar.analysis.CodeNode;
import com.googlecode.dex2jar.analysis.Node;
import com.googlecode.dex2jar.analysis.NodeDump;

public class TypeAnalyzer extends Analyzer {
    /**
     * Phi
     */
    @SuppressWarnings("serial")
    public static class Phi extends ArrayList<Phi> {

        public int hashCode() {
            return System.identityHashCode(this);
        }

        public boolean equals(Object that) {
            return this == that;
        }

        public Set<String> typs = new HashSet<String>();
        public boolean used;

        public Phi() {
            super(4);
        }

        public String toString() {
            if (typs.size() == 0) {
                return "?";
            }
            for (String t : typs) {
                switch (t.charAt(0)) {
                case '[':
                    return "[";
                case 'L':
                    break;
                case '_':
                    switch (t.charAt(1)) {
                    case '1':
                        return "1";
                    case '2':
                        return "2";
                    case '3':
                        return "3";
                    }
                    throw new RuntimeException();
                default:
                    return t.substring(0, 1);
                }
            }
            return "L";
        }
    }

    private final Phi[] frame;

    final boolean isStatic;
    final Method method;

    final TypeVisitor<Phi> typeVisitor;

    List<Phi> allRegs = new ArrayList<Phi>(30);

    public TypeAnalyzer(CodeNode cn, boolean isStatic, Method method) {
        super(cn);
        this.isStatic = isStatic;
        this.method = method;
        this.typeVisitor = new TypeVisitor<Phi>(totalReg, method.getReturnType()) {

            @Override
            public Phi _new() {
                Phi reg = new Phi();
                allRegs.add(reg);
                return reg;
            }

            @Override
            public void _put(int reg, Phi n) {
                frame[reg] = n;
            }

            @Override
            public void _type(Phi r, String desc) {
                Set<String> ts = r.typs;
                if (ts.size() == 0) {
                    ts.add(desc);
                    return;
                }
                if (ts.contains(desc)) {
                    return;
                }
                switch (desc.charAt(0)) {
                case '_': {
                    boolean f = false;
                    for (String s : ts) {
                        if (!s.startsWith("_")) {
                            f = true;
                            break;
                        }
                    }
                    if (!f) {
                        switch (desc.charAt(1)) {
                        case '1':
                            break;
                        case '2':
                            break;
                        case '3':
                            ts.remove(TypeVisitor.IFL);
                            ts.add(desc);
                            break;
                        }
                    }
                }
                    break;
                case '[': {
                    if (desc.length() > 1) {
                        if (desc.charAt(1) == '_') {
                            boolean f = false;
                            for (String s : ts) {
                                if (s.length() >= 2 && s.charAt(0) == '[' && s.charAt(1) != '_') {
                                    f = true;
                                    break;
                                }
                            }
                            if (!f) {
                                ts.add(desc);
                            }
                        } else {
                            ts.remove(TypeVisitor.AIFL);
                            ts.remove(TypeVisitor.AJD);
                        }
                    }
                    if (ts.contains(TypeVisitor.IFL) || ts.contains(TypeVisitor.IL)) {
                        ts.remove(TypeVisitor.IFL);
                        ts.remove(TypeVisitor.IL);
                        ts.add("L");
                    }
                }
                    break;
                case 'L':
                    if (desc.length() == 1) {
                        boolean f = false;
                        for (String s : ts) {
                            if (s.startsWith("L")) {
                                f = true;
                                break;
                            }
                        }
                        if (!f) {
                            ts.add(desc);
                        }
                    } else {
                        ts.remove("L");
                        ts.add(desc);
                    }
                    ts.remove(TypeVisitor.IFL);
                    ts.remove(TypeVisitor.IL);
                    ts.remove(TypeVisitor.JD);
                    break;
                default: {
                    ts.remove(TypeVisitor.IFL);
                    ts.remove(TypeVisitor.IL);
                    ts.remove(TypeVisitor.JD);
                    ts.add(desc);
                }
                }
            }

            @Override
            public Phi _use(int reg) {
                Phi r = (Phi) frame[reg];
                if (r != null) {
                    r.used = true;
                }
                return r;
            }
        };
        frame = new Phi[totalReg + 2];
    }

    static void doAddUsed(Phi r, Set<Phi> regs) {
        if (r.used) {
            if (!regs.contains(r)) {
                regs.add(r);
                for (int i = 0; i < r.size(); i++) {
                    Phi p = r.get(i);
                    p.used = true;
                    doAddUsed(p, regs);
                }
            }
        }
    }

    @Override
    public void analyze() {
        super.analyze();
        Set<Phi> used = new HashSet<Phi>(allRegs.size() / 2);

        for (Phi reg : allRegs) {
            doAddUsed(reg, used);
        }
        this.allRegs.clear();
        this.allRegs.addAll(used);

        for (Node p = cn.first; p != null; p = p.next) {
            Phi[] frame = (Phi[]) p.frame;
            if (frame != null) {
                for (int i = 0; i < frame.length; i++) {
                    Phi r = frame[i];
                    if (r != null) {
                        if (!r.used) {
                            frame[i] = null;
                        }
                    }
                }
            }
        }
        System.out.println(method);
    }

    protected Object createExceptionHandlerFrame(Node handler, String type) {
        Phi[] frame = (Phi[]) handler.frame;
        Phi obj;
        if (frame == null) {
            frame = new Phi[totalReg + 2];
            obj = frame[totalReg + 1] = typeVisitor._new();
        } else {
            obj = frame[totalReg + 1];
        }
        typeVisitor._type(obj, type);
        return frame;
    }

    protected Object exec(Node p) {
        Object[] frame = (Object[]) p.frame;
        if (frame == null) {
            for (int i = 0; i < this.frame.length; i++) {
                this.frame[i] = null;
            }
        } else {
            System.arraycopy(frame, 0, this.frame, 0, totalReg + 2);
        }
        p.accept(typeVisitor);
        return this.frame;
    }

    protected Object initFirstFrame(Node p) {
        int i = 0;
        Phi[] frame = new Phi[totalReg + 2];
        if (!isStatic) {
            Phi obj = frame[cn.args[i]] = typeVisitor._new();
            typeVisitor._type(obj, method.getOwner());
            i++;
        }
        for (String pt : method.getParameterTypes()) {
            Phi obj = frame[cn.args[i]] = typeVisitor._new();
            typeVisitor._type(obj, pt);
            i++;
        }
        return frame;
    }

    protected void merge(Object f, Node target) {
        Phi[] frame = (Phi[]) f;
        if (target._cfg_froms == 1) {// only from one node, direct copy node
            if (target.frame == null) {
                target.frame = new Phi[totalReg + 2];
                System.arraycopy(frame, 0, target.frame, 0, totalReg + 2);
            } else {
                Phi[] targetFrame = (Phi[]) target.frame;
                for (int i = 0; i < totalReg + 2; i++) {
                    if (targetFrame[i] == null) {
                        targetFrame[i] = frame[i];
                    }
                }
            }
        } else {// from 2+ node, insert a phi
            if (target.frame == null) {
                Phi[] targetFrame;
                target.frame = targetFrame = new Phi[totalReg + 2];
                for (int i = 0; i < totalReg + 2; i++) {
                    if (frame[i] != null) {
                        Phi reg = typeVisitor._new();
                        Phi src = (Phi) frame[i];
                        reg.add(src);// add to phi
                        targetFrame[i] = reg;
                    }
                }
            } else {
                Object[] targetFrame = (Object[]) target.frame;
                for (int i = 0; i < totalReg + 2; i++) {
                    if (frame[i] != null) {
                        Phi reg = (Phi) targetFrame[i];
                        Phi src = (Phi) frame[i];
                        if (reg == null) {
                            if (!target._cfg_visited) {
                                reg = typeVisitor._new();
                                targetFrame[i] = reg;
                                reg.add(src);// add to phi
                            }
                        } else {
                            reg.add((Phi) frame[i]);// add to phi
                        }
                    }
                }

            }
        }
    }

    public String toString() {
        StringBuilder tmp = new StringBuilder();
        NodeDump nd = new NodeDump();
        for (Node t : cn.trys) {
            t.accept(nd);
        }

        nd.sb.append('\n');

        for (Node p = cn.first; p != null; p = p.next) {
            Phi[] rs = (Phi[]) p.frame;
            if (p._cfg_visited) {
                nd.sb.append("Y ");
            } else {
                nd.sb.append("N ");
            }
            if (rs != null) {
                for (int i = 0; i < rs.length; i++) {
                    if (rs[i] == null) {
                        tmp.append('.');
                    } else {
                        tmp.append(rs[i]);
                    }
                }
                nd.sb.append(tmp).append(" |");
                tmp.setLength(0);
            }
            p.accept(nd);
        }

        return nd.toString();
    }
}
