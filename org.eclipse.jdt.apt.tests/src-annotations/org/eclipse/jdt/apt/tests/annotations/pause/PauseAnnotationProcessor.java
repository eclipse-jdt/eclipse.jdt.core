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

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

/**
 * Used to test performance in the IDE.  Processing @Pause
 * causes the processor to pause for a defined interval.
 */
public class PauseAnnotationProcessor extends BaseProcessor {

	private final AnnotationTypeDeclaration _annotationDecl;

	public PauseAnnotationProcessor(
			Set<AnnotationTypeDeclaration> decls, AnnotationProcessorEnvironment env) {
		super(env);
        assert decls.size() == 1;
        _annotationDecl = decls.iterator().next();
	}

	/* (non-Javadoc)
	 * @see com.sun.mirror.apt.AnnotationProcessor#process()
	 */
	public void process() {
		String phase = _env.getOptions().get("phase");
        Collection<Declaration> annotatedDecls = _env.getDeclarationsAnnotatedWith(_annotationDecl);
        for (Declaration decl : annotatedDecls) {
        	Pause a = decl.getAnnotation(Pause.class);
        	int pauseMs = a.value();
        	System.out.println(phase + " pausing for " + pauseMs + " to process " + decl.getSimpleName() + "...");
        	// busy sleep
        	long timeoutNanos = System.nanoTime() + pauseMs * 1_000_000L;
        	while (System.nanoTime() < timeoutNanos) {
        		Thread.onSpinWait();
        	}
        	System.out.println(phase + " finished pausing");
        }
	}

}
