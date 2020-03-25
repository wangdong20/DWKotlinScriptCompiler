package com.github.wangdong20.kotlinscriptcompiler.parser.type;

import java.util.List;

public class TypeHighOrderFunction implements Type {
    private final List<Type> parameterList;
    private final Type returnType;

    public TypeHighOrderFunction(List<Type> parameterList, Type returnType) {
        this.parameterList = parameterList;
        this.returnType = returnType;
    }

    public List<Type> getParameterList() {
        return parameterList;
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  TypeHighOrderFunction) {
            if(((TypeHighOrderFunction)obj).getParameterList().equals(parameterList) &&
                    ((TypeHighOrderFunction)obj).getReturnType().equals(returnType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TypeHighOrderFunction{" +
                "parameterList=" + parameterList +
                ", returnType=" + returnType +
                '}';
    }
}
