package res;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import res.I88.A;

@A
public class I88 {

    public static void main(String... args) {
        A a = I88.class.getAnnotation(A.class);
        System.out.println(a.a());
    }

    @A
    I88() {
    }

    @A
    int i;

    @A
    public void a(@A int i) {
        @A
        // TODO the annotation is gone.
        String b = "";
    }

    @A
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE,
            ElementType.METHOD, ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE })
    public @interface A {
        String a() default "234";
    }
}
