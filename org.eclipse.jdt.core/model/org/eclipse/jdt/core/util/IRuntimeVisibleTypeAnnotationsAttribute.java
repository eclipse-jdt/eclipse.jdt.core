/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a runtime visible type annotations attribute as described in the JVM specifications
 * (added in J2SE 1.5).
 *
 * This interface may be implemented by clients.
 *
 * @since 3.6
 */
public interface IRuntimeVisibleTypeAnnotationsAttribute extends IClassFileAttribute {

	/**
	 * Answer back the number of annotations as described in the JVM specifications.
	 *
	 * @return the number of annotations
	 */
	int getExtendedAnnotationsNumber();

	/**
	 * Answer back the extended annotations. Answers an empty collection if none.
	 *
	 * @return the extended annotations. Answers an empty collection if none.
	 */
	IExtendedAnnotation[] getExtendedAnnotations();
}
