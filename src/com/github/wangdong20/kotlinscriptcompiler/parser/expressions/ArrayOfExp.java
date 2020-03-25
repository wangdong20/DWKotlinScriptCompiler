package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

import java.util.List;

public class ArrayOfExp implements Exp {
    private final List<Exp> expList;

    public ArrayOfExp(List<Exp> expList) {
        this.expList = expList;
    }

    public List<Exp> getExpList() {
        return expList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ArrayOfExp) {
            if(((ArrayOfExp)obj).getExpList().equals(expList)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ArrayOfExp{" +
                "expList=" + expList +
                '}';
    }
}
