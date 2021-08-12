package com.googlecode.d2j.dex.writer.io;

public interface DataOut {

    void begin(String s);

    void bytes(String s, byte[] bs);

    void bytes(String string, byte[] buf, int offset, int size);

    void end();

    int offset();

    void sbyte(String s, int b);

    void sint(String s, int i);

    void skip(String s, int n);

    void skip4(String s);

    void sleb128(String s, int i);

    void sshort(String s, int i);

    void ubyte(String s, int b);

    void uint(String s, int i);

    void uleb128(String s, int i);

    void uleb128p1(String s, int i);

    void ushort(String s, int i);

}
