package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import java.util.List;

public class BlockStmt implements Stmt {
    private final List<Stmt> stmtList;

    public BlockStmt(List<Stmt> stmtList) {
        this.stmtList = stmtList;
    }

    public List<Stmt> getStmtList() {
        return stmtList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BlockStmt) {
            if((((BlockStmt) obj).getStmtList() == null && stmtList == null) || ((BlockStmt) obj).getStmtList().equals(stmtList)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BlockStmt{" +
                "stmtList=" + stmtList +
                '}';
    }
}
