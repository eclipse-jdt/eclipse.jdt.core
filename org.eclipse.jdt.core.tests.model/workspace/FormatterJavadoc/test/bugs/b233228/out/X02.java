package test.bugs.b233228;

public class X02 {
	void foo() {
		if (true) {
			if (true) {
				/*
				 * "If we are about to consider an unchecked exception handler,
				 * potential inits may have occured inside the try block that
				 * need to be detected , e.g. try { x = 1; throwSomething();}
				 * catch(Exception e){ x = 2} " "(uncheckedExceptionTypes notNil
				 * and: [uncheckedExceptionTypes at: index]) ifTrue: [catchInits
				 * addPotentialInitializationsFrom: tryInits]."
				 */
			}
		}
	}
}
