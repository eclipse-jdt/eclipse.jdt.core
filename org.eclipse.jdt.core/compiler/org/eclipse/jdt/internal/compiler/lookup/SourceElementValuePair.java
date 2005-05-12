package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class SourceElementValuePair implements IElementValuePair 
{	
	private final char[] name;	
	private final MethodBinding binding;
	private final Object value;
	
	SourceElementValuePair(final MemberValuePair pair)
	{
		this.name = pair.name;
		this.binding = pair.binding;
		this.value = getValue(pair.value);		
	}
	
	public char[] getMemberName()
	{ return this.name; }
	
	public MethodBinding getMethodBinding() {
		return this.binding;
	}
	
	public TypeBinding getType() {
		if(this.binding == null) return null;
		return this.binding.returnType;
	}
	
	public Object getValue() {
		return this.value;	
	}
	
	static Object getValue(Expression expression)
	{
		if( expression == null ) return null;
		Constant constant = expression.constant;
		// literals would hit this case.
		if( constant != null && constant != Constant.NotAConstant) return constant;
			
		if( expression instanceof Annotation )
			return new SourceAnnotation( (Annotation)expression );      
		else if(expression instanceof Reference){
            FieldBinding fieldBinding = null;
            if(expression instanceof FieldReference )
                fieldBinding = ((FieldReference)expression).fieldBinding();
            else if(expression instanceof NameReference ){
                final Binding binding = ((NameReference)expression).binding;
                if( binding != null && binding.kind() == Binding.FIELD )
                    fieldBinding = (FieldBinding)binding;
            }

            if( fieldBinding != null && (fieldBinding.modifiers & IConstants.AccEnum) > 0 ){
				return fieldBinding;
            }
        }

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
        return null;
	}
}
