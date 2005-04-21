package org.eclipse.jdt.internal.compiler.env;


/**
 * This represents class file information about an annotation instance.
 */
public interface IBinaryAnnotation	
{
	public static final IBinaryAnnotation[] NoAnnotation = new IBinaryAnnotation[0];
	public static final IBinaryElementValuePair[] NoMemberValuePair = new IBinaryElementValuePair[0];
	
	/**
	 * @return the fully qualified name of the annotation type.
	 */
	char[] getTypeName();
	
	/**
	 * @return the list of member value pairs of the annotation
	 */
	IBinaryElementValuePair[] getMemberValuePairs();
}
