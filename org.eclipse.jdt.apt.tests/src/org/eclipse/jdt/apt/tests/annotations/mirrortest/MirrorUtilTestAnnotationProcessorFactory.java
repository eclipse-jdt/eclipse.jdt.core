/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.mirrortest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorFactory;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class MirrorUtilTestAnnotationProcessorFactory implements EclipseAnnotationProcessorFactory 
{

	public Collection<String> supportedOptions()
	{
		return Collections.emptyList();
	}

	public Collection<String> supportedAnnotationTypes()
	{
		return annotations;
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env)
	{
		return new MirrorUtilTestAnnotationProcessor(env);
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, EclipseAnnotationProcessorEnvironment env) 
	{
		return new MirrorUtilTestAnnotationProcessor(env);
	}

	private static ArrayList<String> annotations = new ArrayList<String>();
	{
		annotations.add( MirrorUtilTestAnnotation.class.getName() );
	}
}
