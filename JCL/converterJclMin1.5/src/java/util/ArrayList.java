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

import java.io.Serializable;

public class ArrayList<T> extends AbstractList<T> implements List<T>, RandomAccess, Cloneable, Serializable {
	private static final long serialVersionUID = -2169998406647523911L;
	public ArrayList(int i) {
	}
	public ArrayList() {
	}
	public T[] toArray(T[] o) {
		return null;
	}
	public int size() {
		return 0;
	}
	public boolean add(T o) {
		return false;
	}
	public int indexOf(T o) {
		return 0;
	}
	public T remove(int index) {
		return null;
	}
	public T get(int index) {
		return null;
	}
	public boolean contains(T o) {
		return false;
	}
	public Iterator<T> iterator() {
		return null;
	}
	public boolean addAll(Collection<T> c) {
		return false;
	}
	public void set(int i, T o) {
	}
}
