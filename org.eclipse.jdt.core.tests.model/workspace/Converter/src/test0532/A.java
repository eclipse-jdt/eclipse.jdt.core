package test0532;

public class A {
  int field = 4;
  static int i = bar();
  
  {
  	System.out.println();
  }
  
  static {
  	i = 5;
  }

  void foo(int j, int k) {
     j = k;
   }

  int goo() {
    field= 1;
    return;
   }
   
  static int bar() {
  	return 0;
  }
}