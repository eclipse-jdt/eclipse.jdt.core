package test0437;

class A {
  private class CInner {
  }
}

public class D extends A {
  public void goo() {
    CInner a;
  }
}