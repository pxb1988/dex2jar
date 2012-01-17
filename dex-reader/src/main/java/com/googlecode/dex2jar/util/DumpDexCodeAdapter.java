/*
 * Copyright (c) 2009-2012 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DumpDexCodeAdapter extends AbstractDumpDexCodeAdapter {
    private static class TryCatch {
        public DexLabel end;

        public DexLabel handler;
        public DexLabel start;
        public String type;

        public TryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
            super();
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.type = type;
        }
    }

    private List<DexLabel> labels = new ArrayList<DexLabel>();

    private Method method;

    private PrintWriter out;

    private List<TryCatch> trys = new ArrayList<TryCatch>();

    private Map<DexLabel, Integer> lines = new HashMap<DexLabel, Integer>();

    private boolean isStatic;

    /**
     * @param dcv
     */
    public DumpDexCodeAdapter(boolean isStatic, Method m, PrintWriter out) {
        this.method = m;
        this.out = out;
        this.isStatic = isStatic;
    }

    @Override
    protected void info(int opcode, String format, Object... args) {
        String s = String.format(format, args);
        if (opcode < 0) {
            out.printf("%-20s|%5s|%s\n", "", "", s);
        } else {
            out.printf("%-20s|%5s|%s\n", DexOpcodeDump.dump(opcode), "", s);
        }
    }

    @Override
    protected String labelToString(DexLabel label) {
        int i = labels.indexOf(label);
        if (i > -1) {
            return "L" + i;
        }
        labels.add(label);
        return "L" + labels.indexOf(label);
    }

    @Override
    public void visitArguments(int total, int[] args) {
        int i = 0;
        if (!this.isStatic) {
            int reg = args[i++];
            String type = Dump.toJavaClass(method.getOwner());
            out.printf("%20s:v%d   //%s\n", "this", reg, type);
        }
        for (String type : method.getParameterTypes()) {
            int reg = args[i++];
            type = Dump.toJavaClass(type);
            out.printf("%20s:v%d   //%s\n", "", reg, type);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitLabel(int)
     */
    @Override
    public void visitLabel(DexLabel label) {
        boolean find = false;
        for (TryCatch tc : trys) {
            if (label.equals(tc.end)) {
                info(-1, " } // TC_%d", trys.indexOf(tc));
                find = true;
                break;
            }

        }
        Integer line = lines.get(label);
        if (line != null) {
            out.printf("%-20s|%5s: line %d\n", "LABEL", "L" + labelToString(label), line);
        } else {
            out.printf("%-20s|%5s:\n", "LABEL", "L" + labelToString(label));
        }
        if (!find) {
            for (TryCatch tc : trys) {
                if (label.equals(tc.start)) {
                    info(-1, "try { // TC_%d ", trys.indexOf(tc));
                    break;
                }
                if (label.equals(tc.handler)) {
                    String t = tc.type;
                    info(-1, "catch(%s) // TC_%d", t == null ? "all" : t, trys.indexOf(tc));
                    break;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitTryCatch(int, int, int, java.lang.String)
     */
    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
        TryCatch tc = new TryCatch(start, end, handler, type);
        trys.add(tc);
        int id = trys.indexOf(tc);
        if (type == null) {
            out.printf("TR_%d L%s ~ L%s > L%s all\n", id, labelToString(start), labelToString(end), labelToString(handler));
        } else {
            out.printf("TR_%d L%s ~ L%s > L%s %s\n", id, labelToString(start), labelToString(end), labelToString(handler), type);
        }
        super.visitTryCatch(start, end, handler, type);
    }

    @Override
    public void visitLineNumber(int line, DexLabel label) {
        lines.put(label, line);
    }

    @Override
    public void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg) {
        out.printf("LOCAL_VARIABLE L%s ~ L%s v%d -> %s // %s \n", labelToString(start), labelToString(end), reg, name, type);
    }

}
