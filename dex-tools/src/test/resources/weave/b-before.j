.bytecode 50.0
.class public super com/googlecode/d2j/tools/jar/test/res/Res
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
    invokevirtual java/io/PrintStream/append(Ljava/lang/CharSequence;)Ljava/io/PrintStream;
    pop
    getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc "test"
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
    return
  .limit locals 1
  .limit stack 2
.end method

.method public size()I
    aload 0
    invokespecial java/util/ArrayList/size()I
    ireturn
  .limit locals 1
  .limit stack 1
.end method

.method public add(Ljava/lang/Object;)Z
    aload 0
    aload 1
    invokespecial java/util/ArrayList/add(Ljava/lang/Object;)Z
    ireturn
  .limit locals 2
  .limit stack 2
.end method
