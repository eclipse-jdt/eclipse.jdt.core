package c11;
public class A {}
class A1 extends A {}
class A2 extends A {
    public A2() {}
}
class A3 extends A {
    public A3() {
        super();
    }
}
