package com.googlecode.d2j.reader.test;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.util.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.Test;

/**
 * Test case for issue 169
 * 
 * @author bob
 * 
 */
public class BadZipEntryFlagTest {
    @Test
    public void test1() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(BadZipEntryFlagTest.class.getResourceAsStream("/bad.zip"));
        for (ZipArchiveEntry e = zis.getNextZipEntry(); e != null; e = zis.getNextZipEntry()) {
            e.getGeneralPurposeBit().useEncryption(false);
            if (!e.isDirectory()) {
                zis.read();
                System.out.println(e.getName());
            }
        }
    }

    @Test
    public void test0() throws IOException {
        byte[] data = ZipUtil.toByteArray(BadZipEntryFlagTest.class.getResourceAsStream("/bad.zip"));
        try (ZipFile zip = new ZipFile(data)) {
            for (com.googlecode.d2j.util.zip.ZipEntry e : zip.entries()) {
                System.out.println(e);
                if (!e.isDirectory()) {
                    zip.getInputStream(e).read();
                }
            }
        }
    }

    // @Ignore("the way to build bad zip")
    // @Test
    // public void test2() throws IOException {
    // ArchiveOutputStream zis = new ZipArchiveOutputStreamHack(new
    // FileOutputStream("src/test/resources/bad.zip"));
    // ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
    // zis.putArchiveEntry(entry);
    // zis.write("Test!".getBytes("UTF-8"));
    // zis.closeArchiveEntry();
    // zis.close();
    // }

}
