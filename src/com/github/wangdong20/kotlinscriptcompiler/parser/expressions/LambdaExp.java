package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

import java.util.List;

public class LambdaExp implements Exp {
    /**
     * If we do not record type information in parse proces, how can we do type check
     * val a = { i: Int -> i + 1 } in this case, i is record as VariableExp in parse process,
     * but we will lose it type information.
     */
    private final List<Exp> parameterList;
    private final Exp returnExp;

    public LambdaExp(List<Exp> parameterList, Exp returnExp) {
        this.parameterList = parameterList;
        this.returnExp = returnExp;
    }

    public List<Exp> getParameterList() {
        return parameterList;
    }

    public Exp getReturnExp() {
        return returnExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LambdaExp) {
            if(((LambdaExp)obj).getParameterList().equals(parameterList) && ((LambdaExp)obj).getReturnExp().equals(returnExp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "LambdaExp{" +
                "parameterList=" + parameterList +
                ", returnExp=" + returnExp +
                '}';
    }
}
