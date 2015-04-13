grammar Smali;

options{
backtrack=true;
}

@header {
package com.googlecode.d2j.smali;
import com.googlecode.d2j.*;
import com.googlecode.d2j.visitors.*;
import static com.googlecode.d2j.DexConstants.*;
import static com.googlecode.d2j.smali.Utils.*;
}
@lexer::header {
package com.googlecode.d2j.smali;
}

@members{
    DexFileVisitor dexFileVisitor;
    DexClassVisitor dexClassVisitor;
    DexAnnotationAble dexAnnotationAble;
    List<String> regs=new ArrayList();
    List tmpList=new ArrayList();
    Method currentMethod;
    String source;
    int ins_in;
    String clzName;
    List<String> interfaceNames = new ArrayList(5);
    Map<String,DexLabel> labelsMap=new HashMap();
    String superName;
    int locals=-1;
    public DexLabel getLabel(String txt){
        txt=unEscapeId(txt);
	    DexLabel lab=labelsMap.get(txt);
	    if(lab==null){
	       lab=new DexLabel();
	       labelsMap.put(txt,lab);
	       lab.displayName=txt;
	    }
	    return lab;
    }
    public void accept(DexFileVisitor dexFileVisitor) throws RecognitionException {
        this.dexFileVisitor = dexFileVisitor;
        sFile();
    }
    int getReg(String s){

    int r= Integer.parseInt(s.substring(1));
    if(s.startsWith("p")||s.startsWith("P")){
       r+=this.locals;
    }
    return r;
    }
    void buildDexClassVisitor(int acc, String name) {
        this.clzName = name;
        dexAnnotationAble = dexClassVisitor = dexFileVisitor.visit(acc, name, superName, interfaceNames.toArray(new String[0]));
        
    }
    void endClass(){
      if(dexClassVisitor!=null){
       if(this.source!=null){
            dexClassVisitor.visitSource(this.source);
        }
      dexClassVisitor.visitEnd();
     
      }
    }

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
    : (('+'|'-')?( ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT)| ( ('+'|'-') F_INFINITY) )
    ;
fragment
F_NAN : ('N'|'n') ('A'|'a') ('N'|'n');
F_INFINITY: ('I'|'i') ('N'|'n') ('F'|'f') ('I'|'i') ('N'|'n') ('I'|'i') ('T'|'t') ('Y'|'y') ;
FLOAT_NAN : F_NAN ('f'|'F');
DOUBLE_NAN: F_NAN ('d'|'D')?;
FLOAT_INFINITY: F_INFINITY ('f'|'F');
DOUBLE_INFINITY: F_INFINITY ('d'|'D')?;
BASE_FLOAT	:	(('0'..'9')+|FLOAT_NENT) ('f'|'F');
BASE_DOUBLE	:	FLOAT_NENT ('d'|'D')? | ('0'..'9')+ ('d'|'D') ;
CHAR	:	'\''  ( ESC_SEQ | ~('\\'|'\'') ) '\'';
LONG	:	INT_NENT ('L'|'l');
SHORT	:	INT_NENT ('S'|'s');
BYTE	:	INT_NENT ('T'|'t');
INT	:	INT_NENT;
BOOLEAN	:	'true'|'false';
COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '#' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
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

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\''|'\"'|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
VOID_TYPE:'V';
fragment
FRAGMENT_PRIMITIVE_TYPE:'B'|'Z'|'S'|'C'|'I'|'F'|'J'|'D';
fragment
FRAGMENT_OBJECT_TYPE: 'L' (ESC_SEQ |~(';'|':'|'\\'|' '|'\n'|'\t'|'\r'|'('|')'))+ ';' ;
fragment
FRAGMENT_ARRAY_TYPE: ('[')+ (FRAGMENT_PRIMITIVE_TYPE|FRAGMENT_OBJECT_TYPE);

PRIMITIVE_TYPE: FRAGMENT_PRIMITIVE_TYPE;
OBJECT_TYPE: FRAGMENT_OBJECT_TYPE;
ARRAY_TYPE: FRAGMENT_ARRAY_TYPE;
SIMPLE_TYPE_LIST: FRAGMENT_PRIMITIVE_TYPE+;
COMPLEX_TYPE_LIST: (FRAGMENT_OBJECT_TYPE | FRAGMENT_PRIMITIVE_TYPE | FRAGMENT_ARRAY_TYPE)+ ;

ACC:	'public' | 'private' | 'protected' | 'static' | 'final' | 'synchronized' | 'bridge' | 'varargs' | 'native' |
    'abstract' | 'strictfp' | 'synthetic' | 'constructor' | 'interface' | 'enum' |
    'annotation' | 'volatile' | 'transient' | 'declared-synchronized' ;
ANN_VISIBLE
	:	'build' | 'runtime' | 'system';
REGISTER:	('v'|'V'|'p'|'P') '0'..'9'+;
NOP	:	'nop';
MOVE	:	'move';
RETURN	:	'return';
CONST	:	'const';
THROW	:	'throw';
GOTO	:	'goto';
AGET	:	'aget';
APUT	:	'aput';
IGET	:	'iget';
IPUT	:	'iput';
SGET	:	'sget';
SPUT	:	'sput';
NULL    :   'null';
ID  :	('a'..'z'|'A'..'Z'|'_'|'$'|ESC_SEQ) (ESC_SEQ| ~('\\'|'\r'|'\n'|'\t'|' '|':'|'-'|'='|','|'{'|'}'|'('|')') )*
    ;


sFile	:	'.class' acc=sAccList a=OBJECT_TYPE 
( sSuper | sInterface) *
{buildDexClassVisitor(acc,$a.text);}
(sSource |sMethod|sField|sAnnotation)*
                                            '.end class'?  {endClass();}
	;
sSource 	:	'.source' a=STRING {this.source=unEscape($a.text);};
sSuper	:	'.super' a=OBJECT_TYPE {this.superName=$a.text;};
sInterface
	:	'.implements' a=OBJECT_TYPE {this.interfaceNames.add($a.text);};
sMethod	@init{ boolean isStatic=false; SmaliCodeVisitor dcv=null;     DexMethodVisitor dexMethodVisitor=null; DexDebugVisitor debugVisitor=null; int paramIndex=0; }
	:	'.method' acc=sAccList {isStatic=0!=(acc&ACC_STATIC);} 
		( m1=sMethodF { dexMethodVisitor=dexClassVisitor.visitMethod(acc,$m1.m); this.currentMethod=m1; this.ins_in=methodIns($m1.m,isStatic); }
			| m2=sMethodP { dexMethodVisitor=dexClassVisitor.visitMethod(acc,$m2.m);this.currentMethod=$m2.m; this.ins_in=methodIns($m2.m,isStatic);}
		) 
		{this.labelsMap.clear();}
		(  {dexAnnotationAble=dexMethodVisitor;} sAnnotation  {dexAnnotationAble=null;}
		  | {  if(dcv ==null) { dcv=new SmaliCodeVisitor(dexMethodVisitor.visitCode());} if(debugVisitor==null) { debugVisitor=dcv.visitDebug(); }  } sParameter[dexMethodVisitor,paramIndex,debugVisitor] {paramIndex++;  }
		  | {  if(dcv ==null) { dcv=new SmaliCodeVisitor(dexMethodVisitor.visitCode());} if(debugVisitor==null) { debugVisitor=dcv.visitDebug(); }  } sDebug[dcv,debugVisitor]
		  | {  if(dcv ==null) { dcv=new SmaliCodeVisitor(dexMethodVisitor.visitCode());}  } sInstruction[dcv]
		 )*
		'.end method' {if(dcv !=null) {dcv.visitEnd();} dexMethodVisitor.visitEnd();};
sField	@init{DexFieldVisitor dexFieldVisitor=null;}: '.field' acc=sAccList (f=sFieldF|f=sFieldP) ('=' v=sFieldValue)? {dexAnnotationAble=dexFieldVisitor=dexClassVisitor.visitField(acc,f,v);}
		(sAnnotation*
		'.end field')? {dexFieldVisitor.visitEnd();dexAnnotationAble=null;}
	;
sAnnotation @init{DexAnnotationVisitor dexAnnotationVisitor=null;}
	: 	'.annotation' v=ANN_VISIBLE d=OBJECT_TYPE { dexAnnotationVisitor= dexAnnotationAble.visitAnnotation($d.text,getAnnVisibility($v.text)); }
		(kk=sAnnotationKeyName '=' vv=sAnnotationValue {doAccept(dexAnnotationVisitor,$kk.text,$vv.v);})*
		 '.end annotation' {dexAnnotationVisitor.visitEnd();}
	;
sSubannotation returns[Object v] @init{Ann an=new Ann();}
	:	'.subannotation' n=OBJECT_TYPE {an.name=$n.text;}(kk=sAnnotationKeyName '=' vv=sAnnotationValue {an.put($kk.text,vv);})* '.end subannotation' {$v=an;}
	;
sAccList returns [int acc]: {acc=0;}(a=ACC {acc|=getAcc($a.text);})*  ;
sType: OBJECT_TYPE | PRIMITIVE_TYPE | ARRAY_TYPE;
sFieldF	returns [Field f]:	o=OBJECT_TYPE '->' n=sFieldNameF ':' t=sType{f=new Field($o.text,$n.text,$t.text);};
sFieldP	returns [Field f]:	n=sFieldNameP ':' t=sType {f=new Field(this.clzName,$n.text,$t.text);};
sMethodP returns[Method m]:	n=sMethodNameP t=sMethodArgAndRet {$m=new Method(this.clzName,$n.text,toTypeList($t.as),$t.ret);};
sMethodF returns[Method m]:	o=(ARRAY_TYPE|OBJECT_TYPE) '->' n=sMethodNameF t=sMethodArgAndRet {$m=new Method($o.text,$n.text,toTypeList($t.as),$t.ret);};
sMethodArgAndRet returns[String as,String ret]: '(' a=(PRIMITIVE_TYPE|OBJECT_TYPE|ARRAY_TYPE|SIMPLE_TYPE_LIST|COMPLEX_TYPE_LIST| /*empty*/) ')' b=(PRIMITIVE_TYPE|OBJECT_TYPE|ARRAY_TYPE|VOID_TYPE) {$as=$a.text;$ret=$b.text;};
sMethodNameF
	:	sMethodNameP|ACC;
sMethodNameP
	:	'<clinit>'|'<init>'|sBaseMemberName;
sFieldNameF
	:	sFieldNameP|ACC;
sFieldNameP
	:	sBaseMemberName;
sBaseMemberName
	: PRIMITIVE_TYPE |VOID_TYPE | SIMPLE_TYPE_LIST
	|ANN_VISIBLE|REGISTER|BOOLEAN|ID | NULL
	|FLOAT_INFINITY|DOUBLE_INFINITY|FLOAT_NAN|DOUBLE_NAN
	|NOP|MOVE|RETURN|CONST|THROW|GOTO|AGET|APUT|IGET|IPUT|SGET|SPUT
	;
sFieldValue returns[Object s]
	:	a=sBaseValue {$s=$a.v;};
sParameter[DexMethodVisitor dmv, int paramIndex, DexDebugVisitor debugVisitor]
	:	('.parameter' (a=STRING { debugVisitor.visitParameterName(paramIndex,unescapeStr($a.text)); } )?  ( ( {dexAnnotationAble=dmv.visitParameterAnnotation(paramIndex);} sAnnotation {dexAnnotationAble=null;})* '.end parameter')?)
		| ('.param' r1=REGISTER (',' a=STRING {  debugVisitor.visitParameterName(getReg($r1.text), unescapeStr($a.text)); } )? (({dexAnnotationAble=dmv.visitParameterAnnotation(getReg($r1.text));} sAnnotation {dexAnnotationAble=null;})* '.end param')?)
	;
sAnnotationKeyName
	:	sBaseMemberName|ACC;
sAnnotationValue returns[Object v]
	:	a=sSubannotation{$v=$a.v;}|b=sBaseValue{$v=$b.v;}|c=sArrayValue{$v=$c.v;};// field,method,array,subannotation
sBaseValue returns[Object v]
	:	a=STRING {$v=unescapeStr($a.text);}
	|a=BYTE {$v=parseByte($a.text);}
	|a=SHORT {$v=parseShort($a.text);}
	|a=CHAR {$v=unescapeChar($a.text);}
	|a=INT {$v=parseInt($a.text);}
	|a=LONG {$v=parseLong($a.text);}
	|df=sFloat {$v=parseFloat($df.text);}
	|dd=sDouble {$v=parseDouble($dd.text);}
	|a=BOOLEAN {$v=Boolean.parseBoolean($a.text);}
	|c=sMethodF {$v=$c.m;}
	|a=OBJECT_TYPE {$v=new DexType($a.text);}
	|NULL {$v=null;}
	|b=sEnumValue {$v=$b.f;}
	;
sArrayValue returns[Object v] @init{List<Object> vs=new ArrayList();} : '{' (a=sAnnotationValue{vs.add(a);})? (',' a=sAnnotationValue{vs.add(a);})* '}' {$v=vs;};
sEnumValue returns[Field f]
	:	'.enum' a=sFieldF{$f=a;};
sFloat: BASE_FLOAT| FLOAT_INFINITY | FLOAT_NAN;
sDouble: BASE_DOUBLE|DOUBLE_INFINITY|DOUBLE_NAN;
sDebug[SmaliCodeVisitor dcv, DexDebugVisitor ddv]	@init{ String sig=null; String varName=null;}
                                :'.line' a=INT { DexLabel label=new DexLabel(); label.displayName=".line " + $a.text ; dcv.visitLabel(label); ddv.visitLineNumber(parseInt($a.text),label);  }
                                |'.local' r1=REGISTER ',' (key1=sAnnotationKeyName { varName=unEscape($key1.text); } | key2= STRING { varName=unescapeStr($key2.text); }) ':' desc=sType {sig=null;} (',' ss=STRING { sig=unEscape($ss.text); })?   { DexLabel label=new DexLabel(); label.displayName=".local " + $r1.text ; dcv.visitLabel(label);  ddv.visitStartLocal(getReg($r1.text),label,varName,$desc.text,sig); }
                                | '.end local' r1=REGISTER  { DexLabel label=new DexLabel(); label.displayName=".end local " + $r1.text ; dcv.visitLabel(label); ddv.visitEndLocal(getReg($r1.text),label);  }
                                |'.restart local'  r1=REGISTER  { DexLabel label=new DexLabel(); label.displayName=".restart local " + $r1.text ; dcv.visitLabel(label); ddv.visitRestartLocal(getReg($r1.text),label);  }
                                |'.prologue' { DexLabel label=new DexLabel(); label.displayName=".prologue"  ; dcv.visitLabel(label); ddv.visitPrologue(label);  }
                                |'.epiogue'  { DexLabel label=new DexLabel(); label.displayName=".epiogue" ; dcv.visitLabel(label); ddv.visitEpiogue(label);  }
                                ;
sInstruction[SmaliCodeVisitor dcv]
	:	('.registers' a=INT {int reg=parseInt($a.text);this.locals=reg-ins_in; dcv.visitRegister(reg); }) 
	| ('.locals' a=INT {this.locals=parseInt($a.text); dcv.visitRegister(locals+ins_in);}) 
	| ('.catch' a=OBJECT_TYPE '{' b=sLabel '..' c=sLabel  '}' d=sLabel { dcv.visitTryCatch(getLabel($b.text),getLabel($c.text),new DexLabel[]{ getLabel($d.text) }, new String[]{$a.text}); } )
	| ('.catchall' '{' b=sLabel '..' c=sLabel  '}' d=sLabel { dcv.visitTryCatch(getLabel($b.text),getLabel($c.text),new DexLabel[]{ getLabel($d.text) }, new String[]{null}); })
	|(f0x { dcv.visitStmt0R(getOp($f0x.text)); })
	|f0t sLabel { dcv.visitJumpStmt(getOp($f0t.text),-1,-1,getLabel($sLabel.text)); }
	|f1t r1=REGISTER ',' sLabel { dcv.visitJumpStmt(getOp($f1t.text),getReg($r1.text),-1,getLabel($sLabel.text)); }
	|f2t r1=REGISTER ',' r2=REGISTER ',' sLabel { dcv.visitJumpStmt(getOp($f2t.text),getReg($r1.text),getReg($r2.text),getLabel($sLabel.text)); }
	|f1x r1=REGISTER { dcv.visitStmt1R(getOp($f1x.text),getReg($r1.text)); }
	|fconst r1=REGISTER ',' (d1=INT { dcv.visitConstStmt(getOp($fconst.text),getReg($r1.text),parseInt($d1.text)); } | d2=LONG { dcv.visitConstStmt(getOp($fconst.text),getReg($r1.text),parseLong($d2.text)); } )
	|fconstString  r1=REGISTER ',' str=STRING { dcv.visitConstStmt(getOp($fconstString.text),getReg($r1.text),unEscape($str.text)); }
	|ft1c r1=REGISTER ',' obj=(OBJECT_TYPE | ARRAY_TYPE)  { String t=$ft1c.text; if(t.equals("const-class")){ 
				dcv.visitConstStmt(getOp(t),getReg($r1.text),new DexType($obj.text)); 
			} else {
				dcv.visitTypeStmt(getOp(t),getReg($r1.text),-1,$obj.text);
			} }
	|ft2c r1=REGISTER ',' r2=REGISTER ',' obj=(OBJECT_TYPE|ARRAY_TYPE) { dcv.visitTypeStmt(getOp($ft2c.text),getReg($r1.text),getReg($r2.text),$obj.text); }
	|ff1c r1=REGISTER ',' f=sFieldF { dcv.visitFieldStmt(getOp($ff1c.text),getReg($r1.text),-1,$f.f); }
	|ff2c r1=REGISTER ',' r2=REGISTER ',' f=sFieldF { dcv.visitFieldStmt(getOp($ff2c.text),getReg($r1.text),getReg($r2.text),$f.f); }
	|f2x r1=REGISTER ',' r2=REGISTER { dcv.visitStmt2R(getOp($f2x.text),getReg($r1.text),getReg($r2.text)); }
	|f3x r1=REGISTER ',' r2=REGISTER ',' r3=REGISTER { dcv.visitStmt3R(getOp($f3x.text),getReg($r1.text),getReg($r2.text),getReg($r3.text)); }
	|ft5c '{' {regs.clear();}  (r=REGISTER {regs.add($r.text);})? (',' r=REGISTER {regs.add($r.text);})*  '}' ',' obj=ARRAY_TYPE 
		{int regs1[]=new int[regs.size()]; for(int i=0;i<regs.size();i++){ regs1[i]=getReg(regs.get(i)); }
		 dcv.visitFilledNewArrayStmt(getOp($ft5c.text),regs1,$obj.text);
		}
	|fm5c '{' {regs.clear();} (r=REGISTER{regs.add($r.text);} (',' r=REGISTER{regs.add($r.text);})* )? '}' ',' m=sMethodF
		{int regs1[]=new int[regs.size()]; for(int i=0;i<regs.size();i++){ regs1[i]=getReg(regs.get(i)); }
		 dcv.visitMethodStmt(getOp($fm5c.text),regs1,$m.m);
		}
	|fmrc '{' r1=REGISTER '..' r2=REGISTER '}' ',' m=sMethodF 
		{ int iR1=getReg($r1.text); int iR2=getReg($r2.text);
		  int regs[];
		  if(iR1==iR2){
		     regs=new int[]{iR1};
		  }else{
		     regs=new int[iR2-iR1+1];
		     for(int i=0;i<regs.length;i++){ regs[i]=iR1+i; }
		  }
		  dcv.visitMethodStmt(getOp($fmrc.text),regs,$m.m);
		}
	|fmrc '{' '}' ',' m=sMethodF { dcv.visitMethodStmt(getOp($fmrc.text),new int[0],$m.m); }
	|ftrc '{' r1=REGISTER '..' r2=REGISTER '}' ',' obj=(OBJECT_TYPE|ARRAY_TYPE) { throw new RuntimeException(); }
	|sss=sLabel {dcv.visitLabel(getLabel($sss.text)); }
	|f2sb r1=REGISTER ',' r2=REGISTER ',' d2=INT {dcv.visitStmt2R1N(getOp($f2sb.text),getReg($r1.text),getReg($r2.text),parseInt($d2.text));}
	| f31t r1=REGISTER ',' sLabel { dcv.visitF31tStmt(getOp($f31t.text),getReg($r1.text),getLabel($sLabel.text)); }
	| '.packed-switch'  {tmpList.clear();}  d2=INT (sLabel {tmpList.add(getLabel($sLabel.text));})+ '.end packed-switch' { dcv.dPackedSwitch(parseInt($d2.text),(DexLabel[])tmpList.toArray(new DexLabel[0])); } {tmpList.clear();}
	| '.sparse-switch'  {regs.clear();tmpList.clear();}  (d2=INT {regs.add($d2.text);} '->' sLabel {tmpList.add(getLabel($sLabel.text));})* '.end sparse-switch' { dcv.dSparseSwitch(toIntArray(regs),(DexLabel[])tmpList.toArray(new DexLabel[0])); }{regs.clear();tmpList.clear();}
	| '.array-data' d2=INT {tmpList.clear();} (b=sBaseValue {tmpList.add($b.v);} )+ '.end array-data' { dcv.dArrayData(parseInt($d2.text),toByteArray(tmpList)); }
	;
f0x	:	NOP
	|	'return-void'
	;
f0t	:	GOTO|'goto/16'|'goto/32'
	;
f1x	:	'move-result'|'move-result-wide'|'move-result-object'
	|	'move-exception'
	|	RETURN|'return-wide'|'return-object'
	|	THROW
	|	'monitor-enter' | 'monitor-exit'
	;
fconst	:	'const/4'|'const/16'|CONST|'const/high16'
	|	'const-wide/16'|'const-wide/32'|'const-wide/high16' | 'const-wide'
	;
fconstString
	:	'const-string'|'const-string/jumbo';
ft1c
	:	'const-class'|'check-cast'|'new-instance';
ff1c	:	SGET
	|'sget-wide'
	|'sget-object'
	|'sget-boolean'
	|'sget-byte'
	|'sget-char'
	|'sget-short'
	|SPUT
	|'sput-wide'
	|'sput-object'
	|'sput-boolean'
	|'sput-byte'
	|'sput-char'
	|'sput-short'
	;
ft2c	:	'instance-of'|'new-array';
ff2c	:	IGET
	|'iget-wide'
	|'iget-object'
	|'iget-boolean'
	|'iget-byte'
	|'iget-char'
	|'iget-short'
	|	IPUT
	|'iput-wide'
	|'iput-object'
	|'iput-boolean'
	|'iput-byte'
	|'iput-char'
	|'iput-short'
	;
f2x	:	MOVE|'move/from16'|'move/16'
	|	'move-wide'|'move-wide/from16'|'move-wide/16'
	|	'move-object'|'move-object/from16'|'move-object/16'
	|	'array-length'
	|'neg-int'
|'not-int'
|'neg-long'
|'not-long'
|'neg-float'
|'neg-double'
|'int-to-long'
|'int-to-float'
|'int-to-double'
|'long-to-int'
|'long-to-float'
|'long-to-double'
|'float-to-int'
|'float-to-long'
|'float-to-double'
|'double-to-int'
|'double-to-long'
|'double-to-float'
|'int-to-byte'
|'int-to-char'
|'int-to-short'
|'add-int/2addr'
|'sub-int/2addr'
|'mul-int/2addr'
|'div-int/2addr'
|'rem-int/2addr'
|'and-int/2addr'
|'or-int/2addr'
|'xor-int/2addr'
|'shl-int/2addr'
|'shr-int/2addr'
|'ushr-int/2addr'
|'add-long/2addr'
|'sub-long/2addr'
|'mul-long/2addr'
|'div-long/2addr'
|'rem-long/2addr'
|'and-long/2addr'
|'or-long/2addr'
|'xor-long/2addr'
|'shl-long/2addr'
|'shr-long/2addr'
|'ushr-long/2addr'
|'add-float/2addr'
|'sub-float/2addr'
|'mul-float/2addr'
|'div-float/2addr'
|'rem-float/2addr'
|'add-double/2addr'
|'sub-double/2addr'
|'mul-double/2addr'
|'div-double/2addr'
|'rem-double/2addr'
	;
f3x	:	'cmpl-float'|'cmpg-float'|'cmpl-double'|'cmpg-double'|'cmp-long'
	|	AGET|'aget-wide'|'aget-object'|'aget-boolean'|'aget-byte'|'aget-char'|'aget-short'
	|	APUT|'aput-wide'|'aput-object'|'aput-boolean'|'aput-byte'|'aput-char'|'aput-short'
	|'add-int'
|'sub-int'
|'mul-int'
|'div-int'
|'rem-int'
|'and-int'
|'or-int'
|'xor-int'
|'shl-int'
|'shr-int'
|'ushr-int'
|'add-long'
|'sub-long'
|'mul-long'
|'div-long'
|'rem-long'
|'and-long'
|'or-long'
|'xor-long'
|'shl-long'
|'shr-long'
|'ushr-long'
|'add-float'
|'sub-float'
|'mul-float'
|'div-float'
|'rem-float'
|'add-double'
|'sub-double'
|'mul-double'
|'div-double'
|'rem-double'
	;
ft5c	:	'filled-new-array';
fm5c	:	'invoke-virtual'
|'invoke-super'
|'invoke-direct'
|'invoke-static'
|'invoke-interface'
	;
fmrc	:	'invoke-virtual/range'
|'invoke-super/range'
|'invoke-direct/range'
|'invoke-static/range'
|'invoke-interface/range'
	;
ftrc	:	'filled-new-array/range';
sLabel	:	':' (sBaseMemberName|ACC);
f31t: 'fill-array-data'|'packed-switch'|'sparse-switch';
f1t	:'if-eqz'
	|'if-nez'
	|'if-ltz'
	|'if-gez'
	|'if-gtz'
	|'if-lez'
	;
f2t	:	'if-eq'
|'if-ne'
|'if-lt'
|'if-ge'
|'if-gt'
|'if-le'
	;
f2sb	:	'add-int/lit16'
|'rsub-int'
|'mul-int/lit16'
|'div-int/lit16'
|'rem-int/lit16'
|'and-int/lit16'
|'or-int/lit16'
|'xor-int/lit16'
|'add-int/lit8'
|'rsub-int/lit8'
|'mul-int/lit8'
|'div-int/lit8'
|'rem-int/lit8'
|'and-int/lit8'
|'or-int/lit8'
|'xor-int/lit8'
|'shl-int/lit8'
|'shr-int/lit8'
|'ushr-int/lit8'
	;