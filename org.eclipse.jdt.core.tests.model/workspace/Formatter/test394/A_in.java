public class Foo {

  void bar() {
    if (true && false || false || true && false) 
      return;
  }
}