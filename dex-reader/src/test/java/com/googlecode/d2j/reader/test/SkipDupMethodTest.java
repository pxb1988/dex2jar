package com.googlecode.d2j.reader.test;


import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;

public class SkipDupMethodTest {

    @Test
    public void test() throws IOException {
        InputStream is = SkipDupMethodTest.class.getClassLoader().getResourceAsStream("i200.dex");
        Assert.assertNotNull(is);
        DexFileReader reader = new DexFileReader(is);
        DexFileNode dfn1 = new DexFileNode();
        reader.accept(dfn1, DexFileReader.KEEP_ALL_METHODS);
        DexFileNode dfn2 = new DexFileNode();
        reader.accept(dfn2, 0);
        Assert.assertTrue(dfn1.clzs.get(0).methods.size() > dfn2.clzs.get(0).methods.size());
    }

}
