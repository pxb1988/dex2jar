package com.googlecode.d2j.map;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.stmt.Stmt;

import java.util.HashSet;
import java.util.Set;

public class AntiObf {
    private boolean initEnumNames = false;
    private boolean initSourceNames = false;
    private boolean initAssertionNames = false;

    public AntiObf initEnumNames() {
        this.initEnumNames = true;
        return this;
    }

    public AntiObf initSourceNames() {
        this.initSourceNames = true;
        return this;
    }

    public AntiObf initAssertionNames() {
        this.initAssertionNames = true;
        return this;
    }

    static class CodeResolver {
        IrMethod find(String owner, String name, String[] pas, String ret) {
            return null;
        };
    }

    public void suggestNames(InheritanceTree tree, CodeResolver codeResolver) {
        if (initEnumNames) {
            for (InheritanceTree.Clz clz : tree.clzMap.values()) {
                if (clz.stat == InheritanceTree.Stat.APP) {
                    if (clz.superClz.name.oldValue.equals("Ljava/lang/Enum;")) {
                        String clzDesc = clz.name.oldValue;

                        Set<String> enumClzs = new HashSet<>();
                        enumClzs.add(clzDesc);
                        for (InheritanceTree.Clz d : clz.children) {
                            enumClzs.add(d.name.oldValue);
                        }

                        IrMethod ir = codeResolver.find(clzDesc, "<clinit>", new String[0], "V");
                        // InheritanceTree$Stat.UNKNOW = new InheritanceTree$Stat("UNKNOW",0)
                        // InheritanceTree$Stat.LIBRARY = new InheritanceTree$Stat("LIBRARY",1)
                        // InheritanceTree$Stat.APP = new InheritanceTree$Stat("APP",2)
                        // InheritanceTree$Stat.ENUM$VALUES = new InheritanceTree$Stat[]{InheritanceTree$Stat.UNKNOW,
                        // InheritanceTree$Stat.LIBRARY, InheritanceTree$Stat.APP}

                        for (Stmt stmt : ir.stmts) {
                            if (stmt.st == Stmt.ST.ASSIGN && stmt.getOp1().vt == Value.VT.STATIC_FIELD) {
                                StaticFieldExpr sfe = (StaticFieldExpr) stmt.getOp1();
                                if (sfe.owner.equals(clzDesc)) {
                                    if (stmt.getOp2().vt == Value.VT.INVOKE_NEW) {
                                        InvokeExpr n = (InvokeExpr) stmt.getOp2();
                                        if (enumClzs.contains(n.owner) && n.args.length >= 2
                                                && n.args[0].equals("Ljava/lang/String;") && n.args[1].equals("I")) {
                                            if (n.ops[0].vt == Value.VT.CONSTANT) {
                                                String cst = (String) ((Constant) n.ops[0]).value;
                                                InheritanceTree.Fld fld = clz.fields.get(InheritanceTree.toFieldKey(
                                                        sfe.name, sfe.type));
                                                if (!cst.equals(fld.name.oldValue)) {
                                                    if (fld.name.newValue != null && !fld.name.newValue.equals(cst)) {
                                                        System.err.println(String.format(
                                                                "WARN: %s->%s is suggest different names: %s, %s",
                                                                sfe.owner, sfe.name, fld.name.newValue, cst));
                                                    }
                                                    fld.name.newValue = cst;
                                                }
                                            }
                                        }
                                    } else if (stmt.getOp2().vt == Value.VT.FILLED_ARRAY) {
                                        FilledArrayExpr fae = (FilledArrayExpr) stmt.getOp2();
                                        if (fae.type.equals("[" + clzDesc)) {
                                            // TODO check more
                                            InheritanceTree.Fld fld = clz.fields.get(InheritanceTree.toFieldKey(
                                                    sfe.name, sfe.type));
                                            String cst = "ENUM$VALUES";
                                            if (!cst.equals(fld.name.oldValue)) {
                                                if (fld.name.newValue != null && !fld.name.newValue.equals(cst)) {
                                                    System.err.println(String.format(
                                                            "WARN: %s->%s is suggest different names: %s, %s",
                                                            sfe.owner, sfe.name, fld.name.newValue, cst));
                                                }
                                                fld.name.newValue = cst;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
