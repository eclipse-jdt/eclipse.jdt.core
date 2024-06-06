/*******************************************************************************
 * Copyright (c) 2018 Till Brychcy and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class TestCodeUtil {

	private TestCodeUtil() {
	}

	public static boolean isTestCode(ICompilationUnit cu) {
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) cu
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (packageFragmentRoot != null) {
			try {
				return packageFragmentRoot.getResolvedClasspathEntry().isTest();
			} catch (JavaModelException e) {
				// ignore
			}
		}
		return false;
	}
}
