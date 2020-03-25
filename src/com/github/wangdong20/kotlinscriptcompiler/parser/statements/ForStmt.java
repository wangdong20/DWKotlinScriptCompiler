package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.RangeExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.VariableExp;

import java.util.List;

/**
 * This is for loop statement in Kotlin, suppport for(var in var: Array<T>) {S*} or
 * for(var in var(Int)..var(Int)) {s*}
 */
public class ForStmt implements Stmt {
    private final VariableExp iteratorExp;
    private final VariableExp arrayExp;
    private final RangeExp rangeExp;
    private final List<Stmt> stmtList;

    public ForStmt(VariableExp iteratorExp, VariableExp arrayExp, List<Stmt> stmtList) {
        this.iteratorExp = iteratorExp;
        this.arrayExp = arrayExp;
        this.stmtList = stmtList;
        this.rangeExp = null;
    }

    public ForStmt(VariableExp iteratorExp, RangeExp rangeExp, List<Stmt> stmtList) {
        this.iteratorExp = iteratorExp;
        this.rangeExp = rangeExp;
        this.stmtList = stmtList;
        this.arrayExp = null;
    }

    public VariableExp getIteratorExp() {
        return iteratorExp;
    }

    public VariableExp getArrayExp() {
        return arrayExp;
    }

    public List<Stmt> getStmtList() {
        return stmtList;
    }

    public RangeExp getRangeExp() {
        return rangeExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ForStmt) {
            if(((ForStmt)obj).getIteratorExp().equals(iteratorExp) && ((ForStmt)obj).getStmtList().equals(stmtList)) {
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
                ", stmtList=" + stmtList +
                '}';
    }
}
