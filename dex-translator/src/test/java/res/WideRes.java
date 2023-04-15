package res;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class WideRes {
    float aaa() {
        float a = 1.01f + 2;
        float b = 12 + a;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return a + 2;
    }

    double bbb() {
        double b = 1.01 + 2;
        double a = 12312.123 + b;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return b + 2;
    }

    int ccc() {
        int b = 1 + 2;
        int a = 12312 + b;
        a += 1231231231;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return b + 2;
    }

    long ddd() {
        long b = 1L + 2;
        long a = 12312L + b;
        a += 1231232134234234524L;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return b + 2;
    }

}
