package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

/**
 * MutableListExp initial an MutableList with MutableListExp, for instance var b = MutableList(10, {i -> "s" + i * 2})
 */
public class MutableListExp implements Exp {
    private final IntExp size;
    private final LambdaExp lambdaExp;

    public MutableListExp(IntExp size, LambdaExp lambdaExp) {
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
        if(obj instanceof MutableListExp) {
            if(((MutableListExp)obj).getSize().equals(size) && ((MutableListExp)obj).getLambdaExp().equals(lambdaExp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MutableListExp{" +
                "size=" + size +
                ", lambdaExp=" + lambdaExp +
                '}';
    }
}
