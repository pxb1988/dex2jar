package a;

import com.googlecode.d2j.dex.writer.item.ConstPool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CpStringTest {

    @Test
    public void test() {
        ConstPool cp = new ConstPool();
        assertTrue(cp.uniqString("b").compareTo(cp.uniqString("a")) > 0);
        assertTrue(cp.uniqType("Lb;").compareTo(cp.uniqType("La;")) > 0);
    }

}
