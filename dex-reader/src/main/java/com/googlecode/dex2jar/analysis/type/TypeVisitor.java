package com.googlecode.dex2jar.analysis.type;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;
import com.googlecode.dex2jar.visitors.OdexCodeVisitor;

public abstract class TypeVisitor<T> implements OdexCodeVisitor, OdexOpcodes {

    protected void useAndType(int reg, String desc) {
        _type(_use(reg), desc);
    }

    final protected int totalReg;
    final protected String returnType;

    public TypeVisitor(int totalReg, String returnType) {
        super();
        this.totalReg = totalReg;
        this.returnType = returnType;
    }

    protected void newAndType(int reg, String desc) {
        T n = _new();
        _type(n, desc);
        _put(reg, n);
    }

    public abstract void _put(int reg, T n);

    public abstract T _new();

    public abstract T _use(int reg);

    public abstract void _type(T n, String desc);

    public abstract int _size(int i);

    public static String IFL = "_1IFL";
    public static String IL = "_3IL";
    public static String JD = "_2JD";
    public static String AIFL = "[_1IFL";
    public static String AJD = "[_2JD";

    static String[] descs = new String[] { IFL, JD, "L", "Z", "B", "C", "S", "I", "F", "J", "D" };
    static String[] descsArray = new String[] { AIFL, AJD, "[L", "[Z", "[B", "[C", "[S", "[I", "[F", "[J", "[D" };

    @Override
    public void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg, int xt) {
        useAndType(arrayReg, descsArray[xt]);
        useAndType(indexReg, "I");
        if (opcode == OP_AGET) {
            newAndType(formOrToReg, descs[xt]);
        } else {
            useAndType(formOrToReg, descs[xt]);
        }
    }

    @Override
    public void visitBinopLitXStmt(int opcode, int distReg, int srcReg, int content) {
        useAndType(srcReg, "I");
        newAndType(distReg, "I");
    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2, int xt) {
        useAndType(r1, "I");
        useAndType(r2, "I");
        newAndType(toReg, "I");
    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {
        if (opcode == OP_INSTANCE_OF) {
            useAndType(b, "L");
            newAndType(a, "Z");
        } else {
            useAndType(b, "I");
            newAndType(a, type);
        }
    }

    @Override
    public void visitClassStmt(int opcode, int saveTo, String type) {
        if (opcode == OP_CHECK_CAST) {
            useAndType(saveTo, "L");
        }
        newAndType(saveTo, type);
    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC, int xt) {
        useAndType(bB, descs[xt]);
        useAndType(cC, descs[xt]);
        newAndType(distReg, "I");
    }

    @Override
    public void visitConstStmt(int opcode, int toReg, Object value, int xt) {
        if (opcode == OP_CONST_CLASS) {
            newAndType(toReg, "Ljava/lang/Class;");
        } else if (opcode == OP_CONST_STRING) {
            newAndType(toReg, "Ljava/lang/String;");
        } else {
            newAndType(toReg, descs[xt]);
        }
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field, int xt) {
        if (opcode == OP_SGET) {
            newAndType(fromOrToReg, field.getType());
        } else {
            useAndType(fromOrToReg, field.getType());
        }
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field, int xt) {
        useAndType(objReg, field.getOwner());
        if (opcode == OP_IGET) {
            newAndType(fromOrToReg, field.getType());
        } else {
            useAndType(fromOrToReg, field.getType());
        }
    }

    @Override
    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        newAndType(aA, "[");
    }

    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] args, String type) {
        String eType = type.substring(1);
        for (int arg : args) {
            useAndType(arg, eType);
        }
        newAndType(totalReg, type);
    }

    @Override
    public void visitJumpStmt(int opcode, int a, int b, DexLabel label) {
        if (opcode == OP_IF_EQ || opcode == OP_IF_NE) {
            useAndType(a, IL);
            useAndType(b, IL);
        } else {
            useAndType(a, "I");
            useAndType(b, "I");
        }
    }

    @Override
    public void visitJumpStmt(int opcode, int a, DexLabel label) {
        if (opcode == OP_IF_EQZ || opcode == OP_IF_NEZ) {
            useAndType(a, IL);
        } else {
            useAndType(a, "I");
        }
    }

    @Override
    public void visitJumpStmt(int opcode, DexLabel label) {
    }

    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, DexLabel label, int[] cases, DexLabel[] labels) {
        useAndType(aA, "I");
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, Method method) {
        int i = 0;
        if (opcode != OP_INVOKE_STATIC) {
            useAndType(args[i], method.getOwner());
            i++;
        }
        for (String ps : method.getParameterTypes()) {
            useAndType(args[i], ps);
            i++;
        }
        if (!"V".equals(method.getReturnType())) {
            newAndType(totalReg, method.getReturnType());
        }
    }

    @Override
    public void visitMonitorStmt(int opcode, int reg) {
        useAndType(reg, "L");
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int xt) {
        if (opcode == OP_MOVE_RESULT) {
            useAndType(totalReg, descs[xt]);
            _put(totalReg, null);
        } else {
            useAndType(totalReg + 1, descs[xt]);
            _put(totalReg + 1, null);
        }
        newAndType(toReg, descs[xt]);
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg, int xt) {
        useAndType(fromReg, descs[xt]);
        newAndType(toReg, descs[xt]);
    }

    @Override
    public void visitReturnStmt(int opcode) {
    }

    @Override
    public void visitReturnStmt(int opcode, int reg, int xt) {
        if (opcode == OP_THROW) {
            useAndType(reg, "Ljava/lang/Throwable;");
        } else {
            useAndType(reg, this.returnType);
        }
    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {
        useAndType(aA, "I");
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xt) {
        if (opcode == OP_ARRAY_LENGTH) {
            useAndType(fromReg, "[");
            newAndType(toReg, "I");
        } else {
            useAndType(fromReg, descs[xt]);
            newAndType(toReg, descs[xt]);
        }
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xta, int xtb) {
        useAndType(fromReg, descs[xta]);
        newAndType(toReg, descs[xtb]);
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
    }

    @Override
    public void visitArguments(int total, int[] args) {
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public void visitLabel(DexLabel label) {
    }

    @Override
    public void visitLineNumber(int line, DexLabel label) {
    }

    @Override
    public void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg) {
    }

    @Override
    public void visitReturnStmt(int opcode, int cause, Object ref) {
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, int a) {
        for (int i = 0; i < args.length; i += _size(args[i])) {
            _use(args[i]);
        }
        _put(totalReg, _new());
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, int fieldoff, int xt) {
        useAndType(objReg, "L");
        if (opcode == OP_IGET_QUICK) {
            newAndType(fromOrToReg, descs[xt]);
        } else {
            useAndType(fromOrToReg, descs[xt]);
        }
    }

}
