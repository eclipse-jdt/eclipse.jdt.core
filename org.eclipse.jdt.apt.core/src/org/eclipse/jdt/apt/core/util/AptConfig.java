/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

/**
 * Holds configuration data for APT.
 */
public class AptConfig {
	
	private static boolean ENABLED = true;
	
	public static synchronized boolean isEnabled() {
		return ENABLED;
	}
	
	public static synchronized void setEnabled(boolean enabled) {
		ENABLED = enabled;
	}
}
