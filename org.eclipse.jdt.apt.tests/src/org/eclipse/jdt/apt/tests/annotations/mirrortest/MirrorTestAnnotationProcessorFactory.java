/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/


package org.eclipse.jdt.apt.tests.annotations.mirrortest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class MirrorTestAnnotationProcessorFactory implements AnnotationProcessorFactory {

	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

	public Collection<String> supportedAnnotationTypes() {
		return ANNOTATIONS;
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> typeDecls, AnnotationProcessorEnvironment env) {
		return new MirrorTestAnnotationProcessor(env);
	}
	
	private static ArrayList<String> ANNOTATIONS = new ArrayList<String>();
	
	static {
		ANNOTATIONS.add( MirrorTestAnnotation.class.getName() );
	}

}
