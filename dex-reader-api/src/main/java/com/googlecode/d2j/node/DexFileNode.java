package com.googlecode.d2j.node;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import java.util.ArrayList;
import java.util.List;

public class DexFileNode extends DexFileVisitor {

    public List<DexClassNode> clzs = new ArrayList<>();

    public int dexVersion = DexConstants.DEX_035;

    @Override
    public void visitDexFileVersion(int version) {
        this.dexVersion = version;
        super.visitDexFileVersion(version);
    }

    @Override
    public DexClassVisitor visit(int accessFlags, String className, String superClass, String[] interfaceNames) {
        DexClassNode cn = new DexClassNode(accessFlags, className, superClass, interfaceNames);
        clzs.add(cn);
        return cn;
    }

    public void accept(DexClassVisitor dcv) {
        for (DexClassNode cn : clzs) {
            cn.accept(dcv);
        }
    }

    public void accept(DexFileVisitor dfv) {
        for (DexClassNode cn : clzs) {
            cn.accept(dfv);
        }
    }

}
