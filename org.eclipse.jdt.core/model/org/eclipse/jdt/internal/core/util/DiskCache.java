package org.eclipse.jdt.internal.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Util;

import java.io.*;
import java.util.*;

/**
 * A <code>DiskCache</code> is an on-disk cache of files with a configurable maximum size.
 * It implements a Least-Recently-Used (LRU) policy.
 * <p>
 * Entries are keyed by string.  The storage for an entry is written to a stream when an
 * entry is added, but the size of the entry must be known a-priori.
 * Extra information can optionally be associated with an entry.
 * <p>
 * Volatile state in the cache is saved periodically.  The interval between saves can be
 * set using <code>setPeriodicSaveInterval(int)</code>.
 * <p>
 * The disk cache must be explicitly closed using <code>close()</code> when it is no longer needed.
 */
public class DiskCache {

	/**
	 * The directory containing the cache's files.
	 */
	File fDirectory;

	/**
	 * Max size of the cache in K.
	 */
	int fSpaceLimit;
	
	/**
	 * Used for creating cache entry files.
	 */
	AnonymousFileSource fFileSource;

	/**
	 * List of files to be deleted later, due to failures to delete
	 * or because file is still open.
	 */
	Vector fFilesToBeDeleted = new Vector();
	
	/**
	 * Extends the LRU cache entry to add extra fields needed by disk cache.
	 * We could avoided overriding the cache entry class by creating another object 
	 * to hold these fields in the value field of a normal cache entry, but this way
	 * uses fewer objects.
	 * It is static because it doesn't require an outer instance.
	 */
	protected static class Entry extends LRUCache.LRUCacheEntry {
			
		long fLastAccess;
		byte[] fExtraInfo;
		int fFlags;
		static final int F_COMPLETE = 1;
			
		Entry(String key, int size, String fileName, long lastAccess, byte[] extraInfo, int flags) {
			// use super's key, value and space fields for our key, fileName and size.
			super(key, fileName, size);
			fLastAccess = lastAccess;
			fExtraInfo = extraInfo;
			fFlags = flags;
		}

		DiskCacheEntry asDiskCacheEntry() {
			return new DiskCacheEntry((String)_fKey, _fSpace, (String)_fValue, fLastAccess, fExtraInfo);
		}
	}
		
	/**
	 * The maximum size of the cache, in number of K.
	 */
	protected class EntryCache extends LRUCache {

		/**
		 * Add a new entry.
		 */
		void add(Entry entry) {
			removeKey(entry._fKey);
			makeSpace(entry._fSpace);
			privateAddEntry(entry, false);
		}

		/**
		 * An item is being removed from the cache. Delete its file.
		 */
		protected void privateNotifyDeletionFromCache(LRUCacheEntry entry) {
			String fileName = (String)entry._fValue;
			if ((((Entry)entry).fFlags & Entry.F_COMPLETE) != 0) {
				/* Entry has been complete written, so we can delete it. */
				boolean success = deleteFile(fileName);
				if (success) return;
				/* If success == false, then it is inconsistent with the flag's 
				 * F_COMPLETE mask. Something is wrong. */
			} 
			/* Otherwise, or if file deletion failed (probably due to 
			 * a stream on it still being open), mark the file for deletion later 
			 * (at save or reload time) checking again to see if it's closed. */
			 rememberToDelete(fileName);
		}
			
		/**
		 * Lookup an entry.  Expose the Entry object.
		 */
		Entry get(String key) {
			Entry entry = (Entry) fEntryTable.get(key);
			if (entry == null) {
				return null;
			}
			updateTimestamp (entry);
			return entry;
		}

		/**
		 * Remove an entry.  Return it if present.
		 */
		Entry removeKey(String key) {
			Entry entry = (Entry) fEntryTable.get(key);
			if (entry == null) {
				return null;
			}
			this.privateRemoveEntry (entry, false);
			return entry;
		}
			
		/**
		 * Updates the timestamp for the given entry, ensuring that the queue is 
		 * kept in correct order.  The entry must exist
		 */
		protected void updateTimestamp (LRUCacheEntry entry) {
			((Entry)entry).fLastAccess = System.currentTimeMillis();
			super.updateTimestamp(entry);
		}

		/**
		 * Return the number of entries.
		 */
		int getNumEntries() {
			return fEntryTable.size();
		}

		/**
		 * Return an enumeration of the Entry objects in the cache
		 * in most-recently- to least-recently-used order.
		 */
		public Enumeration getEntries() {
			return new Enumeration() {

				Entry current = (Entry)fEntryQueue;
				
				public boolean hasMoreElements() {
					return current != null;
				}
				
				public Object nextElement() {
					if (current == null) {
						throw new NoSuchElementException();
					}
					Entry result = current;
					current = (Entry)current._fNext;
					return result;
				}
			};
		}
	}

	EntryCache fEntryCache;

	static final String CONTENTS_FILE_NAME = "contents"/*nonNLS*/;
	static final String TEMP_CONTENTS_FILE_NAME = "tempcont"/*nonNLS*/;
	static final String CONTENTS_VERSION = "DiskCache.V1.0"/*nonNLS*/;

	/**
	 * Set to true when cache is modified, cleared when saved.
	 */
	boolean fIsDirty = false;

	/**
	 * Thread for doing periodic saves.  Created only if needed.
	 */
	Thread fPeriodicSaveThread;

	/**
	 * Interval for periodic save checks, in ms.  5 sec by default.
	 */
	int fPeriodicSaveInterval = 60000;
	
/**
 * Create a disk cache which uses the given filesystem directory
 * and has the given space limit (in kilobytes).
 */
public DiskCache(File directory, int spaceLimit) {
	fDirectory = directory;
	fFileSource = new AnonymousFileSource(directory);
	fSpaceLimit = spaceLimit;
	createEntryCache();
	try {
		read();
		deleteFilesToBeDeleted();
	}
	catch (IOException e) {
	}
}
	/**
	 * Add an entry to the cache.  The size of the entry in bytes must be given.
	 * Extra information in the form of a byte array may optionally be associated
	 * with the entry (pass null if no extra info is needed).
	 * Returns an initially empty OutputStream to which the contents of the entry
	 * should be written immediately following this operation.
	 *
	 * @throws IOException if a file for the entry's contents could not be created
	 */
	public OutputStream add(final String key, int size, byte[] extraInfo) throws IOException {
		if (size > fSpaceLimit*1024) {
			throw new IOException("Entry size greater than cache size");
		}
		final File file;
		OutputStream output;
		// Allocate the file first.  If errors occur, no entry is added.
		synchronized(fFileSource) {
			file = fFileSource.getAnonymousFile();
			output = new FileOutputStream(file);
		}
		long lastAccess = System.currentTimeMillis();
		if (extraInfo != null && extraInfo.length == 0) {
			extraInfo = null;
		}
		final Entry entry = new Entry(key, size, file.getName(), lastAccess, extraInfo, 0);
		synchronized(fEntryCache) {
			// Only use the simple file name.
			fEntryCache.add(entry);
		}

		// Mark as modified here, as well as when the entry is complete.
		// Hopefully the content is received before the periodic save interval,
		// so state is saved at most once for this entry.
		modified();
		
		// Return a filter stream which sets the complete flag when the stream is closed.
		return new FilterOutputStream(output) {
			public void write(byte b[], int off, int len) throws IOException {
				out.write(b, off, len);
			}
			
			private void closeHelper() throws IOException {
				super.close();
				// Ensure entry is still valid.  
				// It may have been removed or replaced in the interim.
				synchronized(fEntryCache) {
					if (fEntryCache.get(key) != entry) {
						String fileName = (String)entry._fValue;
						boolean success = deleteFile(fileName);
						if (!success) {
							rememberToDelete(fileName);
						}
					}
				}
				modified(); 
			}
			
			public void close() throws IOException {
				entry.fFlags |= Entry.F_COMPLETE;
				closeHelper();
			}
			
			public void finalize() throws Throwable { 
				closeHelper();
				super.finalize();
			}
			
			/* For debugging/testing ONLY. Do not use outside of test suites.*/
			public String toString() {
				return file.toString();
			}
		};
	}
	/**
	 * Clear the cache.  All entries are removed and their associated files are deleted.
	 */
	public void clearCache() {
		synchronized(fEntryCache) {
			fEntryCache.flush();
		}
		// Be sure to save the cleared contents.
		modified(); 
	}
	/**
	 * Close the cache.  
	 * Flush incomplete entries, save volatile state if needed, and halt periodic saves.
	 */
	public synchronized void close() {
		if (fIsDirty || flushIncompleteEntries()) {
			try {
				save();
			}
			catch (IOException e) {
				// Ignore
			}
		}
		Thread thread = fPeriodicSaveThread;
		if (thread != null) {
			fPeriodicSaveThread = null;
			// Interrupt its sleep.
			thread.interrupt();
		}
	}
	/**
	 * Internal - (Re)create the internal entry cache.
	 */
	protected void createEntryCache() {
		fEntryCache = new EntryCache();
		fEntryCache.setSpaceLimit(fSpaceLimit*1024);
	}
	/**
	 * Internal - Delete the file for an entry. Returns the
	 * "success flag" result from java.io.File's delete().
	 */
	protected boolean deleteFile(String fileName) {
		File fileToDelete = fFileSource.fileForName(fileName);
		fileToDelete.delete();
		// Could have already been deleted.
		boolean success = !fileToDelete.exists();
		if(success && fFilesToBeDeleted.contains(fileName))
			fFilesToBeDeleted.removeElement(fileName);
		return success; 
	}
/**
 * Internal - Deletes the files that have been flagged for deletion. Returns
 * the number of flagged files that remain undeleted.
 */
public int deleteFilesToBeDeleted() {
	synchronized (fFilesToBeDeleted) {
		Vector clone = (Vector) fFilesToBeDeleted.clone();
		Enumeration e = clone.elements();
		while(e.hasMoreElements()) {
			String fileName = (String) e.nextElement();
			boolean success = deleteFile(fileName);
			if (success) 
				fFilesToBeDeleted.removeElement(fileName);
		}
		return fFilesToBeDeleted.size();
	}
}
	/**
	 * Flush all incomplete entries from the cache.
	 * Returns true if some were flushed.
	 */
	protected boolean flushIncompleteEntries() {
		Vector v = new Vector();
		for (Enumeration e = fEntryCache.getEntries(); e.hasMoreElements();) {
			Entry entry = (Entry)e.nextElement();
			if ((entry.fFlags & Entry.F_COMPLETE) == 0) {
				v.addElement(entry._fKey);
			}
		}
		for (int i = 0, size = v.size(); i < size; ++i) {
			fEntryCache.removeKey((String)v.elementAt(i));
		}
		return v.size() > 0;
	}
	/**
	 * Returns the directory which holds the cache.
	 */
	public File getDirectory() {
		return fDirectory;
	}
/**
 * For debugging/testing purposes.
 */
public int getNumberOfFilesToBeDeleted() {
	return fFilesToBeDeleted.size();
}
	/**
	 * Return the number of entries in the cache.
	 */
	public int getNumEntries() {
		return fEntryCache.getNumEntries();
	}
	/**
	 * Returns the delay in milliseconds between periodic saves.
	 */
	public int getPeriodicSaveInterval() {
		return fPeriodicSaveInterval;
	}
	/**
	 * Returns the space limit of the cache, in Kilobytes.
	 */
	public int getSpaceLimit() {
		return fSpaceLimit;
	}
	/**
	 * Returns the space used by the cache, in Kilobytes.
	 */
	public int getSpaceUsed() {
		synchronized(fEntryCache) {
			return (fEntryCache.getCurrentSpace() + 1023) / 1024; // Take ceiling.
		}
	}
	/**
	 * Returns an enumeration on the keys (Strings)
	 * of the entries in the cache.
	 */
	public Enumeration keys() {
		return fEntryCache.keys();
	}
	/**
	 * Look up an entry in the cache.  Answer the entry if found, or null if not found.
	 * If the contents are to be retrieved, use the <code>open</code> method rather than
	 * directly referring to the file using the returned file name.
	 *
	 * @see DiskCache#open
	 */
	public DiskCacheEntry lookup(String key) {
		Entry entry;
		synchronized(fEntryCache) {
			entry = fEntryCache.get(key);
		}
		if (entry == null || (entry.fFlags & Entry.F_COMPLETE) == 0) {
			return null;
		}
		else {
			// If the entry was found, then the contents should be
			// resaved because the order of items in it has changed.
			modified();
			return entry.asDiskCacheEntry();
		}
	}
	/**
	 * Internal - The cache has been modified.  Remember to save the state.
	 * Method is synchronized to protect fPeriodicSaveThread from concurrent access.
	 */
	protected synchronized void modified() {
		fIsDirty = true;
		if (fPeriodicSaveThread == null || !fPeriodicSaveThread.isAlive()) {
			startPeriodicSaveThread();
		}
	}
	/**
	 * Open an input stream on the contents of the given entry.
	 * The passed entry must be the result of a previous lookup operation.
	 * If the cache has been modified since the lookup operation, and the entry
	 * entry has since been flushed from the cache, this operation will fail.
	 *
	 * @throws FileNotFoundException if the entry is no longer in the cache
	 */
	public InputStream open(DiskCacheEntry entry) throws FileNotFoundException {
		final String key = entry.getKey();
		final String fileName = entry.getFileName();

		// Has the entry been removed or replaced since the lookup was done?
		final Entry finalEntry = fEntryCache.get(key);
		if (finalEntry == null || !fileName.equals((String) finalEntry._fValue)) {
			throw new FileNotFoundException();
		}
		
		final File file = fFileSource.fileForName(fileName);
		
		class DiskCachePrivateFileInputStream extends FileInputStream {
			DiskCachePrivateFileInputStream(File file) throws FileNotFoundException {
				super(file);
			}
			public void close() throws IOException {
				super.close();
				// Ensure entry is still valid.  
				// It may have been removed or replaced in the interim.
				synchronized(fEntryCache) {
					if (fEntryCache.get(key) != finalEntry) {
						boolean success = deleteFile(fileName);
						if (!success) {
							rememberToDelete(fileName);
						}
					}
				}
				modified(); 
			}
			protected void finalize() throws IOException { 
				close();
				super.finalize();
			}
			/* For debugging/testing ONLY. Do not use outside of test suites.*/
			public String toString() {
				return file.toString();
			}
		};
		try {
			return new DiskCachePrivateFileInputStream(file);
		}
		catch (FileNotFoundException e) {
			// If there's an error while opening the file,
			// delete the entry and the file from the cache.
			remove(key);

			// Propagate the exception
			throw e;
		}
	}
	/**
	 * Read the cache state from persistent storage.
	 * Any undeleted entry files left at the last save the cach now tries to delete.
	 */
	public void read() throws IOException {
		File file = new File(fDirectory, CONTENTS_FILE_NAME);
		if (!file.exists()) {
			return;
		}
		DataInputStream in = 
			new DataInputStream(
				new BufferedInputStream(
					new FileInputStream(file)));
		try {
			String sig = in.readUTF();
			int spaceLimit = in.readInt(); /* Ignored -- use current limit */
			int spaceUsed = in.readInt();  /* Ignored -- updated as entries are read */
			int numEntries = in.readInt();
			if (!sig.equals(CONTENTS_VERSION)) {
				throw new IOException(Util.bind("file.badFormat")/*nonNLS*/);
			}

			/* Read to a temp. array of entries.  The entries are in most- to 
			 * least-recently used order. */
			Entry[] entries = new Entry[numEntries];
			for (int i = 0; i < numEntries; ++i) {
				entries[i] = readEntry(in);
			}

			createEntryCache();
			/* Add entries in least- to most-recently used order, so that the order in 
			 * the new cache is preserved. */
			for (int i = numEntries - 1; i >= 0; --i) {
				fEntryCache.add(entries[i]);
			}
			
			/* Read in names of files to be deleted. */
			/* But first, check that we are not at the end of the file (which occurs
			 * in pre-existing file formats). Try reading an int to find this out. */
			boolean moreToRead = false;
			int numFilesToBeDeleted = -1;
			try {
				numFilesToBeDeleted = in.readInt();
				moreToRead = true;
			} catch (IOException ignoreMe) {
				/* We're at the end of file, so it's a pre-existing format. */
			}
			if(moreToRead) {
				fFilesToBeDeleted = new Vector();
				for (int i = 0; i < numFilesToBeDeleted; i++) {
					String fileName = in.readUTF();
					fFilesToBeDeleted.addElement(fileName);
				}
			}
				
		}
		finally {
			in.close();
		}
	}
	/**
	 * Internal - Read an entry from a stream.  Returns an internal cache Entry.
	 */
	protected Entry readEntry(DataInputStream in) throws IOException {
		String key = in.readUTF();
		int size = in.readInt();
		String fileName = in.readUTF();
		long lastAccess = in.readLong();
		int flags = in.readInt();
		byte[] extraInfo = null;
		int extraLen = in.readInt();
		if (extraLen > 0) {
			extraInfo = new byte[extraLen];
			in.readFully(extraInfo);
		}
		return new Entry(key, size, fileName, lastAccess, extraInfo, flags);
	}
	/**
	 * Internal - Remember to delete a file later.
	 */
	protected void rememberToDelete(String fileName) {
		if (!fFilesToBeDeleted.contains(fileName))
			fFilesToBeDeleted.addElement(fileName);
	}
	/**
	 * Remove an entry from the cache and delete its contents.  
	 * Answer the entry if found, or null if not found.
	 */
	public DiskCacheEntry remove(String key) {
		Entry entry;
		synchronized(fEntryCache) {
			entry = fEntryCache.removeKey(key);
		}
		if (entry == null) {
			return null;
		}
		else {
			modified();
			return entry.asDiskCacheEntry();
		}
	}
public synchronized void removeAll(Vector keys) {
	Enumeration enum = keys.elements();
	while (enum.hasMoreElements()) {
		String key = (String) enum.nextElement();
		remove(key);
	}
}
	/**
	 * Renames an entry in the cache.
	 * Returns true if the rename succeeded, false otherwise.
	 */
	public boolean rename(String oldKey, String newKey) {
		long lastAccess = System.currentTimeMillis();
		synchronized(fEntryCache) {
			Entry oldEntry = (Entry) fEntryCache.get(oldKey);
			if (oldEntry == null) {
				return false;
			}
			else {
				Entry newEntry = new Entry(newKey, oldEntry._fSpace, (String) oldEntry._fValue, lastAccess, oldEntry.fExtraInfo, oldEntry.fFlags);
				fEntryCache.add(newEntry);
			}
		}
		modified();
		return true;
	}
	/**
	 * Save all in-memory cache state to persistent storage.
	 * Tries to deleted undeleted entry files.
	 */
	public synchronized void save() throws IOException {

		/* Try deleting "files to be deleted". */
		deleteFilesToBeDeleted();

		/*
		 * Write the table of contents to a temporary file,
		 * rather than the actual file, to be more resilient in
		 * the face of errors such as disk full. 
		 */
		File tempFile = new File(fDirectory, TEMP_CONTENTS_FILE_NAME);
		DataOutputStream out = 
			new DataOutputStream(
				new BufferedOutputStream(
					new FileOutputStream(tempFile)));
		boolean ok = false;
		try {
			synchronized(fEntryCache) {
				out.writeUTF(CONTENTS_VERSION);
				out.writeInt(getSpaceLimit());
				out.writeInt(getSpaceUsed());
				int numEntries = fEntryCache.getNumEntries();
				out.writeInt(numEntries);
				for (Enumeration e = fEntryCache.getEntries(); e.hasMoreElements();) {
					Entry entry = (Entry)e.nextElement();
					writeEntry(entry, out);
				}
				int numFilesToBeDeleted = fFilesToBeDeleted.size();
				out.writeInt(numFilesToBeDeleted);
				Enumeration e = fFilesToBeDeleted.elements();
				while(e.hasMoreElements()) {
					String fileName = (String) e.nextElement();
					out.writeUTF(fileName);
				}
			}
			out.close();

			/* Replace the old TOC (if any) with the temp file */
			File contentsFile = new File(fDirectory, CONTENTS_FILE_NAME);
			contentsFile.delete();
			if (tempFile.renameTo(contentsFile)) {
				ok = true;
				fIsDirty = false;
			}
		}
		finally {
			if (!ok) {
				out.close();
				tempFile.delete();
			}
		}
	}
	/**
	 * Sets the delay in milliseconds between periodic saves.
	 */
	public void setPeriodicSaveInterval(int interval) {
		fPeriodicSaveInterval = interval;
	}
	/**
	 * Sets the space limit of the cache, in Kilobytes.
	 */
	public void setSpaceLimit(int limit) {
		fSpaceLimit = limit;
		synchronized(fEntryCache) {
			fEntryCache.setSpaceLimit(limit * 1024);
		}
	}
	/**
	 * Internal -
	 * Create and start a thread which checks every so often whether 
	 * the cache needs to be saved.
	 * The thread should only run while there are modifications.
	 */
	protected void startPeriodicSaveThread() {

		fPeriodicSaveThread = new Thread() {
			public void run() {
				try {
					Thread.sleep(fPeriodicSaveInterval);
				}
				catch (InterruptedException e) {
				}
				for (;;) {
					synchronized(DiskCache.this) {
						if (fIsDirty) {
							try {
								save();  // clears dirty flag
							}
							catch (IOException e) {
								// Ignore
							}
						}
						else {
							// drop ref to thread and terminate
							fPeriodicSaveThread = null;  
							return;
						}
					}
					try {
						Thread.sleep(fPeriodicSaveInterval);
					}
					catch (InterruptedException e) {
					}
				}
			}				
		};
		fPeriodicSaveThread.setName("DiskCache periodic save");
		fPeriodicSaveThread.start();
					
	}
	/**
	 * Internal - Write an internal cache Entry to a stream.
	 */
	protected void writeEntry(Entry entry, DataOutputStream out) throws IOException {
		out.writeUTF((String)entry._fKey);
		out.writeInt(entry._fSpace);
		out.writeUTF((String)entry._fValue); // fileName
		out.writeLong(entry.fLastAccess);
		out.writeInt(entry.fFlags);
		byte[] extraInfo = entry.fExtraInfo;
		if (extraInfo == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(extraInfo.length);
			out.write(extraInfo);
		}
	}
}
