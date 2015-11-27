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

import org.eclipse.jdt.internal.core.pdom.field.IDestructableField;
import org.eclipse.jdt.internal.core.pdom.field.IRefCountedField;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * TODO: Add javadoc for all methods
 * @since 3.12
 */
public final class AutoTypeFactory<T> extends AbstractTypeFactory<T> {
	private final Constructor<T> twoArgConstructor;
	private final int size;
	private final Class<T> theClass;
	private final IDestructableField allFieldsDestructor;
	private final boolean hasUserDestructor;
	private IRefCountedField refcountImplemention;
	private boolean refcounted;

	/**
	 * @param theClass
	 * @param constructor
	 * @param recordSize
	 * @param allFieldsDestructor callback that can be invoked after the user destructor to invoke destructors
	 *        on all of the object's fields
	 * @param hasUserDestructor true iff theClass implements IDestructable
	 * @param isRefCounted 
	 * @param refcountImplementation 
	 */
	private AutoTypeFactory(Class<T> theClass, Constructor<T> constructor, int recordSize,
			IDestructableField allFieldsDestructor, boolean hasUserDestructor, IRefCountedField refcountImplementation,
			boolean isRefCounted) {
		this.twoArgConstructor = constructor;
		this.size = recordSize;
		this.theClass = theClass;
		this.allFieldsDestructor = allFieldsDestructor;
		this.hasUserDestructor = hasUserDestructor;
		this.refcountImplemention = refcountImplementation;
		this.refcounted = isRefCounted;
	}

	public static <T> AutoTypeFactory<T> create(Class<T> objectClass, int size, IDestructableField allFieldsDestructor,
			IRefCountedField refcountImplementation, boolean isRefCounted) {
		String fullyQualifiedClassName = objectClass.getName();

		boolean hasUserDestructor = IDestructable.class.isAssignableFrom(objectClass);

		Constructor<T> constructor;
		try {
			constructor = objectClass.getConstructor(new Class<?>[] { PDOM.class, long.class });
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("The node class " + fullyQualifiedClassName //$NON-NLS-1$
					+ " does not have an appropriate constructor for it to be used with PDOM"); //$NON-NLS-1$
		}

		return new AutoTypeFactory<T>(objectClass, constructor, size, allFieldsDestructor, hasUserDestructor,
				refcountImplementation, isRefCounted);
	}

	@Override
	public T create(PDOM dom, long record) {
		try {
			return this.twoArgConstructor.newInstance(dom, record);
		} catch (InvocationTargetException e) {
			Throwable target = e.getCause();

			if (target instanceof RuntimeException) {
				throw (RuntimeException) target;
			}

			throw new RuntimeException("Error in AutoTypeFactory", e); //$NON-NLS-1$
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Error in AutoTypeFactory", e); //$NON-NLS-1$
		}
	}

	@Override
	public int getRecordSize() {
		return this.size;
	}

	@Override
	public Class<?> getElementClass() {
		return this.theClass;
	}

	@Override
	public void destruct(PDOM pdom, long record) {
		if (this.hasUserDestructor) {
			IDestructable destructable = (IDestructable)create(pdom, record);
			destructable.destruct();
		}
		destructFields(pdom, record);
	}

	@Override
	public boolean hasDestructor() {
		return (this.hasUserDestructor || this.allFieldsDestructor != null);
	}

	@Override
	public void destructFields(PDOM dom, long record) {
		if (this.allFieldsDestructor != null) {
			this.allFieldsDestructor.destruct(dom, record);
		}
	}
	
	@Override
	public boolean isRefCounted() {
		return this.refcounted;
	}

	@Override
	public boolean hasReferences(PDOM dom, long record) {
		if (this.refcountImplemention != null) {
			return this.refcountImplemention.hasReferences(dom, record);
		}
		return false;
	}
}
