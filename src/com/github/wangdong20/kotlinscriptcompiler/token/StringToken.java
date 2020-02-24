package com.github.wangdong20.kotlinscriptcompiler.token;

public class StringToken implements Token {
    private final String value;

    public StringToken(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StringToken) {
            if(((StringToken) obj).getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
