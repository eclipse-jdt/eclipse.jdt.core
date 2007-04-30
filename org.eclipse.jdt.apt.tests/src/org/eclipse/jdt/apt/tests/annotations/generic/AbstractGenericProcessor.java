/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.generic;

import java.util.Collection;

import junit.framework.AssertionFailedError;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;

public abstract class AbstractGenericProcessor implements AnnotationProcessor {
	protected AnnotationProcessorEnvironment env;
	protected AnnotationTypeDeclaration genericAnnotation;
	protected Collection<Declaration> decls;
	
	public void setEnv(AnnotationProcessorEnvironment env) {
		this.env = env;
		genericAnnotation = (AnnotationTypeDeclaration) env.getTypeDeclaration(GenericAnnotation.class.getName());
		decls = env.getDeclarationsAnnotatedWith(genericAnnotation);
	}
	
	public abstract void _process();
	
	/**
	 * This method is abstract, so that subclasses need to implement
	 * _process. We'll handle catching any errant throwables
	 * and fail any junit tests.
	 */
	public final void process() {
		try {
			_process();
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new AssertionFailedError("Processor threw an exception during processing");
		}
	}
	
}
