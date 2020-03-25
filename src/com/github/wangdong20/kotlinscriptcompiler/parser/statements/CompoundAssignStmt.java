package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.VariableExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;

public class CompoundAssignStmt implements Stmt {
    private final Exp expression;
    private final VariableExp variable;
    private final CompoundAssignOp op;
    private final Type type;

    public CompoundAssignStmt(Exp expression, VariableExp variable, CompoundAssignOp op) {
        this.expression = expression;
        this.variable = variable;
        this.op = op;
        this.type = null;
    }

    public CompoundAssignStmt(Exp expression, VariableExp variable, CompoundAssignOp op, Type type) {
        this.expression = expression;
        this.variable = variable;
        this.op = op;
        this.type = type;
    }

    public Exp getExpression() {
        return expression;
    }

    public VariableExp getVariable() {
        return variable;
    }

    public CompoundAssignOp getOp() {
        return op;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CompoundAssignStmt) {
            if(((CompoundAssignStmt) obj).getOp() == op &&
                    ((CompoundAssignStmt) obj).getExpression().equals(expression) &&
                    ((CompoundAssignStmt) obj).getVariable().equals(variable)) {
                if((((CompoundAssignStmt) obj).getType() == null && type == null) || ((CompoundAssignStmt) obj).getType().equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompoundAssignStmt{" +
                "expression=" + expression +
                ", variable=" + variable +
                ", op=" + op +
                ", type=" + type +
                '}';
    }
}
