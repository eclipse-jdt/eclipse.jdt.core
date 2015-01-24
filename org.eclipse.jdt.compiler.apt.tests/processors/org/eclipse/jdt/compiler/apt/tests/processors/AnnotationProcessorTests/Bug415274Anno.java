/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     het@google.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

/**
 * This annotation type should only be visible to the processor.
 */
public @interface Bug415274Anno {
}
