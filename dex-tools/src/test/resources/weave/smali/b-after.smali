.class LA;
.super Ljava/lang/Object;

.method static m()V
    .registers 2
    invoke-static { }, LA;->b_A001()V
    invoke-static { }, LA;->b_A001()V
    const v0, 1
    invoke-static { v0 }, LA;->c_A003(I)V
    const-wide v0, 0
    invoke-static { v0, v1 }, LA;->d_A005(J)V
    return-void
.end method

.method private static b_A001()V
    .registers 4
    const/4 v0, 0
    const/4 v1, 0
    const v2, 0
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LX;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static b_CB002([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2
    invoke-static { }, LB;->b()V
    const v0, 0
    return-object v0
.end method

.method private static c_A003(I)V
    .registers 5
    const/4 v0, 0
    const v1, 1
    new-array v1, v1, [Ljava/lang/Object;
    const v2, 0
    invoke-static/range { p0 .. p0 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v3
    aput-object v3, v1, v2
    const v2, 1
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LX;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static c_CB004([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3
    const v0, 0
    aget-object v1, p0, v0
    check-cast v1, Ljava/lang/Integer;
    invoke-virtual/range { v1 .. v1 }, Ljava/lang/Integer;->intValue()I
    move-result v1
    invoke-static { v1 }, LB;->c(I)V
    const v0, 0
    return-object v0
.end method

.method private static d_A005(J)V
    .registers 6
    const/4 v0, 0
    const v1, 1
    new-array v1, v1, [Ljava/lang/Object;
    const v2, 0
    invoke-static/range { p0 .. p1 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v3
    aput-object v3, v1, v2
    const v2, 2
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LX;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static d_CB006([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4
    const v0, 0
    aget-object v1, p0, v0
    check-cast v1, Ljava/lang/Long;
    invoke-virtual/range { v1 .. v1 }, Ljava/lang/Long;->longValue()J
    move-result-wide v1
    invoke-static { v1, v2 }, LB;->d(J)V
    const v0, 0
    return-object v0
.end method

.method m()V
    .registers 3
    const v0, 0
    invoke-static { p0 }, LA;->e_A007(LB;)V
    invoke-static { p0 }, LA;->f_A009(LB;)V
    invoke-static { p0, v0 }, LA;->e_A011(LB;I)V
    invoke-static { p0, v0 }, LA;->f_A013(LB;I)V
    return-void
.end method

.method private static e_A007(LB;)V
    .registers 5
    move-object v0, p0
    const/4 v1, 0
    const v2, 3
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LX;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static e_CB008(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3
    check-cast p0, LB;
    invoke-virtual { p0 }, LB;->e()V
    const v0, 0
    return-object v0
.end method

.method private static f_A009(LB;)V
    .registers 5
    move-object v0, p0
    const/4 v1, 0
    const v2, 4
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LX;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public f_CB010([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3
    invoke-super { p0 }, LB;->f()V
    const v0, 0
    return-object v0
.end method

.method private static e_A011(LB;I)V
    .registers 6
    move-object v0, p0
    const v1, 1
    new-array v1, v1, [Ljava/lang/Object;
    const v2, 0
    invoke-static/range { p1 .. p1 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v3
    aput-object v3, v1, v2
    const v2, 5
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LX;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static e_CB012(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4
    move-object v1, p0
    check-cast v1, LB;
    const v0, 0
    aget-object p0, p1, v0
    check-cast p0, Ljava/lang/Integer;
    invoke-virtual/range { p0 .. p0 }, Ljava/lang/Integer;->intValue()I
    move-result p0
    invoke-virtual { v1, p0 }, LB;->e(I)V
    const v0, 0
    return-object v0
.end method

.method private static f_A013(LB;I)V
    .registers 6
    move-object v0, p0
    const v1, 1
    new-array v1, v1, [Ljava/lang/Object;
    const v2, 0
    invoke-static/range { p1 .. p1 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v3
    aput-object v3, v1, v2
    const v2, 6
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LX;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public f_CB014([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4
    move-object v1, p0
    const v0, 0
    aget-object p0, p1, v0
    check-cast p0, Ljava/lang/Integer;
    invoke-virtual/range { p0 .. p0 }, Ljava/lang/Integer;->intValue()I
    move-result p0
    invoke-super { v1, p0 }, LB;->f(I)V
    const v0, 0
    return-object v0
.end method
