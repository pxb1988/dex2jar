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
        } else if (name.equals("synthetic")) {
            return ACC_SYNTHETIC;
        } else if (name.equals("annotation")) {
            return ACC_ANNOTATION;
        } else if (name.equals("enum")) {
            return ACC_ENUM;
        }
        throw new RuntimeException("not support access flags " + name);
    }
    private static int getOp(String str) {
            switch (str) {
            case "nop":
                return NOP;
            case "aconst_null":
                return ACONST_NULL;
            case "iconst_m1":
                return ICONST_M1;
            case "iconst_0":
                return ICONST_0;
            case "iconst_1":
                return ICONST_1;
            case "iconst_2":
                return ICONST_2;
            case "iconst_3":
                return ICONST_3;
            case "iconst_4":
                return ICONST_4;
            case "iconst_5":
                return ICONST_5;
            case "lconst_0":
                return LCONST_0;
            case "lconst_1":
                return LCONST_1;
            case "fconst_0":
                return FCONST_0;
            case "fconst_1":
                return FCONST_1;
            case "fconst_2":
                return FCONST_2;
            case "dconst_0":
                return DCONST_0;
            case "dconst_1":
                return DCONST_1;
            case "bipush":
                return BIPUSH;
            case "sipush":
                return SIPUSH;
            case "ldc_w":
            case "ldc2_w":
            case "ldc":
                return LDC;
            case "iload":
                return ILOAD;
            case "lload":
                return LLOAD;
            case "fload":
                return FLOAD;
            case "dload":
                return DLOAD;
            case "aload":
                return ALOAD;
            case "iaload":
                return IALOAD;
            case "laload":
                return LALOAD;
            case "faload":
                return FALOAD;
            case "daload":
                return DALOAD;
            case "aaload":
                return AALOAD;
            case "baload":
                return BALOAD;
            case "caload":
                return CALOAD;
            case "saload":
                return SALOAD;
            case "istore":
                return ISTORE;
            case "lstore":
                return LSTORE;
            case "fstore":
                return FSTORE;
            case "dstore":
                return DSTORE;
            case "astore":
                return ASTORE;
            case "iastore":
                return IASTORE;
            case "lastore":
                return LASTORE;
            case "fastore":
                return FASTORE;
            case "dastore":
                return DASTORE;
            case "aastore":
                return AASTORE;
            case "bastore":
                return BASTORE;
            case "castore":
                return CASTORE;
            case "sastore":
                return SASTORE;
            case "pop":
                return POP;
            case "pop2":
                return POP2;
            case "dup":
                return DUP;
            case "dup_x1":
                return DUP_X1;
            case "dup_x2":
                return DUP_X2;
            case "dup2":
                return DUP2;
            case "dup2_x1":
                return DUP2_X1;
            case "dup2_x2":
                return DUP2_X2;
            case "swap":
                return SWAP;
            case "iadd":
                return IADD;
            case "ladd":
                return LADD;
            case "fadd":
                return FADD;
            case "dadd":
                return DADD;
            case "isub":
                return ISUB;
            case "lsub":
                return LSUB;
            case "fsub":
                return FSUB;
            case "dsub":
                return DSUB;
            case "imul":
                return IMUL;
            case "lmul":
                return LMUL;
            case "fmul":
                return FMUL;
            case "dmul":
                return DMUL;
            case "idiv":
                return IDIV;
            case "ldiv":
                return LDIV;
            case "fdiv":
                return FDIV;
            case "ddiv":
                return DDIV;
            case "irem":
                return IREM;
            case "lrem":
                return LREM;
            case "frem":
                return FREM;
            case "drem":
                return DREM;
            case "ineg":
                return INEG;
            case "lneg":
                return LNEG;
            case "fneg":
                return FNEG;
            case "dneg":
                return DNEG;
            case "ishl":
                return ISHL;
            case "lshl":
                return LSHL;
            case "ishr":
                return ISHR;
            case "lshr":
                return LSHR;
            case "iushr":
                return IUSHR;
            case "lushr":
                return LUSHR;
            case "iand":
                return IAND;
            case "land":
                return LAND;
            case "ior":
                return IOR;
            case "lor":
                return LOR;
            case "ixor":
                return IXOR;
            case "lxor":
                return LXOR;
            case "iinc":
                return IINC;
            case "i2l":
                return I2L;
            case "i2f":
                return I2F;
            case "i2d":
                return I2D;
            case "l2i":
                return L2I;
            case "l2f":
                return L2F;
            case "l2d":
                return L2D;
            case "f2i":
                return F2I;
            case "f2l":
                return F2L;
            case "f2d":
                return F2D;
            case "d2i":
                return D2I;
            case "d2l":
                return D2L;
            case "d2f":
                return D2F;
            case "i2b":
                return I2B;
            case "i2c":
                return I2C;
            case "i2s":
                return I2S;
            case "lcmp":
                return LCMP;
            case "fcmpl":
                return FCMPL;
            case "fcmpg":
                return FCMPG;
            case "dcmpl":
                return DCMPL;
            case "dcmpg":
                return DCMPG;
            case "ifeq":
                return IFEQ;
            case "ifne":
                return IFNE;
            case "iflt":
                return IFLT;
            case "ifge":
                return IFGE;
            case "ifgt":
                return IFGT;
            case "ifle":
                return IFLE;
            case "if_icmpeq":
                return IF_ICMPEQ;
            case "if_icmpne":
                return IF_ICMPNE;
            case "if_icmplt":
                return IF_ICMPLT;
            case "if_icmpge":
                return IF_ICMPGE;
            case "if_icmpgt":
                return IF_ICMPGT;
            case "if_icmple":
                return IF_ICMPLE;
            case "if_acmpeq":
                return IF_ACMPEQ;
            case "if_acmpne":
                return IF_ACMPNE;
            case "goto":
                return GOTO;
            case "jsr":
                return JSR;
            case "ret":
                return RET;
            case "tableswitch":
                return TABLESWITCH;
            case "lookupswitch":
                return LOOKUPSWITCH;
            case "ireturn":
                return IRETURN;
            case "lreturn":
                return LRETURN;
            case "freturn":
                return FRETURN;
            case "dreturn":
                return DRETURN;
            case "areturn":
                return ARETURN;
            case "return":
                return RETURN;
            case "getstatic":
                return GETSTATIC;
            case "putstatic":
                return PUTSTATIC;
            case "getfield":
                return GETFIELD;
            case "putfield":
                return PUTFIELD;
            case "invokevirtual":
                return INVOKEVIRTUAL;
            case "invokespecial":
                return INVOKESPECIAL;
            case "invokestatic":
                return INVOKESTATIC;
            case "invokeinterface":
                return INVOKEINTERFACE;
            case "invokedynamic":
                return INVOKEDYNAMIC;
            case "new":
                return NEW;
            case "newarray":
                return NEWARRAY;
            case "anewarray":
                return ANEWARRAY;
            case "arraylength":
                return ARRAYLENGTH;
            case "athrow":
                return ATHROW;
            case "checkcast":
                return CHECKCAST;
            case "instanceof":
                return INSTANCEOF;
            case "monitorenter":
                return MONITORENTER;
            case "monitorexit":
                return MONITOREXIT;
            case "multianewarray":
                return MULTIANEWARRAY;
            case "ifnull":
                return IFNULL;
            case "ifnonnull":
                return IFNONNULL;
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
    :  '\'' ( ESC_SEQ | ~('\\'|'"') )* '\''
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
    'annotation' | 'volatile' | 'transient' | 'declared-synchronized' ;
ANNOTATION_VISIBLITY: 'visible' | 'invisible' ;
METHOD_ANNOTATION_VISIBLITY: 'visibleparam' | 'invisibleparam';
INNER	:	'inner';
OUTTER	:	'outer';
OP0	:	'nop'|'monitorenter'|'monitorexit'|'pop2'|'pop'
	|	'iconst_m1'
	|('a'|'i')'const_' ('0'..'5')
	|('f'|'d'|'l')'const_' ('0'..'1')
	|'aconst_null'
	|('a'|'d'|'f'|'i'|'l')? 'return'
	|('a'|'d'|'f'|'i'|'l') ('store'|'load') '_' ('0'..'3')
	|('a'|'d'|'f'|'i'|'l') ('astore'|'aload')
	|'dcmpg'|'dcmpl' | 'lcmp' |'fcmpg'|'fcmpl'
	|'athrow'
	|('i'|'f'|'d'|'l')'add'|'div'|'sub'|'mul'|'rem'|'shl'|'shr'|'ushr'|'and'|'or'|'xor'|'neg'
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
	|       'invokeinterface'
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
ID  :('B'|'Z'|'S'|'C'|'I'|'F'|'J'|'D'|'e'|'s'|'c') (ESC_SEQ| ~('\\'|'\r'|'\n'|'\t'|' '|':'|'-'|'='|','|'{'|'}'|'('|')') )+
    |(ESC_SEQ| ~('\\' | '\r' | '\n' | '\t' | '\'' | '\"' | ' ' | ':' | '-' | '=' | '.' | ',' | '&' | '@' | '/'
                      | '{'|'['|']'|'}'|'('|')'
                      |'B'|'Z'|'S'|'C'|'I'|'F'|'J'|'D'|'e'|'s'|'c'
                      |'0'..'9'
                 )) (ESC_SEQ| ~('\\'|'\r'|'\n'|'\t'|' '|':'|'-'|'='|','|'{'|'}'|'('|')') )*
    ;
PARRAY_TYPE
	:	'['+ FRAGMENT_OBJECT_TYPE
	|	'[' '['+ FRAGMENT_PRIMITIVE_TYPE
	;
LOW_E	:	'e';
LOW_S	:	's';
LOW_C	:	'c';
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
        |  '.source' a4=sId  { cn.sourceFile=$a4.text; }
		|  '.class' i=sAccList {cn.access|=$i.acc;} b=sId { cn.name=$b.text; }
		|  '.interface' i=sAccList {cn.access|=ACC_INTERFACE|$i.acc;} b=sId { cn.name=$b.text; }
		|  '.super' a1=sId  {  cn.superName=$a1.text; }
		|  '.implements' a2=sId { if(cn.interfaces==null){cn.interfaces=new ArrayList<>();}  cn.interfaces.add($a2.text); }
		|  '.enclosing method' ownerAndName=sId {tmp=null;} (b=sMethodDesc{tmp=$b.text;})? {String on[]=parseOwnerAndName($ownerAndName.text);cn.visitOuterClass(on[0],on[1],tmp);}
		|  sDeprecateAttr  { cn.access|=ACC_DEPRECATED; }
		|  '.debug' a=STRING  { cn.sourceDebug=unEscapeString($a.text); }
		|  '.attribute' sId STRING     { System.err.println("ignore .attribute"); }
		|  '.inner class' (i=sAccList sId{tmpInt=$i.acc;})? {tmp=null;tmp2=null;} ('inner' a3=sId{tmp=$a3.text;})? ('outer' a4=sId{tmp2=$a4.text;})?   { cn.visitInnerClass(null,tmp2,tmp,tmpInt); }
		|  '.signature' a=STRING { cn.signature=unEscapeString($a.text); }
		|  '.no_super' {cn.superName=null;}
		|  '.class_attribute' sId STRING    { System.err.println("ignore .class_attribute"); }
		|  '.enclosing_method_attr' a=STRING b1=STRING c=STRING   {cn.visitOuterClass($a.text,$b1.text,$c.text);}
		|  '.inner_class_attr' ('.inner_class_spec_attr' a=STRING b2=STRING i=sAccList '.end' '.inner_class_spec_attr' { cn.visitInnerClass(null,unEscape($a.text),unEscape($b2.text),i); } )* '.end' '.inner_class_attr'
		|  s=sSigAttr  { cn.signature=$s.s; }
		|  sSynthetic   {cn.access|=ACC_SYNTHETIC;}
		;
sSigAttr returns[String s]:	'.signature_attr' a=STRING{ $s=$a.text; };
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
sId	:	ID|LOW_E|LOW_S|LOW_C|AT|AND|UP_Z|UP_B|UP_S|UP_C|UP_I|UP_F|UP_D|UP_J|ANNOTATION_VISIBLITY|METHOD_ANNOTATION_VISIBLITY|INNER|OUTTER
	|	IIOP|IOP|JOP|OP0|LDC|XFIELD|XTYPE|XINVOKE|MULTIANEWARRAY|LOOKUPSWITCH|TABLESWITCH|DEFAULT|FROM|TO|USING|STACK|LOCALS|HIGH|INVOKEDYNAMIC|VOID_TYPE
	| WBOOLEAN| WBYTE | WSHORT|WCHAR|WINTEGER|WLONG|WFLOAT|WDOUBLE |XNEWARRAY
	;
sWord : sId ;
sAnnotation
	: '.annotation' (b=ANNOTATION_VISIBLITY a=sId { currentAnnotationVisitor= currentAv.visitAnnotation($a.text,!$b.text.contains("invisible")); } |
	                  b=METHOD_ANNOTATION_VISIBLITY c=INT a=sId {currentAnnotationVisitor=currentAv.visitParameterAnnotation(parseInt($c.text),$a.text,!$b.text.contains("invisible"));}
	                ) sAnnotationElement*
	  '.end annotation'
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
    :   a=sId (   LOW_E c=OBJECT_TYPE '=' b=sWord  { _t.visit($a.text,new String[]{$c.text,$b.text}); }
           | AT b2=OBJECT_TYPE '=' {  currentAnnotationVisitor=new AnnotationNode($b2.text);} sSubannotation  { _t.visit($a.text,currentAnnotationVisitor); }
           | LOW_C  '=' b1=sClassDesc { currentAnnotationVisitor.visit($a.text,Type.getType($b1.text)); }
           | LOW_S  '=' b3=STRING      { currentAnnotationVisitor.visit($a.text,unEscapeString($b3.text)); }
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
           | ARRAY_LOW_E c=OBJECT_TYPE '='  (b=sWord {  array.add(new String[]{$c.text,$b.text}); } )+  { currentAnnotationVisitor.visit($a.text,array); }
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
              |a5=sClassDesc {fn.value=parseValue(fn.desc,unEscape($a5.text));}
              )
        )?
            (
            s=sSigAttr{fn.signature=$s.s;}
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
}
    :	'.method' i=sAccList n=sMemberName t=sMethodDesc {mn.access|=i;mn.name=$n.name;mn.desc=unEscape($t.text);}
            (s=sSigAttr{mn.signature=s;}
                |sDeprecateAttr{ cn.access|=ACC_DEPRECATED; }
                |sSynthetic {cn.access|=ACC_SYNTHETIC;}
                |sVisibiltyAnnotation
                |sAnnotation
                |'.throws' a=sId {  mn.exceptions.add(unEscape($a.text)); }
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

sLabel	:	ID|LOW_E|LOW_S|LOW_C|AT|AND|UP_Z|UP_B|UP_S|UP_C|UP_I|UP_F|UP_D|UP_J|ANNOTATION_VISIBLITY|METHOD_ANNOTATION_VISIBLITY|INNER|OUTTER
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
	                               |e=sClassDesc {mn.visitLdcInsn(unEscape($e.text));}
	                              )
	|	a=XFIELD e=sId f=sClassDesc {  line($a.line);  String oa[]=parseOwnerAndName($e.text); mn.visitFieldInsn(getOp($a.text),oa[0],oa[1],unEscape($f.text));   }
	|   a=XNEWARRAY {line($a.line); }  (
	               WBOOLEAN{mn.visitIntInsn(NEWARRAY,T_BOOLEAN);}
	               |WBYTE{mn.visitIntInsn(NEWARRAY,T_BYTE);}
	               |WSHORT{mn.visitIntInsn(NEWARRAY,T_SHORT);}
	               |WCHAR{mn.visitIntInsn(NEWARRAY,T_CHAR);}
	               |WINTEGER{mn.visitIntInsn(NEWARRAY,T_INT);}
	               |WLONG{mn.visitIntInsn(NEWARRAY,T_LONG);}
	               |WFLOAT{mn.visitIntInsn(NEWARRAY,T_FLOAT);}
	               |WDOUBLE{mn.visitIntInsn(NEWARRAY,T_DOUBLE);})
	|	a=XTYPE ff=sId {
	                       line($a.line);
	                          mn.visitTypeInsn(getOp($a.text),unEscape($ff.text));
	            }
	|	a=JOP z=sLabel  { line($a.line); visitJOP(getOp($a.text),getLabel($z.text)); }
	|	a=XINVOKE e=sId  m=sMethodDesc   {line($a.line);
	                    String oa[]=parseOwnerAndName($e.text);
	                    mn.visitMethodInsn(getOp($a.text),oa[0],oa[1],unEscape($m.text));
	                  }
	|	a=INVOKEDYNAMIC sId sMethodDesc sId sMethodDesc '(' sInvokeDynamicE (',' sInvokeDynamicE)* ')'  {line($a.line); if(1==1) throw new RuntimeException("not support Yet!");}
	|	a=MULTIANEWARRAY ff=sClassDesc c=INT   {line($a.line); mn.visitMultiANewArrayInsn(unEscape($ff.text),parseInt($c.text)); }
	|   z=sLabel { Label label=getLabel($z.text); mn.visitLabel(label); if(rebuildLine) {mn.visitLineNumber($z.start.getLine(),label);}
	 }
	|	'.catch' e=sId 'from' z1=sLabel 'to' z2=sLabel 'using' z3=sLabel { String type="all".equals($e.text)?null:unEscape($e.text); mn.visitTryCatchBlock(getLabel($z1.text),getLabel($z2.text),getLabel($z3.text),type); }
	|	'.limit' 'stack' ('?' | INT)
	|	'.limit' 'locals' ('?'| INT)
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
	|	a=TABLESWITCH c=INT ';' 'high' '=' d=INT (z=sLabel  {labels.add(getLabel($z.text));} )* (DEFAULT ':' z=sLabel {defaultLabel=getLabel($z.text);})    {
	        line($a.line); mn.visitTableSwitchInsn(parseInt($c.text),parseInt($d.text),defaultLabel,labels.toArray(new Label[labels.size()]));
	        }
    ;
