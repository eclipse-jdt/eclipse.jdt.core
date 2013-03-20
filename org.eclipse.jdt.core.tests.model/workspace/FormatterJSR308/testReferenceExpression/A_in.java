/**
 * Test
 *
 */
package p1.p2.test;

import java.io.Serializable;
import java.io.IOException;

public abstract class A extends java.lang.Object implements Runnable, Cloneable, Serializable {
	public void run() {
	}

	private static final   Functionalish<List<Integer>, Integer> WHATEVER = List< Integer > :: size ;
	
	public void referenceExpression() {
		ImaginableFunction<String, Integer> func = Klazz
				::  method; 
	}	

}