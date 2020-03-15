package com.github.wangdong20.kotlinscriptcompiler.parser;

public class BiLogicalExp implements Exp {
    private final Exp left;
    private final Exp right;
    private final BiLogicalOp op;

    public BiLogicalOp getOp() {
        return op;
    }

    public Exp getLeft() {
        return left;
    }

    public Exp getRight() {
        return right;
    }

    public BiLogicalExp(BooleanExp left, BooleanExp right, BiLogicalOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BiLogicalExp) {
            if(((BiLogicalExp) obj).getLeft().equals(left) && ((BiLogicalExp) obj).getOp() == op
                && ((BiLogicalExp) obj).getRight().equals(right)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BiLogicalExp{" +
                "left=" + left +
                ", right=" + right +
                ", op=" + op +
                '}';
    }
}
