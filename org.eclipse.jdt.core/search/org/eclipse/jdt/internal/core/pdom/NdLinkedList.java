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

import org.eclipse.jdt.internal.core.pdom.db.IndexException;

/**
 * @since 3.12
 */
public final class NdLinkedList<T> {
	private final NdRawLinkedList rawList;
	private final ITypeFactory<T> elementFactory;

	public static interface ILinkedListVisitor<T> {
		public void visit(T record, short metadataBits, int index) throws IndexException;
	}

	public NdLinkedList(Nd pdom, long record, ITypeFactory<T> elementFactory, int recordsInFirstBlock,
			int recordsInSubsequentBlocks) {
		this(pdom, record, elementFactory, recordsInFirstBlock, recordsInSubsequentBlocks, 0);
	}

	public NdLinkedList(Nd pdom, long record, ITypeFactory<T> elementFactory, int recordsInFirstBlock,
			int recordsInSubsequentBlocks, int metadataBitsPerElement) {
		this.rawList = new NdRawLinkedList(pdom, record, elementFactory.getRecordSize(), recordsInFirstBlock,
				recordsInSubsequentBlocks, metadataBitsPerElement);
		this.elementFactory = elementFactory;
	}

	/**
	 * Computes the size of this list. This is an O(n) operation.
	 * 
	 * @return the size of this list
	 * @throws IndexException 
	 */
	public int size() throws IndexException {
		return this.rawList.size();
	}
	
	public T addMember(short metadataBits) throws IndexException {
		long record = this.rawList.addMember(metadataBits);

		return this.elementFactory.create(this.rawList.getPDOM(), record);
	}

	public void accept(final ILinkedListVisitor<T> visitor) throws IndexException {
		final NdRawLinkedList localRawList = this.rawList;
		final ITypeFactory<T> localElementFactory = this.elementFactory;
		localRawList.accept(new NdRawLinkedList.ILinkedListVisitor() {
			@Override
			public void visit(long record, short metadataBits, int index) throws IndexException {
				visitor.visit(localElementFactory.create(localRawList.getPDOM(),
						record), metadataBits, index);
			}
		});
	}

	public static <T> ITypeFactory<NdLinkedList<T>> getFactoryFor(
			final ITypeFactory<T> elementFactory, final int recordsInFirstBlock, final int recordsInSubsequentBlocks) {
		return getFactoryFor(elementFactory, recordsInSubsequentBlocks, 0);
	}

	public static <T> ITypeFactory<NdLinkedList<T>> getFactoryFor(
			final ITypeFactory<T> elementFactory, final int recordsInFirstBlock, final int recordsInSubsequentBlocks,
			final int metadataBitsPerElement) {

		return new AbstractTypeFactory<NdLinkedList<T>>() {
			public NdLinkedList<T> create(Nd dom, long record) {
				return new NdLinkedList<T>(dom, record, elementFactory, recordsInFirstBlock, recordsInSubsequentBlocks, metadataBitsPerElement);
			}

			@Override
			public int getRecordSize() {
				return NdRawLinkedList.recordSize(elementFactory.getRecordSize(), recordsInFirstBlock,
						metadataBitsPerElement);
			}

			@Override
			public Class<?> getElementClass() {
				return NdLinkedList.class;
			}

			@Override
			public boolean hasDestructor() {
				return true;
			}

			@Override
			public void destructFields(Nd dom, long record) {
				create(dom, record).destruct();
			}

			@Override
			public void destruct(Nd dom, long record) {
				destructFields(dom, record);
			}
		};
	}

	/**
	 * 
	 */
	protected void destruct() {
		if (this.elementFactory.hasDestructor()) {
			final Nd pdom = this.rawList.getPDOM();
			this.rawList.accept(new NdRawLinkedList.ILinkedListVisitor() {
				@Override
				public void visit(long record, short metadataBits, int index) throws IndexException {
					NdLinkedList.this.elementFactory.destruct(pdom, record);
				}
			});
		}
		this.rawList.destruct();
	}
}
