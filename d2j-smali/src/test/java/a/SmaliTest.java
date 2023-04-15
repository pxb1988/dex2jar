package a;

import com.googlecode.d2j.dex.writer.DexFileWriter;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.smali.BaksmaliDumpOut;
import com.googlecode.d2j.smali.BaksmaliDumper;
import com.googlecode.d2j.smali.Smali;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.baksmali.formatter.BaksmaliWriter;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.util.SyntheticAccessorResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SmaliTest {

    @Test
    public void test() throws IOException {
        DexFileNode dfn = new DexFileNode();
        try (InputStream is = SmaliTest.class.getResourceAsStream("/a.smali")) {
            Smali.smaliFile("a.smali", is, dfn);
        }
        for (DexClassNode dcn : dfn.clzs) {
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(System.out));
            new BaksmaliDumper(true, true).baksmaliClass(dcn, new BaksmaliDumpOut(w));
            w.flush();
        }
    }

    Map<String, DexClassNode> readDex(File path) throws IOException {
        DexFileReader dexFileReader = new DexFileReader(ZipUtil.readDex(path));
        DexFileNode dexFileNode = new DexFileNode();
        dexFileReader.accept(dexFileNode);
        Map<String, DexClassNode> map = new HashMap<>();
        for (DexClassNode c : dexFileNode.clzs) {
            map.put(c.className, c);
        }
        return map;
    }

    @Test
    public void test2() throws IOException {
        File dir = new File("../dex-translator/src/test/resources/dexes");
        File[] fs = dir.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.getName().endsWith(".dex") || f.getName().endsWith(".apk")) {
                    System.out.println(f.getName());
                    doTest(f);
                }
            }
        }
    }

    private void doTest(File dexFile) throws IOException {
        DexBackedDexFile dex;
        try {
            dex = DexFileFactory.loadDexFile(dexFile, Opcodes.forApi(14));
        } catch (DexBackedDexFile.NotADexFile ex) {
            ex.printStackTrace();
            return;
        }

        Map<String, DexClassNode> map = readDex(dexFile);

        for (DexBackedClassDef def : dex.getClasses()) {
            String type = def.getType();
            System.out.println(type);
            DexClassNode dexClassNode = map.get(type);
            assertNotNull(dexClassNode);
            String smali = baksmali(def); // original

            Smali.smaliFile2Node("fake.smali", smali);

            {
                byte[] data = toDex(dexClassNode);
                DexBackedClassDef def2 = new DexBackedDexFile(Opcodes.forApi(14), data).getClasses().iterator().next();
                String baksmali3 = baksmali(def2); // original
                assertEquals(smali, baksmali3);
            }

            String psmali = pbaksmali(dexClassNode);
            DexClassNode dexClassNode2 = Smali.smaliFile2Node("fake.smali", psmali);
            assertEquals(psmali, pbaksmali(dexClassNode2), "cmp smalip");

            byte[] data = toDex(dexClassNode2);
            DexBackedClassDef def2 = new DexBackedDexFile(Opcodes.forApi(14), data).getClasses().iterator().next();
            String baksmali3 = baksmali(def2); // original
            assertEquals(smali, baksmali3);
        }
    }

    private byte[] toDex(DexClassNode dexClassNode2) {
        DexFileWriter w = new DexFileWriter();
        dexClassNode2.accept(w);
        w.visitEnd();
        return w.toByteArray();
    }

    private static String pbaksmali(DexClassNode dcn) throws IOException {
        StringWriter bufWriter = new StringWriter();
        BufferedWriter w = new BufferedWriter(bufWriter);
        new BaksmaliDumper(true, true).baksmaliClass(dcn, new BaksmaliDumpOut(w));
        w.flush();
        bufWriter.flush();
        return bufWriter.toString();
    }

    private static String baksmali(DexBackedClassDef def) throws IOException {
        BaksmaliOptions opts = new BaksmaliOptions();
        opts.debugInfo = false;
        opts.syntheticAccessorResolver = new SyntheticAccessorResolver(Opcodes.getDefault(), new ArrayList<>());
        ClassDefinition classDefinition = new ClassDefinition(opts, def);
        StringWriter bufWriter = new StringWriter();
        BaksmaliWriter writer = new BaksmaliWriter(bufWriter);
        classDefinition.writeTo(writer);
        writer.flush();
        return bufWriter.toString();
    }

}
