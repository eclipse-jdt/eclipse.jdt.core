/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.pause;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.Set;
import org.eclipse.jdt.apt.tests.annotations.BaseFactory;

public class PauseAnnotationProcessorFactory extends BaseFactory {

	private final static String annotationName = "org.eclipse.jdt.apt.tests.annotations.pause.Pause";
	public PauseAnnotationProcessorFactory() {
		super(annotationName);
	}

	/* (non-Javadoc)
	 * @see com.sun.mirror.apt.AnnotationProcessorFactory#getProcessorFor(java.util.Set, com.sun.mirror.apt.AnnotationProcessorEnvironment)
	 */
	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> decls,
			AnnotationProcessorEnvironment env) {
		return new PauseAnnotationProcessor(decls, env);
	}

}
