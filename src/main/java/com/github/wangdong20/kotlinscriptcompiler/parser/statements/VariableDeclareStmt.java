package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.VariableExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;

public class VariableDeclareStmt implements Stmt {
    private final VariableExp variableExp;
    private final Type type;
    private final boolean readOnly;

    public VariableDeclareStmt(VariableExp variableExp, Type type, boolean readOnly) {
        this.variableExp = variableExp;
        this.type = type;
        this.readOnly = readOnly;
    }

    public VariableExp getVariableExp() {
        return variableExp;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VariableDeclareStmt) {
            if(((VariableDeclareStmt) obj).getVariableExp().equals(variableExp)
                    && ((VariableDeclareStmt) obj).isReadOnly() == readOnly) {
                if((((VariableDeclareStmt) obj).getType() == null && type == null) || ((VariableDeclareStmt) obj).getType().equals(type))
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "VariableDeclareStmt{" +
                "variableExp=" + variableExp +
                ", type=" + type +
                ", readOnly=" + readOnly +
                '}';
    }
}
