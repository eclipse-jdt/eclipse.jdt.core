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

import java.io.File;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public abstract class ClasspathLocation implements FileSystem.Classpath,
		SuffixConstants {

	public static final int SOURCE = 1;
	public static final int BINARY = 2;
	
	protected AccessRuleSet accessRuleSet;
	protected String encoding; // only useful if referenced in the source path

	protected ClasspathLocation(AccessRuleSet accessRuleSet) {
		this.accessRuleSet = accessRuleSet;
	}

	/**
	 * Return the first access rule which is violated when accessing a given
	 * type, or null if no 'non accessible' access rule applies.
	 * 
	 * @param qualifiedBinaryFileName
	 *            tested type specification, formed as:
	 *            "org/eclipse/jdt/core/JavaCore.class"; on systems that
	 *            use \ as File.separator, the 
	 *            "org\eclipse\jdt\core\JavaCore.class" is accepted as well
	 * @return the first access rule which is violated when accessing a given
	 *         type, or null if none applies
	 */
	protected AccessRestriction fetchAccessRestriction(String qualifiedBinaryFileName) {
		if (this.accessRuleSet == null)
			return null;
		char [] qualifiedTypeName = qualifiedBinaryFileName.
			substring(0, qualifiedBinaryFileName.length() - SUFFIX_CLASS.length)
			.toCharArray(); 
		if (File.separatorChar == '\\') {
			CharOperation.replace(qualifiedTypeName, File.separatorChar, '/');
		}
		return this.accessRuleSet.getViolatedRestriction(qualifiedTypeName);
	}
}
