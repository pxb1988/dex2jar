grammar Smali;

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

COMMENT
    :   ('//' ~('\n'|'\r')* '\r'? '\n'
    |   '#' ~('\n'|'\r')* '\r'? '\n'
    |   '/*' .*? '*/' ) -> skip
    ;

WS  :   [ \t\r\n]+ -> skip
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
FRAGMENT_OBJECT_TYPE: 'L' (ESC_SEQ |~(';'|':'|'\\'|' '|'\n'|'\t'|'\r'|'('|')'|'-'))+ ';' ;
fragment
FRAGMENT_ARRAY_TYPE: ('[')+ (FRAGMENT_PRIMITIVE_TYPE|FRAGMENT_OBJECT_TYPE);

fragment
FRAGMENT_ID: (ESC_SEQ| ~('\\'|'\r'|'\n'|'\t'|' '|':'|'-'|'='|','|'{'|'}'|'('|')'|'+'|'\"'|'\''|'#'|'/'|'.'|';'))+;
fragment
FRAGMENT_METHOD_PART: FRAGMENT_ID '(' (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE)* ')' ('V' | FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE)
;
fragment
FRAGMENT_FIELD_PART:
 FRAGMENT_ID
 ':' (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE)
;


METHOD_FULL: (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE) '->' FRAGMENT_METHOD_PART;
METHOD_PART: FRAGMENT_METHOD_PART;

FIELD_FULL: (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE) '->' FRAGMENT_FIELD_PART;
FIELD_PART: FRAGMENT_FIELD_PART;
LABEL: ':' FRAGMENT_ID;

SMALI_V2_LOCAL_NAME_TYPE : '"' ( ESC_SEQ | ~('\\'|'"') )* '"' ':' (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE)
                         ;

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


STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

OBJECT_TYPE: FRAGMENT_OBJECT_TYPE;
ARRAY_TYPE: FRAGMENT_ARRAY_TYPE;
PRIMITIVE_TYPE: FRAGMENT_PRIMITIVE_TYPE;

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
ID  :	FRAGMENT_ID
    ;
DPARAMETER:'.parameter';
DENUM:'.enum';
DPARAM: '.param';
DLINENUMBER: '.line';
DLOCAL:'.local';
DENDLOCAL:'.end local';
DRESTARTLOCAL:'.restart local';
DPROLOGUE:'.prologue';
DEPIOGUE:'.epiogue';

sFile	:	'.class' sAccList className=OBJECT_TYPE
    ( sSuper | sInterface|sSource|sMethod|sField|sAnnotation)*
             '.end class'?
    	;
sSource :	'.source' src=STRING;
sSuper	:	'.super' name=OBJECT_TYPE;
sInterface:	'.implements' name=OBJECT_TYPE;
sMethod
	:	'.method' sAccList methodObj=(METHOD_FULL|METHOD_PART)
		(  sAnnotation
		  | sParameter
		  | sInstruction
		 )*
		'.end method';
sField	: '.field' sAccList fieldObj=(FIELD_FULL|FIELD_PART) ('=' sBaseValue)?
		(sAnnotation*
		'.end field')?
	;
sAccList: ACC*;
sAnnotation
	: 	'.annotation' visibility=ANN_VISIBLE type=OBJECT_TYPE
		(sAnnotationKeyName '=' sAnnotationValue)*
		 '.end annotation'
	;
sSubannotation
	:	'.subannotation' type=OBJECT_TYPE (sAnnotationKeyName '=' sAnnotationValue )* '.end subannotation'
	;
sParameter
	:	parameter=DPARAMETER (name=STRING)?  ( (sAnnotation)* '.end parameter')?
		| param=DPARAM r=REGISTER (',' name=STRING )? (sAnnotation* '.end param')?
	;
sAnnotationKeyName
	:	PRIMITIVE_TYPE |VOID_TYPE
        	|ANN_VISIBLE|REGISTER|BOOLEAN|ID | NULL
        	|FLOAT_INFINITY|DOUBLE_INFINITY|FLOAT_NAN|DOUBLE_NAN
        	|NOP|MOVE|RETURN|CONST|THROW|GOTO|AGET|APUT|IGET|IPUT|SGET|SPUT|ACC;
sAnnotationValue
	:sSubannotation
	|sBaseValue
	|sArrayValue
	;// field,method,array,subannotation
sBaseValue
	:STRING
	|BOOLEAN|BYTE|SHORT|CHAR|INT|LONG
	|BASE_FLOAT| FLOAT_INFINITY | FLOAT_NAN
	|BASE_DOUBLE|DOUBLE_INFINITY|DOUBLE_NAN
	|METHOD_FULL
	|OBJECT_TYPE
	|NULL
	|DENUM FIELD_FULL
	;
sArrayValue: '{' sAnnotationValue? (',' sAnnotationValue)* '}';


sInstruction
    :fline
    |flocal
    |fend
    |frestart
    |fprologue
    |fepiogue
    |fregisters
	|flocals
	|fcache
	|fcacheall
	|f0x
	|f0t
	|f1t
	|f2t
	|f1x
	|fconst
	|ft2c
	|ff1c
	|ff2c
	|f2x
	|f3x
	|ft5c
	|fm5c
	|fmrc
	|ftrc
	|sLabel
	|f2sb
	|f31t
	|fpackageswitch
	|fspareswitch
	|farraydata
	;
fline:'.line' line=INT;
flocal:'.local' r=REGISTER ','
            (
                    (name1=sAnnotationKeyName |  name2=STRING) ':' type=(OBJECT_TYPE | PRIMITIVE_TYPE | ARRAY_TYPE) // normal case
                |   v1=FIELD_PART // smali 1.x
                |   v2=SMALI_V2_LOCAL_NAME_TYPE // smali 2.x
            )
         (',' sig=STRING)?
        ;
fend:'.end local' r=REGISTER;
frestart:'.restart local'  r=REGISTER;
fprologue:'.prologue';
fepiogue:'.epiogue';
fregisters:'.registers' xregisters=INT;
flocals: '.locals' xlocals=INT;
fcache:'.catch' type=OBJECT_TYPE '{' start=LABEL '..' end=LABEL  '}' handle=LABEL;
fcacheall:'.catchall' '{' start=LABEL '..' end=LABEL  '}' handle=LABEL;
sLabel: label=LABEL;
fpackageswitch:'.packed-switch' start=INT LABEL+ '.end packed-switch';
fspareswitch:'.sparse-switch' (INT '->' LABEL)* '.end sparse-switch';
farraydata:'.array-data' size=INT (sBaseValue)+ '.end array-data';
f0x	:	op=(NOP
	|	'return-void')
	;
f0t	:	op=(GOTO|'goto/16'|'goto/32') target=LABEL
	;
f1x	:	op=('move-result'|'move-result-wide'|'move-result-object'
	|	'move-exception'
	|	RETURN|'return-wide'|'return-object'
	|	THROW
	|	'monitor-enter' | 'monitor-exit' ) r1=REGISTER
	;
fconst
    : op=('const/4'|'const/16'|CONST|'const/high16'|'const-wide/16'|'const-wide/32'|'const-wide/high16'|'const-wide')
                                                      r1=REGISTER ',' cst=(INT|LONG)
	| op=('const-string'|'const-string/jumbo')        r1=REGISTER ','  cst=STRING
    | op=('const-class'|'check-cast'|'new-instance')  r1=REGISTER ','  cst=(OBJECT_TYPE|ARRAY_TYPE)
	;
ff1c	:	op=(SGET
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
	|'sput-short' ) r1=REGISTER ',' fld=FIELD_FULL
	;
ft2c	:	op=('instance-of'|'new-array') r1=REGISTER ',' r2=REGISTER ',' type=(OBJECT_TYPE|ARRAY_TYPE);
ff2c	:	op=(IGET
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
	|'iput-short' ) r1=REGISTER ',' r2=REGISTER ',' fld=FIELD_FULL
	;
f2x	:	op=(MOVE|'move/from16'|'move/16'
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
|'rem-double/2addr') r1=REGISTER ',' r2=REGISTER
	;
f3x	:	op=('cmpl-float'|'cmpg-float'|'cmpl-double'|'cmpg-double'|'cmp-long'
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
|'rem-double') r1=REGISTER ',' r2=REGISTER ',' r3=REGISTER
	;
ft5c	:	op='filled-new-array' '{' (REGISTER (',' REGISTER)* )? '}' ',' type=ARRAY_TYPE;
fm5c	:	op=('invoke-virtual'|'invoke-super'|'invoke-direct'
              |'invoke-static'|'invoke-interface'
            )  '{' (REGISTER (',' REGISTER)* )? '}' ',' method=METHOD_FULL
	;
fmrc	:	op=('invoke-virtual/range'|'invoke-super/range'
              |'invoke-direct/range'|'invoke-static/range'
              |'invoke-interface/range'
            )  '{' (rstart=REGISTER '..' rend=REGISTER)? '}' ',' method=METHOD_FULL
	;
ftrc	:	op='filled-new-array/range' '{' (rstart=REGISTER '..' rend=REGISTER)? '}' ',' type=(OBJECT_TYPE|ARRAY_TYPE);
f31t: op=('fill-array-data'|'packed-switch'|'sparse-switch') r1=REGISTER ',' label=LABEL;
f1t	:op=('if-eqz'|'if-nez'|'if-ltz'|'if-gez'|'if-gtz'|'if-lez')  r1=REGISTER ',' label=LABEL
	;
f2t	:op=('if-eq'|'if-ne'|'if-lt'|'if-ge'|'if-gt'|'if-le') r1=REGISTER ',' r2=REGISTER ',' label=LABEL
	;
f2sb	:op=('add-int/lit16'|'rsub-int'|'mul-int/lit16'|'div-int/lit16'|'rem-int/lit16'|'and-int/lit16'
             |'or-int/lit16'|'xor-int/lit16'|'add-int/lit8'|'rsub-int/lit8'|'mul-int/lit8'
             |'div-int/lit8'|'rem-int/lit8'|'and-int/lit8'|'or-int/lit8'|'xor-int/lit8'|'shl-int/lit8'|'shr-int/lit8'
             |'ushr-int/lit8'
          ) r1=REGISTER ',' r2=REGISTER ',' lit=INT
	;