package pxb.android.dex2jar.optimize;

import org.objectweb.asm.tree.MethodNode;

public class MethodTransformerAdapter implements MethodTransformer {
	protected MethodTransformer tr;

	public MethodTransformerAdapter(MethodTransformer tr) {
		super();
		this.tr = tr;
	}

	@Override
	public void transform(MethodNode method) {
		if (tr != null) {
			tr.transform(method);
		}
	}

}
