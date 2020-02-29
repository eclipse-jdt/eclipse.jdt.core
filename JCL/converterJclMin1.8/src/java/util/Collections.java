/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.util;

public class Collections {
	public static <T extends Comparable<? super T>> void sort(List<T> list) {
	}

	public static final <K,V> Map<K,V> emptyMap() {
		return null;
	}
	public static final <T> List<T> emptyList() {
		return null;
	}
}
