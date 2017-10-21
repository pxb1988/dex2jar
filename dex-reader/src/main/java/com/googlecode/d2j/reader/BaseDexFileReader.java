package com.googlecode.d2j.reader;

import com.googlecode.d2j.visitors.DexFileVisitor;

import java.util.List;

public interface BaseDexFileReader {
    int DEX_035 = 0x00303335;
    int DEX_036 = 0x00303336;
    int DEX_037 = 0x00303337;
    int DEX_038 = 0x00303338;

    int getDexVersion();

    void accept(DexFileVisitor dv);

    List<String> getClassNames();

    void accept(DexFileVisitor dv, int config);

    void accept(DexFileVisitor dv, int classIdx, int config);
}
