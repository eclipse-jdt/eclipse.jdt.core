/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 *
 */
public record LocatorResponse(int level, boolean replacementNodeFound,
		ASTNode replacement, boolean added, boolean canVisitChildren) {
}