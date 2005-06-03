package test0436;

public class A {
  private class CInner {
  }
}

class D extends A {
  public void goo() {
    A.CInner a;
  }
}