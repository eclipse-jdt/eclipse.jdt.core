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

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Creates javax.lang.model wrappers around JDT internal compiler bindings.
 */
public class ElementFactory {

	public static Element newElement(Binding binding) {
		switch (binding.kind()) {
		// TODO: fill in the rest of these
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
			throw new UnsupportedOperationException();
		case Binding.TYPE:
			return TypeElementImpl.newTypeElementImpl((ReferenceBinding)binding);
		case Binding.METHOD:
		case Binding.PACKAGE:
		case Binding.IMPORT:
		case Binding.ARRAY_TYPE:
		case Binding.BASE_TYPE:
		case Binding.PARAMETERIZED_TYPE:
		case Binding.WILDCARD_TYPE:
		case Binding.RAW_TYPE:
		case Binding.GENERIC_TYPE:
		case Binding.TYPE_PARAMETER:
			throw new UnsupportedOperationException();
		}
		return null;
	}
	
}
