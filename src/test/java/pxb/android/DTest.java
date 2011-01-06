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
package pxb.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

import pxb.android.dex2jar.optimize.D;
import res.SwitchRes;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public class DTest {
    private void dump(MethodNode methodNode) {
        TraceMethodVisitor tv = new TraceMethodVisitor();
        methodNode.accept(tv);
        int i = 0;
        for (Object o : tv.text) {
            System.out.print(String.format("%4d%s", i++, o));
        }
    }

    @Test
    public void testSwitch() throws IOException {
        InputStream is = DTest.class.getResourceAsStream('/' + SwitchRes.class.getName().replace('.', '/') + ".class");
        ClassNode cn = new ClassNode();
        new ClassReader(is).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        for (Iterator<?> it = cn.methods.iterator(); it.hasNext();) {
            MethodNode methodNode = (MethodNode) it.next();
            System.out.println("============BEFORE");
            dump(methodNode);
            new D().transform(methodNode);
            System.out.println("============AFTER");
            dump(methodNode);
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        byte[] data = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("target/" + SwitchRes.class.getName().replace('.', '/') + ".class"), data);
    }
}
