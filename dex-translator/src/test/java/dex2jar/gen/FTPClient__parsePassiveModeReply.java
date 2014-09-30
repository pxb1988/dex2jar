package dex2jar.gen;

import com.googlecode.d2j.*;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.test.DexTranslatorRunner;
import com.googlecode.dex2jar.test.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectweb.asm.Opcodes;

import static com.googlecode.d2j.reader.Op.*;
@RunWith(DexTranslatorRunner.class)
public class FTPClient__parsePassiveModeReply implements Opcodes {
    @Test
    public static void m003___parsePassiveModeReply(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PRIVATE, new Method("Lorg/apache/commons/net/ftp/FTPClient;",
                "__parsePassiveModeReply", new String[] { "Ljava/lang/String;" }, "V"));
        if (mv != null) {
            {
                DexAnnotationVisitor av00 = mv.visitAnnotation("Ldalvik/annotation/Throws;", Visibility.RUNTIME);
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
                code.visitRegister(11);
                DexLabel L0 = new DexLabel();
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                code.visitTryCatch(L0, L1, new DexLabel[] { L2 }, new String[] { "Ljava/lang/NumberFormatException;" });

                code.visitConstStmt(CONST, 7, Integer.valueOf(46)); // int: 0x0000002e float:0.000000
                code.visitConstStmt(CONST_STRING, 8, "Could not parse passive host information.\nServer Reply: ");

                code.visitFieldStmt(SGET_OBJECT, 5,-1, new Field("Lorg/apache/commons/net/ftp/FTPClient;", "__parms_pat",
                        "Ljava/util/regex/Pattern;"));
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 10 }, new Method("Ljava/util/regex/Pattern;",
                        "matcher", new String[] { "Ljava/lang/CharSequence;" }, "Ljava/util/regex/Matcher;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 1);

                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/util/regex/Matcher;",
                        "find", new String[] {}, "Z"));
                code.visitStmt1R(MOVE_RESULT, 5);
                DexLabel L13 = new DexLabel();
                code.visitJumpStmt(IF_NEZ, 5, -1,L13);

                code.visitTypeStmt(NEW_INSTANCE, 5,-1, "Lorg/apache/commons/net/MalformedServerReplyException;");
                code.visitTypeStmt(NEW_INSTANCE, 6,-1, "Ljava/lang/StringBuilder;");
                code.visitMethodStmt(INVOKE_DIRECT, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "<init>", new String[] {}, "V"));
                code.visitConstStmt(CONST_STRING, 7, "Could not parse passive host information.\nServer Reply: ");
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 6, 8 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 6, 10 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "toString", new String[] {}, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 6);
                code.visitMethodStmt(INVOKE_DIRECT, new int[] { 5, 6 }, new Method(
                        "Lorg/apache/commons/net/MalformedServerReplyException;", "<init>",
                        new String[] { "Ljava/lang/String;" }, "V"));
                code.visitStmt1R(THROW, 5);
                code.visitLabel(L13);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/util/regex/Matcher;",
                        "group", new String[] {}, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 10);

                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/util/regex/Matcher;",
                        "group", new String[] {}, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 5);
                code.visitConstStmt(CONST_STRING, 6, ",");
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/String;", "split",
                        new String[] { "Ljava/lang/String;" }, "[Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 4);

                code.visitTypeStmt(NEW_INSTANCE, 5, -1,"Ljava/lang/StringBuilder;");
                code.visitMethodStmt(INVOKE_DIRECT, new int[] { 5 }, new Method("Ljava/lang/StringBuilder;",
                        "<init>", new String[] {}, "V"));
                code.visitConstStmt(CONST, 6, Integer.valueOf(0)); // int: 0x00000000 float:0.000000
                code.visitStmt3R(AGET_OBJECT, 6, 4, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 5);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 7 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "C" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 5);
                code.visitConstStmt(CONST, 6, Integer.valueOf(1)); // int: 0x00000001 float:0.000000
                code.visitStmt3R(AGET_OBJECT, 6, 4, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 5);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 7 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "C" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 5);
                code.visitConstStmt(CONST, 6, Integer.valueOf(2)); // int: 0x00000002 float:0.000000
                code.visitStmt3R(AGET_OBJECT, 6, 4, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT, 5);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 7 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "C" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 5);
                code.visitConstStmt(CONST, 6, Integer.valueOf(3)); // int: 0x00000003 float:0.000000
                code.visitStmt3R(AGET_OBJECT, 6, 4, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5, 6 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT, 5);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 5 }, new Method("Ljava/lang/StringBuilder;",
                        "toString", new String[] {}, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT, 5);
                code.visitFieldStmt(IPUT, 5, 9, new Field("Lorg/apache/commons/net/ftp/FTPClient;", "__passiveHost",
                        "Ljava/lang/String;"));
                code.visitConstStmt(CONST, 5, Integer.valueOf(4)); // int: 0x00000004 float:0.000000
                code.visitLabel(L0);
                code.visitStmt3R(AGET_OBJECT, 5, 4, 5);
                code.visitMethodStmt(INVOKE_STATIC, new int[] { 5 }, new Method("Ljava/lang/Integer;", "parseInt",
                        new String[] { "Ljava/lang/String;" }, "I"));
                code.visitStmt1R(MOVE_RESULT, 2);

                code.visitConstStmt(CONST, 5, Integer.valueOf(5)); // int: 0x00000005 float:0.000000
                code.visitStmt3R(AGET_OBJECT, 5, 4, 5);
                code.visitMethodStmt(INVOKE_STATIC, new int[] { 5 }, new Method("Ljava/lang/Integer;", "parseInt",
                        new String[] { "Ljava/lang/String;" }, "I"));
                code.visitStmt1R(MOVE_RESULT, 3);
                                     code.visitStmt2R1N(Op.SHL_INT_LIT8,5,2,8);

                code.visitStmt3R(OR_INT, 5, 5, 3);
                code.visitFieldStmt(IPUT, 5, 9, new Field("Lorg/apache/commons/net/ftp/FTPClient;", "__passivePort",
                        "I"));
                code.visitLabel(L1);
                code.visitStmt0R(RETURN_VOID);
                code.visitLabel(L2);
                code.visitStmt1R(MOVE_EXCEPTION, 5);
                code.visitStmt2R(MOVE, 0, 5);

                code.visitTypeStmt(NEW_INSTANCE, 5, -1,"Lorg/apache/commons/net/MalformedServerReplyException;");
                code.visitTypeStmt(NEW_INSTANCE, 6,-1, "Ljava/lang/StringBuilder;");
                code.visitMethodStmt(INVOKE_DIRECT, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "<init>", new String[] {}, "V"));
                code.visitConstStmt(CONST_STRING, 7, "Could not parse passive host information.\nServer Reply: ");
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 6, 8 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 6, 10 }, new Method("Ljava/lang/StringBuilder;",
                        "append", new String[] { "Ljava/lang/String;" }, "Ljava/lang/StringBuilder;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 6);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 6 }, new Method("Ljava/lang/StringBuilder;",
                        "toString", new String[] {}, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 6);
                code.visitMethodStmt(INVOKE_DIRECT, new int[] { 5, 6 }, new Method(
                        "Lorg/apache/commons/net/MalformedServerReplyException;", "<init>",
                        new String[] { "Ljava/lang/String;" }, "V"));
                code.visitStmt1R(THROW, 5);

                code.visitEnd();
            }
            mv.visitEnd();
        }
    }
}
