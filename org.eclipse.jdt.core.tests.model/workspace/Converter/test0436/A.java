package test0436;

class A {
  private class CInner {
  }
}

public class D extends A {
  public void goo() {
    A.CInner a;
  }
}