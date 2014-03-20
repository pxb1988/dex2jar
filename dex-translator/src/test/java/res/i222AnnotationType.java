package res;

@i222AnnotationType.B(name = "123", value = Object.class)
public class i222AnnotationType {
    public @interface B {
        String name();

        Class<?> value();
    }
}
