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
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Definition of a set of access rules used to flag forbidden references to non API code.
 */
public class AccessRuleSet {

	private AccessRule[] accessRules;
	public String messageTemplate;
	
	
	public AccessRuleSet(AccessRule[] accessRules) {
		this.accessRules = accessRules;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (this == object) 
			return true;
		if (!(object instanceof AccessRuleSet))
			return false;
		AccessRuleSet otherRuleSet = (AccessRuleSet) object;
		if (!this.messageTemplate.equals(otherRuleSet.messageTemplate)) 
			return false;
		int rulesLength = this.accessRules.length;
		if (rulesLength != otherRuleSet.accessRules.length) return false;
		for (int i = 0; i < rulesLength; i++)
			if (!this.accessRules[i].equals(otherRuleSet.accessRules[i]))
				return false;
		return true;
	}
	
	public AccessRule[] getAccessRules() {
		return this.accessRules;
	}
	
	/**
	 * Select the first access rule which is violated when accessing a given type, or null if no 'non accessible' access rule applies.
	 * Target type file path is formed as: "org/eclipse/jdt/core/JavaCore.java".
	 */
	public AccessRestriction getViolatedRestriction(char[] targetTypeFilePath) {
		
		for (int i = 0, length = this.accessRules.length; i < length; i++) {
			AccessRule accessRule = this.accessRules[i];
			if (CharOperation.pathMatch(accessRule.pattern, targetTypeFilePath, true/*case sensitive*/, '/')) {
				switch (accessRule.problemId) {
					case IProblem.ForbiddenReference:
					case IProblem.DiscouragedReference:
						return new AccessRestriction(accessRule, this.messageTemplate);
					default:
						return null;
				}
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer(200);
		buffer.append("AccessRuleSet {\n"); //$NON-NLS-1$
		for (int i = 0, length = this.accessRules.length; i < length; i++) {
			buffer.append('\t');
			AccessRule accessRule = this.accessRules[i];
			buffer.append(accessRule);
			buffer.append('\n');
		}
		buffer
			.append("} [template:\"") //$NON-NLS-1$
			.append(this.messageTemplate)
			.append("\"]"); //$NON-NLS-1$
		return buffer.toString();
	}
}
