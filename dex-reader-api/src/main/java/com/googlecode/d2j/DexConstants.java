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
 * constants in dex file
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public abstract interface DexConstants {

    int ACC_PUBLIC = 0x0001; // class, field, method
    int ACC_PRIVATE = 0x0002; // class, field, method
    int ACC_PROTECTED = 0x0004; // class, field, method
    int ACC_STATIC = 0x0008; // field, method
    int ACC_FINAL = 0x0010; // class, field, method
    // int ACC_SUPER = 0x0020; // class
    int ACC_SYNCHRONIZED = 0x0020; // method
    int ACC_VOLATILE = 0x0040; // field
    int ACC_BRIDGE = 0x0040; // method
    int ACC_VARARGS = 0x0080; // method
    int ACC_TRANSIENT = 0x0080; // field
    int ACC_NATIVE = 0x0100; // method
    int ACC_INTERFACE = 0x0200; // class
    int ACC_ABSTRACT = 0x0400; // class, method
    int ACC_STRICT = 0x0800; // method
    int ACC_SYNTHETIC = 0x1000; // class, field, method
    int ACC_ANNOTATION = 0x2000; // class
    int ACC_ENUM = 0x4000; // class(?) field inner
    int ACC_CONSTRUCTOR = 0x10000;// constructor method (class or instance initializer)
    int ACC_DECLARED_SYNCHRONIZED = 0x20000;

    String ANNOTATION_DEFAULT_TYPE = "Ldalvik/annotation/AnnotationDefault;";
    String ANNOTATION_SIGNATURE_TYPE = "Ldalvik/annotation/Signature;";
    String ANNOTATION_THROWS_TYPE = "Ldalvik/annotation/Throws;";
    String ANNOTATION_ENCLOSING_CLASS_TYPE = "Ldalvik/annotation/EnclosingClass;";
    String ANNOTATION_ENCLOSING_METHOD_TYPE = "Ldalvik/annotation/EnclosingMethod;";
    String ANNOTATION_INNER_CLASS_TYPE = "Ldalvik/annotation/InnerClass;";
    String ANNOTATION_MEMBER_CLASSES_TYPE = "Ldalvik/annotation/MemberClasses;";
}
