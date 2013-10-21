package org.eclipse.jdt.compiler.apt.tests;
import org.eclipse.jdt.compiler.apt.tests.annotations.IFoo;
import org.eclipse.jdt.compiler.apt.tests.annotations.IFooContainer;
@IFooContainer({@IFoo(1), @IFoo(2)})
public class JEP120_7 {
	
}

@IFooContainer({@IFoo(1), @IFoo(2)})
class SubClass3 extends JEP120_7 {
	
}