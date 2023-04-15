package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.DexType;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.E0Expr;
import java.lang.reflect.Array;

/**
 * Represent a constant, number/string/type
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Constant extends E0Expr {

    public static final Object NULL = new Object();

    public Object value;

    public Constant(Object value) {
        super(VT.CONSTANT);
        this.value = value;
    }

    @Override
    public Value clone() {
        return new Constant(value);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new Constant(value);
    }

    @Override
    public String toString0() {
        if (NULL == value) {
            return "null";
        } else if (value == null) {
            return "NULL";
        } else if (value instanceof Number) {
            if (value instanceof Float) {
                return value + "F";
            }
            if (value instanceof Long) {
                return value + "L";
            }
            return value.toString();
        } else if (value instanceof String) {
            StringBuffer buf = new StringBuffer();
            Util.appendString(buf, (String) value);
            return buf.toString();
        } else if (value instanceof DexType) {
            return Util.toShortClassName(((DexType) value).desc) + ".class";
        } else if (value.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            int size = Array.getLength(value);
            for (int i = 0; i < size; i++) {
                sb.append(Array.get(value, i)).append(",");
            }
            if (size > 0) {
                sb.setLength(sb.length() - 1);
            }
            sb.append("]");
            return sb.toString();
        }
        return String.valueOf(value);
    }

}
