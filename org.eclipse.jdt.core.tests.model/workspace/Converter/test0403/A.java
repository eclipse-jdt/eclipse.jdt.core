package test0403;

public class A{
  void f() throws CloneNotSupportedException {
     A a= new A();
     a.clone();
  }
}