.bytecode 50.0
.class public d2j/gen/MI__000
.super java/lang/Object
.implements p

.field private final 'thiz' Ljava/lang/Object;

.field private final 'args' [Ljava/lang/Object;

.field private final 'idx' I

.method public <init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    aload 0
    invokespecial java/lang/Object/<init>()V
    aload 0
    aload 1
    putfield d2j/gen/MI__000/thiz Ljava/lang/Object;
    aload 0
    aload 2
    putfield d2j/gen/MI__000/args [Ljava/lang/Object;
    aload 0
    iload 3
    putfield d2j/gen/MI__000/idx I
    return
  .limit locals 4
  .limit stack 2
.end method

.method public getMethodOwner()Ljava/lang/String;
    aload 0
    getfield d2j/gen/MI__000/idx I
    tableswitch 0
      L0
      default : L1
  L0:
    ldc "A"
    areturn
  L1:
    new java/lang/RuntimeException
    dup
    ldc "invalid idx"
    invokespecial java/lang/RuntimeException/<init>(Ljava/lang/String;)V
    athrow
  .limit locals 1
  .limit stack 3
.end method

.method public getMethodName()Ljava/lang/String;
    aload 0
    getfield d2j/gen/MI__000/idx I
    tableswitch 0
      L0
      default : L1
  L0:
    ldc "m"
    areturn
  L1:
    new java/lang/RuntimeException
    dup
    ldc "invalid idx"
    invokespecial java/lang/RuntimeException/<init>(Ljava/lang/String;)V
    athrow
  .limit locals 1
  .limit stack 3
.end method

.method public getMethodDesc()Ljava/lang/String;
    aload 0
    getfield d2j/gen/MI__000/idx I
    tableswitch 0
      L0
      default : L1
  L0:
    ldc "()V"
    areturn
  L1:
    new java/lang/RuntimeException
    dup
    ldc "invalid idx"
    invokespecial java/lang/RuntimeException/<init>(Ljava/lang/String;)V
    athrow
  .limit locals 1
  .limit stack 3
.end method

.method public getArguments()[Ljava/lang/Object;
    aload 0
    getfield d2j/gen/MI__000/args [Ljava/lang/Object;
    areturn
  .limit locals 1
  .limit stack 1
.end method

.method public getThis()Ljava/lang/Object;
    aload 0
    getfield d2j/gen/MI__000/thiz Ljava/lang/Object;
    areturn
  .limit locals 1
  .limit stack 1
.end method

.method public proceed()Ljava/lang/Object;
.throws java/lang/Throwable
    aload 0
    getfield d2j/gen/MI__000/idx I
    tableswitch 0
      L0
      default : L1
  L0:
    aload 0
    getfield d2j/gen/MI__000/thiz Ljava/lang/Object;
    aload 0
    getfield d2j/gen/MI__000/args [Ljava/lang/Object;
    invokestatic A/m_A001_CB002(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    areturn
  L1:
    new java/lang/RuntimeException
    dup
    ldc "invalid idx"
    invokespecial java/lang/RuntimeException/<init>(Ljava/lang/String;)V
    athrow
  .limit locals 1
  .limit stack 3
.end method
