package com.github.wangdong20.kotlinscriptcompiler.token;

public enum SymbolToken implements Token {
    TK_COLON,       // :
    TK_SEMICOLON,   // ;
    TK_LINE_BREAK,  // \n
    TK_COMMA,       // ,
    TK_ARROW,        // ->
    TK_DOT,         // .
    TK_DOT_DOT,      // ..
    TK_DOLLAR_MARK  // $
}
