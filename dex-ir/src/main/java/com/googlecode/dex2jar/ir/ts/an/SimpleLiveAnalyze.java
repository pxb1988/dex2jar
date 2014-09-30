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
package com.googlecode.dex2jar.ir.ts.an;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.UniqueQueue;

import java.util.*;

public class SimpleLiveAnalyze extends BaseAnalyze<SimpleLiveValue> {
    protected Set<SimpleLiveValue> markUsed() {
        Set<SimpleLiveValue> used = new HashSet<SimpleLiveValue>(aValues.size() / 2);
        Queue<SimpleLiveValue> q = new UniqueQueue<>();
        for (SimpleLiveValue sv : aValues) {
            if (sv.used) {
                q.add(sv);
                while (!q.isEmpty()) {
                    SimpleLiveValue v = q.poll();
                    if (v.used) {
                        if (used.contains(v)) {
                            continue;
                        }
                        used.add(v);
                        {
                            SimpleLiveValue p = v.parent;
                            if (p != null) {
                                if (!p.used) {
                                    p.used = true;
                                    q.add(p);
                                }
                            }
                        }
                        if (v.otherParents != null) {
                            for (SimpleLiveValue p : v.otherParents) {
                                if (!p.used) {
                                    p.used = true;
                                    q.add(p);
                                }
                            }
                            v.otherParents = null;
                        }
                    }
                }
            }
        }

        return used;
    }

    @Override
    protected void analyzeValue() {
        markUsed();
    }

    public int getLocalSize() {
        return localSize;
    }

    public SimpleLiveAnalyze(IrMethod method, boolean reindexLocal) {
        super(method, reindexLocal);
    }

    @Override
    protected SimpleLiveValue onAssignLocal(Local local, Value value) {
        SimpleLiveValue v = super.onAssignLocal(local, value);
        v.used = true;
        return v;
    }

    @Override
    protected void onUseLocal(SimpleLiveValue aValue, Local local) {
        aValue.used = true;
        super.onUseLocal(aValue, local);
    }

    @Override
    public SimpleLiveValue[] merge(SimpleLiveValue[] srcFrame, SimpleLiveValue[] distFrame, Stmt src, Stmt dist) {
        if (distFrame == null) {
            distFrame = new SimpleLiveValue[this.localSize];
            for (int i = 0; i < srcFrame.length; i++) {
                SimpleLiveValue sV = srcFrame[i];
                if (sV != null) {
                    SimpleLiveValue dV = new SimpleLiveValue();
                    aValues.add(dV);
                    dV.parent = sV;
                    distFrame[i] = dV;
                }
            }
        } else {
            for (int i = 0; i < srcFrame.length; i++) {
                SimpleLiveValue sV = srcFrame[i];
                SimpleLiveValue dV = distFrame[i];
                if (sV != null && dV != null) {
                    List<SimpleLiveValue> ps = dV.otherParents;
                    if (ps == null) {
                        dV.otherParents = ps = new ArrayList<>(3);
                    }
                    ps.add(sV);
                }
            }
        }
        return distFrame;
    }

    @Override
    protected SimpleLiveValue[] newFrame(int size) {
        return new SimpleLiveValue[size];
    }

    @Override
    protected SimpleLiveValue newValue() {
        return new SimpleLiveValue();
    }
}