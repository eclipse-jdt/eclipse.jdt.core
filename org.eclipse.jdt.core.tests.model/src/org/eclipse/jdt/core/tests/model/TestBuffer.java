/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;

/*
 * A simple implementation of IBuffer.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TestBuffer implements IBuffer {
		IOpenable owner;
		ArrayList changeListeners;
		char[] contents = null;
		boolean hasUnsavedChanges = false;
		public TestBuffer(IOpenable owner) {
			this.owner = owner;
		}
		/*
		 * @see IBuffer#addBufferChangedListener(IBufferChangedListener)
		 */
		public void addBufferChangedListener(IBufferChangedListener listener) {
			if (this.changeListeners == null) {
				this.changeListeners = new ArrayList(5);
			}
			if (!this.changeListeners.contains(listener)) {
				this.changeListeners.add(listener);
			}
		}

		/*
		 * @see IBuffer#append(char[])
		 */
		public void append(char[] text) {
			this.hasUnsavedChanges = true;
			notifyListeners();
		}

		/*
		 * @see IBuffer#append(String)
		 */
		public void append(String text) {
			this.hasUnsavedChanges = true;
			notifyListeners();
		}

		/*
		 * @see IBuffer#close()
		 */
		public void close() {
			this.contents = null; // mark as closed
			if (this.changeListeners != null) {
				notifyListeners();
				this.changeListeners = null;
			}
		}
		private void notifyListeners() {
			if (this.changeListeners == null) return;
			BufferChangedEvent event = null;
			event = new BufferChangedEvent(this, 0, 0, null);
			for (int i = 0, size = this.changeListeners.size(); i < size; ++i) {
				IBufferChangedListener listener = (IBufferChangedListener) this.changeListeners.get(i);
				listener.bufferChanged(event);
			}
		}

		/*
		 * @see IBuffer#getChar(int)
		 */
		public char getChar(int position) {
			return 0;
		}

		/*
		 * @see IBuffer#getCharacters()
		 */
		public char[] getCharacters() {
			return this.contents;
		}

		/*
		 * @see IBuffer#getContents()
		 */
		public String getContents() {
			return new String(this.contents);
		}

		/*
		 * @see IBuffer#getLength()
		 */
		public int getLength() {
			return this.contents.length;
		}

		/*
		 * @see IBuffer#getOwner()
		 */
		public IOpenable getOwner() {
			return this.owner;
		}

		/*
		 * @see IBuffer#getText(int, int)
		 */
		public String getText(int offset, int length) {
			return null;
		}

		/*
		 * @see IBuffer#getUnderlyingResource()
		 */
		public IResource getUnderlyingResource() {
			return null;
		}

		/*
		 * @see IBuffer#hasUnsavedChanges()
		 */
		public boolean hasUnsavedChanges() {
			return this.hasUnsavedChanges;
		}

		/*
		 * @see IBuffer#isClosed()
		 */
		public boolean isClosed() {
			return this.contents == null;
		}

		/*
		 * @see IBuffer#isReadOnly()
		 */
		public boolean isReadOnly() {
			return false;
		}

		/*
		 * @see IBuffer#removeBufferChangedListener(IBufferChangedListener)
		 */
		public void removeBufferChangedListener(IBufferChangedListener listener) {
			if (this.changeListeners != null) {
				this.changeListeners.remove(listener);
				if (this.changeListeners.size() == 0) {
					this.changeListeners = null;
				}
			}
		}

		/*
		 * @see IBuffer#replace(int, int, char[])
		 */
		public void replace(int position, int length, char[] text) {
			this.hasUnsavedChanges = true;
			notifyListeners();
		}

		/*
		 * @see IBuffer#replace(int, int, String)
		 */
		public void replace(int position, int length, String text) {
			this.hasUnsavedChanges = true;
			notifyListeners();
		}

		/*
		 * @see IBuffer#save(IProgressMonitor, boolean)
		 */
		public void save(IProgressMonitor progress, boolean force) {
			this.hasUnsavedChanges = false;
		}

		/*
		 * @see IBuffer#setContents(char[])
		 */
		public void setContents(char[] characters) {
			this.contents = characters;
			this.hasUnsavedChanges = true;
			notifyListeners();
		}

		/*
		 * @see IBuffer#setContents(String)
		 */
		public void setContents(String characters) {
			this.contents = characters.toCharArray();
			this.hasUnsavedChanges = true;
			notifyListeners();
		}

}
