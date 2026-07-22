package com.googlecode.d2j.dex;

import com.googlecode.dex2jar.tools.Constants;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class LambadaNameSafeClassAdapter extends ClassRemapper {

    private final boolean dontSanitizeNames;

    public String getClassName() {
        return dontSanitizeNames ? className : remapper.mapType(className);
    }

    private static String fixName(String name) {
        if (name == null) return null;
        return name.replace('-', '_');
    }

    private static String[] fixNames(String[] names) {
        if (names == null) return null;
        String[] ret = new String[names.length];
        for (int i = 0; i < names.length; ++i)
            ret[i] = fixName(names[i]);
        return ret;
    }

    public LambadaNameSafeClassAdapter(ClassVisitor cv, boolean dontSanitizeNames) {
        super(Constants.ASM_VERSION, cv, dontSanitizeNames ? new Remapper(Constants.ASM_VERSION) {
        } : new Remapper(Constants.ASM_VERSION) {
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
            public String mapInvokeDynamicMethodName(String name, String descriptor, Handle bootstrapMethodHandle,
                                                     Object... bootstrapMethodArguments) {
                return super.mapInvokeDynamicMethodName(fixName(name), fixName(descriptor), bootstrapMethodHandle,
                        bootstrapMethodArguments);
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

        this.dontSanitizeNames = dontSanitizeNames;
    }

}
