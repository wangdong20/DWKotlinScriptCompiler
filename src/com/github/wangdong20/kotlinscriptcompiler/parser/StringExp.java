package com.github.wangdong20.kotlinscriptcompiler.parser;

import java.util.HashMap;

public class StringExp implements Exp {
    private String strWithoutInterpolation;
    private HashMap<Integer, Exp> interpolationExp;     // Integer is the interpolation expression position in String

    public StringExp(String strWithoutInterpolation, HashMap<Integer, Exp> interpolationExp) {
        this.strWithoutInterpolation = strWithoutInterpolation;
        this.interpolationExp = interpolationExp;
    }

    public String getStrWithoutInterpolation() {
        return strWithoutInterpolation;
    }

    public HashMap<Integer, Exp> getInterpolationExp() {
        return interpolationExp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StringExp) {
            if(((StringExp) obj).getStrWithoutInterpolation().equals(strWithoutInterpolation)) {
                if((((StringExp) obj).getInterpolationExp() == null && interpolationExp == null) ||
                        ((StringExp) obj).getInterpolationExp().equals(interpolationExp))
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "StringExp{" +
                "strWithoutInterpolation='" + strWithoutInterpolation + '\'' +
                ", interpolationExp=" + interpolationExp +
                '}';
    }
}
