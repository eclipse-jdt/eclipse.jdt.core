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

public class System {

	public static PrintStream err;
	public static PrintStream out;

	public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);
	public static String getProperty(String s) {
		return null;
	}
}
