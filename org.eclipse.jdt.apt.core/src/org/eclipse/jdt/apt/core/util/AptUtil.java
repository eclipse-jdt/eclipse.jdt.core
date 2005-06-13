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
package org.eclipse.jdt.apt.core.util;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.apt.core.internal.AptCompilationParticipant;

import com.sun.mirror.apt.AnnotationProcessorFactory;

public class AptUtil {
	
	// Private c-tor to prevent construction
	private AptUtil() {}
	
	/**
	 * Returns the matching annotation processor factory for a given
	 * annotation in a given project.
	 * 
	 * @param fullyQualifiedAnnotation the annotation for which a factory
	 * is desired. This must be fully qualfied -- e.g. "org.eclipse.annotation.Foo"
	 * @param project the project in which the annotation was found
	 */
	public static AnnotationProcessorFactory getFactoryForAnnotation(
			final String fullyQualifiedAnnotation,
			final IProject project) {
		
		// TODO: go to config for project to pull out factories
		List<AnnotationProcessorFactory> allFactories = 
			AptCompilationParticipant.getInstance().getAllFactories();
		
		for (AnnotationProcessorFactory factory : allFactories) {
			Collection<String> supportedAnnos = factory.supportedAnnotationTypes();
			for (String anno : supportedAnnos) {
				if (anno.equals(fullyQualifiedAnnotation)) {
					return factory;
				}
				else if ("*".equals(anno)) {
						return factory;
				}
				else if (anno.endsWith("*")) {
					final String prefix = anno.substring(0,
							anno.length() - 2);
					if (fullyQualifiedAnnotation.startsWith(prefix)) {
						return factory;
					}
				}
			}
		}
		
		return null;
	}
	
	

}
