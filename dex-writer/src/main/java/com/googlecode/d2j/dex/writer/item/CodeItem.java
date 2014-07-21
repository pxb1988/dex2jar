/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ann.Off;
import com.googlecode.d2j.dex.writer.insn.Insn;
import com.googlecode.d2j.dex.writer.insn.Label;
import com.googlecode.d2j.dex.writer.insn.PreBuildInsn;
import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.CodeItem.EncodedCatchHandler.AddrPair;
import com.googlecode.d2j.reader.Op;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class CodeItem extends BaseItem {

    public int registersSize;
    public int insSize;
    public int outsSize;
    public int insn_size;
    public List<TryItem> tries;
    @Off
    public DebugInfoItem debugInfo;
    public List<Insn> insns;
    public List<EncodedCatchHandler> handlers;

    @Override
    public int place(int offset) {
        offset += 16 + insn_size * 2;
        if (tries != null && tries.size() > 0) {
            if ((insn_size & 0x01) != 0) {// padding
                offset += 2;
            }
            offset += 8 * tries.size();
            if (handlers.size() > 0) {
                int base = offset;
                offset += lengthOfUleb128(handlers.size());

                for (EncodedCatchHandler h : handlers) {
                    h.handler_off = offset - base;
                    int size = h.addPairs.size();
                    offset += lengthOfSleb128(h.catchAll != null ? -size : size);
                    for (AddrPair ap : h.addPairs) {
                        offset += lengthOfUleb128(ap.type.index) + lengthOfUleb128(ap.addr.offset);
                    }
                    if (h.catchAll != null) {
                        offset += lengthOfUleb128(h.catchAll.offset);
                    }
                }
            }

        }
        return offset;
    }

    @Override
    public void write(DataOut out) {
        out.ushort("registers_size", registersSize);
        out.ushort("ins_size", insSize);
        out.ushort("outs_size", outsSize);
        out.ushort("tries_size", tries == null ? 0 : tries.size());
        out.uint("debug_info_off", debugInfo == null ? 0 : debugInfo.offset);
        out.uint("insn_size", insn_size);
        ByteBuffer b = ByteBuffer.allocate(insn_size * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (Insn insn : insns) {
            insn.write(b);
        }
        out.bytes("insn", b.array());
        if (tries != null && tries.size() > 0) {
            if ((insn_size & 0x01) != 0) {// padding
                out.skip("padding", 2);
            }
            int lastEnd = 0;
            for (TryItem ti : tries) {
                if (ti.start.offset < lastEnd) {
                    System.err.println("'Out-of-order try' may throwed by libdex");
                }
                out.uint("start_addr", ti.start.offset);
                out.ushort("insn_count", ti.end.offset - ti.start.offset);
                lastEnd = ti.end.offset;
                out.ushort("handler_off", ti.handler.handler_off);
            }
            if (handlers.size() > 0) {
                out.uleb128("size", handlers.size());
                for (EncodedCatchHandler h : handlers) {

                    int size = h.addPairs.size();
                    out.sleb128("size", (h.catchAll != null ? -size : size));
                    for (AddrPair ap : h.addPairs) {
                        out.uleb128("type_idx", (ap.type.index));
                        out.uleb128("addr", (ap.addr.offset));
                    }
                    if (h.catchAll != null) {
                        out.uleb128("catch_all_addr", (h.catchAll.offset));
                    }
                }
            }
        }
    }

    public void prepareTries(List<TryItem> tryItems) {
        if (tryItems.size() > 0) {
            List<CodeItem.TryItem> uniqTrys = new ArrayList<>();
            { // merge dup trys
                Set<TryItem> set = new HashSet<>();
                for (CodeItem.TryItem tryItem : tryItems) {
                    if (!set.contains(tryItem)) {
                        uniqTrys.add(tryItem);
                        set.add(tryItem);
                    } else {
                        for (TryItem t : uniqTrys) {
                            if (t.equals(tryItem)) {
                                mergeExceptionHandler(t.handler, tryItem.handler);
                            }
                        }
                    }
                }
                set.clear();
                this.tries = uniqTrys;
                if (uniqTrys.size() > 0) {
                    Collections.sort(uniqTrys, new Comparator<TryItem>() {
                        @Override
                        public int compare(TryItem o1, TryItem o2) {
                            int x = o1.start.offset - o2.start.offset;
                            if (x == 0) {
                                x = o1.end.offset - o2.end.offset;
                            }
                            return x;
                        }
                    });
                }
            }
            { // merge dup handlers
                List<CodeItem.EncodedCatchHandler> uniqHanders = new ArrayList<>();
                Map<EncodedCatchHandler, EncodedCatchHandler> map = new HashMap<>();
                for (CodeItem.TryItem tryItem : uniqTrys) {
                    CodeItem.EncodedCatchHandler d = tryItem.handler;
                    CodeItem.EncodedCatchHandler uH = map.get(d);
                    if (uH != null) {
                        tryItem.handler = uH;
                    } else {
                        uniqHanders.add(d);
                        map.put(d, d);
                    }
                }
                this.handlers = uniqHanders;
                map.clear();
            }

        }
    }

    private void mergeExceptionHandler(EncodedCatchHandler to, EncodedCatchHandler from) {
        for (AddrPair pair : from.addPairs) {
            if (!to.addPairs.contains(pair)) {
                to.addPairs.add(pair);
            }
        }
        if (to.catchAll == null) {
            to.catchAll = from.catchAll;
        }
    }

    public void prepareInsns(List<Insn> ops, List<Insn> tailOps) {
        int codeSize = 0;
        for (Insn insn : ops) {
            insn.offset = codeSize;
            codeSize += insn.getCodeUnitSize();
        }
        for (Insn insn : tailOps) {
            if ((codeSize & 1) != 0) { // not 32bit alignment
                Insn nop = new PreBuildInsn(new byte[] { (byte) Op.NOP.opcode, 0 }); // f10x
                insn.offset = codeSize;
                codeSize += nop.getCodeUnitSize();
                ops.add(nop);
            }
            insn.offset = codeSize;
            codeSize += insn.getCodeUnitSize();
            ops.add(insn);
        }
        tailOps.clear();
        this.insns = ops;
        this.insn_size = codeSize;
    }

    public static class EncodedCatchHandler {
        public int handler_off;
        public List<AddrPair> addPairs;
        public Label catchAll;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            EncodedCatchHandler that = (EncodedCatchHandler) o;

            if (!addPairs.equals(that.addPairs))
                return false;
            if (catchAll != null ? !catchAll.equals(that.catchAll) : that.catchAll != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = addPairs.hashCode();
            result = 31 * result + (catchAll != null ? catchAll.offset : 0);
            return result;
        }

        public static class AddrPair {
            final public TypeIdItem type;
            final public Label addr;

            public AddrPair(TypeIdItem type, Label addr) {
                this.type = type;
                this.addr = addr;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (o == null || getClass() != o.getClass())
                    return false;

                AddrPair addrPair = (AddrPair) o;

                if (addr.offset != addrPair.addr.offset)
                    return false;
                if (!type.equals(addrPair.type))
                    return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = type.hashCode();
                result = 31 * result + addr.offset;
                return result;
            }
        }
    }

    public static class TryItem {
        public Label start;
        public Label end;
        public EncodedCatchHandler handler;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            TryItem tryItem = (TryItem) o;

            if (end.offset != tryItem.end.offset)
                return false;
            if (start.offset != tryItem.start.offset)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = start.offset;
            result = 31 * result + end.offset;
            return result;
        }
    }
}
