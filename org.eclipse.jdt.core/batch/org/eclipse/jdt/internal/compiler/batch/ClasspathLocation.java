/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public abstract class ClasspathLocation implements FileSystem.Classpath,
		SuffixConstants {

	private AccessRuleSet accessRuleSet;

	public ClasspathLocation(AccessRuleSet accessRuleSet) {
		this.accessRuleSet = accessRuleSet;
	}

	/**
	 * Return the first access rule which is violated when accessing a given
	 * type, or null if no 'non accessible' access rule applies.
	 * 
	 * @param qualifiedBinaryFileName
	 *            tested type specification, formed as:
	 *            "org/eclipse/jdt/core/JavaCore.class"
	 * @return the first access rule which is violated when accessing a given
	 *         type, or null if none applies
	 */
	AccessRestriction fetchAccessRestriction(String qualifiedBinaryFileName) {
		if (this.accessRuleSet == null)
			return null;
		return this.accessRuleSet
				.getViolatedRestriction((qualifiedBinaryFileName.substring(0,
						qualifiedBinaryFileName.length() - 6) + SUFFIX_STRING_java)
						.toCharArray());
	}
}
