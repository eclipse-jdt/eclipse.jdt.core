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
package org.eclipse.jdt.internal.core.pdom;

import org.eclipse.jdt.internal.core.pdom.db.Database;

/**
 * Points to a concrete type, NOT one of its subclasses. This should not be used for node
 * pointers, since they are stored as a pointer to the base class. If you want a pointer to
 * a node, use a NodeFieldDefinition instead.
 * @since 3.12
 */
public class Pointer<T> {
	private final PDOM pdom;
	private final long record;
	private ITypeFactory<T> targetFactory;
	
	public Pointer(PDOM pdom, long record, ITypeFactory<T> targetFactory) {
		this.pdom = pdom;
		this.record = record;
		this.targetFactory = targetFactory;
	}

	public T get() {
		long ptr = this.pdom.getDB().getRecPtr(this.record);

		if (ptr == 0) {
			return null;
		}

		return this.targetFactory.create(this.pdom, ptr);
	}

	public static <T> ITypeFactory<Pointer<T>> getFactory(final ITypeFactory<T> targetFactory) {
		if (PDOMNode.class.isAssignableFrom(targetFactory.getElementClass())) {
			throw new IllegalArgumentException("Use NodePointer rather than Pointer<T> for references to PDOMNode"); //$NON-NLS-1$
		}
		return new AbstractTypeFactory<Pointer<T>>() {
			@Override
			public Pointer<T> create(PDOM dom, long record) {
				return new Pointer<T>(dom, record, targetFactory);
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
