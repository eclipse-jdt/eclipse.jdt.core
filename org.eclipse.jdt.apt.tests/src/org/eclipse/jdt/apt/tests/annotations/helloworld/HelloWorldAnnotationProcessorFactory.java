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

package org.eclipse.jdt.apt.tests.annotations.helloworld;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.apt.tests.annotations.BaseFactory;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class HelloWorldAnnotationProcessorFactory extends BaseFactory {
	
	public HelloWorldAnnotationProcessorFactory() {
		super(HelloWorldAnnotation.class.getName());
	}

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> atds,
			AnnotationProcessorEnvironment env) 
	{
		return new HelloWorldAnnotationProcessor( env );
	}
	
	public Collection<String> supportedOptions() {
		return Collections.singletonList(AptPreferenceConstants.RTTG_ENABLED_OPTION);
	}

}