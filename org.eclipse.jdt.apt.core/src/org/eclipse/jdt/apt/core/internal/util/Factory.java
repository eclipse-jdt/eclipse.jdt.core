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
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationElementDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationValueImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ClassDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.ConstructorDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.DeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EnumConstantDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EnumDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.FieldDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.InterfaceDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.MethodDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.TypeParameterDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.type.ArrayTypeImpl;
import org.eclipse.jdt.apt.core.internal.type.ErrorType;
import org.eclipse.jdt.apt.core.internal.type.WildcardTypeImpl;
import org.eclipse.jdt.core.dom.IResolvedAnnotation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class Factory
{
    public static TypeDeclarationImpl createReferenceType(ITypeBinding binding, ProcessorEnvImpl env)
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
            throw new IllegalStateException("cannot create type declaration from " + binding);

        return mirror;
    }

    public static DeclarationImpl createDeclaration(IBinding binding, ProcessorEnvImpl env)
    {
        if(binding == null) return null;
       
        switch(binding.getKind())
        {
    	case IBinding.TYPE:
    		final ITypeBinding typeBinding = (ITypeBinding)binding;
        	if( typeBinding.isAnonymous() || typeBinding.isArray() || 
    			typeBinding.isWildcardType() || typeBinding.isPrimitive() )       
                throw new IllegalStateException("failed to create declaration from " + binding);
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
            throw new IllegalStateException("failed to create declaration from " + binding);
        }     
    }

    public static TypeMirror createTypeMirror(ITypeBinding binding, ProcessorEnvImpl env)
    {		
        if( binding == null ) return null;        

		if( binding.isPrimitive() ){
			if( "int".equals(binding.getName()) )
				return env.getIntType(); 
			else if( "byte".equals(binding.getName()) )
				return env.getByteType();
			else if( "short".equals(binding.getName()) )
				return env.getShortType();
			else if( "char".equals(binding.getName()) )
				return env.getCharType();
			else if( "long".equals(binding.getName()) )
				return env.getLongType();
			else if( "float".equals(binding.getName()) )
				return env.getFloatType();
			else if( "double".equals(binding.getName()) )
				return env.getDoubleType();
			else if( "boolean".equals(binding.getName()))
				return env.getBooleanType();
			else if( "void".equals(binding.getName()) )
				return env.getVoidType();
			else
				throw new IllegalStateException("unrecognized primitive type: " + binding);
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
  
    /**
     * @param annotation the ast node.
     * @param annotated the declaration that <code>annotation</code> annotated
     * @param env
     * @return a newly created {@link AnnotationMirror} object
     */
    public static AnnotationMirror createAnnotationMirror(final IResolvedAnnotation annotation,
                                                          final DeclarationImpl annotated,
                                                          final ProcessorEnvImpl env)
    {
        return new AnnotationMirrorImpl(annotation, annotated, env);		
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
													 AnnotationElementDeclarationImpl decl, 
													 ProcessorEnvImpl env)
    {
        if( domValue == null ) return null;
		final Object converted = convertDOMValueToMirrorValue(domValue, null, decl, decl, env);		
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
															  ProcessorEnvImpl env)
	{
		if( domValue == null ) return null;
		final Object converted = convertDOMValueToMirrorValue(domValue, elementName, anno, anno.getAnnotatedDeclaration(), env);
		return createAnnotationValue(converted, elementName, -1, anno, env);		
	}
	
	private static AnnotationValue createAnnotationValue(Object convertedValue, 
														 String name,
														 int index,
														 EclipseMirrorImpl mirror, 
														 ProcessorEnvImpl env)	
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
     */
    private static Object convertDOMValueToMirrorValue(Object domValue, 
													   String name,	
													   EclipseMirrorImpl parent,
													   DeclarationImpl decl, 
													   ProcessorEnvImpl env)
    {
        if( domValue == null ) return null;		
        else if(domValue instanceof Boolean   ||
				domValue instanceof Byte      ||
				domValue instanceof Character ||
				domValue instanceof Double    || 
				domValue instanceof Float     ||
				domValue instanceof Integer   ||
				domValue instanceof Long      ||
				domValue instanceof Short     ||
				domValue instanceof String ) 
			return domValue;
        else if( domValue instanceof IVariableBinding )
		{
			return Factory.createDeclaration((IVariableBinding)domValue, env);			
		}
        else if (domValue instanceof Object[])
		{
			final Object[] elements = (Object[])domValue;
			final int len = elements.length;
            final List<AnnotationValue> annoValues = new ArrayList<AnnotationValue>(len);
			for( int i=0; i<len; i++ ){				
                if( elements[i] == null ) continue;
                // can't have multi-dimensional array.
                // there should be already a java compile time error
                else if( elements[i] instanceof Object[] )
                    return null;

				Object o = convertDOMValueToMirrorValue( elements[i], name, parent, decl, env );
				assert( !( o instanceof IResolvedAnnotation ) ) : "Unexpected return value from convertDomValueToMirrorValue! o.getClass().getName() = " + o.getClass().getName();
				
				final AnnotationValue annoValue = createAnnotationValue(o, name, i, parent, env);
                if( annoValue != null )
                    annoValues.add(annoValue);
            }
			return annoValues;
		}
		// caller should have caught this case.
        else if( domValue instanceof ITypeBinding )
			return Factory.createTypeMirror((ITypeBinding)domValue, env);
		
        else if( domValue instanceof IResolvedAnnotation )
		{
			return Factory.createAnnotationMirror((IResolvedAnnotation)domValue, decl, env);
		}
        
		// should never reach this point
		throw new IllegalStateException("cannot build annotation value object from " + domValue);
    }

    public static InterfaceType createErrorInterfaceType(final ITypeBinding binding)
    {
        return new ErrorType.ErrorInterface(binding.getName());
    }

    public static ClassType createErrorClassType(final ITypeBinding binding)
    {
        return new ErrorType.ErrorClass(binding.getName());
    }

    public static AnnotationType createErrorAnnotationType(final ITypeBinding binding)
    {
        return new ErrorType.ErrorAnnotation(binding.getName());
    }
}
