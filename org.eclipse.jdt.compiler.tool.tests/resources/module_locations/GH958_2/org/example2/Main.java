package org.example2;

import org.example.regular.Foo;
import org.example.adder.Adder;

public class Main {
	Foo foo;
    public static void main(String[] args) {
        int a = 123;
        int b = 456;
        System.out.println(new Adder().add(a, b));
    }
}
