package res;

import java.nio.*;

public class Hex {
    public static long[] decode_J(String src) {
        byte[] d = decode_B(src);
        ByteBuffer b = ByteBuffer.wrap(d);
        b.order(ByteOrder.LITTLE_ENDIAN);
        LongBuffer s = b.asLongBuffer();
        long[] data = new long[d.length / 8];
        s.get(data);
        return data;
    }
    public static int[] decode_I(String src) {
        byte[] d = decode_B(src);
        ByteBuffer b = ByteBuffer.wrap(d);
        b.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer s = b.asIntBuffer();
        int[] data = new int[d.length / 4];
        s.get(data);
        return data;
    }

    public static short[] decode_S(String src) {
        byte[] d = decode_B(src);
        ByteBuffer b = ByteBuffer.wrap(d);
        b.order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer s = b.asShortBuffer();
        short[] data = new short[d.length / 2];
        s.get(data);
        return data;
    }

    public static byte[] decode_B(String src) {
        char[] d = src.toCharArray();
        byte[] ret = new byte[src.length() / 2];
        for (int i = 0; i < ret.length; i++) {
            char h = d[2 * i];
            char l = d[2 * i + 1];
            int hh = 0;
            if (h >= '0' && h <= '9') {
                hh = h - '0';
            } else if (h >= 'a' && h <= 'f') {
                hh = h - 'a' + 10;
            } else if (h >= 'A' && h <= 'F') {
                hh = h - 'A' + 10;
            } else {
                throw new RuntimeException();
            }
            int ll = 0;
            if (l >= '0' && l <= '9') {
                ll = h - '0';
            } else if (l >= 'a' && l <= 'f') {
                ll = h - 'a' + 10;
            } else if (l >= 'A' && l <= 'F') {
                ll = h - 'A' + 10;
            } else {
                throw new RuntimeException();
            }
            d[i] = (char) ((hh << 4) | ll);
        }
        return ret;
    }
}
