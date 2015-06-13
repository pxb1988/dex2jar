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
package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.dex.writer.DexWriteException;

import java.util.*;

public class ConstPool {
    public List<EncodedArrayItem> encodedArrayItems = new ArrayList<>();
    public Map<AnnotationSetRefListItem, AnnotationSetRefListItem> annotationSetRefListItems = new HashMap<>();
    public List<CodeItem> codeItems = new ArrayList<>();
    public List<ClassDataItem> classDataItems = new ArrayList<>();
    public List<DebugInfoItem> debugInfoItems = new ArrayList<>();
    public Map<AnnotationItem, AnnotationItem> annotationItems = new HashMap<>();
    public List<AnnotationsDirectoryItem> annotationsDirectoryItems = new ArrayList<>();
    public Map<AnnotationSetItem, AnnotationSetItem> annotationSetItems = new HashMap<>();
    public Map<FieldIdItem, FieldIdItem> fields = new TreeMap<>();
    public Map<MethodIdItem, MethodIdItem> methods = new TreeMap<>();
    public Map<ProtoIdItem, ProtoIdItem> protos = new TreeMap<>();
    public List<StringDataItem> stringDatas = new ArrayList<>(100);
    public Map<String, StringIdItem> strings = new TreeMap<>();
    public Map<TypeListItem, TypeListItem> typeLists = new TreeMap<>();
    public Map<String, TypeIdItem> types = new TreeMap<>();
    public Map<TypeIdItem, ClassDefItem> classDefs = new HashMap<>();

    public Object wrapEncodedItem(Object value) {
        if (value instanceof DexType) {
            value = uniqType(((DexType) value).desc);
        } else if (value instanceof Field) {
            value = uniqField((Field) value);
        } else if (value instanceof String) {
            value = uniqString((String) value);
        } else if (value instanceof Method) {
            value = uniqMethod((Method) value);
        }
        return value;
    }

    public void clean() {
        encodedArrayItems.clear();
        annotationSetRefListItems.clear();
        codeItems.clear();
        classDataItems.clear();
        debugInfoItems.clear();
        annotationItems.clear();
        annotationsDirectoryItems.clear();
        annotationSetItems.clear();
        fields.clear();
        methods.clear();
        protos.clear();
        stringDatas.clear();
        typeLists.clear();
        types.clear();
        classDefs.clear();
    }

    private String buildShorty(String ret, String[] types2) {
        StringBuilder sb = new StringBuilder();
        if (ret.length() == 1) {
            sb.append(ret);
        } else {
            sb.append("L");
        }
        for (String s : types2) {
            if (s.length() == 1) {
                sb.append(s);
            } else {
                sb.append("L");
            }
        }
        return sb.toString();
    }

    PE iterateParent(ClassDefItem p) {
        List<TypeIdItem> list = new ArrayList<>(6);
        list.add(p.superclazz);
        if (p.interfaces != null) {
            list.addAll(p.interfaces.items);
        }
        return new PE(p, list.iterator());
    }

    public void addDebugInfoItem(DebugInfoItem debugInfoItem) {
        debugInfoItems.add(debugInfoItem);
    }

    static class PE {
        final ClassDefItem owner;
        final Iterator<TypeIdItem> it;

        PE(ClassDefItem owner, Iterator<TypeIdItem> it) {
            this.owner = owner;
            this.it = it;
        }
    }

    public List<ClassDefItem> buildSortedClassDefItems() {
        List<ClassDefItem> added = new ArrayList<>();
        Stack<PE> stack1 = new Stack<>();
        Set<ClassDefItem> children = new HashSet<>();

        for (ClassDefItem c : classDefs.values()) {
            if (added.contains(c)) {
                continue;
            }
            children.add(c);
            stack1.push(iterateParent(c));

            while (!stack1.empty()) {
                PE e = stack1.peek();
                boolean canPop = true;
                while (e.it.hasNext()) {
                    TypeIdItem tid = e.it.next();
                    if (tid == null) {
                        continue;
                    }
                    ClassDefItem superDef = classDefs.get(tid);
                    if (superDef != null && !added.contains(superDef)) {
                        if (children.contains(superDef)) {
                            System.err.println("WARN: dep-loop " + e.owner.clazz.descriptor.stringData.string + " -> "
                                    + superDef.clazz.descriptor.stringData.string);
                        } else {
                            canPop = false;
                            children.add(superDef);
                            stack1.push(iterateParent(superDef));
                            break;
                        }
                    }
                }
                if (canPop) {
                    stack1.pop();
                    added.add(e.owner);
                    children.remove(e.owner);
                }
            }
            children.clear();
        }
        return added;
    }

    public AnnotationsDirectoryItem putAnnotationDirectoryItem() {
        AnnotationsDirectoryItem aDirectoryItem = new AnnotationsDirectoryItem();
        annotationsDirectoryItems.add(aDirectoryItem);
        return aDirectoryItem;
    }

    public AnnotationItem uniqAnnotationItem(AnnotationItem key) {
        AnnotationItem v = annotationItems.get(key);
        if (v == null) {
            annotationItems.put(key, key);
            return key;
        }
        return v;
    }

    public ClassDefItem putClassDefItem(int accessFlag, String name, String superClass, String[] itfClass) {
        TypeIdItem type = uniqType(name);
        if (classDefs.containsKey(type)) {
            throw new DexWriteException("dup clz: " + name);
        }
        ClassDefItem classDefItem = new ClassDefItem();
        classDefItem.accessFlags = accessFlag;
        classDefItem.clazz = type;
        if (superClass != null) {
            classDefItem.superclazz = uniqType(superClass);
        }
        if (itfClass != null && itfClass.length > 0) {
            classDefItem.interfaces = putTypeList(Arrays.asList(itfClass));
        }
        classDefs.put(type, classDefItem);
        return classDefItem;
    }

    public FieldIdItem uniqField(Field field) {
        return uniqField(field.getOwner(), field.getName(), field.getType());
    }

    public FieldIdItem uniqField(String owner, String name, String type) {
        FieldIdItem key = new FieldIdItem(uniqType(owner), uniqString(name), uniqType(type));
        FieldIdItem item = fields.get(key);
        if (item != null) {
            return item;
        }
        fields.put(key, key);
        return key;
    }

    public MethodIdItem uniqMethod(Method method) {
        MethodIdItem key = new MethodIdItem(uniqType(method.getOwner()), uniqString(method.getName()), uniqProto(method));
        return uniqMethod(key);
    }

    public MethodIdItem uniqMethod(String owner, String name, String parms[], String ret) {
        MethodIdItem key = new MethodIdItem(uniqType(owner), uniqString(name), uniqProto(parms, ret));
        return uniqMethod(key);
    }

    public MethodIdItem uniqMethod(MethodIdItem key) {
        MethodIdItem item = methods.get(key);
        if (item != null) {
            return item;
        }
        methods.put(key, key);
        return key;
    }

    private ProtoIdItem uniqProto(Method method) {
        return uniqProto(method.getParameterTypes(), method.getReturnType());
    }

    public ProtoIdItem uniqProto(String[] types, String retDesc) {
        TypeIdItem ret = uniqType(retDesc);
        StringIdItem shorty = uniqString(buildShorty(retDesc, types));
        TypeListItem params = putTypeList(types);
        ProtoIdItem key = new ProtoIdItem(params, ret, shorty);
        ProtoIdItem item = protos.get(key);
        if (item != null) {
            return item;
        } else {
            protos.put(key, key);
            return key;
        }
    }

    public StringIdItem uniqString(String data) {
        StringIdItem item = strings.get(data);
        if (item != null) {
            return item;
        }
        StringDataItem sd = new StringDataItem(data);
        stringDatas.add(sd);
        item = new StringIdItem(sd);
        strings.put(data, item);
        return item;
    }

    public TypeIdItem uniqType(String type) {
        TypeIdItem item = types.get(type);
        if (item != null) {
            return item;
        }
        item = new TypeIdItem(uniqString(type));
        types.put(type, item);
        return item;
    }

    private TypeListItem putTypeList(String... subList) {
        if (subList.length == 0) {
            return ZERO_SIZE_TYPE_LIST;
        }
        List<TypeIdItem> idItems = new ArrayList<>(subList.length);
        for (String s : subList) {
            idItems.add(uniqType(s));
        }
        TypeListItem key = new TypeListItem(idItems);
        TypeListItem item = typeLists.get(key);
        if (item != null) {
            return item;
        }
        typeLists.put(key, key);
        return key;
    }

    private static final TypeListItem ZERO_SIZE_TYPE_LIST = new TypeListItem(Collections.EMPTY_LIST);
    static {
        // make sure the offset is 0
        ZERO_SIZE_TYPE_LIST.offset = 0;
    }

    private TypeListItem putTypeList(List<String> subList) {
        if (subList.size() == 0) {
            return ZERO_SIZE_TYPE_LIST;
        }
        List<TypeIdItem> idItems = new ArrayList<>(subList.size());
        for (String s : subList) {
            idItems.add(uniqType(s));
        }
        TypeListItem key = new TypeListItem(idItems);
        TypeListItem item = typeLists.get(key);
        if (item != null) {
            return item;
        }
        typeLists.put(key, key);
        return key;
    }

    public ClassDataItem addClassDataItem(ClassDataItem dataItem) {
        classDataItems.add(dataItem);
        return dataItem;
    }

    // TODO change EncodedArrayItem to uniq
    public EncodedArrayItem putEnCodedArrayItem() {
        EncodedArrayItem arrayItem = new EncodedArrayItem();
        encodedArrayItems.add(arrayItem);
        return arrayItem;
    }

    public AnnotationSetItem uniqAnnotationSetItem(AnnotationSetItem key) {
        List<AnnotationItem> copy = new ArrayList<AnnotationItem>(key.annotations);
        key.annotations.clear();
        for (AnnotationItem annotationItem : copy) {
            key.annotations.add(uniqAnnotationItem(annotationItem));
        }
        AnnotationSetItem v = annotationSetItems.get(key);
        if (v != null) {
            return v;
        }
        annotationSetItems.put(key, key);
        return key;
    }

    public AnnotationSetRefListItem uniqAnnotationSetRefListItem(AnnotationSetRefListItem key) {
        for (int i = 0; i < key.annotationSets.length; i++) {
            AnnotationSetItem anno = key.annotationSets[i];
            if (anno != null) {
                key.annotationSets[i] = uniqAnnotationSetItem(anno);
            }
        }
        AnnotationSetRefListItem v = annotationSetRefListItems.get(key);
        if (v == null) {
            annotationSetRefListItems.put(key, key);
            return key;
        }
        return v;
    }

    public void addCodeItem(CodeItem code) {
        codeItems.add(code);
    }
}
