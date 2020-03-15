package com.github.wangdong20.kotlinscriptcompiler.parser;

public class ComparableExp implements Exp {
    private final Exp left;
    private final Exp right;
    private final ComparableOp op;

    public ComparableExp(Exp left, Exp right, ComparableOp op) {
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

    public ComparableOp getOp() {
        return op;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ComparableExp) {
            if(((ComparableExp) obj).getLeft().equals(left) && ((ComparableExp) obj).getOp().equals(op)
                && ((ComparableExp) obj).getRight().equals(right)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ComparableExp{" +
                "left=" + left +
                ", right=" + right +
                ", op=" + op +
                '}';
    }
}
