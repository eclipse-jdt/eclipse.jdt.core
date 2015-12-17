package org.eclipse.jdt.internal.core.pdom.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.db.Database;
import org.eclipse.jdt.internal.core.pdom.db.IndexException;

/**
 * @since 3.12
 */
public abstract class TagTreeReader {
	public static final int[] UNUSED_RESULT = new int[1];

	public static abstract class TagHandler<T> {
		abstract public T read(PDOM pdom, long address, TagTreeReader reader, int[] bytesRead);
		abstract public void write(PDOM pdom, long address, TagTreeReader reader, T toWrite, int[] bytesWritten);
		abstract public int getSize(PDOM pdom, T object, TagTreeReader reader);
		public void destruct(PDOM pdom, long address, TagTreeReader reader) {}
	}

	public static abstract class FixedSizeTagHandler<T> extends TagHandler<T> {
		protected abstract T read(PDOM pdom, long address);
		protected abstract void write(PDOM pdom, long address, T value);
		protected abstract int getSize();
		protected void destruct(PDOM pdom, long address) {}
		
		public final T read(PDOM pdom, long address, TagTreeReader reader, int[] bytesRead) {
			bytesRead[0] = getSize();
			return read(pdom, address);
		}

		@Override
		public final void write(PDOM pdom, long address, TagTreeReader reader, T value, int[] bytesWritten) {
			bytesWritten[0] = getSize();
			write(pdom, address, value);
		}

		@Override
		public final int getSize(PDOM pdom, T object, TagTreeReader reader) {
			return getSize();
		}

		@Override
		public final void destruct(PDOM pdom, long address, TagTreeReader reader) {
			destruct(pdom, address);
		}
	}

	private TagHandler<?> readers[] = new TagHandler[256];
	private Map<TagHandler<?>, Integer> values = new HashMap<>();

	public final void add(byte key, TagHandler<?> reader) {
		this.readers[key] = reader;
		this.values.put(reader, (int) key);
	}

	public final Object read(PDOM pdom, long address) {
		return read(pdom, address, UNUSED_RESULT);
	}

	public final Object read(PDOM pdom, long address, int[] bytesRead) {
		long readAddress = address;
		Database db = pdom.getDB();
		byte nextByte = db.getByte(address);
		readAddress += Database.BYTE_SIZE;
		TagHandler<?> reader = this.readers[nextByte];
		if (reader == null) {
			throw new IndexException("Found unknown tag with value " + nextByte + " at address " + address); //$NON-NLS-1$//$NON-NLS-2$
		}

		return reader.read(pdom, readAddress, this, bytesRead);
	}

	protected abstract byte getKeyFor(Object toWrite);

	public final void write(PDOM pdom, long address, Object toWrite) {
		write(pdom, address, toWrite, UNUSED_RESULT);
	}

	public final void write(PDOM pdom, long address, Object toWrite, int[] bytesWritten) {
		byte key = getKeyFor(toWrite);

		TagHandler handler = this.readers[key];

		if (handler == null) {
			throw new IndexException("Invalid key " + key + " returned from getKeyFor(...)"); //$NON-NLS-1$//$NON-NLS-2$
		}

		handler.write(pdom, address, this, (Object) toWrite, bytesWritten);
	}

	public final void destruct(PDOM pdom, long address) {
		Database db = pdom.getDB();
		long readAddress = address;
		byte nextByte = db.getByte(readAddress);
		readAddress += Database.BYTE_SIZE;

		TagHandler<?> handler = this.readers[nextByte];
		if (handler == null) {
			throw new IndexException("Found unknown tag with value " + nextByte + " at address " + address); //$NON-NLS-1$//$NON-NLS-2$
		}

		handler.destruct(pdom, readAddress, this);
	}

	public final int getSize(PDOM pdom, Object toMeasure) {
		Database db = pdom.getDB();
		byte key = getKeyFor(toMeasure);

		TagHandler handler = this.readers[key];
		if (handler == null) {
			throw new IndexException("Attempted to get size of object " + toMeasure.toString() + " with unknown key " //$NON-NLS-1$//$NON-NLS-2$
					+ key); 
		}

		return handler.getSize(pdom, toMeasure, this);
	}
}
