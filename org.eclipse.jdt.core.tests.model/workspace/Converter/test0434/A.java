package test0434;

class C {
  private int fCoo;
}

public class A extends C {
  public void goo(C c) {
    C.fCoo= 1;
  }
}