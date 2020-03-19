/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core;

import org.eclipse.jdt.core.compiler.CompilationParticipant;

/**
 * Compilation participant for Java 6 annotation processing.  Java 6 annotation
 * processors are dispatched via the org.eclipse.jdt.core.annotationProcessorManager
 * extension point, but this compilation participant is still required in order
 * to register a managed problem marker.
 * @since 3.3
 */
public class Apt6CompilationParticipant extends CompilationParticipant {

	public Apt6CompilationParticipant() {
	}

}
