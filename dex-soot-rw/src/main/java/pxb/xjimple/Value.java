package pxb.xjimple;

public abstract class Value {
    public static enum VT {

        ADD, AND, ARRAY, CAST, CMP, CMPG, CMPL, DIV, EQ, FIELD, GE, GT, INSTANCEOF, INTERFACE_METHOD, LE, LENGTH, LT, MUL, NE, NEG, NEW, NEW_ARRAY, NEW_MUTI_ARRAY, OR, REM, SHL, SHR, SPECIAL_METHOD, //
        STATIC_METHOD, SUB, USHR, VIRTUAL_METHOD, XOR, LOCAL, CONSTANT, THIS_REF, PARAMETER_REF, EXCEPTION_REF
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
