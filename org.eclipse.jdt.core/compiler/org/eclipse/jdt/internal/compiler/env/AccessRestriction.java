/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

public class AccessRestriction {

	private AccessRule accessRule;
	private String messageTemplate;
	public AccessRestriction(AccessRule accessRule, String messageTemplate) {
		this.accessRule = accessRule;
		this.messageTemplate = messageTemplate;
	}
	
	/**
	 * Returns readable description for problem reporting, 
	 * message is expected to contain room for restricted type name
	 * e.g. "{0} has restricted access"
	 */
	public String getMessageTemplate() {
		return this.messageTemplate;
	}
	
	public int getProblemId() {
		return this.accessRule.problemId;
	}

}
