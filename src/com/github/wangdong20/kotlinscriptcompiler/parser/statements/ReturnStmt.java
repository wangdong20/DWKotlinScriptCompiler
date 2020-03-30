package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;

public class ReturnStmt implements Stmt {
    private final Exp returnExp;

    public ReturnStmt(Exp returnExp) {
        this.returnExp = returnExp;
    }

    public Exp getReturnExp() {
        return returnExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ReturnStmt) {
            if(((ReturnStmt) obj).getReturnExp().equals(returnExp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ReturnStmt{" +
                "returnExp=" + returnExp +
                '}';
    }
}
