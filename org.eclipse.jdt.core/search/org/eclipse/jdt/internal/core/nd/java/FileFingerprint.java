package org.eclipse.jdt.internal.core.nd.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.internal.core.nd.StreamHasher;

/**
 * @since 3.12
 */
public class FileFingerprint {
	private long time;
	private long hash;
	private long size;

	private static final FileFingerprint EMPTY = new FileFingerprint(0,0,0);

	public static final FileFingerprint getEmpty() {
		return EMPTY;
	}

	public static final FileFingerprint create(IPath path, IProgressMonitor monitor) throws CoreException {
		return getEmpty().test(path, monitor).getNewFingerprint();
	}

	public FileFingerprint(long time, long size, long hash) {
		super();
		this.time = time;
		this.size = size;
		this.hash = hash;
	}

	public long getTime() {
		return this.time;
	}

	public long getHash() {
		return this.hash;
	}

	public long getSize() {
		return this.size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.hash ^ (this.hash >>> 32));
		result = prime * result + (int) (this.size ^ (this.size >>> 32));
		result = prime * result + (int) (this.time ^ (this.time >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileFingerprint other = (FileFingerprint) obj;
		if (this.hash != other.hash)
			return false;
		if (this.size != other.size)
			return false;
		if (this.time != other.time)
			return false;
		return true;
	}

	public static class FingerprintTestResult {
		private boolean matches;
		private boolean needsNewFingerprint;
		private FileFingerprint newFingerprint;

		public FingerprintTestResult(boolean matches, boolean needsNewFingerprint, FileFingerprint newFingerprint) {
			super();
			this.matches = matches;
			this.newFingerprint = newFingerprint;
			this.needsNewFingerprint = needsNewFingerprint;
		}

		public boolean needsNewFingerprint() {
			return this.needsNewFingerprint;
		}

		public boolean matches() {
			return this.matches;
		}

		public FileFingerprint getNewFingerprint() {
			return this.newFingerprint;
		}
	}

	/**
	 * Compares the given File with the receiver. If the fingerprint matches (ie: the file
	 */
	public FingerprintTestResult test(IPath path, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		IFileStore store = EFS.getLocalFileSystem().getStore(path);
		IFileInfo fileInfo = store.fetchInfo();

		long lastModified = fileInfo.getLastModified();
		subMonitor.split(5);

		long fileSize = fileInfo.getLength();
		subMonitor.split(5);
		if (lastModified == this.time && fileSize == this.size) {
			return new FingerprintTestResult(true, false, this);
		}

		long hashCode;
		try {
			hashCode = computeHashCode(path.toFile(), fileSize, subMonitor.split(90));
		} catch (IOException e) {
			throw new CoreException(Package.createStatus("An error occurred computing a hash code", e)); //$NON-NLS-1$
		}
		boolean matches = (hashCode == this.hash && fileSize == this.size);

		return new FingerprintTestResult(matches, true, new FileFingerprint(lastModified, fileSize, hashCode));
	}

	private long computeHashCode(File toTest, long fileSize, IProgressMonitor monitor) throws IOException {
		final int BUFFER_SIZE = 2048;
		char[] charBuffer = new char[BUFFER_SIZE];
		byte[] byteBuffer = new byte[BUFFER_SIZE * 2];

		SubMonitor subMonitor = SubMonitor.convert(monitor, (int) (fileSize / (BUFFER_SIZE * 2)));
		StreamHasher hasher = new StreamHasher();
		try {
			InputStream inputStream = new FileInputStream(toTest);
			try {
				while (true) {
					subMonitor.split(1);
					int bytesRead = readUntilBufferFull(inputStream, byteBuffer);

					if (bytesRead < byteBuffer.length) {
						charBuffer = new char[(bytesRead + 1) / 2];
						copyByteArrayToCharArray(charBuffer, byteBuffer, bytesRead);
						hasher.addChunk(charBuffer);
						break;
					}

					copyByteArrayToCharArray(charBuffer, byteBuffer, bytesRead);
					hasher.addChunk(charBuffer);
				}
			} finally {
				inputStream.close();
			}

		} catch (FileNotFoundException e) {
			return 0;
		}

		return hasher.computeHash();
	}

	private void copyByteArrayToCharArray(char[] charBuffer, byte[] byteBuffer, int bytesToCopy) {
		for (int ch = 0; ch < bytesToCopy / 2; ch++) {
			char next = (char) (byteBuffer[ch * 2] + byteBuffer[ch * 2 + 1]);
			charBuffer[ch] = next;
		}

		if (bytesToCopy % 2 != 0) {
			charBuffer[bytesToCopy / 2] = (char) byteBuffer[bytesToCopy - 1];
		}
	}

	int readUntilBufferFull(InputStream inputStream, byte[] buffer) throws IOException {
		int bytesRead = 0;
		while (bytesRead < buffer.length) {
			int thisRead = inputStream.read(buffer, bytesRead, buffer.length - bytesRead);

			if (thisRead == -1) {
				return bytesRead;
			}

			bytesRead += thisRead;
		}
		return bytesRead;
	}

	@Override
	public String toString() {
		return "FileFingerprint [time=" + this.time + ", size=" + this.size + ", hash=" + this.hash + "]";    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}
}