/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.annotations;

/**
 * 
 * @since 3.4
 */
public @interface FilerTestTrigger {
	/** Name of test method to run */
	String test();
	
	/** Arbitrary argument */
	String arg0() default "";

	/** Arbitrary argument */
	String arg1() default "";
}
