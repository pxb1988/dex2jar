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

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A hack to the ZipInputStream, clean the encrypted-bit flag before checking.
 * 
 * from the code we know the {@link #createZipEntry(String)} is always invoked before the check in openjdk7, and in
 * openjdk6 flag is read from tmpbuf after {@link #createZipEntry(String)} , so this class will change
 * {@link ZipInputStream#flag} and {@link ZipInputStream#tmpbuf} in {@link #createZipEntry(String)} using reflection API
 * 
 * <pre>
 * // here are the code from openjdk-7
 * // Force to use UTF-8 if the EFS bit is ON, even the cs is NOT UTF-8
 * ZipEntry e = createZipEntry(((flag &amp; EFS) != 0) ? zc.toStringUTF8(b, len) : zc.toString(b, len));
 * // now get the remaining fields for the entry
 * if ((flag &amp; 1) == 1) {
 *     throw new ZipException(&quot;encrypted ZIP entry not supported&quot;);
 * }
 * e.method = get16(tmpbuf, LOCHOW);
 * e.time = get32(tmpbuf, LOCTIM);
 * </pre>
 * 
 * <pre>
 * // here are the code from openjdk-6
 * ZipEntry e = createZipEntry(getUTF8String(b, 0, len));
 * // now get the remaining fields for the entry
 * flag = get16(tmpbuf, LOCFLG);
 * if ((flag &amp; 1) == 1) {
 *     throw new ZipException(&quot;encrypted ZIP entry not supported&quot;);
 * }
 * e.method = get16(tmpbuf, LOCHOW);
 * e.time = get32(tmpbuf, LOCTIM);
 * </pre>
 * 
 * @author Panxiaobo
 * 
 */
public class ZipInputStreamHack extends ZipInputStream {
    static java.lang.reflect.Field flagField;
    static java.lang.reflect.Field tmpbufField;

    static {
        try {
            flagField = ZipInputStream.class.getDeclaredField("flag");
            flagField.setAccessible(true);
        } catch (Exception ignored) {

        }
        try {
            tmpbufField = ZipInputStream.class.getDeclaredField("tmpbuf");
            tmpbufField.setAccessible(true);
        } catch (Exception ignored) {

        }

    }

    public ZipInputStreamHack(InputStream in) {
        super(in);
    }

    @Override
    protected ZipEntry createZipEntry(String name) {
        if (flagField != null) { // for openjdk7
            try {
                int flag = (Integer) flagField.get(this);
                flagField.set(this, (flag >> 1) << 1);
            } catch (Exception e) {

            }
        }
        if (tmpbufField != null) {// for openjdk6
            try {
                byte[] buffer = (byte[]) tmpbufField.get(this);
                int flagLow8 = buffer[6];// 6 is for LOCFLG
                flagLow8 = (flagLow8 >> 1) << 1;
                buffer[6] = (byte) flagLow8;
            } catch (Exception e) {

            }
        }

        return super.createZipEntry(name);
    }
}
