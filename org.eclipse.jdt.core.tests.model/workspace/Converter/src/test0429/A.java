package test0429;

class C {
  private int fCoo;
}

public class A extends C {
  public static void goo(C c) {
    c.fCoo= 1;
  }
}