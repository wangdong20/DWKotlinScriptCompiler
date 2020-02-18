package com.github.wangdong20.kotlinscriptcompiler.token;

public class BooleanToken implements Token {
    private final boolean value;

    public BooleanToken(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
