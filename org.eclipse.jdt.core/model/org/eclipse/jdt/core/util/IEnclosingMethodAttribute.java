/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of an enclosing method attribute as described in the JVM specifications.
 * 
 * This interface may be implemented by clients. 
 * 
 * @since 3.0
 */
public interface IEnclosingMethodAttribute extends IClassFileAttribute {
	
	/**
	 * Answer back the method descriptor index as described in the JVM specifications. 
	 * 
	 * @return the method descriptor index as described in the JVM specifications
	 */
	int getMethodDescriptorIndex();

	/**
	 * Answer back the method descriptor value as described in the JVM specifications. 
	 * 
	 * @return the method descriptor value as described in the JVM specifications
	 */
	char[] getMethodDescriptor();
}
