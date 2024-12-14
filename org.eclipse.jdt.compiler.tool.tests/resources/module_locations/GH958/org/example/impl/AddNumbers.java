package org.example.impl;

import org.example.adder.Adder;

public class AddNumbers {
    public static void main(String[] args) {
        int a = 123;
        int b = 456;
        System.out.println(new Adder().add(a, b));
    }
}
