package test0428;

class C {
  private int fCoo;
}

public class A extends C {
  public static void goo(C c) {
    C.fCoo= 1;
//    c.fCoo= 1;
  }
}