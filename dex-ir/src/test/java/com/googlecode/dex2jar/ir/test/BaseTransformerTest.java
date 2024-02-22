package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Transformer;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

// Ignore the unused inspection; it is indeed used
public abstract class BaseTransformerTest<T extends Transformer> {

    protected void transform() {
        transformer.transform(this.method);
    }

    @Override
    public String toString() {
        return method.toString();
    }

    int labelIndex = 0;

    protected LabelStmt newLabel() {
        LabelStmt label = Stmts.nLabel();
        label.displayName = "L" + labelIndex++;
        return label;
    }

    public BaseTransformerTest() {
        super();
        Class<?> clz = getClass();
        ParameterizedType t = (ParameterizedType) clz.getGenericSuperclass();
        Class<?> transformerType = (Class<?>) t.getActualTypeArguments()[0];

        try {
            this.transformer = (Transformer) transformerType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final protected Transformer transformer;
    protected IrMethod method = new IrMethod();
    protected StmtList stmts;
    protected List<Local> locals;


    public Local addLocal(String name) {
        Local local = Exprs.nLocal(name);
        method.locals.add(local);
        return local;
    }

    public <D extends Stmt> D addStmt(D stmt) {
        method.stmts.add(stmt);
        return stmt;
    }

    public AssignStmt attachPhi(LabelStmt labelStmt, AssignStmt phi) {
        List<AssignStmt> s = labelStmt.phis;
        if (s == null) {
            labelStmt.phis = s = new ArrayList<>();
        }
        s.add(phi);
        if (method.phiLabels == null) {
            method.phiLabels = new ArrayList<>();
        }
        if (!method.phiLabels.contains(labelStmt)) {
            method.phiLabels.add(labelStmt);
        }
        return phi;
    }

    public void initMethod(boolean isStatic, String ret, String... args) {
        method.ret = ret;
        method.args = args;
        method.isStatic = isStatic;
    }

    @AfterEach
    public void reset() {
        method = null;
        stmts = null;
        locals = null;
        labelIndex = 0;
    }

    @BeforeEach
    public void setup() {
        method = new IrMethod();
        method.owner = "La/Clz;";
        method.name = "call";
        method.ret = "V";
        method.isStatic = true;
        method.args = new String[0];
        stmts = method.stmts;
        locals = method.locals;
    }
}
