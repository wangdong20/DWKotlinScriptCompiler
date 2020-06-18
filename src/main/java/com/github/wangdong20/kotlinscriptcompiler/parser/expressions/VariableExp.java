package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;


public class VariableExp implements Exp, Variable {
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
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "VariableExp{" +
                "name='" + name + '\'' +
                '}';
    }
}
