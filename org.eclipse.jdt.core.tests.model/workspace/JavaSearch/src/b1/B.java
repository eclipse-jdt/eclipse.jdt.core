package b1;
/* Test case for bug 7987 Field reference search should do lookup in 1.4 mode */
public class B extends A {
  void foo() {
    this.x++;
  }
}
