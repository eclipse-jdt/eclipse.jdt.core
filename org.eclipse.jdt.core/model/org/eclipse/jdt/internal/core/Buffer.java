package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

import java.io.*;
import java.util.*;

/**
 * @see IBuffer
 */
public class Buffer implements IBuffer {
	protected BufferManager fManager;
	protected IFile fFile;
	protected int fFlags;
	protected char[] fContents;
	protected Vector fChangeListeners;
	protected IOpenable fOwner;
	protected int fGapStart= -1;
	protected int fGapEnd= -1;

	protected int fHighWatermark= 300;
	protected int fLowWatermark= 50;
	protected Object fLock= new Object();

	String lineSeparator;
	 
	protected static final int F_HAS_UNSAVED_CHANGES= 1;
	protected static final int F_IS_READ_ONLY= 2;
	protected static final int F_IS_CLOSED= 4;
/**
 * Creates a new buffer without an underlying resource.
 */
protected Buffer(BufferManager manager, char[] contents, IOpenable owner, boolean readOnly) {
	fManager = manager;
	fFile = null;
	fContents = contents;
	fOwner = owner;
	fFlags |= F_HAS_UNSAVED_CHANGES;
	setReadOnly(readOnly);
}
/**
 * Creates a new buffer on an underlying resource.
 */
protected Buffer(BufferManager manager, IFile file, char[] contents, IOpenable owner, boolean readOnly) {
	fManager = manager;
	fFile = file;
	fContents = contents;
	fOwner = owner;
	setReadOnly(readOnly);
}
/**
 * @see IBuffer
 */
public void addBufferChangedListener(IBufferChangedListener listener) {
	if (fChangeListeners == null) {
		fChangeListeners = new Vector(5);
	}
	if (!fChangeListeners.contains(listener)) {
		fChangeListeners.addElement(listener);
	}
}
/**
 * The <code>sizeHint</code> represents the range that will be filled afterwards.
 * If the gap is already at the right position, it must only be
 * resized if it will be no longer between low and high watermark.
 */
protected void adjustGap(int position, int sizeHint) {
	if (position == fGapStart) {
		int size = (fGapEnd - fGapStart) - sizeHint;
		if (fLowWatermark <= size && size <= fHighWatermark)
			return;
	}
	moveAndResizeGap(position, sizeHint);
}
/**
 * Append the <code>text</code> to the actual content, the gap is moved
 * to the end of the <code>text</code>.
 */
public void append(char[] text) {
	this.append(text, false);
}
/**
 * Append the <code>text</code> to the actual content, the gap is moved
 * to the end of the <code>text</code>.
 */
public void append(char[] text, boolean convert) {
	if (!isReadOnly()) {
		if (text == null || text.length == 0) {
			return;
		}
		if (convert) text = this.normalizeCRs(text);
		int length = getLength();
		adjustGap(length, text.length);
		System.arraycopy(text, 0, fContents, length, text.length);
		fGapStart += text.length;
		fFlags |= F_HAS_UNSAVED_CHANGES;
		notifyChanged(new BufferChangedEvent(this, length, 0, new String(text)));
	}
}
/**
 * Append the <code>text</code> to the actual content, the gap is moved
 * to the end of the <code>text</code>.
 */
public void append(String text) {
	this.append(text, false);
}
/**
 * Append the <code>text</code> to the actual content, the gap is moved
 * to the end of the <code>text</code>.
 */
public void append(String text, boolean convert) {
	if (text == null) {
		return;
	}
	if (!isReadOnly()) {
		if (convert) text = this.normalizeCRs(text);
		int textLength = text.length();
		if (textLength == 0) {
			return;
		}
		int length = getLength();
		adjustGap(length, textLength);
		System.arraycopy(text.toCharArray(), 0, fContents, length, textLength);
		fGapStart += textLength;
		fFlags |= F_HAS_UNSAVED_CHANGES;
		notifyChanged(new BufferChangedEvent(this, length, 0, text));
	}
}
/**
 * @see IBuffer
 */
public void close() throws IllegalArgumentException {
	BufferChangedEvent event = null;
	synchronized (fLock) {
		if (isClosed())
			return;
		event = new BufferChangedEvent(this, 0, 0, null);
		fContents = null;
		fFlags |= F_IS_CLOSED;
		fManager.removeBuffer(this);
	}
	notifyChanged(event); // notify outside of synchronized block
	fChangeListeners = null;
}
/**
 * Finds the first line separator used by the given text.
 *
 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>,
 *			or <code>null</code> if none found
 */
private String findLineSeparator(char[] text) {
	// find the first line separator
	int length = text.length;
	if (length > 0) {
		char nextChar = text[0];
		for (int i = 0; i < length; i++) {
			char currentChar = nextChar;
			nextChar = i < length-1 ? text[i+1] : ' ';
			switch (currentChar) {
				case '\n': return "\n"; //$NON-NLS-1$
				case '\r': return nextChar == '\n' ? "\r\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	// not found
	return null;
}
/**
 * @see IBuffer
 */
public char getChar(int position) {
	synchronized (fLock) {
		if (position < fGapStart) {
			return fContents[position];
		}
		int gapLength = fGapEnd - fGapStart;
		return fContents[position + gapLength];
	}
}
/**
 * @see IBuffer
 */
public char[] getCharacters() {
	if (fContents == null)
		return new char[0];
	synchronized (fLock) {
		if (fGapStart < 0) {
			return fContents;
		}
		int length = fContents.length;
		char[] contents = new char[length - fGapEnd + fGapStart];
		System.arraycopy(fContents, 0, contents, 0, fGapStart);
		System.arraycopy(fContents, fGapEnd, contents, fGapStart, length - fGapEnd);
		return contents;
	}
}
/**
 * @see IBuffer
 */
public String getContents() {
	if (fContents == null)
		return ""; //$NON-NLS-1$
	synchronized (fLock) {
		if (fGapStart < 0) {
			return new String(fContents);
		}
		int length = fContents.length;
		StringBuffer buffer = new StringBuffer(length - fGapEnd + fGapStart);
		buffer.append(fContents, 0, fGapStart);
		buffer.append(fContents, fGapEnd, length - fGapEnd);
		return buffer.toString();
	}
}
/**
 * @see IBuffer
 */
public int getLength() {
	synchronized (fLock) {
		int length = fGapEnd - fGapStart;
		return (fContents.length - length);
	}
}
/**
 * Returns the line separator used by this buffer.
 * Uses the given text if none found.
 *
 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>
 */
private String getLineSeparator(char[] text) {
	if (this.lineSeparator == null) {
		// search in this buffer's contents first
		this.lineSeparator = this.findLineSeparator(this.getCharacters());
		if (this.lineSeparator == null) {
			// search in the given text
			this.lineSeparator = this.findLineSeparator(text);
			if (this.lineSeparator == null) {
				// default to system line separator
				return JavaModelManager.LINE_SEPARATOR;
			}
		}
	}
	return this.lineSeparator;
}
/**
 * @see IBuffer
 */
public IOpenable getOwner() {
	return fOwner;
}
/**
 * @see IBuffer
 */
public String getText(int offset, int length) {
	if (fContents == null)
		return ""; //$NON-NLS-1$
	synchronized (fLock) {
		if (offset + length < fGapStart)
			return new String(fContents, offset, length);
		if (fGapStart < offset) {
			int gapLength = fGapEnd - fGapStart;
			return new String(fContents, offset + gapLength, length);
		}
		StringBuffer buf = new StringBuffer();
		buf.append(fContents, offset, fGapStart - offset);
		buf.append(fContents, fGapEnd, offset + length - fGapStart);
		return buf.toString();
	}
}
/**
 * @see IBuffer
 */
public IResource getUnderlyingResource() {
	return fFile;
}
/**
 * @see IBuffer
 */
public boolean hasUnsavedChanges() {
	return (fFlags & F_HAS_UNSAVED_CHANGES) != 0;
}
/**
 * @see IBuffer
 */
public boolean isClosed() {
	return (fFlags & F_IS_CLOSED) != 0;
}
/**
 * @see IBuffer
 */
public boolean isReadOnly() {
	return (fFlags & F_IS_READ_ONLY) != 0;
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
	int oldSize = fGapEnd - fGapStart;
	int newSize = fHighWatermark + size;
	if (newSize < 0) {
		if (oldSize > 0) {
			content = new char[fContents.length - oldSize];
			System.arraycopy(fContents, 0, content, 0, fGapStart);
			System.arraycopy(fContents, fGapEnd, content, fGapStart, content.length - fGapStart);
			fContents = content;
		}
		fGapStart = fGapEnd = position;
		return;
	}
	content = new char[fContents.length + (newSize - oldSize)];
	int newGapStart = position;
	int newGapEnd = newGapStart + newSize;
	if (oldSize == 0) {
		System.arraycopy(fContents, 0, content, 0, newGapStart);
		System.arraycopy(fContents, newGapStart, content, newGapEnd, content.length - newGapEnd);
	} else
		if (newGapStart < fGapStart) {
			int delta = fGapStart - newGapStart;
			System.arraycopy(fContents, 0, content, 0, newGapStart);
			System.arraycopy(fContents, newGapStart, content, newGapEnd, delta);
			System.arraycopy(fContents, fGapEnd, content, newGapEnd + delta, fContents.length - fGapEnd);
		} else {
			int delta = newGapStart - fGapStart;
			System.arraycopy(fContents, 0, content, 0, fGapStart);
			System.arraycopy(fContents, fGapEnd, content, fGapStart, delta);
			System.arraycopy(fContents, fGapEnd + delta, content, newGapEnd, content.length - newGapEnd);
		}
	fContents = content;
	fGapStart = newGapStart;
	fGapEnd = newGapEnd;
}
/**
 * Normalizes the cariage returns in the given text.
 * They are all changed  to use this buffer's line sepatator.
 */
private char[] normalizeCRs(char[] text) {
	CharArrayBuffer buffer = new CharArrayBuffer();
	int lineStart = 0;
	int length = text.length;
	if (length == 0) return text;
	String lineSeparator = this.getLineSeparator(text);
	char nextChar = text[0];
	for (int i = 0; i < length; i++) {
		char currentChar = nextChar;
		nextChar = i < length-1 ? text[i+1] : ' ';
		switch (currentChar) {
			case '\n':
				int lineLength = i-lineStart;
				char[] line = new char[lineLength];
				System.arraycopy(text, lineStart, line, 0, lineLength);
				buffer.append(line);
				buffer.append(lineSeparator);
				lineStart = i+1;
				break;
			case '\r':
				lineLength = i-lineStart;
				line = new char[lineLength];
				System.arraycopy(text, lineStart, line, 0, lineLength);
				buffer.append(line);
				buffer.append(lineSeparator);
				if (nextChar == '\n') {
					nextChar = ' ';
					i++;
				}
				lineStart = i+1;
				break;
		}
	}
	char[] lastLine;
	if (lineStart > 0) {
		int lastLineLength = length-lineStart;
		if (lastLineLength > 0) {
			lastLine = new char[lastLineLength];
			System.arraycopy(text, lineStart, lastLine, 0, lastLineLength);
			buffer.append(lastLine);
		}
		return buffer.getContents();
	} else {
		return text;
	}
}
/**
 * Normalizes the cariage returns in the given text.
 * They are all changed  to use this buffer's line sepatator.
 */
private String normalizeCRs(String text) {
	return new String(this.normalizeCRs(text.toCharArray()));
}
/**
 * Notify the listeners that this buffer has changed.
 * To avoid deadlock, this should not be called in a synchronized block.
 */
protected void notifyChanged(BufferChangedEvent event) {
	if (fChangeListeners != null) {
		for (int i = 0, size = fChangeListeners.size(); i < size; ++i) {
			IBufferChangedListener listener = (IBufferChangedListener) fChangeListeners.elementAt(i);
			listener.bufferChanged(event);
		}
	}
}
/**
 * @see IBuffer
 */
public void removeBufferChangedListener(IBufferChangedListener listener) {
	if (fChangeListeners != null) {
		fChangeListeners.removeElement(listener);
		if (fChangeListeners.size() == 0) {
			fChangeListeners = null;
		}
	}
}
/**
 * Replaces <code>length</code> characters starting from <code>position</code> with <code>text<code>.
 * After that operation, the gap is placed at the end of the 
 * inserted <code>text</code>.
 */
public void replace(int position, int length, char[] text) {
	this.replace(position, length, text, false);
}
/**
 * Replaces <code>length</code> characters starting from <code>position</code> with <code>text<code>.
 * After that operation, the gap is placed at the end of the 
 * inserted <code>text</code>.
 */
public void replace(int position, int length, char[] text, boolean convert) {
	if (!isReadOnly()) {
		if (text == null) {
			text = new char[0];
		} else {
			if (convert) text = this.normalizeCRs(text);
		}
		synchronized (fLock) {
			// move gap
			adjustGap(position + length, text.length - length);

			// overwrite
			int min = Math.min(text.length, length);
			for (int i = position, j = 0; i < position + min; i++, j++)
				fContents[i] = text[j];
			if (length > text.length) {
				// enlarge the gap
				fGapStart -= (length - text.length);
			} else
				if (text.length > length) {
					// shrink gap
					fGapStart += (text.length - length);
					for (int i = length; i < text.length; i++)
						fContents[position + i] = text[i];
				}
		}
		fFlags |= F_HAS_UNSAVED_CHANGES;
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
	this.replace(position, length, text, false);
}
/**
 * Replaces <code>length</code> characters starting from <code>position</code> with <code>text<code>.
 * After that operation, the gap is placed at the end of the 
 * inserted <code>text</code>.
 */
public void replace(int position, int length, String text, boolean convert) {
	if (!isReadOnly()) {
		int textLength = 0;
		if (text != null) {
			if (convert) text = this.normalizeCRs(text);
			textLength = text.length();
		}
		
		synchronized (fLock) {
			// move gap
			adjustGap(position + length, textLength - length);

			// overwrite
			int min = Math.min(textLength, length);
			for (int i = position, j = 0; i < position + min; i++, j++)
				fContents[i] = text.charAt(j);
			if (length > textLength) {
				// enlarge the gap
				fGapStart -= (length - textLength);
			} else
				if (textLength > length) {
					// shrink gap
					fGapStart += (textLength - length);
					for (int i = length; i < textLength; i++)
						fContents[position + i] = text.charAt(i);
				}
		}
		fFlags |= F_HAS_UNSAVED_CHANGES;
		
		notifyChanged(new BufferChangedEvent(this, position, length, text));
	}
}
/**
 * @see IBuffer
 */
public void save(IProgressMonitor progress, boolean force) throws JavaModelException {

	// determine if saving is required 
	if (isReadOnly() || fFile == null) {
		return;
	}
	synchronized (fLock) {
		if (!hasUnsavedChanges())
			return;
		byte[] bytes = getContents().getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

		// use a platform operation to update the resource contents
		try {
			fFile.setContents(stream, force, true, null); // record history
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}

		// the resource no longer has unsaved changes
		fFlags &= ~ (F_HAS_UNSAVED_CHANGES);
	}
}
/**
 * @see IBuffer
 */
public void setContents(char[] contents) {
	this.setContents(contents, false);
}
/**
 * @see IBuffer
 */
public void setContents(char[] contents, boolean convert) {
	if (!isReadOnly()) {
		String string = null;
		if (contents != null) {
			if (convert) contents = this.normalizeCRs(contents);
			string = new String(contents);
		}
		BufferChangedEvent event = new BufferChangedEvent(this, 0, this.getLength(), string);
		synchronized (fLock) {
			fContents = contents;
			fFlags |= F_HAS_UNSAVED_CHANGES;
			fGapStart = -1;
			fGapEnd = -1;
		}
		notifyChanged(event);
	}
}
/**
 * @see IBuffer
 */
public void setContents(String contents) {
	this.setContents(contents, false);
}
/**
 * @see IBuffer
 */
public void setContents(String contents, boolean convert) {
	if (!isReadOnly()) {
		char[] charContents = null;
		if (contents != null) {
			charContents = contents.toCharArray();
			if (convert) {
				charContents = this.normalizeCRs(charContents);
				contents = new String(charContents);
			}
		}
		BufferChangedEvent event = new BufferChangedEvent(this, 0, this.getLength(), contents);
		synchronized (fLock) {
			fContents = charContents;
			fFlags |= F_HAS_UNSAVED_CHANGES;
			fGapStart = -1;
			fGapEnd = -1;
		}
		notifyChanged(event);
	}
}
/**
 * Sets this <code>Buffer</code> to be read only.
 */
protected void setReadOnly(boolean readOnly) {
	if (readOnly) {
		fFlags |= F_IS_READ_ONLY;
	} else {
		fFlags &= ~(F_IS_READ_ONLY);
	}
}
/**
 * Adjusts low and high water mark to the specified values.
 */
public void setWaterMarks(int low, int high) {
	fLowWatermark = low;
	fHighWatermark = high;
}
public String toString() {
	char[] contents = this.getCharacters();
	int length = contents.length;
	StringBuffer buffer = new StringBuffer(length);
	buffer.append("Buffer:\n"); //$NON-NLS-1$
	for (int i = 0; i < length; i++) {
		char car = contents[i];
		switch (car) {
			case '\n': 
				buffer.append("\\n\n"); //$NON-NLS-1$
				break;
			case '\r':
				if (i < length-1 && contents[i+1] == '\n') {
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
