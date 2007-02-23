/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.dispatch;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * An implementation of Messager that reports messages via the Compiler
 */
public class BatchMessagerImpl implements Messager {
	
	//private final ProblemReporter _problemReporter;

	public BatchMessagerImpl(ProblemReporter reporter) {
		//TODO: is a problem reporter what we need??  
		//_problemReporter = reporter;
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
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a, AnnotationValue v) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		final String space = " "; //$NON-NLS-1$
		sb.append("APT says:"); //$NON-NLS-1$
		if (kind != null) {
			sb.append(space);
			sb.append(kind);
		}
		if (msg != null) {
			sb.append(space);
			sb.append(msg);
		}
		if (e != null) {
			sb.append(" on element "); //$NON-NLS-1$
			sb.append(e);
		}
		if (a != null) {
			sb.append(" at annotation "); //$NON-NLS-1$
			sb.append(a);
		}
		if (v != null) {
			sb.append(" on value "); //$NON-NLS-1$
			sb.append(v);
		}
		System.err.println(sb.toString());

	}

}
