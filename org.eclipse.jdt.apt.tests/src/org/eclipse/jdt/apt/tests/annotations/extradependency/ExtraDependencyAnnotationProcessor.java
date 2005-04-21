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

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

public class ExtraDependencyAnnotationProcessor implements AnnotationProcessor
{

	public ExtraDependencyAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		_env = env;
	}

	public void process()
	{
		_env.getTypeDeclaration( "p1.C" );
	}

	AnnotationProcessorEnvironment	_env;
}
