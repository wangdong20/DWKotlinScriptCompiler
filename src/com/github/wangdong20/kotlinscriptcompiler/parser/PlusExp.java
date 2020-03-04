package com.github.wangdong20.kotlinscriptcompiler.parser;

public class PlusExp implements Exp {
    private final Exp left;
    private final Exp right;

    public PlusExp(Exp left, Exp right) {
        this.left = left;
        this.right = right;
    }
}
