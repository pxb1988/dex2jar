.class Luseless/new;
.super Ljava/lang/Object;
.method public onCreate1(Landroid/os/Bundle;)V
    .registers 7
    const/4 v4, -2
    const/4 v3, 1
    const/4 v2, 0
    invoke-super { v5, v6 }, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V
    const v0, 2130903050
    invoke-virtual { v5, v0 }, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->setContentView(I)V
    new-instance v0, Landroid/widget/LinearLayout;
    invoke-direct { v0, v5 }, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V
    iput-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->container:Landroid/widget/LinearLayout;
    move-result-object v0
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->PREFS_NAME:Ljava/lang/String;
    invoke-virtual { v5, v0, v2 }, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
    move-result-object v0
    iput-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->settings:Landroid/content/SharedPreferences;
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->settings:Landroid/content/SharedPreferences;
    const-string v1, "times"
    invoke-interface { v0, v1, v2 }, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z
    move-result v0
    iput-boolean v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->times:Z
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->settings:Landroid/content/SharedPreferences;
    const-string v1, "jump"
    invoke-interface { v0, v1, v2 }, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z
    move-result v0
    iput-boolean v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->removeadv:Z
    invoke-static { v5 }, Lcom/mobclick/android/MobclickAgent;->updateOnlineConfig(Landroid/content/Context;)V
    invoke-static { v2 }, Lcom/mobclick/android/MobclickAgent;->setUpdateOnlyWifi(Z)V
    invoke-direct { v5 }, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->se()V
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->a:Ljava/lang/String;
    const-string v1, "f90c2179e4ea7fb0531a1182b2ab90aa"
    invoke-virtual { v0, v1 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :L0
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->pointsTextView:Landroid/widget/TextView;
    const/16 v1, 111
    invoke-virtual { v0, v1 }, Landroid/widget/TextView;->setText(I)V
    :L0
    iget-boolean v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->removeadv:Z
    if-nez v0, :L2
    invoke-virtual { v5 }, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->getApplicationContext()Landroid/content/Context;
    move-result-object v0
    const-string v1, "window"
    invoke-virtual { v0, v1 }, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Landroid/view/WindowManager;
    iput-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wm:Landroid/view/WindowManager;
    new-instance v0, Landroid/view/WindowManager$LayoutParams;
    invoke-direct { v0 }, Landroid/view/WindowManager$LayoutParams;-><init>()V
    iput-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    const/16 v1, 2003
    iput v1, v0, Landroid/view/WindowManager$LayoutParams;->type:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    iput v3, v0, Landroid/view/WindowManager$LayoutParams;->format:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    const/16 v1, 40
    iput v1, v0, Landroid/view/WindowManager$LayoutParams;->flags:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    iput v4, v0, Landroid/view/WindowManager$LayoutParams;->width:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    iput v2, v0, Landroid/view/WindowManager$LayoutParams;->x:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    iput v2, v0, Landroid/view/WindowManager$LayoutParams;->y:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    iput v4, v0, Landroid/view/WindowManager$LayoutParams;->height:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    const/16 v1, 49
    iput v1, v0, Landroid/view/WindowManager$LayoutParams;->gravity:I
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wm:Landroid/view/WindowManager;
    iget-object v1, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->container:Landroid/widget/LinearLayout;
    iget-object v2, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->wmParams:Landroid/view/WindowManager$LayoutParams;
    invoke-interface { v0, v1, v2 }, Landroid/view/WindowManager;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    new-instance v0, Ljava/lang/Object;
    iget-object v1, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->container:Landroid/widget/LinearLayout;
    const/16 v1, 30
    iput-boolean v3, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->Badview:Z
    :L1
    new-instance v0, Lcom/adroidbscpc/a15mSurvival3rh/MainA$ContThread;
    invoke-direct { v0, v5 }, Lcom/adroidbscpc/a15mSurvival3rh/MainA$ContThread;-><init>(Lcom/adroidbscpc/a15mSurvival3rh/MainA;)V
    iput-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->conthred:Ljava/lang/Thread;
    iget-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->conthred:Ljava/lang/Thread;
    invoke-virtual { v0 }, Ljava/lang/Thread;->start()V
    new-instance v0, Lcom/adroidbscpc/a15mSurvival3rh/MainA$1;
    invoke-direct { v0, v5 }, Lcom/adroidbscpc/a15mSurvival3rh/MainA$1;-><init>(Lcom/adroidbscpc/a15mSurvival3rh/MainA;)V
    iput-object v0, v5, Lcom/adroidbscpc/a15mSurvival3rh/MainA;->mainHandler:Landroid/os/Handler;
    return-void
    :L2
    invoke-static { v5 }, Lcom/cooguo/advideo/VideoAdsManager;->getInstance(Landroid/content/Context;)Lcom/cooguo/advideo/VideoAdsManager;
    move-result-object v0
    invoke-virtual { v0, v3 }, Lcom/cooguo/advideo/VideoAdsManager;->receiveVideoAd(I)V
    move-result-object v0
    invoke-static { }, Lcom/kuguo/ad/KuguoAdsManager;->getInstance()Lcom/kuguo/ad/KuguoAdsManager;
    move-result-object v0
    invoke-virtual { v0, v5, v3 }, Lcom/kuguo/ad/KuguoAdsManager;->receivePushMessage(Landroid/content/Context;Z)V
    goto :L1
.end method

.method private static setAdmobAdView(Landroid/content/Context;Landroid/view/ViewGroup;Landroid/view/ViewGroup$LayoutParams;)V
    .registers 5
    new-instance v0, Ljava/lang/Object;
    const/4 v1, 0
    const v1, 16777215
    const/4 v1, -1
    const v1, -3355444
    const-string v1, "android game arcade action casual application"
    const/16 v1, 15
    new-instance v1, Lorg/collcode/xrlophone/AdManager$1;
    invoke-virtual { v3, v0, v4 }, Landroid/view/ViewGroup;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    return-void
.end method
