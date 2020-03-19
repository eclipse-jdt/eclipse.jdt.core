/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc.
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
 *
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.checkargs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * A processor that echoes its command-line arguments to standard out.
 */
@SupportedAnnotationTypes("org.eclipse.jdt.compiler.apt.tests.annotations.CheckArgs")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class CheckArgsProc extends AbstractProcessor {

	private final static String[] _expected =
	{
		"foo", "bar",
		"novalue", null,
		"bar2", null
	};

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver())
			return true;

		Map<String, String> options = new HashMap<String, String>(processingEnv.getOptions());
		options.remove("phase");
		if (_expected.length/2 != options.size()) {
			throw new IllegalStateException(
					"Bad processor arguments: expected " + _expected.length/2 +
					" arguments but found " + options.size());
		}
		for (int i = 0; i < _expected.length; ++i) {
			String key = _expected[i++];
			String value = _expected[i];
			String observedValue = options.get(key);
			if (value == null && observedValue != null) {
				throw new IllegalStateException(
						"Bad processor arguments: key " + key +
						" expected null value but observed value " + observedValue);
			}
			else if (value != null && !value.equals(observedValue)){
				throw new IllegalStateException(
						"Bad processor arguments: key " + key +
						" expected value " + value + " but observed value " + observedValue);
			}
		}
		return true;
	}

}
