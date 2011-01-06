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

import java.util.ArrayList;
import java.util.List;

/**
 * 参数和返回类型
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Proto {
    /**
     * 描述
     */
    private String desc;
    /**
     * 参数类型
     */
    private String[] parameterTypes;
    /**
     * 返回类型
     */
    private String returnType;

    public Proto(Dex dex, DataIn in) {
        // int shorty_idx = in.readIntx();
        in.skip(4);
        int return_type_idx = in.readIntx();
        int parameters_off = in.readIntx();

        returnType = dex.getType(return_type_idx);
        List<String> parameterTypeList = new ArrayList<String>();
        StringBuilder ps = new StringBuilder("(");
        if (parameters_off != 0) {
            in.pushMove(parameters_off);
            try {
                int size = in.readIntx();
                for (int i = 0; i < size; i++) {
                    String p = dex.getType(in.readShortx());
                    parameterTypeList.add(p);
                    ps.append(p);
                }
            } finally {
                in.pop();
            }
        }
        ps.append(")").append(returnType);
        desc = ps.toString();
        parameterTypes = parameterTypeList.toArray(new String[parameterTypeList.size()]);
    }

    public String getDesc() {
        return desc;
    }

    /**
     * @return the parameterTypes
     */
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    public String toString() {
        return desc;
    }
}
