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
public class WideRes {
    float aaa() {
        float a = 1.01f + 2;
        float b = 12 + a;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return a + 2;
    }

    double bbb() {
        double b = 1.01 + 2;
        double a = 12312.123 + b;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return b + 2;
    }

    int ccc() {
        int b = 1 + 2;
        int a = 12312 + b;
        a += 1231231231;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return b + 2;
    }

    long ddd() {
        long b = 1l + 2;
        long a = 12312l + b;
        a += 1231232134234234524L;
        a = a + b;
        a = a * b;
        a = a - b;
        a = a / b;
        return b + 2;
    }

}
