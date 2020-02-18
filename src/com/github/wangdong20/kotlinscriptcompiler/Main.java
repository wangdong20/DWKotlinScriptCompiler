package com.github.wangdong20.kotlinscriptcompiler;

import com.github.wangdong20.kotlinscriptcompiler.token.Tokenizer;

public class Main {

    public static void main(String[] args) {
	// write your code here
        char initial[] = {'-', '2', '3', '1'};
        Tokenizer tokenizer = new Tokenizer(initial);
        System.out.println(tokenizer.tryTokenizeInteger().getValue());
    }
}
