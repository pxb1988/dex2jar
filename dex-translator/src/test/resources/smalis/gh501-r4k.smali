.class public final Lr4k;
.super Ljava/lang/Object;

# interfaces
.implements Lxak;


# direct methods
.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public a(Ljava/lang/Throwable;)Lg9k;
    .registers 7

    instance-of v0, p1, LbAu;

    const/4 v1, 0x0

    if-eqz v0, :cond_2e

    new-instance v0, Lg9k;

    move-object v2, p1

    check-cast v2, LbAu;

    invoke-virtual {v2}, LbAu;->b()I

    move-result v3

    invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    invoke-virtual {v2}, LbAu;->a()I

    move-result v2

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v2

    instance-of v4, p1, LoBu;

    if-eqz v4, :cond_2f

    :goto_1e
    check-cast p1, LoBu;

    iget p1, p1, LoBu;->a:I

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    invoke-virtual {p0, p1}, Lr4k;->e(Ljava/lang/Integer;)Ljava/lang/Integer;

    move-result-object v1

    :cond_2a
    invoke-direct {v0, v3, v2, v1}, Lg9k;-><init>(Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;)V

    move-object v1, v0

    :cond_2e
    return-object v1

    :cond_2f
    instance-of v4, p1, Lq4k;

    if-eqz v4, :cond_2a

    check-cast p1, Lq4k;

    iget-object p1, p1, Lq4k;->b:LbAu;

    instance-of v4, p1, LoBu;

    if-eqz v4, :cond_2a

    goto :goto_1e
.end method

.method public b(Ljava/lang/Exception;)Z
    .registers 5

    instance-of v0, p1, LbAu;

    const/4 v1, 0x0

    const/4 v2, 0x1

    if-eqz v0, :cond_16

    check-cast p1, LbAu;

    invoke-virtual {p1}, LbAu;->b()I

    move-result p1

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    if-nez p1, :cond_17

    :cond_12
    const/4 p1, 0x0

    :goto_13
    if-eqz p1, :cond_16

    const/4 v1, 0x1

    :cond_16
    return v1

    :cond_17
    invoke-virtual {p1}, Ljava/lang/Integer;->intValue()I

    move-result p1

    const/4 v0, 0x3

    if-ne p1, v0, :cond_12

    const/4 p1, 0x1

    goto :goto_13
.end method

.method public c(Ls9k;Ljava/lang/Integer;Ljava/lang/Integer;)Z
    .registers 7

    sget-object v0, Ls9k;->CRONET:Ls9k;

    const/4 v1, 0x0

    if-eq p1, v0, :cond_6

    return v1

    :cond_6
    const/4 p1, 0x1

    if-nez p2, :cond_2b1

    :cond_9
    const/4 v0, 0x2

    if-nez p2, :cond_2b8

    :cond_c
    const/4 v0, 0x0

    :goto_d
    if-eqz v0, :cond_2a3

    :goto_f
    const/4 v0, 0x1

    :goto_10
    if-eqz v0, :cond_295

    :goto_12
    const/4 v0, 0x1

    :goto_13
    if-eqz v0, :cond_287

    :goto_15
    const/4 v0, 0x1

    :goto_16
    if-eqz v0, :cond_279

    :goto_18
    const/4 v0, 0x1

    :goto_19
    if-eqz v0, :cond_26b

    :goto_1b
    const/4 v0, 0x1

    :goto_1c
    if-eqz v0, :cond_25c

    :goto_1e
    const/4 v0, 0x1

    :goto_1f
    if-eqz v0, :cond_24d

    :goto_21
    const/4 v0, 0x1

    :goto_22
    if-eqz v0, :cond_23e

    :goto_24
    const/4 p2, 0x1

    :goto_25
    if-nez p2, :cond_87

    if-nez p3, :cond_22c

    :cond_29
    const/16 p2, -0xd

    if-nez p3, :cond_235

    :cond_2d
    const/4 p2, 0x0

    :goto_2e
    if-eqz p2, :cond_21e

    :goto_30
    const/4 p2, 0x1

    :goto_31
    if-eqz p2, :cond_20f

    :goto_33
    const/4 p2, 0x1

    :goto_34
    if-eqz p2, :cond_200

    :goto_36
    const/4 p2, 0x1

    :goto_37
    if-eqz p2, :cond_1f1

    :goto_39
    const/4 p2, 0x1

    :goto_3a
    if-eqz p2, :cond_1e2

    :goto_3c
    const/4 p2, 0x1

    :goto_3d
    if-eqz p2, :cond_1d3

    :goto_3f
    const/4 p2, 0x1

    :goto_40
    if-eqz p2, :cond_1c4

    :goto_42
    const/4 p2, 0x1

    :goto_43
    if-eqz p2, :cond_1b5

    :goto_45
    const/4 p2, 0x1

    :goto_46
    if-eqz p2, :cond_1a6

    :goto_48
    const/4 p2, 0x1

    :goto_49
    if-eqz p2, :cond_197

    :goto_4b
    const/4 p2, 0x1

    :goto_4c
    if-eqz p2, :cond_188

    :goto_4e
    const/4 p2, 0x1

    :goto_4f
    if-eqz p2, :cond_179

    :goto_51
    const/4 p2, 0x1

    :goto_52
    if-eqz p2, :cond_16a

    :goto_54
    const/4 p2, 0x1

    :goto_55
    if-eqz p2, :cond_15b

    :goto_57
    const/4 p2, 0x1

    :goto_58
    if-eqz p2, :cond_14c

    :goto_5a
    const/4 p2, 0x1

    :goto_5b
    if-eqz p2, :cond_13d

    :goto_5d
    const/4 p2, 0x1

    :goto_5e
    if-eqz p2, :cond_12e

    :goto_60
    const/4 p2, 0x1

    :goto_61
    if-eqz p2, :cond_11f

    :goto_63
    const/4 p2, 0x1

    :goto_64
    if-eqz p2, :cond_110

    :goto_66
    const/4 p2, 0x1

    :goto_67
    if-eqz p2, :cond_101

    :goto_69
    const/4 p2, 0x1

    :goto_6a
    if-eqz p2, :cond_f2

    :goto_6c
    const/4 p2, 0x1

    :goto_6d
    if-eqz p2, :cond_e4

    :goto_6f
    const/4 p2, 0x1

    :goto_70
    if-eqz p2, :cond_d7

    :goto_72
    const/4 p2, 0x1

    :goto_73
    if-eqz p2, :cond_ca

    :goto_75
    const/4 p2, 0x1

    :goto_76
    if-eqz p2, :cond_bd

    :goto_78
    const/4 p2, 0x1

    :goto_79
    if-eqz p2, :cond_b0

    :goto_7b
    const/4 p2, 0x1

    :goto_7c
    if-eqz p2, :cond_a3

    :goto_7e
    const/4 p2, 0x1

    :goto_7f
    if-eqz p2, :cond_96

    :goto_81
    const/4 p2, 0x1

    :goto_82
    if-eqz p2, :cond_89

    :goto_84
    const/4 p2, 0x1

    :goto_85
    if-eqz p2, :cond_88

    :cond_87
    const/4 v1, 0x1

    :cond_88
    return v1

    :cond_89
    const/16 p2, -0x160

    if-nez p3, :cond_8f

    :cond_8d
    const/4 p2, 0x0

    goto :goto_85

    :cond_8f
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result p3

    if-ne p3, p2, :cond_8d

    goto :goto_84

    :cond_96
    const/16 p2, -0x144

    if-nez p3, :cond_9c

    :cond_9a
    const/4 p2, 0x0

    goto :goto_82

    :cond_9c
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_9a

    goto :goto_81

    :cond_a3
    const/16 p2, -0x96

    if-nez p3, :cond_a9

    :cond_a7
    const/4 p2, 0x0

    goto :goto_7f

    :cond_a9
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_a7

    goto :goto_7e

    :cond_b0
    const/16 p2, -0x164

    if-nez p3, :cond_b6

    :cond_b4
    const/4 p2, 0x0

    goto :goto_7c

    :cond_b6
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_b4

    goto :goto_7b

    :cond_bd
    const/16 p2, -0x89

    if-nez p3, :cond_c3

    :cond_c1
    const/4 p2, 0x0

    goto :goto_79

    :cond_c3
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_c1

    goto :goto_78

    :cond_ca
    const/16 p2, -0x6b

    if-nez p3, :cond_d0

    :cond_ce
    const/4 p2, 0x0

    goto :goto_76

    :cond_d0
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_ce

    goto :goto_75

    :cond_d7
    const/16 p2, -0xc9

    if-nez p3, :cond_dd

    :cond_db
    const/4 p2, 0x0

    goto :goto_73

    :cond_dd
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_db

    goto :goto_72

    :cond_e4
    const/16 p2, -0xca

    if-nez p3, :cond_ea

    :cond_e8
    const/4 p2, 0x0

    goto :goto_70

    :cond_ea
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_e8

    goto/16 :goto_6f

    :cond_f2
    const/16 p2, -0xc8

    if-nez p3, :cond_f9

    :cond_f6
    const/4 p2, 0x0

    goto/16 :goto_6d

    :cond_f9
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_f6

    goto/16 :goto_6c

    :cond_101
    const/16 p2, -0x323

    if-nez p3, :cond_108

    :cond_105
    const/4 p2, 0x0

    goto/16 :goto_6a

    :cond_108
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_105

    goto/16 :goto_69

    :cond_110
    const/16 p2, -0x8b

    if-nez p3, :cond_117

    :cond_114
    const/4 p2, 0x0

    goto/16 :goto_67

    :cond_117
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_114

    goto/16 :goto_66

    :cond_11f
    const/16 p2, -0x82

    if-nez p3, :cond_126

    :cond_123
    const/4 p2, 0x0

    goto/16 :goto_64

    :cond_126
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_123

    goto/16 :goto_63

    :cond_12e
    const/16 p2, -0x79

    if-nez p3, :cond_135

    :cond_132
    const/4 p2, 0x0

    goto/16 :goto_61

    :cond_135
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_132

    goto/16 :goto_60

    :cond_13d
    const/16 p2, -0x78

    if-nez p3, :cond_144

    :cond_141
    const/4 p2, 0x0

    goto/16 :goto_5e

    :cond_144
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_141

    goto/16 :goto_5d

    :cond_14c
    const/16 p2, -0x76

    if-nez p3, :cond_153

    :cond_150
    const/4 p2, 0x0

    goto/16 :goto_5b

    :cond_153
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_150

    goto/16 :goto_5a

    :cond_15b
    const/16 p2, -0x6f

    if-nez p3, :cond_162

    :cond_15f
    const/4 p2, 0x0

    goto/16 :goto_58

    :cond_162
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_15f

    goto/16 :goto_57

    :cond_16a
    const/16 p2, -0x6d

    if-nez p3, :cond_171

    :cond_16e
    const/4 p2, 0x0

    goto/16 :goto_55

    :cond_171
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_16e

    goto/16 :goto_54

    :cond_179
    const/16 p2, -0x6a

    if-nez p3, :cond_180

    :cond_17d
    const/4 p2, 0x0

    goto/16 :goto_52

    :cond_180
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_17d

    goto/16 :goto_51

    :cond_188
    const/16 p2, -0x69

    if-nez p3, :cond_18f

    :cond_18c
    const/4 p2, 0x0

    goto/16 :goto_4f

    :cond_18f
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_18c

    goto/16 :goto_4e

    :cond_197
    const/16 p2, -0x68

    if-nez p3, :cond_19e

    :cond_19b
    const/4 p2, 0x0

    goto/16 :goto_4c

    :cond_19e
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_19b

    goto/16 :goto_4b

    :cond_1a6
    const/16 p2, -0x67

    if-nez p3, :cond_1ad

    :cond_1aa
    const/4 p2, 0x0

    goto/16 :goto_49

    :cond_1ad
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_1aa

    goto/16 :goto_48

    :cond_1b5
    const/16 p2, -0x66

    if-nez p3, :cond_1bc

    :cond_1b9
    const/4 p2, 0x0

    goto/16 :goto_46

    :cond_1bc
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_1b9

    goto/16 :goto_45

    :cond_1c4
    const/16 p2, -0x65

    if-nez p3, :cond_1cb

    :cond_1c8
    const/4 p2, 0x0

    goto/16 :goto_43

    :cond_1cb
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_1c8

    goto/16 :goto_42

    :cond_1d3
    const/16 p2, -0x64

    if-nez p3, :cond_1da

    :cond_1d7
    const/4 p2, 0x0

    goto/16 :goto_40

    :cond_1da
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_1d7

    goto/16 :goto_3f

    :cond_1e2
    const/16 p2, -0x1b

    if-nez p3, :cond_1e9

    :cond_1e6
    const/4 p2, 0x0

    goto/16 :goto_3d

    :cond_1e9
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_1e6

    goto/16 :goto_3c

    :cond_1f1
    const/16 p2, -0x1a

    if-nez p3, :cond_1f8

    :cond_1f5
    const/4 p2, 0x0

    goto/16 :goto_3a

    :cond_1f8
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_1f5

    goto/16 :goto_39

    :cond_200
    const/16 p2, -0x17

    if-nez p3, :cond_207

    :cond_204
    const/4 p2, 0x0

    goto/16 :goto_37

    :cond_207
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_204

    goto/16 :goto_36

    :cond_20f
    const/16 p2, -0x15

    if-nez p3, :cond_216

    :cond_213
    const/4 p2, 0x0

    goto/16 :goto_34

    :cond_216
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_213

    goto/16 :goto_33

    :cond_21e
    const/4 p2, -0x3

    if-nez p3, :cond_224

    :cond_221
    const/4 p2, 0x0

    goto/16 :goto_31

    :cond_224
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_221

    goto/16 :goto_30

    :cond_22c
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result p2

    const/16 v0, -0xc

    if-ne p2, v0, :cond_29

    goto :goto_23b

    :cond_235
    invoke-virtual {p3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p2, :cond_2d

    :goto_23b
    const/4 p2, 0x1

    goto/16 :goto_2e

    :cond_23e
    const/16 v0, 0xa

    if-nez p2, :cond_245

    :cond_242
    const/4 p2, 0x0

    goto/16 :goto_25

    :cond_245
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result p2

    if-ne p2, v0, :cond_242

    goto/16 :goto_24

    :cond_24d
    const/16 v0, 0x9

    if-nez p2, :cond_254

    :cond_251
    const/4 v0, 0x0

    goto/16 :goto_22

    :cond_254
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_251

    goto/16 :goto_21

    :cond_25c
    const/16 v0, 0x8

    if-nez p2, :cond_263

    :cond_260
    const/4 v0, 0x0

    goto/16 :goto_1f

    :cond_263
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_260

    goto/16 :goto_1e

    :cond_26b
    const/4 v0, 0x7

    if-nez p2, :cond_271

    :cond_26e
    const/4 v0, 0x0

    goto/16 :goto_1c

    :cond_271
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_26e

    goto/16 :goto_1b

    :cond_279
    const/4 v0, 0x6

    if-nez p2, :cond_27f

    :cond_27c
    const/4 v0, 0x0

    goto/16 :goto_19

    :cond_27f
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_27c

    goto/16 :goto_18

    :cond_287
    const/4 v0, 0x5

    if-nez p2, :cond_28d

    :cond_28a
    const/4 v0, 0x0

    goto/16 :goto_16

    :cond_28d
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_28a

    goto/16 :goto_15

    :cond_295
    const/4 v0, 0x4

    if-nez p2, :cond_29b

    :cond_298
    const/4 v0, 0x0

    goto/16 :goto_13

    :cond_29b
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_298

    goto/16 :goto_12

    :cond_2a3
    const/4 v0, 0x3

    if-nez p2, :cond_2a9

    :cond_2a6
    const/4 v0, 0x0

    goto/16 :goto_10

    :cond_2a9
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_2a6

    goto/16 :goto_f

    :cond_2b1
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v0

    if-ne v0, p1, :cond_9

    goto :goto_2be

    :cond_2b8
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_c

    :goto_2be
    const/4 v0, 0x1

    goto/16 :goto_d
.end method

.method public d(Ls9k;Ljava/lang/Integer;)LK9k;
    .registers 7

    sget-object v0, Ls9k;->CRONET:Ls9k;

    const/4 v1, 0x0

    if-eq p1, v0, :cond_6

    return-object v1

    :cond_6
    const/4 p1, 0x0

    const/4 v0, 0x1

    if-nez p2, :cond_40

    :cond_a
    const/4 v2, 0x0

    :goto_b
    if-eqz v2, :cond_10

    sget-object v1, LK9k;->DNS_ERROR:LK9k;

    :cond_f
    :goto_f
    return-object v1

    :cond_10
    if-nez p2, :cond_1b

    :goto_12
    const/4 v2, 0x6

    if-nez p2, :cond_23

    :cond_15
    const/4 v2, 0x0

    :goto_16
    if-eqz v2, :cond_2b

    sget-object v1, LK9k;->TIMEOUT:LK9k;

    goto :goto_f

    :cond_1b
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    const/4 v3, 0x4

    if-eq v2, v3, :cond_29

    goto :goto_12

    :cond_23
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v3

    if-ne v3, v2, :cond_15

    :cond_29
    const/4 v2, 0x1

    goto :goto_16

    :cond_2b
    if-nez p2, :cond_32

    :cond_2d
    :goto_2d
    if-eqz p1, :cond_3b

    sget-object v1, LK9k;->NETWORK_CHANGED:LK9k;

    goto :goto_f

    :cond_32
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    const/4 v3, 0x3

    if-ne v2, v3, :cond_2d

    const/4 p1, 0x1

    goto :goto_2d

    :cond_3b
    if-eqz p2, :cond_f

    sget-object v1, LK9k;->CONNECTION_ERROR:LK9k;

    goto :goto_f

    :cond_40
    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    if-ne v2, v0, :cond_a

    const/4 v2, 0x1

    goto :goto_b
.end method

.method public final e(Ljava/lang/Integer;)Ljava/lang/Integer;
    .registers 3

    const/4 v0, 0x0

    if-nez p1, :cond_4

    :goto_3
    return-object v0

    :cond_4
    invoke-virtual {p1}, Ljava/lang/Number;->intValue()I

    move-result p1

    if-nez p1, :cond_b

    goto :goto_3

    :cond_b
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    move-object v0, p1

    goto :goto_3
.end method
