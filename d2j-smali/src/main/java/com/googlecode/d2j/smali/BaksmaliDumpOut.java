/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
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
package com.googlecode.d2j.smali;

import com.googlecode.d2j.util.Out;

import java.io.BufferedWriter;
import java.io.IOException;

public class BaksmaliDumpOut implements Out {
    private final BufferedWriter writer;
    int i;
    final String indent;

    public BaksmaliDumpOut(BufferedWriter writer) {
        this("  ", writer);
    }

    public BaksmaliDumpOut(String indent, BufferedWriter writer) {
        this.writer = writer;
        i = 0;
        this.indent = indent;
    }

    @Override
    public void pop() {
        i--;
    }

    @Override
    public void push() {
        i++;
    }

    @Override
    public void s(String s) {
        try {
            for (int i = 0; i < this.i; i++) {
                writer.append(indent);
            }
            writer.append(s);
            writer.newLine();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void s(String format, Object... arg) {
        try {
            for (int i = 0; i < this.i; i++) {
                writer.append(indent);
            }
            writer.append(String.format(format, arg));
            writer.newLine();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
