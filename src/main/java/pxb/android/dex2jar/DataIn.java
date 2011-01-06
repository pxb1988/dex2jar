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
package pxb.android.dex2jar;

/**
 * 输入流
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public interface DataIn {

    /**
     * 获取当前位置
     * 
     * @return
     */
    int getCurrentPosition();

    void move(int offset);

    boolean needPadding();

    void pop();

    void push();

    /**
     * equals to
     * 
     * <pre>
     * push();
     * move(offset);
     * </pre>
     * 
     * @see #push()
     * @see #move(int)
     * @param offset
     */
    void pushMove(int offset);

    /**
	 * 
	 */
    int readByte();

    byte[] readBytes(int size);

    int readIntx();

    long readLongx();

    short readShortx();

    long readSignedLeb128();

    /**
     * @return
     */
    int readUnsignedByte();

    long readUnsignedLeb128();

    /**
     * @param i
     */
    void skip(int bytes);

}
