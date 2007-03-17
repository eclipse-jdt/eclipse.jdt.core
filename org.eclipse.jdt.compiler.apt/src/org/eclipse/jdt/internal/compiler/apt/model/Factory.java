/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Creates javax.lang.model wrappers around JDT internal compiler bindings.
 */
public class Factory {

	public static Element newElement(Binding binding) {
		switch (binding.kind()) {
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
			return new VariableElementImpl(binding);
		case Binding.TYPE:
			return new TypeElementImpl((ReferenceBinding)binding);
		case Binding.METHOD:
			return new ExecutableElementImpl((MethodBinding)binding);
		// TODO: fill in the rest of these
		case Binding.PACKAGE:
		case Binding.IMPORT:
		case Binding.ARRAY_TYPE:
		case Binding.BASE_TYPE:
		case Binding.PARAMETERIZED_TYPE:
		case Binding.WILDCARD_TYPE:
		case Binding.RAW_TYPE:
		case Binding.GENERIC_TYPE:
		case Binding.TYPE_PARAMETER:
			throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
		}
		return null;
	}
	
	public static DeclaredType newDeclaredType(ReferenceBinding binding) {
		if (binding.kind() == Binding.WILDCARD_TYPE) {
			// JDT wildcard binding is a subclass of reference binding, but in JSR269 they're siblings
			throw new IllegalArgumentException("A wildcard binding can't be turned into a DeclaredType"); //$NON-NLS-1$
		}
		return new DeclaredTypeImpl(binding);
	}

	public static TypeMirror newTypeMirror(Binding binding) {
		switch (binding.kind()) {
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
			// For variables, return the type of the variable
			return newTypeMirror(((VariableBinding)binding).type);
			
		case Binding.METHOD:
		case Binding.PACKAGE:
		case Binding.IMPORT:
			throw new IllegalArgumentException("Invalid binding kind: " + binding.kind()); //$NON-NLS-1$
			
		case Binding.TYPE:
			return new DeclaredTypeImpl((ReferenceBinding)binding);
			
		case Binding.ARRAY_TYPE:
			return new ArrayTypeImpl((ArrayBinding)binding);
			
		case Binding.BASE_TYPE:
			// PrimitiveTypeImpl implements both PrimitiveType and NoType
			return new PrimitiveTypeImpl((BaseTypeBinding)binding);
			
			// TODO: fill in the rest of these
		case Binding.PARAMETERIZED_TYPE:
		case Binding.WILDCARD_TYPE:
		case Binding.RAW_TYPE:
		case Binding.GENERIC_TYPE:
		case Binding.TYPE_PARAMETER:
			throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
		}
		return null;
	}

}
