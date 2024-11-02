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

public interface Collection<T> {
	public Iterator<T> iterator();
	public int size();
	public T get(int index);
	public boolean addAll(Collection<T> c);
	public T[] toArray(T[] o);
}
