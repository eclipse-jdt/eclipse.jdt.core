/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.base;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Base class for writing processors used in test cases.
 */
public abstract class BaseProcessor extends AbstractProcessor
{
	protected Elements _elementUtils;
	protected Types _typeUtils;

	/**
	 * Report an error to the test case code
	 * @param value
	 */
	public void reportError(String value) {
		// Debugging - don't report error
		// value = "succeeded";
		System.setProperty(this.getClass().getName(), value);
	}
	
	/**
	 * Report success to the test case code
	 */
	public void reportSuccess() {
		System.setProperty(this.getClass().getName(), "succeeded");
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_elementUtils = processingEnv.getElementUtils();
		_typeUtils = processingEnv.getTypeUtils();
	}
}
