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
package com.googlecode.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Panxiaobo [pxb1988 at gmail.com]
 * 
 */
public class GotoEndTransformer implements MethodTransformer, Opcodes {

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.optimize.MethodTransformer#transform(org.objectweb.asm.tree.MethodNode)
     */
    @Override
    public void transform(MethodNode method) {
        InsnList il = method.instructions;
        for (AbstractInsnNode p = il.getFirst(); p != null; p = p.getNext()) {
            if (p.getOpcode() == Opcodes.GOTO) {
                LabelNode ln = ((JumpInsnNode) p).label;
                AbstractInsnNode q = ln.getNext();
                if (q != null && Util.isEnd(q)) {
                    AbstractInsnNode np = q.clone(null);
                    il.insert(p, np);
                    il.remove(p);
                    p = np;
                }
            } else if (p.getOpcode() >= 153 && p.getOpcode() <= 166) {
                AbstractInsnNode q = p.getNext();
                if (!Util.isEnd(q)) {
                    LabelNode ln = ((JumpInsnNode) p).label;
                    q = ln.getNext();
                    if (q != null && Util.isEnd(q)) {

                        LabelNode labelNode = new LabelNode();
                        il.insert(p, labelNode);
                        AbstractInsnNode np = q.clone(null);
                        il.insert(p, np);
                        JumpInsnNode nj = not((JumpInsnNode) p, labelNode);
                        il.insert(p, nj);
                        il.remove(p);
                        p = labelNode;
                    }
                }
            }
        }
    }

    /**
     * @param p
     * @param labelNode
     * @return
     */
    private JumpInsnNode not(JumpInsnNode p, LabelNode labelNode) {
        int nop = 0;
        switch (p.getOpcode()) {
        case IFEQ:
            nop = IFNE;
            break;
        case IFNE:
            nop = IFEQ;
            break;
        case IFLT:
            nop = IFGE;
            break;
        case IFGE:
            nop = IFLT;
            break;
        case IFGT:
            nop = IFLE;
            break;
        case IFLE:
            nop = IFGT;
            break;
        case IF_ICMPEQ:
            nop = IF_ICMPNE;
            break;
        case IF_ICMPNE:
            nop = IF_ICMPEQ;
            break;
        case IF_ICMPLT:
            nop = IF_ICMPGE;
            break;
        case IF_ICMPGE:
            nop = IF_ICMPLT;
            break;
        case IF_ICMPGT:
            nop = IF_ICMPLE;
            break;
        case IF_ICMPLE:
            nop = IF_ICMPGT;
            break;
        case IF_ACMPEQ:
            nop = IF_ACMPNE;
            break;
        case IF_ACMPNE:
            nop = IF_ACMPEQ;
            break;
        }
        return new JumpInsnNode(nop, labelNode);
    }
}
