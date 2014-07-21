package res;

/**
 * test case for issue 87, http://code.google.com/p/dex2jar/issues/detail?id=87
 * 
 * @author Panxiaobo
 * 
 */
public class NoEndRes {
    public void b() {
    }

    public void a() {
        while (true) {
            b();
        }
    }
}
