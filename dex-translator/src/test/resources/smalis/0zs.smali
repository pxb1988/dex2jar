.class Li;
.super Ljava/lang/Object;

.method protected static getCallScreenClassName()Ljava/lang/String;
    .registers 1

    .prologue
    .line 888
    invoke-static {}, Landroid/telephony/MSimTelephonyManager;->getDefault()Landroid/telephony/MSimTelephonyManager;

    move-result-object v0

    invoke-virtual {v0}, Landroid/telephony/MSimTelephonyManager;->isMultiSimEnabled()Z

    move-result v0

    if-eqz v0, :cond_11

    .line 889
    const-class v0, Lcom/android/phone/MSimInCallScreen;

    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    .line 891
    :goto_10
    return-object v0

    :cond_11
    const-class v0, Lcom/android/phone/InCallScreen;

    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    goto :goto_10
.end method
