/*
 * Copyright (c) 2009-2010 Panxiaobo
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
package pxb.android;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.dump.Dump;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DumpTest {
    static final Logger log = LoggerFactory.getLogger(DumpTest.class);

    @Test
    public void test() throws IOException {
        File file = new File("target/test-classes/dexes");
        for (File f : FileUtils.listFiles(file, new String[] { "dex", "zip" }, false)) {
            log.info("dump file {}", f);
            Dump.doFile(f);
        }
    }
}
