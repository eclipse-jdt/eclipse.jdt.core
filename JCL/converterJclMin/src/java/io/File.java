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

public class File {
	public static final char separatorChar = '\\';
	public static final String separator = "\\"; //$NON-NLS-1$
	
	public File(String s) {
	}
	
	public boolean exists() {
		return false;
	}
	
	public boolean isDirectory() {
		return false;
	}
	
	public String getAbsolutePath() {
		return null;
	}
	
	public boolean mkdirs() {
		return false;
	}
	
	public boolean mkdir() {
		return false;
	}
	
	public String getName() {
		return null;
	}
	public long length() {
		return 0;
	}
}
