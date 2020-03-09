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

    public Exp getCondition() {
        return condition;
    }

    public Exp getTrueBranch() {
        return trueBranch;
    }

    public Exp getFalseBranch() {
        return falseBranch;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IfExp) {
            if(((IfExp) obj).getCondition().equals(condition) &&
                    ((IfExp) obj).getTrueBranch().equals(trueBranch) &&
                    ((IfExp) obj).getFalseBranch().equals(falseBranch)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "IfExp{" +
                "condition=" + condition +
                ", trueBranch=" + trueBranch +
                ", falseBranch=" + falseBranch +
                '}';
    }
}
