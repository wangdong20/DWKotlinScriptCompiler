package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

public abstract class BinaryIntExp implements Exp {
    public abstract Exp getLeft();
    public abstract Exp getRight();
}
