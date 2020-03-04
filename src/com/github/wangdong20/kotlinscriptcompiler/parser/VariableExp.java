package com.github.wangdong20.kotlinscriptcompiler.parser;

public class VariableExp implements Exp {
    private final String name;

    public VariableExp(final String name) {
        this.name = name;
    }
}
