/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
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

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.PackageUtil;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

public class PackageDeclarationImpl extends DeclarationImpl implements PackageDeclaration
{   
	// If this package came from directly requesting it via the environment,
	// need to hide the source position, as this is an artifact of our implementation
	private final boolean _hideSourcePosition;
	
	/** The back-pointer to the type declaration that created this package declaration
	 * @see TypeDeclarationImpl#getPackage()
	 */
	private final TypeDeclarationImpl _typeDecl;
	
	// Lazily initialized unless specified in constructor.
	private IPackageFragment[] _pkgFragments = null;
	
    public PackageDeclarationImpl(
			final IPackageBinding binding, 
			final TypeDeclarationImpl typeDecl, 
			final BaseProcessorEnv env,
			final boolean hideSourcePosition)
    {
        this(binding, 
        	 typeDecl, 
        	 env, 
        	 hideSourcePosition, 
        	 null);
    }
    
    public PackageDeclarationImpl(
			final IPackageBinding binding, 
			final TypeDeclarationImpl typeDecl, 
			final BaseProcessorEnv env,
			final boolean hideSourcePosition,
			final IPackageFragment[] pkgFragments)
    {
        super(binding, env);   
		_typeDecl = typeDecl;
		_hideSourcePosition = hideSourcePosition;
		_pkgFragments = pkgFragments;
    }

    public IPackageBinding getPackageBinding(){ return (IPackageBinding)_binding; }

    public void accept(DeclarationVisitor visitor)
    {
        visitor.visitPackageDeclaration(this);
    }
    
    public <A extends Annotation> A getAnnotation(Class<A> anno)
    {
		return _getAnnotation(anno, getPackageBinding().getAnnotations());
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {	
		return _getAnnotationMirrors(getPackageBinding().getAnnotations());
    }

    public Collection<AnnotationTypeDeclaration> getAnnotationTypes()
    {
		// jdt currently have no support for package declaration.
		return Collections.emptyList();	
    }

    public Collection<ClassDeclaration> getClasses() {
    	initFragments();
    	List<IType> types = getTypesInPackage(_pkgFragments);
		List<ClassDeclaration> classes = new ArrayList<ClassDeclaration>();
		for (IType type : types) {
			try {
				// isClass() will return true if TypeDeclaration is an InterfaceDeclaration
				if (type.isClass()) {
					TypeDeclaration td = _env.getTypeDeclaration( type );
					if ( td instanceof ClassDeclaration ) {				
						classes.add((ClassDeclaration)td);
					}
				}
			}
			catch (JavaModelException ex) {} // No longer exists, don't return it
		}
		
		return classes;
    }

    public Collection<EnumDeclaration> getEnums() {
    	initFragments();
    	List<IType> types = getTypesInPackage(_pkgFragments);
		List<EnumDeclaration> enums = new ArrayList<EnumDeclaration>();
		for (IType type : types) {
			try {
				if (type.isEnum()) {
					enums.add((EnumDeclaration)_env.getTypeDeclaration(type));
				}
			}
			catch (JavaModelException ex) {} // No longer exists, don't return it
		}
		
		return enums;
    }

    public Collection<InterfaceDeclaration> getInterfaces() {
    	initFragments();
    	List<IType> types = getTypesInPackage(_pkgFragments);
		List<InterfaceDeclaration> interfaces = new ArrayList<InterfaceDeclaration>();
		for (IType type : types) {
			try {
				if (type.isInterface()) {
					interfaces.add((InterfaceDeclaration)_env.getTypeDeclaration(type));
				}
			}
			catch (JavaModelException ex) {} // No longer exists, don't return it
		}
		
		return interfaces;
    }

    public String getDocComment()
    {
        return null;
    }

    public Collection<Modifier> getModifiers()
    {
        // package doesn't have modifiers.
        return Collections.emptyList();
    }

    public SourcePosition getPosition()
    {
		if (_hideSourcePosition)
			return null;
		if (isFromSource()){
			final CompilationUnit unit = _typeDecl.getCompilationUnit();
			final ASTNode node = unit.findDeclaringNode(getDeclarationBinding());
			if( node == null ) return null;
			final int start = node.getStartPosition();			
	        return new SourcePositionImpl(start,
										  node.getLength(),
	                                      unit.getLineNumber(start),
	                                      unit.getColumnNumber(start),
	                                      this);			
		}
		return null;
        
    }

    public String getQualifiedName()
    {
		return getPackageBinding().getName();
    }   

    public String getSimpleName()
    {
        IPackageBinding pkg = getPackageBinding();
        final String[] components = pkg.getNameComponents();
        if( components == null || components.length == 0 ) return ""; //$NON-NLS-1$
        return components[components.length - 1];
    }

    public MirrorKind kind(){ return MirrorKind.PACKAGE; }

    public String toString(){ return getQualifiedName(); }
	
	public IPackageBinding getDeclarationBinding(){ return (IPackageBinding)_binding; }

	public boolean isFromSource(){ return _typeDecl != null && _typeDecl.isFromSource(); }
	
	/**
	 * Make sure to call this before attempting to access _pkgFragments.
	 * We initialize this field lazily, because it is very expensive to compute and
	 * there are some common questions such as getQualifiedName() that can be
	 * answered without initializing it at all.
	 */
	private void initFragments() {
		if (null == _pkgFragments) {
			_pkgFragments = PackageUtil.getPackageFragments(_binding.getName(), _env);
		}
	}
	
	private static List<IType> getTypesInPackage(final IPackageFragment[] fragments) {
		List<IType> types = new ArrayList<IType>();
		try {
			// Get all top-level classes -- ignore local, member, and anonymous classes
			for (IPackageFragment fragment : fragments) {
				for (IClassFile classFile : fragment.getClassFiles()) {
					IType type = classFile.getType();
					if (! (type.isLocal() || type.isMember() || type.isAnonymous()) ) {
						types.add(type);
					}
				}
				for (ICompilationUnit compUnit : fragment.getCompilationUnits()) {
					for (IType type : compUnit.getTypes()) {
						if (! (type.isLocal() || type.isMember() || type.isAnonymous()) ) {
							types.add(type);
						}
					}
				}
			}
		}
		catch (JavaModelException jme) {
			// Ignore -- project is in a bad state. This will get recalled if necessary
		}
		return types;
	}
   
}
