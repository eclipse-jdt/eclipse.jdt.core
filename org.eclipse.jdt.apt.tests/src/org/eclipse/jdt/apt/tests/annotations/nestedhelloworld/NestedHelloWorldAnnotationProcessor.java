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


package org.eclipse.jdt.apt.tests.annotations.nestedhelloworld;

import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotationProcessor;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;


public class NestedHelloWorldAnnotationProcessor extends
		HelloWorldAnnotationProcessor {

	public NestedHelloWorldAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		super( env );
	}

	public String getCode() { return CODE; }
	
	private final String CODE = 
		"package " + getPackageName() + ";" + "\n" + 
		"@" + HelloWorldAnnotation.class.getName() + "\n" + 
		"public class NestedHelloWorldAnnotationGeneratedClass " + "\n" +
		"{  }";
		
		public String getPackageName() { return "nested.hello.world.generatedclass.pkg"; }
		public String getTypeName() { return "NestedHelloWorldAnnotationGeneratedClass"; }
}
