package com.github.wangdong20.kotlinscriptcompiler.parser;

import com.github.wangdong20.kotlinscriptcompiler.parser.statements.Stmt;

import java.util.List;

public class Program {
    private final List<Stmt> stmtList;

    public Program(List<Stmt> stmtList) {
        this.stmtList = stmtList;
    }

    public List<Stmt> getStmtList() {
        return stmtList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Program) {
            if(((Program) obj).getStmtList().equals(stmtList)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Program{" +
                "stmtList=" + stmtList +
                '}';
    }
}
