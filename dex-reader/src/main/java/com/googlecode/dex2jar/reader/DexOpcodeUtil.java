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
package com.googlecode.dex2jar.reader;

import static com.googlecode.dex2jar.reader.OpcodeFormat.F10t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F10x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F11n;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F11x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F12x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F20t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21h;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21s;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F21t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22b;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22s;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F22x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F23x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F30t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F31c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F31i;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F31t;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F32x;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F35c;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F3rc;
import static com.googlecode.dex2jar.reader.OpcodeFormat.F51l;

import com.googlecode.dex2jar.DexOpcodeDump;
import com.googlecode.dex2jar.DexOpcodes;

/**
 * 指令的工具类
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
class DexOpcodeUtil implements DexOpcodes, DexInternalOpcode {
    public static int format(int opcode) {
        switch (opcode) {
        case OP_GOTO:
            return F10t;
        case OP_NOP:
        case OP_RETURN_VOID:
            return F10x;
        case OP_CONST_4:
            return F11n;
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_WIDE:
        case OP_MOVE_RESULT_OBJECT:
        case OP_MOVE_EXCEPTION:
        case OP_RETURN:
        case OP_RETURN_WIDE:
        case OP_RETURN_OBJECT:
        case OP_MONITOR_ENTER:
        case OP_MONITOR_EXIT:
        case OP_THROW:
            return F11x;
        case OP_MOVE:
        case OP_MOVE_WIDE:
        case OP_MOVE_OBJECT:
        case OP_ARRAY_LENGTH:
        case OP_NEG_INT:
        case OP_NOT_INT:
        case OP_NEG_LONG:
        case OP_NOT_LONG:
        case OP_NEG_FLOAT:
        case OP_NEG_DOUBLE:
        case OP_INT_TO_LONG:
        case OP_INT_TO_FLOAT:
        case OP_INT_TO_DOUBLE:
        case OP_LONG_TO_INT:
        case OP_LONG_TO_FLOAT:
        case OP_LONG_TO_DOUBLE:
        case OP_FLOAT_TO_INT:
        case OP_FLOAT_TO_LONG:
        case OP_FLOAT_TO_DOUBLE:
        case OP_DOUBLE_TO_INT:
        case OP_DOUBLE_TO_LONG:
        case OP_DOUBLE_TO_FLOAT:
        case OP_INT_TO_BYTE:
        case OP_INT_TO_CHAR:
        case OP_INT_TO_SHORT:
        case OP_ADD_INT_2ADDR:
        case OP_SUB_INT_2ADDR:
        case OP_MUL_INT_2ADDR:
        case OP_DIV_INT_2ADDR:
        case OP_REM_INT_2ADDR:
        case OP_AND_INT_2ADDR:
        case OP_OR_INT_2ADDR:
        case OP_XOR_INT_2ADDR:
        case OP_SHL_INT_2ADDR:
        case OP_SHR_INT_2ADDR:
        case OP_USHR_INT_2ADDR:
        case OP_ADD_LONG_2ADDR:
        case OP_SUB_LONG_2ADDR:
        case OP_MUL_LONG_2ADDR:
        case OP_DIV_LONG_2ADDR:
        case OP_REM_LONG_2ADDR:
        case OP_AND_LONG_2ADDR:
        case OP_OR_LONG_2ADDR:
        case OP_XOR_LONG_2ADDR:
        case OP_SHL_LONG_2ADDR:
        case OP_SHR_LONG_2ADDR:
        case OP_USHR_LONG_2ADDR:
        case OP_ADD_FLOAT_2ADDR:
        case OP_SUB_FLOAT_2ADDR:
        case OP_MUL_FLOAT_2ADDR:
        case OP_DIV_FLOAT_2ADDR:
        case OP_REM_FLOAT_2ADDR:
        case OP_ADD_DOUBLE_2ADDR:
        case OP_SUB_DOUBLE_2ADDR:
        case OP_MUL_DOUBLE_2ADDR:
        case OP_DIV_DOUBLE_2ADDR:
        case OP_REM_DOUBLE_2ADDR:
            return F12x;
        case OP_GOTO_16:
            return F20t;
        case OP_CONST_STRING:
        case OP_CONST_CLASS:
        case OP_CHECK_CAST:
        case OP_NEW_INSTANCE:
        case OP_SGET:
        case OP_SGET_WIDE:
        case OP_SGET_OBJECT:
        case OP_SGET_BOOLEAN:
        case OP_SGET_BYTE:
        case OP_SGET_CHAR:
        case OP_SGET_SHORT:
        case OP_SPUT:
        case OP_SPUT_WIDE:
        case OP_SPUT_OBJECT:
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_BYTE:
        case OP_SPUT_CHAR:
        case OP_SPUT_SHORT:
            return F21c;
        case OP_CONST_HIGH16:
        case OP_CONST_WIDE_HIGH16:
            return F21h;
        case OP_CONST_16:
        case OP_CONST_WIDE_16:
            return F21s;
        case OP_IF_EQZ:
        case OP_IF_NEZ:
        case OP_IF_LTZ:
        case OP_IF_GEZ:
        case OP_IF_GTZ:
        case OP_IF_LEZ:
            return F21t;
        case OP_ADD_INT_LIT8:
        case OP_RSUB_INT_LIT8:
        case OP_MUL_INT_LIT8:
        case OP_DIV_INT_LIT8:
        case OP_REM_INT_LIT8:
        case OP_AND_INT_LIT8:
        case OP_OR_INT_LIT8:
        case OP_XOR_INT_LIT8:
        case OP_SHL_INT_LIT8:
        case OP_SHR_INT_LIT8:
        case OP_USHR_INT_LIT8:
            return F22b;
        case OP_INSTANCE_OF:
        case OP_NEW_ARRAY:
        case OP_IGET:
        case OP_IGET_WIDE:
        case OP_IGET_OBJECT:
        case OP_IGET_BOOLEAN:
        case OP_IGET_BYTE:
        case OP_IGET_CHAR:
        case OP_IGET_SHORT:
        case OP_IPUT:
        case OP_IPUT_WIDE:
        case OP_IPUT_OBJECT:
        case OP_IPUT_BOOLEAN:
        case OP_IPUT_BYTE:
        case OP_IPUT_CHAR:
        case OP_IPUT_SHORT:
            return F22c;
        case OP_ADD_INT_LIT16:
        case OP_RSUB_INT:
        case OP_MUL_INT_LIT16:
        case OP_DIV_INT_LIT16:
        case OP_REM_INT_LIT16:
        case OP_AND_INT_LIT16:
        case OP_OR_INT_LIT16:
        case OP_XOR_INT_LIT16:
            return F22s;
        case OP_IF_EQ:
        case OP_IF_NE:
        case OP_IF_LT:
        case OP_IF_GE:
        case OP_IF_GT:
        case OP_IF_LE:
            return F22t;
        case OP_MOVE_FROM16:
        case OP_MOVE_WIDE_FROM16:
        case OP_MOVE_OBJECT_FROM16:
            return F22x;
        case OP_CMPL_FLOAT:
        case OP_CMPG_FLOAT:
        case OP_CMPL_DOUBLE:
        case OP_CMPG_DOUBLE:
        case OP_CMP_LONG:
        case OP_AGET:
        case OP_AGET_WIDE:
        case OP_AGET_OBJECT:
        case OP_AGET_BOOLEAN:
        case OP_AGET_BYTE:
        case OP_AGET_CHAR:
        case OP_AGET_SHORT:
        case OP_APUT:
        case OP_APUT_WIDE:
        case OP_APUT_OBJECT:
        case OP_APUT_BOOLEAN:
        case OP_APUT_BYTE:
        case OP_APUT_CHAR:
        case OP_APUT_SHORT:
        case OP_ADD_INT:
        case OP_SUB_INT:
        case OP_MUL_INT:
        case OP_DIV_INT:
        case OP_REM_INT:
        case OP_AND_INT:
        case OP_OR_INT:
        case OP_XOR_INT:
        case OP_SHL_INT:
        case OP_SHR_INT:
        case OP_USHR_INT:
        case OP_ADD_LONG:
        case OP_SUB_LONG:
        case OP_MUL_LONG:
        case OP_DIV_LONG:
        case OP_REM_LONG:
        case OP_AND_LONG:
        case OP_OR_LONG:
        case OP_XOR_LONG:
        case OP_SHL_LONG:
        case OP_SHR_LONG:
        case OP_USHR_LONG:
        case OP_ADD_FLOAT:
        case OP_SUB_FLOAT:
        case OP_MUL_FLOAT:
        case OP_DIV_FLOAT:
        case OP_REM_FLOAT:
        case OP_ADD_DOUBLE:
        case OP_SUB_DOUBLE:
        case OP_MUL_DOUBLE:
        case OP_DIV_DOUBLE:
        case OP_REM_DOUBLE:
            return F23x;
        case OP_GOTO_32:
            return F30t;
        case OP_CONST_STRING_JUMBO:
            return F31c;
        case OP_CONST:
        case OP_CONST_WIDE_32:
            return F31i;
        case OP_FILL_ARRAY_DATA:
        case OP_PACKED_SWITCH:
        case OP_SPARSE_SWITCH:
            return F31t;
        case OP_MOVE_16:
        case OP_MOVE_WIDE_16:
        case OP_MOVE_OBJECT_16:
            return F32x;
        case OP_FILLED_NEW_ARRAY:
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_SUPER:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_STATIC:
        case OP_INVOKE_INTERFACE:
            return F35c;
        case OP_FILLED_NEW_ARRAY_RANGE:
        case OP_INVOKE_VIRTUAL_RANGE:
        case OP_INVOKE_SUPER_RANGE:
        case OP_INVOKE_DIRECT_RANGE:
        case OP_INVOKE_STATIC_RANGE:
        case OP_INVOKE_INTERFACE_RANGE:
            return F3rc;
        case OP_CONST_WIDE:
            return F51l;
        }
        throw new RuntimeException("opcode length for " + opcode + " not found!");
    }

    public static int length(int opcode) {
        switch (opcode) {
        // case OP: //length
        case OP_NOP: // 1
        case OP_MOVE: // 1
        case OP_MOVE_WIDE: // 1
        case OP_MOVE_OBJECT: // 1
        case OP_MOVE_RESULT: // 1
        case OP_MOVE_RESULT_WIDE: // 1
        case OP_MOVE_RESULT_OBJECT: // 1
        case OP_MOVE_EXCEPTION: // 1
        case OP_RETURN_VOID: // 1
        case OP_RETURN: // 1
        case OP_RETURN_WIDE: // 1
        case OP_RETURN_OBJECT: // 1
        case OP_CONST_4: // 1
        case OP_MONITOR_ENTER: // 1
        case OP_MONITOR_EXIT: // 1
        case OP_ARRAY_LENGTH: // 1
        case OP_THROW: // 1
        case OP_GOTO: // 1
        case OP_NEG_INT: // 1
        case OP_NOT_INT: // 1
        case OP_NEG_LONG: // 1
        case OP_NOT_LONG: // 1
        case OP_NEG_FLOAT: // 1
        case OP_NEG_DOUBLE: // 1
        case OP_INT_TO_LONG: // 1
        case OP_INT_TO_FLOAT: // 1
        case OP_INT_TO_DOUBLE: // 1
        case OP_LONG_TO_INT: // 1
        case OP_LONG_TO_FLOAT: // 1
        case OP_LONG_TO_DOUBLE: // 1
        case OP_FLOAT_TO_INT: // 1
        case OP_FLOAT_TO_LONG: // 1
        case OP_FLOAT_TO_DOUBLE: // 1
        case OP_DOUBLE_TO_INT: // 1
        case OP_DOUBLE_TO_LONG: // 1
        case OP_DOUBLE_TO_FLOAT: // 1
        case OP_INT_TO_BYTE: // 1
        case OP_INT_TO_CHAR: // 1
        case OP_INT_TO_SHORT: // 1
        case OP_ADD_INT_2ADDR: // 1
        case OP_SUB_INT_2ADDR: // 1
        case OP_MUL_INT_2ADDR: // 1
        case OP_DIV_INT_2ADDR: // 1
        case OP_REM_INT_2ADDR: // 1
        case OP_AND_INT_2ADDR: // 1
        case OP_OR_INT_2ADDR: // 1
        case OP_XOR_INT_2ADDR: // 1
        case OP_SHL_INT_2ADDR: // 1
        case OP_SHR_INT_2ADDR: // 1
        case OP_USHR_INT_2ADDR: // 1
        case OP_ADD_LONG_2ADDR: // 1
        case OP_SUB_LONG_2ADDR: // 1
        case OP_MUL_LONG_2ADDR: // 1
        case OP_DIV_LONG_2ADDR: // 1
        case OP_REM_LONG_2ADDR: // 1
        case OP_AND_LONG_2ADDR: // 1
        case OP_OR_LONG_2ADDR: // 1
        case OP_XOR_LONG_2ADDR: // 1
        case OP_SHL_LONG_2ADDR: // 1
        case OP_SHR_LONG_2ADDR: // 1
        case OP_USHR_LONG_2ADDR: // 1
        case OP_ADD_FLOAT_2ADDR: // 1
        case OP_SUB_FLOAT_2ADDR: // 1
        case OP_MUL_FLOAT_2ADDR: // 1
        case OP_DIV_FLOAT_2ADDR: // 1
        case OP_REM_FLOAT_2ADDR: // 1
        case OP_ADD_DOUBLE_2ADDR: // 1
        case OP_SUB_DOUBLE_2ADDR: // 1
        case OP_MUL_DOUBLE_2ADDR: // 1
        case OP_DIV_DOUBLE_2ADDR: // 1
        case OP_REM_DOUBLE_2ADDR: // 1
            return 1;
        case OP_MOVE_FROM16: // 2
        case OP_MOVE_WIDE_FROM16: // 2
        case OP_MOVE_OBJECT_FROM16: // 2
        case OP_CONST_16: // 2
        case OP_CONST_HIGH16: // 2
        case OP_CONST_WIDE_16: // 2
        case OP_CONST_WIDE_HIGH16: // 2
        case OP_CONST_STRING: // 2
        case OP_CONST_CLASS: // 2
        case OP_CHECK_CAST: // 2
        case OP_INSTANCE_OF: // 2
        case OP_NEW_INSTANCE: // 2
        case OP_NEW_ARRAY: // 2
        case OP_GOTO_16: // 2
        case OP_CMPL_FLOAT: // 2
        case OP_CMPG_FLOAT: // 2
        case OP_CMPL_DOUBLE: // 2
        case OP_CMPG_DOUBLE: // 2
        case OP_CMP_LONG: // 2
        case OP_IF_EQ: // 2
        case OP_IF_NE: // 2
        case OP_IF_LT: // 2
        case OP_IF_GE: // 2
        case OP_IF_GT: // 2
        case OP_IF_LE: // 2
        case OP_IF_EQZ: // 2
        case OP_IF_NEZ: // 2
        case OP_IF_LTZ: // 2
        case OP_IF_GEZ: // 2
        case OP_IF_GTZ: // 2
        case OP_IF_LEZ: // 2
        case OP_AGET: // 2
        case OP_AGET_WIDE: // 2
        case OP_AGET_OBJECT: // 2
        case OP_AGET_BOOLEAN: // 2
        case OP_AGET_BYTE: // 2
        case OP_AGET_CHAR: // 2
        case OP_AGET_SHORT: // 2
        case OP_APUT: // 2
        case OP_APUT_WIDE: // 2
        case OP_APUT_OBJECT: // 2
        case OP_APUT_BOOLEAN: // 2
        case OP_APUT_BYTE: // 2
        case OP_APUT_CHAR: // 2
        case OP_APUT_SHORT: // 2
        case OP_IGET: // 2
        case OP_IGET_WIDE: // 2
        case OP_IGET_OBJECT: // 2
        case OP_IGET_BOOLEAN: // 2
        case OP_IGET_BYTE: // 2
        case OP_IGET_CHAR: // 2
        case OP_IGET_SHORT: // 2
        case OP_IPUT: // 2
        case OP_IPUT_WIDE: // 2
        case OP_IPUT_OBJECT: // 2
        case OP_IPUT_BOOLEAN: // 2
        case OP_IPUT_BYTE: // 2
        case OP_IPUT_CHAR: // 2
        case OP_IPUT_SHORT: // 2
        case OP_SGET: // 2
        case OP_SGET_WIDE: // 2
        case OP_SGET_OBJECT: // 2
        case OP_SGET_BOOLEAN: // 2
        case OP_SGET_BYTE: // 2
        case OP_SGET_CHAR: // 2
        case OP_SGET_SHORT: // 2
        case OP_SPUT: // 2
        case OP_SPUT_WIDE: // 2
        case OP_SPUT_OBJECT: // 2
        case OP_SPUT_BOOLEAN: // 2
        case OP_SPUT_BYTE: // 2
        case OP_SPUT_CHAR: // 2
        case OP_SPUT_SHORT: // 2
        case OP_ADD_INT: // 2
        case OP_SUB_INT: // 2
        case OP_MUL_INT: // 2
        case OP_DIV_INT: // 2
        case OP_REM_INT: // 2
        case OP_AND_INT: // 2
        case OP_OR_INT: // 2
        case OP_XOR_INT: // 2
        case OP_SHL_INT: // 2
        case OP_SHR_INT: // 2
        case OP_USHR_INT: // 2
        case OP_ADD_LONG: // 2
        case OP_SUB_LONG: // 2
        case OP_MUL_LONG: // 2
        case OP_DIV_LONG: // 2
        case OP_REM_LONG: // 2
        case OP_AND_LONG: // 2
        case OP_OR_LONG: // 2
        case OP_XOR_LONG: // 2
        case OP_SHL_LONG: // 2
        case OP_SHR_LONG: // 2
        case OP_USHR_LONG: // 2
        case OP_ADD_FLOAT: // 2
        case OP_SUB_FLOAT: // 2
        case OP_MUL_FLOAT: // 2
        case OP_DIV_FLOAT: // 2
        case OP_REM_FLOAT: // 2
        case OP_ADD_DOUBLE: // 2
        case OP_SUB_DOUBLE: // 2
        case OP_MUL_DOUBLE: // 2
        case OP_DIV_DOUBLE: // 2
        case OP_REM_DOUBLE: // 2
        case OP_ADD_INT_LIT16: // 2
        case OP_RSUB_INT: // 2
        case OP_MUL_INT_LIT16: // 2
        case OP_DIV_INT_LIT16: // 2
        case OP_REM_INT_LIT16: // 2
        case OP_AND_INT_LIT16: // 2
        case OP_OR_INT_LIT16: // 2
        case OP_XOR_INT_LIT16: // 2
        case OP_ADD_INT_LIT8: // 2
        case OP_RSUB_INT_LIT8: // 2
        case OP_MUL_INT_LIT8: // 2
        case OP_DIV_INT_LIT8: // 2
        case OP_REM_INT_LIT8: // 2
        case OP_AND_INT_LIT8: // 2
        case OP_OR_INT_LIT8: // 2
        case OP_XOR_INT_LIT8: // 2
        case OP_SHL_INT_LIT8: // 2
        case OP_SHR_INT_LIT8: // 2
        case OP_USHR_INT_LIT8: // 2
            return 2;
        case OP_MOVE_16: // 3
        case OP_MOVE_WIDE_16: // 3
        case OP_MOVE_OBJECT_16: // 3
        case OP_CONST: // 3
        case OP_CONST_WIDE_32: // 3
        case OP_CONST_STRING_JUMBO: // 3
        case OP_FILLED_NEW_ARRAY: // 3
        case OP_FILLED_NEW_ARRAY_RANGE: // 3
        case OP_FILL_ARRAY_DATA: // 3
        case OP_GOTO_32: // 3
        case OP_PACKED_SWITCH: // 3
        case OP_SPARSE_SWITCH: // 3
        case OP_INVOKE_VIRTUAL: // 3
        case OP_INVOKE_SUPER: // 3
        case OP_INVOKE_DIRECT: // 3
        case OP_INVOKE_STATIC: // 3
        case OP_INVOKE_INTERFACE: // 3
        case OP_INVOKE_VIRTUAL_RANGE: // 3
        case OP_INVOKE_SUPER_RANGE: // 3
        case OP_INVOKE_DIRECT_RANGE: // 3
        case OP_INVOKE_STATIC_RANGE: // 3
        case OP_INVOKE_INTERFACE_RANGE: // 3
            return 3;
        case OP_CONST_WIDE: // 5
            return 5;
        }
        throw new RuntimeException("opcode length for 0x" + Integer.toHexString(opcode) + DexOpcodeDump.dump(opcode)
                + " not found!");
    }

}
