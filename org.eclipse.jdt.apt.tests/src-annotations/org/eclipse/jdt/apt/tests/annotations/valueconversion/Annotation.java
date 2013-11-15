/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.valueconversion;

public @interface Annotation {
	boolean z();
	char c();
	byte b();
	short s();
	int i(); 
	long l();
	float f();
	double d();
}
