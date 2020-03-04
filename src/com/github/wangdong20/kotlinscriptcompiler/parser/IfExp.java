package com.github.wangdong20.kotlinscriptcompiler.parser;

public class IfExp implements Exp {
    private final Exp condition;
    private final Exp trueBranch;
    private final Exp falseBranch;

    public IfExp(Exp condition, Exp trueBranch, Exp falseBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public IfExp(Exp condition, Exp trueBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = null;
    }
}
