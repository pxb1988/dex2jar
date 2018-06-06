.class Lgh/i186;
.super Ljava/lang/Object;

.method public getEntryIndex(FFLcom/github/mikephil/charting/data/DataSet$Rounding;)I
  .registers 14
    iget-object v0, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    const/4 v1, -1
    if-eqz v0, :L16
    iget-object v0, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { v0 }, Ljava/util/List;->isEmpty()Z
    move-result v0
    if-eqz v0, :L0
    goto/16 :L16
  :L0
    const/4 v0, 0
    iget-object v2, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { v2 }, Ljava/util/List;->size()I
    move-result v2
    add-int/lit8 v2, v2, -1
  :L1
    if-ge v0, v2, :L7
    add-int v3, v0, v2
    div-int/lit8 v3, v3, 2
    iget-object v4, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { v4, v3 }, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, Lcom/github/mikephil/charting/data/Entry;
    invoke-virtual { v4 }, Lcom/github/mikephil/charting/data/Entry;->getX()F
    move-result v4
    sub-float/2addr v4, p1
    iget-object v5, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    add-int/lit8 v6, v3, 1
    invoke-interface { v5, v6 }, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v5
    check-cast v5, Lcom/github/mikephil/charting/data/Entry;
    invoke-virtual { v5 }, Lcom/github/mikephil/charting/data/Entry;->getX()F
    move-result v5
    sub-float/2addr v5, p1
    invoke-static { v4 }, Ljava/lang/Math;->abs(F)F
    move-result v7
    invoke-static { v5 }, Ljava/lang/Math;->abs(F)F
    move-result v5
    cmpg-float v8, v5, v7
    if-gez v8, :L3
  :L2
    move v0, v6
    goto :L1
  :L3
    cmpg-float v5, v7, v5
    if-gez v5, :L4
    goto :L5
  :L4
    float-to-double v4, v4
    const-wide/16 v7, 0
    cmpl-double v9, v4, v7
    if-ltz v9, :L6
  :L5
    move v2, v3
    goto :L1
  :L6
    cmpg-double v3, v4, v7
    if-gez v3, :L1
    goto :L2
  :L7
    if-eq v2, v1, :L15
    iget-object v0, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { v0, v2 }, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Lcom/github/mikephil/charting/data/Entry;
    invoke-virtual { v0 }, Lcom/github/mikephil/charting/data/Entry;->getX()F
    move-result v0
    sget-object v1, Lcom/github/mikephil/charting/data/DataSet$Rounding;->UP:Lcom/github/mikephil/charting/data/DataSet$Rounding;
    if-ne p3, v1, :L8
    cmpg-float p1, v0, p1
    if-gez p1, :L9
    iget-object p1, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { p1 }, Ljava/util/List;->size()I
    move-result p1
    add-int/lit8 p1, p1, -1
    if-ge v2, p1, :L9
    add-int/lit8 v2, v2, 1
    goto :L9
  :L8
    sget-object v1, Lcom/github/mikephil/charting/data/DataSet$Rounding;->DOWN:Lcom/github/mikephil/charting/data/DataSet$Rounding;
    if-ne p3, v1, :L9
    cmpl-float p1, v0, p1
    if-lez p1, :L9
    if-lez v2, :L9
    add-int/lit8 v2, v2, -1
  :L9
    invoke-static { p2 }, Ljava/lang/Float;->isNaN(F)Z
    move-result p1
    if-nez p1, :L15
  :L10
    if-lez v2, :L11
    iget-object p1, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    add-int/lit8 p3, v2, -1
    invoke-interface { p1, p3 }, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object p1
    check-cast p1, Lcom/github/mikephil/charting/data/Entry;
    invoke-virtual { p1 }, Lcom/github/mikephil/charting/data/Entry;->getX()F
    move-result p1
    cmpl-float p1, p1, v0
    if-nez p1, :L11
    add-int/lit8 v2, v2, -1
    goto :L10
  :L11
    iget-object p1, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { p1, v2 }, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object p1
    check-cast p1, Lcom/github/mikephil/charting/data/Entry;
    invoke-virtual { p1 }, Lcom/github/mikephil/charting/data/Entry;->getY()F
    move-result p1
    move p3, p1
  :L12
    move p1, v2
  :L13
    add-int/lit8 v2, v2, 1
    iget-object v1, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { v1 }, Ljava/util/List;->size()I
    move-result v1
    if-ge v2, v1, :L14
    iget-object v1, p0, Lcom/github/mikephil/charting/data/DataSet;->mValues:Ljava/util/List;
    invoke-interface { v1, v2 }, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v1
    check-cast v1, Lcom/github/mikephil/charting/data/Entry;
    invoke-virtual { v1 }, Lcom/github/mikephil/charting/data/Entry;->getX()F
    move-result v3
    cmpl-float v3, v3, v0
    if-nez v3, :L14
    invoke-virtual { v1 }, Lcom/github/mikephil/charting/data/Entry;->getY()F
    move-result v1
    sub-float/2addr v1, p2
    invoke-static { v1 }, Ljava/lang/Math;->abs(F)F
    move-result v1
    sub-float v3, p3, p2
    invoke-static { v3 }, Ljava/lang/Math;->abs(F)F
    move-result v3
    cmpg-float v1, v1, v3
    if-gez v1, :L13
    move p3, p2
    goto :L12
  :L14
    move v2, p1
  :L15
    return v2
  :L16
    return v1
.end method

