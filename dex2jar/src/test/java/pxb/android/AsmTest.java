package pxb.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifierClassVisitor;

public class AsmTest {
	@Test
	public void trycatch() throws IOException {
		InputStream in = AsmTest.class.getResourceAsStream("/test/TryCatch.class");
		Assert.assertNotNull(in);
		byte[] data = IOUtils.toByteArray(in);
		new ClassReader(data).accept(new ASMifierClassVisitor(new PrintWriter(System.out)), ClassReader.EXPAND_FRAMES
				| ClassReader.SKIP_DEBUG);
	}
}
