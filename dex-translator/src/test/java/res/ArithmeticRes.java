package res;

public class ArithmeticRes {

    /**
     * @param args
     */
    public static void main(String[] args) {
        int sum = -957401312;
        for (int i = 0; i < 10; i++) {
            System.out.println(sum);
            sum -= -1640531527;
        }
    }

    public static void b() {
        int sum = -957401312;
        for (int i = 0; i < 10; i++) {
            System.out.println(sum);
            sum -= Short.MAX_VALUE;
        }
    }

    public static void c() {
        int sum = -957401312;
        for (int i = 0; i < 10; i++) {
            System.out.println(sum);
            sum += Short.MAX_VALUE + 1;
        }
    }

    public static void d() {
        int sum = -957401312;
        for (int i = 0; i < 10; i++) {
            System.out.println(sum);
            sum += Short.MAX_VALUE;
        }
    }

    public static void e() {
        int sum = -957401312;
        for (int i = 0; i < 10; i++) {
            System.out.println(sum);
            sum -= Short.MAX_VALUE + 1;
        }
    }
}
