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


public class CharArrayAnnotationScanner extends AnnotationScanner {

	private final char[] _data;
	private int index = 0;
	
	public CharArrayAnnotationScanner(final char[] data) {
		_data = data;
	}
	
	@Override
	protected int getNext() throws IOException {
		if (index == _data.length - 1) 
			return -1;
		return _data[index++];
	}

}
