package com.googlecode.d2j.tools.jar;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.googlecode.d2j.util.AccUtils.*;
import static org.objectweb.asm.Opcodes.*;

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
    private boolean initEnumNames = false;
    private boolean initSourceNames = false;
    private boolean initAssertionNames = false;

    public InitOut initEnumNames() {
        this.initEnumNames = true;
        return this;
    }

    public InitOut initSourceNames() {
        this.initSourceNames = true;
        return this;
    }

    public InitOut initAssertionNames() {
        this.initAssertionNames = true;
        return this;
    }

    private void doClass0(String clz) {
        if (clzSet.contains(clz)) {
            return;
        }
        clzSet.add(clz);

        int index = clz.lastIndexOf('$');
        if (index > 0) {
            doClass0(clz.substring(0, index));
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

    private void doMember(String owner, MemberInfo member, final int x) {
        // TODO use suggested name
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

    private List<ClassInfo> collect(File file) throws IOException {
        final List<ClassInfo> clzList = new ArrayList<ClassInfo>();
        final ClassVisitor collectVisitor = new ClassVisitor(ASM4) {
            private static final String ASSERTION_DISABLED_FIELD_NAME = "$assertionsDisabled";
            private static final String ENUM_VALUES_FIELD_NAME = "ENUM$VALUES";
            ClassInfo clz;
            boolean isEnum = false;
            Map<String, String> enumFieldMap = new HashMap<String, String>();

            @Override
            public void visitSource(String source, String debug) {
                if (initSourceNames && !clz.name.contains("$") && source.endsWith(".java")) {
                    clz.suggestName = source.substring(0, source.length() - 5);
                }
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                clz.addMethod(access, name, desc);
                if (initEnumNames && isEnum && name.equals("<clinit>")) {
                    final String thisDesc = "L" + clz.name + ";";
                    return new MethodNode(ASM4, access, name, desc, signature, exceptions) {
                        @Override
                        public void visitEnd() {
                            if (this.instructions != null) {
                                int status = 0;
                                String eFieldName = null;
                                for (AbstractInsnNode p = this.instructions.getFirst(); p != null; p = p.getNext()) {
                                    //looking for  NEW,DUP,LDC,PUTSTATUS

                                    if (status == 0) {  // init
                                        if (p.getOpcode() == NEW) {
                                            TypeInsnNode ti = (TypeInsnNode) p;
                                            if (thisDesc.equals(ti.desc)) {
                                                status = 1;
                                            }
                                        }
                                    } else if (status == 1) {  // find NEW
                                        if (p.getOpcode() == DUP) {
                                            status = 2;
                                        } else {
                                            status = 0;
                                        }
                                    } else if (status == 2) {  //find DUP
                                        if (p.getOpcode() == LDC) {
                                            LdcInsnNode ldc = (LdcInsnNode) p;
                                            if (ldc.cst instanceof String) {
                                                eFieldName = (String) ldc.cst;
                                                status = 3;
                                            } else {
                                                status = 0;
                                            }
                                        } else {
                                            status = 0;
                                        }
                                    } else if (status == 3) {  //find LDC
                                        if (p.getOpcode() == PUTSTATIC) {
                                            FieldInsnNode fin = (FieldInsnNode) p;
                                            if (fin.owner.equals(thisDesc) && fin.desc.equals(thisDesc)) {
                                                if (!fin.name.equals(eFieldName)) {
                                                    enumFieldMap.put(fin.name, eFieldName);
                                                }
                                                eFieldName = null;
                                                status = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    };
                }
                return null;
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                MemberInfo mi = clz.addField(access, name, desc);
                if (initEnumNames && isEnum
                        && isPrivate(access) && isFinal(access) && isSynthetic(access)
                        && !ENUM_VALUES_FIELD_NAME.equals(name)
                        && ("[L" + clz.name + ";").equals(desc)) {
                    mi.suggestName = ENUM_VALUES_FIELD_NAME;
                }
                if (initAssertionNames && isSynthetic(access) && isStatic(access)
                        && !isPrivate(access) && !isPublic(access) && !isProtected(access)
                        && desc.equals("Z") && !ASSERTION_DISABLED_FIELD_NAME.equals(name)) {
                    mi.suggestName = ASSERTION_DISABLED_FIELD_NAME;
                }

                return null;
            }

            @Override
            public void visitEnd() {
                if (initEnumNames) {
                    final String thisDesc = "L" + clz.name + ";";
                    for (Map.Entry<String, String> e : enumFieldMap.entrySet()) {
                        String name = e.getKey();
                        String suggestName = e.getValue();
                        for (MemberInfo mi : clz.fields) {
                            if (isFinal(mi.access) && isStatic(mi.access)
                                    && mi.name.equals(name) && mi.desc.equals(thisDesc)) {
                                mi.suggestName = suggestName;
                            }
                        }
                    }
                }
                clzList.add(clz);
                clz = null;
                isEnum = false;
                enumFieldMap.clear();
            }

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                this.clz = new ClassInfo(name);
                isEnum = isEnum(access);
            }
        };
//        FileWalker fileWalker = new FileWalker().withStreamHandler(new FileWalker.StreamHandler() {
//
//            @Override
//            public void handle(boolean isDir, String name, FileWalker.StreamOpener current, Object nameObject) throws IOException {
//                if ((!isDir) && name.endsWith(".class")) {
//                    new ClassReader(current.get()).accept(collectVisitor, ClassReader.EXPAND_FRAMES);
//                }
//            }
//        });
//        fileWalker.walk(file);
        return clzList;
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

    public void to(Path config) throws IOException {
        List<ClassInfo> classInfoList = collect(from);
        transform(classInfoList);
        List<String> list = new ArrayList<String>();
        list.addAll(pkgMap);
        list.addAll(clzMap);
        list.addAll(memberMap);
        Files.write(config,list, StandardCharsets.UTF_8);
    }

    private void transformerMember(String owner, List<MemberInfo> members) {
        Iterator<MemberInfo> it = members.iterator();
        if (it.hasNext()) {
            MemberInfo current = it.next();
            while (true) {
                if (it.hasNext()) {
                    MemberInfo next = it.next();
                    if (current.name.equals(next.name)) {
                        int x = 1;
                        doMember(owner, current, x++);
                        doMember(owner, next, x++);
                        while (it.hasNext()) {
                            next = it.next();
                            if (current.name.equals(next.name)) {
                                doMember(owner, next, x++);
                            } else {
                                current = next;
                                break;
                            }
                        }
                    } else {
                        doMember(owner, current, 0);
                        current = next;
                    }
                } else {
                    doMember(owner, current, 0);
                    break;
                }
            }
        }
    }

    private void transform(List<ClassInfo> classInfoList) {
        MemberInfoComparator comparator = new MemberInfoComparator();
        for (ClassInfo ci : classInfoList) {
            doClass0(ci.name);
            Collections.sort(ci.fields, comparator);
            transformerMember(ci.name, ci.fields);
            Collections.sort(ci.methods, comparator);
            transformerMember(ci.name, ci.methods);
        }

    }

    static private class ClassInfo {

        final public String name;
        public List<MemberInfo> fields = new ArrayList<MemberInfo>(5);
        public List<MemberInfo> methods = new ArrayList<MemberInfo>(5);
        public String suggestName;

        public ClassInfo(String name) {
            this.name = name;
        }

        public MemberInfo addField(int access, String name, String type) {
            MemberInfo mi = new MemberInfo(access, name, type);
            fields.add(mi);
            return mi;
        }

        public MemberInfo addMethod(int access, String name, String desc) {
            MemberInfo mi = new MemberInfo(access, name, desc);
            methods.add(mi);
            return mi;
        }

        public String toString() {
            return name;
        }
    }

    private static class MemberInfo {
        public int access;
        public String desc;
        public String name;
        public String suggestName;

        public MemberInfo(int access, String name, String desc) {
            this.name = name;
            this.access = access;
            this.desc = desc;
        }
    }

    private static class MemberInfoComparator implements Comparator<MemberInfo> {
        @Override
        public int compare(MemberInfo memberInfo, MemberInfo memberInfo2) {
            int x = memberInfo.name.compareTo(memberInfo2.name);
            if (x != 0) {
                return x;
            }
            return memberInfo.desc.compareTo(memberInfo2.desc);
        }
    }
}
