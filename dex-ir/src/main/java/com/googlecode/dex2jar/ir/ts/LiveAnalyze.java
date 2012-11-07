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
package com.googlecode.dex2jar.ir.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.stmt.Stmt;

public class LiveAnalyze extends BaseLiveAnalyze {
    public static class LivePhi extends Phi {
        public Local local;
        public Phi tag;

        @Override
        public String toString() {
            if (tag != null) {
                return tag.toString();
            }
            if (local != null) {
                return local.toString();
            }
            return "?";
        }
    }

    private static LivePhi trim(LivePhi phi) {
        while (phi.tag != null) {
            phi = (LivePhi) phi.tag;
        }
        return phi;
    }

    public LiveAnalyze(IrMethod method) {
        super(method);

    }

    @Override
    protected void onUseLocal(Phi phi, Local local) {
        LivePhi phi2 = (LivePhi) phi;
        phi2.local = local;
    }

    protected Phi newPhi() {
        return new LivePhi();
    }

    @Override
    protected void analyzePhi() {
        Set<Phi> used = super.markUsed();

        for (Phi x : used) {
            LivePhi reg = (LivePhi) x;
            LivePhi a = trim(reg);
            if (a != reg && reg.local != null) {
                a.local = reg.local;
            }
            if (reg.parents.size() > 0) {
                for (Phi r : reg.parents) {
                    LivePhi b = trim((LivePhi) r);
                    if (a != b) {
                        b.tag = a;
                        if (b.local != null) {
                            a.local = b.local;
                        }
                    }
                }
            }
        }

        for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
            Phi[] frame = (Phi[]) p._ls_forward_frame;
            if (frame != null) {
                for (int i = 0; i < frame.length; i++) {
                    Phi r = frame[i];
                    if (r != null) {
                        if (r.used) {
                            frame[i] = trim((LivePhi) r);
                        } else {
                            frame[i] = null;
                        }
                    }
                }
            }
        }

        phis.clear();
        for (Phi x : used) {
            LivePhi r = (LivePhi) x;
            if (r.used && r.tag == null) {
                r.parents.clear();
                phis.add(r);
            }
        }
        used.clear();

        // reduce the size of nPhis
        List<Phi> nPhis = new ArrayList<Phi>(phis);
        phis.clear();
        Collections.sort(nPhis, new Comparator<Phi>() {
            @Override
            public int compare(Phi o1, Phi o2) {
                LivePhi a1 = (LivePhi) o1;
                LivePhi a2 = (LivePhi) o2;
                return a1.local._ls_index - a2.local._ls_index;
            }
        });
        phis.addAll(nPhis);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Stmt stmt = method.stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            Phi[] frame = (Phi[]) stmt._ls_forward_frame;
            if (frame != null) {
                for (Phi p : frame) {
                    if (p == null) {
                        sb.append('.');
                    } else if (p.used) {
                        sb.append('x');
                    } else {
                        sb.append('?');
                    }
                }
                sb.append(" | ");
            }
            sb.append(stmt.toString()).append('\n');
        }
        return sb.toString();
    }
}