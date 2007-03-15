/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.elements;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * A processor that explores the "model" target hierarchy and complains if it does
 * not find what it expects.  To enable this processor, add 
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc to the command line.
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ElementProc extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey("org.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc")) {
			return false;
		}
		for (Element e : roundEnv.getRootElements()) {
			System.out.println("Found element " + e.getSimpleName());
		}
		return false;
	}

}
