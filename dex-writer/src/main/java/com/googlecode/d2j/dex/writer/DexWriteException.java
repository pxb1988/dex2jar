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
package com.googlecode.d2j.dex.writer;

public class DexWriteException extends RuntimeException {

    private static final long serialVersionUID = 8501861997187282533L;

    public DexWriteException() {
        super();

    }

    public DexWriteException(String message) {
        super(message);

    }

    public DexWriteException(String message, Throwable cause) {
        super(message, cause);

    }

    public DexWriteException(String message, Throwable cause,
                             boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

    }

    public DexWriteException(Throwable cause) {
        super(cause);

    }

}
