/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.util.Dump;
import com.googlecode.dex2jar.v3.Main;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public class DumpTest {

    @Test
    public void test() throws Exception {
        try {
            for (File f : TestUtils.listTestDexFiles(true)) {
                System.out.println("dump file " + f);
                File distDir = new File(f.getParentFile(), FilenameUtils.getBaseName(f.getName()) + "_dump.jar");
                doData(DexFileReader.readDex(f), distDir);
            }
        } catch (Exception e) {
            Main.niceExceptionMessage(e, 0);
            throw e;
        }
    }

    public static void doData(byte[] data, final File destJar) throws IOException {
        Dump.doData(data, destJar);
    }

}
