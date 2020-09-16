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

public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
	private static final long serialVersionUID = -2169998406647523911L;
	public ArrayList(int i) {
	}
	public ArrayList() {
	}
	public <T> T[] toArray(T[] o) {
		return null;
	}
	public int size() {
		return 0;
	}
	public boolean add(E o) {
		return false;
	}
	public int indexOf(E o) {
		return 0;
	}
	public E remove(int index) {
		return null;
	}
	public E get(int index) {
		return null;
	}
	public boolean contains(E o) {
		return false;
	}
	public Iterator<E> iterator() {
		return null;
	}
	public boolean addAll(Collection<E> c) {
		return false;
	}
	public void set(int i, E o) {
	}
}
