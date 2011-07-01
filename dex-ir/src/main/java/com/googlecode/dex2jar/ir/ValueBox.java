package com.googlecode.dex2jar.ir;

public class ValueBox {
    public ValueBox(Value value) {
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }

    public Value value;
}
