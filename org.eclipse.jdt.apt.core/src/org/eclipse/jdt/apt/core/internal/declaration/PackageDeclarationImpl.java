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
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.PackageUtil;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;

import com.sun.mirror.declaration.*;
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
    public PackageDeclarationImpl(
			final IPackageBinding binding, 
			final TypeDeclarationImpl typeDecl, 
			final ProcessorEnvImpl env,
			final boolean hideSourcePosition)
    {
        super(binding, env);   
		_typeDecl = typeDecl;
		_hideSourcePosition = hideSourcePosition;
    }

    public IPackageBinding getPackageBinding(){ return (IPackageBinding)_binding; }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
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
		return PackageUtil.getClasses(this, _env);
    }

    public Collection<EnumDeclaration> getEnums() {
		return PackageUtil.getEnums(this, _env);
    }

    public Collection<InterfaceDeclaration> getInterfaces() {
		return PackageUtil.getInterfaces(this, _env);
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
		if(_typeDecl.isFromSource()){
			final CompilationUnit unit = _typeDecl.getCompilationUnit();
			final ASTNode node = unit.findDeclaringNode(getDeclarationBinding());
			if( node == null ) return null;
			final int start = node.getStartPosition();
	        return new SourcePositionImpl(start,
										  node.getLength(),
	                                      unit.lineNumber(start),
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
        if( components == null || components.length == 0 ) return "";
        return components[components.length - 1];
    }

    public MirrorKind kind(){ return MirrorKind.PACKAGE; }

    public String toString(){ return getQualifiedName(); }
	
	public IPackageBinding getDeclarationBinding(){ return (IPackageBinding)_binding; }

    boolean isFromSource(){ return _typeDecl.isFromSource(); }
   
}
