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
public class ArrayRes {

    void array() {
        int a[] = new int[] { 1, 2, 2, 3, 4, 5, 5, 6, 2, 7, };
        System.out.println(a);
        bb(1, 2, 4, 5, 6, 66, 77, 9, 77, 1, 123);
        int b = a[2];
        int c = (int) System.currentTimeMillis();
        bb(b, c);

    }

    void adadfasd() {
        Object object = new Object();
        Object object1 = new Object();
        Object object2 = new Object();
        Object object3 = new Object();
        Object object4 = new Object();
        Object object5 = new Object();
        Object object6 = new Object();
        Object object7 = new Object();
        Object object8 = new Object();
        Object object9 = new Object();
        Object object10 = new Object();
        Object object11 = new Object[] { object, object1, object2, object3, object4, object5, object6, object7, object8, object9, object10 };

    }

    Object adssss() {
        return new Object[] { new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
                new Object(), new Object(), new Object() };
    }

    void bb(int... aaaaa) {
    }

}
