package com.github.wangdong20.kotlinscriptcompiler.parser;

public class AdditiveExp implements Exp {
    private final Exp left;
    private final Exp right;
    private final AdditiveOp op;

    public AdditiveExp(Exp left, Exp right, AdditiveOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }
}
