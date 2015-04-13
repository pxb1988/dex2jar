.class LA;
.super Ljava/lang/Object;

.method static m()V
.registers 0
    invoke-static {}, LB;->b()V
    return-void
.end method

.method static m1()I
.registers 1
    invoke-static {}, LB;->b()V
    const v0, 0
    return v0
.end method

.method static m2()J
.registers 2
    invoke-static {}, LB;->b()V
    const-wide v0,0
    return-wide v0
.end method

.method static m3(J)V
.registers 2
    invoke-static {}, LB;->b()V
    return-void
.end method

.method m4()V
.registers 1
    invoke-static {}, LB;->b()V
    return-void
.end method

.method m5(J)V
.registers 3
    invoke-static {}, LB;->b()V
    return-void
.end method

.method m6(J)J
.registers 3
    invoke-static {}, LB;->b()V
    return-wide p1
.end method
