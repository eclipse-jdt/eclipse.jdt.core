/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.io.IOException;
import java.io.InputStream;


public class InputStreamAnnotationScanner extends AnnotationScanner {

	private final InputStream input;
	
	public InputStreamAnnotationScanner(final InputStream input) {
		this.input = input;
	}
	
	@Override
	protected int getNext() throws IOException {
		return input.read();
	}

}
