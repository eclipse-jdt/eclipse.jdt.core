package stdjavatest;

import junit.framework.*;
import mypackage.MyClass;

public class TestingMyClass extends TestCase {
public void test_method() {
MyClass objA = null;
objA = new MyClass();
objA.method();
}
}