package test0433;

class C {
  private int fCoo;
}

public class A extends C {
  public void goo(C c) {
    super.fCoo= 1;
  }
}