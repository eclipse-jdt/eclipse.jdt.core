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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Definition of an access restriction rule used to flag forbidden references to non API code.
 * A restriction can chain to further ones, the first violated restriction taking precedence.
 */
public class AccessRestriction {

	private char[][] accessibleFilesPatterns;
	private char[][] nonAccessibleFilesPatterns;
	protected String messageTemplate;
	AccessRestriction furtherRestriction; // subsequent restriction
	
	
	public AccessRestriction(String messageTemplate, char[][] accessibleFilesPatterns, char[][] nonAccessibleFilesPatterns, AccessRestriction furtherRestriction) {
		this.messageTemplate = messageTemplate;
		this.accessibleFilesPatterns = accessibleFilesPatterns;
		this.nonAccessibleFilesPatterns = nonAccessibleFilesPatterns;
		this.furtherRestriction = furtherRestriction;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (this == object) 
			return true;
		if (!(object instanceof AccessRestriction))
			return false;
		AccessRestriction otherRestriction = (AccessRestriction) object;
		if (!this.messageTemplate.equals(otherRestriction.messageTemplate)) 
			return false;
		if (this.accessibleFilesPatterns != otherRestriction.accessibleFilesPatterns) {
			int length = this.accessibleFilesPatterns == null ? 0 : this.accessibleFilesPatterns.length;
			int otherLength = otherRestriction.accessibleFilesPatterns == null ? 0 : otherRestriction.accessibleFilesPatterns.length;
			if (length != otherLength)
				return false;
			for (int i = 0; i < length; i++) {
				if (!CharOperation.equals(this.accessibleFilesPatterns[i], otherRestriction.accessibleFilesPatterns[i]))
						return false;
			}
		}
		if (this.nonAccessibleFilesPatterns != otherRestriction.nonAccessibleFilesPatterns) {
			int length = this.nonAccessibleFilesPatterns == null ? 0 : this.nonAccessibleFilesPatterns.length;
			int otherLength = otherRestriction.nonAccessibleFilesPatterns == null ? 0 : otherRestriction.nonAccessibleFilesPatterns.length;
			if (length != otherLength)
				return false;
			for (int i = 0; i < length; i++) {
				if (!CharOperation.equals(this.nonAccessibleFilesPatterns[i], otherRestriction.nonAccessibleFilesPatterns[i]))
						return false;
			}
		}
		if (this.furtherRestriction != otherRestriction.furtherRestriction) {
			if (this.furtherRestriction == null || otherRestriction.furtherRestriction == null) 
				return false;
			if (!this.furtherRestriction.equals(otherRestriction.furtherRestriction))
				return false;
		}
		return true;
	}
	/**
	 * Select the first restriction which is violated when accessing a given type, or null if no restriction applies.
	 * Target type file path is formed as: "org/eclipse/jdt/core/JavaCore.java".
	 */
	public AccessRestriction getViolatedRestriction(char[] targetTypeFilePath, char[] referringTypeName) {
		
		// check local inclusion/exclusion rules
		if (this.accessibleFilesPatterns != null || this.nonAccessibleFilesPatterns != null) {
			if (Util.isExcluded(targetTypeFilePath, this.accessibleFilesPatterns, this.nonAccessibleFilesPatterns, false)) {
				return this;
			}
		}		
	// then check further restrictions
		return this.furtherRestriction != null 
						? this.furtherRestriction.getViolatedRestriction(targetTypeFilePath, referringTypeName)
						: null;
	}
	public char[][] getExclusionPatterns() {
			return this.nonAccessibleFilesPatterns;
	}
	public char[][] getInclusionPatterns() {
			return this.accessibleFilesPatterns;
	}
	/**
	 * Returns readable description for problem reporting, 
	 * message is expected to contain room for restricted type name
	 * e.g. "{0} has restricted access"
	 */
	public String getMessageTemplate() {
			return this.messageTemplate;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer(200);
		buffer
			.append("AccessRestriction [accessible:\"") //$NON-NLS-1$
			.append(CharOperation.concatWith(this.accessibleFilesPatterns,'/'))
			.append("\"][non accessible:\"") //$NON-NLS-1$
			.append(CharOperation.concatWith(this.nonAccessibleFilesPatterns,'/'))
			.append("\"][template:\"") //$NON-NLS-1$
			.append(this.messageTemplate)
			.append("\"]"); //$NON-NLS-1$
		if (this.furtherRestriction != null) {
			buffer.append('\n').append(this.furtherRestriction);
		}
		return buffer.toString();
	}
}
