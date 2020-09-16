/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.io;

public class InputStream {
	public void close() throws IOException {
	}
	public int available() throws IOException {
		return 0;
	}
    public int read(byte b[], int off, int len) throws IOException {
    	return 0;
    }	
}
