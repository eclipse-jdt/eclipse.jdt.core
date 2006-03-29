/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.PrintWriter;
import java.io.StringWriter;

public class NullPrintWriter extends PrintWriter {

public NullPrintWriter() {
	super(new StringWriter());
}

public void flush() {
	// do nothing
}

public void write(char[] buf, int off, int len) {
	// do nothing
}

public void write(int c) {
	// do nothing
}

public void write(String s, int off, int len) {
	// do nothing
}

}
