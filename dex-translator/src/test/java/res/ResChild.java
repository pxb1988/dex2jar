package res;

public class ResChild extends ResParent {
    @Override
    public void someMethod(int a, String b) {
        super.someMethod(a, b);
        this.someMethod(a, b);
    }

    public void anotherMethod() {
        this.someMethod(0, null);
        super.someMethod(0, null);
        super.bbb(0, null);
    }
}
