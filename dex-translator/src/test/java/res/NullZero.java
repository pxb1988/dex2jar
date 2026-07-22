package res;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class NullZero {
    void nullzero() {
        String _null = null;
        int zero = 0;
        if (_null == null) {
            _null = "asdf";
            if (zero == 1) {
                zero = 123;
            }
        }
        System.out.println(0);
        System.out.println((String) null);
        System.out.println(_null);
        System.out.println(zero);
    }
}
