/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of nest members attribute as described in the JVM
 * specifications.
 *
 * @since 3.15 BETA_JAVA11
 */
public interface INestMembersAttribute extends IClassFileAttribute {

	/**
	 * Answer back the number of nest members as specified in
	 * the JVM specifications.
	 *
	 * @return the number of nest members as specified in
	 * the JVM specifications
	 */
	int getNumberOfNestMembers();

	/**
	 * Answer back the array of nest member attribute entries as specified in
	 * the JVM specifications, or an empty array if none.
	 *
	 * @return the array of nest member attribute entries as specified in
	 * the JVM specifications, or an empty array if none
	 */
	INestMemberAttributeEntry[] getNestMemberAttributesEntries();
}
