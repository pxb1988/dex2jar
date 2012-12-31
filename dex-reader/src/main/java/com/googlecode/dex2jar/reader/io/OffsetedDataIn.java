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
package com.googlecode.dex2jar.reader.io;

/**
 * 
 * @author Panxiaobo
 * @version $Rev$
 */
public class OffsetedDataIn extends DataInWrapper {

    private int offset;

    public OffsetedDataIn(DataIn in, int offset) {
        super(in);
        super.move(offset);
        this.offset = offset;
    }

    @Override
    public int getCurrentPosition() {
        return super.getCurrentPosition() - offset;
    }

    @Override
    public void move(int absOffset) {
        super.move(absOffset + offset);
    }

    @Override
    public void pushMove(int absOffset) {
        super.pushMove(absOffset + offset);
    }

}
