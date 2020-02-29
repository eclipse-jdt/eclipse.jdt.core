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
package java.lang;

import java.io.PrintStream;
import java.io.Writer;

public class Throwable {
	public Throwable(String s) {
	}
	public Throwable() {
	}
	
	public void printStackTrace() {
	}
	
	public void printStackTrace(Writer w) {
	}

	public void printStackTrace(PrintStream w) {
	}
	public String getMessage() {
		return null;
	}	
}
