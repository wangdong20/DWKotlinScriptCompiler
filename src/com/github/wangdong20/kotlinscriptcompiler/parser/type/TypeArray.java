package com.github.wangdong20.kotlinscriptcompiler.parser.type;

public class TypeArray implements Type {
    private final BasicType basicType;

    public TypeArray(BasicType basicType) {
        this.basicType = basicType;
    }

    public BasicType getBasicType() {
        return basicType;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  TypeArray) {
            if(((TypeArray)obj).getBasicType().equals(basicType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TypeArray{" +
                "basicType=" + basicType +
                '}';
    }
}
