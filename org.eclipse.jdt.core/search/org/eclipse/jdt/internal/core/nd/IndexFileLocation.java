/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
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
 * An implementation of IIndexFileLocation.
 */
public class IndexFileLocation implements IIndexFileLocation {
	private final URI uri;
	private final String fullPath;

	public IndexFileLocation(URI uri, String fullPath) {
		if (uri == null)
			throw new IllegalArgumentException();
		this.uri = uri;
		this.fullPath = fullPath;
	}

	@Override
	public String getFullPath() {
		return this.fullPath;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IIndexFileLocation) {
			return this.uri.equals(((IIndexFileLocation) obj).getURI());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.uri.hashCode();
	}

	@Override
	public String toString() {
		if (this.fullPath == null) {
			return this.uri.toString();
		}
		return this.fullPath.toString() + " (" + this.uri.toString() + ')'; //$NON-NLS-1$
	}
}
