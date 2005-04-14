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
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Represents a formal parameter that came from source
 */
public class ParameterDeclarationImpl extends DeclarationImpl implements ParameterDeclaration
{
    static final String ARG = "arg";
	/** this executable that this parameter came from */
	private final ExecutableDeclarationImpl _executable;
	/** thie parameter is the <code>_paramIndex</code>th in <code>_executable</code> */
	private final int _paramIndex;

    // Ideally, we would store the IVariableBinding, but getting to it could be expensive
    // since it requires the ast node (SingleVariableDeclaration).
    // This could have a performance impact if the parameter did not come
    // from the compilation unit that is in the processor environment. In such a case,
    // we will have to go parse and resolve the file that contains the declaration.
    // Currently, the decision is to turn this into a on-demand process. (theodora)
   
    /**
     * Parameter declaration from source files
     * @param the executable that declares this parameter
     * @param type the type of the parameter
     * @param index the index of this parameter in <code>executable</code>'s param list.
     */
    public ParameterDeclarationImpl(ExecutableDeclarationImpl executable, 
									ITypeBinding type, 
									int index,
									ProcessorEnvImpl env)
    {	
		super(type, env);
		_executable = executable;
		_paramIndex = index;
        assert _executable != null : "missing executable";
        assert _paramIndex > 0 : "invalid param index " + _paramIndex;
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitParameterDeclaration(this);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
        final SingleVariableDeclaration paramDecl = getAstNode();
        if( paramDecl == null ) return null;
        final List<IExtendedModifier> extMods = paramDecl.modifiers();
        if( extMods == null || extMods.isEmpty() ) return null;
        final List<org.eclipse.jdt.core.dom.Annotation> annos =
            new ArrayList<org.eclipse.jdt.core.dom.Annotation>();
        for( IExtendedModifier extMod : extMods ){
            if( extMod.isAnnotation() )
                annos.add((org.eclipse.jdt.core.dom.Annotation)extMod);
        }
        return _getAnnotation(annotationClass, annos);
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {
        final SingleVariableDeclaration paramDecl = getAstNode();
        if( paramDecl == null ) return Collections.emptyList();
        final List<IExtendedModifier> extMods = paramDecl.modifiers();
        if( extMods == null || extMods.isEmpty() )
            return Collections.emptyList();
        final List<org.eclipse.jdt.core.dom.Annotation> annos =
            new ArrayList<org.eclipse.jdt.core.dom.Annotation>();
        for( IExtendedModifier extMod : extMods ){
            if( extMod.isAnnotation() )
                annos.add((org.eclipse.jdt.core.dom.Annotation)extMod);
        }
        return _getAnnotationMirrors(annos);
    }

    public String getDocComment()
    {
        return null;
    }   

    public SourcePosition getPosition()
    {
		final ASTNode node = getAstNode();
		if( node == null ) return null;
        final CompilationUnit unit = _executable.getCompilationUnit();
        return new SourcePositionImpl(node.getStartPosition(),
                                      node.getLength(),
                                      unit.lineNumber(node.getStartPosition()),
                                      this);
    }

    public String getSimpleName()
    {
        final SingleVariableDeclaration decl = getAstNode();
        if( decl == null ) return ARG + _paramIndex;
        final SimpleName name = decl.getName();
        return name == null ? ARG : name.toString();
    }

    public TypeMirror getType()
    {
        final TypeMirror mirrorType = Factory.createTypeMirror(getTypeBinding(), _env);
        if( mirrorType == null )
            return Factory.createErrorClassType(getTypeBinding());
        return mirrorType;
    }

    private ITypeBinding getTypeBinding(){ return (ITypeBinding)_binding; }

    public MirrorKind kind(){ return MirrorKind.FORMAL_PARAMETER; }

    public int hashCode(){ return _executable.getDeclarationBinding().hashCode() + _paramIndex; }

    public boolean equals(Object obj){
        if( obj instanceof ParameterDeclarationImpl ){
            final ParameterDeclarationImpl otherParam = (ParameterDeclarationImpl)obj;
            return otherParam._paramIndex == _paramIndex  &&
                   otherParam._executable.getDeclarationBinding().isEqualTo(_executable.getDeclarationBinding()) ;
        }
        return false;
    }

    public String toString(){
        return getSimpleName();
    }
	
	public IBinding getDeclarationBinding(){ throw new UnsupportedOperationException("should never be called"); }
	
	boolean isFromSource(){ return true; }

    SingleVariableDeclaration getAstNode()
    {
        final MethodDeclaration methodDecl = (MethodDeclaration)_executable.getAstNode();
		if( methodDecl == null ) return null;
        return (SingleVariableDeclaration)methodDecl.parameters().get(_paramIndex);
    }

    CompilationUnit getCompilationUnit()
    {
        return _executable.getCompilationUnit();
    }

    public IResource getResource(){
        return _executable.getResource();
    }
}
