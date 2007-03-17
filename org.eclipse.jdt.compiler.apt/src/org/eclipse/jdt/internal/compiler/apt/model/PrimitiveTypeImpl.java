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

import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;

import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * 
 * @since 3.3
 */
public class PrimitiveTypeImpl extends TypeMirrorImpl implements PrimitiveType, NoType {

	PrimitiveTypeImpl(BaseTypeBinding binding) {
		super(binding);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.TypeMirrorImpl#getKind()
	 */
	@Override
	public TypeKind getKind() {
		switch (((BaseTypeBinding)_binding).id) {
		case TypeIds.T_boolean:
			return TypeKind.BOOLEAN;
		case TypeIds.T_byte:
			return TypeKind.BYTE;
		case TypeIds.T_char:
			return TypeKind.CHAR;
		case TypeIds.T_double:
			return TypeKind.DOUBLE;
		case TypeIds.T_float:
			return TypeKind.FLOAT;
		case TypeIds.T_int:
			return TypeKind.INT;
		case TypeIds.T_long:
			return TypeKind.LONG;
		case TypeIds.T_short:
			return TypeKind.SHORT;
		case TypeIds.T_void:
			return TypeKind.VOID;
		case TypeIds.T_undefined:
			return TypeKind.NONE;
		default:
			throw new IllegalArgumentException("BaseTypeBinding of unexpected id " + ((BaseTypeBinding)_binding).id); //$NON-NLS-1$
		}
	}

}
