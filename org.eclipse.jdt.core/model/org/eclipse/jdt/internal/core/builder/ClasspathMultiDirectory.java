/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

class ClasspathMultiDirectory extends ClasspathDirectory {

String sourcePath;
String encoding;

ClasspathMultiDirectory(String sourcePath, String binaryPath, String encoding) {
	super(binaryPath);

	this.sourcePath = sourcePath;
	if (!sourcePath.endsWith("/")) //$NON-NLS-1$
		this.sourcePath += "/"; //$NON-NLS-1$
	this.encoding = encoding;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathMultiDirectory)) return false;

	ClasspathMultiDirectory md = (ClasspathMultiDirectory) o;
	return binaryPath.equals(md.binaryPath) && sourcePath.equals(md.sourcePath);
} 

NameEnvironmentAnswer findSourceFile(
	String qualifiedSourceFileName,
	String qualifiedPackageName,
	char[] typeName,
	String[] additionalSourceFilenames) {

	// if an additional source file is waiting to be compiled, answer it
	// BUT not if this is a secondary type search,
	// if we answer the source file X.java which may no longer define Y
	// then the binary type looking for Y will fail & think the class path is wrong
	// let the recompile loop fix up dependents when Y has been deleted from X.java
	String fullSourceName = sourcePath + qualifiedSourceFileName;
	for (int i = 0, l = additionalSourceFilenames.length; i < l; i++)
		if (fullSourceName.equals(additionalSourceFilenames[i]))
			return new NameEnvironmentAnswer(
				new SourceFile(fullSourceName, typeName, CharOperation.splitOn('/', qualifiedPackageName.toCharArray()), encoding));
	return null;
}

public String toString() {
	return "Source classpath directory " + sourcePath + //$NON-NLS-1$
		" with binary directory " + binaryPath; //$NON-NLS-1$
}
}