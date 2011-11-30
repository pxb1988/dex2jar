package com.googlecode.dex2jar.analysis;

import java.util.ArrayList;
import java.util.Stack;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.OdexOpcodes;

public abstract class Analyzer implements OdexOpcodes {

    private static void link(Node f, Node t) {
        if (!f._cfg_tos.contains(t)) {
            f._cfg_tos.add(t);
            t._cfg_froms++;
        }
    }

    private static boolean mayThrow(int opcode) {
        switch (opcode) {
        case OP_AGET:
        case OP_ARRAY_LENGTH:
        case OP_APUT:
        case OP_CHECK_CAST:
        case OP_CONST_CLASS:
        case OP_DIV:
        case OP_DIV_INT_LIT_X:
        case OP_EXECUTE_INLINE:
        case OP_IGET:
        case OP_IGET_QUICK:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_STATIC:
        case OP_INVOKE_SUPER:
        case OP_INVOKE_SUPER_QUICK:
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_VIRTUAL_QUICK:
        case OP_IPUT:
        case OP_IPUT_QUICK:
        case OP_SGET:
        case OP_SPUT:
        case OP_THROW:
        case OP_THROW_VERIFICATION_ERROR:
            return true;
        }
        return false;
    }

    public Analyzer(CodeNode cn) {
        super();
        this.cn = cn;

        this.totalReg = cn.total;
    }

    final protected CodeNode cn;

    final protected int totalReg;;

    public void analyze() {
        // clean
        for (Node p = cn.first; p != null; p = p.next) {
            p._cfg_froms = 0;
            if (p._cfg_tos != null) {
                p._cfg_tos.clear();
            } else {
                p._cfg_tos = new ArrayList<Node>(5);
            }
            p._cfg_visited = false;
            p.frame = null;
        }

        for (Node p = cn.first; p != null; p = p.next) {
            switch (p.opcode) {
            case OP_IF_EQZ:
            case OP_IF_NEZ:
            case OP_IF_LTZ:
            case OP_IF_GEZ:
            case OP_IF_GTZ:
            case OP_IF_LEZ:
            case OP_IF_EQ:
            case OP_IF_NE:
            case OP_IF_LT:
            case OP_IF_GE:
            case OP_IF_GT:
            case OP_IF_LE:
                link(p, p.next);
                link(p, (Node) p.la.info);
                break;
            case OP_GOTO:
                link(p, (Node) p.la.info);
                break;
            case OP_PACKED_SWITCH:
            case OP_SPARSE_SWITCH:
                link(p, (Node) p.la.info);
                for (DexLabel dl : p.ls) {
                    link(p, (Node) dl.info);
                }
                break;
            case OP_RETURN:
            case OP_RETURN_VOID:
            case OP_THROW:
            case OP_THROW_VERIFICATION_ERROR:
                break;
            default:
                if (p.opcode == CodeNode.OP_LABEL && p.next == null) {
                    // ignore last label
                } else {
                    link(p, p.next);
                }
                break;
            }
        }

        for (Node t : cn.trys) {
            Node endNode = (Node) t.lb.info;
            Node handlerNode = (Node) t.lc.info;
            Node pre = (Node) t.la.info;
            for (Node p = pre.next; p != endNode; p = p.next) {
                if (mayThrow(p.opcode)) {
                    link(pre, handlerNode);
                }
                pre = p;
            }
            handlerNode.frame = createExceptionHandlerFrame(handlerNode, t.type == null ? "Ljava/lang/Throwable;"
                    : t.type);
        }

        Node p = cn.first;
        p.frame = initFirstFrame(p);
        Stack<Node> stack = new Stack<Node>();

        // System.out.println();

        stack.push(p);
        while (!stack.empty()) {
            p = stack.pop();
            if (p._cfg_visited) {
                continue;
            }
            p._cfg_visited = true;
            // System.out.print(p);
            Object frame = exec(p);
            for (Node x : p._cfg_tos) {
                merge(frame, x);
                stack.push(x);
            }

        }
    }

    protected abstract Object exec(Node p);

    protected abstract void merge(Object frame, Node target);

    protected abstract Object initFirstFrame(Node p);

    protected abstract Object createExceptionHandlerFrame(Node handlerNode, String string);

}
