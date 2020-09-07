package com.thftgr;

public class gen {
    public static void main(String[] args) {
        Thread[] threads = new Thread[100];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() ->{System.out.println("asdasd");});
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        System.out.println("asdasdaddddddddddddddd");
    }
}
