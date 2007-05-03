/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BatchMessagerImpl;

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
	public void printMessage(Kind kind, CharSequence msg) {
		printMessage(kind, msg, null, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element)
	 */
	public void printMessage(Kind kind, CharSequence msg, Element e) {
		printMessage(kind, msg, e, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror)
	 */
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a) {
		printMessage(kind, msg, e, a, null);

	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror, javax.lang.model.element.AnnotationValue)
	 */
	public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a,
			AnnotationValue v) {
		CategorizedProblem problem = BatchMessagerImpl.createProblem(kind, msg, e);
		Apt6Plugin.log(new Status(IStatus.INFO, Apt6Plugin.PLUGIN_ID, Apt6Plugin.STATUS_EXCEPTION, problem.toString(), null));
	}

}
