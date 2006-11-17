public class A {
	public MyClass getMyObject() {
		MyClass o = new MyClass();

		doSomehting(1);

		if (somethingBad)
			throw( new IllegalBlaException("bad bla") );

		return( o );
	}
}
