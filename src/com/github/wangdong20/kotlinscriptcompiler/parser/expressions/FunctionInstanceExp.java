package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

import java.util.List;

public class FunctionInstanceExp implements Exp {
    private final VariableExp funcName;
    private final List<Exp> parameterList;

    public FunctionInstanceExp(VariableExp funcName, List<Exp> parameterList) {
        this.funcName = funcName;
        this.parameterList = parameterList;
    }

    public VariableExp getFuncName() {
        return funcName;
    }

    public List<Exp> getParameterList() {
        return parameterList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FunctionInstanceExp) {
            if(((FunctionInstanceExp)obj).getFuncName().equals(funcName)) {
                if((((FunctionInstanceExp)obj).getParameterList() == null && parameterList == null)
                        ||((FunctionInstanceExp)obj).getParameterList().equals(parameterList)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "FunctionInstanceExp{" +
                "funcName=" + funcName +
                ", parameterList=" + parameterList +
                '}';
    }
}
