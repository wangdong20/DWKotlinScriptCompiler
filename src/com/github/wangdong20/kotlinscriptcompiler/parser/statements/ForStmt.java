package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.RangeExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.VariableExp;

/**
 * This is for loop statement in Kotlin, suppport for(var in var: Array<T>) {S*} or
 * for(var in var(Int)..var(Int)) {s*}
 */
public class ForStmt implements Stmt {
    private final VariableExp iteratorExp;
    private final VariableExp arrayExp;
    private final RangeExp rangeExp;
    private final BlockStmt blockStmt;

    public ForStmt(VariableExp iteratorExp, VariableExp arrayExp, BlockStmt blockStmt) {
        this.iteratorExp = iteratorExp;
        this.arrayExp = arrayExp;
        this.blockStmt = blockStmt;
        this.rangeExp = null;
    }

    public ForStmt(VariableExp iteratorExp, RangeExp rangeExp, BlockStmt blockStmt) {
        this.iteratorExp = iteratorExp;
        this.rangeExp = rangeExp;
        this.blockStmt = blockStmt;
        this.arrayExp = null;
    }

    public VariableExp getIteratorExp() {
        return iteratorExp;
    }

    public VariableExp getArrayExp() {
        return arrayExp;
    }

    public BlockStmt getBlockStmt() {
        return blockStmt;
    }

    public RangeExp getRangeExp() {
        return rangeExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ForStmt) {
            if(((ForStmt)obj).getIteratorExp().equals(iteratorExp) && ((ForStmt)obj).getBlockStmt().equals(blockStmt)) {
                if(((ForStmt)obj).getArrayExp() != null && arrayExp != null && ((ForStmt)obj).getArrayExp().equals(arrayExp)) {
                    return true;
                } else if(((ForStmt)obj).getRangeExp() != null && rangeExp != null && ((ForStmt)obj).getRangeExp().equals(rangeExp)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ForStmt{" +
                "iteratorExp=" + iteratorExp +
                ", arrayExp=" + arrayExp +
                ", rangeExp=" + rangeExp +
                ", blockStmt=" + blockStmt +
                '}';
    }
}
