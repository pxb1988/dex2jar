package com.googlecode.dex2jar.analysis.type;

import java.util.HashSet;
import java.util.Set;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.analysis.Analyzer;
import com.googlecode.dex2jar.analysis.CodeNode;
import com.googlecode.dex2jar.analysis.Node;

public class TypeAnalyzer extends Analyzer {
    public static class Reg {
        /**
         * Phi
         */
        public Set<Reg> phi = new HashSet<Reg>(3);
        public Set<String> typs = new HashSet<String>();
        public boolean used;

        public String toString() {
            if (typs.size() == 0) {
                return "?";
            }
            if (typs.size() == 1) {
                return typs.iterator().next().substring(0, 1);
            }
            return sizeOf(this) == 1 ? "S" : "W";
        }
    }

    public static int sizeOf(Reg reg) {
        for (String s : reg.typs) {
            switch (s.charAt(0)) {
            case 'J':
            case 'D':
                return 2;
            }
        }
        return 1;
    }

    final TypeVisitor<Reg> typeVisitor;

    private final Reg[] frame;
    final boolean isStatic;

    final Method method;

    public TypeAnalyzer(CodeNode cn, boolean isStatic, Method method) {
        super(cn);
        this.isStatic = isStatic;
        this.method = method;
        this.typeVisitor = new TypeVisitor<Reg>(totalReg, method.getReturnType()) {

            @Override
            public Reg _new() {
                return new Reg();
            }

            @Override
            public void _put(int reg, Reg n) {
                frame[reg] = n;
            }

            @Override
            public int _size(int i) {
                return sizeOf((Reg) frame[i]);
            }

            @Override
            public void _type(Reg r, String desc) {
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
            public Reg _use(int reg) {
                Reg r = (Reg) frame[reg];
                if (r != null) {
                    r.used = true;
                }
                return r;
            }
        };
        frame = new Reg[totalReg + 2];
    }

    protected Object createExceptionHandlerFrame(Node handler, String type) {
        Reg[] frame = (Reg[]) handler.frame;
        Reg obj;
        if (frame == null) {
            frame = new Reg[totalReg + 2];
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
        Reg[] frame = new Reg[totalReg + 2];
        if (!isStatic) {
            Reg obj = frame[cn.args[i]] = typeVisitor._new();
            typeVisitor._type(obj, method.getOwner());
            i++;
        }
        for (String pt : method.getParameterTypes()) {
            Reg obj = frame[cn.args[i]] = typeVisitor._new();
            typeVisitor._type(obj, pt);
            i++;
        }
        return frame;
    }

    protected void merge(Object f, Node target) {
        Object[] frame = (Object[]) f;
        if (target._cfg_froms == 1) {// only from one node, direct copy node
            if (target.frame == null) {
                target.frame = new Object[totalReg + 2];
                System.arraycopy(frame, 0, target.frame, 0, totalReg + 2);
            } else {
                Object[] targetFrame = (Object[]) target.frame;
                for (int i = 0; i < totalReg + 2; i++) {
                    if (targetFrame[i] == null) {
                        targetFrame[i] = frame[i];
                    }
                }
            }
        } else {// from 2+ node, insert a phi
            if (target.frame == null) {
                Object[] targetFrame;
                target.frame = targetFrame = new Object[totalReg + 2];
                for (int i = 0; i < totalReg + 2; i++) {
                    if (frame[i] != null) {
                        Reg reg = new Reg();
                        reg.phi.add((Reg) frame[i]);
                        targetFrame[i] = reg;
                    }
                }
            } else {
                Object[] targetFrame = (Object[]) target.frame;
                for (int i = 0; i < totalReg + 2; i++) {
                    if (frame[i] != null) {
                        Reg reg = (Reg) targetFrame[i];
                        if (reg == null) {
                            if (!target._cfg_visited) {
                                reg = new Reg();
                                targetFrame[i] = reg;
                                reg.phi.add((Reg) frame[i]);
                            }
                        } else {
                            reg.phi.add((Reg) frame[i]);
                        }
                    }
                }

            }
        }
    }
}
