/*******************************************************************************
 * Copyright (c) 2014 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

/** An instance of of this type answers external annotations for the methods and fields of a given class. */
public interface IExternalAnnotationProvider {
	
	String ANNOTATION_FILE_SUFFIX = ".eea"; //$NON-NLS-1$ // FIXME(SH): define file extension

	ITypeAnnotationWalker forMethod(char[] selector, char[] signature, LookupEnvironment environment);

}
