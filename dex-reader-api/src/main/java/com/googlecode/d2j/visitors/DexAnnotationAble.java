package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Visibility;

/**
 * 用于访问注解
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public interface DexAnnotationAble {

    /**
     * 访问注解
     *
     * @param name       注解名
     * @param visibility 是否运行时可见
     */
    DexAnnotationVisitor visitAnnotation(String name, Visibility visibility);

}
