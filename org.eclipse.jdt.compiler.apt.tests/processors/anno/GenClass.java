/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package anno;

/**
 * Processing this annotation will produce a class whose name is the value of
 * <code>clazz</code>, with a method whose name is the value of
 * <code>method</code> and whose return type is <code>String</code>.
 */
public @interface GenClass {
	String clazz();
	String method();
}
