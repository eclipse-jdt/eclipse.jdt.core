package org.eclipse.jdt.internal.compiler.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;

import org.eclipse.jdt.internal.compiler.env.*;

public interface ITypeRequestor {
/**
 * Accept the resolved binary form for the requested type.
 */

void accept(IBinaryType binaryType, PackageBinding packageBinding);
/**
 * Accept the requested type's compilation unit.
 */

void accept(ICompilationUnit unit);
/**
 * Accept the unresolved source form for the requested type.
 */

void accept(ISourceType sourceType, PackageBinding packageBinding);
}
