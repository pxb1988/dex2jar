package com.googlecode.d2j.dex;


/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public final class V3 {

    public static final int REUSE_REGISTER = 1;

    public static final int TOPOLOGICAL_SORT = 1 << 1;

    public static final int PRINT_IR = 1 << 2;

    public static final int OPTIMIZE_SYNCHRONIZED = 1 << 3;

    private V3() {
        throw new UnsupportedOperationException();
    }

}
