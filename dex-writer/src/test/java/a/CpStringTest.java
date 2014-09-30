package a;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.d2j.dex.writer.item.ConstPool;

public class CpStringTest {
    @Test
    public void test() {
        ConstPool cp = new ConstPool();
        Assert.assertTrue(cp.uniqString("b").compareTo(cp.uniqString("a")) > 0);
        Assert.assertTrue(cp.uniqType("Lb;").compareTo(cp.uniqType("La;")) > 0);
    }

}
