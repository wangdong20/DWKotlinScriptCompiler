package com.github.wangdong20.kotlinscriptcompiler.parser;

public class AssignStmt implements Stmt {
    private final Exp expression;
    private final VariableExp variable;

    public Exp getExpression() {
        return expression;
    }

    public VariableExp getVariable() {
        return variable;
    }

    public AssignStmt(Exp expression, VariableExp variable) {
        this.expression = expression;
        this.variable = variable;
    }
}
