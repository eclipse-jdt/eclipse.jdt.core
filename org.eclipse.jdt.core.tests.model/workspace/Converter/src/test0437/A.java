package test0437;

public class A {
  private class CInner {
  }
}

class D extends A {
  public void goo() {
    CInner a;
  }
}