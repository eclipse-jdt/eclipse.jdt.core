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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.*;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

public abstract class MemberDeclarationImpl extends DeclarationImpl implements MemberDeclaration, EclipseMirrorImpl
{
    MemberDeclarationImpl(final IBinding binding, ProcessorEnvImpl env)
    {
        super(binding, env);
    }
    
    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitMemberDeclaration(this);
    }
    
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
		final IResolvedAnnotation[] instances = getAnnotationInstances();
		return _getAnnotation(annotationClass, instances);
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {
		final IResolvedAnnotation[] instances = getAnnotationInstances();
		return _getAnnotationMirrors(instances);		
    }
	
	private IResolvedAnnotation[] getAnnotationInstances()
	{
		final IBinding binding = getDeclarationBinding();
		final IResolvedAnnotation[] instances;
		switch( binding.getKind() )
		{
		case IBinding.TYPE:
			instances = ((ITypeBinding)binding).getAnnotations();
			break;
		case IBinding.METHOD:
			instances = ((IMethodBinding)binding).getAnnotations();
			break;
		case IBinding.VARIABLE:
			instances = ((IVariableBinding)binding).getAnnotations();
			break;
		case IBinding.PACKAGE:
			// TODO: support package annotation
			return null;
		default:			
			throw new IllegalStateException();
		}
		return instances;
	}

    public String getDocComment()
    {
        if( isFromSource()){
            final Javadoc javaDoc = ((BodyDeclaration)getAstNode()).getJavadoc();
            if( javaDoc == null ) return "";
            return javaDoc.toString();
        }
        return null;

    }
	
	/**
	 * @return the ast node that holds the range of this member declaration in source.
	 *         The default is to find the name of the node and if that fails, return the 
	 *         node with the smallest range that contains the declaration.
	 */
	private ASTNode getRangeNode()
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
				 if( declName.equals(frag.getName()) ){
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

	/**
	 * @return the source position of this declaration. 
	 *         Return null if this declaration did not come from source or 
	 *         if the declaration is (or is part of) a secondary type that is defined 
	 *         outside of the file associated with the environment.
	 */
    public SourcePosition getPosition()
    {
        if( isFromSource() ){
			final ASTNode node = getRangeNode();
			if( node == null ) return null;			       
            final CompilationUnit unit = getCompilationUnit();
            final int start = node.getStartPosition();
            return new SourcePositionImpl(start,
					node.getLength(),
					unit.lineNumber(start),
					this);
        }
        return null;
    }

    /**
     * @return the list of annotation ast node on the given body declaration.
     * This declaration must came from source. 
     * Return the empty list if the declaration is part of a secondary type outside
     * of the file associated with the environment.
     */
    List<org.eclipse.jdt.core.dom.Annotation> getAnnotations()
    {
        assert isFromSource() : "Declaration did not come from source.";
        final BodyDeclaration decl = (BodyDeclaration)getAstNode(); 
		if( decl == null ) return Collections.emptyList();
        final List<IExtendedModifier> extMods = decl.modifiers();
        if( extMods == null || extMods.isEmpty() ) return Collections.emptyList();
        List<org.eclipse.jdt.core.dom.Annotation> annos = new ArrayList<org.eclipse.jdt.core.dom.Annotation>(4);
        for( IExtendedModifier extMod : extMods ){
            if( extMod.isAnnotation() )
                annos.add((org.eclipse.jdt.core.dom.Annotation)extMod);
        }
        return annos;
    }    
} 
