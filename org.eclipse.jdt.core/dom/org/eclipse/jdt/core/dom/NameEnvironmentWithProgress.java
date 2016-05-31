/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 440687 - [compiler][batch][null] improve command line option for external annotations
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.ClasspathDirectory;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.INameEnvironmentWithProgress;
import org.eclipse.jdt.internal.core.NameLookup;

/**
 * Batch name environment that can be canceled using a monitor.
 * @since 3.6
 */
class NameEnvironmentWithProgress extends FileSystem implements INameEnvironmentWithProgress {
	IProgressMonitor monitor;
	
	public NameEnvironmentWithProgress(Classpath[] paths, String[] initialFileNames, IProgressMonitor monitor) {
		super(paths, initialFileNames, false);
		setMonitor(monitor);
	}
	private void checkCanceled() {
		if (this.monitor != null && this.monitor.isCanceled()) {
			if (NameLookup.VERBOSE) {
				System.out.println(Thread.currentThread() + " CANCELLING LOOKUP "); //$NON-NLS-1$
			}
			throw new AbortCompilation(true/*silent*/, new OperationCanceledException());
		}
	}
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] client) {
		return super.findType(typeName, packageName, client, true);
	}
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModule[] modules) {
		return findType(typeName, packageName, modules, true);
	}
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModule[] modules, boolean searchSecondaryTypes) {
		checkCanceled();
		NameEnvironmentAnswer answer = super.findType(typeName, packageName, modules);
		if (answer == null && searchSecondaryTypes) {
			NameEnvironmentAnswer suggestedAnswer = null;
			String qualifiedPackageName = new String(CharOperation.concatWith(packageName, '/'));
			String qualifiedTypeName = new String(CharOperation.concatWith(packageName, typeName, '/'));
			String qualifiedBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
			for (int i = 0, length = this.classpaths.length; i < length; i++) {
				if (!(this.classpaths[i] instanceof ClasspathDirectory)) continue;
				ClasspathDirectory classpathDirectory = (ClasspathDirectory) this.classpaths[i];
				for (IModule iModule : modules) {
					if (!classpathDirectory.servesModule(iModule)) continue;
					answer = classpathDirectory.findSecondaryInClass(typeName, qualifiedPackageName, qualifiedBinaryFileName);
					if (answer != null) {
						if (!answer.ignoreIfBetter()) {
							if (answer.isBetter(suggestedAnswer))
								return answer;
						} else if (answer.isBetter(suggestedAnswer))
							// remember suggestion and keep looking
							suggestedAnswer = answer;
					}
				}
			}
		}
		return answer;
	}

	public NameEnvironmentAnswer findType(char[][] compoundName, IModule[] modules) {
		checkCanceled();
		return super.findType(compoundName, modules);
	}
	public boolean isPackage(char[][] compoundName, char[] packageName, IModule[] modules) {
		checkCanceled();
		return super.isPackage(compoundName, packageName, modules);
	}
	
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
}
