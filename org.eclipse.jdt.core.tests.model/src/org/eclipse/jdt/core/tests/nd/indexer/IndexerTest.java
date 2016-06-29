/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd.indexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.indexer.ClassFileToIndexConverter;
import org.eclipse.jdt.internal.core.nd.indexer.IndexTester;
import org.eclipse.jdt.internal.core.nd.indexer.Indexer;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.model.BinaryTypeFactory;
import org.eclipse.jdt.internal.core.util.Util;

import junit.framework.Test;

/**
 * Tests for the {@link Database} class.
 */
public class IndexerTest extends AbstractJavaModelTests {

	public IndexerTest(String name) {
		super(name);
	}

	private static JavaIndex index;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (IndexerTest.index == null) {
			IndexerTest.index = createIndex(new NullProgressMonitor());
		}
	}

	public static Test suite() {
		return buildModelTestSuite(IndexerTest.class);
	}

	private JavaIndex createIndex(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		String testName = getName();
		JavaIndex localIndex = JavaIndexTestUtil.createTempIndex(testName);

		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_FULL"}, "bin", "1.8", true);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Indexer indexer = new Indexer(localIndex.getNd(), root);

		indexer.rescan(subMonitor.split(1));

		return localIndex;
	}

	public void testSubclassesOfGenericTypeCanBeFound() throws Exception {
		try (IReader reader = IndexerTest.index.getNd().acquireReadLock()) {
			NdTypeId javaUtilList = IndexerTest.index.findType("Ljava/util/List;".toCharArray());
			NdTypeId javaUtilArrayList = IndexerTest.index.findType("Ljava/util/ArrayList;".toCharArray());

			boolean found = false;
			List<NdType> subtypes = javaUtilList.getSubTypes();
			for (NdType next : subtypes) {
				if (Objects.equals(next.getTypeId(), javaUtilArrayList)) {
					found = true;
				}
			}

			assertTrue("ArrayList was found as a subtype of List", found);
		}
	}

	private void collectAllClassFiles(List<? super IClassFile> result, IParent nextRoot) throws CoreException {
		for (IJavaElement child : nextRoot.getChildren()) {
			int type = child.getElementType();

			if (type == IJavaElement.CLASS_FILE) {
				result.add((IClassFile)child);
			} else if (child instanceof IParent) {
				IParent parent = (IParent) child;

				collectAllClassFiles(result, parent);
			}
		}
	}

	public void testReadingAllClassesInIndexAreEquivalentToOriginalJarFiles() throws Exception {
		boolean foundAtLeastOneClass = false;
		SubMonitor subMonitor = SubMonitor.convert(null);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try (IReader reader = IndexerTest.index.getNd().acquireReadLock()) {
			IProject project = root.getProject("P");

			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
			subMonitor.setWorkRemaining(roots.length);
			for (IPackageFragmentRoot next : roots) {
				SubMonitor iterationMon = subMonitor.split(1);
				if (next.getKind() == IPackageFragmentRoot.K_BINARY) {
					List<IClassFile> result = new ArrayList<>();
					collectAllClassFiles(result, next);
					for (IClassFile nextClass : result) {
						IBinaryType originalBinaryType = ClassFileToIndexConverter.getTypeFromClassFile(nextClass, iterationMon);
						PackageFragment pkg = (PackageFragment) nextClass.getParent();
						String classFilePath = Util.concatWith(pkg.names, nextClass.getElementName(), '/');

						IBinaryType indexedBinaryType = BinaryTypeFactory.create(nextClass, JavaNames.classFilePathToBinaryName(classFilePath));
						IndexTester.testType(originalBinaryType, indexedBinaryType);
						foundAtLeastOneClass = true;
					}
				}
			}
		}
		assertTrue("No classes found in the index", foundAtLeastOneClass);
	}
}
