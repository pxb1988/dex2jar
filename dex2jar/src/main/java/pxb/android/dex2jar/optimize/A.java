package pxb.android.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class A extends MethodTransformerAdapter implements Opcodes {

	public A(MethodTransformer tr) {
		super(tr);
	}

	/**
	 * 
	 * <pre>
	 * L2 ~ L3 > L4 Ljava/io/UnsupportedEncodingException;
	 * LABEL               |   L2:
	 * NEW_INSTANCE        |     |v4=NEW Ljava/lang/String;
	 * CONST_4             |     |v6=0  //
	 * CONST_STRING        |     |v7="8859_1"
	 * INVOKE_DIRECT       |     |v4.<init>(v5,v6,v8,v7)  //Ljava/lang/String;.<init>([BIILjava/lang/String;)V
	 * INVOKE_STATIC       |     |XXX=javax.servlet.http.HttpUtils.parseQueryString(v4)  //Ljavax/servlet/http/HttpUtils;.parseQueryString(Ljava/lang/String;)Ljava/util/Hashtable;
	 * LABEL               |   L3:
	 * MOVE_RESULT_OBJECT  |     |v6=XXX
	 * GOTO                |     |goto L9
	 * LABEL               |   L4:
	 * MOVE_EXCEPTION      |     |v6=XXX
	 * MOVE_OBJECT         |     |v0 = v6
	 * NEW_INSTANCE        |     |v6=NEW Ljava/lang/IllegalArgumentException;
	 * INVOKE_VIRTUAL      |     |XXX=v0.getMessage()  //Ljava/io/UnsupportedEncodingException;.getMessage()Ljava/lang/String;
	 * MOVE_RESULT_OBJECT  |     |v7=XXX
	 * INVOKE_DIRECT       |     |v6.<init>(v7)  //Ljava/lang/IllegalArgumentException;.<init>(Ljava/lang/String;)V
	 * THROW               |     |throw v6
	 * </pre>
	 * 转换为
	 * <pre>
	 * L2 ~ L3 > L4 Ljava/io/UnsupportedEncodingException;
	 * LABEL               |   L2:
	 * NEW_INSTANCE        |     |v4=NEW Ljava/lang/String;
	 * CONST_4             |     |v6=0  //
	 * CONST_STRING        |     |v7="8859_1"
	 * INVOKE_DIRECT       |     |v4.<init>(v5,v6,v8,v7)  //Ljava/lang/String;.<init>([BIILjava/lang/String;)V
	 * INVOKE_STATIC       |     |XXX=javax.servlet.http.HttpUtils.parseQueryString(v4)  //Ljavax/servlet/http/HttpUtils;.parseQueryString(Ljava/lang/String;)Ljava/util/Hashtable;
	 * MOVE_RESULT_OBJECT  |     |v6=XXX
	 * LABEL               |   L3:
	 * GOTO                |     |goto L9
	 * LABEL               |   L4:
	 * MOVE_EXCEPTION      |     |v6=XXX
	 * MOVE_OBJECT         |     |v0 = v6
	 * NEW_INSTANCE        |     |v6=NEW Ljava/lang/IllegalArgumentException;
	 * INVOKE_VIRTUAL      |     |XXX=v0.getMessage()  //Ljava/io/UnsupportedEncodingException;.getMessage()Ljava/lang/String;
	 * MOVE_RESULT_OBJECT  |     |v7=XXX
	 * INVOKE_DIRECT       |     |v6.<init>(v7)  //Ljava/lang/IllegalArgumentException;.<init>(Ljava/lang/String;)V
	 * THROW               |     |throw v6
	 * </pre>
	 */
	@Override
	public void transform(MethodNode method) {
		for (Object o : method.tryCatchBlocks) {
			TryCatchBlockNode tcb = (TryCatchBlockNode) o;
			AbstractInsnNode end = tcb.end;
			AbstractInsnNode p = end.getNext();
			if (p != null && Util.isWrite(p)) {
				AbstractInsnNode q = p.getNext();
				if (q != null && q.getOpcode() == GOTO) {
					method.instructions.remove(p);
					method.instructions.insertBefore(end, p);
					AbstractInsnNode r = p.getPrevious();
					if (r.getOpcode() == POP) {
						method.instructions.remove(r);
					}
				}
			}
		}
		super.transform(method);
	}
}
