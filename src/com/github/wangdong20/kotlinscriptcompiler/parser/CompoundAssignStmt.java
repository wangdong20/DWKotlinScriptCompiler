package com.github.wangdong20.kotlinscriptcompiler.parser;

public class CompoundAssignStmt implements Stmt {
    private final Exp expression;
    private final VariableExp variable;
    private final CompoundAssignOp op;

    public CompoundAssignStmt(Exp expression, VariableExp variable, CompoundAssignOp op) {
        this.expression = expression;
        this.variable = variable;
        this.op = op;
    }
}
