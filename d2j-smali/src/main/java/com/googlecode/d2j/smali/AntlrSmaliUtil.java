package com.googlecode.d2j.smali;

import com.googlecode.d2j.*;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.smali.antlr4.SmaliBaseVisitor;
import com.googlecode.d2j.smali.antlr4.SmaliLexer;
import com.googlecode.d2j.smali.antlr4.SmaliParser;
import com.googlecode.d2j.visitors.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.d2j.smali.Utils.*;

public class AntlrSmaliUtil {
    public static void acceptFile(SmaliParser.SFileContext ctx, DexFileVisitor dexFileVisitor) {
        DexClassVisitor dexClassVisitor;
        String className = Utils.unEscapeId(ctx.className.getText());
        int access = collectAccess(ctx.sAccList());
        List<SmaliParser.SSuperContext> superContexts = ctx.sSuper();
        String superClass = null;
        if (superContexts.size() > 0) {
            superClass = Utils.unEscapeId(superContexts.get(superContexts.size() - 1).name.getText());
        }
        List<SmaliParser.SInterfaceContext> itfs = ctx.sInterface();
        String[] interfaceNames = null;
        if (itfs.size() > 0) {
            interfaceNames = new String[itfs.size()];
            for (int i = 0; i < itfs.size(); i++) {
                interfaceNames[i] = Utils.unEscapeId(itfs.get(i).name.getText());
            }
        }

        dexClassVisitor = dexFileVisitor.visit(access, className, superClass, interfaceNames);

        List<SmaliParser.SSourceContext> sources = ctx.sSource();
        if (sources.size() > 0) {
            dexClassVisitor.visitSource(
                    Utils.unescapeStr(sources.get(sources.size() - 1).src.getText())
            );
        }
        acceptAnnotations(ctx.sAnnotation(), dexClassVisitor);
        acceptField(ctx.sField(), className, dexClassVisitor);
        acceptMethod(ctx.sMethod(), className, dexClassVisitor);

        dexClassVisitor.visitEnd();
    }

    private static void acceptMethod(List<SmaliParser.SMethodContext> sMethodContexts, String className, DexClassVisitor dexClassVisitor) {
        if (dexClassVisitor == null || sMethodContexts == null || sMethodContexts.size() == 0) {
            return;
        }
        for (SmaliParser.SMethodContext ctx : sMethodContexts) {
            acceptMethod(ctx, className, dexClassVisitor);
        }
    }

    public static void acceptMethod(SmaliParser.SMethodContext ctx, String className, DexClassVisitor dexClassVisitor) {
        Method method;
        Token methodObj = ctx.methodObj;
        if (methodObj.getType() == SmaliLexer.METHOD_FULL) {
            method = Utils.parseMethodAndUnescape(methodObj.getText());
        } else {// PART
            method = Utils.parseMethodAndUnescape(className, methodObj.getText());
        }
        int access = collectAccess(ctx.sAccList());
        boolean isStatic = 0 != (access & DexConstants.ACC_STATIC);
        DexMethodVisitor dexMethodVisitor = dexClassVisitor.visitMethod(access, method);
        if (dexMethodVisitor != null) {
            acceptAnnotations(ctx.sAnnotation(), dexMethodVisitor);
            int ins = Utils.methodIns(method, isStatic);
            int totalRegisters = findTotalRegisters(ctx, ins);
            if (totalRegisters < 0) {
                totalRegisters = ins;
            }
            M m = new M(method, totalRegisters, ins, isStatic);
            acceptParameter(ctx.sParameter(), m, dexMethodVisitor);
            acceptCode(ctx, m, dexMethodVisitor);
            dexMethodVisitor.visitEnd();
        }
    }

    private static class M {
        int locals;
        String[] paramNames;
        int map[];
        public int total;

        void setNameByIdx(int index, String name) {
            if (index >= 0 && index < paramNames.length) {
                paramNames[index] = name;
            }
        }

        int regToParamIdx(int reg) {
            int x = reg - locals;
            if (x >= 0 && x < map.length) {
                return map[x];
            }
            return 0;
        }

        int pareReg(String str) {
            char f = Character.toLowerCase(str.charAt(0));
            if (f == 'p') {
                return parseInt(str.substring(1)) + locals;
            } else {
                return parseInt(str.substring(1));
            }
        }

        M(Method method, int totals, int ins, boolean isStatic) {
            this.locals = totals - ins;
            this.total = totals;
            String paramTypes[] = method.getParameterTypes();
            paramNames = new String[paramTypes.length];
            map = new int[ins];
            int start = 0;
            if (!isStatic) {
                map[start] = -1;
                start++;
            }

            for (int i = 0; i < paramTypes.length; i++) {
                char t = paramTypes[i].charAt(0);
                map[start++] = i;
                if (t == 'J' || t == 'D') {
                    map[start++] = i;
                }
            }
        }
    }

    private static void acceptCode(SmaliParser.SMethodContext ctx, final M m, DexMethodVisitor dexMethodVisitor) {
        if (ctx == null || dexMethodVisitor == null) {
            return;
        }
        final DexCodeVisitor dexCodeVisitor = dexMethodVisitor.visitCode();
        if (dexCodeVisitor == null) {
            return;
        }
        final SmaliCodeVisitor scv = new SmaliCodeVisitor(dexCodeVisitor);
        final DexDebugVisitor dexDebugVisitor = scv.visitDebug();
        final List<SmaliParser.SInstructionContext> instructionContexts = ctx.sInstruction();
        final SmaliBaseVisitor v = new SmaliBaseVisitor() {
            @Override
            public Object visitFregisters(SmaliParser.FregistersContext ctx) {
                return null;
            }

            @Override
            public Object visitFlocals(SmaliParser.FlocalsContext ctx) {
                return null;
            }

            @Override
            public Object visitFline(SmaliParser.FlineContext ctx) {
                if (dexDebugVisitor != null) {
                    DexLabel dexLabel = new DexLabel();
                    scv.visitLabel(dexLabel);
                    dexDebugVisitor.visitLineNumber(Utils.parseInt(ctx.line.getText()), dexLabel);
                }
                return null;
            }

            @Override
            public Object visitFend(SmaliParser.FendContext ctx) {
                if (dexDebugVisitor != null) {
                    DexLabel dexLabel = new DexLabel();
                    scv.visitLabel(dexLabel);
                    int reg = m.pareReg(ctx.r.getText());
                    dexDebugVisitor.visitEndLocal(reg, dexLabel);
                }
                return null;
            }

            @Override
            public Object visitFlocal(SmaliParser.FlocalContext ctx) {
                if (dexDebugVisitor != null) {
                    DexLabel dexLabel = new DexLabel();
                    scv.visitLabel(dexLabel);
                    int reg = m.pareReg(ctx.r.getText());
                    String name;
                    String type;
                    if (ctx.v1 != null) {
                        Field fld = parseFieldAndUnescape("Lt;", ctx.v1.getText());
                        name = fld.getName();
                        type = fld.getType();
                    } else if (ctx.v2 != null) {
                        String txt = ctx.v2.getText();
                        int i = findString(txt, 1, txt.length(), '\"');
                        name = unescapeStr(txt.substring(0, i + 1));
                        type = unEscapeId(txt.substring(i + 2));
                    } else {
                        if (ctx.name2 != null) {
                            name = unescapeStr(ctx.name2.getText());
                        } else {
                            name = unEscapeId(ctx.name1.getText());
                        }
                        type = unEscapeId(ctx.type.getText());
                    }
                    String sig = ctx.sig == null ? null : unescapeStr(ctx.sig.getText());
                    dexDebugVisitor.visitStartLocal(reg, dexLabel, name, type, sig);
                }
                return null;
            }

            @Override
            public Object visitFrestart(SmaliParser.FrestartContext ctx) {
                if (dexDebugVisitor != null) {
                    DexLabel dexLabel = new DexLabel();
                    scv.visitLabel(dexLabel);
                    int reg = m.pareReg(ctx.r.getText());
                    dexDebugVisitor.visitRestartLocal(reg, dexLabel);
                }
                return null;
            }

            @Override
            public Object visitFprologue(SmaliParser.FprologueContext ctx) {
                if (dexDebugVisitor != null) {
                    DexLabel dexLabel = new DexLabel();
                    scv.visitLabel(dexLabel);
                    dexDebugVisitor.visitPrologue(dexLabel);
                }
                return null;
            }

            Map<String, DexLabel> labelMap = new HashMap<>();

            @Override
            public Object visitSLabel(SmaliParser.SLabelContext ctx) {
                scv.visitLabel(getLabel(ctx.label.getText()));
                return null;
            }

            @Override
            public Object visitFspareswitch(SmaliParser.FspareswitchContext ctx) {
                List<TerminalNode> ints = ctx.INT();
                List<TerminalNode> ts = ctx.LABEL();
                int cases[] = new int[ts.size()];
                DexLabel labels[] = new DexLabel[ts.size()];
                for (int i = 0; i < ts.size(); i++) {
                    cases[i] = parseInt(ints.get(i).getSymbol().getText());
                    labels[i] = getLabel(ts.get(i).getSymbol().getText());
                }
                scv.dSparseSwitch(cases, labels);
                return null;
            }

            @Override
            public Object visitFarraydata(SmaliParser.FarraydataContext ctx) {
                int size = parseInt(ctx.size.getText());
                List<SmaliParser.SBaseValueContext> ts = ctx.sBaseValue();
                byte[] ps = new byte[ts.size()];
                for (int i = 0; i < ts.size(); i++) {
                    ps[i] = ((Number) parseBaseValue(ts.get(i))).byteValue();
                }
                scv.dArrayData(size, ps);
                return null;
            }

            Op getOp(Token t) {
                return Utils.getOp(t.getText());
            }

            @Override
            public Object visitF0x(SmaliParser.F0xContext ctx) {
                scv.visitStmt0R(getOp(ctx.op));
                return null;
            }

            @Override
            public Object visitF0t(SmaliParser.F0tContext ctx) {
                scv.visitJumpStmt(getOp(ctx.op), 0, 0, getLabel(ctx.target.getText()));
                return null;
            }

            @Override
            public Object visitF1x(SmaliParser.F1xContext ctx) {
                scv.visitStmt1R(getOp(ctx.op), m.pareReg(ctx.r1.getText()));
                return null;
            }

            @Override
            public Object visitFconst(SmaliParser.FconstContext ctx) {
                Op op = getOp(ctx.op);
                int r = m.pareReg(ctx.r1.getText());
                Token cst = ctx.cst;

                switch (op) {
                    case CONST_STRING:
                    case CONST_STRING_JUMBO:
                        scv.visitConstStmt(op, r, unescapeStr(cst.getText()));
                        break;
                    case CONST_CLASS:
                        scv.visitConstStmt(op, r, new DexType(unEscapeId(cst.getText())));
                        break;
                    case CHECK_CAST:
                    case NEW_INSTANCE:
                        scv.visitTypeStmt(op, r, 0, unEscapeId(cst.getText()));
                        break;
                    case CONST_WIDE:
                        scv.visitConstStmt(op, r, cst.getType() == SmaliLexer.INT ? ((long) parseInt(cst.getText())) : parseLong(cst.getText()));
                        break;
                    case CONST_WIDE_16: {
                        long v;
                        if (cst.getType() == SmaliLexer.LONG) {
                            v = parseLong(cst.getText());
                        } else {

                            v = (short) parseInt(cst.getText());
                        }
                        scv.visitConstStmt(op, r, v);
                    }
                    break;
                    case CONST_WIDE_32: {
                        long v;
                        if (cst.getType() == SmaliLexer.LONG) {
                            v = parseLong(cst.getText());
                        } else {
                            v = parseInt(cst.getText());
                        }
                        scv.visitConstStmt(op, r, v);
                    }
                    break;
                    case CONST_WIDE_HIGH16: {
                        long v;
                        if (cst.getType() == SmaliLexer.LONG) {
                            v = parseLong(cst.getText());
                        } else {
                            v = (short) parseInt(cst.getText());
                            v <<= 48;
                        }
                        scv.visitConstStmt(op, r, v);
                    }
                    break;
                    case CONST:
                    case CONST_4:
                    case CONST_16: {
                        int v = parseInt(cst.getText());
                        scv.visitConstStmt(op, r, v);
                    }
                    break;
                    case CONST_HIGH16: {
                        int v = parseInt(cst.getText());
                        v <<= 16;
                        scv.visitConstStmt(op, r, v);
                    }
                    break;
                    default:
                        throw new RuntimeException();
                }
                return null;
            }

            @Override
            public Object visitFf1c(SmaliParser.Ff1cContext ctx) {
                int r = m.pareReg(ctx.r1.getText());
                Field field = parseFieldAndUnescape(ctx.fld.getText());
                scv.visitFieldStmt(getOp(ctx.op), r, 0, field);
                return null;
            }

            @Override
            public Object visitFt2c(SmaliParser.Ft2cContext ctx) {
                int r1 = m.pareReg(ctx.r1.getText());
                int r2 = m.pareReg(ctx.r2.getText());
                scv.visitTypeStmt(getOp(ctx.op), r1, r2, unEscapeId(ctx.type.getText()));
                return null;
            }

            @Override
            public Object visitFf2c(SmaliParser.Ff2cContext ctx) {
                int r1 = m.pareReg(ctx.r1.getText());
                int r2 = m.pareReg(ctx.r2.getText());
                scv.visitFieldStmt(getOp(ctx.op), r1, r2, parseFieldAndUnescape(ctx.fld.getText()));
                return null;
            }

            @Override
            public Object visitF2x(SmaliParser.F2xContext ctx) {
                int r1 = m.pareReg(ctx.r1.getText());
                int r2 = m.pareReg(ctx.r2.getText());
                scv.visitStmt2R(getOp(ctx.op), r1, r2);
                return null;
            }

            @Override
            public Object visitF3x(SmaliParser.F3xContext ctx) {
                int r1 = m.pareReg(ctx.r1.getText());
                int r2 = m.pareReg(ctx.r2.getText());
                int r3 = m.pareReg(ctx.r3.getText());
                scv.visitStmt3R(getOp(ctx.op), r1, r2, r3);
                return null;
            }

            @Override
            public Object visitFt5c(SmaliParser.Ft5cContext ctx) {
                Op op = getOp(ctx.op);

                List<TerminalNode> ts = ctx.REGISTER();
                int rs[] = new int[ts.size()];
                for (int i = 0; i < ts.size(); i++) {
                    rs[i] = m.pareReg(ts.get(i).getSymbol().getText());
                }
                scv.visitFilledNewArrayStmt(op, rs, unEscapeId(ctx.type.getText()));
                return null;
            }

            @Override
            public Object visitFm5c(SmaliParser.Fm5cContext ctx) {
                Op op = getOp(ctx.op);

                List<TerminalNode> ts = ctx.REGISTER();
                int rs[] = new int[ts.size()];
                for (int i = 0; i < ts.size(); i++) {
                    rs[i] = m.pareReg(ts.get(i).getSymbol().getText());
                }
                scv.visitMethodStmt(op, rs, parseMethodAndUnescape(ctx.method.getText()));
                return null;
            }

            @Override
            public Object visitFmrc(SmaliParser.FmrcContext ctx) {
                if (ctx.rstart != null) {
                    int start = m.pareReg(ctx.rstart.getText());
                    int end = m.pareReg(ctx.rend.getText());
                    int size = end - start + 1;
                    int rs[] = new int[size];
                    for (int i = 0; i < size; i++) {
                        rs[i] = start + i;
                    }
                    scv.visitMethodStmt(getOp(ctx.op), rs, parseMethodAndUnescape(ctx.method.getText()));
                } else {
                    scv.visitMethodStmt(getOp(ctx.op), new int[0], parseMethodAndUnescape(ctx.method.getText()));
                }
                return null;
            }

            @Override
            public Object visitFtrc(SmaliParser.FtrcContext ctx) {
                if (ctx.rstart != null) {
                    int start = m.pareReg(ctx.rstart.getText());
                    int end = m.pareReg(ctx.rend.getText());
                    int size = end - start + 1;
                    int rs[] = new int[size];
                    for (int i = 0; i < size; i++) {
                        rs[i] = start + i;
                    }
                    scv.visitFilledNewArrayStmt(getOp(ctx.op), rs, unEscapeId(ctx.type.getText()));
                } else {
                    scv.visitFilledNewArrayStmt(getOp(ctx.op), new int[0], unEscapeId(ctx.type.getText()));
                }
                return null;
            }

            @Override
            public Object visitF31t(SmaliParser.F31tContext ctx) {
                scv.visitF31tStmt(getOp(ctx.op), m.pareReg(ctx.r1.getText()), getLabel(ctx.label.getText()));
                return null;
            }

            @Override
            public Object visitF1t(SmaliParser.F1tContext ctx) {
                scv.visitJumpStmt(getOp(ctx.op), m.pareReg(ctx.r1.getText()), 0, getLabel(ctx.label.getText()));
                return null;
            }

            @Override
            public Object visitF2t(SmaliParser.F2tContext ctx) {
                scv.visitJumpStmt(getOp(ctx.op), m.pareReg(ctx.r1.getText()), m.pareReg(ctx.r2.getText()), getLabel(ctx.label.getText()));
                return null;
            }

            @Override
            public Object visitF2sb(SmaliParser.F2sbContext ctx) {
                scv.visitStmt2R1N(getOp(ctx.op), m.pareReg(ctx.r1.getText()), m.pareReg(ctx.r2.getText()), parseInt(ctx.lit.getText()));
                return null;
            }

            @Override
            public Object visitFpackageswitch(SmaliParser.FpackageswitchContext ctx) {
                int start = parseInt(ctx.start.getText());
                List<TerminalNode> ts = ctx.LABEL();
                DexLabel labels[] = new DexLabel[ts.size()];
                for (int i = 0; i < ts.size(); i++) {
                    labels[i] = getLabel(ts.get(i).getSymbol().getText());
                }
                scv.dPackedSwitch(start, labels);
                return null;
            }

            @Override
            public Object visitFcache(SmaliParser.FcacheContext ctx) {
                scv.visitTryCatch(getLabel(ctx.start.getText()), getLabel(ctx.end.getText()),
                        new DexLabel[]{getLabel(ctx.handle.getText())},
                        new String[]{unEscapeId(ctx.type.getText())}
                );
                return null;
            }

            @Override
            public Object visitFcacheall(SmaliParser.FcacheallContext ctx) {
                scv.visitTryCatch(getLabel(ctx.start.getText()), getLabel(ctx.end.getText()),
                        new DexLabel[]{getLabel(ctx.handle.getText())},
                        new String[]{null}
                );
                return null;
            }

            DexLabel getLabel(String name) {
                DexLabel dexLabel = labelMap.get(name);
                if (dexLabel == null) {
                    dexLabel = new DexLabel();
                    labelMap.put(name, dexLabel);
                }
                return dexLabel;
            }

            @Override
            public Object visitFepiogue(SmaliParser.FepiogueContext ctx) {
                if (dexDebugVisitor != null) {
                    DexLabel dexLabel = new DexLabel();
                    scv.visitLabel(dexLabel);
                    dexDebugVisitor.visitEpiogue(dexLabel);
                }
                return null;
            }
        };
        scv.visitRegister(m.total);
        if (dexDebugVisitor != null) {
            for (int i = 0; i < m.paramNames.length; i++) {
                String name = m.paramNames[i];
                if (name != null) {
                    dexDebugVisitor.visitParameterName(i, name);
                }
            }
        }
        for (SmaliParser.SInstructionContext instructionContext : instructionContexts) {
            ParserRuleContext parserRuleContext = (ParserRuleContext) instructionContext.getChild(0);
            parserRuleContext.accept(v);
        }
        scv.visitEnd();
    }

    private static int findTotalRegisters(SmaliParser.SMethodContext ctx, int ins) {
        int totalRegisters = -1;
        List<SmaliParser.SInstructionContext> instructionContexts = ctx.sInstruction();
        for (SmaliParser.SInstructionContext instructionContext : instructionContexts) {
            ParserRuleContext parserRuleContext = (ParserRuleContext) instructionContext.getChild(0);
            if (parserRuleContext != null) {
                int ruleIndex = parserRuleContext.getRuleIndex();
                if (ruleIndex == SmaliParser.RULE_fregisters) {
                    totalRegisters = parseInt(((SmaliParser.FregistersContext) parserRuleContext).xregisters.getText());
                    break;
                } else if (ruleIndex == SmaliParser.RULE_flocals) {
                    totalRegisters = ins + parseInt(((SmaliParser.FlocalsContext) parserRuleContext).xlocals.getText());
                    break;
                }
            }
        }
        return totalRegisters;
    }

    private static void acceptParameter(List<SmaliParser.SParameterContext> sParameterContexts, M m, DexMethodVisitor dexMethodVisitor) {
        if (sParameterContexts == null || sParameterContexts.size() == 0 || dexMethodVisitor == null) {
            return;
        }
        boolean hasParam = false;
        boolean hasParamter = false;
        for (SmaliParser.SParameterContext ctx : sParameterContexts) {
            if (ctx.param != null) {
                hasParam = true;
            }
            if (ctx.parameter != null) {
                hasParamter = true;
            }
        }
        if (hasParam && hasParamter) {
            throw new RuntimeException("cant mix use .param and .parameter on method");
        }
        for (int i = 0; i < sParameterContexts.size(); i++) {
            SmaliParser.SParameterContext ctx = sParameterContexts.get(i);
            int index;
            if (ctx.param != null) {
                index = m.regToParamIdx(m.pareReg(ctx.r.getText()));
            } else {
                index = i;
            }
            if (ctx.name != null) {
                m.setNameByIdx(index, unescapeStr(ctx.name.getText()));
            }
            List<SmaliParser.SAnnotationContext> annotationContexts = ctx.sAnnotation();
            if (annotationContexts.size() > 0) {
                acceptAnnotations(annotationContexts, dexMethodVisitor.visitParameterAnnotation(index));
            }
        }


    }

    private static void acceptField(List<SmaliParser.SFieldContext> sFieldContexts, String className, DexClassVisitor dexClassVisitor) {
        if (sFieldContexts == null || sFieldContexts.size() == 0 || dexClassVisitor == null) {
            return;
        }
        for (SmaliParser.SFieldContext ctx : sFieldContexts) {
            acceptField(ctx, className, dexClassVisitor);
        }
    }

    public static void acceptField(SmaliParser.SFieldContext ctx, String className, DexClassVisitor dexClassVisitor) {
        Field field;
        Token fieldObj = ctx.fieldObj;
        if (fieldObj.getType() == SmaliLexer.FIELD_FULL) {
            field = Utils.parseFieldAndUnescape(fieldObj.getText());
        } else {
            field = Utils.parseFieldAndUnescape(className, fieldObj.getText());
        }
        int access = collectAccess(ctx.sAccList());
        Object value = null;
        SmaliParser.SBaseValueContext vctx = ctx.sBaseValue();
        if (vctx != null) {
            value = parseBaseValue(vctx);
        }
        DexFieldVisitor dexFieldVisitor = dexClassVisitor.visitField(access, field, value);
        if (dexFieldVisitor != null) {
            acceptAnnotations(ctx.sAnnotation(), dexFieldVisitor);
            dexFieldVisitor.visitEnd();
        }
    }

    private static Object parseBaseValue(SmaliParser.SBaseValueContext ctx) {
        Token value;
        if (ctx.getChildCount() == 1) {
            TerminalNode tn = (TerminalNode) ctx.getChild(0);
            value = tn.getSymbol();
        } else {
            TerminalNode tn = (TerminalNode) ctx.getChild(1);
            value = tn.getSymbol();
        }
        switch (value.getType()) {
            case SmaliLexer.STRING:
                return unescapeStr(value.getText());
            case SmaliLexer.BOOLEAN:
                return "true".equals(value.getText());
            case SmaliLexer.BYTE:
                return parseByte(value.getText());
            case SmaliLexer.SHORT:
                return parseShort(value.getText());
            case SmaliLexer.CHAR:
                return unescapeChar(value.getText());
            case SmaliLexer.INT:
                return parseInt(value.getText());
            case SmaliLexer.LONG:
                return parseLong(value.getText());

            case SmaliLexer.BASE_FLOAT:
            case SmaliLexer.FLOAT_INFINITY:
            case SmaliLexer.FLOAT_NAN:
                return parseFloat(value.getText());
            case SmaliLexer.BASE_DOUBLE:
            case SmaliLexer.DOUBLE_INFINITY:
            case SmaliLexer.DOUBLE_NAN:
                return parseDouble(value.getText());

            case SmaliLexer.METHOD_FULL:
                return parseMethodAndUnescape(value.getText());
            case SmaliLexer.OBJECT_TYPE:
                return new DexType(unEscapeId(value.getText()));
            case SmaliLexer.NULL:
                return null;
            case SmaliLexer.FIELD_FULL:
                return parseFieldAndUnescape(value.getText());
        }
        return null;
    }

    private static void acceptAnnotations(List<SmaliParser.SAnnotationContext> sAnnotationContexts, DexAnnotationAble dexAnnotationAble) {
        if (dexAnnotationAble == null) {
            return;
        }
        if (sAnnotationContexts.size() > 0) {
            for (SmaliParser.SAnnotationContext ctx : sAnnotationContexts) {
                Visibility visibility = Utils.getAnnVisibility(ctx.visibility.getText());
                String type = Utils.unEscapeId(ctx.type.getText());
                DexAnnotationVisitor dexAnnotationVisitor = dexAnnotationAble.visitAnnotation(type, visibility);
                if (dexAnnotationVisitor != null) {
                    List<SmaliParser.SAnnotationKeyNameContext> keys = ctx.sAnnotationKeyName();
                    if (keys.size() > 0) {
                        List<SmaliParser.SAnnotationValueContext> values = ctx.sAnnotationValue();
                        for (int i = 0; i < keys.size(); i++) {
                            acceptAnnotation(dexAnnotationVisitor, Utils.unEscapeId(keys.get(i).getText()), values.get(i));
                        }
                    }
                    dexAnnotationVisitor.visitEnd();
                }
            }
        }
    }

    private static void acceptAnnotation(DexAnnotationVisitor dexAnnotationVisitor, String name, SmaliParser.SAnnotationValueContext ctx) {
        ParserRuleContext t = (ParserRuleContext) ctx.getChild(0);
        switch (t.getRuleIndex()) {
            case SmaliParser.RULE_sSubannotation: {
                SmaliParser.SSubannotationContext subannotationContext = (SmaliParser.SSubannotationContext) t;
                DexAnnotationVisitor annotationVisitor = dexAnnotationVisitor.visitAnnotation(name, Utils
                        .unEscapeId(subannotationContext.type.getText()));
                if (annotationVisitor != null) {
                    List<SmaliParser.SAnnotationKeyNameContext> keys = subannotationContext.sAnnotationKeyName();
                    if (keys.size() > 0) {
                        List<SmaliParser.SAnnotationValueContext> values = subannotationContext.sAnnotationValue();
                        for (int i = 0; i < keys.size(); i++) {
                            acceptAnnotation(annotationVisitor, Utils.unEscapeId(keys.get(i).getText()), values.get(i));
                        }
                    }
                    annotationVisitor.visitEnd();
                }
                break;
            }
            case SmaliParser.RULE_sArrayValue: {
                SmaliParser.SArrayValueContext arrayValueContext = (SmaliParser.SArrayValueContext) t;
                DexAnnotationVisitor annotationVisitor = dexAnnotationVisitor.visitArray(name);
                if (annotationVisitor != null) {
                    for (SmaliParser.SAnnotationValueContext annotationValueContext : arrayValueContext
                            .sAnnotationValue()) {
                        acceptAnnotation(annotationVisitor, null, annotationValueContext);
                    }
                    annotationVisitor.visitEnd();
                }
                break;
            }
            case SmaliParser.RULE_sBaseValue:
                SmaliParser.SBaseValueContext baseValueContext = (SmaliParser.SBaseValueContext) t;
                Object value = parseBaseValue(baseValueContext);
                dexAnnotationVisitor.visit(name, value);
                break;
        }
    }


    static private int collectAccess(SmaliParser.SAccListContext ctx) {
        int access = 0;
        for (TerminalNode acc : ctx.ACC()) {
            access |= Utils.getAcc(acc.getSymbol().getText());
        }
        return access;
    }
}
