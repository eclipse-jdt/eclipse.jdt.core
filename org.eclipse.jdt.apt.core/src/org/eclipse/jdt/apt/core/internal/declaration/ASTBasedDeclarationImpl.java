/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
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
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.util.SourcePosition;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * Most mirror implementation are based on bindings but in some cases because of
 * incomplete information no bindings will be created at all. This implementation
 * is to allow clients to get to the partial information that's available on certain
 * declarations.
 *
 * The prefered implementation of the API is to rely on the binding since it is a more
 * complete and versatile representation of a declaration.
 */
public abstract class ASTBasedDeclarationImpl extends EclipseDeclarationImpl {

	static final String EMPTY_STRING = ""; //$NON-NLS-1$
	/** either a <code>BodyDeclaration</code> or a <code>VariableDeclaration</code> */
	protected final ASTNode _astNode;
	/** the file which this ast node came from */
	protected final IFile _file;
	public ASTBasedDeclarationImpl(
			ASTNode astNode,
			IFile file,
			BaseProcessorEnv env)
	{
		super(env);
		assert astNode != null : "ast node cannot be missing"; //$NON-NLS-1$
		assert file != null : "file cannot be missing";//$NON-NLS-1$

		assert astNode instanceof BodyDeclaration ||
		   astNode instanceof VariableDeclaration :
		   "ast node must be either a body declaration or a variable declaration"; //$NON-NLS-1$

		_astNode = astNode;
		_file = file;
	}

	@Override
	public Collection<Modifier> getModifiers()
	{
		int modBits = 0;
		if( _astNode instanceof BodyDeclaration )
			modBits = ((BodyDeclaration)_astNode).getModifiers();
		else if( _astNode instanceof SingleVariableDeclaration )
			modBits = ((SingleVariableDeclaration)_astNode).getModifiers();
		else{
			ASTNode parent = _astNode.getParent();
			if( _astNode instanceof BodyDeclaration )
				modBits = ((BodyDeclaration)parent).getModifiers();
		}

		return getModifiers(modBits);
	}

	private Collection<Modifier> getModifiers(int modBits)
	{
		final List<Modifier> mods = new ArrayList<>(4);
        if( org.eclipse.jdt.core.dom.Modifier.isAbstract(modBits) )
        	mods.add(Modifier.ABSTRACT);
        if( org.eclipse.jdt.core.dom.Modifier.isFinal(modBits) )
        	mods.add(Modifier.FINAL);
        if( org.eclipse.jdt.core.dom.Modifier.isNative(modBits) )
        	mods.add(Modifier.NATIVE);
        if( org.eclipse.jdt.core.dom.Modifier.isPrivate(modBits) )
        	mods.add(Modifier.PRIVATE);
        if( org.eclipse.jdt.core.dom.Modifier.isProtected(modBits) )
        	mods.add(Modifier.PROTECTED);
        if( org.eclipse.jdt.core.dom.Modifier.isPublic(modBits) )
        	mods.add(Modifier.PUBLIC);
        if( org.eclipse.jdt.core.dom.Modifier.isStatic(modBits) )
        	mods.add(Modifier.STATIC);
        if( org.eclipse.jdt.core.dom.Modifier.isStrictfp(modBits) )
        	mods.add(Modifier.STRICTFP);
        if( org.eclipse.jdt.core.dom.Modifier.isSynchronized(modBits) )
        	mods.add(Modifier.SYNCHRONIZED);
        if( org.eclipse.jdt.core.dom.Modifier.isTransient(modBits) )
        	mods.add(Modifier.TRANSIENT);
        if( org.eclipse.jdt.core.dom.Modifier.isVolatile(modBits) )
        	mods.add(Modifier.VOLATILE);
        return mods;

	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
		final IAnnotationBinding[] instances = getAnnotationInstancesFromAST();
		return _getAnnotation(annotationClass, instances);
    }

    @Override
	public Collection<AnnotationMirror> getAnnotationMirrors()
    {
		final IAnnotationBinding[] instances = getAnnotationInstancesFromAST();
		return _getAnnotationMirrors(instances);
    }

	@SuppressWarnings("rawtypes") // DOM AST API returns raw collections
	private IAnnotationBinding[] getAnnotationInstancesFromAST()
	{
		IAnnotationBinding[] instances = null;
		List extendsMods = null;
		switch( _astNode.getNodeType() )
		{
		case ASTNode.TYPE_DECLARATION:
		case ASTNode.ANNOTATION_TYPE_DECLARATION:
		case ASTNode.ENUM_DECLARATION:
		case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
		case ASTNode.METHOD_DECLARATION:
		case ASTNode.FIELD_DECLARATION:
		case ASTNode.ENUM_CONSTANT_DECLARATION:
			extendsMods = ((BodyDeclaration)_astNode).modifiers();
			break;

		case ASTNode.SINGLE_VARIABLE_DECLARATION:
			extendsMods = ((SingleVariableDeclaration)_astNode).modifiers();
			break;
		case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
			final ASTNode parent = _astNode.getParent();
			if( parent instanceof BodyDeclaration )
				extendsMods = ((BodyDeclaration)parent).modifiers();
			break;

		default:
			throw new IllegalStateException();
		}
		if( extendsMods != null ){
			int count = 0;
			for( Object obj : extendsMods ){
				final IExtendedModifier extMod = (IExtendedModifier)obj;
				if( extMod.isAnnotation() )
					count ++;
			}
			instances = new IAnnotationBinding[count];
			int index = 0;
			for( Object obj : extendsMods ){
				final IExtendedModifier extMod = (IExtendedModifier)obj;
				if( extMod.isAnnotation() )
					instances[index ++] =
						((org.eclipse.jdt.core.dom.Annotation)extMod).resolveAnnotationBinding();
			}
		}
		return instances;
	}

	@Override
	public boolean isFromSource(){ return true; }

	@Override
	ASTNode getAstNode(){
		return _astNode;
	}

    @Override
	CompilationUnit getCompilationUnit(){
        return (CompilationUnit)_astNode.getRoot();
    }

	@Override
	public IFile getResource(){
		return _file;
	}

	@Override
	public SourcePosition getPosition()
	{
		final ASTNode node = getRangeNode();
		if( node == null ) return null;
        final CompilationUnit unit = getCompilationUnit();
        final int start = node.getStartPosition();
        return new SourcePositionImpl(
        		start,
				node.getLength(),
				unit.getLineNumber(start),
				unit.getColumnNumber(start),
				this);

	}

	@Override
	public boolean isBindingBased(){ return false; }

	@Override
	public boolean equals(Object obj)
    {
        if(obj instanceof ASTBasedDeclarationImpl)
        	return _astNode == ((ASTBasedDeclarationImpl)obj)._astNode;

        return false;
    }

	@Override
	public int hashCode(){
		return _astNode.hashCode();
	}
}
