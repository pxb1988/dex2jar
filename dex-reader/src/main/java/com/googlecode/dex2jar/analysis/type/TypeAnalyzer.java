package com.googlecode.dex2jar.analysis.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.analysis.Analyzer;
import com.googlecode.dex2jar.analysis.CodeNode;
import com.googlecode.dex2jar.analysis.Node;
import com.googlecode.dex2jar.analysis.NodeDump;

public class TypeAnalyzer extends Analyzer {
    /**
     * Phi
     */
    public static class Phi {

        public List<Phi> parent = new ArrayList<Phi>(5);

        public Set<String> typs = new HashSet<String>();
        public boolean used;

        public Phi tag;

        public String toStringx() {
            if (tag != null) {
                return tag.toStringx();
            }
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
                    return t.substring(1, 2);
                default:
                    return t.substring(0, 1);
                }
            }
            return "L";
        }

        public String toString() {
            if (tag != null) {
                return tag.toString();
            }
            return typs.toString();
        }
    }

    private final Phi[] frame;

    final boolean isStatic;
    final Method method;

    boolean debug = true;

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
                        case '1':// IFL
                            break;
                        case '2':// JD
                            break;
                        case '3':// IL
                            ts.remove(TypeVisitor.IFL);
                            if (ts.remove(TypeVisitor.IF)) {
                                ts.add("I");
                            } else {
                                ts.add(desc);
                            }
                            break;
                        case '4':// IF
                            ts.remove(TypeVisitor.IFL);
                            if (ts.remove(TypeVisitor.IL)) {
                                ts.add("I");
                            } else {
                                ts.add(desc);
                            }
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
                    ts.remove(TypeVisitor.IF);
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

    private static void doAddUsed(Phi r, Set<Phi> regs) {
        if (r.used) {
            if (!regs.contains(r)) {
                regs.add(r);
                for (int i = 0; i < r.parent.size(); i++) {
                    Phi p = r.parent.get(i);
                    p.used = true;
                    doAddUsed(p, regs);
                }
            }
        }
    }

    Phi trim(Phi phi) {
        while (phi.tag != null) {
            phi = phi.tag;
        }
        return phi;
    }

    @Override
    public void analyze() {
        super.analyze();
        {
            List<Phi> allRegs = this.allRegs;
            Set<Phi> used = new HashSet<Phi>(allRegs.size() / 2);

            for (Phi reg : allRegs) {
                doAddUsed(reg, used);
            }
            allRegs.clear();

            for (Phi reg : used) {
                Phi a = trim(reg);
                if (a != reg) {
                    for (String t : reg.typs) {
                        typeVisitor._type(a, t);
                    }
                    // a.typs.addAll(reg.typs);
                }
                if (reg.parent.size() > 0) {
                    for (Phi r : reg.parent) {
                        Phi b = trim(r);
                        if (a != b) {
                            for (String t : r.typs) {
                                typeVisitor._type(a, t);
                            }
                            // a.typs.addAll(r.typs);
                            b.tag = a;
                        }
                    }
                }
            }

            for (Phi reg : used) {
                if (reg.tag == null) {
                    reg.parent = null;
                    allRegs.add(reg);
                }
            }
            used.clear();
        }
        List<Pairs> same = new ArrayList<Pairs>();
        List<Pairs> array = new ArrayList<Pairs>();
        for (Node p = cn.first; p != null; p = p.next) {
            Phi[] frame = (Phi[]) p.frame;
            if (frame != null) {
                for (int i = 0; i < frame.length; i++) {
                    Phi r = frame[i];
                    if (r != null) {
                        if (!r.used) {
                            frame[i] = null;
                        } else {
                            frame[i] = trim(r);
                        }
                    }
                }
            }

            switch (p.opcode) {
            case OP_MOVE:
                same.add(new Pairs(trim((Phi) p.cst), ((Phi[]) p.frame)[p.b]));
                break;
            case OP_MOVE_EXCEPTION:
                same.add(new Pairs(trim((Phi) p.cst), ((Phi[]) p.frame)[totalReg + 1]));
                break;
            case OP_MOVE_RESULT:
                same.add(new Pairs(trim((Phi) p.cst), ((Phi[]) p.frame)[totalReg]));
                break;
            case OP_AGET:
                array.add(new Pairs(trim((Phi) p.cst), ((Phi[]) p.frame)[p.b]));
                break;
            case OP_APUT:
                array.add(new Pairs(((Phi[]) p.frame)[p.a], ((Phi[]) p.frame)[p.b]));
                break;
            }

        }

        List<Phi> argsCache = new ArrayList();
        for (Node p = cn.first; p != null; p = p.next) {
            switch (p.opcode) {
            case OP_IGET_QUICK:
            case OP_IPUT_QUICK: {
                Phi[] frame = (Phi[]) p.frame;
                Field field = getField(frame[p.b], p.c, frame[p.a]);
                // typeVisitor._type(frame[p.b], field.getOwner());
                // typeVisitor._type(frame[p.a], field.getType());
                // p.opcode = OP_IGET_QUICK == p.opcode ? OP_IGET : OP_IPUT;
                // p.field = field;
            }
                break;
            case OP_INVOKE_VIRTUAL_QUICK:
            case OP_INVOKE_SUPER_QUICK: {
                Phi[] frame = (Phi[]) p.frame;

                for (int i = 1; i < p.args.length; i++) {
                    Phi phi = frame[p.args[i]];
                    if (phi != null) {
                        argsCache.add(phi);
                    }
                }
                Method m = getMethod(frame[p.args[0]], p.a, argsCache);
                // p.method = m;
                // p.opcode = OP_INVOKE_VIRTUAL_QUICK == p.opcode ? OP_INVOKE_VIRTUAL : OP_INVOKE_SUPER;
                // TODO update p.args
                argsCache.clear();
                break;
            }
            case OP_EXECUTE_INLINE:
                // TODO
            }
        }

        System.out.println(method);
    }

    static class Pairs {
        Pairs(Phi a, Phi b) {
            this.a = a;
            this.b = b;
        }

        public String toString() {
            return a + "<>" + b;
        }

        Phi a, b;
    }

    private Method getMethod(Phi phi, int a, List<Phi> args) {
        // TODO Auto-generated method stub
        return null;
    }

    private Field getField(Phi phi, int c, Phi phi2) {
        // TODO Auto-generated method stub
        return null;
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
        switch (p.opcode) {
        case OP_MOVE:
        case OP_MOVE_EXCEPTION:
        case OP_MOVE_RESULT:
            p.cst = this.frame[p.a];
            break;
        case OP_AGET:
            p.cst = this.frame[p.a];
            break;
        }
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
        if (!debug && target._cfg_froms == 1) {// from one node, direct copy node
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
                        reg.parent.add(src);// add to phi
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
                                reg.parent.add(src);// add to phi
                            }
                        } else {
                            reg.parent.add((Phi) frame[i]);// add to phi
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
                nd.sb.append("y ");
            } else {
                nd.sb.append("  ");
            }
            if (rs != null) {
                for (int i = 0; i < rs.length; i++) {
                    if (rs[i] == null) {
                        tmp.append('.');
                    } else {
                        tmp.append(rs[i].toStringx());
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
