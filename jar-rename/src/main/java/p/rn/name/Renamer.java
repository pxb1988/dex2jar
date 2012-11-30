package p.rn.name;

import static p.rn.util.AccUtils.isEnum;
import static p.rn.util.AccUtils.isFinal;
import static p.rn.util.AccUtils.isPrivate;
import static p.rn.util.AccUtils.isProtected;
import static p.rn.util.AccUtils.isPublic;
import static p.rn.util.AccUtils.isStatic;
import static p.rn.util.AccUtils.isSynthetic;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;

import p.rn.ClassInfo;
import p.rn.ClassInfo.MemberInfo;
import p.rn.Scann;
import p.rn.util.FileOut;
import p.rn.util.FileOut.OutHandler;
import p.rn.util.FileWalker;
import p.rn.util.FileWalker.StreamHandler;
import p.rn.util.FileWalker.StreamOpener;

public class Renamer {
    private static final Remapper EmptyRemapper = new SimpleRemapper(Collections.emptyMap());

    private static String doReplace(Collection<E> replace, String str) {
        for (E e : replace) {
            str = str.replaceAll(e.key, e.value);
        }
        return str;
    }

    private File from;

    private Remapper remapper;

    private File to;

    private void check() {
        if (from == null || to == null) {
            throw new RuntimeException("from and to must be set");
        }
        if (remapper == null) {
            remapper = EmptyRemapper;
        }
    }

    private void doRename() throws IOException {
        check();

        final Map<String, ClassInfo> clzMap = Scann.scanLib(from);
        final Map<String, String> _enum = new HashMap<String, String>();
        for (ClassInfo classInfo : clzMap.values()) {
            for (List<MemberInfo> fs : classInfo.members.values()) {
                for (MemberInfo f : fs) {
                    int access = f.access;
                    if (isEnum(classInfo.access)) {
                        if (isSynthetic(access) && isPrivate(access) && isStatic(access) && isFinal(access)
                                && f.desc.equals("[L" + classInfo.name + ";") && !"ENUM$VALUES".equals(f.name)) {
                            _enum.put(classInfo.name + "." + f.name, "ENUM$VALUES");
                        }
                    }
                    if (isSynthetic(access) && !isPrivate(access) && !isPublic(access) && !isProtected(access)
                            && isStatic(access) && f.desc.equals("Z") && !"$assertionsDisabled".equals(f.name)) {
                        _enum.put(classInfo.name + "." + f.name, "$assertionsDisabled");
                    }
                }
            }

        }

        final Remapper xRemapper = new Remapper() {

            private String findOwner(String clz, String name, String desc) {
                ClassInfo classInfo = clzMap.get(clz);
                int index = desc.indexOf(')');
                String key = index >= 0 ? name + desc.substring(0, index + 1) : name;
                if (classInfo != null) {
                    List<MemberInfo> members = classInfo.members.get(key);
                    if (members != null) {
                        for (MemberInfo m : members) {
                            if (m.name.equals(name) && m.desc.equals(desc) && isPrivate(m.access)) {
                                return clz;
                            }
                        }
                    }
                    for (String p : classInfo.parent) {
                        String oS = findOwnerNotPrivate(p, name, desc, key);
                        if (oS != null) {
                            return oS;
                        }
                    }
                }
                return null;
            }

            private String findOwnerNotPrivate(String clz, String name, String desc, String key) {
                ClassInfo classInfo = clzMap.get(clz);
                if (classInfo != null) {
                    for (String p : classInfo.parent) {
                        String oS = findOwnerNotPrivate(p, name, desc, key);
                        if (oS != null) {
                            return oS;
                        }
                    }
                    List<MemberInfo> members = classInfo.members.get(key);
                    if (members != null) {
                        for (MemberInfo m : members) {
                            if (m.name.equals(name) && m.desc.equals(desc) && !isPrivate(m.access)) {
                                return clz;
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            public String map(String typeName) {
                if (typeName.startsWith("java/") || typeName.startsWith("javax/"))
                    return typeName;

                if (typeName.charAt(0) == 'L' && typeName.charAt(typeName.length() - 1) == ';') {
                    return 'L' + map(typeName.substring(1, typeName.length() - 1)) + ';';
                }

                return remapper.map(typeName);
            }

            @Override
            public String mapFieldName(String owner, String name, String desc) {
                if (owner.startsWith("java/") || owner.startsWith("javax/"))
                    return name;
                String nName = _enum.get(owner + "." + name);
                if (nName != null)
                    return nName;
                String nOwner = findOwner(owner, name, desc);
                return remapper.mapFieldName(nOwner == null ? owner : nOwner, name, desc);
            }

            @Override
            public String mapMethodName(String owner, String name, String desc) {
                if (owner.startsWith("java/") || owner.startsWith("javax/") || name.startsWith("<"))
                    return name;
                String nOwner = findOwner(owner, name, desc);
                return remapper.mapMethodName(nOwner == null ? owner : nOwner, name, desc);
            }
        };

        final OutHandler zo = FileOut.create(to);

        new FileWalker().withStreamHandler(new StreamHandler() {

            @Override
            public void handle(boolean isDir, String name, StreamOpener current, Object nameObject) throws IOException {
                if (isDir) {
                    // ignore
                } else if (name.endsWith(".class")) {
                    ClassWriter cw = new ClassWriter(0);
                    new ClassReader(current.get()).accept(new RemappingClassAdapter(cw, xRemapper),
                            ClassReader.EXPAND_FRAMES);
                    name = name.substring(0, name.length() - ".class".length());
                    name = xRemapper.map(name);
                    zo.write(false, name + ".class", cw.toByteArray(), null);
                } else {
                    zo.write(false, name, current.get(), nameObject);
                }
            }
        }).walk(from);
        zo.close();
    }

    public Renamer from(File file) {
        this.from = file;
        return this;
    }

    public void to(File to) throws IOException {
        this.to = to;
        doRename();
    }

    private static class E implements Comparable<E> {
        public String key;
        public String value;

        public int compareTo(E arg0) {
            int i = arg0.key.length() - key.length();
            return i == 0 ? arg0.key.compareTo(key) : i;
        }
    }

    public Renamer withConfig(File config) throws IOException {

        final Map<String, String> clz = new HashMap<String, String>();
        final Map<String, String> pkg = new HashMap<String, String>();
        final Map<String, String> member = new HashMap<String, String>();
        Set<E> replace = new TreeSet<E>();
        if (config != null) {
            for (String ln : FileUtils.readLines(config, "UTF-8")) {
                if ("".equals(ln) || ln.startsWith("#")) {
                    continue;
                }
                int index = ln.lastIndexOf('=');
                if (index > 0) {
                    String key = ln.substring(2, index);
                    String value = ln.substring(index + 1);
                    switch (ln.charAt(0)) {
                    case 'm':
                    case 'f':
                    case 'M':
                    case 'F':
                        member.put(doReplace(replace, key), value);
                        break;
                    case 'c':
                    case 'C':
                        clz.put(doReplace(replace, key), value);
                        break;
                    case 'p':
                    case 'P':
                        pkg.put(doReplace(replace, key), value);
                        break;
                    case '@':
                        E e = new E();
                        e.key = '@' + key;
                        e.value = doReplace(replace, value);
                        replace.add(e);
                        break;
                    }
                }
            }
        }

        return withTransformer(new Transformer() {

            private StringBuilder doClass(String str) {
                int index = str.lastIndexOf('$');
                if (index > 0) {
                    String nName = clz.get(str);
                    if (nName == null) {
                        nName = str.substring(index + 1);
                    }
                    return doClass(str.substring(0, index)).append("$").append(nName);
                } else {
                    index = str.lastIndexOf('/');
                    String nName = clz.get(str);
                    if (index > 0) {
                        if (nName == null) {
                            nName = str.substring(index + 1);
                        }
                        return doPkg(str.substring(0, index)).append('/').append(nName);
                    } else {
                        if (nName == null) {
                            nName = str;
                        }
                        return new StringBuilder(nName);
                    }
                }
            }

            private Map<String, String> pkgCache = new HashMap<String, String>();

            private StringBuilder doPkg(String str) {
                String x = pkgCache.get(str);
                if (x != null)
                    return new StringBuilder(x);
                int index = str.lastIndexOf('/');
                String nName = pkg.get(str);
                StringBuilder sb;
                if (index > 0) {
                    if (nName == null) {
                        nName = str.substring(index + 1);
                    }
                    sb = doPkg(str.substring(0, index)).append('/').append(nName);
                } else {
                    if (nName == null) {
                        nName = str;
                    }
                    sb = new StringBuilder(nName);
                }
                pkgCache.put(str, sb.toString());
                return sb;
            }

            public Object transform(Object input) {
                String key = input.toString();
                int index = key.indexOf('.');
                if (index > 0) {// field/method
                    String desc = key.substring(index + 1);
                    String nName = member.get(key);
                    if (nName == null) {
                        index = desc.indexOf('(');
                        if (index > 0) {
                            nName = desc.substring(0, index);
                        } else {
                            index = desc.indexOf('[');
                            if (index > 0) {
                                nName = desc.substring(0, index);
                            } else {
                                nName = desc;
                            }
                        }
                    }
                    return nName;
                } else {// class
                    return doClass(key).toString();
                }
            }

        });
    }

    public Renamer withRemapper(Remapper remapper) {

        this.remapper = remapper;
        return this;
    }

    public Renamer withTransformer(Transformer tf) throws IOException {
        return withRemapper(new SimpleRemapper(LazyMap.decorate(new HashMap<String, String>(), tf)) {
            public String mapFieldName(String owner, String name, String desc) {
                String s = map(owner + '.' + name + "[" + desc + "]");
                if (s == null || name.equals(s)) {
                    s = map(owner + '.' + name);
                }
                return s == null ? name : s;
            }

            @Override
            public String mapMethodName(String owner, String name, String desc) {
                String s = map(owner + '.' + name + desc);
                if (s == null || name.equals(s)) {
                    s = map(owner + '.' + name + desc.substring(0, desc.indexOf(')') + 1));
                }
                return s == null ? name : s;
            }
        });
    }
}
