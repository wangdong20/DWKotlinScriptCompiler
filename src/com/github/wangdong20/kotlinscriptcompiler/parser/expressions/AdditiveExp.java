package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

public class AdditiveExp extends BinaryIntExp {
    private final Exp left;
    private final Exp right;
    private final AdditiveOp op;

    public AdditiveExp(Exp left, Exp right, AdditiveOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Exp getLeft() {
        return left;
    }

    public Exp getRight() {
        return right;
    }

    public AdditiveOp getOp() {
        return op;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AdditiveExp) {
            if(((AdditiveExp) obj).getOp() == op &&
                    ((AdditiveExp) obj).getLeft().equals(left) &&
                    ((AdditiveExp) obj).getRight().equals(right)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "AdditiveExp{" +
                "left=" + left +
                ", right=" + right +
                ", op=" + op +
                '}';
    }
}
