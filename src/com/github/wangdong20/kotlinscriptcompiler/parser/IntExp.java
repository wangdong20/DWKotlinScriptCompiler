package com.github.wangdong20.kotlinscriptcompiler.parser;

public class IntExp implements Exp {
    private final int value;

    public IntExp(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IntExp) {
            if(((IntExp) obj).getValue() == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "IntExp{" +
                "value=" + value +
                '}';
    }
}
