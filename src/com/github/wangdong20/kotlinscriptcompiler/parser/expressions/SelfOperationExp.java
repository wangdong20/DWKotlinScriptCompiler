package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

public class SelfOperationExp implements Exp {
    private final Exp variableExp;
    private final SelfOp op;
    private final boolean isPreOrder;   // if true, then ++i else , then i++

    public SelfOperationExp(Exp variableExp, SelfOp op, boolean isPreOrder) {
        this.variableExp = variableExp;
        this.op = op;
        this.isPreOrder = isPreOrder;
    }

    public Exp getVariableExp() {
        return variableExp;
    }

    public SelfOp getOp() {
        return op;
    }

    public boolean getPreOrder() {
        return isPreOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SelfOperationExp) {
            if(((SelfOperationExp)obj).getVariableExp().equals(variableExp)
                && ((SelfOperationExp)obj).getOp().equals(op)
                    && ((SelfOperationExp)obj).getPreOrder() == isPreOrder) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "SelfOperationExp{" +
                "variableExp=" + variableExp +
                ", op=" + op +
                ", isPreOrder=" + isPreOrder +
                '}';
    }
}
