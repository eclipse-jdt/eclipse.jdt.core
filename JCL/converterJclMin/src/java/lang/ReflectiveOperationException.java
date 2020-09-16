/*******************************************************************************
 * Copyright (c) 2020 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.lang;

public class ReflectiveOperationException extends Exception {
	public ReflectiveOperationException() {
		super();
	}
	public ReflectiveOperationException(String s, Throwable t) {
		this(s);
	}
	public ReflectiveOperationException(java.lang.String s) {
		super(s);
	}
}
