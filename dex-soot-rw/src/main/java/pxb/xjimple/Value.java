package pxb.xjimple;

public abstract class Value {
    public static enum VT {

        ADD, AND, ARRAY, CAST, CMP, CMPG, CMPL, DIV, EQ, FIELD, GE, GT, INSTANCEOF, INVOKE_INTERFACE, LE, LENGTH, LT, MUL, NE, NEG, NEW_ARRAY, NEW_MUTI_ARRAY, OR, REM, SHL, SHR, INVOKE_SPECIAL, //
        INVOKE_STATIC, SUB, USHR, INVOKE_VIRTUAL, INVOKE_NEW, XOR, LOCAL, CONSTANT, THIS_REF, PARAMETER_REF, EXCEPTION_REF
    }

    final public VT vt;

    /**
     * @param vt
     */
    public Value(VT vt) {
        super();
        this.vt = vt;
    }
}
