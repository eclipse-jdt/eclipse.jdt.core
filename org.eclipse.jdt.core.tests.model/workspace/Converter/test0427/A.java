package test0427;

class C {
  private int fCoo;
}

public class A extends C {
  public static void goo(C c) {
    super.fCoo= 1;
/*    C.fCoo= 1;
    c.fCoo= 1;*/
  }
}