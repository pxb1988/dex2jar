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
