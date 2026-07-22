.class Lnpe/cause/trap/fail;
.super Lb;

.method public final run()V
    .catch Ljava/lang/Exception; { :L0 .. :L1 } :L8
    .catch Ljava/lang/Exception; { :L4 .. :L5 } :L8
    .catch Ljava/lang/Exception; { :L5 .. :L6 } :L6
    .catch Ljava/lang/Exception; { :L7 .. :L8 } :L8
    .catch Ljava/lang/Exception; { :L9 .. :L10 } :L6
    .catch Ljava/lang/Exception; { :L11 .. :L12 } :L22
    .catch Ljava/lang/Exception; { :L13 .. :L14 } :L6
    .catch Ljava/lang/Exception; { :L15 .. :L16 } :L22
    .catch Ljava/lang/Exception; { :L18 .. :L21 } :L6
    .registers 8
    const/4 v6, 0
    iput-object v7, v7, Lcom/jcraft/jsch/e;->a:Ljava/lang/Runnable;
    :L0
    iget-object v0, v7, Lcom/jcraft/jsch/e;->a:Ljava/lang/Runnable;
    :L1
    if-nez v0, :L3
    :L2
    iput-object v6, v7, Lcom/jcraft/jsch/e;->a:Ljava/lang/Runnable;
    return-void
    :L3
    const/4 v0, 0
    :L4
    invoke-virtual { v0 }, Ljava/net/ServerSocket;->accept()Ljava/net/Socket;
    move-result-object v0
    const/4 v1, 1
    invoke-virtual { v0, v1 }, Ljava/net/Socket;->setTcpNoDelay(Z)V
    invoke-virtual { v0 }, Ljava/net/Socket;->getInputStream()Ljava/io/InputStream;
    move-result-object v1
    invoke-virtual { v0 }, Ljava/net/Socket;->getOutputStream()Ljava/io/OutputStream;
    move-result-object v2
    new-instance v3, Lcom/jcraft/jsch/v;
    invoke-direct { v3 }, Lcom/jcraft/jsch/v;-><init>()V
    invoke-virtual { v3 }, Lcom/jcraft/jsch/v;->a()V
    iget-object v4, v3, Lcom/jcraft/jsch/v;->a:Lcom/jcraft/jsch/ad;
    iput-object v1, v4, Lcom/jcraft/jsch/ad;->a:Ljava/io/InputStream;
    iget-object v1, v3, Lcom/jcraft/jsch/v;->a:Lcom/jcraft/jsch/ad;
    iput-object v2, v1, Lcom/jcraft/jsch/ad;->a:Ljava/io/OutputStream;
    const/4 v1, 0
    invoke-virtual { v1, v3 }, Lcom/jcraft/jsch/f;->a(Lcom/jcraft/jsch/ab;)V
    const/4 v1, 0
    iput-object v1, v3, Lcom/jcraft/jsch/v;->a:Ljava/lang/String;
    const/4 v1, 0
    iput v1, v3, Lcom/jcraft/jsch/v;->a:I
    invoke-virtual { v0 }, Ljava/net/Socket;->getInetAddress()Ljava/net/InetAddress;
    move-result-object v1
    invoke-virtual { v1 }, Ljava/net/InetAddress;->getHostAddress()Ljava/lang/String;
    move-result-object v1
    iput-object v1, v3, Lcom/jcraft/jsch/v;->b:Ljava/lang/String;
    invoke-virtual { v0 }, Ljava/net/Socket;->getPort()I
    move-result v0
    iput v0, v3, Lcom/jcraft/jsch/v;->b:I
    :L5
    invoke-virtual { v3 }, Lcom/jcraft/jsch/v;->a()Lcom/jcraft/jsch/f;
    move-result-object v1
    invoke-virtual { v1 }, Lcom/jcraft/jsch/f;->a()Z
    move-result v0
    if-nez v0, :L9
    new-instance v0, Lcom/jcraft/jsch/JSchException;
    const-string v1, "session is down"
    invoke-direct { v0, v1 }, Lcom/jcraft/jsch/JSchException;-><init>(Ljava/lang/String;)V
    throw v0
    :L6
    move-exception v0
    :L7
    iget-object v1, v3, Lcom/jcraft/jsch/v;->a:Lcom/jcraft/jsch/ad;
    invoke-virtual { v1 }, Lcom/jcraft/jsch/ad;->b()V
    const/4 v1, 0
    iput-object v1, v3, Lcom/jcraft/jsch/v;->a:Lcom/jcraft/jsch/ad;
    invoke-static { v3 }, Lcom/jcraft/jsch/ab;->a(Lcom/jcraft/jsch/ab;)V
    instance-of v1, v0, Lcom/jcraft/jsch/JSchException;
    if-eqz v1, :L0
    check-cast v0, Lcom/jcraft/jsch/JSchException;
    throw v0
    :L8
    move-exception v0
    goto :L2
    :L9
    new-instance v0, Lcom/jcraft/jsch/k;
    const/16 v2, 150
    invoke-direct { v0, v2 }, Lcom/jcraft/jsch/k;-><init>(I)V
    new-instance v2, Lcom/jcraft/jsch/i;
    invoke-direct { v2, v0 }, Lcom/jcraft/jsch/i;-><init>(Lcom/jcraft/jsch/k;)V
    invoke-virtual { v2 }, Lcom/jcraft/jsch/i;->a()V
    const/16 v4, 90
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->a(B)V
    const-string v4, "direct-tcpip"
    invoke-static { v4 }, Lcom/jcraft/jsch/q;->a(Ljava/lang/String;)[B
    move-result-object v4
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->b([B)V
    iget v4, v3, Lcom/jcraft/jsch/v;->c:I
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->a(I)V
    iget v4, v3, Lcom/jcraft/jsch/v;->f:I
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->a(I)V
    iget v4, v3, Lcom/jcraft/jsch/v;->g:I
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->a(I)V
    const/4 v4, 0
    invoke-static { v4 }, Lcom/jcraft/jsch/q;->a(Ljava/lang/String;)[B
    move-result-object v4
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->b([B)V
    const/4 v4, 0
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->a(I)V
    iget-object v4, v3, Lcom/jcraft/jsch/v;->b:Ljava/lang/String;
    invoke-static { v4 }, Lcom/jcraft/jsch/q;->a(Ljava/lang/String;)[B
    move-result-object v4
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->b([B)V
    iget v4, v3, Lcom/jcraft/jsch/v;->b:I
    invoke-virtual { v0, v4 }, Lcom/jcraft/jsch/k;->a(I)V
    invoke-virtual { v1, v2 }, Lcom/jcraft/jsch/f;->a(Lcom/jcraft/jsch/i;)V
    :L10
    const/16 v0, 1000
    :L11
    invoke-virtual { v3 }, Lcom/jcraft/jsch/v;->a()I
    move-result v2
    const/4 v4, -1
    if-ne v2, v4, :L13
    invoke-virtual { v1 }, Lcom/jcraft/jsch/f;->a()Z
    move-result v2
    if-eqz v2, :L13
    if-lez v0, :L13
    iget-boolean v2, v3, Lcom/jcraft/jsch/v;->a:Z
    :L12
    if-eqz v2, :L14
    :L13
    invoke-virtual { v1 }, Lcom/jcraft/jsch/f;->a()Z
    move-result v2
    if-nez v2, :L17
    new-instance v0, Lcom/jcraft/jsch/JSchException;
    const-string v1, "session is down"
    invoke-direct { v0, v1 }, Lcom/jcraft/jsch/JSchException;-><init>(Ljava/lang/String;)V
    throw v0
    :L14
    const-wide/16 v4, 50
    :L15
    invoke-static { v4, v5 }, Ljava/lang/Thread;->sleep(J)V
    :L16
    add-int/lit8 v0, v0, -1
    goto :L11
    :L17
    if-eqz v0, :L19
    :L18
    iget-boolean v0, v3, Lcom/jcraft/jsch/v;->a:Z
    if-eqz v0, :L20
    :L19
    new-instance v0, Lcom/jcraft/jsch/JSchException;
    const-string v1, "channel is not opened."
    invoke-direct { v0, v1 }, Lcom/jcraft/jsch/JSchException;-><init>(Ljava/lang/String;)V
    throw v0
    :L20
    const/4 v0, 1
    iput-boolean v0, v3, Lcom/jcraft/jsch/v;->c:Z
    iget-object v0, v3, Lcom/jcraft/jsch/v;->a:Lcom/jcraft/jsch/ad;
    iget-object v0, v0, Lcom/jcraft/jsch/ad;->a:Ljava/io/InputStream;
    if-eqz v0, :L0
    new-instance v0, Ljava/lang/Thread;
    invoke-direct { v0, v3 }, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
    iput-object v0, v3, Lcom/jcraft/jsch/v;->a:Ljava/lang/Thread;
    iget-object v0, v3, Lcom/jcraft/jsch/v;->a:Ljava/lang/Thread;
    new-instance v2, Ljava/lang/StringBuilder;
    const-string v4, "DirectTCPIP thread "
    invoke-direct { v2, v4 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
    invoke-virtual { v1 }, Lcom/jcraft/jsch/f;->a()Ljava/lang/String;
    move-result-object v1
    invoke-virtual { v2, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v1
    invoke-virtual { v1 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v1
    invoke-virtual { v0, v1 }, Ljava/lang/Thread;->setName(Ljava/lang/String;)V
    iget-object v0, v3, Lcom/jcraft/jsch/v;->a:Ljava/lang/Thread;
    invoke-virtual { v0 }, Ljava/lang/Thread;->start()V
    :L21
    goto/16 :L0
    :L22
    move-exception v2
    goto :L13
.end method
