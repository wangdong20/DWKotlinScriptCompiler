package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;

import java.util.List;

public class WhileStmt implements Stmt {
    private final Exp condition;
    private final List<Stmt> stmtList;

    public WhileStmt(Exp condition, List<Stmt> stmtList) {
        this.condition = condition;
        this.stmtList = stmtList;
    }

    public Exp getCondition() {
        return condition;
    }

    public List<Stmt> getStmtList() {
        return stmtList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WhileStmt) {
            if(((WhileStmt) obj).getCondition().equals(condition) && ((WhileStmt) obj).getStmtList().equals(stmtList)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "WhileStmt{" +
                "condition=" + condition +
                ", stmtList=" + stmtList +
                '}';
    }
}
