/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

@Override
public void flush() {
	// do nothing
}

@Override
public void write(char[] buf, int off, int len) {
	// do nothing
}

@Override
public void write(int c) {
	// do nothing
}

@Override
public void write(String s, int off, int len) {
	// do nothing
}

}
