package com.googlecode.d2j.dex;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class LambadaNameSafeClassAdapter extends ClassRemapper {

    public String getClassName() {
        return remapper.mapType(className);
    }

    public LambadaNameSafeClassAdapter(ClassVisitor cv) {
        super(cv, new Remapper() {
            @Override
            public String mapType(String type) {
                if (type == null) {
                    return null;
                }
                return type.replace('-', '_');
            }
        });
    }

}
