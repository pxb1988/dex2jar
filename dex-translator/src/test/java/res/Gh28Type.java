package res;

import java.lang.reflect.Array;

/**
 * https://github.com/pxb1988/dex2jar/issues/28
 */
public class Gh28Type {

    protected void onCreate() {
        double t0[] = new double[2];
        t0[0] = 1.0;
        t0[1] = 2.0; // for https://github.com/pxb1988/dex2jar/issues/101
        double t1[] = (double[]) Array.newInstance(Double.TYPE, 1);
        t1[0] = 0;
        double t2[][] = new double[1][1];
        t2[0][0] = 0; // incorrectly translated to 0L (long) rather than 0.0 (double)
        double t3[][] = (double[][]) Array.newInstance(Double.TYPE, 1, 1);
        t3[0][0] = 0; // incorrectly translated to 0L (long) rather than 0.0 (double)
        a(t0);
        a(t1);
        a(t2[0]);
        a(t3[0]);
    }

    private void a(double[] t0) {
        // Just to avoid optimization of unused local variables in onCreate here above
    }

}
