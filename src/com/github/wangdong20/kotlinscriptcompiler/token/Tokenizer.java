package com.github.wangdong20.kotlinscriptcompiler.token;

public class Tokenizer {
    private static char[] input;
    private int inputPos;

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
}
