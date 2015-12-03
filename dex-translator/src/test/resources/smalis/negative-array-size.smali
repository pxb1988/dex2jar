.class Li;
.super Ljava/lang/Object;

.method public getFileLength()I
  .catch Ljava/lang/Exception; { :L0 .. :L1 } :L2
  .catch Ljava/lang/Exception; { :L3 .. :L4 } :L5
  .registers 3
    const/4 v0, -1
  :L0
    new-array v1, v0, [I
  :L1
    goto :L0
  :L2
    move-exception v0
    const/4 v0, 0
    sput v0, Lz;->b:I
  :L3
    iget v0, p0, Lz;->b:I
  :L4
    return v0
  :L5
    move-exception v0
    throw v0
.end method
