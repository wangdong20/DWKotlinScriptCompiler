package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.FunctionInstanceExp;

public class FunctionInstanceStmt implements Stmt {
    private final FunctionInstanceExp functionInstanceExp;

    public FunctionInstanceStmt(FunctionInstanceExp functionInstanceExp) {
        this.functionInstanceExp = functionInstanceExp;
    }

    public FunctionInstanceExp getFunctionInstanceExp() {
        return functionInstanceExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FunctionInstanceStmt) {
            if(((FunctionInstanceStmt) obj).getFunctionInstanceExp().equals(functionInstanceExp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "FunctionInstanceStmt{" +
                "functionInstanceExp=" + functionInstanceExp +
                '}';
    }
}
