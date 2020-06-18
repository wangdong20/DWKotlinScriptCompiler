package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

public class NotExp implements Exp {
    private final Exp value;

    public NotExp(Exp value) {
        this.value = value;
    }

    public Exp getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NotExp) {
            if(((NotExp) obj).getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NotExp{" +
                "value=" + value +
                '}';
    }
}
