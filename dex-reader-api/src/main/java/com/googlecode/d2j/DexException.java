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
package com.googlecode.d2j;

/**
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public DexException() {
    }

    /**
     * @param message
     */
    public DexException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public DexException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public DexException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * this is equals to
     * 
     * <b> new DexException(String.format(messageFormat, args), cause); </b>
     * 
     * @param cause
     * @param messageFormat
     * @param args
     */
    public DexException(Throwable cause, String messageFormat, Object... args) {
        this(String.format(messageFormat, args), cause);
    }

    /**
     * this is equals to
     * 
     * <b> new DexException(String.format(messageFormat, args)); </b>
     * 
     * @param messageFormat
     * @param args
     */
    public DexException(String messageFormat, Object... args) {
        this(String.format(messageFormat, args));
    }
}
