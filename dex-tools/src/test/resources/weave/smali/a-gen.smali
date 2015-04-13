.class public Ld2j/gen/MI__000;
.super Ljava/lang/Object;
.implements Lp;

.field private final thiz:Ljava/lang/Object;

.field private final args:[Ljava/lang/Object;

.field private final idx:I

.method public constructor <init>(Ljava/lang/Object;[Ljava/lang/Object;I)V
    .registers 4
    iput-object p1, p0, Ld2j/gen/MI__000;->thiz:Ljava/lang/Object;
    iput-object p2, p0, Ld2j/gen/MI__000;->args:[Ljava/lang/Object;
    iput p3, p0, Ld2j/gen/MI__000;->idx:I
    return-void
.end method

.method public getMethodOwner()Ljava/lang/String;
    .registers 3
    iget v0, p0, Ld2j/gen/MI__000;->idx:I
    packed-switch v0, :L1
    new-instance v0, Ljava/lang/RuntimeException;
    const-string v1, "invalid idx"
    invoke-direct { v0, v1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
    :L0
    const-string v0, "A"
    return-object v0
    :L1
    .packed-switch 0
        :L0
        :L0
        :L0
        :L0
        :L0
        :L0
        :L0
    .end packed-switch
.end method

.method public getMethodName()Ljava/lang/String;
    .registers 3
    iget v0, p0, Ld2j/gen/MI__000;->idx:I
    packed-switch v0, :L7
    new-instance v0, Ljava/lang/RuntimeException;
    const-string v1, "invalid idx"
    invoke-direct { v0, v1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
    :L0
    const-string v0, "m"
    return-object v0
    :L1
    const-string v0, "m1"
    return-object v0
    :L2
    const-string v0, "m2"
    return-object v0
    :L3
    const-string v0, "m3"
    return-object v0
    :L4
    const-string v0, "m4"
    return-object v0
    :L5
    const-string v0, "m5"
    return-object v0
    :L6
    const-string v0, "m6"
    return-object v0
    :L7
    .packed-switch 0
        :L0
        :L1
        :L2
        :L3
        :L4
        :L5
        :L6
    .end packed-switch
.end method

.method public getMethodDesc()Ljava/lang/String;
    .registers 3
    iget v0, p0, Ld2j/gen/MI__000;->idx:I
    packed-switch v0, :L5
    new-instance v0, Ljava/lang/RuntimeException;
    const-string v1, "invalid idx"
    invoke-direct { v0, v1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
    :L0
    const-string v0, "()I"
    return-object v0
    :L1
    const-string v0, "()J"
    return-object v0
    :L2
    const-string v0, "()V"
    return-object v0
    :L3
    const-string v0, "(J)J"
    return-object v0
    :L4
    const-string v0, "(J)V"
    return-object v0
    :L5
    .packed-switch 0
        :L2
        :L0
        :L1
        :L4
        :L2
        :L4
        :L3
    .end packed-switch
.end method

.method public getArguments()[Ljava/lang/Object;
    .registers 2
    iget v0, p0, Ld2j/gen/MI__000;->args:[Ljava/lang/Object;
    return-object v0
.end method

.method public getThis()Ljava/lang/Object;
    .registers 2
    iget v0, p0, Ld2j/gen/MI__000;->thiz:Ljava/lang/Object;
    return-object v0
.end method

.method public proceed()Ljava/lang/Object;
    .registers 4
    iget v0, p0, Ld2j/gen/MI__000;->thiz:Ljava/lang/Object;
    iget v1, p0, Ld2j/gen/MI__000;->args:[Ljava/lang/Object;
    iget v2, p0, Ld2j/gen/MI__000;->idx:I
    packed-switch v2, :L7
    new-instance v0, Ljava/lang/RuntimeException;
    const-string v1, "invalid idx"
    invoke-direct { v0, v1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
    :L0
    invoke-static { v1 }, LA;->m_A001_CB002([Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
    :L1
    invoke-static { v1 }, LA;->m1_A003_CB004([Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
    :L2
    invoke-static { v1 }, LA;->m2_A005_CB006([Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
    :L3
    invoke-static { v1 }, LA;->m3_A007_CB008([Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
    :L4
    invoke-static { v0, v1 }, LA;->m4_A009_CB010(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
    :L5
    invoke-static { v0, v1 }, LA;->m5_A011_CB012(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
    :L6
    invoke-static { v0, v1 }, LA;->m6_A013_CB014(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
    :L7
    .packed-switch 0
        :L0
        :L1
        :L2
        :L3
        :L4
        :L5
        :L6
    .end packed-switch
.end method
