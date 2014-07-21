package res;

public interface ConstValues {

    byte b1 = 0;
    byte b2 = -1;
    byte b3 = 1;
    byte b4 = 0x7F;

    short s1 = 0;
    short s2 = -1;
    short s3 = 1;
    short s4 = 0x7FFF;

    char c1 = 0;
    char c2 = 1;
    char c3 = 0x7FFF;

    int i1 = -1;
    int i2 = 0;
    int i3 = 0xFF;
    int i4 = 0xFFFF;
    int i5 = 0xFFFFFF;
    int i6 = 0x7FFFFFFF;

    float f1 = 0;
    float f2 = -1;
    float f3 = 1;
    float f4 = Float.MAX_VALUE;
    float f5 = Float.MIN_VALUE;

    double d1 = 0;
    double d2 = -1;
    double d3 = 1;
    double d4 = Double.MAX_VALUE;
    double d5 = Double.MIN_VALUE;

    long l1 = 0;
    long l2 = -1;
    long l3 = 1;
    long l4 = 0x7FFFFFFFFFFFFFFFL;

    boolean bl1 = true;
    boolean bl2 = false;

    Abc abc1 = null;
    Abc abc2 = Abc.X;

    enum Abc {
        X, Y
    }
}
