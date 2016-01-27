/*******************************************************************************
  * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Points to a concrete type, NOT one of its subclasses. This should not be used for node
 * pointers, since they are stored as a pointer to the base class. If you want a pointer to
 * a node, use a NodeFieldDefinition instead.
 * @since 3.12
 */
public class Pointer<T> {
	private final Nd pdom;
	private final long address;
	private ITypeFactory<T> targetFactory;

	public Pointer(Nd pdom, long address, ITypeFactory<T> targetFactory) {
		this.pdom = pdom;
		this.address = address;
		this.targetFactory = targetFactory;
	}

	public T get() {
		long ptr = this.pdom.getDB().getRecPtr(this.address);

		if (ptr == 0) {
			return null;
		}

		return this.targetFactory.create(this.pdom, ptr);
	}

	public static <T> ITypeFactory<Pointer<T>> getFactory(final ITypeFactory<T> targetFactory) {
		if (NdNode.class.isAssignableFrom(targetFactory.getElementClass())) {
			throw new IllegalArgumentException("Use NodePointer rather than Pointer<T> for references to PDOMNode"); //$NON-NLS-1$
		}
		return new AbstractTypeFactory<Pointer<T>>() {
			@Override
			public Pointer<T> create(Nd dom, long address) {
				return new Pointer<T>(dom, address, targetFactory);
			}

			@Override
			public int getRecordSize() {
				return Database.PTR_SIZE;
			}

			@Override
			public Class<?> getElementClass() {
				return Pointer.class;
			}
		};
	}
}
