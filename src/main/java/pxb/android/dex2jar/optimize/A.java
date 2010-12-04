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
package pxb.android.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class A implements MethodTransformer, Opcodes {

	/**
	 * 
	 * <pre>
	 * L2 ~ L3 > L4 Ljava/io/UnsupportedEncodingException;
	 * LABEL               |   L2:
	 * NEW_INSTANCE        |     |v4=NEW Ljava/lang/String;
	 * CONST_4             |     |v6=0  //
	 * CONST_STRING        |     |v7="8859_1"
	 * INVOKE_DIRECT       |     |v4.&lt;init>(v5,v6,v8,v7)  //Ljava/lang/String;.<init>([BIILjava/lang/String;)V
	 * INVOKE_STATIC       |     |TEMP=javax.servlet.http.HttpUtils.parseQueryString(v4)  //Ljavax/servlet/http/HttpUtils;.parseQueryString(Ljava/lang/String;)Ljava/util/Hashtable;
	 * LABEL               |   L3:
	 * MOVE_RESULT_OBJECT  |     |v6=TEMP
	 * GOTO                |     |goto L9
	 * LABEL               |   L4:
	 * MOVE_EXCEPTION      |     |v6=TEMP
	 * MOVE_OBJECT         |     |v0 = v6
	 * NEW_INSTANCE        |     |v6=NEW Ljava/lang/IllegalArgumentException;
	 * INVOKE_VIRTUAL      |     |TEMP=v0.getMessage()  //Ljava/io/UnsupportedEncodingException;.getMessage()Ljava/lang/String;
	 * MOVE_RESULT_OBJECT  |     |v7=TEMP
	 * INVOKE_DIRECT       |     |v6.&lt;init>(v7)  //Ljava/lang/IllegalArgumentException;.<init>(Ljava/lang/String;)V
	 * THROW               |     |throw v6
	 * </pre>
	 * 
	 * 转换为
	 * 
	 * <pre>
	 * L2 ~ L3 > L4 Ljava/io/UnsupportedEncodingException;
	 * LABEL               |   L2:
	 * NEW_INSTANCE        |     |v4=NEW Ljava/lang/String;
	 * CONST_4             |     |v6=0  //
	 * CONST_STRING        |     |v7="8859_1"
	 * INVOKE_DIRECT       |     |v4.&lt;init>(v5,v6,v8,v7)  //Ljava/lang/String;.<init>([BIILjava/lang/String;)V
	 * INVOKE_STATIC       |     |TEMP=javax.servlet.http.HttpUtils.parseQueryString(v4)  //Ljavax/servlet/http/HttpUtils;.parseQueryString(Ljava/lang/String;)Ljava/util/Hashtable;
	 * MOVE_RESULT_OBJECT  |     |v6=TEMP
	 * LABEL               |   L3:
	 * GOTO                |     |goto L9
	 * LABEL               |   L4:
	 * MOVE_EXCEPTION      |     |v6=TEMP
	 * MOVE_OBJECT         |     |v0 = v6
	 * NEW_INSTANCE        |     |v6=NEW Ljava/lang/IllegalArgumentException;
	 * INVOKE_VIRTUAL      |     |TEMP=v0.getMessage()  //Ljava/io/UnsupportedEncodingException;.getMessage()Ljava/lang/String;
	 * MOVE_RESULT_OBJECT  |     |v7=TEMP
	 * INVOKE_DIRECT       |     |v6.&lt;init>(v7)  //Ljava/lang/IllegalArgumentException;.<init>(Ljava/lang/String;)V
	 * THROW               |     |throw v6
	 * </pre>
	 */
	public void transform(MethodNode method) {
		for (Object o : method.tryCatchBlocks) {
			TryCatchBlockNode tcb = (TryCatchBlockNode) o;
			AbstractInsnNode end = tcb.end;
			AbstractInsnNode pop = end.getPrevious();
			if (pop != null && (pop.getOpcode() == POP || pop.getOpcode() == POP2)) {
				AbstractInsnNode write = end.getNext();
				if (write != null && Util.isWrite(write)) {
					method.instructions.remove(pop);
					method.instructions.remove(write);
					method.instructions.insertBefore(end, write);
				}
			}
		}
	}
}
