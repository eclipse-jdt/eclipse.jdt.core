/*******************************************************************************
 * Copyright (c) 2025 Groq Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Martin Bazley - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

public interface Location {
	int sourceEnd();
	int sourceStart();
	default int nameSourceStart() { return sourceStart(); }
	default int nameSourceEnd() { return sourceEnd(); }
}
