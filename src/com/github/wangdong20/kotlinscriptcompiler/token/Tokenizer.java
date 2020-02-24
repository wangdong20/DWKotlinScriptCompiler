package com.github.wangdong20.kotlinscriptcompiler.token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokenizer {
    private static char[] input;
    private int inputPos;
    private static Map<String, Token> keywordMap;

    static {
        keywordMap = new HashMap<String, Token>();
        keywordMap.put("if", KeywordToken.TK_IF);
        keywordMap.put("else", KeywordToken.TK_ELSE);
        keywordMap.put("break", KeywordToken.TK_BREAK);
        keywordMap.put("continue", KeywordToken.TK_CONTINUE);
        keywordMap.put("while", KeywordToken.TK_WHILE);
        keywordMap.put("for", KeywordToken.TK_FOR);
        keywordMap.put("fun", KeywordToken.TK_FUN);
        keywordMap.put("var", KeywordToken.TK_VAR);
        keywordMap.put("val", KeywordToken.TK_VAL);
        keywordMap.put("in", KeywordToken.TK_IN);
        keywordMap.put("return", KeywordToken.TK_RETURN);
        keywordMap.put("print", KeywordToken.TK_PRINT);
        keywordMap.put("println", KeywordToken.TK_PRINTLN);
    }

    public Tokenizer(final String input) {
        this.input = input.toCharArray();
        this.inputPos = 0;
    }

    public Tokenizer(final char[] input) {
        this.input = input;
        this.inputPos = 0;
    }

    public IntToken tryTokenizeInteger() {
        String digits = "";

        if(inputPos < input.length && input[inputPos] == '-') {
            digits += input[inputPos];
            inputPos++;
        }

        while(inputPos < input.length && Character.isDigit(input[inputPos])) {
            digits += input[inputPos];
            inputPos++;
        }

        if(digits.length() > 0) {
            return new IntToken(Integer.parseInt(digits));
        }
        return null;
    }

    public KeywordToken tryTokenizeIf() {
        if(inputPos + 2 < input.length && input[inputPos] == 'i' &&
            input[inputPos + 1] == 'f') {
            if (input[inputPos + 2] == '(' || input[inputPos + 2] == ' ') {
                inputPos += 2;
                return KeywordToken.TK_IF;
            } else {
                return null;
            }
        } else if(inputPos + 2 >= input.length) {
            inputPos += 2;
            return KeywordToken.TK_IF;
        } else {
            return null;
        }
    }

    public Token tryTokenizeVariableOrKeyword() {
        String letters = "";

        if(inputPos < input.length && Character.isLetter(input[inputPos])) {
            letters += input[inputPos];
            inputPos++;

            while(inputPos < input.length && Character.isLetterOrDigit(input[inputPos])) {
                letters += input[inputPos];
                inputPos++;
            }

            // Now consider all the keyword case
            if(keywordMap.containsKey(letters)) {
                return keywordMap.get(letters);
            } else {
                return new VariableToken(letters);
            }
        } else {
            return null;
        }
    }

    public void skipWhiteSpace() {
        while(inputPos < input.length && Character.isWhitespace(input[inputPos])) {
            inputPos++;
        }
    }

    public List<Token> tokenize() throws TokenizerException {
        List<Token> tokens = new ArrayList<Token>();
        while(inputPos < input.length) {
            skipWhiteSpace();
            tokens.add(tokenizeOne());
        }
        return tokens;
    }

    // assume it's not starting on whitespace
    public Token tokenizeOne() throws TokenizerException {
        Token read = tryTokenizeVariableOrKeyword();
        if(read != null) {
            return read;
        } else {
            read = tryTokenizeInteger();
            if(read != null) {
                return read;
            } else {
                if(inputPos < input.length) {
                    if(input[inputPos] == '(') {
                        inputPos++;
                        return BracketsToken.TK_LPAREN;
                    } else if(input[inputPos] == ')') {
                        inputPos++;
                        return BracketsToken.TK_RPAREN;
                    } else {
                        throw new TokenizerException("Tokenize failed");
                    }
                } else {
                    throw new TokenizerException("Has more input");
                }
            }
        }
    }
}
