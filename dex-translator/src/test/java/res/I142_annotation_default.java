package res;

public @interface I142_annotation_default {

    enum AA {
        A, B, C
    }

    AA aaa() default AA.A;

    AA bbb();

    String ccc() default "";

    String ddd() default "ddd";

    int eee() default 1;

    byte fff() default 1;

    short ggg() default 1;

    char hhh() default 1;

    boolean iii() default true;

    long jjj() default 1L;

    float kkk() default 1.0F;

    double lll() default 1.0D;

}
