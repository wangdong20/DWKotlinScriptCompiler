package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.SelfOperationExp;

public class SelfOperationStmt implements Stmt {
    private final SelfOperationExp selfOperationExp;

    public SelfOperationStmt(SelfOperationExp selfOperationExp) {
        this.selfOperationExp = selfOperationExp;
    }

    public SelfOperationExp getSelfOperationExp() {
        return selfOperationExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SelfOperationStmt) {
            if(((SelfOperationStmt) obj).getSelfOperationExp().equals(selfOperationExp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "SelfOperationStmt{" +
                "selfOperationExp=" + selfOperationExp +
                '}';
    }
}
