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

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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
        final BodyDeclaration astNode = (BodyDeclaration)getAstNode();
        if(astNode != null){
            final List<org.eclipse.jdt.core.dom.Annotation> annoInstances = getAnnotations();
            return _getAnnotation(annotationClass, annoInstances);
        }
        else{
            // TODO: (theodora) handle the binary case.
            return null;
        }
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {
        if( isFromSource() )
        {
            final List<org.eclipse.jdt.core.dom.Annotation> annoInstances = getAnnotations();
            return _getAnnotationMirrors(annoInstances);
        }
        else{
            // TODO: (theodora) handle the binary case.
            return null;
        }
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
