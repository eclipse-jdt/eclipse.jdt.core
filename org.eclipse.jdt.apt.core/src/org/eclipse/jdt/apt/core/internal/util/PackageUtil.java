/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     mkaufman@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility class for dealing with packages, using
 * Eclipse's underlying SearchEngine
 */
public class PackageUtil {

	private PackageUtil() {}

	public static IPackageFragment[] getPackageFragments(
			final String packageName,
			final BaseProcessorEnv env) {

		List<IPackageFragment> packages = new ArrayList<>();
		try {
			// The environment caches our package fragment roots
			IPackageFragmentRoot[] roots = env.getAllPackageFragmentRoots();
			for (IPackageFragmentRoot root : roots) {
				IPackageFragment fragment = root.getPackageFragment(packageName);
				if (fragment != null && fragment.exists())
					packages.add(fragment);
			}
		}
		catch (JavaModelException e) {
			return new IPackageFragment[0];
		}

		return packages.toArray(new IPackageFragment[packages.size()]);
	}

}
