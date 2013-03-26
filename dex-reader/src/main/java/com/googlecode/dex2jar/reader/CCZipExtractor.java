/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;

/**
 * Based on commons-compress
 * 
 * @author Panxiaobo
 * 
 */
public class CCZipExtractor extends ZipExtractor {

    @Override
    public byte[] extract(byte[] data, String name) throws IOException {
        ZipArchiveInputStream zis = null;
        try {
            zis = new ZipArchiveInputStream(new ByteArrayInputStream(data));
            for (ZipArchiveEntry e = zis.getNextZipEntry(); e != null; e = zis.getNextZipEntry()) {
                e.getGeneralPurposeBit().useEncryption(false);
                if (e.getName().equals(name)) {
                    data = IOUtils.toByteArray(zis);
                    zis.close();
                    return data;
                }
            }
        } finally {
            IOUtils.closeQuietly(zis);
        }
        throw new IOException("can't find classes.dex in the zip");
    }

}
