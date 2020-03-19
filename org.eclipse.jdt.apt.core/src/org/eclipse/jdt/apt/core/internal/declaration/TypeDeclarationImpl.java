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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;

public abstract class TypeDeclarationImpl extends MemberDeclarationImpl
	implements TypeDeclaration, DeclaredType, ReferenceType, EclipseMirrorType
{
	// jdt core compiler add a field to a type with the following name when there is a hierachy problem with the type.
	private static final String HAS_INCONSISTENT_TYPE_HIERACHY = "has inconsistent hierarchy"; //$NON-NLS-1$
    public TypeDeclarationImpl(final ITypeBinding binding,
                               final BaseProcessorEnv env)
    {
        super(binding, env);
    }

    @Override
	public String getQualifiedName()
    {
        ITypeBinding type = getTypeBinding();
        return type.getQualifiedName();
    }

    @Override
	public String getSimpleName()
    {
    	ITypeBinding type = getTypeBinding();
    	return type.getName();
    }

    @Override
	public PackageDeclaration getPackage()
    {
        ITypeBinding binding = getDeclarationBinding();
		return new PackageDeclarationImpl(binding.getPackage(), this, _env, false);
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitTypeDeclaration(this);
    }

    @Override
	public ITypeBinding getTypeBinding(){ return (ITypeBinding)_binding; }

	private void getASTFields(
    		final AbstractTypeDeclaration typeDecl,
    		final List<FieldDeclaration> results){
    	final List<?> bodyDecls = typeDecl.bodyDeclarations();
    	for( int i=0, len=bodyDecls.size(); i<len; i++ ){
    		final BodyDeclaration bodyDecl = (BodyDeclaration)bodyDecls.get(i);
    		IFile file = null;
    		if( bodyDecl.getNodeType() == ASTNode.FIELD_DECLARATION ){
    			final List<VariableDeclarationFragment> fragments =
                    ((org.eclipse.jdt.core.dom.FieldDeclaration)bodyDecl).fragments();
    			for( VariableDeclarationFragment frag : fragments ){
    				final IBinding fieldBinding = frag.resolveBinding();
    				if( fieldBinding == null ){
    					if( file == null )
    						file = getResource();
    					final EclipseDeclarationImpl decl = Factory.createDeclaration(frag, file, _env);
    					if( decl != null )
        					results.add((FieldDeclaration)decl);
    				}
    			}
    		}
    	}
    }

    @Override
	public Collection<FieldDeclaration> getFields()
    {
    	final List<FieldDeclaration> results = new ArrayList<>();
    	final ITypeBinding typeBinding = getDeclarationBinding();
    	if( isFromSource() ){
    		final ASTNode node =
    			_env.getASTNodeForBinding(typeBinding);
    		if( node != null ){
    			switch( node.getNodeType() )
    			{
    			case ASTNode.TYPE_DECLARATION:
    			case ASTNode.ANNOTATION_TYPE_DECLARATION:
    			case ASTNode.ENUM_DECLARATION:
    				AbstractTypeDeclaration typeDecl =
    					(AbstractTypeDeclaration)node;
    				// built the ast based methods first.
    				getASTFields(typeDecl, results);
    				break;
    			default:
    				// the ast node for a type binding should be a AbstractTypeDeclaration.
    				throw new IllegalStateException("expecting a AbstractTypeDeclaration but got "  //$NON-NLS-1$
    						+ node.getClass().getName() );
    			}
    		}
    	}
    	// either type is binary or
    	// constructing the binding based fields for source type.
        final IVariableBinding[] fields = typeBinding.getDeclaredFields();
        for( IVariableBinding field : fields ){
        	// note that the name HAS_INCONSISTENT_TYPE_HIERACHY is not a legal java identifier
        	// so there is no chance that we are filtering out actual declared fields.
        	if( field.isSynthetic() || HAS_INCONSISTENT_TYPE_HIERACHY.equals(field.getName()))
        		continue;
            Declaration mirrorDecl = Factory.createDeclaration(field, _env);
            if( mirrorDecl != null)
                results.add( (FieldDeclaration)mirrorDecl);
        }
        return results;
    }

    @Override
	public Collection<TypeDeclaration> getNestedTypes()
    {
        final ITypeBinding[] memberTypes = getDeclarationBinding().getDeclaredTypes();
        final List<TypeDeclaration> results = new ArrayList<>(memberTypes.length);
        for( ITypeBinding type : memberTypes ){
            Declaration mirrorDecl = Factory.createReferenceType(type, _env);
            if( mirrorDecl != null )
                results.add((TypeDeclaration)mirrorDecl);
        }
        return results;
    }

    @Override
	public Collection<TypeParameterDeclaration> getFormalTypeParameters()
    {
        final ITypeBinding[] typeParams = getDeclarationBinding().getTypeParameters();
        final List<TypeParameterDeclaration> results = new ArrayList<>(typeParams.length);
        for( ITypeBinding typeParam : typeParams ){
            Declaration mirrorDecl = Factory.createDeclaration(typeParam, _env);
            if( mirrorDecl != null )
                results.add( (TypeParameterDeclaration)mirrorDecl );
        }
        return results;
    }

    @Override
	public TypeDeclaration getDeclaringType()
    {
        final ITypeBinding decl = getDeclarationBinding();
        if( decl.isMember() )
        	return Factory.createReferenceType(decl.getDeclaringClass(), _env);
        return null;
    }

    // Start of implementation of DeclaredType API
    @Override
	public Collection<TypeMirror> getActualTypeArguments()
    {
        final ITypeBinding type = getTypeBinding();
        final ITypeBinding[] typeArgs = type.getTypeArguments();
        if( typeArgs == null || typeArgs.length == 0 )
    		return Collections.emptyList();

        final Collection<TypeMirror> result = new ArrayList<>(typeArgs.length);
        for( ITypeBinding arg : typeArgs ){
            final TypeMirror mirror = Factory.createTypeMirror(arg, _env);
            if (mirror == null)
                result.add(Factory.createErrorClassType(arg));
            else
                result.add(mirror);
        }

        return result;
    }

    @Override
	public DeclaredType getContainingType()
    {
        final ITypeBinding outer = getTypeBinding().getDeclaringClass();
        return Factory.createReferenceType(outer, _env);
    }

    @Override
	public TypeDeclaration getDeclaration()
    {
        final ITypeBinding declBinding = getDeclarationBinding();
        if( declBinding == _binding ) return this;
        else return Factory.createReferenceType(declBinding, _env);
    }

    @Override
	public Collection<InterfaceType> getSuperinterfaces()
    {
        final ITypeBinding[] superInterfaceBindings = getDeclarationBinding().getInterfaces();
        if( superInterfaceBindings == null || superInterfaceBindings.length == 0 )
            return Collections.emptyList();
        final List<InterfaceType> results = new ArrayList<>(superInterfaceBindings.length);
        for( ITypeBinding binding : superInterfaceBindings ){
            if( binding.isInterface() ){
                final TypeDeclarationImpl mirrorDecl = Factory.createReferenceType(binding, _env);
                if( mirrorDecl != null && mirrorDecl.kind() == MirrorKind.TYPE_INTERFACE ){
                    results.add((InterfaceType)mirrorDecl);
                }
            }
            else results.add(Factory.createErrorInterfaceType(binding));
        }
        return results;
    }


    // End of implementation of DeclaredType API

    @Override
	public ITypeBinding getDeclarationBinding()
    {
        final ITypeBinding type = getTypeBinding();
        return type.getTypeDeclaration();
    }

    /**
     * create mirror methods that does not have a binding represention.
     */
    @SuppressWarnings("rawtypes")
	protected void getASTMethods(
    		final AbstractTypeDeclaration typeDecl,
    		final List<MethodDeclaration> results){
    	final List bodyDecls = typeDecl.bodyDeclarations();
    	IFile file = null;
    	for( int i=0, len=bodyDecls.size(); i<len; i++ ){
    		final BodyDeclaration bodyDecl = (BodyDeclaration)bodyDecls.get(i);
    		switch(bodyDecl.getNodeType()){
    		case ASTNode.METHOD_DECLARATION:
    			final org.eclipse.jdt.core.dom.MethodDeclaration methodDecl =
    					(org.eclipse.jdt.core.dom.MethodDeclaration)bodyDecl;

    			if( !methodDecl.isConstructor() ){
    				final IMethodBinding methodBinding = methodDecl.resolveBinding();
    				// built an ast based representation.
    				if( methodBinding == null ){
    					if( file == null )
        					file = getResource();
        				MethodDeclaration mirrorDecl =
        					(MethodDeclaration)Factory.createDeclaration(methodDecl, file, _env);
        				if( mirrorDecl != null )
        					results.add(mirrorDecl);
    				}
    			}
    			break;
    		case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
    			final AnnotationTypeMemberDeclaration memberDecl =
    				(AnnotationTypeMemberDeclaration)bodyDecl;
    			final IMethodBinding methodBinding = memberDecl.resolveBinding();
				// built an ast based representation.
				if( methodBinding == null ){
					if( file == null )
    					file = getResource();
    				MethodDeclaration mirrorDecl =
    					(MethodDeclaration)Factory.createDeclaration(memberDecl, file, _env);
    				if( mirrorDecl != null )
    					results.add(mirrorDecl);
				}
				break;
    		}
    	}
    }

    protected List<? extends MethodDeclaration> _getMethods()
    {
    	final List<MethodDeclaration> results = new ArrayList<>();
    	if( isFromSource() ){
    		// need to consult the ast since methods with broken signature
    		// do not appear in bindings.
    		final ITypeBinding typeBinding = getDeclarationBinding();
    		final ASTNode node =
    			_env.getASTNodeForBinding(typeBinding);
    		if( node != null ){
    			switch( node.getNodeType() )
    			{
    			case ASTNode.TYPE_DECLARATION:
    			case ASTNode.ANNOTATION_TYPE_DECLARATION:
    			case ASTNode.ENUM_DECLARATION:
    				AbstractTypeDeclaration typeDecl =
    					(AbstractTypeDeclaration)node;
    				// built the ast based methods first.
    				getASTMethods(typeDecl, results);
    				break;
    			default:
    				// the ast node for a type binding should be a AbstractTypeDeclaration.
    				throw new IllegalStateException("expecting a AbstractTypeDeclaration but got "  //$NON-NLS-1$
    						+ node.getClass().getName() );
    			}
    		}
    	}
        // build methods for binding type or
    	// build the binding based method for source type.
    	final IMethodBinding[] methods = getDeclarationBinding().getDeclaredMethods();
        for( IMethodBinding method : methods ){
            if( method.isConstructor() || method.isSynthetic() ) continue;
            Declaration mirrorDecl = Factory.createDeclaration(method, _env);
            if( mirrorDecl != null)
                results.add((MethodDeclaration)mirrorDecl);
        }
        return results;
    }

    @Override
	public String toString()
    {
    	return getQualifiedName();
    }

    @Override
	public boolean isFromSource(){ return getDeclarationBinding().isFromSource(); }

    // This is not just a call to ITypeBinding.isAssignmentCompatible(),
    // because that can fail in the so-called "multiverse" case where the
    // left and right types are drawn from different parser instances.
	@Override
	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		ITypeBinding leftBinding = left.getTypeBinding();
		if (leftBinding.isPrimitive()) {
			// Primitives, thankfully, are immune to the multiverse problem;
			// but are eligible for autoboxing and unboxing.
			return getTypeBinding().isAssignmentCompatible(leftBinding);
		}
		return isSubTypeCompatible(left);
	}

	@Override
	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		// Operate on erasures - ignore generics for now
		// Also ignore boxing for now
		ITypeBinding thisErased = getTypeBinding().getErasure();
		ITypeBinding typeErased = type.getTypeBinding().getErasure();

		if (kind() == MirrorKind.TYPE_CLASS) {
			if (type.kind() == MirrorKind.TYPE_CLASS)
				return isSubClassOf(thisErased, typeErased);
			if (type.kind() == MirrorKind.TYPE_INTERFACE)
				return isImplementorOf(thisErased, typeErased);
			return false;
		}
		else { //kind() == MirrorKind.TYPE_INTERFACE
			if (type.kind() == MirrorKind.TYPE_INTERFACE)
				return isImplementorOf(thisErased, typeErased);
			if (type.kind() == MirrorKind.TYPE_CLASS)
				return "java.lang.Object".equals(getQualifiedName()); //$NON-NLS-1$
			return false;
		}
	}

	private static boolean isImplementorOf(ITypeBinding t1, ITypeBinding t2) {
		if (eq(t1,t2)) return true;
		ITypeBinding[] intfs = t1.getInterfaces();

		for (ITypeBinding intf : intfs) {
			if (isImplementorOf(intf.getErasure(), t2))
				return true;
		}
        ITypeBinding superClass = t1.getSuperclass();
        if (superClass != null) {
                if (isImplementorOf(superClass, t2)) {
                        return true;
                }
        }
		return false;
	}

	private static boolean isSubClassOf(ITypeBinding t1, ITypeBinding t2) {
		while(t1 != null) {
			if (eq(t1, t2))	return true;
			t1 = t1.getSuperclass();
		}
		return false;
	}

	private static boolean eq(ITypeBinding t1, ITypeBinding t2) {
		return t1.getQualifiedName().equals(t2.getQualifiedName());
	}
}
