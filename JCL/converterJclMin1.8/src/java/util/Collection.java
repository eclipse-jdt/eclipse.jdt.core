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

public interface Collection<E> extends Iterable<E> {
	public Iterator<E> iterator();
	public int size();
	public E get(int index);
	public boolean addAll(Collection<E> c);
	public <T> T[] toArray(T[] o);
}
