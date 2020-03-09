package com.github.wangdong20.kotlinscriptcompiler.parser;

import java.util.List;

public class WhileStmt implements Stmt {
    private final Exp condition;
    private final List<Stmt> stmtList;

    public WhileStmt(Exp condition, List<Stmt> stmtList) {
        this.condition = condition;
        this.stmtList = stmtList;
    }
}
