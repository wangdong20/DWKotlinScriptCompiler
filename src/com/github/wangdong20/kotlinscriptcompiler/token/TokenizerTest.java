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

    public static void main(String[] args) throws TokenizerException {
        // write your code here
        testLeftParen();
        testRightParen();
        testVariableAlone();
        testVariableWithWhitespaceBefore();
        testVariableWithWhitespaceAfter();
        testVariableContainKeywords();
        testIfElseKeywords();
    }

}
