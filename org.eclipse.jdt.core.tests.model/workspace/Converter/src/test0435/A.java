package test0435;

class C {
  private int fCoo;
}

public class A extends C {
  public void goo(C c) {
    c.fCoo= 1;
  }
}