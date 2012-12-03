package dex2jar.gen;

import static com.googlecode.dex2jar.DexOpcodes.OP_AGET;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST_STRING;
import static com.googlecode.dex2jar.DexOpcodes.OP_IF_NEZ;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_DIRECT;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_VIRTUAL;
import static com.googlecode.dex2jar.DexOpcodes.OP_IPUT;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_EXCEPTION;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_RESULT;
import static com.googlecode.dex2jar.DexOpcodes.OP_NEW_INSTANCE;
import static com.googlecode.dex2jar.DexOpcodes.OP_OR;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN_VOID;
import static com.googlecode.dex2jar.DexOpcodes.OP_SGET;
import static com.googlecode.dex2jar.DexOpcodes.OP_SHL_INT_LIT_X;
import static com.googlecode.dex2jar.DexOpcodes.OP_THROW;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.DexType;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.test.TestUtils;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class FTPClient__parsePassiveModeReply implements Opcodes {
    @Test
    public void test() throws Exception {
        TestUtils.testDexASMifier(getClass(), "m003___parsePassiveModeReply");
    }

    public static void m003___parsePassiveModeReply(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PRIVATE, new Method("Lorg/apache/commons/net/ftp/FTPClient;",
                "__parsePassiveModeReply", new String[] { "Ljava/lang/String;" }, "V"));
        if (mv != null) {
            {
                DexAnnotationVisitor av00 = mv.visitAnnotation("Ldalvik/annotation/Throws;", Boolean.valueOf(true));
                if (av00 != null) {
                    {
                        DexAnnotationVisitor av01 = av00.visitArray("value");
                        if (av01 != null) {
                            av01.visit(null, new DexType("Lorg/apache/commons/net/MalformedServerReplyException;"));
                            av01.visitEnd();
                        }
                    }
                    av00.visitEnd();
                }
            }
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitArguments(11, new int[] { 9, 10 });
                DexLabel L0 = new DexLabel();
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                code.visitTryCatch(L0, L1, new DexLabel[] { L2 }, new String[] { "Ljava/lang/NumberFormatException;" });

                code.visitConstStmt(OP_CONST, 7, Integer.valueOf(46), 0); // int: 0x0000002e float:0.000000
                code.visitConstStmt(OP_CONST_STRING, 8, "Could not parse passive host information.\nServer Reply: ", 2);

                code.visitFieldStmt(OP_SGET, 5, new Field("Lorg/apache/commons/net/ftp/FTPClient;", "__parms_pat",
                        "Ljava/util/regex/Pattern;"), 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 10 }, new Method("Ljava/util/regex/Pattern;",
                        "matcher", new String[] { "Ljava/lang/CharSequence;" }, "Ljava/util/regex/Matcher;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 1, 2);

                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/util/regex/Matcher;",
                        "find", new String[] {}, "Z"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 0);
                DexLabel L13 = new DexLabel();
                code.visitJumpStmt(OP_IF_NEZ, 5, L13);

                code.visitClassStmt(OP_NEW_INSTANCE, 5, "Lorg/apache/commons/net/MalformedServerReplyException;");
                code.visitClassStmt(OP_NEW_INSTANCE, 6, "Ljava/lang/StringBuilder;");
                code.visitMethodStmt(OP_INVOKE_DIRECT, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "<init>", new String[] {}, "V"));
                code.visitConstStmt(OP_CONST_STRING, 7, "Could not parse passive host information.\nServer Reply: ", 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 6, 8 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 6, 10 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "toString", new String[] {}, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 6, 2);
                code.visitMethodStmt(OP_INVOKE_DIRECT, new int[] { 5, 6 }, new Method(
                        "Lorg/apache/commons/net/MalformedServerReplyException;", "<init>",
                        new String[] { "Ljava/lang/String;" }, "V"));
                code.visitReturnStmt(OP_THROW, 5, 2);
                code.visitLabel(L13);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/util/regex/Matcher;",
                        "group", new String[] {}, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 10, 2);

                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/util/regex/Matcher;",
                        "group", new String[] {}, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitConstStmt(OP_CONST_STRING, 6, ",", 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/String;", "split",
                        new String[] { "Ljava/lang/String;" }, "[Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 4, 2);

                code.visitClassStmt(OP_NEW_INSTANCE, 5, "Ljava/lang/StringBuilder;");
                code.visitMethodStmt(OP_INVOKE_DIRECT, new int[] { 5 }, new Method("Ljava/lang/StringBuilder;",
                        "<init>", new String[] {}, "V"));
                code.visitConstStmt(OP_CONST, 6, Integer.valueOf(0), 0); // int: 0x00000000 float:0.000000
                code.visitArrayStmt(OP_AGET, 6, 4, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 7 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "C" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitConstStmt(OP_CONST, 6, Integer.valueOf(1), 0); // int: 0x00000001 float:0.000000
                code.visitArrayStmt(OP_AGET, 6, 4, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 7 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "C" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitConstStmt(OP_CONST, 6, Integer.valueOf(2), 0); // int: 0x00000002 float:0.000000
                code.visitArrayStmt(OP_AGET, 6, 4, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 7 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "C" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitConstStmt(OP_CONST, 6, Integer.valueOf(3), 0); // int: 0x00000003 float:0.000000
                code.visitArrayStmt(OP_AGET, 6, 4, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 5 }, new Method("Ljava/lang/StringBuilder;",
                        "toString", new String[] {}, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 5, 2);
                code.visitFieldStmt(OP_IPUT, 5, 9, new Field("Lorg/apache/commons/net/ftp/FTPClient;", "__passiveHost",
                        "Ljava/lang/String;"), 2);
                code.visitConstStmt(OP_CONST, 5, Integer.valueOf(4), 0); // int: 0x00000004 float:0.000000
                code.visitLabel(L0);
                code.visitArrayStmt(OP_AGET, 5, 4, 5, 2);
                code.visitMethodStmt(OP_INVOKE_STATIC, new int[] { 5 }, new Method("Ljava/lang/Integer;", "parseInt",
                        new String[] { "Ljava/lang/String;" }, "I"));
                code.visitMoveStmt(OP_MOVE_RESULT, 2, 0);

                code.visitConstStmt(OP_CONST, 5, Integer.valueOf(5), 0); // int: 0x00000005 float:0.000000
                code.visitArrayStmt(OP_AGET, 5, 4, 5, 2);
                code.visitMethodStmt(OP_INVOKE_STATIC, new int[] { 5 }, new Method("Ljava/lang/Integer;", "parseInt",
                        new String[] { "Ljava/lang/String;" }, "I"));
                code.visitMoveStmt(OP_MOVE_RESULT, 3, 0);

                code.visitBinopLitXStmt(OP_SHL_INT_LIT_X, 5, 2, 8);
                code.visitBinopStmt(OP_OR, 5, 5, 3, 7);
                code.visitFieldStmt(OP_IPUT, 5, 9, new Field("Lorg/apache/commons/net/ftp/FTPClient;", "__passivePort",
                        "I"), 0);
                code.visitLabel(L1);
                code.visitReturnStmt(OP_RETURN_VOID);
                code.visitLabel(L2);
                code.visitMoveStmt(OP_MOVE_EXCEPTION, 5, 2);
                code.visitMoveStmt(OP_MOVE, 0, 5, 2);

                code.visitClassStmt(OP_NEW_INSTANCE, 5, "Lorg/apache/commons/net/MalformedServerReplyException;");
                code.visitClassStmt(OP_NEW_INSTANCE, 6, "Ljava/lang/StringBuilder;");
                code.visitMethodStmt(OP_INVOKE_DIRECT, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "<init>", new String[] {}, "V"));
                code.visitConstStmt(OP_CONST_STRING, 7, "Could not parse passive host information.\nServer Reply: ", 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 6, 8 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 6, 10 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 6, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "toString", new String[] {}, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 6, 2);
                code.visitMethodStmt(OP_INVOKE_DIRECT, new int[] { 5, 6 }, new Method(
                        "Lorg/apache/commons/net/MalformedServerReplyException;", "<init>",
                        new String[] { "Ljava/lang/String;" }, "V"));
                code.visitReturnStmt(OP_THROW, 5, 2);

                code.visitEnd();
            }
            mv.visitEnd();
        }
    }
}
