.class Lgh/i4;
.super Ljava/lang/Object;

.method static public a(LIndenter;)Ljava/lang/Object;
  .registers 2
  if-nez v1, :L0
    new-instance v0, LNopIndenter;
    move-object v1, v0  #### use before init
    invoke-direct { v0 }, LNopIndenter;-><init>()V
  :L0
    return-object v1
.end method
