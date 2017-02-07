package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.VarJarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.WkspJarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;

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
		return new TestSuite( FactoryPathTests.class );
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
		int index = 0;
		List<FactoryContainer> toIterate = new ArrayList<>();
		toIterate.addAll(path.getAllContainers().keySet());
		Collections.reverse(toIterate);
		for (FactoryContainer container : toIterate) {
			FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(new File(filesInPath[index++]));
			assertEquals("Unexpected jar file", fc, container);
		}
	}

	public void testGetEnabledContainersOrder() throws Exception {
		int index = 0;
		List<FactoryContainer> toIterate = new ArrayList<>();
		toIterate.addAll(path.getEnabledContainers().keySet());
		Collections.reverse(toIterate);
		for (FactoryContainer container : toIterate) {
			FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(new File(filesInPath[index++]));
			assertEquals("Unexpected jar file", fc, container);
		}
	}

	public void testRemoveExternalJar() throws Exception {
		File toRemove = new File(filesInPath[3]);
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(toRemove);

		assertTrue("Initial classpath should have contained jar", path.getAllContainers().containsKey(fc));

		path.removeExternalJar(toRemove);

		assertFalse("Final classpath should not have contained jar", path.getAllContainers().containsKey(fc));
	}

	public void testAddRemoveVarJar() throws Exception {
		IPath toAdd = new Path("/foo/bar/baz.jar");

		path.addVarJar(toAdd);

		// Will throw an exception if the newly added jar isn't first
		VarJarFactoryContainer fc = (VarJarFactoryContainer)path.getAllContainers().keySet().iterator().next();
		assertEquals(toAdd.toString(), fc.getId());

		path.removeVarJar(toAdd);

		assertFalse("Factory path should not contain the removed jar", path.getAllContainers().containsKey(fc));
	}

	public void testAddRemoveWorkspaceJar() throws Exception {
		IPath toAdd = new Path("/foo/bar/baz.jar");

		path.addWkspJar(toAdd);

		// Will throw an exception if the newly added jar isn't first
		WkspJarFactoryContainer fc = (WkspJarFactoryContainer)path.getAllContainers().keySet().iterator().next();
		assertTrue(fc.getJarFile().toString().endsWith(toAdd.toString()));

		path.removeWkspJar(toAdd);

		assertFalse("Factory path should not contain the removed jar", path.getAllContainers().containsKey(fc));
	}

	public void testSetContainers() throws Exception {
		FactoryPath newPath = new FactoryPath();
		newPath.setContainers(path.getAllContainers());
	
		List<FactoryContainer> toIterate = new ArrayList<>();
		toIterate.addAll(newPath.getAllContainers().keySet());
		Collections.reverse(toIterate);
		int index = 0;
		for (FactoryContainer container : toIterate) {
			FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(new File(filesInPath[index++]));
			assertEquals("Unexpected jar file", fc, container);
		}
	}
}
