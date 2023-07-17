/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.generic;

import java.util.Collection;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;

import junit.framework.AssertionFailedError;

public abstract class AbstractGenericProcessor implements AnnotationProcessor {
	protected AnnotationProcessorEnvironment env;
	protected AnnotationTypeDeclaration genericAnnotation;
	protected Collection<Declaration> decls;

	public void setEnv(AnnotationProcessorEnvironment env) {
		this.env = env;
		genericAnnotation = (AnnotationTypeDeclaration) env.getTypeDeclaration(GenericAnnotation.class.getName());
		decls = env.getDeclarationsAnnotatedWith(genericAnnotation);
	}

	/**
	 * This method is abstract, so that subclasses need to implement
	 * _process. We'll handle catching any errant throwables
	 * and fail any junit tests.
	 */
	public abstract void _process();

	@Override
	public final void process() {
		try {
			_process();
		}
		catch (Throwable t) {
			if (t instanceof AssertionFailedError) {
				throw t;
			}
			t.printStackTrace();
			AssertionFailedError assertionFailedError = new AssertionFailedError(
					"Processor threw an exception during processing: " + t);
			assertionFailedError.initCause(t);
			throw assertionFailedError;
		}
	}

}
