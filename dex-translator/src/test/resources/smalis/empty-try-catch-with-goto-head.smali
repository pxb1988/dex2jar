.class Letcwgh;
.super Ljava/lang/Object;

.method private aaa(F)V
  .catch Ljava/lang/Exception; { :L1 .. :L2 } :L3
  .registers 8
  :L0
    iget-object v0, p0, Lz;->z:Lz;
    if-eqz v0, :L1
    iget-object v0, p0, Lz;->z:Lz;
    invoke-static { }, Ljava/util/Locale;->getDefault()Ljava/util/Locale;
    move-result-object v1
    const-string/jumbo v2, "%.1f"
    const/4 v3, 1
    new-array v3, v3, [Ljava/lang/Object;
    const/4 v4, 0
    invoke-static { p1 }, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;
    move-result-object v5
    aput-object v5, v3, v4
    invoke-static { v1, v2, v3 }, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v1
    invoke-virtual { v0, v1 }, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
  :L1
    return-void
  :L2
  :L3
    goto :L0
.end method