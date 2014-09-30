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
package com.googlecode.dex2jar.test;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.junit.Test;

import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.util.ASMifierFileV;
import com.googlecode.dex2jar.tools.BaseCmd;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
public class ASMifierTest {
    public static String getBaseName(String fn) {
        int x = fn.lastIndexOf('.');
        return x >= 0 ? fn.substring(0, x) : fn;
    }

    @Test
    public void test() throws Exception {
        try {
            for (Path f : TestUtils.listTestDexFiles()) {
                System.out.println("asmifier file " + f);
                File distDir = new File("target", getBaseName(f.getFileName().toString()) + "_asmifier.zip");
                try (FileSystem fs = BaseCmd.createZip(distDir.toPath())) {
                    ASMifierFileV.doFile(f, fs.getPath("/"));
                }
            }
        } catch (Exception e) {
            DexFileReader.niceExceptionMessage(e, 0);
            throw e;
        }
    }

}
