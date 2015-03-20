package com.googlecode.dex2jar.tools;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.dex.writer.DexFileWriter;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.smali.Utils;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

@BaseCmd.Syntax(cmd = "d2j-dex-weaver", syntax = "[options] dex", desc = "replace invoke in dex", onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/DexWeaver")
public class DexWeaverCmd extends BaseCmd {
    @Opt(opt = "o", longOpt = "output", description = "output .dex file", argName = "out-dex-file")
    private Path output;
    @Opt(opt = "c", longOpt = "config", description = "config file", argName = "config")
    private Path config;
    @Opt(opt = "s", longOpt = "stub-dex", description = "stub dex", argName = "stub")
    private Path stub;

    static Method parseMethod(String str) {
        int i = str.indexOf('.');
        String owner = str.substring(0, i);
        int j = str.indexOf('(', i);
        String name = str.substring(i + 1, j);
        i = str.indexOf(')', j);
        String args = str.substring(j + 1, i);
        String ret = str.substring(i + 1);
        return new Method(owner, name, Utils.toTypeList(args), ret);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length == 0) {
            throw new HelpException("no odex");
        }

        final Map<String, Method> map = new HashMap<>();
        for (String ln : Files.readAllLines(config, StandardCharsets.UTF_8)) {
            if (ln.startsWith("#") || ln.length() == 0) {
                continue;
            }
            String[] x = ln.split("=");
            map.put(x[0], parseMethod(x[1]));
        }

        DexFileWriter out = new DexFileWriter();
        DexFileVisitor fv = new DexFileVisitor(out) {
            @Override
            public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
                DexClassVisitor dcv = super.visit(access_flags, className, superClass, interfaceNames);
                if (dcv != null) {
                    return new DexClassVisitor(dcv) {
                        @Override
                        public DexMethodVisitor visitMethod(int accessFlags, Method method) {
                            DexMethodVisitor dmv = super.visitMethod(accessFlags, method);
                            if (dmv != null) {
                                return new DexMethodVisitor(dmv) {
                                    @Override
                                    public DexCodeVisitor visitCode() {
                                        DexCodeVisitor code = super.visitCode();
                                        if (code != null) {
                                            return new DexCodeVisitor(code) {
                                                @Override
                                                public void visitMethodStmt(Op op, int[] args, Method method) {
                                                    Method replaceTo = map.get(method.toString());
                                                    if (replaceTo != null) {
                                                        switch (op) {
                                                        case INVOKE_DIRECT:
                                                        case INVOKE_INTERFACE:
                                                        case INVOKE_STATIC:
                                                        case INVOKE_SUPER:
                                                        case INVOKE_VIRTUAL:
                                                            super.visitMethodStmt(Op.INVOKE_STATIC, args, replaceTo);
                                                            break;
                                                        case INVOKE_DIRECT_RANGE:
                                                        case INVOKE_INTERFACE_RANGE:
                                                        case INVOKE_STATIC_RANGE:
                                                        case INVOKE_SUPER_RANGE:
                                                        case INVOKE_VIRTUAL_RANGE:
                                                            super.visitMethodStmt(Op.INVOKE_STATIC_RANGE, args, replaceTo);
                                                            break;
                                                        default:
                                                            // impossible here
                                                        }
                                                    } else {
                                                        super.visitMethodStmt(op, args, method);
                                                    }
                                                }
                                            };
                                        }
                                        return code;
                                    }
                                };
                            }
                            return dmv;
                        }
                    };
                }
                return dcv;
            }

            @Override
            public void visitEnd() {

            }
        };
        for (String f : remainingArgs) {
            byte[] data = ZipUtil.readDex(new File(f).toPath());
            DexFileReader r = new DexFileReader(data);
            r.accept(fv);
        }
        if (stub != null) {
            byte[] data = ZipUtil.readDex(stub);
            DexFileReader r = new DexFileReader(data);
            r.accept(new DexFileVisitor(out) {
                @Override
                public void visitEnd() {

                }
            });
        }

        out.visitEnd();

        byte[] data = out.toByteArray();
        Files.write(output, data);

    }

    public static void main(String[] args) {
        new DexWeaverCmd().doMain(args);
    }

}
