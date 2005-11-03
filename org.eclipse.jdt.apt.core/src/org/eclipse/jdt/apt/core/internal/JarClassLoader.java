/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.apt.core.AptPlugin;

/**
 * This classloader allows us to close out underlying jars,
 * so that projects can be deleted even if they contain
 * factory jars that are in use.<P>
 * 
 * This classloader caches open jars while it is in use,
 * and once closed it will close those jars. It can still be used
 * after that point, but it will open and close on each classloader
 * operation.
 */
public class JarClassLoader extends ClassLoader {
	
	private final List<File> _files;
	private final ClassLoader _parent;
	
	// This can be nulled out periodically when the classloader is closed
	private List<JarFile> _jars;
	
	private int _openCounter = 0;
	
	public JarClassLoader(List<File> jarFiles, final ClassLoader parent) {
		super(parent);
		_files = jarFiles;
		_parent = parent;
		open();
	}
	
	public synchronized void open() {
		_openCounter++;
		if (_openCounter == 1) {
			// Create all jar files
			_jars = new ArrayList<JarFile>(_files.size());
			for (File f : _files) {
				try {
					JarFile jar = new JarFile(f);
					_jars.add(jar);
				}
				catch (IOException ioe) {
					AptPlugin.log(ioe, "Unable to create JarFile for file: " + f); //$NON-NLS-1$
				}
			}
		}
	}
	
	public synchronized void close() {
		if (_openCounter < 1) {
			throw new IllegalStateException("Attempt to close an already closed JarClassLoader"); //$NON-NLS-1$
		}
		_openCounter--;
		if (_openCounter == 0) {
			for (JarFile jar : _jars) {
				try {
					jar.close();
				}
				catch (IOException ioe) {
					AptPlugin.log(ioe, "Failed to close jar: " + jar); //$NON-NLS-1$
				}
			}
			_jars = null;
		}
	}
	
	@Override
	protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
		open();
		try {
			byte[] b = loadClassData(name);
			if (b == null)
				throw new ClassNotFoundException("Could not find class " + name); //$NON-NLS-1$
			return defineClass(name, b, 0, b.length);
		}
		finally {
			close();
		}
	}
	
	// returns null if no class found
	private byte[] loadClassData(String name) {
		name = name.replace('.','/');
		InputStream input = getResourceAsStream(name + ".class"); //$NON-NLS-1$
		if (input == null)
			return null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
	        int len;
	        while ((len = input.read(buf)) > 0) {
	            baos.write(buf, 0, len);
	        }
	        baos.close();
	        return baos.toByteArray();
		}
		catch (IOException ioe) {
			return null;
		}
		finally {
			try {input.close();} catch (IOException ioe) {}
		}		
	}
	
	@Override
	public synchronized InputStream getResourceAsStream(String name) {
		InputStream input = _parent.getResourceAsStream(name);
		if (input != null)
			return input;
		open();
		try {
			for (JarFile j : _jars) {
				try {
					ZipEntry entry = j.getEntry(name);
					if (entry != null) {
						InputStream zipInput = j.getInputStream(entry);
						return new JarCLInputStream(zipInput);
					}
				}
				catch (IOException ioe) {
					AptPlugin.log(ioe, "Unable to get entry from jar: " + j); //$NON-NLS-1$
				}
			}
			return null;
		}
		finally {
			close();
		}
	}
	
	/**
	 * This is difficult to implement and close out resources underneath.
	 * Delaying until someone actually requests this.
	 */
	@Override
	public URL getResource(String name) {
		throw new UnsupportedOperationException("getResource() not implemented"); //$NON-NLS-1$
	}

	/**
	 * This is difficult to implement and close out resources underneath.
	 * Delaying until someone actually requests this.
	 */
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		throw new UnsupportedOperationException("getResources() not implemented"); //$NON-NLS-1$
	}

	
	private class JarCLInputStream extends InputStream {
		
		private final InputStream _input;
		
		public JarCLInputStream(InputStream origInput) {
			_input = origInput;
			open();
		}

		@Override
		public void close() throws IOException {
			try {
				super.close();
				_input.close();
			}
			finally {
				JarClassLoader.this.close();
			}
		}

		@Override
		public int read() throws IOException {
			return _input.read();
		}

		@Override
		public int available() throws IOException {
			return _input.available();
		}

		@Override
		public synchronized void mark(int readlimit) {
			_input.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return _input.markSupported();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return _input.read(b, off, len);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return _input.read(b);
		}

		@Override
		public synchronized void reset() throws IOException {
			_input.reset();
		}

		@Override
		public long skip(long n) throws IOException {
			return _input.skip(n);
		}
	}
}
