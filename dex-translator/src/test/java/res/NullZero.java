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
public class NullZero {
    void nullzero() {
        String _null = null;
        int zero = 0;
        if (_null == null) {
            _null = "asdf";
            if (zero == 1) {
                zero = 123;
            }
        }
        System.out.println(0);
        System.out.println((String) null);
        System.out.println(_null);
        System.out.println(zero);
    }
}
