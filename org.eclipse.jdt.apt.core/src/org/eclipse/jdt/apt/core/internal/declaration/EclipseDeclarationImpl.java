/*******************************************************************************
 * Copyright (c) 2005, 2017 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

 package org.eclipse.jdt.apt.core.internal.declaration;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.util.DeclarationVisitor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.AnnotationInvocationHandler;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.*;

public abstract class EclipseDeclarationImpl implements Declaration, EclipseMirrorObject
{
    final BaseProcessorEnv _env;

    EclipseDeclarationImpl(final BaseProcessorEnv env)
    {
        assert env != null : "missing environment"; //$NON-NLS-1$
        _env = env;
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitDeclaration(this);
    }

	@SuppressWarnings("unchecked")
	<A extends Annotation> A _getAnnotation(Class<A> annotationClass,
                                            IAnnotationBinding[] annoInstances)
    {
    	if( annoInstances == null || annoInstances.length == 0 || annotationClass == null )
    		return null;

        String annoTypeName = annotationClass.getName();
		if( annoTypeName == null ) return null;
        annoTypeName = annoTypeName.replace('$', '.');
		for( IAnnotationBinding annoInstance :  annoInstances){
        	if (annoInstance == null)
        		continue;
            final ITypeBinding binding = annoInstance.getAnnotationType();
            if( binding != null && binding.isAnnotation() ){
                final String curTypeName = binding.getQualifiedName();
                if( annoTypeName.equals(curTypeName) ){
                    final AnnotationMirrorImpl annoMirror =
                        (AnnotationMirrorImpl)Factory.createAnnotationMirror(annoInstance, this, _env);
                    final AnnotationInvocationHandler handler = new AnnotationInvocationHandler(annoMirror, annotationClass);
                    return (A)Proxy.newProxyInstance(annotationClass.getClassLoader(),
                                                     new Class[]{ annotationClass }, handler );
                }
            }
        }
        return null;
    }

    Collection<AnnotationMirror> _getAnnotationMirrors(IAnnotationBinding[] annoInstances)
    {
        if( annoInstances == null || annoInstances.length == 0 )
        	return Collections.emptyList();
        final List<AnnotationMirror> result = new ArrayList<>(annoInstances.length);
        for(IAnnotationBinding annoInstance : annoInstances){
        	if (annoInstance != null) {
	            final AnnotationMirrorImpl annoMirror =
                        (AnnotationMirrorImpl)Factory.createAnnotationMirror(annoInstance, this, _env);
        		result.add(annoMirror);
         	}
        }
        return result;
    }

	Collection<AnnotationMirror> _getAnnotationMirrors(List<org.eclipse.jdt.core.dom.Annotation> annoInstances)
	{
		if( annoInstances == null || annoInstances.size() == 0 ) return Collections.emptyList();
		final List<AnnotationMirror> result = new ArrayList<>(annoInstances.size());
		for( org.eclipse.jdt.core.dom.Annotation annoInstance : annoInstances){
			if (annoInstance != null) {
				final AnnotationMirrorImpl annoMirror =
					(AnnotationMirrorImpl)Factory.createAnnotationMirror(annoInstance.resolveAnnotationBinding(), this, _env);
				result.add(annoMirror);
			}
		}
		return result;
	}

	/**
     * @return the ast node that corresponding to this declaration,
     * or null if this declaration came from binary.
     * @see #isFromSource()
     */
    abstract ASTNode getAstNode();

    /**
     * @return the compilation unit that the ast node of this declaration came from
     *         Return null if this declaration came from binary.
     * @see #isFromSource()
     */
    abstract CompilationUnit getCompilationUnit();

	/**
	 * @return the resource of this declaration if the declaration is from source.
	 */
	abstract public IFile getResource();

    /**
     * @return true iff this declaration came from a source file.
     *         Return false otherwise.
     */
    public abstract boolean isFromSource();

    public abstract boolean isBindingBased();

	@Override
	public BaseProcessorEnv getEnvironment(){ return _env; }

	/**
	 * @return the ast node that holds the range of this member declaration in source.
	 *         The default is to find the name of the node and if that fails, return the
	 *         node with the smallest range that contains the declaration.
	 */
	protected ASTNode getRangeNode()
	{
		final ASTNode node = getAstNode();
		if( node == null ) return null;
		SimpleName name = null;
		switch( node.getNodeType() )
		{
		case ASTNode.TYPE_DECLARATION:
		case ASTNode.ANNOTATION_TYPE_DECLARATION:
		case ASTNode.ENUM_DECLARATION:
			name = ((AbstractTypeDeclaration)node).getName();
			break;
		case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
			name = ((AnnotationTypeMemberDeclaration)node).getName();
			break;
		case ASTNode.METHOD_DECLARATION:
			name = ((MethodDeclaration)node).getName();
			break;
		case ASTNode.SINGLE_VARIABLE_DECLARATION:
			name = ((SingleVariableDeclaration)node).getName();
			break;
		case ASTNode.FIELD_DECLARATION:
			final String declName = getSimpleName();
			if( declName == null ) return node;
			for(Object obj : ((FieldDeclaration)node).fragments() ){
				 VariableDeclarationFragment frag = (VariableDeclarationFragment)obj;
				 if( declName.equals(frag.getName().toString()) ){
					 name = frag.getName();
					 break;
				 }
			}
			break;
		case ASTNode.ENUM_CONSTANT_DECLARATION:
			name = ((EnumConstantDeclaration)node).getName();
			break;
		default:
			return node;
		}
		if( name == null ) return node;
		return name;
	}

	protected String getDocComment(final BodyDeclaration decl)
    {
    	final Javadoc javaDoc = decl.getJavadoc();
        if( javaDoc == null ) return ""; //$NON-NLS-1$
        return javaDoc.toString();
    }
}
