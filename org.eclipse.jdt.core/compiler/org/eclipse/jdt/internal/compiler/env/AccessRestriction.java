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
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Definition of an access restriction rule used to flag forbidden references to non API code.
 * A restriction can chain to further ones, the first violated restriction taking precedence.
 */
public class AccessRestriction {

	private char[][] inclusionPatterns;
	private char[][] exclusionPatterns;
	protected String messageTemplate;
	AccessRestriction furtherRestriction; // subsequent restriction
	
	
	public AccessRestriction(String messageTemplate, char[][] inclusionPatterns, char[][] exclusionPatterns, AccessRestriction furtherRestriction) {
		this.messageTemplate = messageTemplate;
		this.inclusionPatterns = inclusionPatterns;
		this.exclusionPatterns = exclusionPatterns;
		this.furtherRestriction = furtherRestriction;
	}
	/**
	 * Select the first restriction which is violated when accessing a given type, or null if no restriction applies.
	 * Type name is formed as: "java/lang/Object".
	 */
	public AccessRestriction getViolatedRestriction(char[] targetTypeName, char[] referringTypeName) {
		
		// check local inclusion/exclusion rules
		if (this.inclusionPatterns != null || this.exclusionPatterns != null) {
			if (Util.isExcluded(targetTypeName, this.inclusionPatterns, this.exclusionPatterns, false)) {
				return this;
			}
		}		
	// then check further restrictions
		return this.furtherRestriction != null 
						? this.furtherRestriction.getViolatedRestriction(targetTypeName, referringTypeName)
						: null;
	}
	/**

	 * Returns readable description for problem reporting, 
	 * message is expected to contain room for restricted type name
	 * e.g. "{0} has restricted access"
	 */
	public String getMessageTemplate() {
			return this.messageTemplate;
	}
}
