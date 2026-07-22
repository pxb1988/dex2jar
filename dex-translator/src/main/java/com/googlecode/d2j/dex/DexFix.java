package com.googlecode.d2j.dex;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFieldNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.node.insn.ConstStmtNode;
import com.googlecode.d2j.node.insn.DexStmtNode;
import com.googlecode.d2j.node.insn.MethodStmtNode;
import com.googlecode.d2j.node.insn.Stmt1RNode;
import com.googlecode.d2j.node.insn.TypeStmtNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1. Dex omits the value of static-final field if it is the default value.
 * <p>
 * 2. static-final field init by zero, but assigned in clinit
 * <p>
 * this method is try to fix the problems.
 */
public final class DexFix {

    private static final int ACC_STATIC_FINAL = DexConstants.ACC_STATIC | DexConstants.ACC_FINAL;

    private DexFix() {
        throw new UnsupportedOperationException();
    }

    public static void fixStaticFinalFieldValue(final DexFileNode dex) {
        if (dex.clzs != null) {
            for (DexClassNode classNode : dex.clzs) {
                fixStaticFinalFieldValue(classNode);
            }
        }
    }

    /**
     * Target: Class
     * <p>
     * init value to default if the field is static and final, and the field is not init in clinit method
     * <p>
     * erase the default value if the field is init in clinit method
     */
    public static void fixStaticFinalFieldValue(final DexClassNode classNode) {
        if (classNode.fields == null) {
            return;
        }
        final Map<String, DexFieldNode> fs = new HashMap<>();
        final Map<String, DexFieldNode> shouldNotBeAssigned = new HashMap<>();
        for (DexFieldNode fn : classNode.fields) {
            if ((fn.access & ACC_STATIC_FINAL) == ACC_STATIC_FINAL) {
                if (fn.cst == null) {
                    char t = fn.field.getType().charAt(0);
                    if (t == 'L' || t == '[') {
                        // ignore Object
                        continue;
                    }
                    fs.put(fn.field.getName() + ":" + fn.field.getType(), fn);
                } else if (isPrimitiveZero(fn.field.getType(), fn.cst)) {
                    shouldNotBeAssigned.put(fn.field.getName() + ":" + fn.field.getType(), fn);
                }
            }
        }
        if (fs.isEmpty() && shouldNotBeAssigned.isEmpty()) {
            return;
        }
        DexMethodNode node = null;
        if (classNode.methods != null) {
            for (DexMethodNode mn : classNode.methods) {
                if (mn.method.getName().equals("<clinit>")) {
                    node = mn;
                    break;
                }
            }
        }
        if (node != null) {
            if (node.codeNode != null) {
                node.codeNode.accept(new DexCodeVisitor() {
                    @Override
                    public void visitFieldStmt(Op op, int a, int b, Field field) {
                        switch (op) {
                        case SPUT:
                        case SPUT_BOOLEAN:
                        case SPUT_BYTE:
                        case SPUT_CHAR:
                        case SPUT_OBJECT:
                        case SPUT_SHORT:
                        case SPUT_WIDE:
                            if (field.getOwner().equals(classNode.className)) {
                                String key = field.getName() + ":" + field.getType();
                                fs.remove(key);
                                DexFieldNode dn = shouldNotBeAssigned.get(key);
                                if (dn != null) {
                                    //System.out.println(field.getName() + ":" + field.getType());
                                    dn.cst = null;
                                }
                            }
                            break;
                        default:
                            // ignored
                            break;
                        }
                    }
                });
            } else {
                // has init but no code
                return;
            }
        }

        for (DexFieldNode fn : fs.values()) {
            fn.cst = getDefaultValueOfType(fn.field.getType().charAt(0));
        }

    }

    /**
     * Target: Method
     * <p>
     * Fixes too long strings not being translated correctly
     * </p>
     */
    public static void fixTooLongStringConstant(final DexMethodNode methodNode) {
        if ((methodNode.access & DexConstants.ACC_NATIVE) != 0 || (methodNode.access & DexConstants.ACC_ABSTRACT) != 0) {
            return; // in case of unimplemented method
        }

        if (methodNode.codeNode != null) {
            HashMap<DexStmtNode, List<DexStmtNode>> toBeReplaced = new HashMap<>();
            AtomicInteger maxRegister = new AtomicInteger(methodNode.codeNode.totalRegister);
            methodNode.codeNode.stmts.forEach(insn -> {
                if (insn instanceof ConstStmtNode &&
                    (insn.op == Op.CONST_STRING || insn.op == Op.CONST_STRING_JUMBO) &&
                    ((ConstStmtNode) insn).value instanceof String) {
                    String s = (String) ((ConstStmtNode) insn).value;
                    int register = ((ConstStmtNode) insn).a;

                    if (s.length() < 32767) {
                        return; // don't care about normal strings
                    }

                    List<String> parts = new ArrayList<>();
                    int length = s.length();
                    for (int i = 0; i < length; i += 32767) {
                        parts.add(s.substring(i, Math.min(length, i + 32767)));
                    }

                    ArrayList<DexStmtNode> generatedStringConcat = new ArrayList<>();
                    // new-instance v0 Ljava/lang/StringBuilder;
                    generatedStringConcat.add(new TypeStmtNode(
                            Op.NEW_INSTANCE, register, 0, "Ljava/lang/StringBuilder;"
                    ));
                    // const v1 STR_LEN
                    generatedStringConcat.add(new ConstStmtNode(Op.CONST, register + 1, s.length()));
                    // invoke-direct {v0, v1} Ljava/lang/StringBuilder;-><init>(I)V
                    generatedStringConcat.add(new MethodStmtNode(
                            Op.INVOKE_DIRECT,
                            new int[]{register, register + 1},
                            new Method("Ljava/lang/StringBuilder;", "<init>", new String[]{"I"}, "V")
                    ));

                    for (String part : parts) {
                        // const-string v1 "xxx"
                        generatedStringConcat.add(new ConstStmtNode(Op.CONST_STRING, register + 1, part));
                        // invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)
                        // Ljava/lang/StringBuilder;
                        generatedStringConcat.add(new MethodStmtNode(
                                Op.INVOKE_VIRTUAL,
                                new int[]{register, register + 1},
                                new Method("Ljava/lang/StringBuilder;", "append",
                                        new String[]{"Ljava/lang/String;"}, "Ljava/lang/StringBuilder;")
                        ));
                    }

                    // invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                    generatedStringConcat.add(new MethodStmtNode(
                            Op.INVOKE_VIRTUAL,
                            new int[]{register},
                            new Method("Ljava/lang/StringBuilder;", "toString",
                                    new String[]{}, "Ljava/lang/String;")
                    ));

                    // move-result-object v0
                    generatedStringConcat.add(new Stmt1RNode(Op.MOVE_RESULT_OBJECT, register));

                    toBeReplaced.put(insn, generatedStringConcat);
                    maxRegister.set(Math.max(register + 2, maxRegister.get()));
                }
            });

            methodNode.codeNode.totalRegister = maxRegister.get();
            toBeReplaced.keySet().forEach(i -> {
                int index = methodNode.codeNode.stmts.indexOf(i);
                methodNode.codeNode.stmts.addAll(index, toBeReplaced.get(i));
                methodNode.codeNode.stmts.remove(i);
            });
        }
    }

    private static Object getDefaultValueOfType(char t) {
        switch (t) {
        case 'B':
            return (byte) 0;
        case 'Z':
            return Boolean.FALSE;
        case 'S':
            return (short) 0;
        case 'C':
            return (char) 0;
        case 'I':
            return 0;
        case 'F':
            return (float) 0.0;
        case 'J':
            return 0L;
        case 'D':
            return 0.0;
        case '[':
        case 'L':
        default:
            return null;
        // impossible
        }
    }

    static boolean isPrimitiveZero(String desc, Object value) {
        if (value != null && desc != null && !desc.isEmpty()) {
            switch (desc.charAt(0)) {
            // case 'V':// VOID_TYPE
            case 'Z':// BOOLEAN_TYPE
                return !((Boolean) value);
            case 'C':// CHAR_TYPE
                return (Character) value == (char) 0;
            case 'B':// BYTE_TYPE
                return (Byte) value == 0;
            case 'S':// SHORT_TYPE
                return (Short) value == 0;
            case 'I':// INT_TYPE
                return (Integer) value == 0;
            case 'F':// FLOAT_TYPE
                return (Float) value == 0f;
            case 'J':// LONG_TYPE
                return (Long) value == 0L;
            case 'D':// DOUBLE_TYPE
                return (Double) value == 0.0;
            default:
                break;
            }
        }
        return false;
    }

}
