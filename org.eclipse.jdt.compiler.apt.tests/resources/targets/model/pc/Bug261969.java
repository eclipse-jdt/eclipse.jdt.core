/*******************************************************************************
 * Copyright (c) 2010 Walter Harley and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.model.pc;

import org.eclipse.jdt.compiler.apt.tests.annotations.*;

/**
 * Bug 261969 concerns the ability to read a unary string value
 * in a string array typed annotation value.
 * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4961299
 * for discussion of why this should be permitted.
 */
public class Bug261969 {
	@TypedAnnos.AnnoArrayString("foo")
	public class Annotated {}
}