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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AssignStmt) {
            if(((AssignStmt) obj).getVariable().equals(variable) && ((AssignStmt) obj).getExpression().equals(expression)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "AssignStmt{" +
                "expression=" + expression +
                ", variable=" + variable +
                '}';
    }
}
