package org.eclipse.jdt.internal.compiler.lookup;

public class BinaryElementValuePair implements IElementValuePair
{
	 //The annotation that directly contains this value pair.	 
	private final BinaryAnnotation anno;
	private final char[] membername;
	private final Object value;
	
	BinaryElementValuePair(final BinaryAnnotation anno, final char[] membername, final Object value)
	{
		this.anno = anno;
		this.membername = membername;
		this.value = value;
	}
	
	public char[] getMemberName()
	{ return membername; }
	
	/**
	 * @return the method binding that defined this member value pair or null
	 *  	   if no such binding exists.
	 */
	public MethodBinding getMethodBinding()
	{
		final ReferenceBinding typeBinding = anno.getAnnotationType();
		if( typeBinding != null ){
			final MethodBinding[] methods = typeBinding.getMethods(this.membername);
			// there should be exactly one since the type is an annotation type.
			if( methods != null && methods.length == 1)
				return methods[0];
		}
		return null;
	}
	
	/**
	 * Convinence method. 
	 * The type will determine the type of objects returned from {@link #getValue()}
	 * @return the type of the member value or null if it cannot be determined
	 * @see #getMethodBinding()
	 * @see #getValue()
	 * 
	 */
	public TypeBinding getType()
	{
		final MethodBinding method = getMethodBinding();
		return method == null ? null : method.returnType;
	}
	
	/**
	 * <br><li>Return {@link TypeBinding} for member value of type {@link java.lang.Class}</li></br>
	 * <br><li>Return {@link org.eclipse.jdt.internal.compiler.impl.Constant} 
	 * for member of primitive type or String</li></br>
	 * <br><li>Return {@link FieldBinding} for enum constant</li></br>
	 * <br><li>Return {@link IAnnotationInstance} for annotation instance</li></br>
	 * <br><li>Return <code>Object[]</code> for member value of array type.
	 * @return the value of this member value pair or null if the value is missing or
	 *         is not a compile-time constant.
	 */
	public Object getValue(){ return this.value; }
}
