.class Li;
.super Ljava/lang/Object;


.method public final a(Ljava/lang/Object;IZ)Ljava/lang/Object;
  .registers 8
    const/4 v0, 0
    instance-of v1, p1, Ljava/lang/Byte;
    if-eqz v1, :L1
    invoke-virtual { p0, v0, p2, p3 }, Lct/be;->a(BIZ)B
    move-result v0
    invoke-static { v0 }, Ljava/lang/Byte;->valueOf(B)Ljava/lang/Byte;
    move-result-object v0
  :L0
    return-object v0
  :L1
    instance-of v1, p1, Ljava/lang/Boolean;
    if-eqz v1, :L3
    invoke-virtual { p0, v0, p2, p3 }, Lct/be;->a(BIZ)B
    move-result v1
    if-eqz v1, :L2
    const/4 v0, 1
  :L2
    invoke-static { v0 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
    move-result-object v0
    goto :L0
  :L3
    instance-of v1, p1, Ljava/lang/Short;
    if-eqz v1, :L4
    invoke-virtual { p0, v0, p2, p3 }, Lct/be;->a(SIZ)S
    move-result v0
    invoke-static { v0 }, Ljava/lang/Short;->valueOf(S)Ljava/lang/Short;
    move-result-object v0
    goto :L0
  :L4
    instance-of v1, p1, Ljava/lang/Integer;
    if-eqz v1, :L5
    invoke-virtual { p0, v0, p2, p3 }, Lct/be;->a(IIZ)I
    move-result v0
    invoke-static { v0 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    goto :L0
  :L5
    instance-of v1, p1, Ljava/lang/Long;
    if-eqz v1, :L6
    const-wide/16 v0, 0
    invoke-virtual { p0, v0, v1, p2, p3 }, Lct/be;->a(JIZ)J
    move-result-wide v0
    invoke-static { v0, v1 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v0
    goto :L0
  :L6
    instance-of v1, p1, Ljava/lang/Float;
    if-eqz v1, :L7
    const/4 v0, 0
    invoke-direct { p0, v0, p2, p3 }, Lct/be;->a(FIZ)F
    move-result v0
    invoke-static { v0 }, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;
    move-result-object v0
    goto :L0
  :L7
    instance-of v1, p1, Ljava/lang/Double;
    if-eqz v1, :L8
    const-wide/16 v0, 0
    invoke-direct { p0, v0, v1, p2, p3 }, Lct/be;->a(DIZ)D
    move-result-wide v0
    invoke-static { v0, v1 }, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;
    move-result-object v0
    goto :L0
  :L8
    instance-of v1, p1, Ljava/lang/String;
    if-eqz v1, :L9
    invoke-virtual { p0, p2, p3 }, Lct/be;->a(IZ)Ljava/lang/String;
    move-result-object v0
    goto :L0
  :L9
    instance-of v1, p1, Ljava/util/Map;
    if-eqz v1, :L10
    check-cast p1, Ljava/util/Map;
    new-instance v0, Ljava/util/HashMap;
    invoke-direct { v0 }, Ljava/util/HashMap;-><init>()V
    invoke-virtual { p0, v0, p1, p2, p3 }, Lct/be;->a(Ljava/util/Map;Ljava/util/Map;IZ)Ljava/util/Map;
    move-result-object v0
    check-cast v0, Ljava/util/HashMap;
    goto :L0
  :L10
    instance-of v1, p1, Ljava/util/List;
    if-eqz v1, :L16
    check-cast p1, Ljava/util/List;
    if-eqz p1, :L11
    invoke-interface { p1 }, Ljava/util/List;->isEmpty()Z
    move-result v1
    if-eqz v1, :L12
  :L11
    new-instance v0, Ljava/util/ArrayList;
    invoke-direct { v0 }, Ljava/util/ArrayList;-><init>()V
    goto/16 :L0
  :L12
    invoke-interface { p1, v0 }, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v1
    invoke-direct { p0, v1, p2, p3 }, Lct/be;->b(Ljava/lang/Object;IZ)[Ljava/lang/Object;
    move-result-object v2
    if-nez v2, :L13
    const/4 v0, 0
    goto/16 :L0
  :L13
    new-instance v1, Ljava/util/ArrayList;
    invoke-direct { v1 }, Ljava/util/ArrayList;-><init>()V
  :L14
    array-length v3, v2
    if-ge v0, v3, :L15
    aget-object v3, v2, v0
    invoke-virtual { v1, v3 }, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    add-int/lit8 v0, v0, 1
    goto :L14
  :L15
    move-object v0, v1
    goto/16 :L0
  :L16
    instance-of v1, p1, Lct/bg;
    if-eqz v1, :L17
    check-cast p1, Lct/bg;
    invoke-virtual { p0, p1, p2, p3 }, Lct/be;->a(Lct/bg;IZ)Lct/bg;
    move-result-object v0
    goto/16 :L0
  :L17
    invoke-virtual { p1 }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    move-result-object v1
    invoke-virtual { v1 }, Ljava/lang/Class;->isArray()Z
    move-result v1
    if-eqz v1, :L28
    instance-of v1, p1, [B
    if-nez v1, :L18
    instance-of v1, p1, [Ljava/lang/Byte;
    if-eqz v1, :L19
  :L18
    invoke-virtual { p0, p2, p3 }, Lct/be;->b(IZ)[B
    move-result-object v0
    goto/16 :L0
  :L19
    instance-of v1, p1, [Z
    if-eqz v1, :L20
    invoke-direct { p0, p2, p3 }, Lct/be;->c(IZ)[Z
    move-result-object v0
    goto/16 :L0
  :L20
    instance-of v1, p1, [S
    if-eqz v1, :L21
    invoke-direct { p0, p2, p3 }, Lct/be;->d(IZ)[S
    move-result-object v0
    goto/16 :L0
  :L21
    instance-of v1, p1, [I
    if-eqz v1, :L22
    invoke-direct { p0, p2, p3 }, Lct/be;->e(IZ)[I
    move-result-object v0
    goto/16 :L0
  :L22
    instance-of v1, p1, [J
    if-eqz v1, :L23
    invoke-direct { p0, p2, p3 }, Lct/be;->f(IZ)[J
    move-result-object v0
    goto/16 :L0
  :L23
    instance-of v1, p1, [F
    if-eqz v1, :L24
    invoke-direct { p0, p2, p3 }, Lct/be;->g(IZ)[F
    move-result-object v0
    goto/16 :L0
  :L24
    instance-of v1, p1, [D
    if-eqz v1, :L25
    invoke-direct { p0, p2, p3 }, Lct/be;->h(IZ)[D
    move-result-object v0
    goto/16 :L0
  :L25
    check-cast p1, [Ljava/lang/Object;
    if-eqz p1, :L26
    array-length v1, p1
    if-nez v1, :L27
  :L26
    new-instance v0, Ljava/lang/RuntimeException;
    const-string/jumbo v1, "unable to get type of key and value."
    invoke-direct { v0, v1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
  :L27
    aget-object v0, p1, v0
    invoke-direct { p0, v0, p2, p3 }, Lct/be;->b(Ljava/lang/Object;IZ)[Ljava/lang/Object;
    move-result-object v0
    goto/16 :L0
  :L28
    new-instance v0, Ljava/lang/RuntimeException;
    const-string/jumbo v1, "read object error: unsupport type."
    invoke-direct { v0, v1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
.end method

