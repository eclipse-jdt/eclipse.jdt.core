package test0103;

enum A {
   ONE, TWO, THREE;
   
   public static int foo(int i) {
	return i;
   }
   public static int foo() {
   		return 0;
   }
   public static int foo(double d) {
		return (int) d;
   }
}