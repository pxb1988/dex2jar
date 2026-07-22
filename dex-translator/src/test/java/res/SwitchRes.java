package res;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class SwitchRes {

    void sw1() {
        int a = 1;
        switch (a) {
        case 1:
        case 2:
        case 3:
            System.out.println("123");
            break;
        case 100:
            System.out.println("100");
            break;
        default:
            System.out.println("def");
            break;
        }
    }

    void sw2() {
        int a = 1;
        int b = 2;
        switch (a) {
        case 1:
            if (b == 2) {
                System.out.println("b");
            } else {
                System.out.println("bbb");
            }
        case 2:
        case 3:
            System.out.println("123");
            break;
        case 100:
            System.out.println("100");
            break;
        default:
            System.out.println("def");
            break;
        }
    }

}
