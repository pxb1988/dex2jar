.class Li230;
.super Li230;

.method public i230(II)F
    .catchall { :L5 .. :L10 } :L9
    .registers 10
    move-object v0, p0
    monitor-enter v0
    :L5
    int-to-float v0, p1
    int-to-float v1, p2
    div-float/2addr v1, v0
    move-object v0, p0
    monitor-exit v0
    return v1

    :L9
    move-exception v1
    monitor-exit v0
    :L10
    const v1, 0
    return v1
.end method
