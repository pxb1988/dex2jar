package com.googlecode.dex2jar.test;

import java.util.ArrayList;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.LocalRemove;
import com.googlecode.dex2jar.ir.ts.LocalSplit;
import com.googlecode.dex2jar.ir.ts.LocalType;
import com.googlecode.dex2jar.ir.ts.Transformer;
import com.googlecode.dex2jar.v3.EndRemover;
import com.googlecode.dex2jar.v3.ExceptionHandlerCurrect;
import com.googlecode.dex2jar.v3.IrMethod2AsmMethod;
import com.googlecode.dex2jar.v3.LocalCurrect;

/**
 * test case for issue 63
 */
public class I63Test {
    @Test
    public void a() throws AnalyzerException {
        IrMethod irMethod = new IrMethod();
        irMethod.name = "test";
        irMethod.args = new Type[] {};
        irMethod.ret = Type.VOID_TYPE;

        LabelStmt L1 = Stmts.nLabel();
        LabelStmt L2 = Stmts.nLabel();
        Local left = Exprs.nLocal("a", null);
        irMethod.locals.add(left);
        irMethod.stmts.add(L1);
        irMethod.stmts.add(Stmts.nAssign(left, Exprs.nStaticField(Type.getType("La/A;"), "a", Type.getType("La/A;"))));
        irMethod.stmts.add(L2);
        irMethod.stmts.add(Stmts.nReturnVoid());

        irMethod.traps.add(new Trap(L1, L2, L2, null));

        Transformer[] tses = new Transformer[] { new ExceptionHandlerCurrect(), new LocalSplit(), new LocalRemove(),
                new LocalType(), new LocalCurrect() };
        Transformer endremove = new EndRemover();
        endremove.transform(irMethod);

        // indexLabelStmt4Debug(irMethod.stmts);

        MethodNode methodNode = new MethodNode();

        methodNode.name = "a";
        methodNode.access = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
        methodNode.desc = "()V";
        methodNode.tryCatchBlocks = new ArrayList();

        for (Transformer ts : tses) {
            ts.transform(irMethod);
        }
        new IrMethod2AsmMethod().convert(irMethod, methodNode);

        methodNode.maxLocals = 0;
        methodNode.maxStack = 1;

        BasicVerifier verifier = new BasicVerifier();
        Analyzer a = new Analyzer(verifier);
        a.analyze("a.B", methodNode);
    }
}
