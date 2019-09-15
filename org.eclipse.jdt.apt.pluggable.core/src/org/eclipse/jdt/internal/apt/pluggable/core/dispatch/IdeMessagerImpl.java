/*******************************************************************************
 * Copyright (c) 2007, 2010 BEA Systems, Inc. and others
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

package org.eclipse.jdt.internal.apt.pluggable.core.dispatch;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.apt.dispatch.AptProblem;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseMessagerImpl;

/**
 *
 * @since 3.3
 */
public class IdeMessagerImpl implements Messager {

	private final IdeAnnotationProcessorManager _manager;
	private final IdeProcessingEnvImpl _env;

	public IdeMessagerImpl(IdeAnnotationProcessorManager manager,
			IdeProcessingEnvImpl env) {
		_manager = manager;
		_env = env;
		// This check is just here so the compiler doesn't complain about unread fields:
		if (null == _manager || null == _env) {
			throw new NullPointerException();
		}
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg) {
		printMessage(kind, msg, null, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e) {
		printMessage(kind, msg, e, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a) {
		printMessage(kind, msg, e, a, null);

	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror, javax.lang.model.element.AnnotationValue)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a,
			AnnotationValue v) {
		AptProblem problem = BaseMessagerImpl.createProblem(kind, msg, e, a, v);
		if (kind == Kind.NOTE) {
			Apt6Plugin.log(new Status(IStatus.INFO, Apt6Plugin.PLUGIN_ID, Apt6Plugin.STATUS_EXCEPTION, problem.getMessage(), null));
		}
		else if (null != problem._referenceContext) {
			CompilationResult result = problem._referenceContext.compilationResult();
			result.record(problem, problem._referenceContext);
		}
		else {
			// Unknown reference context; e.g., reported against an element not being compiled.
			// TODO: report against project??  log??
			Apt6Plugin.log(new Status(IStatus.INFO, Apt6Plugin.PLUGIN_ID, Apt6Plugin.STATUS_EXCEPTION, problem.getMessage(), null));
		}
	}

}
