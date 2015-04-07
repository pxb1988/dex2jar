.bytecode 50.0
.class A
.super java/lang/Object

.method m()V
    new d2j/gen/MI__000
    dup
    aload 0
    aconst_null
    iconst_0
    invokespecial d2j/gen/MI__000/<init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invokestatic B/t(Lp;)Ljava/lang/Object;
    pop
    return
  .limit locals 1
  .limit stack -1
.end method

.method public static m_$$A_$$$$_callback(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    aload 0
    checkcast A
    invokevirtual A/m_$$A_()V
    aconst_null
    areturn
  .limit locals -1
  .limit stack -1
.end method

.method m_$$A_()V
    return
  .limit locals 1
  .limit stack -1
.end method
