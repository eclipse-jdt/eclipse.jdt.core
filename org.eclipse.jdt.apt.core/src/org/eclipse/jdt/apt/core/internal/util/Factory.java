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

package org.eclipse.jdt.apt.core.internal.util;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ASTBasedAnnotationElementDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ASTBasedFieldDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ASTBasedMethodDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationElementDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationValueImpl;
import org.eclipse.jdt.apt.core.internal.declaration.BinaryParameterDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ClassDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ConstructorDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EnumConstantDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EnumDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ExecutableDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.FieldDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.InterfaceDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.MethodDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.SourceParameterDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.TypeParameterDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.type.ArrayTypeImpl;
import org.eclipse.jdt.apt.core.internal.type.ErrorType;
import org.eclipse.jdt.apt.core.internal.type.WildcardTypeImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.IResolvedAnnotation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class Factory
{
    public static TypeDeclarationImpl createReferenceType(ITypeBinding binding, BaseProcessorEnv env)
    {
        if(binding == null || binding.isNullType()) return null;        
        TypeDeclarationImpl mirror = null;
        // must test for annotation type before interface since annotation 
        // is an interface
        if( binding.isAnnotation() )
            mirror = new AnnotationDeclarationImpl(binding, env);
        else if (binding.isInterface() )
            mirror = new InterfaceDeclarationImpl(binding, env);
        // must test for enum first since enum is also a class. 
        else if( binding.isEnum() ) 
        	mirror = new EnumDeclarationImpl(binding, env);
        else if( binding.isClass() )
            mirror = new ClassDeclarationImpl(binding, env);
        else
            throw new IllegalStateException("cannot create type declaration from " + binding); //$NON-NLS-1$

        return mirror;
    }

    public static EclipseDeclarationImpl createDeclaration(IBinding binding, BaseProcessorEnv env)
    {
        if(binding == null) return null;
       
        switch(binding.getKind())
        {
    	case IBinding.TYPE:
    		final ITypeBinding typeBinding = (ITypeBinding)binding;
        	if( typeBinding.isAnonymous() || typeBinding.isArray() || 
    			typeBinding.isWildcardType() || typeBinding.isPrimitive() )       
                throw new IllegalStateException("failed to create declaration from " + binding); //$NON-NLS-1$
            return createReferenceType(typeBinding, env);
        case IBinding.VARIABLE:
        	final IVariableBinding varBinding = (IVariableBinding)binding;            
            if(varBinding.isEnumConstant())
                return new EnumConstantDeclarationImpl(varBinding, env);
            else
                return new FieldDeclarationImpl(varBinding, env);
        case IBinding.METHOD:
            final IMethodBinding method = (IMethodBinding)binding;
            if( method.isConstructor() )
                return new ConstructorDeclarationImpl(method, env);
            final ITypeBinding declaringType = method.getDeclaringClass();
            if( declaringType != null && declaringType.isAnnotation() )
                return new AnnotationElementDeclarationImpl(method, env);
            else
                return new MethodDeclarationImpl(method, env);             
        default:
            throw new IllegalStateException("failed to create declaration from " + binding); //$NON-NLS-1$
        }     
    }
    
    public static EclipseDeclarationImpl createDeclaration(
    		ASTNode node, 
    		IFile file,
    		BaseProcessorEnv env)
    {
    	 if( node == null )
    		 return null;
    	 switch( node.getNodeType() )
    	 {
    	 case ASTNode.SINGLE_VARIABLE_DECLARATION:
    		 return new SourceParameterDeclarationImpl((SingleVariableDeclaration)node, file, env);
    	 case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
    		 return new ASTBasedFieldDeclarationImpl( (VariableDeclarationFragment)node, file, env );
    	 case ASTNode.METHOD_DECLARATION :
    		  return new ASTBasedMethodDeclarationImpl( (org.eclipse.jdt.core.dom.MethodDeclaration)node, file, env );
    	 case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
    		 return new ASTBasedMethodDeclarationImpl((AnnotationTypeMemberDeclaration)node, file, env);
    	 default :
    		 throw new UnsupportedOperationException(
    				 "cannot create mirror type from " +   //$NON-NLS-1$
    				 node.getClass().getName() );
    	 }
    }

    public static TypeMirror createTypeMirror(ITypeBinding binding, BaseProcessorEnv env)
    {		
        if( binding == null ) return null;        

		if( binding.isPrimitive() ){
			if( "int".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getIntType(); 
			else if( "byte".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getByteType();
			else if( "short".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getShortType();
			else if( "char".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getCharType();
			else if( "long".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getLongType();
			else if( "float".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getFloatType();
			else if( "double".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getDoubleType();
			else if( "boolean".equals(binding.getName())) //$NON-NLS-1$
				return env.getBooleanType();
			else if( "void".equals(binding.getName()) ) //$NON-NLS-1$
				return env.getVoidType();
			else
				throw new IllegalStateException("unrecognized primitive type: " + binding); //$NON-NLS-1$
        }
        else if( binding.isArray() )
            return new ArrayTypeImpl(binding, env);
        else if( binding.isWildcardType() ){
			return new WildcardTypeImpl(binding, env);            
        }
        else if( binding.isTypeVariable() )
            return new TypeParameterDeclarationImpl(binding, env);
        else
            return createReferenceType(binding, env);       
    }
    
    public static ParameterDeclaration createParameterDeclaration(
    		final SingleVariableDeclaration param,
    		final IFile file,
    		final BaseProcessorEnv env)
    {
    	return new SourceParameterDeclarationImpl(param, file, env);
    }
    
    
    public static ParameterDeclaration createParameterDeclaration(
    		final ExecutableDeclarationImpl exec,
    		final int paramIndex,
    		final ITypeBinding type,
    		final BaseProcessorEnv env )
    {
    	return new BinaryParameterDeclarationImpl(exec, type, paramIndex, env);
    }
   
  
    /**
     * @param annotation the ast node.
     * @param annotated the declaration that <code>annotation</code> annotated
     * @param env
     * @return a newly created {@link AnnotationMirror} object
     */
    public static AnnotationMirror createAnnotationMirror(final IResolvedAnnotation annotation,
                                                          final EclipseDeclarationImpl annotated,
                                                          final BaseProcessorEnv env)
    {
        return new AnnotationMirrorImpl(annotation, annotated, env);		
    }
    
    public static AnnotationValue createDefaultValue(
    		Object domValue,
    		AnnotationElementDeclarationImpl decl,
    		BaseProcessorEnv env)
    {
    	if( domValue == null ) return null;		
		final Object converted = convertDOMValueToMirrorValue(
				domValue, null, decl, decl, env, decl.getReturnType());
		
        return createAnnotationValue(converted, null, -1, decl, env);
    }
	
	/**
	 * Build an {@link AnnotationValue} object based on the given dom value.
	 * @param domValue default value according to the DOM API.
	 * @param decl the element declaration whose default value is <code>domValue</code>
	 * 			   if {@link #domValue} is an annotation, then this is the declaration it annotated. 
	 * 			   In all other case, this parameter is ignored.
	 * @param env 
	 * @return an annotation value
	 */
    public static AnnotationValue createDefaultValue(Object domValue, 
													 ASTBasedAnnotationElementDeclarationImpl decl, 
													 BaseProcessorEnv env)
    {
        if( domValue == null ) return null;		
		final Object converted = convertDOMValueToMirrorValue(
				domValue, null, decl, decl, env, decl.getReturnType());
		
        return createAnnotationValue(converted, null, -1, decl, env);
    }
	
	/**
	 * Build an {@link AnnotationValue} object based on the given dom value.
	 * @param domValue annotation member value according to the DOM API.
	 * @param elementName the name of the member value
	 * @param anno the annotation that directly contains <code>domValue</code>	
	 * @param env 
	 * @return an annotation value
	 */
	public static AnnotationValue createAnnotationMemberValue(Object domValue,
															  String elementName,
															  AnnotationMirrorImpl anno, 														
															  BaseProcessorEnv env,
															  TypeMirror expectedType)
	{
		if( domValue == null ) return null;
		final Object converted = convertDOMValueToMirrorValue(
				domValue, elementName, anno, 
				anno.getAnnotatedDeclaration(), env, expectedType);
		return createAnnotationValue(converted, elementName, -1, anno, env);		
	}
	
	/**
	 * @param convertedValue value in mirror form.
	 * @param name the name of the annotation member or null for default value 
	 * @param index the number indicate the source order of the annotation member value 
	 *              in the annotation instance.
	 * @param mirror either {@link AnnotationMirrorImpl } or {@link AnnotationElementDeclarationImpl}
	 * @param env
	 * @param needBoxing whether the expected type of the member value is an array or not.
	 * @return
	 */
	private static AnnotationValue createAnnotationValue(Object convertedValue, 
														 String name,
														 int index,
														 EclipseMirrorImpl mirror, 
														 BaseProcessorEnv env)	
	{
		if( convertedValue == null ) return null;
		if( mirror instanceof AnnotationMirrorImpl )
			return new AnnotationValueImpl(convertedValue, name, index, (AnnotationMirrorImpl)mirror, env);
		else
			return new AnnotationValueImpl(convertedValue, index, (AnnotationElementDeclarationImpl)mirror, env);
	}


    /**
     * Building an annotation value object based on the dom value.
     * 
     * @param dom the dom value to convert to the mirror specification.      
     * @see com.sun.mirror.declaration.AnnotationValue.getObject()
     * @param name the name of the element if <code>domValue</code> is an 
     * element member value of an annotation
     * @param parent the parent of this annotation value.
     * @param decl if <code>domValue</code> is a default value, then this is the 
     * annotation element declaration where the default value originates
     * if <code>domValue</code> is an annotation, then <code>decl</code>
     * is the declaration that it annotates.
     * @param expectedType the declared type of the member value.
     * @param needBoxing <code>true</code> indicate an array should be returned. 
     * @return the converted annotation value or null if the conversion failed
     */
    private static Object convertDOMValueToMirrorValue(Object domValue, 
													   String name,	
													   EclipseMirrorImpl parent,
													   EclipseDeclarationImpl decl, 
													   BaseProcessorEnv env,
													   TypeMirror expectedType)													   
    {
        if( domValue == null ) return null;		
        
        final Object returnValue;
        if( domValue instanceof Boolean   ||
			domValue instanceof Byte      ||
			domValue instanceof Character ||
			domValue instanceof Double    || 
			domValue instanceof Float     ||
			domValue instanceof Integer   ||
			domValue instanceof Long      ||
			domValue instanceof Short     ||
			domValue instanceof String ) 
			returnValue = domValue;
        
        else if( domValue instanceof IVariableBinding )
		{
        	returnValue = Factory.createDeclaration((IVariableBinding)domValue, env);			
		}
        else if (domValue instanceof Object[])
		{
			final Object[] elements = (Object[])domValue;
			final int len = elements.length;
            final List<AnnotationValue> annoValues = new ArrayList<AnnotationValue>(len);
            final TypeMirror leaf; 
            if( expectedType instanceof ArrayType )
            	leaf = ((ArrayType)expectedType).getComponentType();
            else
            	leaf = expectedType; // doing our best here.
			for( int i=0; i<len; i++ ){				
                if( elements[i] == null ) continue;
                // can't have multi-dimensional array.
                // there should be already a java compile time error
                else if( elements[i] instanceof Object[] )
                    return null;

				Object o = convertDOMValueToMirrorValue( elements[i], name, parent, decl, env, leaf );				
				if( o == null ) 
					return null; 
				assert( !( o instanceof IResolvedAnnotation ) ) : 
					"Unexpected return value from convertDomValueToMirrorValue! o.getClass().getName() = " //$NON-NLS-1$
					+ o.getClass().getName(); 
				
				final AnnotationValue annoValue = createAnnotationValue(o, name, i, parent, env);
                if( annoValue != null )
                    annoValues.add(annoValue);
            }
			return annoValues;
		}
		// caller should have caught this case.
        else if( domValue instanceof ITypeBinding )
			returnValue = Factory.createTypeMirror((ITypeBinding)domValue, env);
		
        else if( domValue instanceof IResolvedAnnotation )
		{
			returnValue = Factory.createAnnotationMirror((IResolvedAnnotation)domValue, decl, env);
		}
        else	        
			// should never reach this point
			throw new IllegalStateException("cannot build annotation value object from " + domValue); //$NON-NLS-1$       

        return performNecessaryTypeConversion(expectedType, returnValue, name, parent, env);
    }
    
    /**
     * Apply type conversion according to JLS 5.1.2 and 5.1.3 and / or auto-boxing.
     * @param expectedType the expected type
     * @param value the value where conversion may be applied to
     * @param name name of the member value
     * @param parent the of the annotation of the member value
     * @param env 
     * @return the value matching the expected type or itself if no conversion can be applied.
     */
    private static Object performNecessaryTypeConversion(final TypeMirror expectedType,
	    											     final Object value,
	    											     final String name,
	    											     final EclipseMirrorImpl parent,
	    											     final BaseProcessorEnv env)
    {
    	if(expectedType == null )return value;
    	// apply widening or narrowing primitive type conversion based on JLS 5.1.2 and 5.1.3
    	if( expectedType instanceof PrimitiveType )
    	{    	
    		// widening byte -> short, int, long, float or double
    		// narrowing byte -> char
    		if( value instanceof Byte )
    		{
    			final byte b = ((Byte)value).byteValue();
    			switch( ((PrimitiveType)expectedType).getKind() )
    			{
    			case CHAR:
    				return new Character((char)b);
    			case SHORT:
    				return new Short(b);
    			case INT:
    				return new Integer(b);
    			case LONG:
    				return new Long(b);
    			case FLOAT:
    				return new Float(b);
    			case DOUBLE:
    				return new Double(b);
    			default:
    				// it is either already correct or it is completely wrong,
    				// which doesn't really matter what's returned
    				return value;
    			}
    		}
    		// widening short -> int, long, float, or double 
    		// narrowing short -> byte or char
    		else if( value instanceof Short )
    		{
    			final short s = ((Short)value).shortValue();
    			switch( ((PrimitiveType)expectedType).getKind() )
    			{
    			case BYTE:
    				return new Byte((byte)s);
    			case CHAR:
    				return new Character((char)s);  
    			case INT:
    				return new Integer(s); 
    			case LONG:
    				return new Long(s);
    			case FLOAT:
    				return new Float(s);
    			case DOUBLE:
    				return new Double(s);
    			default:
    				// it is either already correct or it is completely wrong,
    				// which doesn't really matter what's returned
    				return value;
    			}
    		}
    		// widening char -> int, long, float, or double 
    		// narrowing char -> byte or short
    		else if( value instanceof Character )
    		{
    			final char c = ((Character)value).charValue();
    			switch( ((PrimitiveType)expectedType).getKind() )
    			{
    			case INT:
    				return new Integer(c); 
    			case LONG:
    				return new Long(c);
    			case FLOAT:
    				return new Float(c);
    			case DOUBLE:
    				return new Double(c);
    			case BYTE:
    				return new Byte((byte)c);
    			case SHORT:
    				return new Short((short)c);  
    			
    			default:
    				// it is either already correct or it is completely wrong,
    				// which doesn't really matter what's returned
    				return value;
    			}
    		}
    		
    		// widening int -> long, float, or double 
    		// narrowing int -> byte, short, or char 
    		else if( value instanceof Integer )
    		{
    			final int i = ((Integer)value).intValue();
    			switch( ((PrimitiveType)expectedType).getKind() )
    			{    		
    			case LONG:
    				return new Long(i);
    			case FLOAT:
    				return new Float(i);
    			case DOUBLE:
    				return new Double(i);
    			case BYTE:
    				return new Byte((byte)i);
    			case SHORT:
    				return new Short((short)i);  
    			case CHAR:
    				return new Character((char)i);
    			default:
    				// it is either already correct or it is completely wrong,
    				// which doesn't really matter what's returned
    				return value;
    			}
    		}
    		// widening long -> float or double
    		else if( value instanceof Long )
    		{
    			final long l = ((Long)value).longValue();
    			switch( ((PrimitiveType)expectedType).getKind() )
    			{
    			case FLOAT:
    				return new Float(l);
    			case DOUBLE:
    				return new Double(l);    		
    			default:
    				// it is either already correct or it is completely wrong,
    				// which doesn't really matter what's returned
    				return value;
    			}
    		}
    		
    		// widening float -> double    		 
    		else if( value instanceof Float )
    		{
    			final float f = ((Float)value).floatValue();
    			switch( ((PrimitiveType)expectedType).getKind() )
    			{    			
    			case DOUBLE:
    				return new Double(f);    		
    			default:
    				// it is either already correct or it is completely wrong,
    				// which doesn't really matter what's returned
    				return value;
    			}
    		}
    		else // boolean or double case. Nothing we can do here.
    			return value;
    	}
    	// handle auto-boxing
    	else if( expectedType instanceof ArrayType)
    	{
    		final TypeMirror componentType = ((ArrayType)expectedType).getComponentType();
    		Object converted = value;
    		// if it is an error case, will just leave it as is.
    		if( !(componentType instanceof ArrayType ) )    		
    			converted = performNecessaryTypeConversion(componentType, value, name, parent, env);
    		
    		final AnnotationValue annoValue = createAnnotationValue(converted, name, 0, parent, env);
        	return Collections.singletonList(annoValue);
    	}
    	else // no change
    		return value;
    }

    public static InterfaceType createErrorInterfaceType(final ITypeBinding binding)
    {
        return new ErrorType.ErrorInterface(binding.getName());
    }

    public static ClassType createErrorClassType(final ITypeBinding binding)
    {
        return createErrorClassType(binding.getName());
    }
    
    public static ClassType createErrorClassType(final String name)
    {
    	return new ErrorType.ErrorClass(name);
    }

    public static AnnotationType createErrorAnnotationType(final ITypeBinding binding)
    {
        return createErrorAnnotationType(binding.getName());
    }
    
    public static AnnotationType createErrorAnnotationType(String name)
    {	
        return new ErrorType.ErrorAnnotation(name);
    }
    
    public static ArrayType createErrorArrayType(final String name, final int dimension)
    {
    	return new ErrorType.ErrorArrayType(name, dimension);
    }
}
