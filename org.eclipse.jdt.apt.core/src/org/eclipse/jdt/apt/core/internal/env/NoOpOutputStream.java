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
package org.eclipse.jdt.apt.core.internal.env;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Dummy output stream for filer operations 
 */
public class NoOpOutputStream extends OutputStream {
	
	public NoOpOutputStream() {
		super();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
	}

	@Override
	public void write(byte[] b) throws IOException {
	}

	@Override
	public void write(int b) throws IOException {
	}
}
