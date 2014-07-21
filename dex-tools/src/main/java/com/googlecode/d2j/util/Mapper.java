package com.googlecode.d2j.util;

public interface Mapper {

    String mapClassName(String oldDesc);

    String mapFieldName(String owner, String name, String desc);

    /**
     * @param owner
     * @param name
     * @param args  null for annotation element
     * @param ret   null for annotation element
     * @return
     */
    String mapMethodName(String owner, String name, String[] args, String ret);

    String mapFieldOwner(String owner, String name, String type);

    String mapMethodOwner(String owner, String name, String[] parameterTypes, String returnType);
}
