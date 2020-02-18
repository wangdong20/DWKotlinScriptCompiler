package com.github.wangdong20.kotlinscriptcompiler.token;

public class VariableToken implements Token {
    private final String name;

    public VariableToken(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
