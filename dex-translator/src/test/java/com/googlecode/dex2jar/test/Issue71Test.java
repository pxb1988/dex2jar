/*
 * Copyright (c) 2009-2011 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.ir.Constant.nInt;
import static com.googlecode.dex2jar.ir.Constant.nLong;
import static com.googlecode.dex2jar.ir.Constant.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.nAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nExceptionRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeSpecial;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeStatic;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeVirtual;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLCmp;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNe;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNew;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNewArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nParameterRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nRem;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nGoto;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLabel;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;

import java.util.ArrayList;

import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.ts.LocalRemove;
import com.googlecode.dex2jar.ir.ts.LocalSplit;
import com.googlecode.dex2jar.ir.ts.LocalType;
import com.googlecode.dex2jar.ir.ts.Transformer;
import com.googlecode.dex2jar.v3.EndRemover;
import com.googlecode.dex2jar.v3.IrMethod2AsmMethod;
import com.googlecode.dex2jar.v3.LocalCurrect;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public class Issue71Test {
    @Test
    public void test() {
        IrMethod irMethod = generate();
        Transformer[] tses = new Transformer[] { new LocalSplit(), new LocalRemove(), new LocalType(),
                new LocalCurrect() };
        Transformer endremove = new EndRemover();
        endremove.transform(irMethod);

        // indexLabelStmt4Debug(irMethod.stmts);

        for (Transformer ts : tses) {
            ts.transform(irMethod);
        }
        MethodNode node = new MethodNode();
        node.tryCatchBlocks = new ArrayList();
        new IrMethod2AsmMethod().convert(irMethod, node);
    }

    IrMethod generate() {
        IrMethod ir = new IrMethod();
        ir.access = 0x000a;
        ir.name = "writeAGig";
        ir.owner = Type.getType("Lcom/nitrodesk/nitroid/helpers/StoopidHelpers;");
        ir.args = Type.getArgumentTypes("(Ljava/lang/String;)Z");
        ir.ret = Type.BOOLEAN_TYPE;
        StmtList stmts = ir.stmts;

        Local[] v = new Local[15];// locals
        for (int i = 0; i < v.length; i++) {
            v[i] = new Local("v" + i, null);
            ir.locals.add(v[i]);
        }
        Local v0 = v[0];
        Local v1 = v[1];
        Local v2 = v[2];
        Local v3 = v[3];
        Local v4 = v[4];
        Local v5 = v[5];
        // Local v6 = v[6];
        Local v7 = v[7];
        Local v8 = v[8];
        Local v9 = v[9];
        // Local v10 = v[10];
        Local v11 = v[11];
        Local v12 = v[12];
        Local v13 = v[13];
        Local v14 = v[14];
        LabelStmt L0019 = nLabel();
        LabelStmt L000e = nLabel();
        LabelStmt L0075 = nLabel();
        LabelStmt L0018 = nLabel();
        LabelStmt L002b = nLabel();
        LabelStmt L0001 = nLabel();
        LabelStmt L0023 = nLabel();

        LabelStmt L005d = nLabel();
        LabelStmt L0077 = nLabel();

        LabelStmt L0025 = nLabel();

        LabelStmt L0090 = nLabel();

        LabelStmt L0034 = nLabel();
        LabelStmt L005c = nLabel();

        stmts.add(nAssign(v12, nParameterRef(ir.args[0], 0)));

        // 22ca80:_120b___________________________________|0000:_const/4_v11,_#int_0_//_#0
        stmts.add(nAssign(v11, Constant.nInt(0)));
        // 22ca82:_2202_8108______________________________|0001:_new-instance_v2,_Ljava/io/FileOutputStream;_//_type@0881
        stmts.add(L0001);
        stmts.add(nAssign(v2, nNew(Type.getType("Ljava/io/FileOutputStream;"))));
        // 22ca86:_7020_a337_c200_________________________|0003:_invoke-direct_{v2,_v12},_Ljava/io/FileOutputStream;.<init>:(Ljava/lang/String;)V_//_method@37a3
        stmts.add(nAssign(
                v13,
                nInvokeSpecial(new Value[] { v2, v12 }, Type.getType("Ljava/io/FileOutputStream;"), "<init>",
                        Type.getArgumentTypes("(Ljava/lang/String;)V"), Type.VOID_TYPE)));
        // 22ca8c:_1507_1000______________________________|0006:_const/high16_v7,_#int_1048576_//_#10
        stmts.add(nAssign(v7, nInt(1048576)));
        // 22ca90:_2370_1b14______________________________|0008:_new-array_v0,_v7,_[B_//_type@141b
        stmts.add(nAssign(v0, nNewArray(Type.BYTE_TYPE, v7)));
        // 22ca94:_1605_0000______________________________|000a:_const-wide/16_v5,_#int_0_//_#0
        stmts.add(nAssign(v5, nLong(0)));
        // 22ca98:_1603_0000______________________________|000c:_const-wide/16_v3,_#int_0_//_#0
        stmts.add(nAssign(v3, nLong(0)));
        // 22ca9c:_1607_0004______________________________|000e:_const-wide/16_v7,_#int_1024_//_#400
        stmts.add(L000e);
        stmts.add(nAssign(v7, nLong(1024)));
        // 22caa0:_3107_0307______________________________|0010:_cmp-long_v7,_v3,_v7
        stmts.add(nAssign(v7, nLCmp(v3, v7)));
        // 22caa4:_3a07_0700______________________________|0012:_if-ltz_v7,_0019_//_+0007
        stmts.add(nIf(nLt(v7, nInt(0)), L0019));
        // 22caa8:_6e10_a537_0200_________________________|0014:_invoke-virtual_{v2},_Ljava/io/FileOutputStream;.close:()V_//_method@37a5
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v2 }, Type.getType("Ljava/io/FileOutputStream;"), "close", new Type[0],
                        Type.VOID_TYPE)));
        // 22caae:_1217___________________________________|0017:_const/4_v7,_#int_1_//_#1
        stmts.add(nAssign(v7, nInt(1)));
        // 22cab0:_0f07___________________________________|0018:_return_v7
        stmts.add(L0018);
        stmts.add(nReturn(v7));
        // 22cab2:_1207___________________________________|0019:_const/4_v7,_#int_0_//_#0
        stmts.add(L0019);
        stmts.add(nAssign(v7, nInt(0)));
        // 22cab4:_7110_8c16_0700_________________________|001a:_invoke-static_{v7},_Lcom/nitrodesk/nitroid/BaseActivity;.poke:(Z)V_//_method@168c
        stmts.add(nAssign(
                v13,
                nInvokeStatic(new Value[] { v7 }, Type.getType("Lcom/nitrodesk/nitroid/BaseActivity;"), "poke",
                        new Type[] { Type.BOOLEAN_TYPE }, Type.VOID_TYPE)));
        // 22caba:_6e20_a737_0200_________________________|001d:_invoke-virtual_{v2,_v0},_Ljava/io/FileOutputStream;.write:([B)V_//_method@37a7
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v2, v0 }, Type.getType("Ljava/io/FileOutputStream;"), "write",
                        Type.getArgumentTypes("([B)V"), Type.VOID_TYPE)));
        // 22cac0:_6e10_a637_0200_________________________|0020:_invoke-virtual_{v2},_Ljava/io/FileOutputStream;.flush:()V_//_method@37a6
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v2 }, Type.getType("Ljava/io/FileOutputStream;"), "flush",
                        Type.getArgumentTypes("()V"), Type.VOID_TYPE)));
        // 22cac6:_1607_c800______________________________|0023:_const-wide/16_v7,_#int_200_//_#c8
        stmts.add(L0023);
        stmts.add(nAssign(v7, nLong(200)));
        // 22caca:_7120_8939_8700_________________________|0025:_invoke-static_{v7,_v8},_Ljava/lang/Thread;.sleep:(J)V_//_method@3989
        stmts.add(L0025);
        stmts.add(nAssign(
                v13,
                nInvokeStatic(new Value[] { v7 /* , v8 */}, Type.getType("Ljava/lang/Thread;"), "sleep",
                        Type.getArgumentTypes("(J)V"), Type.VOID_TYPE)));
        // 22cad0:_7100_8c39_0000_________________________|0028:_invoke-static_{},_Ljava/lang/Thread;.yield:()V_//_method@398c
        stmts.add(nAssign(
                v13,
                nInvokeStatic(new Value[] {}, Type.getType("Ljava/lang/Thread;"), "yield",
                        Type.getArgumentTypes("()V"), Type.VOID_TYPE)));
        // 22cad6:_1707_0000_1000_________________________|002b:_const-wide/32_v7,_#float_0.000000_//_#00100000
        stmts.add(L002b);
        stmts.add(nAssign(v7, nLong(0x00100000L)));
        // 22cadc:_bb75___________________________________|002e:_add-long/2addr_v5,_v7
        stmts.add(nAssign(v5, nAdd(v5, v7)));
        // 22cade:_1607_0100______________________________|002f:_const-wide/16_v7,_#int_1_//_#1
        stmts.add(nAssign(v7, nLong(1)));
        // 22cae2:_bb73___________________________________|0031:_add-long/2addr_v3,_v7
        stmts.add(nAssign(v3, nAdd(v3, v7)));
        // 22cae4:_1607_6400______________________________|0032:_const-wide/16_v7,_#int_100_//_#64
        stmts.add(nAssign(v7, nLong(100)));
        // 22cae8:_9f07_0307______________________________|0034:_rem-long_v7,_v3,_v7
        stmts.add(L0034);
        stmts.add(nAssign(v7, nRem(v3, v7)));
        // 22caec:_1609_0000______________________________|0036:_const-wide/16_v9,_#int_0_//_#0
        stmts.add(nAssign(v9, nLong(0)));
        // 22caf0:_3107_0709______________________________|0038:_cmp-long_v7,_v7,_v9
        stmts.add(nAssign(v7, nLCmp(v7, v9)));
        // 22caf4:_3907_d4ff______________________________|003a:_if-nez_v7,_000e_//_-002c
        stmts.add(nIf(nNe(v7, nInt(0)), L000e));
        // 22caf8:_2207_d608______________________________|003c:_new-instance_v7,_Ljava/lang/StringBuilder;_//_type@08d6
        stmts.add(nAssign(v7, nNew(Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cafc:_7010_5539_0700_________________________|003e:_invoke-direct_{v7},_Ljava/lang/StringBuilder;.<init>:()V_//_method@3955
        stmts.add(nAssign(
                v13,
                nInvokeSpecial(new Value[] { v7 }, Type.getType("Ljava/lang/StringBuilder;"), "<init>",
                        Type.getArgumentTypes("()V"), Type.VOID_TYPE)));

        // 22cb02:_6e20_5e39_c700_________________________|0041:_invoke-virtual_{v7,_v12},_Ljava/lang/StringBuilder;.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;_//_method@395e
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7, v12 }, Type.getType("Ljava/lang/StringBuilder;"), "append",
                        Type.getArgumentTypes("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                        Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb08:_0c07___________________________________|0044:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb0a:_1a08_9b01______________________________|0045:_const-string_v8,_"_written_:"_//_string@019b
        stmts.add(nAssign(v8, nString(" written :")));
        // 22cb0e:_6e20_5e39_8700_________________________|0047:_invoke-virtual_{v7,_v8},_Ljava/lang/StringBuilder;.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;_//_method@395e
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7, v8 }, Type.getType("Ljava/lang/StringBuilder;"), "append",
                        Type.getArgumentTypes("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                        Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb14:_0c07___________________________________|004a:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb16:_6e30_5b39_3704_________________________|004b:_invoke-virtual_{v7,_v3,_v4},_Ljava/lang/StringBuilder;.append:(J)Ljava/lang/StringBuilder;_//_method@395b
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7, v3 /* ,v4 --long */}, Type.getType("Ljava/lang/StringBuilder;"),
                        "append", Type.getArgumentTypes("(J)Ljava/lang/StringBuilder;"),
                        Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb1c:_0c07___________________________________|004e:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb1e:_1a08_c100______________________________|004f:_const-string_v8,_"_Mb."_//_string@00c1
        stmts.add(nAssign(v8, nString(" Mb.")));
        // 22cb22:_6e20_5e39_8700_________________________|0051:_invoke-virtual_{v7,_v8},_Ljava/lang/StringBuilder;.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;_//_method@395e
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7, v8 }, Type.getType("Ljava/lang/StringBuilder;"), "append",
                        Type.getArgumentTypes("(J)Ljava/lang/StringBuilder;"),
                        Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb28:_0c07___________________________________|0054:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb2a:_6e10_6539_0700_________________________|0055:_invoke-virtual_{v7},_Ljava/lang/StringBuilder;.toString:()Ljava/lang/String;_//_method@3965
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7 }, Type.getType("Ljava/lang/StringBuilder;"), "toString",
                        Type.getArgumentTypes("()Ljava/lang/String;"), Type.getType("Ljava/lang/String;"))));
        // 22cb30:_0c07___________________________________|0058:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb32:_7110_7a24_0700_________________________|0059:_invoke-static_{v7},_Lcom/nitrodesk/nitroid/helpers/CallLogger;.Log:(Ljava/lang/String;)V_//_method@247a
        stmts.add(nAssign(
                v13,
                nInvokeStatic(new Value[] { v7 }, Type.getType("Lcom/nitrodesk/nitroid/helpers/CallLogger;"), "Log",
                        Type.getArgumentTypes("(Ljava/lang/String;)V"), Type.getType("V"))));
        // 22cb38:_28b2___________________________________|005c:_goto_000e_//_-004e
        stmts.add(L005c);
        stmts.add(nGoto(L000e));
        // 22cb3a:_0d07___________________________________|005d:_move-exception_v7
        stmts.add(L005d);
        stmts.add(nAssign(v14, nExceptionRef(Type.getType("Ljava/io/FileNotFoundException;"))));
        stmts.add(nAssign(v7, v14));
        // 22cb3c:_0771___________________________________|005e:_move-object_v1,_v7
        stmts.add(nAssign(v1, v7));
        // 22cb3e:_2207_d608______________________________|005f:_new-instance_v7,_Ljava/lang/StringBuilder;_//_type@08d6
        stmts.add(nAssign(v7, nNew(Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb42:_1a08_c861______________________________|0061:_const-string_v8,_"Write_file_exception:_"_//_string@61c8
        stmts.add(nAssign(v8, nString("Write file exception: ")));
        // 22cb46:_7020_5739_8700_________________________|0063:_invoke-direct_{v7,_v8},_Ljava/lang/StringBuilder;.<init>:(Ljava/lang/String;)V_//_method@3957
        stmts.add(nAssign(
                v13,
                nInvokeSpecial(new Value[] { v7, v8 }, Type.getType("Ljava/lang/StringBuilder;"), "<init>",
                        Type.getArgumentTypes("(Ljava/lang/String;)V"), Type.VOID_TYPE)));

        // 22cb4c:_6e10_a037_0100_________________________|0066:_invoke-virtual_{v1},_Ljava/io/FileNotFoundException;.getMessage:()Ljava/lang/String;_//_method@37a0
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v1 }, Type.getType("Ljava/io/FileNotFoundException;"), "getMessage",
                        Type.getArgumentTypes("()Ljava/lang/String;"), Type.getType("Ljava/lang/String;"))));
        // 22cb52:_0c08___________________________________|0069:_move-result-object_v8
        stmts.add(nAssign(v8, v13));
        // 22cb54:_6e20_5e39_8700_________________________|006a:_invoke-virtual_{v7,_v8},_Ljava/lang/StringBuilder;.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;_//_method@395e
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7, v8 }, Type.getType("Ljava/lang/StringBuilder;"), "append",
                        Type.getArgumentTypes("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                        Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb5a:_0c07___________________________________|006d:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb5c:_6e10_6539_0700_________________________|006e:_invoke-virtual_{v7},_Ljava/lang/StringBuilder;.toString:()Ljava/lang/String;_//_method@3965
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7 }, Type.getType("Ljava/lang/StringBuilder;"), "toString",
                        Type.getArgumentTypes("()Ljava/lang/String;"), Type.getType("Ljava/lang/String;"))));
        // 22cb62:_0c07___________________________________|0071:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb64:_7110_7a24_0700_________________________|0072:_invoke-static_{v7},_Lcom/nitrodesk/nitroid/helpers/CallLogger;.Log:(Ljava/lang/String;)V_//_method@247a
        stmts.add(nAssign(
                v13,
                nInvokeStatic(new Value[] { v7 }, Type.getType("Lcom/nitrodesk/nitroid/helpers/CallLogger;"), "Log",
                        Type.getArgumentTypes("(Ljava/lang/String;)V"), Type.getType("V"))));
        // 22cb6a:_01b7___________________________________|0075:_move_v7,_v11
        stmts.add(L0075);
        stmts.add(nAssign(v7, v11));
        // 22cb6c:_28a2___________________________________|0076:_goto_0018_//_-005e
        stmts.add(nGoto(L0018));
        // 22cb6e:_0d07___________________________________|0077:_move-exception_v7
        stmts.add(L0077);
        stmts.add(nAssign(v14, nExceptionRef(Type.getType("Ljava/io/IOException;"))));
        stmts.add(nAssign(v7, v14));
        // 22cb70:_0771___________________________________|0078:_move-object_v1,_v7
        stmts.add(nAssign(v1, v7));
        // 22cb72:_2207_d608______________________________|0079:_new-instance_v7,_Ljava/lang/StringBuilder;_//_type@08d6
        stmts.add(nAssign(v7, nNew(Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb76:_1a08_3681______________________________|007b:_const-string_v8,_"file_io_exception:_"_//_string@8136
        stmts.add(nAssign(v8, nString("file io exception: ")));
        // 22cb7a:_7020_5739_8700_________________________|007d:_invoke-direct_{v7,_v8},_Ljava/lang/StringBuilder;.<init>:(Ljava/lang/String;)V_//_method@3957
        stmts.add(nAssign(
                v13,
                nInvokeSpecial(new Value[] { v7, v8 }, Type.getType("Ljava/lang/StringBuilder;"), "<init>",
                        Type.getArgumentTypes("(Ljava/lang/String;)V"), Type.VOID_TYPE)));

        // 22cb80:_6e10_b937_0100_________________________|0080:_invoke-virtual_{v1},_Ljava/io/IOException;.getMessage:()Ljava/lang/String;_//_method@37b9
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v1 }, Type.getType("Ljava/io/IOException;"), "getMessage",
                        Type.getArgumentTypes("()Ljava/lang/String;"), Type.getType("Ljava/lang/String;"))));
        // 22cb86:_0c08___________________________________|0083:_move-result-object_v8
        stmts.add(nAssign(v8, v13));
        // 22cb88:_6e20_5e39_8700_________________________|0084:_invoke-virtual_{v7,_v8},_Ljava/lang/StringBuilder;.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;_//_method@395e
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7, v8 }, Type.getType("Ljava/lang/StringBuilder;"), "append",
                        Type.getArgumentTypes("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                        Type.getType("Ljava/lang/StringBuilder;"))));
        // 22cb8e:_0c07___________________________________|0087:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb90:_6e10_6539_0700_________________________|0088:_invoke-virtual_{v7},_Ljava/lang/StringBuilder;.toString:()Ljava/lang/String;_//_method@3965
        stmts.add(nAssign(
                v13,
                nInvokeVirtual(new Value[] { v7 }, Type.getType("Ljava/lang/StringBuilder;"), "toString",
                        Type.getArgumentTypes("()Ljava/lang/String;"), Type.getType("Ljava/lang/String;"))));
        // 22cb96:_0c07___________________________________|008b:_move-result-object_v7
        stmts.add(nAssign(v7, v13));
        // 22cb98:_7110_7a24_0700_________________________|008c:_invoke-static_{v7},_Lcom/nitrodesk/nitroid/helpers/CallLogger;.Log:(Ljava/lang/String;)V_//_method@247a
        stmts.add(nAssign(
                v13,
                nInvokeStatic(new Value[] { v7 }, Type.getType("Lcom/nitrodesk/nitroid/helpers/CallLogger;"), "Log",
                        Type.getArgumentTypes("(Ljava/lang/String;)V"), Type.getType("V"))));
        // 22cb9e:_28e6___________________________________|008f:_goto_0075_//_-001a
        stmts.add(nGoto(L0075));
        // 22cba0:_0d07___________________________________|0090:_move-exception_v7
        stmts.add(L0090);
        stmts.add(nAssign(v14, nExceptionRef(Type.getType("Ljava/lang/Throwable;"))));
        stmts.add(nAssign(v7, v14));
        // 22cba2:_289a___________________________________|0091:_goto_002b_//_-0066
        stmts.add(nGoto(L002b));

        // ________0x0001_-_0x0023
        // __________Ljava/io/FileNotFoundException;_->_0x005d
        // __________Ljava/io/IOException;_->_0x0077
        ir.traps.add(new Trap(L0001, L0023, L005d, Type.getType("Ljava/io/FileNotFoundException;")));
        ir.traps.add(new Trap(L0001, L0023, L0077, Type.getType("Ljava/io/Exception;")));

        // ________0x0025_-_0x002b
        // __________Ljava/lang/Throwable;_->_0x0090
        // __________Ljava/io/FileNotFoundException;_->_0x005d
        // __________Ljava/io/IOException;_->_0x0077
        ir.traps.add(new Trap(L0025, L002b, L0090, Type.getType("Ljava/lang/Throwable;")));
        ir.traps.add(new Trap(L0025, L002b, L005d, Type.getType("Ljava/io/FileNotFoundException;")));
        ir.traps.add(new Trap(L0025, L002b, L0077, Type.getType("Ljava/io/Exception;")));

        // ________0x0034_-_0x005c
        // __________Ljava/io/FileNotFoundException;_->_0x005d
        // __________Ljava/io/IOException;_->_0x0077
        ir.traps.add(new Trap(L0034, L005c, L005d, Type.getType("Ljava/io/FileNotFoundException;")));
        ir.traps.add(new Trap(L0034, L005c, L0077, Type.getType("Ljava/io/Exception;")));
        return ir;
    }

    @Test
    public void shortTest() {
        IrMethod irMethod = new IrMethod();
        irMethod.name = "test";
        irMethod.args = new Type[] {};
        irMethod.ret = Type.VOID_TYPE;
        Local a = nLocal("a", null);
        irMethod.locals.add(a);

        irMethod.stmts.add(nAssign(a, nLong(0L)));
        irMethod.stmts.add(nAssign(a, nAdd(a, nLong(2))));

        Transformer[] tses = new Transformer[] { new LocalSplit(), new LocalRemove(), new LocalType(),
                new LocalCurrect() };
        Transformer endremove = new EndRemover();
        endremove.transform(irMethod);

        // indexLabelStmt4Debug(irMethod.stmts);

        for (Transformer ts : tses) {
            ts.transform(irMethod);
        }
        MethodNode node = new MethodNode();
        node.tryCatchBlocks = new ArrayList();
        new IrMethod2AsmMethod().convert(irMethod, node);
    }

}
