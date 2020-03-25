package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

public class ArrayExp implements Exp {
    private final IntExp size;
    private final LambdaExp lambdaExp;

    public ArrayExp(IntExp size, LambdaExp lambdaExp) {
        this.size = size;
        this.lambdaExp = lambdaExp;
    }

    public IntExp getSize() {
        return size;
    }

    public LambdaExp getLambdaExp() {
        return lambdaExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ArrayExp) {
            if(((ArrayExp)obj).getSize().equals(size) && ((ArrayExp)obj).getLambdaExp().equals(lambdaExp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ArrayExp{" +
                "size=" + size +
                ", lambdaExp=" + lambdaExp +
                '}';
    }
}
