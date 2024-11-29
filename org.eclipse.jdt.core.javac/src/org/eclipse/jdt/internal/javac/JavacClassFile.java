/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.javac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

import com.sun.tools.javac.tree.JCTree.JCModuleDecl;

public class JavacClassFile extends ClassFile {
	private String fullName;
	private byte[] bytes = null;
	private final File proxyFile;
	private final IFile outputFile;

	public JavacClassFile(String qualifiedName, ClassFile enclosingClass, IContainer outputDir, java.nio.file.Path tempDir) {
		this.fullName = qualifiedName;
		this.isNestedType = enclosingClass != null;
		this.enclosingClassFile = enclosingClass;
		var relativePath = new Path(this.fullName.replace('.', File.separatorChar)).addFileExtension(SuffixConstants.EXTENSION_class);
		this.outputFile = outputDir.getFile(relativePath);
		this.proxyFile = tempDir.resolve(relativePath.toPath()).toFile();
	}

	public JavacClassFile(JCModuleDecl moduleDecl, IContainer outputDir, java.nio.file.Path tempDir) {
		// TODO: moduleDecl probably needs to be used, but how?
		this.fullName = "module-info";
		this.isNestedType = false;
		this.enclosingClassFile = null;
		var relativePath = new Path(this.fullName.replace('.', File.separatorChar)).addFileExtension(SuffixConstants.EXTENSION_class);
		this.outputFile = outputDir.getFile(relativePath);
		this.proxyFile = tempDir.resolve(relativePath.toPath()).toFile();
	}

	@Override
	public char[][] getCompoundName() {
		String[] names = this.fullName.split("\\.");
		char[][] compoundNames = new char[names.length][];
		for (int i = 0; i < names.length; i++) {
			compoundNames[i] = names[i].toCharArray();
		}
		return compoundNames;
	}

	@Override
	public char[] fileName() {
		String compoundName = this.fullName.replace('.', '/');
		return compoundName.toCharArray();
	}

	@Override
	public byte[] getBytes() {
		if (this.bytes == null) {
			if (!this.proxyFile.exists()) {
				this.bytes = new byte[0];
			} else {
				try {
					this.bytes = Files.readAllBytes(this.proxyFile.toPath());
				} catch (IOException e) {
					this.bytes = new byte[0];
				}
				this.proxyFile.delete();
			}
		}

		return this.bytes;
	}

	void deleteExpectedClassFile() {
		try {
			this.outputFile.delete(true, null);
		} catch (CoreException e) {
			// ignore
		}
	}



	public void flushTempToOutput() {
		try {
			createFolder(outputFile.getParent());
			outputFile.write(getBytes(), true, true, false, null);
			this.proxyFile.delete();
		} catch (CoreException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}

	private static IContainer createFolder(IContainer container) throws CoreException {
		if (container instanceof IFolder folder && !folder.exists()) {
			createFolder(folder.getParent());
			folder.create(IResource.FORCE | IResource.DERIVED, true, null);
		}
		return container;
	}


}
