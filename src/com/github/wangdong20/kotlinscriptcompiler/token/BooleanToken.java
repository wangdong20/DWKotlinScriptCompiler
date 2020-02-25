package com.github.wangdong20.kotlinscriptcompiler.token;

public class BooleanToken implements Token {
    private final boolean value;

    public BooleanToken(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  BooleanToken) {
            if(((BooleanToken) obj).getValue() == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BooleanToken{" +
                "value=" + value +
                '}';
    }
}
