/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.env;

/**
 * Enum for APT related operation phase.
 * @see EclipseAnnotationProcessorEnvironment#getPhase()
 */
public enum Phase { 
	
	/** During Reconcile phase */
	RECONCILE, 
	/** During Build phase */    
	BUILD, 
	/** 
	 * Neither reconcile nor build. Completion would be an example.  
	 */ 
	OTHER 
}
