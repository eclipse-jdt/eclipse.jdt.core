package test0497;

public abstract class A {
    public Object foo1() { return null; }
    public Object foo2() throws IllegalArgumentException { return null; }
    public Object foo3()[][] { return null; }
    public Object foo4()[][] throws IllegalArgumentException { return null; }
    public Object foo5()[][] { return null; }
    public Object foo6(int i)[][] throws IllegalArgumentException { return null; }
    public Object foo7(int i)[][] { return null; }
    public Object[] foo8(int i)[][] { return null; }
}