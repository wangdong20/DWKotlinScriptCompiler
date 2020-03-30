package com.github.wangdong20.kotlinscriptcompiler.typechecker;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.BooleanExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Exp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.IntExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.StringExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;

public class Typechecker {
    public static Type typeOf(final Exp e) throws IllTypedException {
        if(e instanceof IntExp) {
            return BasicType.TYPE_INT;
        } else if(e instanceof BooleanExp) {
            return BasicType.TYPE_BOOLEAN;
        } else if(e instanceof StringExp) {
            return BasicType.TYPE_STRING;
        } else {
            throw new IllTypedException("Unknown type!");
        }
    }
}
