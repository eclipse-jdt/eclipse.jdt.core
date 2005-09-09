/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.declaration;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.AnnotationInvocationHandler;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IResolvedAnnotation;
import org.eclipse.jdt.core.dom.IResolvedMemberValuePair;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeLiteral;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.util.SourcePosition;

/**
 * Annotation instance from source.
 */
public class AnnotationMirrorImpl implements AnnotationMirror, EclipseMirrorImpl
{
    /**The ast node that correspond to the annotation.*/
    private final IResolvedAnnotation _domAnnotation;
    private final BaseProcessorEnv _env;
    /** the declaration that is annotated by this annotation or the annotation element declaration
     *  if this is (part of) a default value*/
    private final EclipseDeclarationImpl _annotated;
    
    public AnnotationMirrorImpl(IResolvedAnnotation annotationAstNode, EclipseDeclarationImpl decl, BaseProcessorEnv env)
    {
		_domAnnotation = annotationAstNode;
        _env = env;
        _annotated = decl;
        assert _domAnnotation != null : "annotation node missing."; //$NON-NLS-1$
        assert _annotated   != null : "missing the declaration that is annotated with this annotation."; //$NON-NLS-1$
    }
	
    public AnnotationType getAnnotationType()
    {		
        final ITypeBinding binding = _domAnnotation.getAnnotationType();
        if( binding == null ){
        	final ASTNode node = _annotated.getCompilationUnit().findDeclaringNode(_domAnnotation);
        	String name = ""; //$NON-NLS-1$
        	if( node != null && node instanceof Annotation ){
        		final Name typeNameNode = ((Annotation)node).getTypeName();
        		if( typeNameNode != null )
        			name = typeNameNode.toString();
        	}
        	return Factory.createErrorAnnotationType(name);
        }
        else
        	return (AnnotationType)Factory.createReferenceType(binding, _env);        
    }

    public Map<AnnotationTypeElementDeclaration, AnnotationValue> getElementValues()
    {
		final IResolvedMemberValuePair[] pairs = _domAnnotation.getDeclaredMemberValuePairs();
		if (pairs.length == 0) {
			return Collections.emptyMap();
		}
		
		final Map<AnnotationTypeElementDeclaration, AnnotationValue> result =
			new LinkedHashMap<AnnotationTypeElementDeclaration, AnnotationValue>(pairs.length * 4 / 3 + 1 );
		for( IResolvedMemberValuePair pair : pairs ){
			 final String name = pair.getName();
             if( name == null ) continue;
             IMethodBinding elementMethod = pair.getMemberBinding();            
             if( elementMethod != null ){           
                 final EclipseDeclarationImpl mirrorDecl = Factory.createDeclaration(elementMethod, _env);
                 if( mirrorDecl != null && mirrorDecl.kind() == EclipseMirrorImpl.MirrorKind.ANNOTATION_ELEMENT  )
                 {
                	 final AnnotationTypeElementDeclaration elementDecl = 
                		 (AnnotationTypeElementDeclaration)mirrorDecl;
                	 final AnnotationValue annoValue = 
    					 Factory.createAnnotationMemberValue(pair.getValue(), name, this, _env, elementDecl.getReturnType());
                	 if( annoValue != null )
                		 result.put( elementDecl, annoValue);
                 }
             }
		}
        return result;
    }

    public SourcePosition getPosition()
    {
		if( isFromSource() ){
			final CompilationUnit unit = _annotated.getCompilationUnit();
			final org.eclipse.jdt.core.dom.Annotation annotation = getAstNode();
			if( annotation == null ) return null;
			org.eclipse.jdt.core.dom.ASTNode astNode = annotation.getTypeName();
			if( astNode == null )
				astNode = annotation;
			
			final int offset = astNode.getStartPosition();			
			return new SourcePositionImpl(astNode.getStartPosition(),
										  astNode.getLength(),
						                  unit.lineNumber(offset),
						                  unit.columnNumber(offset), 
						                  _annotated);
		}
		return null;
    }

    public String toString()
    {
		return _domAnnotation.toString();			
    }

    /**
     * @return the type(s) of the member value named <code>membername</code>.
     * If the value is a class literal, then return the type binding corresponding to the type requested.
     * Otherwise, return the type of the expression.
     * If the value is an array initialization, then the type of each of the initialization expresion will
     * be returned. Return null if no match is found.
     */
    public ITypeBinding[] getMemberValueTypeBinding(String membername)
    {
        if( membername == null ) return null;
		final IResolvedMemberValuePair[] declaredPairs = _domAnnotation.getDeclaredMemberValuePairs();
		for( IResolvedMemberValuePair pair : declaredPairs ){			
			if( membername.equals(pair.getName()) ){
				final Object value = pair.getValue();
				return getValueTypeBinding(value, pair.getMemberBinding().getReturnType());
			}
		}
      
        // didn't find it in the ast, check the default values.
        final IMethodBinding binding = getMethodBinding(membername);
		if(binding == null ) return null;
		final Object defaultValue = binding.getDefaultValue();
		if( defaultValue != null )		
			return getValueTypeBinding(defaultValue, binding.getReturnType() );
		else
			return null;		
    }
	
	private ITypeBinding[] getValueTypeBinding(Object value, final ITypeBinding resolvedType)
	{
		if( value == null ) return null;		
		if( resolvedType.isPrimitive() ||  resolvedType.isAnnotation() || value instanceof String )
			return new ITypeBinding[]{ resolvedType };
		else if( resolvedType.isArray() ){
			final Object[] elements = (Object[])value;
			final ITypeBinding[] result = new ITypeBinding[elements.length];
			final ITypeBinding leafType = resolvedType.getElementType();
			for(int i=0, len = elements.length; i<len; i++ ){
				final ITypeBinding[] t = getValueTypeBinding(elements[i], leafType);
				result[i] = t == null ? null : t[0];
			}
			return result;
		}		 
		else if( value instanceof IVariableBinding )
			return new ITypeBinding[]{ ( (IVariableBinding)value ).getDeclaringClass() };
		else if( value instanceof ITypeBinding )
			return new ITypeBinding[]{ (ITypeBinding)value };
		else
			throw new IllegalStateException("value = " + value + " resolvedType = " + resolvedType ); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

    private ITypeBinding[] getExpressionTypeBindings(Expression expr)
    {
        if(expr == null) return null;
        switch(expr.getNodeType())
        {
        case ASTNode.ARRAY_INITIALIZER:
            final ArrayInitializer arrayInit = (ArrayInitializer)expr;
            final List<Expression> exprs = arrayInit.expressions();
            if( exprs == null || exprs.size() == 0 )
                return new ITypeBinding[0];
            final ITypeBinding[] bindings = new ITypeBinding[exprs.size()];
            for( int i=0, size = exprs.size(); i<size; i++ ){
                final Expression initExpr = exprs.get(i);
                bindings[i] = getExpressionTypeBinding(initExpr);
            }
            return bindings;
        default:
            return new ITypeBinding[]{ getExpressionTypeBinding(expr) };
        }
    }

    private ITypeBinding getExpressionTypeBinding(Expression expr)
    {
        if( expr.getNodeType() == ASTNode.TYPE_LITERAL )
            return  ((TypeLiteral)expr).getType().resolveBinding();
        else
            return expr.resolveTypeBinding();
    }

    /**
     * @param memberName the name of the member
     * @return the value of the given member
     */
    private Object getValue(final String memberName)
    {
		if( memberName == null ) return null;
		final IResolvedMemberValuePair[] declaredPairs = _domAnnotation.getDeclaredMemberValuePairs();
		for( IResolvedMemberValuePair pair : declaredPairs ){			
			if( memberName.equals(pair.getName()) ){
				return pair.getValue();				
			}
		}
      
        // didn't find it in the ast, check the default values.
        final IMethodBinding binding = getMethodBinding(memberName);
		if(binding == null ) return null;
		return binding.getDefaultValue();
    }

    /**
     * @return the method binding that matches the given name from the annotation type
     *         referenced by this annotation.
     */
    public IMethodBinding getMethodBinding(final String memberName)
    {
        if( memberName == null ) return null;
        final ITypeBinding typeBinding = _domAnnotation.getAnnotationType();
		if( typeBinding == null ) return null;
        final IMethodBinding[] methods  = typeBinding.getDeclaredMethods();
        for( IMethodBinding method : methods ){
            if( memberName.equals(method.getName()) )
                return method;
        }
        return null;
    }
    
    public IResolvedAnnotation getResolvedAnnotaion(){return _domAnnotation; }

    public Object getReflectionValue(String memberName, Method method)
        throws Throwable
    {
        if(memberName == null || memberName.length() == 0 ) return null;
        final Class targetType = method.getReturnType();
        final Object value = getValue(memberName);	
        return getReflectionValue(value, targetType);
    }

    private Object getReflectionValue(final Object value, final Class targetType)
        throws Throwable
    {
        if( value == null ) return null;
        else if(value instanceof Boolean   ||
				value instanceof Byte      ||
				value instanceof Character ||
				value instanceof Double    || 
				value instanceof Float     ||
				value instanceof Integer   ||
				value instanceof Long      ||
				value instanceof Short     ||
				value instanceof String ) 
			return value;
        else if( value instanceof IVariableBinding )
		{
			final IVariableBinding varBinding = (IVariableBinding)value;
            final ITypeBinding declaringClass = varBinding.getDeclaringClass();
            if( declaringClass != null ){
         
                final Field returnedField = targetType.getField( varBinding.getName() );
                if (returnedField == null)
                	return null;
                if( returnedField.getType() != targetType )
                    throw new ClassCastException( targetType.getName() );
                return returnedField.get(null);
            }
		}
        else if (value instanceof Object[])
		{
			final Object[] elements = (Object[])value;
			assert targetType.isArray();
            final Class componentType = targetType.getComponentType();
            final int length = elements.length;;
            final Object array = Array.newInstance(componentType, length);
            if( length == 0) return array;

            for( int i=0; i<length; i++ ){                
                final Object returnObj = getReflectionValue( elements[i], componentType );
                // fill in the array.
                // If it is an array of some primitive type, we will need to unwrap it.
                if( componentType.isPrimitive() ){
                    if( componentType == boolean.class ){
                        final Boolean bool = (Boolean)returnObj;
                        Array.setBoolean( array, i, bool.booleanValue());
                    }
                    else if( componentType == byte.class ){
                        final Byte b = (Byte)returnObj;
                        Array.setByte( array, i, b.byteValue() );
                    }
                    else if( componentType == char.class ){
                        final Character c = (Character)returnObj;
                        Array.setChar( array, i, c.charValue() );
                    }
                    else if( componentType == double.class ){
                        final Double d = (Double)returnObj;
                        Array.setDouble( array, i, d.doubleValue() );
                    }
                    else if( componentType == float.class ){
                        final Float f = (Float)returnObj;
                        Array.setFloat( array, i, f.floatValue() );
                    }
                    else if( componentType == int.class ){
                        final Integer integer = (Integer)returnObj;
                        Array.setInt( array, i, integer.intValue() );
                    }
                    else if( componentType == long.class ){
                        final Long l = (Long)returnObj;
                        Array.setLong( array, i, l.longValue() );
                    }
                    else if( componentType == short.class ){
                        final Short s = (Short)returnObj;
                        Array.setShort( array, i, s.shortValue() );
                    }
                    else {
                        throw new IllegalStateException("unrecognized primitive type: "  + componentType ); //$NON-NLS-1$
                    }
                }
                else{
                    Array.set( array, i, returnObj );
                }
            }
            return array;
		}
		// caller should have caught this case.
        else if( value instanceof ITypeBinding )
			throw new IllegalStateException();
		
        else if( value instanceof IResolvedAnnotation )
		{
			final AnnotationMirrorImpl annoMirror =
                (AnnotationMirrorImpl)Factory.createAnnotationMirror((IResolvedAnnotation)value, _annotated, _env);
            final AnnotationInvocationHandler handler = new AnnotationInvocationHandler(annoMirror, targetType);
            return Proxy.newProxyInstance(targetType.getClassLoader(),
                                             new Class[]{ targetType }, handler );
		}

        return null;
    }

    public MirrorKind kind(){ return MirrorKind.ANNOTATION_MIRROR; }

    boolean isFromSource()
	{  
		return _annotated.isFromSource();		
	}

    org.eclipse.jdt.core.dom.Annotation getAstNode()
	{ 
		if( isFromSource() ){
			final CompilationUnit unit = _annotated.getCompilationUnit();
			final ASTNode node = unit.findDeclaringNode(_domAnnotation);
			if( node instanceof org.eclipse.jdt.core.dom.Annotation )
				return (org.eclipse.jdt.core.dom.Annotation)node;			
		}	
		return null;
    }
	
	ASTNode getASTNodeForElement(String name)
	{
		if( name == null ) return null;
		final org.eclipse.jdt.core.dom.Annotation anno = getAstNode();
		if( anno != null ){
			if( anno.isSingleMemberAnnotation() ){
				if( "value".equals(name) ) //$NON-NLS-1$
					return ((SingleMemberAnnotation)anno).getValue();
			}
			else if( anno.isNormalAnnotation() ){
				final List<MemberValuePair> pairs = ((NormalAnnotation)anno).values();
				for( MemberValuePair pair : pairs )
				{
					final String pairName = pair.getName() == null ? null : pair.getName().toString();
					if( name.equals(pairName) )
						return pair.getValue();
				}
			}
		}
		// marker annotation or no match.
		return null;
	}

    CompilationUnit getCompilationUnit() { return _annotated.getCompilationUnit(); }

	public BaseProcessorEnv getEnvironment(){ return _env; }
	
	public IFile getResource()
	{ 	return _annotated.getResource(); }
	
	public EclipseDeclarationImpl getAnnotatedDeclaration(){ return _annotated; }

    public boolean equals(Object obj){
        if( obj instanceof AnnotationMirrorImpl ){
            return ((AnnotationMirrorImpl)obj)._domAnnotation == _domAnnotation;
        }
        return false;
    }

    public int hashCode(){
        return _domAnnotation.hashCode();
    }
}
