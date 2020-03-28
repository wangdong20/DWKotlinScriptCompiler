package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.VariableExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;

/**
 * Compound assignment should not have type, because no int x += 2; only x += 2;
 * So remove type
 */
public class CompoundAssignStmt implements Stmt {
    private final Exp expression;
    private final VariableExp variable;
    private final CompoundAssignOp op;

    public CompoundAssignStmt(Exp expression, VariableExp variable, CompoundAssignOp op) {
        this.expression = expression;
        this.variable = variable;
        this.op = op;
    }

    public CompoundAssignStmt(Exp expression, VariableExp variable, CompoundAssignOp op, Type type) {
        this.expression = expression;
        this.variable = variable;
        this.op = op;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CompoundAssignStmt) {
            if(((CompoundAssignStmt) obj).getOp() == op &&
                    ((CompoundAssignStmt) obj).getExpression().equals(expression) &&
                    ((CompoundAssignStmt) obj).getVariable().equals(variable)) {
                return true;
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
                '}';
    }
}
