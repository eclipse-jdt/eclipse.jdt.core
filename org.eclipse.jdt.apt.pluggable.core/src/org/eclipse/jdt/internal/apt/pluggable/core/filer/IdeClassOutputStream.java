/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation.
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

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * @see IdeClassOutputStream
 */
public class IdeClassOutputStream  extends ByteArrayOutputStream
{
	private final IdeProcessingEnvImpl _env;
	private final IFile _file;

	public IdeClassOutputStream(IdeProcessingEnvImpl env, IFile file) {
		_env = env;
		_file = file;
	}

	@Override
	public void close() throws IOException {
		super.close();
		byte[] byteArray = toByteArray();
		Compiler compiler = this._env.getCompiler();

		IBinaryType binaryType = null;
		try {
			FileSystemUtil.saveToDisk(_file, byteArray);
			binaryType = ClassFileReader.read(this._file.getLocation().toString());
			char[][] splitOn = CharOperation.splitOn('/', binaryType.getName());
			ReferenceBinding type = compiler.lookupEnvironment.getType(splitOn);
			if (type != null && type.isValidBinding()) {
				if (type.isBinaryBinding()) {
					_env.addNewClassFile(type);
				} else {
					BinaryTypeBinding binaryBinding = new BinaryTypeBinding(type.getPackage(), binaryType, compiler.lookupEnvironment, true);
					if (binaryBinding != null)
						_env.addNewClassFile(binaryBinding);
				}
			}
		} catch(Exception ex) {
			Apt6Plugin.log(ex, "Could not create generated class file " + _file.getName()); //$NON-NLS-1$
		}
	}
}
