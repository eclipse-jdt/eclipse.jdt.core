/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 
 ******************************************************************************/

package org.eclipse.jdt.internal.codeassist;

public interface RelevanceConstants {
	final static int R_DEFAULT = 0;
	final static int R_CASE = 10;//5;
	final static int R_EXPECTED_TYPE = 20;
	final static int R_INTERFACE = 100;//5;
	final static int R_CLASS = 1000;//5;
	final static int R_EXCEPTION = 10000;//5;
}
