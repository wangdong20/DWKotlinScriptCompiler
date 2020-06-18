package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;

public class PrintStmt implements Stmt {
    private final Exp value;        // It can be StringExp and VariableExp or anything convert to String

    public PrintStmt(Exp value) {
        this.value = value;
    }

    public Exp getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PrintStmt) {
            if(((PrintStmt) obj).getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "PrintStmt{" +
                "value=" + value +
                '}';
    }
}
