/*******************************************************************************
 * Copyright (c) 2005, 2012 BEA Systems, Inc. and others
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

package org.eclipse.jdt.apt.tests.annotations.messager;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.util.SourcePosition;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

/**
 * Used to test the Messager interface.  Processing @MessagerAnnotation
 * causes messages to be posted.
 */
public class MessagerAnnotationProcessor extends BaseProcessor {

	private final AnnotationTypeDeclaration _annotationDecl;

	// Text of problems created
	public static final String PROBLEM_TEXT_INFO = "Annotated with MessagerAnnotation(INFO)"; //$NON-NLS-1$
	public static final String PROBLEM_TEXT_WARNING = "Annotated with MessagerAnnotation(WARNING)"; //$NON-NLS-1$
	public static final String PROBLEM_TEXT_ERROR = "Annotated with MessagerAnnotation(ERROR)"; //$NON-NLS-1$

	public MessagerAnnotationProcessor(
			Set<AnnotationTypeDeclaration> decls, AnnotationProcessorEnvironment env) {
		super(env);
        assert decls.size() == 1;
        _annotationDecl = decls.iterator().next();
	}

	/* (non-Javadoc)
	 * @see com.sun.mirror.apt.AnnotationProcessor#process()
	 */
	public void process() {
        Collection<Declaration> annotatedDecls = _env.getDeclarationsAnnotatedWith(_annotationDecl);
        Messager m = _env.getMessager();
        for (Declaration decl : annotatedDecls) {
        	MessagerAnnotation a = decl.getAnnotation(MessagerAnnotation.class);
        	SourcePosition sp = decl.getPosition();
        	MessagerAnnotation.Severity sev = a.severity();
        	switch (sev) {
        	case ERROR :
        		m.printError(sp, PROBLEM_TEXT_ERROR);
        		break;
        	case WARNING :
        		m.printWarning(sp, PROBLEM_TEXT_WARNING);
        		break;
        	case INFO :
        		m.printNotice(sp, PROBLEM_TEXT_INFO);
        		break;
        	case OK:
        		break;
        	}
        }
        reportSuccess(this.getClass());
	}

}
