package com.github.wangdong20.kotlinscriptcompiler.parser;

import java.util.List;

public class IfStmt implements Stmt {
    private final Exp condition;
    private final List<Stmt> trueBranch;
    private final List<Stmt> falseBranch;

    public IfStmt(Exp condition, List<Stmt> trueBranch, List<Stmt> falseBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public IfStmt(Exp condition, List<Stmt> trueBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = null;
    }

    public Exp getCondition() {
        return condition;
    }

    public List<Stmt> getTrueBranch() {
        return trueBranch;
    }

    public List<Stmt> getFalseBranch() {
        return falseBranch;
    }
}
