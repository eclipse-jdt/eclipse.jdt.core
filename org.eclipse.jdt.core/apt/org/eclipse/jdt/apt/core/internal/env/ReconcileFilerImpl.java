/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		@Override
		public void write(int b) throws IOException {
			return;
		}
	};

	private static final class NoOpWriter extends Writer{
		@Override
		public void write(char[] cbuf, int off, int len)
			throws IOException {
			return;
		}
		@Override
		public void flush() throws IOException {
			return;
		}
		@Override
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

	@Override
	public OutputStream createBinaryFile(Filer.Location loc, String pkg, File relPath)
		throws IOException {
		return NO_OP_STREAM;
	}

	@Override
	public OutputStream createClassFile(String name) throws IOException {
		return NO_OP_STREAM;
	}

	@Override
	public PrintWriter createTextFile(Filer.Location loc, String pkg, File relPath, String charsetName)
		throws IOException {
		return NO_OP_WRITER;
	}
}