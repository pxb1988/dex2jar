.class LA;
.super Ljava/lang/Object;

.method static m()V
.registers 2
    invoke-static {}, LB;->b()V
    invoke-static {}, LB;->b()V
    const v0, 1
    invoke-static {v0}, LB;->c(I)V
    const-wide v0, 0
    invoke-static {v0,v1}, LB;->d(J)V
    return-void
.end method

.method m()V
.registers 3
    const v0, 0
    invoke-virtual {v2}, LB;->e()V
    invoke-super {v2}, LB;->f()V
    invoke-virtual {v2,v0}, LB;->e(I)V
    invoke-super {v2,v0}, LB;->f(I)V
    return-void
.end method
