/*******************************************************************************
 * Copyright (c) 2007, 2015 BEA Systems, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	public void reportFailure() {
		System.setProperty(this.getClass().getName(), "failed");
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_elementUtils = processingEnv.getElementUtils();
		_typeUtils = processingEnv.getTypeUtils();
	}
}
