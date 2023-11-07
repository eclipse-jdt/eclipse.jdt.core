/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.model.Semaphore.TimeOutException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.UserLibrary;
import org.eclipse.jdt.internal.core.UserLibraryClasspathContainer;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ClasspathInitializerTests extends ModifyingResourceTests {

public static class DefaultVariableInitializer implements VariablesInitializer.ITestInitializer {
	Map variableValues;

	/*
	 * values is [<var name>, <var value>]*
	 */
	public DefaultVariableInitializer(String[] values) {
		this.variableValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			this.variableValues.put(values[i], new Path(values[i+1]));
		}
	}

	@Override
	public void initialize(String variable) throws JavaModelException {
		if (this.variableValues == null) return;
		JavaCore.setClasspathVariable(
			variable,
			(IPath)this.variableValues.get(variable),
			null);
	}
}

// Simple container initializer, which keeps setting container to null
// (30920 - stackoverflow when setting container to null)
public static class NullContainerInitializer implements ContainerInitializer.ITestInitializer {
	public boolean hasRun = false;
	@Override
	public boolean allowFailureContainer() {
		return false; // allow the initializer to run again
	}
	@Override
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		this.hasRun = true;
		JavaCore.setClasspathContainer(
			containerPath,
			new IJavaProject[] {project},
			new IClasspathContainer[] { null },
			null);
	}
}

public ClasspathInitializerTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(ClasspathInitializerTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "testBug346002" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 2, 12 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 16, -1 };
}
@Override
protected void tearDown() throws Exception {
	// Cleanup caches
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	manager.containers = new HashMap(5);
	manager.variables = new HashMap(5);

	super.tearDown();
}

public void testContainerInitializer01() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testContainerInitializer02() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// simulate state on startup
		simulateExitRestart();

		startDeltas();
		p2.getResolvedClasspath(true);

		assertDeltas(
			"Unexpected delta on startup",
			""
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testContainerInitializer03() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// change value of TEST_CONTAINER
		createFile("/P1/lib2.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib2.jar"}));

		// simulate state on startup
		simulateExitRestart();

		startDeltas();
		getJavaProject("P2").getResolvedClasspath(true);

		assertDeltas(
			"Unexpected delta on startup",
			"P2[*]: {CHILDREN | RESOLVED CLASSPATH CHANGED}\n" +
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" +
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/* Ensure that initializer is not callled when resource tree is locked.
 * (regression test for bug 29585 Core Exception as resource tree is locked initializing classpath container)
 */
public void testContainerInitializer04() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(initializer);
		createJavaProject(
				"P2",
				new String[] {""},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// simulate state on startup
		simulateExitRestart();

		startDeltas();
		createFile("/P2/X.java", "public class X {}");

		assertEquals("Should not get exception", null, initializer.exception);

		assertDeltas(
			"Unexpected delta on startup",
			"P2[*]: {CHILDREN}\n" +
			"	<project root>[*]: {CHILDREN}\n" +
			"		<default>[*]: {CHILDREN}\n" +
			"			X.java[+]: {}"
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * 30920 - Stack overflow when container resolved to null
 */
public void testContainerInitializer05() throws CoreException {
	try {
		NullContainerInitializer nullInitializer = new NullContainerInitializer();
		ContainerInitializer.setInitializer(nullInitializer);
		createJavaProject(
				"P1",
				new String[] {""},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// simulate state on startup
		simulateExitRestart();

		startDeltas();

		// will trigger classpath resolution (with null container value)
		createFile("/P1/X.java", "public class X {}");
		assertDeltas(
			"Unexpected delta on startup",
			"P1[*]: {CHILDREN}\n" +
			"	<project root>[*]: {CHILDREN}\n" +
			"		<default>[*]: {CHILDREN}\n" +
			"			X.java[+]: {}"
		);
		assertTrue("initializer did not run", nullInitializer.hasRun);

		// next cp resolution request will rerun the initializer
		waitForAutoBuild();
		nullInitializer.hasRun = false; // reset
		getJavaProject("P1").getResolvedClasspath(true);
		assertTrue("initializer did not run", nullInitializer.hasRun); // initializer should have run again (since keep setting to null)

		// assigning new (non-null) value to container
		waitForAutoBuild();
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}));
		clearDeltas();
		getJavaProject("P1").getResolvedClasspath(true);
		assertDeltas(
			"Unexpected delta after setting container",
			"P1[*]: {CHILDREN | RESOLVED CLASSPATH CHANGED}\n" +
			"	lib.jar[*]: {ADDED TO CLASSPATH}"
		);

	} catch (StackOverflowError e) {
		e.printStackTrace();
		assertTrue("stack overflow assigning container", false);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensures that running the initializer during a reconcile operation just after workspace startup
 * doesn't throw a NPE
 * (regression test for bug 48818 NPE in delta processor)
  */
public void testContainerInitializer06() throws CoreException {
    ICompilationUnit workingCopy = null;
	try {
		createProject("P1");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", ""}));
		createJavaProject(
				"P2",
				new String[] {"src"},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"bin");
		createFile(
			"/P2/src/X,java",
			"public class X {\n" +
			"}"
		);

		// change value of TEST_CONTAINER
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));

		// simulate state on startup
		simulateExitRestart();

		startDeltas();
		workingCopy = getCompilationUnit("/P2/src/X.java");
		workingCopy.becomeWorkingCopy(null);

		assertWorkingCopyDeltas(
			"Unexpected delta on startup",
			"P2[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		<default>[*]: {CHILDREN}\n" +
			"			[Working copy] X.java[+]: {PRIMARY WORKING COPY}"
		);
	} finally {
		stopDeltas();
		if (workingCopy != null) workingCopy.discardWorkingCopy();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensure that an OperationCanceledException goes through
 * (regression test for bug 59363 Should surface cancellation exceptions)
 */
public void testContainerInitializer07() throws CoreException {
	try {
		boolean gotException = false;
		try {
			ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}) {
				@Override
				public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
					throw new OperationCanceledException("test");
				}});
			IJavaProject p1 = createJavaProject(
					"P1",
					new String[] {},
					new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
					"");
			p1.getResolvedClasspath(true);
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException", gotException);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensure that the stack doesn't blow up if initializer is missbehaving
 * (regression test for bug 61052 Flatten cp container initialization)
 */
public void testContainerInitializer08() throws CoreException {
	final int projectLength = 10;
	final String[] projects = new String[projectLength];
	for (int i = 0; i < projectLength; i++) {
		projects[i] = "P" + i;
	}
	try {
		String[] projectRefs = new String[(projectLength-1) * 2];
		for (int i = 0; i < projectLength-1; i++) {
			projectRefs[i*2] = "P" + i;
			projectRefs[(i*2)+1] = "/P" + i + "/test.jar";
		}
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(projectRefs) {
			void foo(int n) {
				if (n > 0) {
					foo(n-1);
					return;
				}
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				for (int i = 0; i < projectLength-1; i++) {
					try {
						JavaCore.create(root.getProject(projects[i])).getResolvedClasspath(true);
					} catch (JavaModelException e) {
						// project doesn't exist: ignore
					}
				}
			}
			@Override
			public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
				foo(500);
				super.initialize(containerPath, project);
			}
		});
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < projectLength; i++) {
					createProject(projects[i]);
					editFile(
						"/" + projects[i] + "/.project",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<projectDescription>\n" +
						"	<name>" + projects[i] + "</name>\n" +
						"	<comment></comment>\n" +
						"	<projects>\n" +
						(i == 0 ? "" : "<project>" + projects[i-1] + "</project>\n") +
						"	</projects>\n" +
						"	<buildSpec>\n" +
						"		<buildCommand>\n" +
						"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
						"			<arguments>\n" +
						"			</arguments>\n" +
						"		</buildCommand>\n" +
						"	</buildSpec>\n" +
						"	<natures>\n" +
						"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +
						"	</natures>\n" +
						"</projectDescription>"
					);
					createFile(
						"/" + projects[i] + "/.classpath",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<classpath>\n" +
						(i == 0 ? "" : "<classpathentry kind=\"src\" path=\"/" + projects[i-1] + "\"/>\n") +
						"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
						"	<classpathentry kind=\"output\" path=\"\"/>\n" +
						"</classpath>"
					);
				}
			}
		}, null);
		getJavaProject("P0").getResolvedClasspath(true);
	} finally {
		stopDeltas();
		deleteProjects(projects);
	}
}
/*
 * Ensure that a StackOverFlowError is not thrown if the initializer asks for the resolved classpath
 * that is being resolved.
 * (regression test for bug 61040 Should add protect for reentrance to #getResolvedClasspath)
 */
public void testContainerInitializer09() throws CoreException {
	try {
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}) {
			@Override
			protected DefaultContainer newContainer(char[][] libPaths) {
				return new DefaultContainer(libPaths) {
					@Override
					public IClasspathEntry[] getClasspathEntries() {
						try {
							getJavaProject("P1").getResolvedClasspath(true);
						} catch (JavaModelException e) {
							// project doesn't exist: ignore
						}
						return super.getClasspathEntries();
					}
				};
			}
		};
		ContainerInitializer.setInitializer(initializer);
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createProject("P1");
				editFile(
					"/P1/.project",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<projectDescription>\n" +
					"	<name>P1</name>\n" +
					"	<comment></comment>\n" +
					"	<projects>\n" +
					"	</projects>\n" +
					"	<buildSpec>\n" +
					"		<buildCommand>\n" +
					"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
					"			<arguments>\n" +
					"			</arguments>\n" +
					"		</buildCommand>\n" +
					"	</buildSpec>\n" +
					"	<natures>\n" +
					"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +
					"	</natures>\n" +
					"</projectDescription>"
				);
				createFile(
					"/P1/.classpath",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<classpath>\n" +
					"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
					"	<classpathentry kind=\"output\" path=\"\"/>\n" +
					"</classpath>"
				);
			}
		}, null);
		getJavaProject("P1").getResolvedClasspath(true);
	} finally {
		stopDeltas();
		ContainerInitializer.setInitializer(null);
		deleteProject("P1");
	}
}
/*
 * Ensure that creating a Java project initializes a container and refreshes the external jar at the same time
 * without throwing a ConcurrentModificationException
 * (regression test for bug 63534 ConcurrentModificationException after "catching up")
 */
public void testContainerInitializer10() throws CoreException {
	class LogListener implements ILogListener {
    	IStatus loggedStatus;
        public void logging(IStatus status, String plugin) {
            this.loggedStatus = status;
        }
	}
	LogListener listener = new LogListener();
	try {
		Platform.addLogListener(listener);
		final IJavaProject p1 = createJavaProject("P1");
		final IJavaProject p2 = createJavaProject("P2");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P3", "/P1"}) {
			@Override
	        public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
	            super.initialize(containerPath, project);
	            getJavaModel().refreshExternalArchives(new IJavaElement[] {p1}, null);
	        }
		});
		getWorkspace().run(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                p2.setRawClasspath(new IClasspathEntry[] {JavaCore.newSourceEntry(new Path("/P2/src"))}, new Path("/P2/bin"), null);
				createProject("P3");
                editFile(
                	"/P3/.project",
                	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                	"<projectDescription>\n" +
                	"	<name>P3</name>\n" +
                	"	<comment></comment>\n" +
                	"	<projects>\n" +
                	"	</projects>\n" +
                	"	<buildSpec>\n" +
                	"		<buildCommand>\n" +
                	"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
                	"			<arguments>\n" +
                	"			</arguments>\n" +
                	"		</buildCommand>\n" +
                	"	</buildSpec>\n" +
                	"	<natures>\n" +
                	"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +
                	"	</natures>\n" +
                	"</projectDescription>\n"
                );
                createFile(
                	"/P3/.classpath",
                	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                	"<classpath>\n" +
                	"	<classpathentry kind=\"src\" path=\"\"/>\n" +
                	"	<classpathentry kind=\"var\" path=\"JCL_LIB\"/>\n" +
                	"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
                	"	<classpathentry kind=\"output\" path=\"\"/>\n" +
                	"</classpath>"
                );
             }
        }, null);

		assertEquals("Should not get any exception in log", null, listener.loggedStatus);
	} finally {
	    Platform.removeLogListener(listener);
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}
/*
 * Ensure that a classpath initializer is not run on shutdown
 * (regression test for bug 93941 Classpath initialization on shutdown)
 */
public void testContainerInitializer11() throws CoreException {
	boolean hasExited = false;
	try {
		ContainerInitializer.setInitializer(null);
		createJavaProject(
			"P",
			new String[] {},
			new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
			"");
		simulateExitRestart();
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {}) {
			@Override
			public void initialize(IPath containerPath,IJavaProject project) throws CoreException {
				assertTrue("Should not initialize container on shutdown", false);
			}
		};
		ContainerInitializer.setInitializer(initializer);
		simulateExit();
		hasExited = true;
	} finally {
		ContainerInitializer.setInitializer(null);
		if (hasExited)
			simulateRestart();
		deleteProject("P");
	}
}

/*
 * Ensure that the initializer is removed from the cache when the project is deleted
 * (regression test for bug 116072 cached classpath containers not removed when project deleted)
 */
public void testContainerInitializer12() throws CoreException {
	try {
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}));
		IJavaProject project =  createJavaProject(
			"P1",
			new String[] {},
			new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
			"");
		createFile("/P1/lib.jar", "");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
		deleteProject("P1");

		class Initializer extends DefaultContainerInitializer {
			boolean initialized;
			public Initializer(String[] args) {
				super(args);
			}
			@Override
			public void initialize(IPath containerPath, IJavaProject p) throws CoreException {
				super.initialize(containerPath, p);
				this.initialized = true;
			}
		}
		Initializer initializer = new Initializer(new String[] {"P1", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(initializer);
		createJavaProject(
			"P1",
			new String[] {},
			new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
			"");
		createFile("/P1/lib.jar", "");
		assertTrue("/P1/lib.jar should exist", root.exists());
		assertTrue("Should have been initialized", initializer.initialized);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}

/*
 * Ensures that no resource deta is reported if a container that was not initialized is initialized with null
 * (regression test for bug 149043 Unresolvable classpath container leads to lots of scheduled jobs)
 */
public void testContainerInitializer13() throws CoreException {
	IResourceChangeListener listener = new IResourceChangeListener() {
		StringBuffer buffer = new StringBuffer();
		public void resourceChanged(IResourceChangeEvent event) {
			this.buffer.append(event.getDelta().findMember(new Path("/P1")));
		}
		public String toString() {
			return this.buffer.toString();
		}
	};
	try {
		NullContainerInitializer nullInitializer = new NullContainerInitializer();
		ContainerInitializer.setInitializer(nullInitializer);
		IJavaProject project = createJavaProject(
				"P1",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// simulate state on startup
		simulateExitRestart();

		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);

		// force resolution of container
		project.findPackageFragmentRoots(project.getRawClasspath()[0]);

		assertEquals(
			"Unexpected resource delta on startup",
			"",
			listener.toString()
		);
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
		deleteProject("P1");
	}
}

/*
 * Ensures that a misbehaving container (that initializes another project than the one asked for) doesn't cause
 * the container to be initialized again
 * (regression test for bug 160005 Add protection about misbehaving container initializer)
 */
public void testContainerInitializer14() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		class Container extends DefaultContainerInitializer {
			int initializeCount = 0;
			Container(String[] values) {
				super(values);
			}
			@Override
			public void initialize(IPath containerPath, IJavaProject project) 	throws CoreException {
				this.initializeCount++;
				super.initialize(containerPath, getJavaProject("P1"));
			}
		}
		Container container = new Container(new String[] {"P2", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(container);
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		p2.getResolvedClasspath(true);
		assertEquals("Unexpected number of initalizations", 1, container.initializeCount);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that if a container is misbehaving (it doesn't initialize a project when asked for),
 * then the resulting container's classpath is not null
 * (regression test for bug 161846 Expanding a java project with invalid classpath container entries in Project Explorer causes CPU to stay at 100%)
 */
public void testContainerInitializer15() throws CoreException {
	try {
		class Container extends DefaultContainerInitializer {
			Container(String[] values) {
				super(values);
			}
			@Override
			public void initialize(IPath containerPath, IJavaProject project) 	throws CoreException {
			}
		}
		Container container = new Container(new String[] {"P1", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(container);
		IJavaProject p1 = createJavaProject(
				"P1",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		IClasspathContainer classpathContainer = JavaCore.getClasspathContainer(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"), p1);
		assertClasspathEquals(classpathContainer.getClasspathEntries(), "");
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensure that an initializer cannot return a project entry that points to the project of the container (cycle).
 */
public void testContainerInitializer16() throws CoreException {
	try {
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1"}));
		JavaModelException exception = null;
		try {
			IJavaProject p1 = createJavaProject(
				"P1",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
			p1.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			exception = e;
		}
		assertExceptionEquals(
			"Unexpected expection",
			"Project 'P1' cannot reference itself",
			exception);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}

/*
 * Ensures that no resource deta is reported if a container is initialized right after startup  to the same value it had before shutdown.
 * (regression test for bug 175849 Project is touched on restart)
 */
public void testContainerInitializer17() throws CoreException {
	IResourceChangeListener listener = new IResourceChangeListener() {
		StringBuffer buffer = new StringBuffer();
		public void resourceChanged(IResourceChangeEvent event) {
			this.buffer.append(event.getDelta().findMember(new Path("/P2")));
		}
		public String toString() {
			return this.buffer.toString();
		}
	};
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar", "P3", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		createJavaProject(
				"P3",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// simulate state on startup
		simulateExitRestart();

		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);

		// initialize to the same value
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar", "P3", "/P1/lib.jar"}) {
			@Override
	        public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
	        	// simulate concurrency (another thread is initializing all containers in parallel and thus this flag is set to true)
	        	JavaModelManager.getJavaModelManager().batchContainerInitializations = JavaModelManager.NEED_BATCH_INITIALIZATION;
	            super.initialize(containerPath, project);
	        }
		});
		p2.getResolvedClasspath(true);

		assertEquals(
			"Unexpected resource delta on container initialization",
			"",
			listener.toString()
		);
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}
/*
 * Ensures that an unbound container marker is created if container is reset to null
 * (regression test for 182204 Deleting a JRE referenced by container does not result in unbound container problem)
 */
public void testContainerInitializer18() throws CoreException {
	try {
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}));
		IJavaProject p1 = createJavaProject(
				"P1",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		createFile("/P1/lib.jar", "");
		p1.getResolvedClasspath(true);
		waitForAutoBuild();

		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[0]));
		JavaCore.setClasspathContainer(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"), new IJavaProject[] {p1}, new IClasspathContainer[] {null}, null);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Unbound classpath container: \'org.eclipse.jdt.core.tests.model.TEST_CONTAINER\' in project \'P1\'",
			p1);
	} finally {
		deleteProject("P1");
	}
}

/*
 * Ensures that a container is not kept in the cache if no longer referenced on the classpath
 * (regression test for 139446 [build path] bug in the Edit Library dialog box, when changing the default JRE, and switching from alternate JRE to workspace default)
 */
public void testContainerInitializer19() throws CoreException {
	try {
		// setup
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib1.jar"}));
		IJavaProject p1 = createJavaProject(
				"P1",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		createFile("/P1/lib1.jar", "");
		createFile("/P1/lib2.jar", "");
		p1.getResolvedClasspath(true);
		IClasspathEntry[] initialClasspath = p1.getRawClasspath();

		// remove reference to container, change initializer, and add reference to container back
		p1.setRawClasspath(new IClasspathEntry[0], null);
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib2.jar"}));
		p1.setRawClasspath(initialClasspath, null);

		assertClasspathEquals(
			p1.getResolvedClasspath(true),
			"/P1/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:false]"
		);
	} finally {
		deleteProject("P1");
	}
}

/*
 * Ensures that container a container is not kept in the cache if no longer referenced on the classpath
 * (regression test for 136382 [classpath] Discard container if not referenced on classpath)
 */
public void testContainerInitializer20() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P");
		final StringBuilder paths = new StringBuilder();
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {"P", "/P/lib.jar"}) {
			@Override
			public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
				paths.append(containerPath);
				paths.append('\n');
				super.initialize(containerPath, project);
			}
		};
		ContainerInitializer.setInitializer(initializer);

		setClasspath(p, new IClasspathEntry[] {JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER/JRE1"))});
		setClasspath(p, new IClasspathEntry[] {JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER/JRE2"))});
		setClasspath(p, new IClasspathEntry[] {JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER/JRE1"))});
		assertStringEquals(
			"org.eclipse.jdt.core.tests.model.TEST_CONTAINER/JRE1\n" +
			"org.eclipse.jdt.core.tests.model.TEST_CONTAINER/JRE2\n" +
			"org.eclipse.jdt.core.tests.model.TEST_CONTAINER/JRE1\n",
			paths.toString(),
			false);
	} finally {
		stopDeltas();
		deleteProject("P");
	}
}

public void testContainerInitializer21() throws CoreException {
	try {
		createProject("P1");
		createExternalFolder("externalLib");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", getExternalResourcePath("externalLib")}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getExternalResourcePath("externalLib"));
		assertTrue(getExternalResourcePath("externalLib") + " should exist", root.exists());
	} finally {
		stopDeltas();
		deleteExternalResource("externalLib");
		deleteProject("P1");
		deleteProject("P2");
	}
}

public void testContainerInitializer22() throws CoreException {
	try {
		createProject("P1");
		createExternalFile("externalLib.abc", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", getExternalResourcePath("externalLib.abc")}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getExternalResourcePath("externalLib.abc"));
		assertTrue(getExternalResourcePath("externalLib.abc") + " should exist", root.exists());
	} finally {
		stopDeltas();
		deleteExternalResource("externalLib.abc");
		deleteProject("P1");
		deleteProject("P2");
	}
}

public void testContainerInitializer23() throws CoreException {
	try {
		createProject("P1");
		IFile lib = createFile("/P1/internalLib.abc", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/internalLib.abc"}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(lib);
		assertTrue("/P1/internalLib.abc should exist", root.exists());
	} finally {
		stopDeltas();
		deleteExternalResource("externalLib.abc");
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that the value of a resolved classpath is correct if another thread is resolving the classpath concurrently
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=232478 )
 */
public void testContainerInitializer24() throws Exception {
	BPThread.TIMEOUT = 30000; // wait 30s max
	BPThread thread = new BPThread("getResolvedClasspath()");
	ClasspathResolutionBreakpointListener listener = new ClasspathResolutionBreakpointListener(new BPThread[] {thread});
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		final JavaProject project2 = (JavaProject) createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		simulateExit();
		try {
			deleteResource(JavaCore.getPlugin().getStateLocation().append("variablesAndContainers.dat").toFile());
		} finally {
			simulateRestart();
		}

		JavaProject.addCPResolutionBPListener(listener);
		thread.start(new Runnable() {
			@Override
			public void run() {
				try {
					project2.getResolvedClasspath();
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		});
		thread.runToBP(1);
		thread.runToBP(2);
		thread.runToBP(3);

		IClasspathEntry[] classpath = project2.getResolvedClasspath();
		assertClasspathEquals(
			classpath,
			"/P1/lib.jar[CPE_LIBRARY][K_BINARY][isExported:false]"
		);

	} finally {
		JavaProject.removeCPResolutionBPListener(listener);
		thread.runToEnd();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Ensures that the project references are updated on startup if the initializer gives a different value.
 */
public void testContainerInitializer25() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));

		// simulate state on startup
		simulateExitRestart();

		p2.getResolvedClasspath(true);
		assertResourcesEqual(
			"Unexpected project references on startup",
			"/P1",
			p2.getProject().getReferencedProjects());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}


/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=327471
 * [java.io.EOFException at java.io.DataInputStream.readInt(Unknown Source)]
 * This test ensures that there is no exception on a restart of eclipse after
 * the project is closed after the workspace save
 */
public void testContainerInitializer26() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));

		waitForAutoBuild();
		getWorkspace().save(true/*full save*/, null/*no progress*/);
		p2.getProject().close(null); // close the project after the save and before the shutdown
		JavaModelManager.getJavaModelManager().shutdown();

		startLogListening();
		simulateRestart();
		assertLogEquals(""); // no error should be logged

	} finally {
		stopLogListening();
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testVariableInitializer01() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer02() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaProject p2 = createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertEquals("Unexpected source attachment path", "/P1/src.zip", root.getSourceAttachmentPath().toString());
		assertEquals("Unexpected source attachment root path", "src", root.getSourceAttachmentRootPath().toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer03() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		String[] variableValues = new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		};
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(variableValues));
		createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");

		// simulate state on startup
		simulateExitRestart();

		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;
		getJavaProject("P2").getResolvedClasspath(true);

		assertDeltas(
			"Unexpected delta on startup",
			""
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer04() throws CoreException {
	try {
		final StringBuilder buffer = new StringBuilder();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			@Override
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer05() throws CoreException {
	try {
		final StringBuilder buffer = new StringBuilder();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			@Override
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				JavaCore.getClasspathVariable("TEST_SRC");
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Initializing TEST_SRC\n" +
			"Setting variable TEST_SRC to test_src\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that if the initializer doesn't initialize a variable, it can be
 * initialized later on.
 */
public void testVariableInitializer06() throws CoreException {
	try {
		final StringBuilder buffer = new StringBuilder();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			@Override
			public void initialize(String variable) {
				// do nothing
				buffer.append("Ignoring request to initialize");
			}
		});
		IPath path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after initializer was called",
			null,
			path);
		IPath varValue = new Path("src.zip");
		JavaCore.setClasspathVariable("TEST_SRC", varValue, null);
		path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after setting it",
			varValue,
			path);
	} finally {
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer07() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		String[] variableValues = new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		};
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(variableValues));
		createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");

		// change value of TEST_LIB
		createFile("/P1/lib2.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib2.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));

		// simulate state on startup
		simulateExitRestart();

		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;
		getJavaProject("P2").getResolvedClasspath(true);

		assertDeltas(
			"Unexpected delta on startup",
			"P2[*]: {CHILDREN | RESOLVED CLASSPATH CHANGED}\n" +
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" +
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
/*
 * Ensure that an OperationCanceledException goes through
 * (regression test for bug 59363 Should surface cancellation exceptions)
 */

public void testVariableInitializer08() throws CoreException {
	try {
		boolean gotException = false;
		try {
			VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}) {
				@Override
				public void initialize(String variable) throws JavaModelException {
					throw new OperationCanceledException("test");
				}
			});
			IJavaProject p1 = createJavaProject("P1", new String[] {}, new String[] {"TEST_LIB"}, "");
			p1.getResolvedClasspath(true);
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException", gotException);
	} finally {
		deleteProject("P1");
		VariablesInitializer.reset();
	}
}

/*
 * Ensure that removing a classpath variable while initializing it doesn't throw a StackOverFlowError
 * (regression test for bug 112609 StackOverflow when initializing Java Core)
 */
public void testVariableInitializer09() throws CoreException {
	try {
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}) {
			@Override
			public void initialize(String variable) throws JavaModelException {
				JavaCore.removeClasspathVariable("TEST_LIB", null);
			}
		});
		IJavaProject p1 = createJavaProject("P1", new String[] {}, new String[] {"TEST_LIB"}, "");
		IClasspathEntry[] resolvedClasspath = p1.getResolvedClasspath(true);
		assertClasspathEquals(
			resolvedClasspath,
			""
		);
	} finally {
		deleteProject("P1");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that not initializing a classpath variable and asking for its value returns null
 * (regression test for bug 113110 TestFailures in DebugSuite)
 */
public void testVariableInitializer10() throws CoreException {
	try {
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}) {
			@Override
			public void initialize(String variable) throws JavaModelException {
				// don't initialize
			}
		});
		// force resolution
		JavaCore.getClasspathVariable("TEST_LIB");
		// second call should still be null
		assertEquals("TEST_LIB should be null", null, JavaCore.getClasspathVariable("TEST_LIB"));
	} finally {
		deleteProject("P1");
		VariablesInitializer.reset();
	}
}
/**
 * Bug 125965: [prefs] "Export/Import preferences" should let user to choose wich preference to export/import
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125965"
 */
public void testVariableInitializer11() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_LIB";
		String initialValue = "/P1/lib.jar";
		String newValue = "/tmp/file.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {varName, initialValue}));
		assertEquals("JavaCore classpath value should have been initialized", JavaCore.getClasspathVariable(varName).toString(), initialValue);

		// Modify preference
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IEclipsePreferences preferences = manager.getInstancePreferences();
		preferences.put(JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX+varName, newValue);

		// verify that JavaCore preferences have been reset
		assertEquals("JavaCore classpath value should be unchanged", JavaCore.getClasspathVariable(varName).toString(), initialValue);
		assertEquals("JavaCore preferences value should be unchanged", preferences.get(varName, "X"), initialValue);
	} finally {
		VariablesInitializer.reset();
	}
}

/**
 * @bug 138599: [model][classpath] Need a way to mark a classpath variable as deprecated
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=138599"
 */
public void testVariableInitializerDeprecated() throws CoreException, IOException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED";
		String filePath = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {varName, filePath}));
		assertEquals("JavaCore classpath value should have been initialized", JavaCore.getClasspathVariable(varName).toString(), filePath);

		// Verify that Classpath Variable is deprecated
		assertEquals("JavaCore classpath variable should be deprecated", "Test deprecated flag", JavaCore.getClasspathVariableDeprecationMessage(varName));

		// Create project
		IJavaProject project = createJavaProject("P1");
		addLibrary(project, "lib.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					JavaCore.VERSION_1_4);
		IClasspathEntry variable = JavaCore.newVariableEntry(new Path("TEST_DEPRECATED"), null, null);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(project, variable, false);
		assertStatus("Classpath variable 'TEST_DEPRECATED' in project 'P1' is deprecated: Test deprecated flag", status);
		assertFalse("Status should not be OK", status.isOK());
		assertEquals("Status should have WARNING severity", IStatus.WARNING, status.getSeverity());
		assertEquals("Status should have deprecated code", IJavaModelStatusConstants.DEPRECATED_VARIABLE, status.getCode());
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}
public void testVariableInitializerUnboundAndDeprecated() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED";
		String filePath = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {varName, filePath}));
		assertEquals("JavaCore classpath value should have been initialized", JavaCore.getClasspathVariable(varName).toString(), filePath);

		// Verify that Classpath Variable is deprecated
		assertEquals("JavaCore classpath variable should be deprecated", "Test deprecated flag", JavaCore.getClasspathVariableDeprecationMessage(varName));

		// Create project
		IJavaProject project = createJavaProject("P1");
		IClasspathEntry variable = JavaCore.newVariableEntry(new Path("TEST_DEPRECATED"), null, null);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(project, variable, false);
		assertStatus("Project 'P1' is missing required library: 'lib.jar'", status);
		assertFalse("Status should not be OK", status.isOK());
		assertEquals("Status should have WARNING severity", IStatus.ERROR, status.getSeverity());
		assertEquals("Status should have deprecated code", IJavaModelStatusConstants.INVALID_CLASSPATH, status.getCode());
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}

/**
 * @bug 156226: [model][classpath] Allow classpath variable to be marked as non modifiable
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156226"
 */
public void testVariableInitializerReadOnly() throws CoreException, IOException {
	try {
		// Create initializer
		String varName = "TEST_READ_ONLY";
		String path = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] { varName, path }));
		assertEquals("JavaCore classpath value should have been initialized", JavaCore.getClasspathVariable(varName).toString(), path);

		// verify that Classpath Variable is read-only
		assertTrue("JavaCore classpath variable should be read-only", JavaCore.isClasspathVariableReadOnly(varName));

		// Create project
		IJavaProject project = createJavaProject("P1");
		addLibrary(project, "lib.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					JavaCore.VERSION_1_4);
		IClasspathEntry variable = JavaCore.newVariableEntry(new Path("TEST_READ_ONLY"), null, null);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(project, variable, false);
		assertStatus("OK", status);
		assertTrue("Status should be OK", status.isOK());
		assertEquals("Status should be VERIFIED_OK", JavaModelStatus.VERIFIED_OK, status);
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}
public void testVariableInitializerDeprecatedAndReadOnly() throws CoreException, IOException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED_READ_ONLY";
		String path = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] { varName, path }));
		assertEquals("JavaCore classpath value should have been initialized", JavaCore.getClasspathVariable(varName).toString(), path);

		// verify that Classpath Variable is read-only
		assertEquals("JavaCore classpath variable should be deprecated", "A deprecated and read-only initializer", JavaCore.getClasspathVariableDeprecationMessage(varName));
		assertTrue("JavaCore classpath variable should be read-only", JavaCore.isClasspathVariableReadOnly(varName));

		// Create project
		IJavaProject project = createJavaProject("P1");
		addLibrary(project, "lib.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					JavaCore.VERSION_1_4);
		IClasspathEntry variable = JavaCore.newVariableEntry(new Path("TEST_DEPRECATED_READ_ONLY"), null, null);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(project, variable, false);
		assertStatus("Classpath variable 'TEST_DEPRECATED_READ_ONLY' in project 'P1' is deprecated: A deprecated and read-only initializer", status);
		assertFalse("Status should not be OK", status.isOK());
		assertEquals("Status should have WARNING severity", IStatus.WARNING, status.getSeverity());
		assertEquals("Status should have deprecated code", IJavaModelStatusConstants.DEPRECATED_VARIABLE, status.getCode());
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}

/**
 * @bug 172207: [model] Marker for deprecated classpath variable should always have WARNING severity
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=172207"
 */
public void testVariableInitializerBug172207() throws CoreException, IOException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED_READ_ONLY";
		String path = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] { varName, path }));
		assertEquals("JavaCore classpath value should have been initialized", JavaCore.getClasspathVariable(varName).toString(), path);

		// verify that Classpath Variable is read-only
		assertEquals("JavaCore classpath variable should be deprecated", "A deprecated and read-only initializer", JavaCore.getClasspathVariableDeprecationMessage(varName));
		assertTrue("JavaCore classpath variable should be read-only", JavaCore.isClasspathVariableReadOnly(varName));

		// Create project
		IJavaProject project = createJavaProject("P1");
		addLibrary(project, "lib.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					JavaCore.VERSION_1_4);
		IClasspathEntry variable = JavaCore.newVariableEntry(new Path("TEST_DEPRECATED_READ_ONLY"), null, null);
		IClasspathEntry[] entries = project.getRawClasspath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length+1], 0, length);
		entries[length] = variable;
		project.setRawClasspath(entries, null);

		// verify markers
		waitForAutoBuild();
		IMarker[] markers = project.getProject().findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
		sortMarkers(markers);
		assertMarkers("Unexpected marker(s)",
			"Classpath variable 'TEST_DEPRECATED_READ_ONLY' in project 'P1' is deprecated: A deprecated and read-only initializer",
			markers);
		assertEquals("Marker on deprecated variable should be a WARNING", IMarker.SEVERITY_WARNING, markers[0].getAttribute(IMarker.SEVERITY, -1));
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}

/**
 * @bug 186113: [model] classpath variable deprecation messages not initialized when called
 * @test	a) Verify that deprecation message can be get through {@link JavaCore#getClasspathVariableDeprecationMessage(String)}
 * 	even if the variable initializer was not called before
 * 			b) Verify that message is not stored in cache when variable is not initialized (othwerise we could not free it up...)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=186113"
 */
public void testVariableInitializerBug186113a() throws CoreException {
	assertEquals("Invalid deprecation message!",
		"Test deprecated flag",
		JavaCore.getClasspathVariableDeprecationMessage("TEST_DEPRECATED")
	);
}
public void testVariableInitializerBug186113b() throws CoreException {
	JavaCore.getClasspathVariableDeprecationMessage("TEST_DEPRECATED");
	assertNull("Deprecation message should not have been stored!", JavaModelManager.getJavaModelManager().deprecatedVariables.get("TEST_DEPRECATED"));
}

/**
 * @bug 200449: [model] classpath variable deprecation messages not initialized when called
 * @test	a) Verify that deprecation message is well stored in cache when variable is iniatialized
 * 			b) Verify that deprecation message is well removed in cache when variable is removed
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=200449"
 */
public void testVariableInitializerBug200449() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED";
		String filePath = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {varName, filePath}));
		JavaCore.getClasspathVariable(varName); // init variable

		// Verify that deprecation message has been stored
		assertNotNull("Deprecation message should have been stored!", JavaModelManager.getJavaModelManager().deprecatedVariables.get("TEST_DEPRECATED"));
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}
public void testVariableInitializerBug200449b() throws CoreException {
	// Verify that deprecated variable has been removed
	assertNull("Deprecation message should have been removed!", JavaModelManager.getJavaModelManager().deprecatedVariables.get("TEST_DEPRECATED"));
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=61872"
 */
public void testUserLibraryInitializer1() throws Exception {
	try {
		// Create new user library "SWT"
		ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
		String libraryName = "SWT";
		IPath containerPath = new Path(JavaCore.USER_LIBRARY_CONTAINER_ID);
		UserLibraryClasspathContainer containerSuggestion = new UserLibraryClasspathContainer(libraryName);
		initializer.requestClasspathContainerUpdate(containerPath.append(libraryName), null, containerSuggestion);

		// Create java project
		createJavaProject("p61872");
		IFile jarFile = createFile("/p61872/swt.jar", "");
		IFile srcFile = createFile("/p61872/swtsrc.zip", "");

		// Modify user library
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		String propertyName = JavaModelManager.CP_USERLIBRARY_PREFERENCES_PREFIX+"SWT";
		StringBuilder propertyValue = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		String jarFullPath = getWorkspaceRoot().getLocation().append(jarFile.getFullPath()).toString();
		propertyValue.append(" path=\""+jarFullPath);
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.put(propertyName, propertyValue.toString());
		preferences.flush();

		// Modify project classpath
		editFile(
			"/p61872/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.USER_LIBRARY/SWT\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>"
		);

		// Verify
		IClasspathEntry[] entries = getJavaProject("p61872").getResolvedClasspath(true);
		assertEquals("Invalid entries number in resolved classpath for project p61872!", 1, entries.length);
		assertEquals("Invalid path for project 61872 classpath entry!", jarFullPath.toLowerCase(), entries[0].getPath().toString().toLowerCase());
		assertNull("Project 61872 classpath entry should not have any source attached!", entries[0].getSourceAttachmentPath());

		// Modify user library
		propertyValue = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		String srcFullPath = getWorkspaceRoot().getLocation().append(srcFile.getFullPath()).toString();
		propertyValue.append(" sourceattachment=\""+srcFullPath);
		propertyValue.append("\" path=\""+jarFullPath);
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.put(propertyName, propertyValue.toString());
		preferences.flush();

		// Verify
		entries = getJavaProject("p61872").getResolvedClasspath(true);
		assertEquals("Invalid entries number in resolved classpath for project p61872!", 1, entries.length);
		assertEquals("Invalid path for project 61872 classpath entry!", jarFullPath.toLowerCase(), entries[0].getPath().toString().toLowerCase());
		assertEquals("Invalid source attachement path for project 61872 classpath entry!", srcFullPath.toLowerCase(), entries[0].getSourceAttachmentPath().toString().toLowerCase());
	} finally {
		deleteProject("p61872");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=346002"
 */
public void testBug346002() throws Exception {
	ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
	String libraryName = "TEST";
	IPath containerPath = new Path(JavaCore.USER_LIBRARY_CONTAINER_ID);
	UserLibraryClasspathContainer containerSuggestion = new UserLibraryClasspathContainer(libraryName);
	initializer.requestClasspathContainerUpdate(containerPath.append(libraryName), null, containerSuggestion);

	String libPath = "C:/test/test.jar";

	IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
	String propertyName = JavaModelManager.CP_USERLIBRARY_PREFERENCES_PREFIX+ "TEST";

	StringBuilder propertyValue = new StringBuilder(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"2\">\r\n<archive");
	propertyValue.append(" path=\"" + libPath + "\"/>\r\n");
	propertyValue.append("</userlibrary>\r\n");
	preferences.put(propertyName, propertyValue.toString());

	propertyName = JavaModelManager.CP_USERLIBRARY_PREFERENCES_PREFIX + "INVALID";
	propertyValue = new StringBuilder(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"2\">\r\n<archive");
	propertyValue.append(" path=\"\"/>");
	propertyValue.append("</userlibrary>\r\n");
	preferences.put(propertyName, propertyValue.toString());
	preferences.flush();

	try {
		simulateExitRestart();

		UserLibrary userLibrary = JavaModelManager.getUserLibraryManager().getUserLibrary(libraryName);
		assertNotNull(userLibrary);
		IPath entryPath = userLibrary.getEntries()[0].getPath();
		assertEquals("Path should be absolute", true, entryPath.isAbsolute());

		userLibrary = JavaModelManager.getUserLibraryManager().getUserLibrary("INVALID");
		assertNull(userLibrary);
	}
	catch (ClasspathEntry.AssertionFailedException e) {
		fail("Should not throw AssertionFailedException");
	}
}

/*
 * Ensures that when multiple threads enter the batch container initialization,
 * a second thread does not initialize a container if the first thread has already completed it
 */
public void testBug525597() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar", "P3", "/P1/lib.jar"}));
		createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		createJavaProject(
				"P3",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		createJavaProject(
				"P4",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// simulate state on startup
		simulateExitRestart();

		Thread helperThread = new Thread() {
			@Override
				public void run() {
					try {
						JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
						javaModelManager.containerRemove(null);
						javaModelManager.forceBatchInitializations(false);
						javaModelManager.getClasspathContainer(Path.EMPTY, null);
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
		};
		helperThread.setName("ClasspathInitializerTests Helper");
		AtomicInteger p3Counter=new AtomicInteger(0);
		Thread mainThread=Thread.currentThread();
		Semaphore s1=new Semaphore(0);
		Semaphore s2=new Semaphore(0);

		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar", "P3", "/P1/lib.jar"}) {
			@Override
			public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
				if (Thread.currentThread() == helperThread) {
					if (project.getElementName().equals("P2")) {
						s1.release();
						try {
							s2.acquire(10000);
						} catch (TimeOutException e) {
							// ignore
						}
					} else if (project.getElementName().equals("P3")) {
						p3Counter.incrementAndGet();
					}
				} else if (Thread.currentThread() == mainThread) {
					if (project.getElementName().equals("P3")) {
						p3Counter.incrementAndGet();
					} else if (project.getElementName().equals("P4")) {
						// this point is reached when helperThread is still waiting in P2
						// and already has P3 on its TODO list.
						s2.release();
					}
				}
				super.initialize(containerPath, project);
			}
		});
		helperThread.start();
		// wait till helperThread has reached initializer
		try {
			s1.acquire(10000);
		} catch (TimeOutException e1) {
			// ignore
		}
		JavaModelManager.getJavaModelManager().getClasspathContainer(Path.EMPTY, null);
		try {
			helperThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals("P3 initialized more than once.", 1, p3Counter.get());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
		deleteProject("P4");
	}
}
/*
 * Ensures that when multiple threads enter the batch container initialization,
 * and two threads both initialize a container, only one result is used.
 */
public void testBug525597B() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar", "P3", "/P1/lib.jar"}));
		createJavaProject(
				"P2",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");
		createJavaProject(
				"P3",
				new String[] {},
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"},
				"");

		// simulate state on startup
		simulateExitRestart();

		Thread helperThread = new Thread() {
			@Override
				public void run() {
					try {
						JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
						javaModelManager.containerRemove(null);
						javaModelManager.forceBatchInitializations(false);
						javaModelManager.getClasspathContainer(Path.EMPTY, null);
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
		};
		helperThread.setName("ClasspathInitializerTests Helper");
		AtomicInteger p2Counter=new AtomicInteger(0);
		AtomicReference<IClasspathContainer> helperContainer=new AtomicReference<>();
		AtomicReference<IClasspathContainer> mainContainer=new AtomicReference<>();
		Thread mainThread=Thread.currentThread();
		Semaphore s1=new Semaphore(0);
		Semaphore s2=new Semaphore(0);
		Semaphore s3=new Semaphore(0);
		Semaphore s4=new Semaphore(0);
		AtomicReference<IJavaProject> p2Project=new AtomicReference<>();

		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar", "P3", "/P1/lib.jar"}) {
			@Override
			public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
				if (Thread.currentThread() == helperThread) {
					if (project.getElementName().equals("P2")) {
						p2Project.set(project);
						s1.release();
						try {
							s2.acquire(10000);
						} catch (TimeOutException e) {
							// ignore
						}
					} else if (project.getElementName().equals("P3")) {
						s3.release();
						try {
							s4.acquire(10000);
						} catch (TimeOutException e) {
							// ignore
						}
						if (JavaModelManager.getJavaModelManager().containerBeingInitializedGet(p2Project.get(),
								containerPath) != null) {
							p2Counter.incrementAndGet();
						}
						helperContainer.set(JavaModelManager.getJavaModelManager().getClasspathContainer(containerPath, p2Project.get()));
					}
				} else if (Thread.currentThread() == mainThread) {
					if (project.getElementName().equals("P2")) {
						// this point is reached when helperThread is also still waiting in P2
						s2.release();
					} else if (project.getElementName().equals("P3")) {
						try {
							s3.acquire(10000);
						} catch (TimeOutException e) {
							// ignore
						}
						s4.release();
						if (JavaModelManager.getJavaModelManager().containerBeingInitializedGet(p2Project.get(),
								containerPath) != null) {
							p2Counter.incrementAndGet();
						}
						mainContainer.set(JavaModelManager.getJavaModelManager().getClasspathContainer(containerPath, p2Project.get()));
					}
				}
				super.initialize(containerPath, project);
			}
		});
		helperThread.start();
		// wait till helperThread has reached initializer
		try {
			s1.acquire(10000);
		} catch (TimeOutException e1) {
			// ignore
		}
		JavaModelManager.getJavaModelManager().getClasspathContainer(Path.EMPTY, null);
		try {
			helperThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals("more than one result for P2 used.", 1, p2Counter.get());
		assertEquals("main and helper threads did not see the same container.", helperContainer.get(), mainContainer.get());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}

}
