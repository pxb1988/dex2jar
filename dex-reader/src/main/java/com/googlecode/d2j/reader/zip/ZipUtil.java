/*
 * Copyright (c) 2009-2012 Panxiaobo
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

package com.googlecode.d2j.reader.zip;

import com.googlecode.d2j.util.zip.AccessBufByteArrayOutputStream;
import com.googlecode.d2j.util.zip.ZipEntry;
import com.googlecode.d2j.util.zip.ZipFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author bob
 * 
 */
public class ZipUtil {
    public static byte[] toByteArray(InputStream is) throws IOException {
        AccessBufByteArrayOutputStream out = new AccessBufByteArrayOutputStream();
        byte[] buff = new byte[1024];
        for (int c = is.read(buff); c > 0; c = is.read(buff)) {
            out.write(buff, 0, c);
        }
        return out.getBuf();
    }

    /**
     * read the dex file from file, if the file is a zip file, it will return the content of classes.dex in the zip
     * file.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readDex(File file) throws IOException {
        return readDex(file.toPath());
    }

    public static byte[] readDex(Path file) throws IOException {
        return readDex(Files.readAllBytes(file));
    }

    public static byte[] readDex(InputStream in) throws IOException {
        return readDex(toByteArray(in));
    }

    /**
     * read the dex file from byte array, if the byte array is a zip stream, it will return the content of classes.dex
     * in the zip stream.
     * 
     * @param data
     * @return the content of classes.dex
     * @throws IOException
     */
    public static byte[] readDex(byte[] data) throws IOException {
        if (data.length < 3) {
            throw new IOException("File too small to be a dex/zip");
        }
        if ("dex".equals(new String(data, 0, 3, StandardCharsets.ISO_8859_1))) {// dex
            return data;
        } else if ("PK".equals(new String(data, 0, 2, StandardCharsets.ISO_8859_1))) {// ZIP
            try (ZipFile zipFile = new ZipFile(data)) {
                ZipEntry classes = zipFile.findFirstEntry("classes.dex");
                if (classes != null) {
                    return toByteArray(zipFile.getInputStream(classes));
                } else {
                    throw new IOException("Can not find classes.dex in zip file");
                }
            }
        }
        throw new IOException("the src file not a .dex or zip file");
    }
}
