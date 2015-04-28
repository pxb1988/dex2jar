.class public 'public'
.implements A
.implements interface
.implements public

.annotation visible Ljava/lang/A;
.annotation visible java/lang/B
.end annotation

.annotation visible Ljava/lang/annotation/Retention;
value e Ljava/lang/annotation/RetentionPolicy; = CLASS
value [e LR; = CLASS 'CLASS2' "CLASS3"
.end annotation

.method public ldc()V
ldc 0
ldc 1L
ldc "abc"
ldc I                ;;;;;;; equals to LI;
ldc Ljava/lang/Object;
ldc [I
ldc [[Ljava/lang/Object;
.end method

.method public checkcast()V
invokeinterface android/content/DialogInterface/dismiss()V 0
checkcast Ljava/lang/Object;
checkcast [I
checkcast [[Ljava/lang/Object;
checkcast I
invokestatic LB;->clone()V
invokestatic B/clone()V
invokestatic [B->clone()V
invokestatic [B/clone()V
getstatic LB;->a:I
getstatic B/a I
getstatic [B->a:I
getstatic [B/a I


getstatic B/public I

.end method
.method public "public"()V
.end method
.method public 'static'()V
.end method
