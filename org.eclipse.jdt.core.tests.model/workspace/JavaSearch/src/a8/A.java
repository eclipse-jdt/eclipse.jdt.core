package a8;
/* Test case for bug 7344 Search - write acces give wrong result */
public class A {
   public A a;
   public int i;
   public void foo(){
      a.i = 25;
   }
}