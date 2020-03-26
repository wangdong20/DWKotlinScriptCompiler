package com.github.wangdong20.kotlinscriptcompiler.token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokenizer {
    private static char[] input;
    private int inputPos;
    private static Map<String, Token> keywordMap;
    private static Map<String, Token> bracketsMap;
    private static Map<String, Token> binopMap;
    private static Map<String, Token> unopMap;
    private static Map<String, Token> symbolMap;
    private static Map<String, Token> typeMap;

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
        keywordMap.put("true", KeywordToken.TK_TRUE);
        keywordMap.put("false", KeywordToken.TK_FALSE);
        keywordMap.put("main", KeywordToken.TK_MAIN);
        keywordMap.put("arrayOf", KeywordToken.TK_ARRAY_OF);
        keywordMap.put("mutableListOf", KeywordToken.TK_MUTABLE_LIST_OF);

        bracketsMap = new HashMap<String, Token>();
        bracketsMap.put("(", BracketsToken.TK_LPAREN);
        bracketsMap.put(")", BracketsToken.TK_RPAREN);
        bracketsMap.put("{", BracketsToken.TK_LCURLY);
        bracketsMap.put("}", BracketsToken.TK_RCURLY);

        // Angle brackets may have conflict with greater, less token, so comment first
//        bracketsMap.put("<", BracketsToken.TK_LANGLE);
//        bracketsMap.put(">", BracketsToken.TK_RANGLE);

        binopMap = new HashMap<String, Token>();
        binopMap.put("+", BinopToken.TK_PLUS);
        binopMap.put("-", BinopToken.TK_MINUS);
        binopMap.put("*", BinopToken.TK_MULTIPLY);
        binopMap.put("/", BinopToken.TK_DIVIDE);
        binopMap.put("%", BinopToken.TK_MOD);
        binopMap.put("+=", BinopToken.TK_PLUS_EQUAL);
        binopMap.put("-=", BinopToken.TK_MINUS_EQUAL);
        binopMap.put("*=", BinopToken.TK_MULTIPLY_EQUAL);
        binopMap.put("/=", BinopToken.TK_DIVIDE_EQUAL);
        binopMap.put("||", BinopToken.TK_OR);
        binopMap.put("&&", BinopToken.TK_AND);
        binopMap.put("!=", BinopToken.TK_NOT_EQUAL);
        binopMap.put(">", BinopToken.TK_GREATER_THAN);
        binopMap.put("<", BinopToken.TK_LESS_THAN);
        binopMap.put("==", BinopToken.TK_EQUAL_EQUAL);
        binopMap.put(">=", BinopToken.TK_GREATER_OR_EQUAL);
        binopMap.put("<=", BinopToken.TK_LESS_OR_EQUAL);
        binopMap.put("=", BinopToken.TK_EQUAL);

        unopMap = new HashMap<String, Token>();
        unopMap.put("!", UnopToken.TK_NOT);
        unopMap.put("++", UnopToken.TK_PLUS_PLUS);
        unopMap.put("--", UnopToken.TK_MINUS_MINUS);

        symbolMap = new HashMap<String, Token>();
        symbolMap.put(":", SymbolToken.TK_COLON);
        symbolMap.put(";", SymbolToken.TK_SEMICOLON);
        symbolMap.put("\n", SymbolToken.TK_LINE_BREAK);
        symbolMap.put("->", SymbolToken.TK_ARROW);
        symbolMap.put(",", SymbolToken.TK_COMMA);
        symbolMap.put(".", SymbolToken.TK_DOT);
        symbolMap.put("..", SymbolToken.TK_DOT_DOT);
        symbolMap.put("$", SymbolToken.TK_DOLLAR_MARK);


        typeMap = new HashMap<String, Token>();
        typeMap.put("Int", TypeToken.TK_TYPE_INT);
        typeMap.put("String", TypeToken.TK_TYPE_STRING);
        typeMap.put("Boolean", TypeToken.TK_TYPE_BOOLEAN);
        typeMap.put("Unit", TypeToken.TK_TYPE_UNIT);
        typeMap.put("Array", TypeToken.TK_ARRAY);
        typeMap.put("MutableList", TypeToken.TK_MUTABLE_LIST);
        typeMap.put("Any", TypeToken.TK_ANY);
        // I think high order function token will be a class not a enum case, so will not add this token
    }

    public Tokenizer(final String input) {
        this.input = input.toCharArray();
        this.inputPos = 0;
    }

    public Tokenizer(final char[] input) {
        this.input = input;
        this.inputPos = 0;
    }

    private IntToken tryTokenizeInteger() {
        String digits = "";

        if(inputPos < input.length && input[inputPos] == '-' && inputPos + 1 < input.length &&
            Character.isDigit(input[inputPos + 1])) {
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

    private Token tryTokenizeVariableOrKeywordOrType() {
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
                if(typeMap.containsKey(letters)) {
                    return typeMap.get(letters);
                } else {
                    return new VariableToken(letters);
                }
            }
        } else {
            return null;
        }
    }

    private Token tryTokenizeBracket() {
        if(inputPos < input.length) {
            String key = Character.toString(input[inputPos]);
            if(bracketsMap.containsKey(key)) {
                inputPos++;
                return bracketsMap.get(key);
            }
        }
        return null;
    }

    // now combine binop and unop together to figure out which token is
    private Token tryTokenizeOp() {
        if(inputPos < input.length) {
            switch (input[inputPos]) {
                case '+':
                case '-':
                    if(inputPos + 1 < input.length && (input[inputPos + 1] == '=' || input[inputPos + 1] == input[inputPos])) {
                        char first = input[inputPos];
                        char second = input[inputPos + 1];
                        String key = Character.toString(first) + second;

                        if(input[inputPos + 1] == '=') {    // +=, -= case
                            inputPos += 2;
                            return binopMap.get(key);
                        } else if(input[inputPos + 1] == input[inputPos]) {  // ++, -- case
                            inputPos += 2;
                            return unopMap.get(key);
                        } else {
                            return null;
                        }

                    } else if((inputPos + 1 < input.length && input[inputPos] != '-' || input[inputPos + 1] != '>') || inputPos + 1 >= input.length) {    // +, - case but exclude ->
                        String key = Character.toString(input[inputPos]);
                        if(binopMap.containsKey(key)) {
                            inputPos += 1;
                            return binopMap.get(key);
                        } else if(unopMap.containsKey(key)) {
                            inputPos += 1;
                            return unopMap.get(key);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                case '!':
                case '=':
                case '>':
                case '<':
                case '*':
                case '/':
                    if(inputPos - 1 >= 0 && input[inputPos - 1] == '-' && input[inputPos] == '>') {
                        return null;
                    }
                    if(inputPos + 1 < input.length && input[inputPos + 1] == '=') { // !=, ==, >=, <=, *=, /= case
                        char first = input[inputPos];
                        char second = input[inputPos + 1];
                        inputPos += 2;
                        return binopMap.get(Character.toString(first) + second);
                    } else {    // !, =, <, >
                        String key = Character.toString(input[inputPos]);
                        if(binopMap.containsKey(key)) {
                            inputPos += 1;
                            return binopMap.get(key);
                        } else if(unopMap.containsKey(key)) {
                            inputPos += 1;
                            return unopMap.get(key);
                        } else {
                            return null;
                        }
                    }
                case '|':
                    if(inputPos + 1 < input.length && input[inputPos + 1] == '|') { // ||
                        inputPos += 2;
                        return binopMap.get("||");
                    } else {
                        return null;
                    }
                case '&':
                    if(inputPos + 1 < input.length && input[inputPos + 1] == '&') { // &&
                        inputPos += 2;
                        return binopMap.get("&&");
                    } else {
                        return null;
                    }
                default:
                    String key = Character.toString(input[inputPos]);
                    if(input[inputPos] != '-') {
                        if (binopMap.containsKey(key)) {
                            inputPos += 1;
                            return binopMap.get(key);
                        } else if (unopMap.containsKey(key)) {
                            inputPos += 1;
                            return unopMap.get(key);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
            }
        } else {
            return null;
        }
    }

    private Token tryTokenizeSymbol() {
        if(inputPos < input.length) {
            switch (input[inputPos]) {
                case '\n':
                    inputPos++;
                    return symbolMap.get("\n");
                case ';':
                    inputPos++;
                    return symbolMap.get(";");
                case ':':
                    inputPos++;
                    return symbolMap.get(":");
                case ',':
                    inputPos++;
                    return symbolMap.get(",");
                case '-':
                    if(inputPos + 1 < input.length && input[inputPos + 1] == '>') {
                        inputPos += 2;
                        return symbolMap.get("->");
                    } else {
                        return null;
                    }
                case '.':
                    if(inputPos + 1 < input.length && input[inputPos + 1] == '.') {
                        inputPos += 2;
                        return symbolMap.get("..");
                    } else {
                        inputPos += 1;
                        return symbolMap.get(".");
                    }
                case '$':
                    inputPos++;
                    return symbolMap.get("$");
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    private Token tryTokenizeString() {
        if(inputPos < input.length) {

            String value = "";

            // This can be the end of string token or start of string token
            if(input[inputPos] == '"') {
                inputPos++;
                boolean isStringToken = false;
                while(inputPos < input.length) {
                    if(input[inputPos] != '"') {
                        value += input[inputPos];
                        inputPos++;
                    } else {
                        isStringToken = true;
                        inputPos++;
                        break;
                    }
                }
                if(isStringToken) {
                    return new StringToken(value);
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }

    private void skipWhiteSpace() {
        while(inputPos < input.length && Character.isWhitespace(input[inputPos]) && input[inputPos] != '\n') {
            inputPos++;
        }
    }

    private void skipComment() {
        if(inputPos < input.length) {
            // "//" comment case
            if(input[inputPos] == '/' && inputPos + 1 < input.length && input[inputPos + 1] == '/') {
                inputPos += 2;
                while(inputPos < input.length && input[inputPos] != '\n') {
                    inputPos++;
                }
            }
            // "/*  */" comment case
            else if(input[inputPos] == '/' && inputPos + 1 < input.length && input[inputPos + 1] == '*') {
                inputPos += 2;
                while(inputPos < input.length) {
                    if(input[inputPos] == '*' && inputPos + 1 < input.length && input[inputPos + 1] == '/') {
                        inputPos += 2;
                        break;
                    } else {
                        inputPos++;
                    }
                }
            }
        }
    }

    public List<Token> tokenize() throws TokenizerException {
        List<Token> tokens = new ArrayList<Token>();
        while(inputPos < input.length) {
            skipWhiteSpace();
            skipComment();
            skipWhiteSpace();
            if(inputPos < input.length) {
                tokens.add(tokenizeOne());
            }
        }
        return tokens;
    }

    // assume it's not starting on whitespace
    private Token tokenizeOne() throws TokenizerException {
        Token read = tryTokenizeString();
        if(read != null) {
            return read;
        } else {
            read = tryTokenizeVariableOrKeywordOrType();
            if (read != null) {
                return read;
            } else {
                read = tryTokenizeInteger();
                if (read != null) {
                    return read;
                } else {
                    read = tryTokenizeBracket();
                    if (read != null) {
                        return read;
                    } else {
                        read = tryTokenizeOp();
                        if(read != null) {
                            return read;
                        } else {
                            read = tryTokenizeSymbol();
                            if(read != null) {
                                return read;
                            } else {
                                throw new TokenizerException("Has more input!");
                            }
                        }
                    }
                }
            }
        }
    }
}
