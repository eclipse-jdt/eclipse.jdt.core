/*******************************************************************************

 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;

public abstract class BaseProcessor implements AnnotationProcessor {

	public static final String SUCCESS = "succeeded";

	public static String getPropertyName(Class<? extends AnnotationProcessor> processor) {
		return processor.getName() + ".result";
	}

	protected void reportSuccess(Class<? extends AnnotationProcessor> processor) {
		System.setProperty(getPropertyName(processor), SUCCESS);
	}

	protected void reportError(Class<? extends AnnotationProcessor> processor, String msg) {
		System.setProperty(getPropertyName(processor), msg);
	}

	protected final AnnotationProcessorEnvironment _env;

	public BaseProcessor(final AnnotationProcessorEnvironment env) {
		_env = env;
	}

	protected void assertEqual(final int expected, final int actual, final String message){
		if(expected != actual){
			final Messager msgr = _env.getMessager();
			msgr.printError(message + " expected: " + expected + " actual: " + actual );
		}
	}

	protected void assertEqual(final Object expected, final Object actual, final String message) {
		if( expected == null ){
			final Messager msgr = _env.getMessager();
			msgr.printError(message + " actual: " + actual );
		}
		else if( actual == null ){
			final Messager msgr = _env.getMessager();
			msgr.printError(message + "expected " + expected );
		}
		else if( !expected.equals(actual) ){
			final Messager msgr = _env.getMessager();
			msgr.printError(message + " expected: " + expected + " actual: " + actual );
		}
	}

	protected void assertEqual(final String expected, final String actual, final String message){
		if( expected == null ){
			final Messager msgr = _env.getMessager();
			msgr.printError(message + " actual: " + actual );
		}
		else if( actual == null ){
			final Messager msgr = _env.getMessager();
			msgr.printError(message + "expected " + expected );
		}
		else if( !expected.equals(actual) ){
			final Messager msgr = _env.getMessager();
			msgr.printError(message + " expected: " + expected + " actual: " + actual );
		}
	}

	protected void assertNonNull(final Object obj, final String message){
		if( obj == null ){
			final Messager msgr = _env.getMessager();
			msgr.printError(message);
		}
	}
}
