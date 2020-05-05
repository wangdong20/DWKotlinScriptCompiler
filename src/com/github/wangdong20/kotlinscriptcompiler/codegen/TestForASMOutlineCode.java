package com.github.wangdong20.kotlinscriptcompiler.codegen;

/**
 * This class only for see test ASM outline code in Intelij plugin.
 * It has nothing related to the project, it can be ignored, modified to see any kind of ASM code you want.
 */
public class TestForASMOutlineCode {

    public TestForASMOutlineCode() {
//        String[] s = new String[10];
//        s[0] = "a";
//        System.out.println(s[1]);
//        int[] a = new int[10];
//        a[0] = 1;
//        a[1]++;
        boolean[] b = new boolean[4];
        b[2] = false;
        boolean flag = false;
//        System.out.println(flag);
//        int x = 1;
//        int y = 2;
//        a[x + y] = 3;
//        int z = 1 + 2 * 3 / (2 + 1);
//        System.out.println(z);
        String s;
        int k;
        if(!b[0] && flag) {
            System.out.println("Yes!");
        }
        s = "";
        int a = 10;
        k = 1;
        k += a;
        System.out.println(s);
        System.out.println(k);
    }

}
