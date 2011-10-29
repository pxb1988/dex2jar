/*
 * Copyright (c) 2009-2011 Panxiaobo
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class ArrayOut implements Out {

    int i = 0;

    public List<String> array = new ArrayList<String>();
    public List<Integer> is = new ArrayList<Integer>();

    public void push() {
        i++;
    }

    public void s(String s) {
        is.add(i);
        array.add(s);
    }

    public void s(String format, Object... arg) {
        s(String.format(format, arg));
    }

    public void pop() {
        i--;
    }

}
