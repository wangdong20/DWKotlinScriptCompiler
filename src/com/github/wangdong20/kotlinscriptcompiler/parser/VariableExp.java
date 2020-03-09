package com.github.wangdong20.kotlinscriptcompiler.parser;


public class VariableExp implements Exp {
    private final String name;

    public VariableExp(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VariableExp) {
            if(((VariableExp) obj).getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "VariableExp{" +
                "name='" + name + '\'' +
                '}';
    }
}
