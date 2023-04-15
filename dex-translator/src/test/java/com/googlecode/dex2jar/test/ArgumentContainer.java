package com.googlecode.dex2jar.test;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import java.nio.file.Path;

public class ArgumentContainer {
    public final Path path;
    public final DexFileNode containingFile;
    public final DexClassNode cls;

    public ArgumentContainer(Path path, DexFileNode containingFile, DexClassNode cls) {
        this.path = path;
        this.cls = cls;
        this.containingFile = containingFile;
    }

    public boolean allowFailure() {
        return path.getFileName().toString().contains("mayfail");
    }

    @Override
    public String toString() {
        return path.getFileName() + " - " + cls.className;
    }
}
