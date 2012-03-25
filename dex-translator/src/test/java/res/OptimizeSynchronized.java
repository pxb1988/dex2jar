package res;

public class OptimizeSynchronized {
    public void a() {
        synchronized (this) {
            System.out.println(this);
        }
    }

    Object b;

    public void b() {
        synchronized (OptimizeSynchronized.class) {
            System.out.println(this);
        }
    }

    public void c() {
        synchronized (this.b) {
            System.out.println(this);
        }
    }
}
