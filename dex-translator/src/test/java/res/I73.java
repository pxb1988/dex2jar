package res;

public class I73 {
    String a() {
        Object x = new Object();
        String[] y = (String[]) x;
        y[0] = null;
        int[] z = (int[]) x;
        z[0] = 0;
        return "" + y + z;
    }
}
