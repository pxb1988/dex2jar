package p.rn.name;

import static p.rn.util.AccUtils.isPrivate;
import static p.rn.util.AccUtils.isPublic;
import static p.rn.util.AccUtils.isStatic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import p.rn.ClassInfo;
import p.rn.ClassInfo.MemberInfo;
import p.rn.Scann;

public class InitOut {
    private static Set<String> keywords = new HashSet<String>(Arrays.asList("abstract", "continue", "for", "new",
            "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this",
            "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws",
            "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char",
            "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const",
            "float", "native", "super", "while"));

    private int clzIndex = 0;
    private Set<String> clzMap = new TreeSet<String>();
    private Set<String> clzSet = new TreeSet<String>();
    private File from;

    private int maxLength = 40;
    private Set<String> memberMap = new TreeSet<String>();

    private int minLength = 2;

    private int pkgIndex = 0;

    private Set<String> pkgMap = new TreeSet<String>();
    private Set<String> pkgSet = new TreeSet<String>();

    private void doClass(String clz) {
        if (clzSet.contains(clz)) {
            return;
        }
        clzSet.add(clz);

        int index = clz.lastIndexOf('$');
        if (index > 0) {
            doClass(clz.substring(0, index));
            String cName = clz.substring(index + 1);
            try {
                Integer.parseInt(cName);
            } catch (Exception ex) {
                if (shouldRename(cName)) {
                    clzMap.add(String.format("c %s=CI%03d%s", clz, clzIndex++, short4LongName(cName)));
                }
            }
        } else {
            index = clz.lastIndexOf('/');
            if (index > 0) {
                doPkg(clz.substring(0, index));
                String cName = clz.substring(index + 1);
                if (shouldRename(cName)) {
                    clzMap.add(String.format("c %s=C%03d%s", clz, clzIndex++, short4LongName(cName)));
                }
            } else {
                if (shouldRename(clz)) {
                    clzMap.add(String.format("c %s=CI_%03d%s", clz, clzIndex++, short4LongName(clz)));
                }
            }
        }
    }

    private String short4LongName(String name) {
        if (name.length() > maxLength) {
            return "x" + Integer.toHexString(name.hashCode());
        } else {
            return name;
        }
    }

    private void doMethod(String owner, MemberInfo member, int x) {
        if (x > 0 || shouldRename(member.name)) {
            if (member.desc.indexOf('(') >= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(isStatic(member.access) ? "M" : "m");
                if (isPrivate(member.access)) {
                    sb.append("p");
                } else if (isPublic(member.access)) {
                    sb.append("P");
                }
                if (x > 0) {
                    sb.append(x);
                }
                sb.append(short4LongName(member.name));
                if (x > 0) {
                    memberMap.add("m " + owner + "." + member.name + member.desc + "=" + sb.toString());
                } else {
                    memberMap.add("m " + owner + "." + member.name
                            + member.desc.substring(0, member.desc.indexOf(')') + 1) + "=" + sb.toString());
                }
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(isStatic(member.access) ? "F" : "f");
                if (isPrivate(member.access)) {
                    sb.append("p");
                } else if (isPublic(member.access)) {
                    sb.append("P");
                }
                if (x > 0) {
                    sb.append(x);
                }
                sb.append(short4LongName(member.name));
                if (x > 0) {
                    memberMap.add("m " + owner + "." + member.name + "[" + member.desc + "]" + "=" + sb.toString());
                } else {
                    memberMap.add("m " + owner + "." + member.name + "=" + sb.toString());
                }
            }
        }
    }

    private void doOut() throws IOException {
        Map<String, ClassInfo> map = Scann.scanLib(from);
        for (ClassInfo info : map.values()) {
            doClass(info.name);
            for (List<MemberInfo> ms : info.members.values()) {
                if (ms.size() == 1) {
                    for (MemberInfo m : ms) {
                        doMethod(info.name, m, 0);
                    }
                } else {
                    int i = 1;
                    for (MemberInfo m : ms) {
                        doMethod(info.name, m, i++);
                    }
                }
            }
        }
    }

    private void doPkg(String pkg) {
        if (pkgSet.contains(pkg)) {
            return;
        }
        pkgSet.add(pkg);
        int index = pkg.lastIndexOf('/');
        if (index > 0) {
            doPkg(pkg.substring(0, index));
            String cName = pkg.substring(index + 1);
            if (shouldRename(cName)) {
                pkgMap.add(String.format("p %s=p%02d%s", pkg, pkgIndex++, short4LongName(cName)));
            }
        } else {
            if (shouldRename(pkg)) {
                pkgMap.add(String.format("p %s=p%02d%s", pkg, pkgIndex++, short4LongName(pkg)));
            }
        }
    }

    public InitOut from(File from) {
        this.from = from;
        return this;
    }

    public InitOut maxLength(int m) {
        this.maxLength = m;
        return this;
    }

    public InitOut minLength(int m) {
        this.minLength = m;
        return this;
    }

    private boolean shouldRename(String s) {
        return s.length() > maxLength || s.length() < minLength || keywords.contains(s);
    }

    public void to(File config) throws IOException {
        doOut();
        List<String> list = new ArrayList<String>();
        list.addAll(pkgMap);
        list.addAll(clzMap);
        list.addAll(memberMap);
        FileUtils.writeLines(config, "UTF-8", list);
    }

}
