grammar Jasmin;

@header {
package com.googlecode.d2j.jasmin;
import java.util.List;
import java.util.ArrayList;
import java.math.BigInteger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;
}
@lexer::header {
package com.googlecode.d2j.jasmin;
}
@members{
    private static int versions[] = { 0, V1_1, V1_2, V1_3, V1_4, V1_5, V1_6, V1_7, 52 // V1_8 ?
            , 53 // V1_9 ?
    };
    private ClassNode cn;
    private FieldNode fn;
    private MethodNode mn;
    private String tmp;
    private int tmpInt;
    private String tmp2;
    public boolean rebuildLine=false;
    private java.util.Map<String, Label> labelMap = new java.util.HashMap<>();
    private void reset0() {
        cn = new ClassNode(ASM4);
        fn = null;
        mn = null;
    }

    static private int parseInt(String str, int start, int end) {
        int sof = start;
        int x = 1;
        if (str.charAt(sof) == '+') {
            sof++;
        } else if (str.charAt(sof) == '-') {
            sof++;
            x = -1;
        }
        long v;
        if (str.charAt(sof) == '0') {
            sof++;
            if (sof >= end) {
                return 0;
            }
            char c = str.charAt(sof);
            if (c == 'x' || c == 'X') {// hex
                sof++;
                v = Long.parseLong(str.substring(sof, end), 16);
            } else {// oct
                v = Long.parseLong(str.substring(sof, end), 8);
            }
        } else {
            v = Long.parseLong(str.substring(sof, end), 10);
        }
        return (int) (v * x);
    }

    static private int parseInt(String str) {
        return parseInt(str, 0, str.length());
    }

    static private Long parseLong(String str) {
        int sof = 0;
        int end = str.length() - 1;
        int x = 1;
        if (str.charAt(sof) == '+') {
            sof++;
        } else if (str.charAt(sof) == '-') {
            sof++;
            x = -1;
        }
        BigInteger v;
        if (str.charAt(sof) == '0') {
            sof++;
            if (sof >= end) {
                return 0L;
            }
            char c = str.charAt(sof);
            if (c == 'x' || c == 'X') {// hex
                sof++;
                v = new BigInteger(str.substring(sof, end), 16);
            } else {// oct
                v = new BigInteger(str.substring(sof, end), 8);
            }
        } else {
            v = new BigInteger(str.substring(sof, end), 10);
        }
        if (x == -1) {
            return v.negate().longValue();
        } else {
            return v.longValue();
        }
    }

    static private float parseFloat(String str) {
        str = str.toLowerCase();
        int s = 0;
        float x = 1f;
        if (str.charAt(s) == '+') {
            s++;
        } else if (str.charAt(s) == '-') {
            s++;
            x = -1;
        }
        int e = str.length() - 1;
        if (str.charAt(e) == 'f') {
            e--;
        }
        str = str.substring(s, e + 1);
        if (str.equals("floatnan")) {
            return Float.NaN;
        }
        if (str.equals("floatinfinity")) {
            return x < 0 ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }
        return (float) x * Float.parseFloat(str);
    }

    static private double parseDouble(String str) {
        str = str.toLowerCase();
        int s = 0;
        double x = 1;
        if (str.charAt(s) == '+') {
            s++;
        } else if (str.charAt(s) == '-') {
            s++;
            x = -1;
        }
        int e = str.length() - 1;
        if (str.charAt(e) == 'd') {
            e--;
        }
        str = str.substring(s, e + 1);
        if (str.equals("doublenan")) {
            return Double.NaN;
        }
        if (str.equals("doubleinfinity")) {
            return x < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        return x * Double.parseDouble(str);
    }

    private void line(int ln){
         if(rebuildLine) {
            Label label=new Label();
            mn.visitLabel(label);
            mn.visitLineNumber(ln, label);
         }
    }
    private static String unEscapeString(String str) {
        return unEscape0(str, 1, str.length() - 1);
    }
    private static String unEscape(String str) {
            return unEscape0(str, 0, str.length());
    }

    private static String unEscape0(String str, int start, int end) {

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end;) {
            char c = str.charAt(i);
            if (c == '\\') {
                char d = str.charAt(i + 1);
                switch (d) {
                // ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
                case 'b':
                    sb.append('\b');
                    i += 2;
                    break;
                case 't':
                    sb.append('\t');
                    i += 2;
                    break;
                case 'n':
                    sb.append('\n');
                    i += 2;
                    break;
                case 'f':
                    sb.append('\f');
                    i += 2;
                    break;
                case 'r':
                    sb.append('\r');
                    i += 2;
                    break;
                case '\"':
                    sb.append('\"');
                    i += 2;
                    break;
                case '\'':
                    sb.append('\'');
                    i += 2;
                    break;
                case '\\':
                    sb.append('\\');
                    i += 2;
                    break;
                case 'u':
                    String sub = str.substring(i + 2, i + 6);
                    sb.append((char) Integer.parseInt(sub, 16));
                    i += 6;
                    break;
                default:
                    int x = 0;
                    while (x < 3) {
                        char e = str.charAt(i + 1 + x);
                        if (e >= '0' && e <= '7') {
                            x++;
                        } else {
                            break;
                        }
                    }
                    if (x == 0) {
                        throw new RuntimeException("can't pase string");
                    }
                    sb.append((char) Integer.parseInt(str.substring(i + 1, i + 1 + x), 8));
                    i += 1 + x;
                }

            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static int getAcc(String name) {
        if (name.equals("public")) {
            return ACC_PUBLIC;
        } else if (name.equals("private")) {
            return ACC_PRIVATE;
        } else if (name.equals("protected")) {
            return ACC_PROTECTED;
        } else if (name.equals("static")) {
            return ACC_STATIC;
        } else if (name.equals("final")) {
            return ACC_FINAL;
        } else if (name.equals("synchronized")) {
            return ACC_SYNCHRONIZED;
        } else if (name.equals("volatile")) {
            return ACC_VOLATILE;
        } else if (name.equals("bridge")) {
            return ACC_BRIDGE;
        } else if (name.equals("varargs")) {
            return ACC_VARARGS;
        } else if (name.equals("transient")) {
            return ACC_TRANSIENT;
        } else if (name.equals("native")) {
            return ACC_NATIVE;
        } else if (name.equals("interface")) {
            return ACC_INTERFACE;
        } else if (name.equals("abstract")) {
            return ACC_ABSTRACT;
        } else if (name.equals("strict")) {
            return ACC_STRICT;
        } else if (name.equals("strictfp")) {
            return ACC_STRICT;
        } else if (name.equals("synthetic")) {
            return ACC_SYNTHETIC;
        } else if (name.equals("annotation")) {
            return ACC_ANNOTATION;
        } else if (name.equals("enum")) {
            return ACC_ENUM;
        } else if (name.equals("super")) {
            return ACC_SUPER;
        }
        throw new RuntimeException("not support access flags " + name);
    }
    private static int getOp(String str) {
            switch (str) {
            case "nop":
                return Opcodes.NOP;
            case "aconst_null":
                return Opcodes.ACONST_NULL;
            case "iconst_m1":
                return Opcodes.ICONST_M1;
            case "iconst_0":
                return Opcodes.ICONST_0;
            case "iconst_1":
                return Opcodes.ICONST_1;
            case "iconst_2":
                return Opcodes.ICONST_2;
            case "iconst_3":
                return Opcodes.ICONST_3;
            case "iconst_4":
                return Opcodes.ICONST_4;
            case "iconst_5":
                return Opcodes.ICONST_5;
            case "lconst_0":
                return Opcodes.LCONST_0;
            case "lconst_1":
                return Opcodes.LCONST_1;
            case "fconst_0":
                return Opcodes.FCONST_0;
            case "fconst_1":
                return Opcodes.FCONST_1;
            case "fconst_2":
                return Opcodes.FCONST_2;
            case "dconst_0":
                return Opcodes.DCONST_0;
            case "dconst_1":
                return Opcodes.DCONST_1;
            case "bipush":
                return Opcodes.BIPUSH;
            case "sipush":
                return Opcodes.SIPUSH;
            case "ldc_w":
            case "ldc2_w":
            case "ldc":
                return Opcodes.LDC;
            case "iload":
                return Opcodes.ILOAD;
            case "lload":
                return Opcodes.LLOAD;
            case "fload":
                return Opcodes.FLOAD;
            case "dload":
                return Opcodes.DLOAD;
            case "aload":
                return Opcodes.ALOAD;
            case "iaload":
                return Opcodes.IALOAD;
            case "laload":
                return Opcodes.LALOAD;
            case "faload":
                return Opcodes.FALOAD;
            case "daload":
                return Opcodes.DALOAD;
            case "aaload":
                return Opcodes.AALOAD;
            case "baload":
                return Opcodes.BALOAD;
            case "caload":
                return Opcodes.CALOAD;
            case "saload":
                return Opcodes.SALOAD;
            case "istore":
                return Opcodes.ISTORE;
            case "lstore":
                return Opcodes.LSTORE;
            case "fstore":
                return Opcodes.FSTORE;
            case "dstore":
                return Opcodes.DSTORE;
            case "astore":
                return Opcodes.ASTORE;
            case "iastore":
                return Opcodes.IASTORE;
            case "lastore":
                return Opcodes.LASTORE;
            case "fastore":
                return Opcodes.FASTORE;
            case "dastore":
                return Opcodes.DASTORE;
            case "aastore":
                return Opcodes.AASTORE;
            case "bastore":
                return Opcodes.BASTORE;
            case "castore":
                return Opcodes.CASTORE;
            case "sastore":
                return Opcodes.SASTORE;
            case "pop":
                return Opcodes.POP;
            case "pop2":
                return Opcodes.POP2;
            case "dup":
                return Opcodes.DUP;
            case "dup_x1":
                return Opcodes.DUP_X1;
            case "dup_x2":
                return Opcodes.DUP_X2;
            case "dup2":
                return Opcodes.DUP2;
            case "dup2_x1":
                return Opcodes.DUP2_X1;
            case "dup2_x2":
                return Opcodes.DUP2_X2;
            case "swap":
                return Opcodes.SWAP;
            case "iadd":
                return Opcodes.IADD;
            case "ladd":
                return Opcodes.LADD;
            case "fadd":
                return Opcodes.FADD;
            case "dadd":
                return Opcodes.DADD;
            case "isub":
                return Opcodes.ISUB;
            case "lsub":
                return Opcodes.LSUB;
            case "fsub":
                return Opcodes.FSUB;
            case "dsub":
                return Opcodes.DSUB;
            case "imul":
                return Opcodes.IMUL;
            case "lmul":
                return Opcodes.LMUL;
            case "fmul":
                return Opcodes.FMUL;
            case "dmul":
                return Opcodes.DMUL;
            case "idiv":
                return Opcodes.IDIV;
            case "ldiv":
                return Opcodes.LDIV;
            case "fdiv":
                return Opcodes.FDIV;
            case "ddiv":
                return Opcodes.DDIV;
            case "irem":
                return Opcodes.IREM;
            case "lrem":
                return Opcodes.LREM;
            case "frem":
                return Opcodes.FREM;
            case "drem":
                return Opcodes.DREM;
            case "ineg":
                return Opcodes.INEG;
            case "lneg":
                return Opcodes.LNEG;
            case "fneg":
                return Opcodes.FNEG;
            case "dneg":
                return Opcodes.DNEG;
            case "ishl":
                return Opcodes.ISHL;
            case "lshl":
                return Opcodes.LSHL;
            case "ishr":
                return Opcodes.ISHR;
            case "lshr":
                return Opcodes.LSHR;
            case "iushr":
                return Opcodes.IUSHR;
            case "lushr":
                return Opcodes.LUSHR;
            case "iand":
                return Opcodes.IAND;
            case "land":
                return Opcodes.LAND;
            case "ior":
                return Opcodes.IOR;
            case "lor":
                return Opcodes.LOR;
            case "ixor":
                return Opcodes.IXOR;
            case "lxor":
                return Opcodes.LXOR;
            case "iinc":
                return Opcodes.IINC;
            case "i2l":
                return Opcodes.I2L;
            case "i2f":
                return Opcodes.I2F;
            case "i2d":
                return Opcodes.I2D;
            case "l2i":
                return Opcodes.L2I;
            case "l2f":
                return Opcodes.L2F;
            case "l2d":
                return Opcodes.L2D;
            case "f2i":
                return Opcodes.F2I;
            case "f2l":
                return Opcodes.F2L;
            case "f2d":
                return Opcodes.F2D;
            case "d2i":
                return Opcodes.D2I;
            case "d2l":
                return Opcodes.D2L;
            case "d2f":
                return Opcodes.D2F;
            case "i2b":
                return Opcodes.I2B;
            case "i2c":
                return Opcodes.I2C;
            case "i2s":
                return Opcodes.I2S;
            case "lcmp":
                return Opcodes.LCMP;
            case "fcmpl":
                return Opcodes.FCMPL;
            case "fcmpg":
                return Opcodes.FCMPG;
            case "dcmpl":
                return Opcodes.DCMPL;
            case "dcmpg":
                return Opcodes.DCMPG;
            case "ifeq":
                return Opcodes.IFEQ;
            case "ifne":
                return Opcodes.IFNE;
            case "iflt":
                return Opcodes.IFLT;
            case "ifge":
                return Opcodes.IFGE;
            case "ifgt":
                return Opcodes.IFGT;
            case "ifle":
                return Opcodes.IFLE;
            case "if_icmpeq":
                return Opcodes.IF_ICMPEQ;
            case "if_icmpne":
                return Opcodes.IF_ICMPNE;
            case "if_icmplt":
                return Opcodes.IF_ICMPLT;
            case "if_icmpge":
                return Opcodes.IF_ICMPGE;
            case "if_icmpgt":
                return Opcodes.IF_ICMPGT;
            case "if_icmple":
                return Opcodes.IF_ICMPLE;
            case "if_acmpeq":
                return Opcodes.IF_ACMPEQ;
            case "if_acmpne":
                return Opcodes.IF_ACMPNE;
            case "goto":
                return Opcodes.GOTO;
            case "jsr":
                return Opcodes.JSR;
            case "ret":
                return Opcodes.RET;
            case "tableswitch":
                return Opcodes.TABLESWITCH;
            case "lookupswitch":
                return Opcodes.LOOKUPSWITCH;
            case "ireturn":
                return Opcodes.IRETURN;
            case "lreturn":
                return Opcodes.LRETURN;
            case "freturn":
                return Opcodes.FRETURN;
            case "dreturn":
                return Opcodes.DRETURN;
            case "areturn":
                return Opcodes.ARETURN;
            case "return":
                return Opcodes.RETURN;
            case "getstatic":
                return Opcodes.GETSTATIC;
            case "putstatic":
                return Opcodes.PUTSTATIC;
            case "getfield":
                return Opcodes.GETFIELD;
            case "putfield":
                return Opcodes.PUTFIELD;
            case "invokevirtual":
                return Opcodes.INVOKEVIRTUAL;
            case "invokespecial":
                return Opcodes.INVOKESPECIAL;
            case "invokestatic":
                return Opcodes.INVOKESTATIC;
            case "invokeinterface":
                return Opcodes.INVOKEINTERFACE;
            case "invokedynamic":
                return Opcodes.INVOKEDYNAMIC;
            case "new":
                return Opcodes.NEW;
            case "newarray":
                return Opcodes.NEWARRAY;
            case "anewarray":
                return Opcodes.ANEWARRAY;
            case "arraylength":
                return Opcodes.ARRAYLENGTH;
            case "athrow":
                return Opcodes.ATHROW;
            case "checkcast":
                return Opcodes.CHECKCAST;
            case "instanceof":
                return Opcodes.INSTANCEOF;
            case "monitorenter":
                return Opcodes.MONITORENTER;
            case "monitorexit":
                return Opcodes.MONITOREXIT;
            case "multianewarray":
                return Opcodes.MULTIANEWARRAY;
            case "ifnull":
                return Opcodes.IFNULL;
            case "ifnonnull":
                return Opcodes.IFNONNULL;
            case "iload_0":
                return 26;
            case "iload_1":
                return 27;
            case "iload_2":
                return 28;
            case "iload_3":
                return 29;
            case "lload_0":
                return 30;
            case "lload_1":
                return 31;
            case "lload_2":
                return 32;
            case "lload_3":
                return 33;
            case "fload_0":
                return 34;
            case "fload_1":
                return 35;
            case "fload_2":
                return 36;
            case "fload_3":
                return 37;
            case "dload_0":
                return 38;
            case "dload_1":
                return 39;
            case "dload_2":
                return 40;
            case "dload_3":
                return 41;
            case "aload_0":
                return 42;
            case "aload_1":
                return 43;
            case "aload_2":
                return 44;
            case "aload_3":
                return 45;
            case "istore_0":
                return 59;
            case "istore_1":
                return 60;
            case "istore_2":
                return 61;
            case "istore_3":
                return 62;
            case "lstore_0":
                return 63;
            case "lstore_1":
                return 64;
            case "lstore_2":
                return 65;
            case "lstore_3":
                return 66;
            case "fstore_0":
                return 67;
            case "fstore_1":
                return 68;
            case "fstore_2":
                return 69;
            case "fstore_3":
                return 70;
            case "dstore_0":
                return 71;
            case "dstore_1":
                return 72;
            case "dstore_2":
                return 73;
            case "dstore_3":
                return 74;
            case "astore_0":
                return 75;
            case "astore_1":
                return 76;
            case "astore_2":
                return 77;
            case "astore_3":
                return 78;
            }
            return 0;
        }

    private String[] parseOwnerAndName(String str) {
        int x=str.lastIndexOf('/');
        if(x>0){
        return new String[]{ unEscape0(str,0,x), unEscape0(str,x+1,str.length()) };
        }
        throw new RuntimeException("can't get owner and type from '"+str+"'");
    }

    public Object parseValue(String desc, Object v) {
        switch(desc) {
        case "Z": return ((Number)v).intValue()!=0;
        case "B": return ((Number)v).byteValue();
        case "S": return ((Number)v).shortValue();
        case "I": return ((Number)v).intValue();
        case "F": return ((Number)v).floatValue();
        case "D": return ((Number)v).doubleValue();
        case "J": return ((Number)v).longValue();
        case "C": return (char)((Number)v).intValue();
        }
        return v;
    }

    static class AV {
        public AnnotationNode visitAnnotation(final String desc, final boolean visible) {
            return null;
        };

        public AnnotationNode visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
            return null;
        }
    }

    AV cnv = new AV() {
        public AnnotationNode visitAnnotation(final String desc, final boolean visible) {
            return (AnnotationNode) cn.visitAnnotation(desc, visible);
        }
    };
    AV fnv = new AV() {
        public AnnotationNode visitAnnotation(final String desc, final boolean visible) {
            return (AnnotationNode) fn.visitAnnotation(desc, visible);
        }
    };
    AV mnv = new AV() {
        public AnnotationNode visitAnnotation(final String desc, final boolean visible) {
            return (AnnotationNode) mn.visitAnnotation(desc, visible);
        }

        public AnnotationNode visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
            return (AnnotationNode) mn.visitParameterAnnotation(parameter, desc, visible);
        }
    };
    private void visitOP0(int op){
    if(op>=26&&op<=45){    // xload_y
            int x=op-26;
            mn.visitVarInsn(ILOAD+x/4,x\%4);
            }else if(op>=59&&op<=78){    // xstore_y
                     int x=op-26;
                     mn.visitVarInsn(ISTORE+x/4,x\%4);
            }else{
        mn.visitInsn(op);
        }
    }
    private void visitIOP(int op, int a){
         // xstore
         // xload
         if(op>=21&&op<=58){
         mn.visitVarInsn(op,a);
         }  else {
         // xipush
         mn.visitIntInsn(op,a);
         }
    }
    private void visitJOP(int op, Label label){
        mn.visitJumpInsn(op,label);
    }
    private void visitIIOP(int op, int a, int b){
        mn.visitIincInsn(a,b);
    }
    private Label getLabel(String name){
    Label label=labelMap.get(name);
    if(label==null){
    label= new Label();
    labelMap.put(name,label);
    }
        return  label;
    }
    public void accept(ClassVisitor cv) throws RecognitionException{
        sFile();
        cn.accept(cv);
    }
    public ClassNode parse() throws RecognitionException {
        sFile();
        ClassNode cn=this.cn;
        reset0();
        return cn;
    }
    AV currentAv;
    AnnotationNode currentAnnotationVisitor;
}



fragment
INT_NENT: ('+'|'-')? (
               '0' 
            | ('1'..'9') ('0'..'9')* 
            | '0' ('0'..'7')+ 
            | ('0x'|'0X') HEX_DIGIT+
         );
fragment
FLOAT_NENT
    : ('+'|'-')?( ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT)
    ;
fragment
F_FLOAT	:	('f'|'F') ('l'|'L')('o'|'O')('a'|'A')('t'|'T');
fragment
F_DOUBLE	:('d'|'D')('o'|'O')('u'|'U')('b'|'B')('l'|'L')('e'|'E');
fragment
F_NAN : ('N'|'n') ('A'|'a') ('N'|'n');
fragment
F_INFINITY: ('I'|'i') ('N'|'n') ('F'|'f') ('I'|'i') ('N'|'n') ('I'|'i') ('T'|'t') ('Y'|'y') ;

FLOAT	:	((('0'..'9')+|FLOAT_NENT) ('f'|'F')) 
		| ('+'|'-')F_FLOAT F_INFINITY
		| '+' F_FLOAT F_NAN
		;
DOUBLE	:	FLOAT_NENT ('d'|'D')? 
		| ('0'..'9')+ ('d'|'D') 
		| ('+'|'-') F_DOUBLE F_INFINITY
		| '+' F_DOUBLE F_NAN
		;
LONG	:	INT_NENT ('L'|'l');
INT	:	INT_NENT;

COMMENT
    :   ';' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;
DSTRING
    :  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    ;
	
fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\''|'\"'|'\\')
    |   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    |   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

VOID_TYPE:'V';
fragment
FRAGMENT_PRIMITIVE_TYPE:'B'|'Z'|'S'|'C'|'I'|'F'|'J'|'D';
fragment
FRAGMENT_OBJECT_TYPE: 'L' (ESC_SEQ |~(';'|':'|'\\'|' '|'\n'|'\t'|'\r'|'('|')'))+ ';' ;

METHOD_DESC_WITHOUT_RET: '(' ('['*(FRAGMENT_PRIMITIVE_TYPE|FRAGMENT_OBJECT_TYPE))* ')';
OBJECT_TYPE: 'L' (ESC_SEQ |~(';'|':'|'\\'|' '|'\n'|'\t'|'\r'|'('|')'))+ ';' ;
ACC:	'public' | 'private' | 'protected' | 'static' | 'final' | 'synchronized' | 'bridge' | 'varargs' | 'native' |
    'abstract' | 'strictfp' | 'synthetic' | 'constructor' | 'interface' | 'enum' |
    'annotation' | 'volatile' | 'transient' | 'declared-synchronized' | 'super' | 'strict';
ANNOTATION_VISIBLITY: 'visible' | 'invisible' ;
METHOD_ANNOTATION_VISIBLITY: 'visibleparam' | 'invisibleparam';
INNER	:	'inner';
OUTTER	:	'outer';
OP0	:	'nop'|'monitorenter'|'monitorexit'|'pop2'|'pop'
	|	'iconst_m1'
	|('a'|'i')'const_' ('0'..'5')
	|('d'|'l')'const_' ('0'..'1')
	|'fconst_' ('0'..'2')
	|'aconst_null'
	|('a'|'d'|'f'|'i'|'l')? 'return'
	|('a'|'d'|'f'|'i'|'l') ('store'|'load') '_' ('0'..'3')
	|('a'|'b'|'c'|'d'|'f'|'i'|'l') ('astore'|'aload')
	|'dcmpg'|'dcmpl' | 'lcmp' |'fcmpg'|'fcmpl'
	|'athrow'
	|('i'|'f'|'d'|'l')('add'|'div'|'sub'|'mul'|'rem'|'shl'|'shr'|'ushr'|'and'|'or'|'xor'|'neg')
	|'arraylength'
	|'dup'|'dup2'|'dup_x2'|'dup2_x2'|'dup2_x1'
	|'swap'
	|'i2b' | 'i2c' |'i2d' | 'i2f' | 'i2s' | 'i2l'
	| 'f2d' | 'f2i' | 'f2l'
	| 'd2f' | 'd2i' | 'd2l'
	| 'l2d' | 'l2f' | 'l2i'
	;
IOP	:	('a'|'d'|'f'|'i'|'l') 'load'
	|	('a'|'d'|'f'|'i'|'l') 'store'
	|'bipush'|'sipush'
	;
IIOP	:	'iinc'
	;
JOP	:	'goto'
	|	'jsr'
	|	'if' ('null'|'nonnull'|'eq'|'ne'|'gt'|'ge'|'lt'|'le')
	|       'if_' ('a'|'i') 'cmp' ('eq'|'ne'|'gt'|'ge'|'lt'|'le')
	;
LDC	:	'ldc'|'ldc_w'|'ldc2_w'
	;
XFIELD	:	'getstatic'|'putstatic'|'getfield'|'putfield';
XNEWARRAY: 'newarray' ;
XTYPE	:	'checkcast'|'instanceof'|'new'|'anewarray'
	;
MULTIANEWARRAY
	:	'multianewarray'
	;
LOOKUPSWITCH:	'lookupswitch';
TABLESWITCH:	'tableswitch';
XINVOKE	:	'invokestatic'
	|	'invokevirtual'
	|       'invokespecial'
	;
INVOKEINTERFACE  :
	       'invokeinterface'
	;
INVOKEDYNAMIC
	:	'invokedynamic';
HIGH	:	'high';
DEFAULT	:	'default';
FROM	:	'from';
TO	:	'to';
USING	:	'using';
STACK	:	'stack';
LOCALS	:	'locals';
WBOOLEAN: 'boolean';
WBYTE: 'byte';
WSHORT: 'short';
WCHAR: 'char';
WINTEGER: 'int';
WFLOAT: 'float';
WLONG: 'long';
WDOUBLE: 'double';

fragment
F_ID_FOLLOWS: ESC_SEQ| ~('\\'|'\r'|'\n'|'\t'|' '|':'|'-'|'='|','|'{'|'}'|'('|')');
ID  :    FRAGMENT_PRIMITIVE_TYPE F_ID_FOLLOWS+
    |    ESC_SEQ F_ID_FOLLOWS*
    |    ~(FRAGMENT_PRIMITIVE_TYPE| '0'..'9'| '\\' | '\r' | '\n' | '\t' | '\'' | '\"' | ' ' | ':' | '-' | '=' | '.' | ',' | '&' | '@' | '/' | '{'|'['|']'|'}'|'('|')') F_ID_FOLLOWS*
    ;
PARRAY_TYPE
	:	'['+ FRAGMENT_OBJECT_TYPE
	|	'[' '['+ FRAGMENT_PRIMITIVE_TYPE
	;
AT	:	'@';
AND	:	'&';
UP_Z	:	'Z';
UP_B	:	'B';
UP_S	:	'S';
UP_C	:	'C';
UP_I	:	'I';
UP_F	:	'F';
UP_D	:	'D';
UP_J	:	'J';
ARRAY_Z	:	'[Z';
ARRAY_B	:	'[B';
ARRAY_S	:	'[S';
ARRAY_C	:	'[C';
ARRAY_I	:	'[I';
ARRAY_F	:	'[F';
ARRAY_D	:	'[D';
ARRAY_J	:	'[J';
ARRAY_LOW_E	:	'[e';
ARRAY_LOW_S	:	'[s';
ARRAY_LOW_C	:	'[c';
ARRAY_AT	:	'[@';
ARRAY_AND	:	'[&';
LEFT_PAREN: '(';
RIGHT_PAREN: ')';

sFile	: { reset0(); currentAv=cnv; }
sHead+ (sAnnotation|sVisibiltyAnnotation)* (sField|sMethod)*
	;
sHead   :  '.bytecode' ( a=INT { int v=parseInt($a.text); cn.version=versions[v>=45?v-45:v];}
                        |a=DOUBLE {double v=parseDouble($a.text); cn.version=versions[(int)(v<2.0?(v*10)\%10:(v-44))]; }
                       )
        |  '.source' aa4=sAnyIdOrString  { cn.sourceFile=$aa4.str; }
		|  '.class' i=sAccList {cn.access|=$i.acc; if ((cn.access & Opcodes.ACC_INTERFACE) == 0) {cn.access |= Opcodes.ACC_SUPER;} else { cn.access &= ~Opcodes.ACC_SUPER; } } a1=sInternalNameOrDesc { cn.name=Type.getType($a1.desc).getInternalName(); }
		|  '.interface' i=sAccList {cn.access|=ACC_INTERFACE|$i.acc;} a1=sInternalNameOrDesc { cn.name=Type.getType($a1.desc).getInternalName(); }
		|  '.super' a1=sInternalNameOrDescACC  {  cn.superName=Type.getType($a1.desc).getInternalName(); }
		|  '.implements' a1=sInternalNameOrDescACC { if(cn.interfaces==null){cn.interfaces=new ArrayList<>();}  cn.interfaces.add(Type.getType($a1.desc).getInternalName()); }
		|  '.enclosing method' ownerAndName=sOwnerAndName {tmp=null;} (b=sMethodDesc{tmp=$b.text;})? {cn.visitOuterClass($ownerAndName.ownerInternalName,$ownerAndName.memberName,tmp);}
		|  sDeprecateAttr  { cn.access|=ACC_DEPRECATED; }
		|  '.debug' a=STRING  { cn.sourceDebug=unEscapeString($a.text); }
		|  '.attribute' sId STRING     { System.err.println("ignore .attribute"); }
		|  '.inner class' (i=sAccList sId{tmpInt=$i.acc;})? {tmp=null;tmp2=null;} ('inner' a3=sId{tmp=$a3.text;})? ('outer' a4=sId{tmp2=$a4.text;})?   { cn.visitInnerClass(null,tmp2,tmp,tmpInt); }
		|  '.no_super' {cn.superName=null;}
		|  '.class_attribute' sId STRING    { System.err.println("ignore .class_attribute"); }
		|  '.enclosing_method_attr' a=STRING b1=STRING c=STRING   {cn.visitOuterClass($a.text,$b1.text,$c.text);}
		|  '.inner_class_attr' ('.inner_class_spec_attr' a=STRING b2=STRING i=sAccList '.end' '.inner_class_spec_attr' { cn.visitInnerClass(null,unEscape($a.text),unEscape($b2.text),i); } )* '.end' '.inner_class_attr'
		|  s=sSigAttr  { cn.signature=$s.sig; }
		|  sSynthetic   {cn.access|=ACC_SYNTHETIC;}
		;
sSigAttr returns[String sig]:	('.signature_attr' | '.signature') a=STRING{ $sig=unEscapeString($a.text); };
sDeprecateAttr:	'.deprecated';
sSynthetic
	:	'.synthetic'
	;
sArrayType
	:	PARRAY_TYPE|ARRAY_Z|ARRAY_B|ARRAY_S|ARRAY_C|ARRAY_I|ARRAY_F|ARRAY_D|ARRAY_J
	;
sClassDesc
	:	sArrayType|OBJECT_TYPE|UP_Z|UP_B|UP_S|UP_C|UP_I|UP_J|UP_D|UP_F
	;
sId	:	ID|AT|AND|UP_Z|UP_B|UP_S|UP_C|UP_I|UP_F|UP_D|UP_J|ANNOTATION_VISIBLITY|METHOD_ANNOTATION_VISIBLITY|INNER|OUTTER
	|	IIOP|IOP|JOP|OP0|LDC|XFIELD|XTYPE|XINVOKE|INVOKEINTERFACE|MULTIANEWARRAY|LOOKUPSWITCH|TABLESWITCH|DEFAULT|FROM|TO|USING|STACK|LOCALS|HIGH|INVOKEDYNAMIC|VOID_TYPE
	| WBOOLEAN| WBYTE | WSHORT|WCHAR|WINTEGER|WLONG|WFLOAT|WDOUBLE |XNEWARRAY
	;
sWord : sId ;
sAnnotation
	: '.annotation' (b=ANNOTATION_VISIBLITY aInternalOrDesc=sInternalNameOrDescACC { currentAnnotationVisitor= currentAv.visitAnnotation($aInternalOrDesc.desc,!$b.text.contains("invisible")); } |
	                  b=METHOD_ANNOTATION_VISIBLITY c=INT a=sId {currentAnnotationVisitor=currentAv.visitParameterAnnotation(parseInt($c.text),$a.text,!$b.text.contains("invisible"));}
	                )
	    (sAnnotationElement* '.end annotation')?
	;
sVisibiltyAnnotation
	: {boolean visible=false;} ('.runtime_visible_annotation' {visible=true;}|'.runtime_invisible_annotation'{visible=false;}) a=STRING      { currentAnnotationVisitor= currentAv.visitAnnotation(unEscape($a.text),visible); }
	sAnnotationSoot*
	 '.end' '.annotation_attr'
	;
sAnnotationSoot
	: '.annotation' 
	(t=sAnnotationElementSoot {currentAnnotationVisitor.visit($t.nn,$t.v);} )*
	'.end' '.annotation'
	;
sAnnotationElementSoot returns[String nn,Object v]
	:'.elem' ('.bool_kind' a=STRING b=INT          {$nn=unEscapeString($a.text); $v=0!=parseInt($b.text);}
              	    | '.short_kind' a=STRING b=INT        {$nn=unEscapeString($a.text); $v=(short)parseInt($b.text);}
              	    | '.byte_kind' a=STRING b=INT         {$nn=unEscapeString($a.text); $v=(byte)parseInt($b.text);}
              	    | '.char_kind' a=STRING b=INT         {$nn=unEscapeString($a.text); $v=(char)parseInt($b.text);}
              	    | '.int_kind' a=STRING b=INT          {$nn=unEscapeString($a.text); $v=parseInt($b.text);}
              	    | '.long_kind' a=STRING b=(INT|LONG)  {$nn=unEscapeString($a.text); $v=parseLong($b.text);}
              	    | '.float_kind' a=STRING b=INT        {$nn=unEscapeString($a.text); $v=parseFloat($b.text);}
              	    | '.doub_kind' a=STRING b=(INT|LONG)  {$nn=unEscapeString($a.text); $v=parseDouble($b.text);}
              	    | '.str_kind' a=STRING b=STRING       {$nn=unEscapeString($a.text); $v=unEscapeString($b.text);}
              	    | '.enum_kind' a=STRING b=STRING      {$nn=unEscapeString($a.text); String on[]=parseOwnerAndName($b.text);$v=new String[]{on[0],on[1]};}
              	    | '.cls_kind' a=STRING b=STRING       {$nn=unEscapeString($a.text); $v=Type.getType(unEscapeString($b.text));}
              	    | '.arr_kind' a=STRING {List<Object> array=new ArrayList<>();} (t=sAnnotationElementSoot{array.add($t.v);})* '.end'  '.arr_elem'   {$nn=unEscapeString($a.text); $v=array;}
              	    | '.ann_kind' a=STRING q=sSubannotationSoot '.end' '.annot_elem'     {$nn=unEscapeString($a.text); $v=$q.v;})
	;

sSubannotationSoot  returns[AnnotationNode v]
	:	'.annotation' a=STRING   { $v=new AnnotationNode(unEscapeString($a.text)); }
	     (t=sAnnotationElementSoot {$v.visit($t.nn,$t.v);} )*
	    '.end' '.annotation'
	;
sAnnotationElement @init{List<Object> array = new ArrayList<Object>(); AnnotationNode _t= currentAnnotationVisitor;}
    :   a=sId (
             xid=ID { if(!"e".contains($xid.text)){ throw new RecognitionException(input);} }  c=OBJECT_TYPE '=' b=sWord {  _t.visit($a.text,new String[]{$c.text,$b.text}); }
           | AT b2=OBJECT_TYPE '=' {  currentAnnotationVisitor=new AnnotationNode($b2.text);} sSubannotation  { _t.visit($a.text,currentAnnotationVisitor); }
           | xid=ID { if(!"c".contains($xid.text)){ throw new RecognitionException(input);} } '=' b1=sClassDesc { currentAnnotationVisitor.visit($a.text,Type.getType($b1.text)); }
           | xid=ID { if(!"s".contains($xid.text)){ throw new RecognitionException(input);} } '=' b3=STRING      { currentAnnotationVisitor.visit($a.text,unEscapeString($b3.text)); }
           | UP_B  '=' b4=INT  { currentAnnotationVisitor.visit($a.text,(byte)parseInt($b4.text)); }
           | UP_Z  '=' b5=INT  { currentAnnotationVisitor.visit($a.text,0!=parseInt($b5.text)); }
           | UP_S  '=' b6=INT   { currentAnnotationVisitor.visit($a.text,(short)parseInt($b6.text)); }
           | UP_C  '=' b7=INT   { currentAnnotationVisitor.visit($a.text,(char)parseInt($b7.text)); }
           | UP_I  '=' b8=INT   { currentAnnotationVisitor.visit($a.text,parseInt($b8.text)); }
           | UP_J  '=' b9=(INT|LONG)  { currentAnnotationVisitor.visit($a.text,parseLong($b9.text)); }
           | UP_F  '=' b10=(INT|FLOAT|DOUBLE)  { currentAnnotationVisitor.visit($a.text,parseFloat($b10.text)); }
           | UP_D  '=' b11=(INT|FLOAT|DOUBLE)   { currentAnnotationVisitor.visit($a.text,parseDouble($b11.text)); }
           | ARRAY_B '='  (b12=INT {array.add((byte)parseInt($b12.text));} )+    { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_Z '='  (b13=INT {array.add(0!=parseInt($b13.text));} )+       { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_S '='  (b14=INT {array.add((short)parseInt($b14.text));} )+   { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_C '='  (b15=INT {array.add((char)parseInt($b15.text));} )+    { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_I '='  (b16=INT {array.add(parseInt($b16.text));} )+          { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_J '='  (b17=(INT|LONG) {array.add(parseLong($b17.text));} )+  { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_F '='  (b18=(INT|FLOAT|DOUBLE) {array.add(parseFloat($b18.text));} )+  { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_D '='  (b19=(INT|DOUBLE) {array.add(parseDouble($b19.text));} )+       { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_LOW_E c=OBJECT_TYPE '='  ((b1=sWord{  array.add(new String[]{$c.text,unEscape($b1.text)}); }|b2=STRING{  array.add(new String[]{$c.text,unEscapeString($b2.text)}); }|b3=DSTRING{  array.add(new String[]{$c.text,unEscapeString($b3.text)}); })  )+  { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_AND b20=OBJECT_TYPE '=' ARRAY_AT '='  ({currentAnnotationVisitor=new AnnotationNode($b20.text);} sSubannotation{ array.add(currentAnnotationVisitor); })+ { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_LOW_C '='  (b=sClassDesc {array.add(Type.getType($b.text));} )+       { currentAnnotationVisitor.visit($a.text,array); }
           | ARRAY_LOW_S '='  (b21=STRING {array.add(unEscapeString($b21.text));})+   { currentAnnotationVisitor.visit($a.text,array); }
		)
	{ currentAnnotationVisitor=_t; }
;
sSubannotation
	:	'.annotation' 
	     sAnnotationElement*
	    '.end annotation'
	;
sAccList returns[int acc]: {$acc=0;} ( a=ACC {$acc|=getAcc($a.text);})*  ;
sMemberName returns[String name]: a=sId {$name=unEscape($a.text);} | b=STRING {$name=unEscapeString($b.text);}| c=DSTRING {$name=unEscapeString($c.text);};
sField	@init {
                    if(cn.fields==null){
                        cn.fields=new ArrayList<>();
                    }
                    currentAv=fnv;
                    fn=new FieldNode(0,null,null,null,null);
                    cn.fields.add(fn);
              }
    :	'.field' i=sAccList n=sMemberName t=sClassDesc { fn.access|=i;fn.name=$n.name;fn.desc=unEscape($t.text); }
        ( '=' (a=STRING {fn.value=parseValue(fn.desc,unEscapeString($a.text));}
              |a1=INT      {fn.value=parseValue(fn.desc,parseInt($a1.text));}
              |a2=LONG     {fn.value=parseValue(fn.desc,parseLong($a2.text));}
              |a3=FLOAT    {fn.value=parseValue(fn.desc,parseFloat($a3.text));}
              |a4=DOUBLE   {fn.value=parseValue(fn.desc,parseDouble($a4.text));}
              |a5=sClassDesc {fn.value=parseValue(fn.desc,Type.getType(unEscape($a5.text)));}
              )
        )?
            (
            s=sSigAttr{fn.signature=$s.sig;}
            |sDeprecateAttr{ fn.access|=ACC_DEPRECATED; }
            |sSynthetic {cn.access|=ACC_SYNTHETIC;}
            |sVisibiltyAnnotation
            |sAnnotation
            )*
    ('.end field'| '.end' '.field')?
	('.field_attribute' sId STRING {System.err.println("ignore .field_attribute");})*
	;
sMethod	@init{
    if(cn.methods==null){
        cn.methods=new ArrayList<>();
    }
    currentAv=mnv;
    mn=new MethodNode(ASM4);
    cn.methods.add(mn);
    labelMap.clear();
    if(mn.exceptions==null){
        mn.exceptions=new ArrayList<>();
    }
    if(mn.tryCatchBlocks==null){
            mn.tryCatchBlocks=new ArrayList<>();
        }
}
    :	'.method' i=sAccList n=sMemberName t=sMethodDesc {mn.access|=i;mn.name=$n.name;mn.desc=unEscape($t.text);}
            (s=sSigAttr{mn.signature=$s.sig;}
                |sDeprecateAttr{ cn.access|=ACC_DEPRECATED; }
                |sSynthetic {cn.access|=ACC_SYNTHETIC;}
                |sVisibiltyAnnotation
                |sAnnotation
                |'.throws' at=sInternalNameOrDesc {  mn.exceptions.add(Type.getType($at.desc).getInternalName()); }
                |'.annotation_default' (t=sAnnotationElementSoot {currentAnnotationVisitor=(AnnotationNode)mn.visitAnnotationDefault();} )? '.end' '.annotation_default'
                |'.param' ('.runtime_invisible_annotation'|'.runtime_visible_annotation') {int index=0;}
                    ({boolean visible=false;} ('.runtime_visible_annotation' {visible=true;}|'.runtime_invisible_annotation'{visible=false;}) a1=STRING
                    { currentAnnotationVisitor= currentAv.visitParameterAnnotation(index,unEscapeString($a1.text),visible); }
                        sAnnotationSoot*
                       '.end' '.annotation_attr' {index++;}
                    )*
                 '.end' '.param'
                | code
            )*
        '.end method'
	('.method_attribute' sId STRING {System.err.println("ignore method_attribute");})*
	;

sLabel	:	ACC|ID|UP_Z|UP_B|UP_S|UP_C|UP_I|UP_F|UP_D|UP_J|ANNOTATION_VISIBLITY|METHOD_ANNOTATION_VISIBLITY|INNER|OUTTER
	;
code
    :	a=OP0 { line($a.line); visitOP0(getOp($a.text)); }
	|	a=IOP b=INT { line($a.line); visitIOP(getOp($a.text),parseInt($b.text)); }
	|	a=IIOP b=INT c=INT   { line($a.line); visitIIOP(getOp($a.text),parseInt($b.text),parseInt($c.text)); }
	| 	a=LDC  {line($a.line);  } ( c=INT        {mn.visitLdcInsn(parseInt($c.text));}
	                               |c=LONG       {mn.visitLdcInsn(parseLong($c.text));}
	                               |c=FLOAT      {mn.visitLdcInsn(parseFloat($c.text));}
	                               |c=DOUBLE     {mn.visitLdcInsn(parseDouble($c.text));}
	                               |c=STRING     {mn.visitLdcInsn(unEscapeString($c.text));}
	                               |eTV=sInternalNameOrDescNoString {mn.visitLdcInsn(Type.getType($eTV.desc));}
	                              )
	|	a=XFIELD efo=sFieldObject {  line($a.line);  mn.visitFieldInsn(getOp($a.text),$efo.ownerInternalName,$efo.memberName,$efo.type);   }
	|   a=XNEWARRAY {line($a.line); }  (
	               WBOOLEAN{mn.visitIntInsn(NEWARRAY,T_BOOLEAN);}
	               |WBYTE{mn.visitIntInsn(NEWARRAY,T_BYTE);}
	               |WSHORT{mn.visitIntInsn(NEWARRAY,T_SHORT);}
	               |WCHAR{mn.visitIntInsn(NEWARRAY,T_CHAR);}
	               |WINTEGER{mn.visitIntInsn(NEWARRAY,T_INT);}
	               |WLONG{mn.visitIntInsn(NEWARRAY,T_LONG);}
	               |WFLOAT{mn.visitIntInsn(NEWARRAY,T_FLOAT);}
	               |WDOUBLE{mn.visitIntInsn(NEWARRAY,T_DOUBLE);})
	|	a=XTYPE ffTV=sInternalNameOrDescACC {
	                       line($a.line);
	                          mn.visitTypeInsn(getOp($a.text),Type.getType($ffTV.desc).getInternalName());
	            }
	|	a=JOP z=sLabel  { line($a.line); visitJOP(getOp($a.text),getLabel($z.text)); }
	|	a=XINVOKE e1=sMethodObject   {line($a.line);
	                    mn.visitMethodInsn(getOp($a.text),$e1.ownerInternalName,$e1.memberName,$e1.desc);
	                  }
	|	a=INVOKEINTERFACE e2=sMethodObject INT?  {line($a.line);
	                    mn.visitMethodInsn(getOp($a.text),$e2.ownerInternalName,$e2.memberName,$e2.desc);
	                  }
	|	a=INVOKEDYNAMIC e3=sMethodObject sId sMethodDesc '(' sInvokeDynamicE (',' sInvokeDynamicE)* ')'  {line($a.line); if(1==1) throw new RuntimeException("not support Yet!");}
	|	a=MULTIANEWARRAY ff=sClassDesc c=INT   {line($a.line); mn.visitMultiANewArrayInsn(unEscape($ff.text),parseInt($c.text)); }
	|   z=sLabel ':' { Label label=getLabel($z.text); mn.visitLabel(label); if(rebuildLine) {mn.visitLineNumber($z.start.getLine(),label);}
	 }
	|	'.catch' e=sId 'from' z1=sLabel 'to' z2=sLabel 'using' z3=sLabel { String type="all".equals($e.text)?null:unEscape($e.text); mn.visitTryCatchBlock(getLabel($z1.text),getLabel($z2.text),getLabel($z3.text),type); }
	|	'.limit' 'stack' ('?' { mn.maxStack=-1; } | i1=INT { mn.maxStack=parseInt($i1.text); })
	|	'.limit' 'locals' ('?' {mn.maxLocals=-1;}| i1=INT { mn.maxLocals=parseInt($i1.text);})
	|	'.code_attribute' sId STRING   { System.err.println("ignore .code_attribute"); }
	|	'.line' b=INT  { if(!rebuildLine) { Label label=new Label(); mn.visitLabel(label); mn.visitLineNumber(parseInt($b.text),label); } }
	|   '.var' var=INT 'is' mber=sMemberName desc=sClassDesc ('signature' sig=STRING)? 'from' z1=sLabel 'to' z2=sLabel  { mn.visitLocalVariable($mber.name,unEscape($desc.text),unEscapeString($sig.text),getLabel($z1.text),getLabel($z2.text),parseInt($var.text)); }
    |   sSwitch
	;
sInvokeDynamicE
	:	METHOD_DESC_WITHOUT_RET (INT|LONG|FLOAT|DOUBLE|STRING)
	;
sMethodDesc
	:	METHOD_DESC_WITHOUT_RET (sClassDesc|VOID_TYPE)
	;
sSwitch @init {List<Integer> keys=null;List<Label> labels=null;Label defaultLabel=null;}
	:	a=LOOKUPSWITCH { keys=new ArrayList<>(); labels=new ArrayList<>();  } (c=INT ':' z=sLabel { keys.add(parseInt($c.text)); labels.add(getLabel($z.text)); })* (DEFAULT ':' z=sLabel { defaultLabel=getLabel($z.text); })   {
	        line($a.line);
	        int ts[]=new int[keys.size()];
	        for(int i=0;i<keys.size();i++){
	            ts[i]=keys.get(i);
	        }
	        mn.visitLookupSwitchInsn(defaultLabel, ts, labels.toArray(new Label[labels.size()]));
	        }
	|	a=TABLESWITCH { labels=new ArrayList<>();  } c=INT ';' 'high' '=' d=INT (z=sLabel  {labels.add(getLabel($z.text));} )* (DEFAULT ':' z=sLabel {defaultLabel=getLabel($z.text);})    {
	        line($a.line); mn.visitTableSwitchInsn(parseInt($c.text),parseInt($c.text)+labels.size()-1,defaultLabel,labels.toArray(new Label[labels.size()]));
	        }
	|   a=TABLESWITCH { labels=new ArrayList<>();  } c=INT (z=sLabel  {labels.add(getLabel($z.text));} )* (DEFAULT ':' z=sLabel {defaultLabel=getLabel($z.text);})    {
        	        line($a.line); mn.visitTableSwitchInsn(parseInt($c.text),parseInt($c.text)+labels.size()-1,defaultLabel,labels.toArray(new Label[labels.size()]));
        	        }
    ;
sInternalNameOrDesc returns[String desc]
    : (a=sArrayType { $desc=unEscape($a.text); }|b=OBJECT_TYPE { $desc=unEscape($b.text); })
    | c=sId {  $desc= "L"+unEscape($c.text)+";"; }
    | DSTRING {  $desc= "L"+unEscapeString($DSTRING.text)+";"; }
    | STRING {  $desc= "L"+unEscapeString($STRING.text)+";"; }
    ;
sInternalNameOrDescACC returns[String desc]
        : (a=sArrayType { $desc=unEscape($a.text); }|b=OBJECT_TYPE { $desc=unEscape($b.text); })
        | c=sAnyId {  $desc= "L"+unEscape($c.text)+";"; }
        | DSTRING {  $desc= "L"+unEscapeString($DSTRING.text)+";"; }
        | STRING {  $desc= "L"+unEscapeString($STRING.text)+";"; }
        ;
sInternalNameOrDescNoString returns[String desc]
    : (a=sArrayType { $desc=unEscape($a.text); }|b=OBJECT_TYPE { $desc=unEscape($b.text); })
    | c=sAnyId {  $desc= "L"+unEscape($c.text)+";"; }
    ;
sAnyId
    : ACC | sId
    ;
sAnyIdOrString returns[String str]
    : sAnyId { $str=unEscape($sAnyId.text);}
    | STRING { $str=unEscapeString($STRING.text); }
    | DSTRING { $str=unEscapeString($DSTRING.text); }
    ;
sOwnerAndName returns[String ownerInternalName, String memberName]
    :    a=sArrayType '/' x=sAnyId { if($x.text.contains("/")){ throw new RecognitionException(input);}  $ownerInternalName=unEscape($a.text); $memberName=unEscape($x.text); }
    | b=sClassDesc '->' x=sAnyId { if($x.text.contains("/")){ throw new RecognitionException(input);} $ownerInternalName=Type.getType(unEscape($b.text)).getInternalName(); $memberName=unEscape($x.text);  }
    | c=sId { String cstr=$c.text; int idx=cstr.lastIndexOf('/'); if(idx<=0) { throw new RecognitionException(input); } $ownerInternalName=unEscape(cstr.substring(0,idx)); $memberName=unEscape(cstr.substring(idx+1)); }
    ;
sMethodObject returns[String ownerInternalName, String memberName, String desc]
    :   ( a=sArrayType '/' x=sAnyId { if($x.text.contains("/")){ throw new RecognitionException(input);}  $ownerInternalName=unEscape($a.text); $memberName=unEscape($x.text); }
        | b=sClassDesc '->' x=sAnyId { if($x.text.contains("/")){ throw new RecognitionException(input);} $ownerInternalName=Type.getType(unEscape($b.text)).getInternalName(); $memberName=unEscape($x.text);  }
        | c=sId { String cstr=$c.text; int idx=cstr.lastIndexOf('/'); if(idx<=0) { throw new RecognitionException(input); } $ownerInternalName=unEscape(cstr.substring(0,idx)); $memberName=unEscape(cstr.substring(idx+1)); }
        )
      d=sMethodDesc { $desc=unEscape($d.text);  }
    ;
sFieldObject returns[String ownerInternalName, String memberName, String type]
    :   ( a=sArrayType '/' x=sAnyId { if($x.text.contains("/")){ throw new RecognitionException(input);}  $ownerInternalName=unEscape($a.text); $memberName=unEscape($x.text); }
        | b=sClassDesc '->' x=sAnyId ':' { if($x.text.contains("/")){ throw new RecognitionException(input);} $ownerInternalName=Type.getType(unEscape($b.text)).getInternalName(); $memberName=unEscape($x.text);  }
        | c=sId { String cstr=$c.text; int idx=cstr.lastIndexOf('/'); if(idx<=0) { throw new RecognitionException(input); } $ownerInternalName=unEscape(cstr.substring(0,idx)); $memberName=unEscape(cstr.substring(idx+1)); }
        )
      b=sClassDesc { $type=unEscape($b.text);  }
    ;
