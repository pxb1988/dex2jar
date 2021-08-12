package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value.E0Expr;

/**
 * TODO DOC
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Local extends E0Expr {

    public int lsIndex;

    public String signature;

    public String debugName;

    public Local(String debugName) {
        super(Value.VT.LOCAL);
        this.debugName = debugName;
    }

    public Local(int index, String debugName) {
        super(Value.VT.LOCAL);
        this.debugName = debugName;
        this.lsIndex = index;
    }

    public Local() {
        super(Value.VT.LOCAL);
    }

    public Local(int index) {
        super(Value.VT.LOCAL);
        this.lsIndex = index;
    }

    @Override
    public Value clone() {
        Local clone = new Local(lsIndex);
        clone.debugName = debugName;
        clone.signature = this.signature;
        clone.valueType = this.valueType;
        return clone;
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return mapper.map(this);
    }

    @Override
    public String toString0() {
        if (debugName == null) {
            return "a" + lsIndex;
        } else {
            return debugName + "_" + lsIndex;
        }
    }

}
