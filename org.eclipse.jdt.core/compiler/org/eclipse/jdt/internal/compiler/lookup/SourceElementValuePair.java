package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class SourceElementValuePair implements IElementValuePair 
{
	private final MemberValuePair astPair;
	private Object value = null;
	
	SourceElementValuePair(final MemberValuePair pair)
	{
		this.astPair = pair;
	}
	
	public char[] getMemberName()
	{ return this.astPair.name; }
	
	public MethodBinding getMethodBinding() {
		return this.astPair.binding;
	}
	
	public TypeBinding getType() {
		if(this.astPair.binding == null) return null;
		return this.astPair.binding.returnType;
	}
	
	public Object getValue() {
		if( this.value != null ) return this.value;
		
		final Expression expression = this.astPair.value;
		this.value = getValue(expression);
		return this.value;
	}
	
	static Object getValue(Expression expression)
	{
		if( expression == null ) return null;
		Constant constant = expression.constant;
		// literals would hit this case.
		if( constant != null ) return constant;
			
		if( expression instanceof Annotation )
			return new SourceAnnotation( (Annotation)expression );            

        else if( expression instanceof ArrayInitializer )
        {
            final Expression[] exprs = ((ArrayInitializer)expression).expressions;
			int len = exprs == null ? 0 : exprs.length;
			final Object[] values = new Object[len];
			for( int i=0; i<len; i++ )
				values[i] = getValue(exprs[i]);
			return values;
        }
        else if( expression instanceof ClassLiteralAccess )
        {
            final ClassLiteralAccess classLiteral = (ClassLiteralAccess)expression;
			return classLiteral.targetType;            
        }        
        // something that isn't a compile time constant.
        else
            return null;
	}
}
