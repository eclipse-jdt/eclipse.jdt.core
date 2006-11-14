/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import com.sun.mirror.apt.Filer;

/**
 * An implementation of com.sun.mirror.apt.Filer for use during reconcile phase.
 * This implementation is able to generate new Java source types (though it
 * does not lay them onto disk), but it silently ignores attempts to generate 
 * binary, text, or class files.
 */
final class ReconcileFilerImpl extends FilerImpl {
	
	private final ReconcileEnv _env;
	
	private static final OutputStream NO_OP_STREAM = new OutputStream(){
		public void write(int b) throws IOException {
			return;
		}
	};
	
	private static final class NoOpWriter extends Writer{
		public void write(char[] cbuf, int off, int len) 
			throws IOException {
			return;
		}
		public void flush() throws IOException {
			return;
		}		
		public void close() throws IOException {
			return;
		}
	}
	
	public ReconcileFilerImpl(ReconcileEnv env) {
		_env = env;
	}
	
	@Override
	protected AbstractCompilationEnv getEnv() {
		return _env;
	}

	private static final PrintWriter NO_OP_WRITER = new PrintWriter(new NoOpWriter());
	
	public OutputStream createBinaryFile(Filer.Location loc, String pkg, File relPath)
		throws IOException {
		return NO_OP_STREAM;
	}
	
	public OutputStream createClassFile(String name) throws IOException {
		return NO_OP_STREAM;
	}

	public PrintWriter createTextFile(Filer.Location loc, String pkg, File relPath, String charsetName) 
		throws IOException {
		return NO_OP_WRITER;
	}
}