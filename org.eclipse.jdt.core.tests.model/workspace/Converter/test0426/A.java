package test0426;

class C {
  private class CInner {
  }
}

public class A extends C {
  public static void goo() {
    CInner c;
  }
}