package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

public class MultiplicativeExp extends BinaryIntExp {
    private final Exp left;
    private final Exp right;
    private final MultiplicativeOp op;

    public MultiplicativeExp(Exp left, Exp right, MultiplicativeOp op) {
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

    public MultiplicativeOp getOp() {
        return op;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MultiplicativeExp) {
            if(((MultiplicativeExp) obj).getLeft().equals(left) && ((MultiplicativeExp) obj).getOp().equals(op)
                && ((MultiplicativeExp) obj).getRight().equals(right)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MultiplicativeExp{" +
                "left=" + left +
                ", right=" + right +
                ", op=" + op +
                '}';
    }
}
