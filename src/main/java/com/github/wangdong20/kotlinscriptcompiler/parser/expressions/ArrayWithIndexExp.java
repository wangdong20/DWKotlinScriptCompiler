package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;


public class ArrayWithIndexExp implements Exp, Variable {
    private final VariableExp variableExp;
    private final Exp indexExp;

    public ArrayWithIndexExp(VariableExp variableExp, Exp indexExp) {
        this.variableExp = variableExp;
        this.indexExp = indexExp;
    }

    public VariableExp getVariableExp() {
        return variableExp;
    }

    public Exp getIndexExp() {
        return indexExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ArrayWithIndexExp) {
            if(((ArrayWithIndexExp) obj).getVariableExp().equals(variableExp) &&
                ((ArrayWithIndexExp) obj).getIndexExp().equals(indexExp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ArrayWithIndexExp{" +
                "variableExp=" + variableExp +
                ", indexExp=" + indexExp +
                '}';
    }
}
