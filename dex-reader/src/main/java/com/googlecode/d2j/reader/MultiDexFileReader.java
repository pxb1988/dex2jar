package com.googlecode.d2j.reader;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.util.zip.AccessBufByteArrayOutputStream;
import com.googlecode.d2j.util.zip.ZipEntry;
import com.googlecode.d2j.util.zip.ZipFile;
import com.googlecode.d2j.visitors.DexFileVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class MultiDexFileReader implements BaseDexFileReader {

    private final List<DexFileReader> readers = new ArrayList<>();

    private final List<Item> items = new ArrayList<>();

    public MultiDexFileReader(Collection<DexFileReader> readers) {
        this.readers.addAll(readers);
        init();
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        AccessBufByteArrayOutputStream out = new AccessBufByteArrayOutputStream();
        byte[] buff = new byte[1024];
        for (int c = is.read(buff); c > 0; c = is.read(buff)) {
            out.write(buff, 0, c);
        }
        return out.getBuf();
    }

    public static BaseDexFileReader open(InputStream in) throws IOException {
        return open(toByteArray(in));
    }

    public static BaseDexFileReader open(byte[] data) throws IOException {
        if (data.length < 3) {
            throw new IOException("File too small to be a dex/zip");
        }
        if ("dex".equals(new String(data, 0, 3, StandardCharsets.ISO_8859_1))) { // dex
            return new DexFileReader(data);
        } else if ("PK".equals(new String(data, 0, 2, StandardCharsets.ISO_8859_1))) { // ZIP
            TreeMap<String, DexFileReader> dexFileReaders = new TreeMap<>();
            try (ZipFile zipFile = new ZipFile(data)) {
                for (ZipEntry e : zipFile.entries()) {
                    String entryName = e.getName();
                    if (entryName.startsWith("classes") && entryName.endsWith(".dex")) {
                        if (!dexFileReaders.containsKey(entryName)) { // only the first one
                            dexFileReaders.put(entryName, new DexFileReader(toByteArray(zipFile.getInputStream(e))));
                        }
                    }
                }
            }
            if (dexFileReaders.size() == 0) {
                throw new IOException("Can not find classes.dex in zip file");
            } else if (dexFileReaders.size() == 1) {
                return dexFileReaders.firstEntry().getValue();
            } else {
                return new MultiDexFileReader(dexFileReaders.values());
            }
        }
        throw new IOException("The source file is not a .dex or .zip file");
    }

    void init() {
        Set<String> classes = new HashSet<>();
        for (DexFileReader reader : readers) {
            List<String> classNames = reader.getClassNames();
            for (int i = 0; i < classNames.size(); i++) {
                String className = classNames.get(i);
                if (classes.add(className)) {
                    items.add(new Item(i, reader, className));
                }
            }
        }
    }

    @Override
    public int getDexVersion() {
        return readers.stream().mapToInt(DexFileReader::getDexVersion)
                .max().orElse(DexConstants.DEX_035);
    }

    @Override
    public void accept(DexFileVisitor dv) {
        accept(dv, 0);
    }

    @Override
    public List<String> getClassNames() {
        return new AbstractList<String>() {
            @Override
            public String get(int index) {
                return items.get(index).className;
            }

            @Override
            public int size() {
                return items.size();
            }
        };
    }

    @Override
    public void accept(DexFileVisitor dv, int config) {
        int size = items.size();
        for (int i = 0; i < size; i++) {
            accept(dv, i, config);
        }
    }

    @Override
    public void accept(DexFileVisitor dv, int classIdx, int config) {
        Item item = items.get(classIdx);
        item.reader.accept(dv, item.idx, config);
    }

    static class Item {

        int idx;

        DexFileReader reader;

        String className;

        Item(int i, DexFileReader reader, String className) {
            idx = i;
            this.reader = reader;
            this.className = className;
        }

    }

}
