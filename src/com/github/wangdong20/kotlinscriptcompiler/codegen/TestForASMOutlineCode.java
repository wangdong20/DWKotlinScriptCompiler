package com.github.wangdong20.kotlinscriptcompiler.codegen;

public class TestForASMOutlineCode {

    public TestForASMOutlineCode() {
        String[] s = new String[10];
        s[0] = "a";
        System.out.println(s[1]);
        int[] a = new int[10];
        a[0] = 1;
        a[1]++;
        boolean[] b = new boolean[4];
        b[2] = false;
        boolean flag = false;
        System.out.println(flag);
        int x = 1;
        int y = 2;
        a[x + y] = 3;
    }

}
