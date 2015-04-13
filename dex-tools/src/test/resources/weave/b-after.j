.bytecode 50.0
.class public com/googlecode/d2j/tools/jar/test/res/Res
.super java/util/ArrayList

.method public <init>()V
    aload 0
    invokespecial java/util/ArrayList/<init>()V
    return
  .limit locals 1
  .limit stack 1
.end method

.method public static varargs main([Ljava/lang/String;)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc ""
    invokestatic com/googlecode/d2j/tools/jar/test/res/Res/append_A001(Ljava/io/PrintStream;Ljava/lang/CharSequence;)Ljava/io/PrintStream;
    pop
    getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc "test"
    invokestatic com/googlecode/d2j/tools/jar/test/WaveTest/println(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    pop
    return
  .limit locals 1
  .limit stack 2
.end method

.method private static synthetic append_A001(Ljava/io/PrintStream;Ljava/lang/CharSequence;)Ljava/io/PrintStream;
    new d2j/gen/MI__000
    dup
    aload 0
    iconst_1
    anewarray java/lang/Object
    dup
    iconst_0
    aload 1
    aastore
    iconst_0
    invokespecial d2j/gen/MI__000/<init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invokestatic com/googlecode/d2j/tools/jar/test/WaveTest/append(Lp;)Ljava/lang/Object;
    checkcast java/io/PrintStream
    areturn
  .limit locals 2
  .limit stack 7
.end method

.method public static append_CB002(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    aload 0
    checkcast java/io/PrintStream
    aload 1
    iconst_0
    aaload
    checkcast java/lang/CharSequence
    invokevirtual java/io/PrintStream/append(Ljava/lang/CharSequence;)Ljava/io/PrintStream;
    areturn
  .limit locals 2
  .limit stack 3
.end method

.method public size()I
    aload 0
    invokestatic com/googlecode/d2j/tools/jar/test/res/Res/size_A003(Ljava/util/ArrayList;)I
    ireturn
  .limit locals 1
  .limit stack 1
.end method

.method private static synthetic size_A003(Ljava/util/ArrayList;)I
    new d2j/gen/MI__000
    dup
    aload 0
    aconst_null
    iconst_1
    invokespecial d2j/gen/MI__000/<init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invokestatic com/googlecode/d2j/tools/jar/test/WaveTest/size(Lp;)Ljava/lang/Object;
    checkcast java/lang/Number
    invokevirtual java/lang/Number/intValue()I
    ireturn
  .limit locals 1
  .limit stack 5
.end method

.method public size_CB004([Ljava/lang/Object;)Ljava/lang/Object;
    aload 0
    invokespecial java/util/ArrayList/size()I
    invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
    areturn
  .limit locals 2
  .limit stack 1
.end method

.method public add(Ljava/lang/Object;)Z
    aload 0
    aload 1
    invokestatic com/googlecode/d2j/tools/jar/test/WaveTest/add(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    checkcast java/lang/Boolean
    invokevirtual java/lang/Boolean/booleanValue()Z
    ireturn
  .limit locals 2
  .limit stack 2
.end method
