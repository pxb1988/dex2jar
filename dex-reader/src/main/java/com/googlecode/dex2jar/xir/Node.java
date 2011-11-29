package com.googlecode.dex2jar.xir;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;
import com.googlecode.dex2jar.visitors.OdexCodeVisitor;

public class Node implements OdexOpcodes {
    public int opcode;
    public int a, b, c, d;
    public String type;
    public Object cst;
    public Field field;
    public Method method;
    public int args[];
    public DexLabel la, lb, lc;
    public DexLabel[] ls;

    public Node(int opcode, int a, int b, String type) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
        this.type = type;
    }

    public Node(int opcode, DexLabel la, DexLabel lb, DexLabel lc, String type) {
        super();
        this.opcode = opcode;
        this.la = la;
        this.lb = lb;
        this.lc = lc;
        this.type = type;
    }

    public Node(int opcode) {
        super();
        this.opcode = opcode;
    }

    public Node(int opcode, int a, int b, DexLabel la) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
        this.la = la;
    }

    public Node(int opcode, int a) {
        super();
        this.opcode = opcode;
        this.a = a;
    }

    public Node(int opcode, int a, int b) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
    }

    public Node(int opcode, int a, DexLabel la) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.la = la;
    }

    public Node(int opcode, DexLabel la) {
        super();
        this.opcode = opcode;
        this.la = la;
    }

    public Node(int opcode, int[] args, String type) {
        super();
        this.opcode = opcode;
        this.args = args;
        this.type = type;
    }

    public Node(int opcode, int[] args, Method method) {
        super();
        this.opcode = opcode;
        this.args = args;
        this.method = method;
    }

    public Node(int opcode, int[] args, int a) {
        super();
        this.opcode = opcode;
        this.args = args;
        this.a = a;
    }

    public Node(int opcode, int a, Object cst) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.cst = cst;
    }

    public Node(int opcode, int a, int b, Object cst) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
        this.cst = cst;
    }

    public Node(int opcode, int a, Object cst, int b) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
        this.cst = cst;
    }

    public Node(int opcode, int a, int b, Field f) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.field = f;
        this.b = b;
    }

    public Node(int opcode, int a, int b, int c, Field f) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
        this.field = f;
        this.c = c;
    }

    public Node(int opcode, int a, String s) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.type = s;
    }

    public Node(int opcode, int a, int b, int c, int d) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Node(int opcode, int a, int b, int c) {
        super();
        this.opcode = opcode;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Node(int opcode, int a, DexLabel la, int[] cst, DexLabel[] ls) {
        this.opcode = opcode;
        this.a = a;
        this.la = la;
        this.cst = cst;
        this.ls = ls;
    }

    public Node(int opcode, int a, DexLabel la, int b, int c, DexLabel[] ls) {
        this.opcode = opcode;
        this.a = a;
        this.la = la;
        this.b = b;
        this.c = c;
        this.ls = ls;
    }

    public void accept(OdexCodeVisitor dcv) {
        switch (opcode) {
        case CodeNode.OP_LABEL:
            dcv.visitLabel(la);
            break;
        case CodeNode.OP_TRYS:
            dcv.visitTryCatch(la, lb, lc, type);
            break;
        case OP_AGET:
        case OP_APUT:
            dcv.visitArrayStmt(opcode, a, b, c, d);
            break;
        case OP_ADD_INT_LIT_X:
        case OP_RSUB_INT_LIT_X:
        case OP_MUL_INT_LIT_X:
        case OP_DIV_INT_LIT_X:
        case OP_REM_INT_LIT_X:
        case OP_AND_INT_LIT_X:
        case OP_OR_INT_LIT_X:
        case OP_XOR_INT_LIT_X:
        case OP_SHL_INT_LIT_X:
        case OP_SHR_INT_LIT_X:
        case OP_USHR_INT_LIT_X:
            dcv.visitBinopLitXStmt(opcode, a, a, c);
            break;
        case OP_ADD:
        case OP_SUB:
        case OP_MUL:
        case OP_DIV:
        case OP_REM:
        case OP_AND:
        case OP_OR:
        case OP_XOR:
        case OP_SHL:
        case OP_SHR:
        case OP_USHR:
            dcv.visitBinopStmt(opcode, a, b, c, d);
            break;
        case OP_INSTANCE_OF:
        case OP_NEW_ARRAY:
            dcv.visitClassStmt(opcode, a, type);
            break;
        case OP_CHECK_CAST:
        case OP_NEW_INSTANCE:
            dcv.visitClassStmt(opcode, a, b, type);
            break;
        case OP_CMPL:
        case OP_CMPG:
        case OP_CMP:
            dcv.visitCmpStmt(opcode, a, b, c, d);
            break;
        case OP_CONST:
        case OP_CONST_WIDE:
        case OP_CONST_STRING:
        case OP_CONST_CLASS:
            dcv.visitConstStmt(opcode, a, cst, b);
            break;
        case OP_SGET:
        case OP_SPUT:
            dcv.visitFieldStmt(opcode, a, field, b);
            break;
        case OP_IGET:
        case OP_IPUT:
            dcv.visitFieldStmt(opcode, a, b, field, c);
            break;
        case OP_FILL_ARRAY_DATA:
            dcv.visitFillArrayStmt(opcode, a, b, c, (Object[]) cst);
            break;
        case OP_FILLED_NEW_ARRAY:
            dcv.visitFilledNewArrayStmt(opcode, args, type);
            break;
        case OP_IF_EQ:
        case OP_IF_NE:
        case OP_IF_LT:
        case OP_IF_GE:
        case OP_IF_GT:
        case OP_IF_LE:
            dcv.visitJumpStmt(opcode, a, b, la);
            break;
        case OP_IF_EQZ:
        case OP_IF_NEZ:
        case OP_IF_LTZ:
        case OP_IF_GEZ:
        case OP_IF_GTZ:
        case OP_IF_LEZ:
            dcv.visitJumpStmt(opcode, a, la);
            break;
        case OP_GOTO:
            dcv.visitJumpStmt(opcode, la);
            break;
        case OP_SPARSE_SWITCH:
            dcv.visitLookupSwitchStmt(opcode, a, la, args, ls);
            break;
        case OP_PACKED_SWITCH:
            dcv.visitTableSwitchStmt(opcode, a, la, b, c, ls);
            break;
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_SUPER:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_STATIC:
        case OP_INVOKE_INTERFACE:
            dcv.visitMethodStmt(opcode, args, a);
            break;
        case OP_MONITOR_ENTER:
        case OP_MONITOR_EXIT:
            dcv.visitMonitorStmt(opcode, a);
            break;
        case OP_MOVE_RESULT:
        case OP_MOVE_EXCEPTION:
            dcv.visitMoveStmt(opcode, a, b);
            break;
        case OP_MOVE:
            dcv.visitMoveStmt(opcode, a, b, c);
            break;
        case OP_RETURN_VOID:
            dcv.visitReturnStmt(opcode);
            break;
        case OP_RETURN:
        case OP_THROW:
            dcv.visitReturnStmt(opcode, a, b);
            break;
        case OP_ARRAY_LENGTH:
        case OP_NOT:
        case OP_NEG:
            dcv.visitUnopStmt(opcode, a, b, c);
            break;
        case OP_IGET_QUICK:
        case OP_IPUT_QUICK:
            dcv.visitFieldStmt(opcode, a, b, c, d);
            break;
        case OP_EXECUTE_INLINE:
        case OP_INVOKE_SUPER_QUICK:
        case OP_INVOKE_VIRTUAL_QUICK:
            dcv.visitMethodStmt(opcode, args, a);
            break;
        case OP_THROW_VERIFICATION_ERROR:
            dcv.visitReturnStmt(opcode, a, cst);
            break;
        case OP_X_TO_Y:
            dcv.visitUnopStmt(opcode, a, b, c, d);
            break;
        }
    }

    public String toString() {
        NodeDump nd = new NodeDump();
        this.accept(nd);
        return nd.toString();
    }

}
