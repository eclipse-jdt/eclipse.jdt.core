/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
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

package org.eclipse.jdt.apt.core.internal.util;

import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.util.Declarations;
import org.eclipse.jdt.apt.core.internal.NonEclipseImplementationException;
import org.eclipse.jdt.apt.core.internal.declaration.DeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject;
import org.eclipse.jdt.apt.core.internal.declaration.MemberDeclarationImpl;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

public class DeclarationsUtil implements Declarations
{
    @Override
	public boolean hides(MemberDeclaration sub, MemberDeclaration sup) {

		// A declaration cannot hide itself
		if (sub == sup || sub.equals(sup))
			return false;

		if( ! ((EclipseDeclarationImpl)sub).isBindingBased() ||
			! ((EclipseDeclarationImpl)sup).isBindingBased() )
			return false;

		MemberDeclarationImpl subImpl = (MemberDeclarationImpl)sub;
		MemberDeclarationImpl supImpl = (MemberDeclarationImpl)sup;

		IBinding subBinding = subImpl.getDeclarationBinding();
		IBinding supBinding = supImpl.getDeclarationBinding();


		// Hiding can only take place between declarations of the same kind and name,
		// and types, variables and methods
		int subKind = subBinding.getKind();
		int supKind = supBinding.getKind();
		if (subKind != supKind || subKind == IBinding.PACKAGE)
			return false;
		if (!subBinding.getName().equals(supBinding.getName()))
			return false;

		// Methods must be both static and the sub a subsignature of the sup
		if (subKind == IBinding.METHOD) {
			boolean allowed = false;
			int modifiers = subBinding.getModifiers();
			if ((modifiers & Modifier.STATIC) == Modifier.STATIC) {
				IMethodBinding methodBinding = (IMethodBinding)subBinding;
				if (methodBinding.isSubsignature((IMethodBinding)supBinding)) {
					allowed = true;
				}
			}
			if (!allowed)
				return false;
		}

		// sub's enclosing class must be a subclass of sup's
		ITypeBinding subClass = getDeclaringClass(subBinding);
		ITypeBinding supClass = getDeclaringClass(supBinding);
		if (subClass == null || supClass == null)
			return false;
		if (!subClass.isSubTypeCompatible(supClass))
			return false;

		// sup must be visible from sub
		if (!isVisibleForHiding(supClass, supClass, supBinding.getModifiers()))
			return false;

		return true;
    }

	/**
	 * Is a method, field, type visible from the viewer?
	 * That is, do accessibility rules allow it? (public, protected, etc.)<P>
	 *
	 * Note that we make an assumption about protected here since
	 * its use in hides() already determines that the declaringTarget
	 * must be a subclass of the declaringViewer.
	 */
	private static boolean isVisibleForHiding(
			final ITypeBinding declaringTarget,
			final ITypeBinding declaringViewer,
			final int modifiers) {

		// Public is always visible
		if ((modifiers & Modifier.PUBLIC) == Modifier.PUBLIC)
			return true;
		if ((modifiers & Modifier.PRIVATE) == Modifier.PRIVATE) {
			// Must be the same class
			if (declaringTarget.equals(declaringViewer))
				return true;
			else
				return false;
		}
		if ((modifiers & Modifier.PROTECTED) == Modifier.PROTECTED) {
			// We've already checked for subclassing
			return true;
		}
		// Package-friendly (no accessibility modifier)
		// Classes must be in the same package
		IPackageBinding targetPackage = declaringTarget.getPackage();
		IPackageBinding viewerPackage = declaringViewer.getPackage();
		return targetPackage.equals(viewerPackage);
	}

    @Override
	public boolean overrides(MethodDeclaration sub, MethodDeclaration sup) {
        final IMethodBinding subBinding = (IMethodBinding)getBinding(sub);
        final IMethodBinding supBinding = (IMethodBinding)getBinding(sup);
        if(subBinding == null || supBinding == null) return false;
        return subBinding.overrides(supBinding);
    }

    private static IBinding getBinding(MemberDeclaration memberDecl)
        throws NonEclipseImplementationException
    {
        if( memberDecl == null ) return null;
        if( memberDecl instanceof EclipseMirrorObject ){
        	if( memberDecl instanceof DeclarationImpl )
        		return ((DeclarationImpl)memberDecl).getDeclarationBinding();
        	else
        		return null;
        }
        throw new NonEclipseImplementationException("only applicable to eclipse type system objects." + //$NON-NLS-1$
                                                    " Found " + memberDecl.getClass().getName()); //$NON-NLS-1$
    }

	private static ITypeBinding getDeclaringClass(IBinding binding) {
		int kind = binding.getKind();
		if (kind == IBinding.TYPE)
			return ((ITypeBinding)binding).getDeclaringClass();
		if (kind == IBinding.METHOD)
			return ((IMethodBinding)binding).getDeclaringClass();
		if (kind == IBinding.VARIABLE)
			return ((IVariableBinding)binding).getDeclaringClass();

		// Package binding -- no declaring class
		return null;
	}
}
