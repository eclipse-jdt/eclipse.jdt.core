package org.eclipse.jdt.core.tests.model;

import static java.util.stream.Collectors.toCollection;

import java.util.Collection;
import java.util.LinkedHashSet;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.search.matching.ClasspathSourceDirectory;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchNameEnvironment;

public class JavaSearchNameEnvironmentTest extends ModifyingResourceTests {

	static {
		//NameLookup.VERBOSE = true;
	}

	static class JavaSearchNameEnvironmentUnderTest extends JavaSearchNameEnvironment {
		public JavaSearchNameEnvironmentUnderTest(IJavaProject javaProject, ICompilationUnit[] copies) {
			super(javaProject, copies);
		}
		public LinkedHashSet<ClasspathLocation> getLocationSet() {
			return super.locationSet;
		}
		@Override
		public Iterable<ClasspathLocation> getLocationsFor(String moduleName, String qualifiedPackageName) {
			return super.getLocationsFor(moduleName, qualifiedPackageName);
		}
		public LinkedHashSet<ClasspathLocation> getAllIndexedLocations() {
			return super.packageNameToClassPathLocations.values().stream().flatMap(Collection::stream).collect(toCollection(LinkedHashSet::new));
		}
		@Override
		public void addProjectClassPath(JavaProject javaProject) {
			super.addProjectClassPath(javaProject);
		}
	}

	private IJavaProject p1;
	private IJavaProject p2;

	public JavaSearchNameEnvironmentTest(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchNameEnvironmentTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.p1 = setUpJavaProject("JavaSearchMultipleProjects1");
		this.p2 = setUpJavaProject("JavaSearchMultipleProjects2");
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			deleteProject(this.p1.getElementName());
			deleteProject(this.p2.getElementName());
		} finally {
			super.tearDown();
		}
	}

	public void testLocationsAreEqual() throws CoreException {
		JavaSearchNameEnvironmentUnderTest nameEnvironment = newJavaSearchEnvironment(this.p1, this.p2);

		LinkedHashSet<ClasspathLocation> locationSet = nameEnvironment.getLocationSet();
		LinkedHashSet<ClasspathLocation> allIndexedLocations = nameEnvironment.getAllIndexedLocations();

		for (ClasspathLocation cp : locationSet) {
			assertTrue("index must contain: " + cp, allIndexedLocations.contains(cp));
		}
	}

	public void testWorkingCopies() throws CoreException {

		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchMultipleProjects2/src/b88300/SubClass.java",
				"package b88300;\n" +
				"public class SubClass extends SuperClass {\n" +
				"	private void aMethod(String x) {\n" +
				"	}\n" +
				"	public void aMethod(Object x) {\n" +
				"	}\n" +
				"}\n"
			);
			this.workingCopies[1] = getWorkingCopy("/JavaSearchMultipleProjects2/src/b88300/SuperClass.java",
				"package b88300;\n" +
				"public class SuperClass {\n" +
				"    public void aMethod(Object x) {\n" +
				"    }\n" +
				"}\n"
				);
			this.workingCopies[2] = getWorkingCopy("/JavaSearchMultipleProjects2/src/b88300/User.java",
				"package b88300;\n" +
				"public class User {\n" +
				"    public void methodUsingSubClassMethod() {\n" +
				"        SuperClass user = new SubClass();\n" +
				"        user.aMethod(new Object());\n" +
				"    }\n" +
				"}\n"
			);

		JavaSearchNameEnvironmentUnderTest nameEnvironment = newJavaSearchEnvironment(this.p2, this.p1);

		Iterable<ClasspathLocation> locationsForPackage = nameEnvironment.getLocationsFor(null, "b88300");
		assertNotNull(locationsForPackage);
		assertTrue(locationsForPackage.iterator().hasNext());
		ClasspathLocation cp = locationsForPackage.iterator().next();
		assertTrue(cp instanceof ClasspathSourceDirectory);

		char[][] packageName = new char[][] { "b88300".toCharArray() };
		assertNotNull("Type User not found!", nameEnvironment.findType("User".toCharArray(), packageName));
		assertNotNull("Type SuperClass not found!", nameEnvironment.findType("SuperClass".toCharArray(), packageName));
		assertNotNull("Type SubClass not found!", nameEnvironment.findType("SubClass".toCharArray(), packageName));
	}

	private JavaSearchNameEnvironmentUnderTest newJavaSearchEnvironment(IJavaProject first, IJavaProject... remaining) {
		JavaSearchNameEnvironmentUnderTest env = new JavaSearchNameEnvironmentUnderTest(first, this.workingCopies);
		if(remaining != null) {
			for (int i = 0; i < remaining.length; i++) {
				env.addProjectClassPath((JavaProject) remaining[i]);
			}
		}
		return env;
	}
}
