package pxb.java.nio.charset;

import java.nio.charset.Charset;

public final class StandardCharsets {

    private StandardCharsets() {
        throw new UnsupportedOperationException();
    }

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static final Charset ISO_8859_1 = Charset.forName("iso-8859-1");

}
