package com.googlecode.d2j;

/**
 * a light weight version of org.objectweb.asm.Type
 *
 * @author Panxiaobo
 * @version $Rev$
 */
public class DexType {

    public DexType(String desc) {
        this.desc = desc;
    }

    /**
     * type descriptor, in TypeDescriptor format
     */
    public final String desc;

    @Override
    public String toString() {
        return desc;
    }

}
