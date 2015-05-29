package com.googlecode.d2j.util.zip;

import java.io.ByteArrayOutputStream;

public class AccessBufByteArrayOutputStream extends ByteArrayOutputStream {
    public byte[] getBuf() {
        return buf;
    }
}
