package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;

import java.util.List;

public class WhileStmt implements Stmt {
    private final Exp condition;
    private final BlockStmt blockStmt;

    public WhileStmt(Exp condition, BlockStmt blockStmt) {
        this.condition = condition;
        this.blockStmt = blockStmt;
    }

    public Exp getCondition() {
        return condition;
    }

    public BlockStmt getBlockStmt() {
        return blockStmt;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WhileStmt) {
            if(((WhileStmt) obj).getCondition().equals(condition) && ((WhileStmt) obj).getBlockStmt().equals(blockStmt)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "WhileStmt{" +
                "condition=" + condition +
                ", blockStmt=" + blockStmt +
                '}';
    }
}
