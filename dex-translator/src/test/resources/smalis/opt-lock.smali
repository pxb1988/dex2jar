.class Lopt/lock;
.super Ljava/lang/Object;
.method public static a()V
    .catchall { :L0 .. :L1 } :L2
    .registers 2
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    :L0
    monitor-enter v0
    const-string v1, "haha"
    invoke-virtual { v0, v1 }, Ljava/io/PrintString;->println(Ljava/lang/String;)V
    :L1
    monitor-exit v0
    return-void
    :L2
    move-exception v1
    monitor-exit v0
    throw v1
.end method
