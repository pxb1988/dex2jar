package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.ValueBox;

public class BinopExpr extends E2Expr {

    public BinopExpr(VT type, Value op1, Value op2) {
        super(type, new ValueBox(op1), new ValueBox(op2));
    }

    public String toString() {
        String x = "?";
        switch (vt) {
        case ADD:
            x = "+";
            break;
        case AND:
            x = "&";
            break;
        case CMP:
            x = "==";
            break;
        case CMPG:
            x = ">";
            break;
        case CMPL:
            x = "<";
            break;
        case DIV:
            x = "/";
            break;
        case EQ:
            x = "==";
            break;
        case GE:
            x = ">=";
            break;
        case GT:
            x = ">";
            break;
        case LE:
            x = "<=";
            break;
        case LT:
            x = "<";
            break;
        case MUL:
            x = "*";
            break;
        case NE:
            x = "!=";
            break;
        case OR:
            x = "|";
            break;
        case REM:
            x = "%";
            break;
        case SHL:
            x = "<<";
            break;
        case SHR:
            x = ">>";
            break;
        case SUB:
            x = "-";
            break;
        case USHR:
            x = ">>>";
            break;
        case XOR:
            x = "^";
            break;
        }

        return "(" + op1 + x + op2 + ")";
    }
}
