package b7;
/* Test case for bug 16751 Renaming a class doesn't update all references  */
class SuperClass {
  public static final String CONSTANT = "value";
}

class SubClass extends SuperClass {
}

class Test {
  public static void main(String[] arguments) {
    System.out.println(SubClass.CONSTANT);
  }
}
