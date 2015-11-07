.class LML;
.super Ljava/lang/Object;
.source "ML.java"


# direct methods
.method constructor <init>()V
    .registers 1

    .prologue
    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method a()Ljava/lang/Object;
    .registers 4

    .prologue
    .line 3
    const/4 v0, 0x4

    const/4 v1, 0x5

    const/4 v2, 0x6

    filled-new-array {v0, v1, v2}, [I

    move-result-object v0

    const-class v1, Ljava/lang/String;

    invoke-static {v1, v0}, Ljava/lang/reflect/Array;->newInstance(Ljava/lang/Class;[I)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, [[[Ljava/lang/String;

    return-object v0
.end method
