package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.*;

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
                if (op.vt == Value.VT.CHECK_CAST) {
                    TypeExpr te = (TypeExpr) op;
                    if (te.op.vt == Value.VT.CHECK_CAST) {
                        TypeExpr te2 = (TypeExpr) te.op;
                        if (te.type.equals(te2.type)) {
                            op = te2;
                        }
                    }
                }
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
                                    && invokeExpr.args[0].equals("Ljava/lang/Class;")) {
                                Value arg0 = invokeExpr.getOps()[0];
                                String elementType = null;
                                if (arg0.vt == Value.VT.CONSTANT) {
                                    elementType = ((Constant.Type) ((Constant) invokeExpr.getOps()[0]).value).desc;
                                } else {
                                    if (arg0.vt == Value.VT.STATIC_FIELD) {
                                        StaticFieldExpr sfe = (StaticFieldExpr) arg0;
                                        if (sfe.owner.startsWith("Ljava/lang/") && sfe.name.equals("TYPE")) {
                                            switch (sfe.owner) {
                                                case "Ljava/lang/Boolean;":
                                                    elementType = "Z";
                                                    break;
                                                case "Ljava/lang/Byte;":
                                                    elementType = "B";
                                                    break;
                                                case "Ljava/lang/Short;":
                                                    elementType = "S";
                                                    break;
                                                case "Ljava/lang/Character;":
                                                    elementType = "C";
                                                    break;
                                                case "Ljava/lang/Integer;":
                                                    elementType = "I";
                                                    break;
                                                case "Ljava/lang/Long;":
                                                    elementType = "J";
                                                    break;
                                                case "Ljava/lang/Float;":
                                                    elementType = "F";
                                                    break;
                                                case "Ljava/lang/Double;":
                                                    elementType = "D";
                                                    break;
                                                case "Ljava/lang/Void;":
                                                    elementType = "V";
                                                    break;
                                                default:
                                            }
                                        }
                                    }
                                }
                                if (elementType != null) {
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
                                                return Exprs.nNewArray(elementType, dt);
                                            }
                                        }
                                    } else {// [I
                                        if (dt.vt == Value.VT.FILLED_ARRAY) {
                                            FilledArrayExpr filledArrayExpr = (FilledArrayExpr) dt;
                                            int d = filledArrayExpr.getOps().length;
                                            if (te.type.length() > d && te.type.substring(d).equals(elementType)) {
                                                int d1 = 0;
                                                while (elementType.charAt(d1) == '[') {
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
                }
                return op;
            }
        }.travel(method);

        return changed[0];
    }
}
