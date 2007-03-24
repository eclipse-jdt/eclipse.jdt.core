/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.filegen;

public @interface TextGenAnnotation {

	String value(); // name of file to generate
}
