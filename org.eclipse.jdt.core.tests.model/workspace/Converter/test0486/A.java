package test0486;

public class A {
  int field;
  void foo(int i, int k) {
     i= k;
   }

  int goo() {
     // offset here
    field= 1;
    return;
   }
}