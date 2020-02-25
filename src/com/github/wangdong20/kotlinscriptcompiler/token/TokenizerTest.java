package com.github.wangdong20.kotlinscriptcompiler.token;

import java.util.ArrayList;
import java.util.List;

public class TokenizerTest {

    // input: "("
    // output: BracketsToken.TK_LPAREN
    public static void testLeftParen() throws TokenizerException {
        testTokenizes("(", BracketsToken.TK_LPAREN);
    }

    // input: ")"
    // output: BracketsToken.TK_RPAREN
    public static void testRightParen() throws TokenizerException {
        testTokenizes(")", BracketsToken.TK_RPAREN);
    }

    public static void testTokenizes(final String input,
                                     final Token... expectedTokens) throws TokenizerException {
        final Tokenizer tokenizer = new Tokenizer(input);
        final List<Token> receivedTokens = tokenizer.tokenize();
        final List<Token> expected = new ArrayList<Token>();
        for(final Token token : expectedTokens) {
            expected.add(token);
        }
        System.out.println(expected);
        System.out.println(receivedTokens);
        assert(receivedTokens.equals(expected));
    }

    public static void testVariableAlone() throws TokenizerException {
        testTokenizes("x", new VariableToken("x"));
    }

    public static void testVariableWithWhitespaceBefore() throws TokenizerException {
        testTokenizes(" x", new VariableToken("x"));
    }

    public static void testVariableWithWhitespaceAfter() throws TokenizerException {
        testTokenizes("x ", new VariableToken("x"));
    }

    public static void testVariableContainKeywords() throws TokenizerException {
        testTokenizes("ifelse", new VariableToken("ifelse"));
    }

    public static void testIfElseKeywords() throws TokenizerException {
        testTokenizes("if else", KeywordToken.TK_IF, KeywordToken.TK_ELSE);
    }

    public static void testIntToken() throws TokenizerException {
        testTokenizes("-123", new IntToken(-123));
    }

    public static void testForLoopToken() throws TokenizerException {
        testTokenizes("for(i in a) {print(i)}", KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("i"), KeywordToken.TK_IN, new VariableToken("a"),
                BracketsToken.TK_RPAREN, BracketsToken.TK_LCURLY, KeywordToken.TK_PRINT,
                BracketsToken.TK_LPAREN, new VariableToken("i"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_RCURLY);
    }

    public static void testOperator() throws TokenizerException {
        testTokenizes("var a = 1; a += 2; a++; a--; a*=2; a /= 2; a = a % 2; if(a > 1) return true;",
            KeywordToken.TK_VAR, new VariableToken("a"), BinopToken.TK_EQUAL, new IntToken(1), SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), BinopToken.TK_PLUS_EQUAL, new IntToken(2), SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), UnopToken.TK_PLUS_PLUS, SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), UnopToken.TK_MINUS_MINUS, SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), BinopToken.TK_MULTIPLY_EQUAL, new IntToken(2), SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), BinopToken.TK_DIVIDE_EQUAL, new IntToken(2), SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), BinopToken.TK_EQUAL, new VariableToken("a"),
                BinopToken.TK_MOD, new IntToken(2), SymbolToken.TK_SEMICOLON,
                KeywordToken.TK_IF, BracketsToken.TK_LPAREN, new VariableToken("a"), BinopToken.TK_GREATER_THAN,
                new IntToken(1), BracketsToken.TK_RPAREN, KeywordToken.TK_RETURN, new VariableToken("true"), SymbolToken.TK_SEMICOLON);
    }

    public static void main(String[] args) throws TokenizerException {
        // write your code here
//        testLeftParen();
//        testRightParen();
//        testVariableAlone();
//        testVariableWithWhitespaceBefore();
//        testVariableWithWhitespaceAfter();
//        testVariableContainKeywords();
//        testIfElseKeywords();
//        testIntToken();
//        testForLoopToken();
        testOperator();
    }

}
