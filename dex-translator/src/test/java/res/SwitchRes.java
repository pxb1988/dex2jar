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
public class SwitchRes {

    void sw1() {
        int a = 1;
        switch (a) {
        case 1:
        case 2:
        case 3:
            System.out.println("123");
            break;
        case 100:
            System.out.println("100");
            break;
        default:
            System.out.println("def");
            break;
        }
    }

    void sw2() {
        int a = 1;
        int b = 2;
        switch (a) {
        case 1:
            if (b == 2) {
                System.out.println("b");
            } else {
                System.out.println("bbb");
            }
        case 2:
        case 3:
            System.out.println("123");
            break;
        case 100:
            System.out.println("100");
            break;
        default:
            System.out.println("def");
            break;
        }
    }

}
