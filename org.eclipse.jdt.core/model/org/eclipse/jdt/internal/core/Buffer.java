package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

import java.io.*;
import java.util.*;

/**
 * @see IBuffer
 */
public class Buffer implements IBuffer {
	protected IFile file;
	protected int flags;
	protected char[] contents;
	protected ArrayList changeListeners;
	protected IOpenable owner;
	protected int gapStart= -1;
	protected int gapEnd= -1;

	protected int highWatermark= 300;
	protected int lowWatermark= 50;
	protected Object lock= new Object();

	protected static final int F_HAS_UNSAVED_CHANGES= 1;
	protected static final int F_IS_READ_ONLY= 2;
	protected static final int F_IS_CLOSED= 4;

/**
 * Creates a new buffer on an underlying resource.
 */
protected Buffer(IFile file, IOpenable owner, boolean readOnly) {
	this.file = file;
	this.owner = owner;
	if (file == null) {
		setReadOnly(readOnly);
	}
}
/**
 * @see IBuffer
 */
public void addBufferChangedListener(IBufferChangedListener listener) {
	if (this.changeListeners == null) {
		this.changeListeners = new ArrayList(5);
	}
	if (!this.changeListeners.contains(listener)) {
		this.changeListeners.add(listener);
	}
}
/**
 * The <code>sizeHint</code> represents the range that will be filled afterwards.
 * If the gap is already at the right position, it must only be
 * resized if it will be no longer between low and high watermark.
 */
protected void adjustGap(int position, int sizeHint) {
	if (position == this.gapStart) {
		int size = (this.gapEnd - this.gapStart) - sizeHint;
		if (this.lowWatermark <= size && size <= this.highWatermark)
			return;
	}
	moveAndResizeGap(position, sizeHint);
}
/**
 * Append the <code>text</code> to the actual content, the gap is moved
 * to the end of the <code>text</code>.
 */
public void append(char[] text) {
	if (!isReadOnly()) {
		if (text == null || text.length == 0) {
			return;
		}
		int length = getLength();
		adjustGap(length, text.length);
		System.arraycopy(text, 0, this.contents, length, text.length);
		this.gapStart += text.length;
		this.flags |= F_HAS_UNSAVED_CHANGES;
		notifyChanged(new BufferChangedEvent(this, length, 0, new String(text)));
	}
}
/**
 * Append the <code>text</code> to the actual content, the gap is moved
 * to the end of the <code>text</code>.
 */
public void append(String text) {
	if (text == null) {
		return;
	}
	if (!isReadOnly()) {
		int textLength = text.length();
		if (textLength == 0) {
			return;
		}
		int length = getLength();
		adjustGap(length, textLength);
		System.arraycopy(text.toCharArray(), 0, this.contents, length, textLength);
		this.gapStart += textLength;
		this.flags |= F_HAS_UNSAVED_CHANGES;
		notifyChanged(new BufferChangedEvent(this, length, 0, text));
	}
}
/**
 * @see IBuffer
 */
public void close() throws IllegalArgumentException {
	BufferChangedEvent event = null;
	synchronized (this.lock) {
		if (isClosed())
			return;
		event = new BufferChangedEvent(this, 0, 0, null);
		this.contents = null;
		this.flags |= F_IS_CLOSED;
	}
	notifyChanged(event); // notify outside of synchronized block
	this.changeListeners = null;
}
/**
 * @see IBuffer
 */
public char getChar(int position) {
	synchronized (this.lock) {
		if (position < this.gapStart) {
			return this.contents[position];
		}
		int gapLength = this.gapEnd - this.gapStart;
		return this.contents[position + gapLength];
	}
}
/**
 * @see IBuffer
 */
public char[] getCharacters() {
	if (this.contents == null) return null;
	synchronized (this.lock) {
		if (this.gapStart < 0) {
			return this.contents;
		}
		int length = this.contents.length;
		char[] newContents = new char[length - this.gapEnd + this.gapStart];
		System.arraycopy(this.contents, 0, newContents, 0, this.gapStart);
		System.arraycopy(this.contents, this.gapEnd, newContents, this.gapStart, length - this.gapEnd);
		return newContents;
	}
}
/**
 * @see IBuffer
 */
public String getContents() {
	if (this.contents == null) return null;
	synchronized (this.lock) {
		if (this.gapStart < 0) {
			return new String(this.contents);
		}
		int length = this.contents.length;
		StringBuffer buffer = new StringBuffer(length - this.gapEnd + this.gapStart);
		buffer.append(this.contents, 0, this.gapStart);
		buffer.append(this.contents, this.gapEnd, length - this.gapEnd);
		return buffer.toString();
	}
}
/**
 * @see IBuffer
 */
public int getLength() {
	synchronized (this.lock) {
		int length = this.gapEnd - this.gapStart;
		return (this.contents.length - length);
	}
}
/**
 * @see IBuffer
 */
public IOpenable getOwner() {
	return this.owner;
}
/**
 * @see IBuffer
 */
public String getText(int offset, int length) {
	if (this.contents == null)
		return ""; //$NON-NLS-1$
	synchronized (this.lock) {
		if (offset + length < this.gapStart)
			return new String(this.contents, offset, length);
		if (this.gapStart < offset) {
			int gapLength = this.gapEnd - this.gapStart;
			return new String(this.contents, offset + gapLength, length);
		}
		StringBuffer buf = new StringBuffer();
		buf.append(this.contents, offset, this.gapStart - offset);
		buf.append(this.contents, this.gapEnd, offset + length - this.gapStart);
		return buf.toString();
	}
}
/**
 * @see IBuffer
 */
public IResource getUnderlyingResource() {
	return this.file;
}
/**
 * @see IBuffer
 */
public boolean hasUnsavedChanges() {
	return (this.flags & F_HAS_UNSAVED_CHANGES) != 0;
}
/**
 * @see IBuffer
 */
public boolean isClosed() {
	return (this.flags & F_IS_CLOSED) != 0;
}
/**
 * @see IBuffer
 */
public boolean isReadOnly() {
	if (this.file == null) {
		return (this.flags & F_IS_READ_ONLY) != 0;
	} else {
		return this.file.isReadOnly();
	}
}
/**
 * Moves the gap to location and adjust its size to the
 * anticipated change size. The size represents the expected 
 * range of the gap that will be filled after the gap has been moved.
 * Thus the gap is resized to actual size + the specified size and
 * moved to the given position.
 */
protected void moveAndResizeGap(int position, int size) {
	char[] content = null;
	int oldSize = this.gapEnd - this.gapStart;
	int newSize = this.highWatermark + size;
	if (newSize < 0) {
		if (oldSize > 0) {
			content = new char[this.contents.length - oldSize];
			System.arraycopy(this.contents, 0, content, 0, this.gapStart);
			System.arraycopy(this.contents, this.gapEnd, content, this.gapStart, content.length - this.gapStart);
			this.contents = content;
		}
		this.gapStart = this.gapEnd = position;
		return;
	}
	content = new char[this.contents.length + (newSize - oldSize)];
	int newGapStart = position;
	int newGapEnd = newGapStart + newSize;
	if (oldSize == 0) {
		System.arraycopy(this.contents, 0, content, 0, newGapStart);
		System.arraycopy(this.contents, newGapStart, content, newGapEnd, content.length - newGapEnd);
	} else
		if (newGapStart < this.gapStart) {
			int delta = this.gapStart - newGapStart;
			System.arraycopy(this.contents, 0, content, 0, newGapStart);
			System.arraycopy(this.contents, newGapStart, content, newGapEnd, delta);
			System.arraycopy(this.contents, this.gapEnd, content, newGapEnd + delta, this.contents.length - this.gapEnd);
		} else {
			int delta = newGapStart - this.gapStart;
			System.arraycopy(this.contents, 0, content, 0, this.gapStart);
			System.arraycopy(this.contents, this.gapEnd, content, this.gapStart, delta);
			System.arraycopy(this.contents, this.gapEnd + delta, content, newGapEnd, content.length - newGapEnd);
		}
	this.contents = content;
	this.gapStart = newGapStart;
	this.gapEnd = newGapEnd;
}
/**
 * Notify the listeners that this buffer has changed.
 * To avoid deadlock, this should not be called in a synchronized block.
 */
protected void notifyChanged(BufferChangedEvent event) {
	if (this.changeListeners != null) {
		for (int i = 0, size = this.changeListeners.size(); i < size; ++i) {
			IBufferChangedListener listener = (IBufferChangedListener) this.changeListeners.get(i);
			listener.bufferChanged(event);
		}
	}
}
/**
 * @see IBuffer
 */
public void removeBufferChangedListener(IBufferChangedListener listener) {
	if (this.changeListeners != null) {
		this.changeListeners.remove(listener);
		if (this.changeListeners.size() == 0) {
			this.changeListeners = null;
		}
	}
}
/**
 * Replaces <code>length</code> characters starting from <code>position</code> with <code>text<code>.
 * After that operation, the gap is placed at the end of the 
 * inserted <code>text</code>.
 */
public void replace(int position, int length, char[] text) {
	if (!isReadOnly()) {
		if (text == null) {
			text = new char[0];
		}
		synchronized (this.lock) {
			// move gap
			adjustGap(position + length, text.length - length);

			// overwrite
			int min = Math.min(text.length, length);
			for (int i = position, j = 0; i < position + min; i++, j++)
				this.contents[i] = text[j];
			if (length > text.length) {
				// enlarge the gap
				this.gapStart -= (length - text.length);
			} else
				if (text.length > length) {
					// shrink gap
					this.gapStart += (text.length - length);
					for (int i = length; i < text.length; i++)
						this.contents[position + i] = text[i];
				}
		}
		this.flags |= F_HAS_UNSAVED_CHANGES;
		String string = null;
		if (text.length > 0) {
			string = new String(text);
		}
		notifyChanged(new BufferChangedEvent(this, position, length, string));
	}
}
/**
 * Replaces <code>length</code> characters starting from <code>position</code> with <code>text<code>.
 * After that operation, the gap is placed at the end of the 
 * inserted <code>text</code>.
 */
public void replace(int position, int length, String text) {
	if (!isReadOnly()) {
		int textLength = 0;
		if (text != null) {
			textLength = text.length();
		}
		
		synchronized (this.lock) {
			// move gap
			adjustGap(position + length, textLength - length);

			// overwrite
			int min = Math.min(textLength, length);
			for (int i = position, j = 0; i < position + min; i++, j++)
				this.contents[i] = text.charAt(j);
			if (length > textLength) {
				// enlarge the gap
				this.gapStart -= (length - textLength);
			} else
				if (textLength > length) {
					// shrink gap
					this.gapStart += (textLength - length);
					for (int i = length; i < textLength; i++)
						this.contents[position + i] = text.charAt(i);
				}
		}
		this.flags |= F_HAS_UNSAVED_CHANGES;
		
		notifyChanged(new BufferChangedEvent(this, position, length, text));
	}
}
/**
 * @see IBuffer
 */
public void save(IProgressMonitor progress, boolean force) throws JavaModelException {

	// determine if saving is required 
	if (isReadOnly() || this.file == null) {
		return;
	}
	synchronized (this.lock) {
		if (!hasUnsavedChanges())
			return;
		byte[] bytes = getContents().getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

		// use a platform operation to update the resource contents
		try {
			this.file.setContents(stream, force, true, null); // record history
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}

		// the resource no longer has unsaved changes
		this.flags &= ~ (F_HAS_UNSAVED_CHANGES);
	}
}
/**
 * @see IBuffer
 */
public void setContents(char[] newContents) {
	// allow special case for first initialization 
	// after creation by buffer factory
	if (this.contents == null) {
		this.contents = newContents;
		this.flags &= ~ (F_HAS_UNSAVED_CHANGES);
		return;
	}
	
	if (!isReadOnly()) {
		String string = null;
		if (newContents != null) {
			string = new String(newContents);
		}
		BufferChangedEvent event = new BufferChangedEvent(this, 0, this.getLength(), string);
		synchronized (this.lock) {
			this.contents = newContents;
			this.flags |= F_HAS_UNSAVED_CHANGES;
			this.gapStart = -1;
			this.gapEnd = -1;
		}
		notifyChanged(event);
	}
}
/**
 * @see IBuffer
 */
public void setContents(String newContents) {
	// allow special case for first initialization 
	// after creation by buffer factory
	if (this.contents == null) {
		this.contents = newContents.toCharArray();
		this.flags &= ~ (F_HAS_UNSAVED_CHANGES);
		return;
	}
	
	if (!isReadOnly()) {
		char[] charContents = null;
		if (newContents != null) {
			charContents = newContents.toCharArray();
		}
		BufferChangedEvent event = new BufferChangedEvent(this, 0, this.getLength(), newContents);
		synchronized (this.lock) {
			this.contents = charContents;
			this.flags |= F_HAS_UNSAVED_CHANGES;
			this.gapStart = -1;
			this.gapEnd = -1;
		}
		notifyChanged(event);
	}
}
/**
 * Sets this <code>Buffer</code> to be read only.
 */
protected void setReadOnly(boolean readOnly) {
	if (readOnly) {
		this.flags |= F_IS_READ_ONLY;
	} else {
		this.flags &= ~(F_IS_READ_ONLY);
	}
}
/**
 * Adjusts low and high water mark to the specified values.
 */
public void setWaterMarks(int low, int high) {
	this.lowWatermark = low;
	this.highWatermark = high;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Owner: " + ((JavaElement)this.owner).toStringWithAncestors()); //$NON-NLS-1$
	buffer.append("\nHas unsaved changes: " + this.hasUnsavedChanges()); //$NON-NLS-1$
	buffer.append("\nIs readonly: " + this.isReadOnly()); //$NON-NLS-1$
	buffer.append("\nIs closed: " + this.isClosed()); //$NON-NLS-1$
	int length = this.contents.length;
	buffer.append("\nContents:\n"); //$NON-NLS-1$
	for (int i = 0; i < length; i++) {
		char car = this.contents[i];
		switch (car) {
			case '\n': 
				buffer.append("\\n\n"); //$NON-NLS-1$
				break;
			case '\r':
				if (i < length-1 && this.contents[i+1] == '\n') {
					buffer.append("\\r\\n\n"); //$NON-NLS-1$
					i++;
				} else {
					buffer.append("\\r\n"); //$NON-NLS-1$
				}
				break;
			default:
				buffer.append(car);
				break;
		}		
	}
	return buffer.toString();
}
}
