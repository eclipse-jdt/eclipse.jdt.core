package b5;
/* Test case for bug 9642 Search - missing inaccurate type matches */
import x.y.Zork;
public class A {
  {
    Zork[] zork = new Zork[0];
    int i = Zork.foo;
  }

}