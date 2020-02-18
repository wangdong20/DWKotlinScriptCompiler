package com.github.wangdong20.kotlinscriptcompiler.token;

public class IntToken implements Token {
    private final int value;

    public IntToken(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
