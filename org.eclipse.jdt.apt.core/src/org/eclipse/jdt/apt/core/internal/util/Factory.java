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
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.apt.core.internal.type.PrimitiveTypeImpl;
import org.eclipse.jdt.apt.core.internal.type.VoidTypeImpl;
import org.eclipse.jdt.apt.core.internal.type.WildcardTypeImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;

public class Factory
{
    public static TypeDeclarationImpl createReferenceType(ITypeBinding binding, ProcessorEnvImpl env)
    {
        if(binding == null || binding.isNullType()) return null;
        // TODO: (theodora) how to detect invalid binding? 
        //       Do they manifest at public API level? 
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
				return PrimitiveTypeImpl.PRIMITIVE_INT; 
			else if( "byte".equals(binding.getName()) )
				return PrimitiveTypeImpl.PRIMITIVE_BYTE;
			else if( "short".equals(binding.getName()) )
				return PrimitiveTypeImpl.PRIMITIVE_SHORT;
			else if( "char".equals(binding.getName()) )
				return PrimitiveTypeImpl.PRIMITIVE_CHAR;
			else if( "long".equals(binding.getName()) )
				return PrimitiveTypeImpl.PRIMITIVE_LONG;
			else if( "float".equals(binding.getName()) )
				return PrimitiveTypeImpl.PRIMITIVE_FLOAT;
			else if( "double".equals(binding.getName()) )
				return PrimitiveTypeImpl.PRIMITIVE_DOUBLE;
			else if( "boolean".equals(binding.getName()))
				return PrimitiveTypeImpl.PRIMITIVE_BOOLEAN;
			else if( "void".equals(binding.getName()) )
				return VoidTypeImpl.TYPE_VOID;
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
    public static AnnotationMirror createAnnotationMirror(final Annotation annotation,
                                                          final DeclarationImpl annotated,
                                                          final ProcessorEnvImpl env)
    {
        return new AnnotationMirrorImpl(annotation, annotated, env);
    }

    public static AnnotationValue createAnnotationValue(Expression expression, DeclarationImpl decl, ProcessorEnvImpl env)
    {
        if( expression == null ) return null;
        return new AnnotationValueImpl(expression, decl, env);
    }

    /**
     * Building an annotation value object based on an expression
     * @see com.sun.mirror.declaration.AnnotationValue.getObject()
     */
    public static Object createAnnotationValueObject(Expression expr, DeclarationImpl decl, ProcessorEnvImpl env)
    {
        if( expr == null ) return null;
		final Object constantValue = expr.resolveConstantExpressionValue();
		if( constantValue != null ) return constantValue;
        switch(expr.getNodeType())
        {
        case ASTNode.SIMPLE_NAME:
        case ASTNode.QUALIFIED_NAME:
            final Name name = (Name)expr;
            final IBinding nameBinding = name.resolveBinding();
            if( nameBinding.getKind() == IBinding.VARIABLE ) {
                return ((IVariableBinding)nameBinding).getConstantValue();
            }
            break;
        case ASTNode.ARRAY_INITIALIZER:

            final List<Expression> exprs = ((ArrayInitializer)expr).expressions();
            final List<AnnotationValue> annoValues = new ArrayList<AnnotationValue>(exprs.size());
            for(Expression initExpr : exprs ){
                if( initExpr == null ) continue;
                // can't have multi-dimensional array.
                // there should be already a java compile time error
                else if( initExpr.getNodeType() == ASTNode.ARRAY_INITIALIZER )
                    return null;

                final AnnotationValue value = createAnnotationValue(initExpr, decl, env);
                if( value != null )
                    annoValues.add(value);
            }
            return annoValues;
        case ASTNode.NORMAL_ANNOTATION:
        case ASTNode.MARKER_ANNOTATION:
        case ASTNode.SINGLE_MEMBER_ANNOTATION:
            return Factory.createAnnotationMirror((Annotation)expr, decl, env);        
        case ASTNode.TYPE_LITERAL:
            throw new IllegalStateException("illegal expression " + expr);     
        }

        return null;
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
