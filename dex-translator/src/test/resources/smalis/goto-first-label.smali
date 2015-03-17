.class Lxgoto/first/label;
.super Ljava/lang/Object;

.method public static assertSlept()V
    .registers 3
    :L0
    sget-object v1, LA;->sleepSemaphore:Ljava/util/concurrent/Semaphore;
    invoke-virtual { v1 }, Ljava/util/concurrent/Semaphore;->availablePermits()I
    move-result v1
    if-nez v1, :L1
    return-void
    :L1
    const-wide/16 v1, 50
    invoke-static { v1, v2 }, Ljava/lang/Thread;->sleep(J)V
    goto :L0
.end method

.method public static g2(LObj;)V
    .registers 4
    :L0
    invoke-virtual { p0 }, LObj;->next()LObj;
    move-result-object v3
    if-nez v3, :L1
    return-void
    :L1
    invoke-virtual { p0 }, LObj;->next()LObj;
    move-result-object p0
    goto :L0
.end method
