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
package com.googlecode.d2j;

/**
 * @author bob
 */
public enum Visibility {
    BUILD(0), RUNTIME(1), SYSTEM(2);
    public int value;

    // int VISIBILITY_BUILD = 0;
    // int VISIBILITY_RUNTIME = 1;
    // int VISIBILITY_SYSTEM = 2;
    Visibility(int v) {
        this.value = v;
    }

    public String displayName() {
        return name().toLowerCase();
    }
}
