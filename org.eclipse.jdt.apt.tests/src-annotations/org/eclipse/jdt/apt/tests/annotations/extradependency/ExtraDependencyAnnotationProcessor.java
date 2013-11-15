/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.extradependency;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

public class ExtraDependencyAnnotationProcessor extends BaseProcessor {

	public ExtraDependencyAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process() {
		_env.getTypeDeclaration( "p1.p2.p3.p4.C" ); //$NON-NLS-1$
	}
}
