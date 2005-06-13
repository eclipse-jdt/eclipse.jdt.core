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
package org.eclipse.jdt.apt.tests.annotations.helloworld;

import java.util.Collection;
import java.util.Collections;

/**
 * Processor factory that claims annotations with a wildcard 
 * ("org.eclipse.jdt.apt.tests.annotations.helloworld.*")
 */
public class HelloWorldWildcardAnnotationProcessorFactory extends
		HelloWorldAnnotationProcessorFactory {

	
	public Collection<String> supportedAnnotationTypes() {
		return Collections.singletonList("org.eclipse.jdt.apt.tests.annotations.helloworld.*");
	}
}
