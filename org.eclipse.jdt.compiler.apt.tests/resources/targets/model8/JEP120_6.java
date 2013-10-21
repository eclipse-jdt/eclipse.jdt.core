package org.eclipse.jdt.compiler.apt.tests;
import org.eclipse.jdt.compiler.apt.tests.annotations.IFoo;
import org.eclipse.jdt.compiler.apt.tests.annotations.IFooContainer;
@IFoo(1) @IFooContainer({@IFoo(2)})
public class JEP120_6 {
	
}

@IFoo(3) @IFoo(4)
class SubClass extends JEP120_6 {
	
}

@IFoo(5)
class SubClass2 extends JEP120_6 {
	
}

