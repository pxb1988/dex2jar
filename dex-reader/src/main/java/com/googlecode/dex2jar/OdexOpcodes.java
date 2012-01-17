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
package com.googlecode.dex2jar;

/**
 * dex2jar odex instruction set
 * 
 * @author Panxiaobo
 * @version $Rev$
 */
public interface OdexOpcodes extends DexOpcodes {
    int OP_THROW_VERIFICATION_ERROR = 0x0000ed;
    int OP_EXECUTE_INLINE = 0x0000ee;
    int OP_INVOKE_SUPER_QUICK = 0x0000fa;
    int OP_INVOKE_VIRTUAL_QUICK = 0x0000f8;
    int OP_IGET_QUICK = 0x0000f2;
    int OP_IPUT_QUICK = 0x0000f5;

}
