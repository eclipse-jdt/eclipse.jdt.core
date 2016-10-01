/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import java.net.URI;

/**
 * Files in the index are (conceptually) partitioned into workspace and non-workspace (external) files. Two index file
 * locations are considered equal if their URIs are equal.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexFileLocation {
	/**
	 * Returns the URI of the indexed file (non-{@code null}).
	 */
	public URI getURI();

	/**
	 * Returns the workspace relative path of the file in the index or {@code null} if the file is not in the workspace.
	 */
	public String getFullPath();
}
