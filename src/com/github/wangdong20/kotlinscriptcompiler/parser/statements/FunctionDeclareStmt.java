package com.github.wangdong20.kotlinscriptcompiler.parser.statements;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.VariableExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;

import java.util.LinkedHashMap;
import java.util.List;

public class FunctionDeclareStmt implements Stmt {
    private final VariableExp funcName;
    private final Type returnType;
    private final LinkedHashMap<Exp, Type> parameterList;
    private final BlockStmt blockStmt;

    public FunctionDeclareStmt(VariableExp funcName, Type returnType, LinkedHashMap<Exp, Type> parameterList, BlockStmt blockStmt) {
        this.funcName = funcName;
        this.returnType = returnType;
        this.parameterList = parameterList;
        this.blockStmt = blockStmt;
    }

    public VariableExp getFuncName() {
        return funcName;
    }

    public Type getReturnType() {
        return returnType;
    }

    public LinkedHashMap<Exp, Type> getParameterList() {
        return parameterList;
    }

    public BlockStmt getBlockStmt() {
        return blockStmt;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FunctionDeclareStmt) {
            if(((FunctionDeclareStmt)obj).getReturnType().equals(returnType)
                && ((FunctionDeclareStmt)obj).getFuncName().equals(funcName)) {
                if ((((FunctionDeclareStmt)obj).getParameterList() == null && parameterList == null)
                        || ((FunctionDeclareStmt)obj).getParameterList().equals(parameterList)){
                    if((((FunctionDeclareStmt)obj).getBlockStmt() == null && blockStmt == null)
                            || ((FunctionDeclareStmt)obj).getBlockStmt().equals(blockStmt)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "FunctionDeclareStmt{" +
                "funcName=" + funcName +
                ", returnType=" + returnType +
                ", parameterList=" + parameterList +
                ", blockStmt=" + blockStmt +
                '}';
    }
}
