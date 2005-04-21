/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.env;

import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * Extend the APT annotation processor factory API to provide extra features.  
 * Implementation of this annotation processor is treated just like 
 * the regular annotation processor during build and reconcile.
 * @author tyeung
 *
 */
public interface EclipseAnnotationProcessorFactory extends
		AnnotationProcessorFactory 
{
	/**
	 * Returns an annotation processor for a set of annotation types.
	 * Implementation of this API is guaranteed to be invoked with an 
	 * extended annotation processor environment for both 
	 * {@link #getProcessorFor} version of the method.
	 *  
	 * @param atds the set of annotation types
	 * @param env the environment for processing.
	 * @return an annotation processor or null if the processor cannot be created.
	 */
	AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, 
										EclipseAnnotationProcessorEnvironment env);	
}
