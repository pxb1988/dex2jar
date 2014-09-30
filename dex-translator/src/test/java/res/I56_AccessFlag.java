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
package res;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
public class I56_AccessFlag {
    static class AStaticInnerClass {
    }

    class AInnerClass {
        class AAA {
        }
    }

    interface AInterface {
    }

    public static class B1 {
    };

    public static class B11 {
        public static class XXX1 {
        };
    };

    private static class B2 {
    };

    protected static class B3 {
    };

    /* package */static class B0 {
    };

    final static class B4 {
    }

    /* package */class C0 {
    }

    public class C1 {
    }

    private class C2 {
    }

    protected class C3 {
    }

    final class C4 {
    }

    abstract class C5 {
    }

    synchronized void sync1() {
    }

    /**
     * seams that dx translate this method to
     * 
     * <pre>
     * void sync2() {
     *     synchronized (this) {
     *         new Object();
     *     }
     * }
     * </pre>
     */
    synchronized void sync2() {
        new Object();
    }

    void a() {
        new Object() {
            Object o = new Object() {
            };

            class AX {
            }
        };
    }

    Object o = new Object() {
    };

    static interface AStaticInterface {
    }
}
