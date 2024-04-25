/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.javac;

import java.net.URI;
import java.nio.charset.Charset;

import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileObject;

public class JavacFileObject extends EclipseFileObject {
	private ICompilationUnit originalUnit;

	public JavacFileObject(ICompilationUnit originalUnit, String className, URI uri, Kind kind, Charset charset) {
		super(className, uri, kind, charset);
		this.originalUnit = originalUnit;
	}

	public ICompilationUnit getOriginalUnit() {
		return this.originalUnit;
	}
}
