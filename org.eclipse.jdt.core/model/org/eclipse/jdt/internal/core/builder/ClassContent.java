/*******************************************************************************
 * Copyright (c) 2023 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.compiler.IClassContent;

class ClassContent implements IClassContent, IAdaptable{

	private String fileName;
	private IFile file;
	private SoftReference<byte[]> bytes;

	@Override
	public byte[] getBytes() throws CoreException {
		if (this.bytes == null) {
			return new byte[0];
		}
		byte[] bs = this.bytes.get();
		if (bs == null) {
			// cache was cleared out recover from underlying file...
			IFile f = getFile();
			if (f == null) {
				return new byte[0];
			}
			try {
				bs = f.getContents().readAllBytes();
			} catch (IOException e) {
				throw new CoreException(Status.error("reading bytes failed", e)); //$NON-NLS-1$
			}
			this.bytes = new SoftReference<byte[]>(bs);
		}
		return bs.clone();
	}

	@Override
	public void setBytes(byte[] classBytes) throws CoreException {
		InputStream input = new ByteArrayInputStream(classBytes);
		IFile f = getFile();
		if (f.exists()) {
			// Deal with shared output folders... last one wins... no collision cases detected
			if (JavaBuilder.DEBUG)
				System.out.println("Writing changed class file " + f.getName());//$NON-NLS-1$
			if (!f.isDerived())
				f.setDerived(true, null);
			f.setContents(input, true, false, null);
		} else {
			if (JavaBuilder.DEBUG)
				System.out.println("Writing new class file " + f.getName());//$NON-NLS-1$
			f.create(input, IResource.FORCE | IResource.DERIVED, null);
		}
		setBytesInternal(classBytes);
	}

	public IFile getFile() {
		return this.file;
	}

	@Override
	public String getFileName() {
		return this.fileName;
	}

	void setFile(IFile file) {
		this.file = file;
	}

	void setFileName(String fileName) {
		this.fileName = fileName;
	}

	void setBytesInternal(byte[] direct) {
		this.bytes = new SoftReference<byte[]>(direct);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isInstance(this.file)) {
			return adapter.cast(this.file);
		}
		return null;
	}

}
