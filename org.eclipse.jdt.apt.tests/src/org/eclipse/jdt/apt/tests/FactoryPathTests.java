/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.VarJarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.WkspJarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FactoryPathTests extends TestCase {
	private FactoryPath path;

	private final String[] filesInPath = { "all/your.jar", "base.jar", "are.jar", "belong/to/us.jar",
			"you/have/no/chance.jar", "to.jar", "survive.jar" };

	public FactoryPathTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(FactoryPathTests.class);
	}

	private static void assertExtJars(List<String> expected, Map<FactoryContainer, Attributes> actual) {
		List<FactoryContainer> toIterate = new ArrayList<>();
		toIterate.addAll(actual.keySet());
		Collections.reverse(toIterate);
		assertEquals("The size of the path list should match up", expected.size(), toIterate.size());
		int index = 0;
		for (FactoryContainer container : toIterate) {
			FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(new File(expected.get(index++)));
			assertEquals("Unexpected jar file", fc, container);
		}
	}

	@Override
	protected void setUp() throws Exception {
		path = new FactoryPath();
		for (String next : filesInPath) {
			path.addExternalJar(new File(next));
		}
		super.setUp();
	}

	public void testGetAllContainers() throws Exception {
		assertExtJars(Arrays.asList(filesInPath), path.getAllContainers());
	}

	public void testGetEnabledContainersOrder() throws Exception {
		assertExtJars(Arrays.asList(filesInPath), path.getEnabledContainers());
	}

	public void testRemoveExternalJar() throws Exception {
		File toRemove = new File(filesInPath[3]);
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(toRemove);

		assertTrue("Initial classpath should have contained jar", path.getAllContainers().containsKey(fc));

		path.removeExternalJar(toRemove);

		assertFalse("Final classpath should not have contained jar", path.getAllContainers().containsKey(fc));
		final String[] expectedResult = { "all/your.jar", "base.jar", "are.jar", "you/have/no/chance.jar", "to.jar",
				"survive.jar" };
		assertExtJars(Arrays.asList(expectedResult), path.getAllContainers());
	}

	public void testAddRemoveVarJar() throws Exception {
		IPath toAdd = new Path("/foo/bar/baz.jar");

		path.addVarJar(toAdd);

		// Will throw an exception if the newly added jar isn't first
		VarJarFactoryContainer fc = (VarJarFactoryContainer) path.getAllContainers().keySet().iterator().next();
		assertEquals(toAdd.toString(), fc.getId());

		path.removeVarJar(toAdd);

		assertFalse("Factory path should not contain the removed jar", path.getAllContainers().containsKey(fc));
	}

	public void testAddRemoveWorkspaceJar() throws Exception {
		IPath toAdd = new Path("/foo/bar/baz.jar");

		path.addWkspJar(toAdd);

		// Will throw an exception if the newly added jar isn't first
		WkspJarFactoryContainer fc = (WkspJarFactoryContainer) path.getAllContainers().keySet().iterator().next();
		assertTrue(fc.getJarFile().toString().endsWith(toAdd.toOSString()));

		path.removeWkspJar(toAdd);

		assertFalse("Factory path should not contain the removed jar", path.getAllContainers().containsKey(fc));
	}

	public void testSetContainers() throws Exception {
		FactoryPath newPath = new FactoryPath();
		newPath.setContainers(path.getAllContainers());

		assertExtJars(Arrays.asList(filesInPath), newPath.getAllContainers());
	}
}
