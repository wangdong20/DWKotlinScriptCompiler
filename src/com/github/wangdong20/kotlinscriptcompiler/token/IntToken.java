package com.github.wangdong20.kotlinscriptcompiler.token;

public class IntToken implements Token {
    private final int value;

    public IntToken(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IntToken) {
            if(((IntToken) obj).getValue() == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "IntToken{" +
                "value=" + value +
                '}';
    }
}
