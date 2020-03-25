package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

import java.util.List;

public class MutableListOfExp implements Exp {
    private final List<Exp> expList;

    public MutableListOfExp(List<Exp> expList) {
        this.expList = expList;
    }

    public List<Exp> getExpList() {
        return expList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MutableListOfExp) {
            if(((MutableListOfExp)obj).getExpList().equals(expList)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MutableListOfExp{" +
                "expList=" + expList +
                '}';
    }
}
