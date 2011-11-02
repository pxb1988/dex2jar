package res;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import res.I88.A;

@A
public class I88 {
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
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE,
            ElementType.METHOD, ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE })
    public @interface A {
        String a() default "234";
    }
}
