package com.mygdx.game;

public class test {

    public static void main(String[] args) {
        int n = 0;
        System.out.println(a(n));
        System.out.println("Should be 45");
        n = 1;
        System.out.println(a(n));
        System.out.println("Should be 44");
        n = 10;
        System.out.println(a(n));
        System.out.println("Should be 35");
        n = 30;
        System.out.println(a(n));
        System.out.println("Should be 15");
        n = 45;
        System.out.println(a(n));
        System.out.println("Should be 0");
        n = 90;
        System.out.println(a(n));
        System.out.println("Should be 45");
        n = 179;
        System.out.println(a(n));
        System.out.println("Should be 44");
        n = 181;
        System.out.println(a(n));
        System.out.println("Should be 44");
        n = 180+44;
        System.out.println(a(n));
        System.out.println("Should be 1");
        n = -1;
        System.out.println(a(n));
        System.out.println("Should be 44");
        n = -46;
        System.out.println(a(n));
        System.out.println("Should be 1");
        n = -45;
        System.out.println(a(n));
        System.out.println("Should be 0");

    }

    private static int a(int i) {
        int n = Math.abs(i) % 90;
        return 45 - Math.min(n, 90 - n);
    }
}
