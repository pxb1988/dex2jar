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
            private String fixName(String name) {
                if (name != null) {
                    return name.replace('-', '_');
                }
                return null;
            }

            private String[] fixNames(String[] names) {
                if (names != null) {
                    String[] ret = new String[names.length];
                    for (int i = 0; i < names.length; ++i) {
                        ret[i] = fixName(names[i]);
                    }
                    return ret;
                }
                return null;
            }

            @Override
            public String mapType(String type) {
                return super.mapType(fixName(type));
            }

            @Override
            public String mapInnerClassName(String name, String ownerName, String innerName) {
                return super.mapInnerClassName(fixName(name), fixName(ownerName), fixName(innerName));
            }

            @Override
            public String mapAnnotationAttributeName(String descriptor, String name) {
                return super.mapAnnotationAttributeName(fixName(descriptor), fixName(name));
            }

            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                return super.mapFieldName(fixName(owner), fixName(name), descriptor);
            }

            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                return super.mapMethodName(fixName(owner), fixName(name), descriptor);
            }

            @Override
            public String mapInvokeDynamicMethodName(String name, String descriptor) {
                return super.mapInvokeDynamicMethodName(fixName(name), descriptor);
            }

            @Override
            public String mapModuleName(String name) {
                return super.mapModuleName(fixName(name));
            }

            @Override
            public String mapPackageName(String name) {
                return super.mapPackageName(fixName(name));
            }

            @Override
            public String mapRecordComponentName(String owner, String name, String descriptor) {
                return super.mapRecordComponentName(fixName(owner), fixName(name), descriptor);
            }

            @Override
            public String[] mapTypes(String[] internalNames) {
                return super.mapTypes(fixNames(internalNames));
            }

            @Override
            public String map(String internalName) {
                return super.map(fixName(internalName));
            }
        });
    }

}
