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
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Represents JSR 175 Annotation instances in the type-system.
 */ 
public interface IAnnotationInstance{
	
	/**
	 * @return the annotation type of this instance.
	 */
	ReferenceBinding getAnnotationType();

	/**
	 * @return the declared element value pairs of this instance.
	 */
	IElementValuePair[] getElementValuePairs();
}
