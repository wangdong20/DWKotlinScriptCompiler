package com.github.wangdong20.kotlinscriptcompiler.codegen;

import com.github.wangdong20.kotlinscriptcompiler.parser.statements.FunctionDeclareStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.*;

public class Descriptor {

    public static String toDescriptorString(Type type) throws CodeGeneratorException {
        if(type instanceof BasicType) {
            switch ((BasicType) type) {
                case TYPE_INT:
                    return "I";
                case TYPE_BOOLEAN:
                    return "Z";
                case TYPE_STRING:
                    return "Ljava/lang/String;";
                case TYPE_UNIT:
                    return "V";
                case TYPE_ANY:
                    return "Ljava/lang/Object;";
                default:
                    throw new CodeGeneratorException("Unknown type " + type);
            }
        } else if(type instanceof TypeArray) {
            switch (((TypeArray)type).getBasicType()) {
                case TYPE_INT:
                    return "[I";
                case TYPE_BOOLEAN:
                    return "[Z";
                case TYPE_STRING:
                    return "[Ljava/lang/String;";
                case TYPE_UNIT:
                    return "[V";
                case TYPE_ANY:
                    return "[Ljava/lang/Object;";
                default:
                    throw new CodeGeneratorException("Unknown type " + type);
            }
        } else if(type instanceof TypeMutableList) {
            return "Ljava/util/List;";
        } else if(type instanceof TypeHighOrderFunction) {
            // return ? don't know how to implement high order function
            return null;
        } else {
            throw new CodeGeneratorException("Unknown type " + type);
        }
    }

    private static String toDescriptorString(FunctionDeclareStmt functionDeclareStmt) throws CodeGeneratorException {
        StringBuilder result = new StringBuilder();
        result.append("(");
        for(Type type : functionDeclareStmt.getParameterList().values()) {
            result.append(toDescriptorString(type));
        }
        result.append(")");
        result.append(toDescriptorString(functionDeclareStmt.getReturnType()));
        return result.toString();
    }

}
