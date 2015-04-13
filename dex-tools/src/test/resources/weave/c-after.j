.bytecode 50.0
.class A
.super java/lang/Object

.method m()V
    invokestatic A/a_A001()V
    invokestatic A/b_A003()V
    invokestatic A/c_A005()I
    pop
    invokestatic T/d()I
    pop
    return
  .limit locals 1
  .limit stack 1
.end method

.method private static synthetic a_A001()V
    new d2j/gen/MI__000
    dup
    aconst_null
    aconst_null
    iconst_0
    invokespecial d2j/gen/MI__000/<init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invokestatic B/a(Lp;)Ljava/lang/Object;
    pop
    return
  .limit locals 0
  .limit stack 5
.end method

.method public static a_CB002([Ljava/lang/Object;)Ljava/lang/Object;
    invokestatic T/a()V
    aconst_null
    areturn
  .limit locals 1
  .limit stack 1
.end method

.method private static synthetic b_A003()V
    new d2j/gen/MI__000
    dup
    aconst_null
    aconst_null
    iconst_1
    invokespecial d2j/gen/MI__000/<init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invokestatic B/b(Lp;)V
    return
  .limit locals 0
  .limit stack 5
.end method

.method public static b_CB004([Ljava/lang/Object;)Ljava/lang/Object;
    invokestatic T/b()V
    aconst_null
    areturn
  .limit locals 1
  .limit stack 1
.end method

.method private static synthetic c_A005()I
    new d2j/gen/MI__000
    dup
    aconst_null
    aconst_null
    iconst_2
    invokespecial d2j/gen/MI__000/<init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invokestatic B/c(Lp;)Ljava/lang/Object;
    checkcast java/lang/Number
    invokevirtual java/lang/Number/intValue()I
    ireturn
  .limit locals 0
  .limit stack 5
.end method

.method public static c_CB006([Ljava/lang/Object;)Ljava/lang/Object;
    invokestatic T/c()I
    invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
    areturn
  .limit locals 1
  .limit stack 1
.end method
