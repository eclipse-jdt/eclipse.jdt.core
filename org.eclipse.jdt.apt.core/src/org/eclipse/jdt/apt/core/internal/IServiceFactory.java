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

package org.eclipse.jdt.apt.core.internal;

import org.eclipse.core.runtime.CoreException;

/**
 * An abstract factory for services such as annotation processors.
 * This abstraction exists because the way a service is instantiated
 * may depend on where it was loaded from as well as what kind of
 * service it is (e.g., Java 6 annotation processors use their class
 * object as a factory; Java 5 processors have an explicit factory
 * object.)
 * @since 3.3
 * @see AnnotationProcessorFactoryLoader
 */
public interface IServiceFactory {
	
	Object newInstance() throws CoreException;
}
