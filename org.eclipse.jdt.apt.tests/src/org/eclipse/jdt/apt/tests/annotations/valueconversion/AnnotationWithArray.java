package org.eclipse.jdt.apt.tests.annotations.valueconversion;

public @interface AnnotationWithArray {
	boolean[] booleans(); 
	byte[] bytes();
	short[] shorts(); 
	int[] ints();
	long[] longs();
	float[] floats();
	double[] doubles();
	char[] chars();
	String str() default "some string";
}
