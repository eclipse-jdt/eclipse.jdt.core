/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.core.IJavaProject;

public final class AptUtil {

	// Private c-tor to prevent construction
	private AptUtil() {}

	/**
	 * Returns the matching annotation processor factory for a given
	 * annotation in a given project.
	 *
	 * @param fullyQualifiedAnnotation the annotation for which a factory
	 * is desired. This must be fully qualfied -- e.g. "org.eclipse.annotation.Foo"
	 */
	public static AnnotationProcessorFactory getFactoryForAnnotation(
			final String fullyQualifiedAnnotation,
			final IJavaProject jproj) {

		AnnotationProcessorFactoryLoader loader = AnnotationProcessorFactoryLoader.getLoader();
		List<AnnotationProcessorFactory> factories = loader.getJava5FactoriesForProject( jproj );

		for (AnnotationProcessorFactory factory : factories) {
			Collection<String> supportedAnnos = factory.supportedAnnotationTypes();
			for (String anno : supportedAnnos) {
				if (anno.equals(fullyQualifiedAnnotation)) {
					return factory;
				}
				else if ("*".equals(anno)) { //$NON-NLS-1$
						return factory;
				}
				else if (anno.endsWith("*")) { //$NON-NLS-1$
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
