package com.github.wangdong20.kotlinscriptcompiler.token;

public enum KeywordToken implements Token {
    TK_IF,
    TK_ELSE,
    TK_WHILE,
    TK_RETURN,
    TK_BREAK,
    TK_CONTINUE,
    TK_FOR,
    TK_IN,
    TK_VAR,     // var
    TK_VAL,     // val
    TK_PRINT,    // print
    TK_PRINTLN,   // println
    TK_FUN,        // fun(function in Kotlin)
    TK_TRUE,        // true
    TK_FALSE        // false
}
