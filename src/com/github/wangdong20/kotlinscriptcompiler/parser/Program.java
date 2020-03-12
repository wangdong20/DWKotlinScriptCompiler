package com.github.wangdong20.kotlinscriptcompiler.parser;

import java.util.List;

public class Program {
    private final List<Stmt> stmtList;

    public Program(List<Stmt> stmtList) {
        this.stmtList = stmtList;
    }
}
