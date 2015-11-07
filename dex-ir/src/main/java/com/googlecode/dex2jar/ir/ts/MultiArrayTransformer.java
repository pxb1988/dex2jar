package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.*;

import java.lang.reflect.Array;

/**
 * dex does have the instruction to create a multi-array. the implement is to
 * using the Array.newInstance().
 * transform
 * <pre>
 * ((String[][][])Array.newInstance(String.class,new int[]{4, 5, 6}))
 * </pre>
 * to
 * <pre>
 * new String[4][5][6]
 * </pre>
 */
public class MultiArrayTransformer extends StatedTransformer {
    @Override
    public boolean transformReportChanged(IrMethod method) {
        final boolean changed[] = {false};
        new StmtTraveler() {
            @Override
            public Value travel(Value op) {
                op = super.travel(op);
                if (op.vt == Value.VT.CHECK_CAST) {
                    TypeExpr te = (TypeExpr) op;
                    if (te.type.charAt(0) == '[') {
                        Value from = te.getOp();
                        if (from.vt == Value.VT.INVOKE_STATIC) {
                            InvokeExpr invokeExpr = (InvokeExpr) from;
                            if (invokeExpr.name.equals("newInstance")
                                    && invokeExpr.owner.equals("Ljava/lang/reflect/Array;")
                                    && invokeExpr.args.length == 2
                                    && invokeExpr.args[0].equals("Ljava/lang/Class;")
                                    && invokeExpr.getOps()[0].vt == Value.VT.CONSTANT
                                    ) {

                                String elementType = ((Constant.Type) ((Constant) invokeExpr.getOps()[0]).value).desc;
                                Value dt = invokeExpr.getOps()[1];
                                if (invokeExpr.args[1].equals("I")) {
                                    if (te.type.equals("[" + elementType)) {
                                        int d = 0;
                                        while (elementType.charAt(d) == '[') {
                                            d++;
                                        }
                                        changed[0] = true;
                                        if (d > 0) {
                                            return Exprs.nNewMutiArray(elementType.substring(d), d + 1, new Value[]{dt});
                                        } else {
                                            return Exprs.nFilledArray(elementType, new Value[]{dt});
                                        }
                                    }
                                } else {// [I
                                    if (dt.vt == Value.VT.FILLED_ARRAY) {
                                        FilledArrayExpr filledArrayExpr = (FilledArrayExpr) dt;
                                        int d = filledArrayExpr.getOps().length;
                                        if (te.type.length() > d && te.type.substring(d).equals(elementType)) {
                                            int d1 = 0;
                                            while (elementType.charAt(d) == '[') {
                                                d1++;
                                            }
                                            changed[0] = true;
                                            return Exprs.nNewMutiArray(elementType.substring(d1), d1 + d, filledArrayExpr.getOps());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return op;
            }
        }.travel(method);

        return changed[0];
    }
}
