/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * Implementation of a TypeMirror.  TypeMirror represents a type, including
 * types that have no declaration, such as primitives (int, boolean) and
 * types that are specializations of declarations (List<String>).
 */
public class TypeMirrorImpl implements TypeMirror {

	protected final Binding _binding;
	
	/* package */ TypeMirrorImpl(Binding binding) {
		_binding = binding;
	}
	
	/* package */ Binding binding() {
		return _binding;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#getKind()
	 */
	@Override
	public TypeKind getKind() {
		switch (_binding.kind()) {
		// case Binding.TYPE: 
		//case Binding.RAW_TYPE:
		//case Binding.GENERIC_TYPE:
		// case Binding.PARAMETERIZED_TYPE:
		// handled by DeclaredTypeImpl, etc.
		
		// TODO: fill in the rest of these
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
		case Binding.METHOD:
		case Binding.IMPORT:
			throw new IllegalArgumentException("Invalid binding kind: " + _binding.kind()); //$NON-NLS-1$
		case Binding.PACKAGE:
			return TypeKind.PACKAGE;
		case Binding.ARRAY_TYPE:
			return TypeKind.ARRAY;
		case Binding.BASE_TYPE:
			// TODO: return appropriate TypeKind
			throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
		case Binding.WILDCARD_TYPE:
			return TypeKind.WILDCARD;
		case Binding.TYPE_PARAMETER:
			throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
		}
		return null;
	}

}
