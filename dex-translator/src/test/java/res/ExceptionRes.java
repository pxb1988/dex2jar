package res;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class ExceptionRes {
    public int a() {
        try {
            System.out.println("abc");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
