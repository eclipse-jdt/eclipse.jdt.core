/*******************************************************************************
 * Copyright (c) 2021 IBM Joerg Kubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

/** Wrapper around AutoCloseable. The close() does not throw any Exception.
 *  Useful for try-with-resource when close() exceptions should be ignored. **/
public class QuietClose<T extends AutoCloseable> implements AutoCloseable {
	T resource;

	public QuietClose(T resource) {
		this.resource = resource;
	}

	/** using @SuppressWarnings("resource") when you use this method is safe **/
	public T get() {
		return this.resource;
	}

	@Override
	public void close() {
		try {
			if (this.resource != null) {
				this.resource.close();
			}
		} catch (Exception e) {
			// quiet
		}
	}
}