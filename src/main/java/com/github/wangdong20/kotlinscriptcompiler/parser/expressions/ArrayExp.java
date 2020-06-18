package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

/**
 * ArrayExp initial an array with ArrayExp, for instance var b = Array(10, {i -> "s" + i * 2})
 */
public class ArrayExp implements Exp {
    private final Exp size;
    private final LambdaExp lambdaExp;

    public ArrayExp(Exp size, LambdaExp lambdaExp) {
        this.size = size;
        this.lambdaExp = lambdaExp;
    }

    public Exp getSize() {
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
