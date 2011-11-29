package com.googlecode.dex2jar.xir;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.OdexCodeVisitor;

public class CodeNode implements OdexCodeVisitor {

    public static final int OP_LABEL = -1;
    public static final int OP_TRYS = -2;

    public List<Node> insns = new ArrayList<Node>();

    public List<Node> trys = new ArrayList<Node>(5);

    int total;
    int args[];

    // public List<Insn> lines = new ArrayList(5);

    @Override
    public void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg, int xt) {
        insns.add(new Node(opcode, formOrToReg, arrayReg, indexReg, xt));
    }

    @Override
    public void visitBinopLitXStmt(int opcode, int distReg, int srcReg, int content) {
        insns.add(new Node(opcode, distReg, srcReg, content));
    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2, int xt) {
        insns.add(new Node(opcode, toReg, r1, r2, xt));
    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {
        insns.add(new Node(opcode, a, b, type));
    }

    @Override
    public void visitClassStmt(int opcode, int saveTo, String type) {
        insns.add(new Node(opcode, saveTo, type));
    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC, int xt) {
        insns.add(new Node(opcode, distReg, bB, cC, xt));
    }

    @Override
    public void visitConstStmt(int opcode, int toReg, Object value, int xt) {
        insns.add(new Node(opcode, toReg, value, xt));
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field, int xt) {
        insns.add(new Node(opcode, fromOrToReg, xt, field));
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field, int xt) {
        insns.add(new Node(opcode, fromOrToReg, objReg, xt, field));
    }

    @Override
    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        insns.add(new Node(opcode, aA, elemWidth, values));
    }

    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] args, String type) {
        insns.add(new Node(opcode, args, type));
    }

    @Override
    public void visitJumpStmt(int opcode, int a, int b, DexLabel label) {
        insns.add(new Node(opcode, a, b, label));
    }

    @Override
    public void visitJumpStmt(int opcode, int reg, DexLabel label) {
        insns.add(new Node(opcode, reg, label));
    }

    @Override
    public void visitJumpStmt(int opcode, DexLabel label) {
        insns.add(new Node(opcode, label));
    }

    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, DexLabel label, int[] cases, DexLabel[] labels) {
        insns.add(new Node(opcode, aA, label, cases, labels));
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, Method method) {
        insns.add(new Node(opcode, args, method));
    }

    @Override
    public void visitMonitorStmt(int opcode, int reg) {
        insns.add(new Node(opcode, reg));
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int xt) {
        insns.add(new Node(opcode, toReg, xt));
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg, int xt) {
        insns.add(new Node(opcode, toReg, fromReg, xt));
    }

    @Override
    public void visitReturnStmt(int opcode) {
        insns.add(new Node(opcode));
    }

    @Override
    public void visitReturnStmt(int opcode, int reg, int xt) {
        insns.add(new Node(opcode, reg, xt));
    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {
        insns.add(new Node(opcode, aA, label, first_case, last_case, labels));
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xt) {
        insns.add(new Node(opcode, toReg, fromReg, xt));
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
        trys.add(new Node(OP_TRYS, start, end, handler, type));
    }

    @Override
    public void visitArguments(int total, int[] args) {
        this.total = total;
        this.args = args;
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public void visitLabel(DexLabel label) {
        insns.add(new Node(OP_LABEL, label));
    }

    @Override
    public void visitLineNumber(int line, DexLabel label) {
        // lines.add(new Insn(OP_LINE, label));
        // TODO Auto-generated method stub
    }

    @Override
    public void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitReturnStmt(int opcode, int cause, Object ref) {
        insns.add(new Node(opcode, cause, ref));
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, int a) {
        insns.add(new Node(opcode, args, a));
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, int fieldoff, int xt) {
        insns.add(new Node(opcode, fromOrToReg, objReg, fieldoff, xt));
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xta, int xtb) {
        insns.add(new Node(opcode, fromReg, xta, xtb));
    }

}
