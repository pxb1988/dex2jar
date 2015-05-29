package com.googlecode.d2j.reader;

import com.googlecode.d2j.visitors.DexFileVisitor;

import java.util.List;

public interface BaseDexFileReader {

    void accept(DexFileVisitor dv);

    List<String> getClassNames();

    void accept(DexFileVisitor dv, int config);

    void accept(DexFileVisitor dv, int classIdx, int config);
}
