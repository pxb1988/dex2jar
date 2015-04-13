.class LA;
.super Ljava/lang/Object;

.method static m()V
    .registers 4
    const/4 v0, 0
    const/4 v1, 0
    const v2, 0
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LB;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static m_A001_CB002([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2
    invoke-static/range { }, LA;->m_A001()V
    const v0, 0
    return-object v0
.end method

.method public static m_A001()V
    .registers 0
    invoke-static { }, LB;->b()V
    return-void
.end method

.method static m1()I
    .registers 4
    const/4 v0, 0
    const/4 v1, 0
    const v2, 1
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LB;->t(Lp;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/Integer;
    invoke-virtual/range { v0 .. v0 }, Ljava/lang/Integer;->intValue()I
    move-result v0
    return v0
.end method

.method public static m1_A003_CB004([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2
    invoke-static/range { }, LA;->m1_A003()I
    move-result v0
    invoke-static/range { v0 .. v0 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    return-object v0
.end method

.method public static m1_A003()I
    .registers 1
    invoke-static { }, LB;->b()V
    const v0, 0
    return v0
.end method

.method static m2()J
    .registers 4
    const/4 v0, 0
    const/4 v1, 0
    const v2, 2
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LB;->t(Lp;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/Long;
    invoke-virtual/range { v0 .. v0 }, Ljava/lang/Long;->longValue()J
    move-result-wide v0
    return-wide v0
.end method

.method public static m2_A005_CB006([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2
    invoke-static/range { }, LA;->m2_A005()J
    move-result-wide v0
    invoke-static/range { v0 .. p0 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v0
    return-object v0
.end method

.method public static m2_A005()J
    .registers 2
    invoke-static { }, LB;->b()V
    const-wide v0, 0
    return-wide v0
.end method

.method static m3(J)V
    .registers 6
    const/4 v0, 0
    const v1, 1
    new-array v1, v1, [Ljava/lang/Object;
    const v2, 0
    invoke-static/range { p0 .. p1 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v3
    aput-object v3, v1, v2
    const v2, 3
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LB;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static m3_A007_CB008([Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4
    const v0, 0
    aget-object v1, p0, v0
    check-cast v1, Ljava/lang/Long;
    invoke-virtual/range { v1 .. v1 }, Ljava/lang/Long;->longValue()J
    move-result-wide v1
    invoke-static/range { v1 .. v2 }, LA;->m3_A007(J)V
    const v0, 0
    return-object v0
.end method

.method public static m3_A007(J)V
    .registers 2
    invoke-static { }, LB;->b()V
    return-void
.end method

.method m4()V
    .registers 5
    move-object v0, p0
    const/4 v1, 0
    const v2, 4
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LB;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static m4_A009_CB010(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3
    check-cast p0, LA;
    invoke-virtual/range { p0 .. p0 }, LA;->m4_A009()V
    const v0, 0
    return-object v0
.end method

.method public m4_A009()V
    .registers 1
    invoke-static { }, LB;->b()V
    return-void
.end method

.method m5(J)V
    .registers 7
    move-object v0, p0
    const v1, 1
    new-array v1, v1, [Ljava/lang/Object;
    const v2, 0
    invoke-static/range { p1 .. p2 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v3
    aput-object v3, v1, v2
    const v2, 5
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LB;->t(Lp;)Ljava/lang/Object;
    return-void
.end method

.method public static m5_A011_CB012(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 5
    move-object v1, p0
    check-cast v1, LA;
    const v0, 0
    aget-object v2, p1, v0
    check-cast v2, Ljava/lang/Long;
    invoke-virtual/range { v2 .. v2 }, Ljava/lang/Long;->longValue()J
    move-result-wide v2
    invoke-virtual/range { v1 .. p0 }, LA;->m5_A011(J)V
    const v0, 0
    return-object v0
.end method

.method public m5_A011(J)V
    .registers 3
    invoke-static { }, LB;->b()V
    return-void
.end method

.method m6(J)J
    .registers 7
    move-object v0, p0
    const v1, 1
    new-array v1, v1, [Ljava/lang/Object;
    const v2, 0
    invoke-static/range { p1 .. p2 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v3
    aput-object v3, v1, v2
    const v2, 6
    new-instance v3, Ld2j/gen/MI__000;
    invoke-direct { v3, v0, v1, v2 }, Ld2j/gen/MI__000;-><init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    invoke-static { v3 }, LB;->t(Lp;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/Long;
    invoke-virtual/range { v0 .. v0 }, Ljava/lang/Long;->longValue()J
    move-result-wide v0
    return-wide v0
.end method

.method public static m6_A013_CB014(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 5
    move-object v1, p0
    check-cast v1, LA;
    const v0, 0
    aget-object v2, p1, v0
    check-cast v2, Ljava/lang/Long;
    invoke-virtual/range { v2 .. v2 }, Ljava/lang/Long;->longValue()J
    move-result-wide v2
    invoke-virtual/range { v1 .. p0 }, LA;->m6_A013(J)J
    move-result-wide v0
    invoke-static/range { v0 .. v1 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v0
    return-object v0
.end method

.method public m6_A013(J)J
    .registers 3
    invoke-static { }, LB;->b()V
    return-wide p1
.end method
