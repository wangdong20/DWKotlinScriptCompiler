package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

public class BooleanExp implements Exp {
    private final boolean value;

    public BooleanExp(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BooleanExp) {
            if(((BooleanExp) obj).getValue() == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BooleanExp{" +
                "value=" + value +
                '}';
    }
}
