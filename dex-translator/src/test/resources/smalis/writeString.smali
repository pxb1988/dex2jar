.class LDD;
.super Lee;
.method writeString(Ljava/lang/String;[BIZ)I
    .catch Ljava/io/UnsupportedEncodingException; { :L0 .. :L1 } :L14
    .catch Ljava/io/UnsupportedEncodingException; { :L2 .. :L3 } :L16
    .catch Ljava/io/UnsupportedEncodingException; { :L4 .. :L5 } :L14
    .catch Ljava/io/UnsupportedEncodingException; { :L6 .. :L7 } :L16
    .catch Ljava/io/UnsupportedEncodingException; { :L8 .. :L11 } :L14
    .catch Ljava/io/UnsupportedEncodingException; { :L12 .. :L13 } :L16
    .registers 12
    move v2, v10
    if-eqz v11, :L10
    :L0
    iget v4, v7, Ljcifs/smb/ServerMessageBlock;->headerStart:I
    sub-int v4, v10, v4
    rem-int/lit8 v4, v4, 2
    :L1
    if-eqz v4, :L4
    add-int/lit8 v1, v10, 1
    const/4 v4, 0
    :L2
    aput-byte v4, v9, v10
    :L3
    move v10, v1
    :L4
    const-string v4, "UTF-16LE"
    invoke-virtual { v8, v4 }, Ljava/lang/String;->getBytes(Ljava/lang/String;)[B
    move-result-object v4
    const/4 v5, 0
    invoke-virtual { v8 }, Ljava/lang/String;->length()I
    move-result v6
    mul-int/lit8 v6, v6, 2
    invoke-static { v4, v5, v9, v10, v6 }, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V
    invoke-virtual { v8 }, Ljava/lang/String;->length()I
    :L5
    move-result v4
    mul-int/lit8 v4, v4, 2
    add-int/2addr v10, v4
    add-int/lit8 v1, v10, 1
    const/4 v4, 0
    :L6
    aput-byte v4, v9, v10
    :L7
    add-int/lit8 v10, v1, 1
    const/4 v4, 0
    :L8
    aput-byte v4, v9, v1
    :L9
    sub-int v4, v10, v2
    return v4
    :L10
    sget-object v4, Ljcifs/smb/SmbConstants;->OEM_ENCODING:Ljava/lang/String;
    invoke-virtual { v8, v4 }, Ljava/lang/String;->getBytes(Ljava/lang/String;)[B
    move-result-object v0
    const/4 v4, 0
    array-length v5, v0
    invoke-static { v0, v4, v9, v10, v5 }, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V
    array-length v4, v0
    :L11
    add-int/2addr v10, v4
    add-int/lit8 v1, v10, 1
    const/4 v4, 0
    :L12
    aput-byte v4, v9, v10
    :L13
    move v10, v1
    goto :L9
    :L14
    move-exception v4
    move-object v3, v4
    :L15
    sget-object v4, Ljcifs/smb/ServerMessageBlock;->log:Ljcifs/util/LogStream;
    sget v4, Ljcifs/util/LogStream;->level:I
    const/4 v5, 1
    if-le v4, v5, :L9
    sget-object v4, Ljcifs/smb/ServerMessageBlock;->log:Ljcifs/util/LogStream;
    invoke-virtual { v3, v4 }, Ljava/io/UnsupportedEncodingException;->printStackTrace(Ljava/io/PrintStream;)V
    goto :L9
    :L16
    move-exception v4
    move-object v3, v4
    move v10, v1
    goto :L15
.end method
