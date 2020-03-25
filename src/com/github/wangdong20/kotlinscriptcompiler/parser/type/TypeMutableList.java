package com.github.wangdong20.kotlinscriptcompiler.parser.type;

public class TypeMutableList implements Type {
    private final BasicType basicType;

    public TypeMutableList(BasicType basicType) {
        this.basicType = basicType;
    }

    public BasicType getBasicType() {
        return basicType;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  TypeMutableList) {
            if(((TypeMutableList)obj).getBasicType().equals(basicType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TypeMutableList{" +
                "basicType=" + basicType +
                '}';
    }
}
