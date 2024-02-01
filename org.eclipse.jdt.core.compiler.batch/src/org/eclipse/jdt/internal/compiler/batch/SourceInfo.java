/*******************************************************************************
 * Copyright (c) 2024 Christoph Läubrich.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

/**
 * Collects all information of a source file
 */
public record SourceInfo(int index, String filename, String encoding, String moduleName, String destinationPath) {

}
