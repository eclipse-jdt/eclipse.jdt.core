/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
