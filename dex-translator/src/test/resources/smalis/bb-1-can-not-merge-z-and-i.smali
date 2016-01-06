.class Landroid/preference/MultiSelectListPreference;
.super Ljava/lang/Object;

.method test(Ljava/util/Set;Ljava/lang/Object;)V
  .registers 3
    invoke-interface { p1, p2 }, Ljava/util/Set;->add(Ljava/lang/Object;)Z
    move-result p1
    invoke-static { p0, p1 },  // p1 is boolean but used as integer
        Landroid/preference/MultiSelectListPreference;->access$076(Landroid/preference/MultiSelectListPreference;I)Z
    return-void
.end method

