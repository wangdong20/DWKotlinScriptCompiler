package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;

import java.util.LinkedHashMap;
import java.util.Objects;

public class LambdaExp implements Exp {
    /**
     * If we do not record type information in parse proces, how can we do type check
     * val a = { i: Int -> i + 1 } in this case, i is record as VariableExp in parse process,
     * but we will lose it type information. Type only support basic type
     */
    private final LinkedHashMap<Exp, Type> parameterList;
    private final Exp returnExp;

    public LambdaExp(LinkedHashMap<Exp, Type> parameterList, Exp returnExp) {
        this.parameterList = parameterList;
        this.returnExp = returnExp;
    }

    public LinkedHashMap<Exp, Type> getParameterList() {
        return parameterList;
    }

    public Exp getReturnExp() {
        return returnExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LambdaExp) {
            if(((LambdaExp)obj).getReturnExp().equals(returnExp)) {
                if ((((LambdaExp)obj).getParameterList() == null && parameterList == null)
                        || ((LambdaExp)obj).getParameterList().equals(parameterList)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterList, returnExp);
    }

    @Override
    public String toString() {
        return "LambdaExp{" +
                "parameterList=" + parameterList +
                ", returnExp=" + returnExp +
                '}';
    }
}
