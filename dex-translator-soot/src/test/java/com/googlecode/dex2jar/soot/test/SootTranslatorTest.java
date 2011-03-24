/*
 *  dex2jar - A tool for converting Android's .dex format to Java's .class format
 *  Copyright (c) 2009-2011 Panxiaobo
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package com.googlecode.dex2jar.soot.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.ExceptionUtils;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.soot.Main;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public class SootTranslatorTest {
    static final Logger log = LoggerFactory.getLogger(SootTranslatorTest.class);

    @Test
    public void test() throws IOException {
        File file = new File("target/test-classes/dexes");
        boolean fail = false;
        DexFileReader.ContinueOnException = true;
        if (file.exists() && file.isDirectory()) {
            for (File f : FileUtils.listFiles(file, new String[] { "dex", "zip" }, false)) {
                log.info("dex2jar file {}", f);
                Main.doFile(f);
            }
        }
        Assert.assertFalse(fail);
    }
}
