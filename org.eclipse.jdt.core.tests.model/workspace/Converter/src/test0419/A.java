package test0419;

class C {
  private int fCoo;
}

public class A {
  public static void goo(C c) {
    fCoo= 1;
/*    super.fCoo= 1;
    C.fCoo= 1;
    c.fCoo= 1;*/
  }
}