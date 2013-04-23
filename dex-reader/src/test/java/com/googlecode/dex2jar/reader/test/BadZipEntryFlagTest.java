package com.googlecode.dex2jar.reader.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStreamHack;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.dex2jar.reader.ZipInputStreamHack;

/**
 * Test case for issue 169
 * 
 * @author bob
 * 
 */
public class BadZipEntryFlagTest {
    @Test
    public void test1() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream("src/test/resources/bad.zip"));
        for (ZipArchiveEntry e = zis.getNextZipEntry(); e != null; e = zis.getNextZipEntry()) {
            e.getGeneralPurposeBit().useEncryption(false);
            if (!e.isDirectory()) {
                IOUtils.toByteArray(zis);
                System.out.println(e.getName());
            }
        }
    }

    @Test
    public void test0() throws IOException {
        ZipInputStream zis = new ZipInputStreamHack(new FileInputStream("src/test/resources/bad.zip"));
        for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
            System.out.println(e);
            IOUtils.toByteArray(zis);
        }
    }

    @Ignore("the way to build bad zip")
    @Test
    public void test2() throws IOException {
        ArchiveOutputStream zis = new ZipArchiveOutputStreamHack(new FileOutputStream("src/test/resources/bad.zip"));
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zis.putArchiveEntry(entry);
        zis.write("Test!".getBytes("UTF-8"));
        zis.closeArchiveEntry();
        zis.close();
    }

}
