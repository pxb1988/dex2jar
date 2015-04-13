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
  .limit stack 5
.end method

.method public static m_A001_CB002(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    aload 0
    checkcast A
    invokevirtual A/m_A001()V
    aconst_null
    areturn
  .limit locals 2
  .limit stack 1
.end method

.method public m_A001()V
    return
  .limit locals 1
  .limit stack 0
.end method
