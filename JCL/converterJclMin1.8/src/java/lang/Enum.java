/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.lang;

public abstract class Enum<T extends Enum<T>> implements Comparable<T>,
		java.io.Serializable {
	
	private static final long serialVersionUID = 2L;

	protected Enum(String name, int ordinal) {
	}

	public final String name() {
		return null;
	}

	public final int ordinal() {
		return 0;
	}

	public final int compareTo(T o) {
		return 0;
	}

	public final Class<T> getDeclaringClass() {
        return null;
	}
	public static <T extends Enum<T>> T valueOf(Class<T> enumClass,
			String name) {
		return null;   
	}
}