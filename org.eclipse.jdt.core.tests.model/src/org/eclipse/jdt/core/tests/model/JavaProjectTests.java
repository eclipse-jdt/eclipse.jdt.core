package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.*;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JavaProjectTests extends AbstractJavaModelTests {
public JavaProjectTests(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new Suite(JavaProjectTests.class.getName());
	suite.addTest(new JavaProjectTests("testProjectGetChildren"));
	suite.addTest(new JavaProjectTests("testProjectGetPackageFragments"));
	suite.addTest(new JavaProjectTests("testRootGetPackageFragments"));
	suite.addTest(new JavaProjectTests("testInternalArchiveCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testExternalArchiveCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testProjectCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testPackageFragmentCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testPackageFragmentHasSubpackages"));
	suite.addTest(new JavaProjectTests("testIsDefaultPackage"));
	suite.addTest(new JavaProjectTests("testPackageFragmentRootCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testJarPackageFragmentCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testCompilationUnitCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testClassFileCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testArchiveClassFileCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testBinaryTypeCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testSourceMethodCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testOutputLocationNotAddedAsPackageFragment"));
	suite.addTest(new JavaProjectTests("testOutputLocationNestedInRoot"));
	suite.addTest(new JavaProjectTests("testChangeOutputLocation"));
	suite.addTest(new JavaProjectTests("testFindElementPackage"));
	suite.addTest(new JavaProjectTests("testFindElementClassFile"));
	suite.addTest(new JavaProjectTests("testFindElementCompilationUnit"));
	suite.addTest(new JavaProjectTests("testFindElementCompilationUnitDefaultPackage"));
	suite.addTest(new JavaProjectTests("testFindElementInvalidPath"));
	suite.addTest(new JavaProjectTests("testProjectClose"));
	suite.addTest(new JavaProjectTests("testPackageFragmentRenameAndCreate"));
	suite.addTest(new JavaProjectTests("testFolderWithDotName"));
	suite.addTest(new JavaProjectTests("testPackageFragmentNonJavaResources"));
	suite.addTest(new JavaProjectTests("testPackageFragmentRootNonJavaResources"));
	suite.addTest(new JavaProjectTests("testAddNonJavaResourcePackageFragmentRoot"));
	suite.addTest(new JavaProjectTests("testDeletePackageWithAutobuild"));
	return suite;
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("JavaProjectTests");
	setUpJavaProject("JavaProjectSrcTests");
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaProjectTests");
	deleteProject("JavaProjectSrcTests");
	super.tearDownSuite();
}


/**
 * Test adding a non-java resource in a package fragment root that correspond to
 * the project.
 * (Regression test for PR #1G58NB8)
 */
public void testAddNonJavaResourcePackageFragmentRoot() throws JavaModelException, CoreException {
	// get resources of source package fragment root at project level
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "");
	Object[] resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources", 2, resources.length); // .classpath and .project files
	assertTrue("resource should be an IFile",  resources[0] instanceof IFile);
	IFile resource = (IFile)resources[0];
	IPath newPath = root.getUnderlyingResource().getFullPath().append("TestNonJavaResource.abc");
	try {
		// copy and rename resource
		resource.copy(
			newPath, 
			true, 
			null);
		
		// ensure the new resource is present
		root.getParent().getUnderlyingResource().refreshLocal(1, null);
		resources = root.getNonJavaResources();
		assertEquals("incorrect number of non java resources", 2, resources.length);
	} finally {
		// clean up
		resource.getWorkspace().getRoot().getFile(newPath).delete(true, null);
	}
}
/**
 * Test that a class file in a jar has no corresponding resource.
 */
public void testArchiveClassFileCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IPackageFragment element = root.getPackageFragment("p");
	IClassFile cf= element.getClassFile("X.class");
	IResource corr = cf.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that a binary type
 * has a corresponding resource.
 */
public void testBinaryTypeCorrespondingResource() throws JavaModelException {
	IClassFile element= getClassFile("JavaProjectTests", "", "p", "Y.class");
	IType type= element.getType();
	IResource corr= type.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * When the output location is changed, package fragments can be added/removed
 */
public void testChangeOutputLocation() throws JavaModelException, CoreException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IContainer underLyingResource = (IContainer)project.getUnderlyingResource();
	IFolder folder= underLyingResource.getFolder(new Path("output"));

	try {
		startDeltas();
		project.setOutputLocation(folder.getFullPath(), null);
		IPackageFragment fragment= root.getPackageFragment("bin");
		assertTrue("bin package fragment should appear", getDeltaFor(fragment).getKind() == IJavaElementDelta.ADDED);
	} finally {
		stopDeltas();
		try {
			startDeltas();
			folder= underLyingResource.getFolder(new Path("bin"));	
			project.setOutputLocation(folder.getFullPath(), null);
			IPackageFragment fragment= root.getPackageFragment("bin");
			assertTrue("bin package fragment should be removed", getDeltaFor(fragment).getKind() == IJavaElementDelta.REMOVED);
		} finally {
			stopDeltas();
		}
	}
}
/**
 * Test that a class file
 * has a corresponding resource.
 */
public void testClassFileCorrespondingResource() throws JavaModelException {
	IClassFile element= getClassFile("JavaProjectTests", "", "p", "Y.class");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("p").getFile("Y.class");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that a compilation unit
 * has a corresponding resource.
 */
public void testCompilationUnitCorrespondingResource() throws JavaModelException {
	ICompilationUnit element= getCompilationUnit("JavaProjectTests", "", "q", "A.java");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("q").getFile("A.java");
	assertTrue("incorrect corresponding resource", corr.equals(res));
	assertEquals("Project is incorrect for the compilation unit", "JavaProjectTests", corr.getProject().getName());
}
/**
 * Tests the fix for "1FWNMKD: ITPJCORE:ALL - Package Fragment Removal not reported correctly"
 */
public void testDeletePackageWithAutobuild() throws JavaModelException, CoreException, IOException {
	// close all project except JavaProjectTests so as to avoid side effects while autobuilding
	IProject[] projects = getWorkspaceRoot().getProjects();
	for (int i = 0; i < projects.length; i++) {
		IProject project = projects[i];
		if (project.getName().equals("JavaProjectTests")) continue;
		project.close(null);
	}

	// turn autobuilding on
	IWorkspace workspace = getWorkspace();
	boolean autoBuild = workspace.isAutoBuilding();
	IWorkspaceDescription description = workspace.getDescription();
	description.setAutoBuilding(true);
	workspace.setDescription(description);

	startDeltas();
	IPackageFragment frag = getPackageFragment("JavaProjectTests", "", "x.y");
	IFolder folder = (IFolder) frag.getUnderlyingResource();
	try {
		folder.delete(true, null);
		assertTrue("should have been notified of package removal", getDeltaFor(frag) != null);
	} finally {
		stopDeltas();
		
		// turn autobuild off
		description.setAutoBuilding(autoBuild);
		workspace.setDescription(description);

		// reopen projects
		projects = getWorkspaceRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (project.getName().equals("JavaProjectTests")) continue;
			project.open(null);
		}
	}
}
/**
 * Test that an (external) jar
 * has no corresponding resource.
 */
public void testExternalArchiveCorrespondingResource() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot element= project.getPackageFragmentRoot(getExternalJCLPath());
	IResource corr= element.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that a compilation unit can be found for a binary type
 */
public void testFindElementClassFile() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("java/lang/Object.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.CLASS_FILE
		&& element.getElementName().equals("Object.class"));
}
/**
 * Test that a compilation unit can be found
 */
public void testFindElementCompilationUnit() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("x/y/A.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT
		&& element.getElementName().equals("A.java"));
}
/**
 * Test that a compilation unit can be found in a default package
 */
public void testFindElementCompilationUnitDefaultPackage() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("B.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT
		&& element.getElementName().equals("B.java"));
}
/**
 * Test that an invlaid path throws an exception
 */
public void testFindElementInvalidPath() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	boolean failed= false;
	try {
		project.findElement(null);
	} catch (JavaModelException e) {
		failed= true;
		assertTrue("wrong status code" , e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_PATH);
	}
	assertTrue("Shold have failed", failed);
	
	failed = false;
	try {
		project.findElement(new Path("/something/absolute"));
	} catch (JavaModelException e) {
		failed= true;
		assertTrue("wrong status code" , e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_PATH);
	}
	assertTrue("Shold have failed", failed);

	IJavaElement element= project.findElement(new Path("does/not/exist/HelloWorld.java"));
	assertTrue("should get no element", element == null);
}
/**
 * Test that a package can be found
 */
public void testFindElementPackage() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("x/y"));
	assertTrue("package not found" , element != null && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT
		&& element.getElementName().equals("x.y"));
}
/**
 * Test that a folder with a dot name does not relate to a package fragment
 */
public void testFolderWithDotName() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IContainer folder= (IContainer)root.getCorrespondingResource();
	try {
		startDeltas();
		folder.getFolder(new Path("org.eclipse")).create(false, true, null);
		assertTrue("should be one Java Delta", this.deltaListener.deltas.length == 1);
		
		stopDeltas();
		IJavaElement[] children = root.getChildren();
		IPackageFragment bogus = root.getPackageFragment("org.eclipse");
		for (int i = 0; i < children.length; i++) {
			assertTrue("org.eclipse should not be present as child", !children[i].equals(bogus));
		}
		assertTrue("org.eclipse should not exist", !bogus.exists());
	} finally {
		folder.getFolder(new Path("org.eclipse")).delete(true, null);
	}	
}
/**
 * Test that an (internal) jar
 * has a corresponding resource.
 */
public void testInternalArchiveCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot element= getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFile("lib.jar");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test IJavaPackageFragment.isDefaultPackage().
 */
public void testIsDefaultPackage() throws JavaModelException {
	IPackageFragment def = getPackageFragment("JavaProjectTests", "", "");
	assertTrue("should be default package", def.isDefaultPackage());
	IPackageFragment y =
		getPackageFragment("JavaProjectTests", "", "x.y");
	assertTrue("x.y should not be default pakackage", !y.isDefaultPackage());

	IPackageFragment def2 = getPackageFragment("JavaProjectTests", "lib.jar", "");
	assertTrue("lib.jar should have default package", def2.isDefaultPackage());
	IPackageFragment p =
		getPackageFragment("JavaProjectTests", "lib.jar", "p");
	assertTrue("p should not be default package", !p.isDefaultPackage());
}
/**
 * Test that a package fragment in a jar has no corresponding resource.
 */
public void testJarPackageFragmentCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IPackageFragment element = root.getPackageFragment("p");
	IResource corr = element.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that an output location can't be set to a location inside a package fragment
 * root, except the root project folder.
 */
public void testOutputLocationNestedInRoot() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectSrcTests", "src");
	IFolder folder= (IFolder) root.getUnderlyingResource();
	IJavaProject project= getJavaProject("JavaProjectSrcTests");
	folder= folder.getFolder("x");
	boolean failed= false;
	try {
		project.setOutputLocation(folder.getFullPath(), null);
	} catch (JavaModelException e) {
		assertTrue("should be an invalid classpath", e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_CLASSPATH);
		failed= true;
	}
	assertTrue("should have failed", failed);
	
}
/**
 * Test that an output location folder is not created as a package fragment.
 */
public void testOutputLocationNotAddedAsPackageFragment() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IJavaElement[] packages= root.getChildren();
	assertEquals("unepected number of packages", 5, packages.length);
	assertTrue("should be default", packages[0].getElementName().equals(""));


	// create a nested folder in the output location and make sure it does not appear
	// as a package fragment
	IContainer underLyingResource = (IContainer)root.getUnderlyingResource();
	IFolder newFolder= underLyingResource.getFolder(new Path("bin")).getFolder(new Path("nested"));
	try {
		startDeltas();
		newFolder.create(false, true, null);
		assertTrue("should be one delta (resource deltas)", this.deltaListener.deltas != null || this.deltaListener.deltas.length == 1);
	} finally {
		stopDeltas();
		newFolder.delete(true, null);
	}
}
/**
 * Test that a package fragment (non-external, non-jar, non-default)
 * has a corresponding resource.
 */
public void testPackageFragmentCorrespondingResource() throws JavaModelException {
	IPackageFragment element= getPackageFragment("JavaProjectTests", "", "x.y");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("x").getFolder("y");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that a package fragment (non-external, non-jar, non-default)
 * has a corresponding resource.
 */
public void testPackageFragmentHasSubpackages() throws JavaModelException {
	IPackageFragment def=		getPackageFragment("JavaProjectTests", "", "");
	IPackageFragment x=		getPackageFragment("JavaProjectTests", "", "x");
	IPackageFragment y=		getPackageFragment("JavaProjectTests", "", "x.y");
	assertTrue("default should have subpackages",							def.hasSubpackages());
	assertTrue("x should have subpackages",								x.hasSubpackages());
	assertTrue("x.y should NOT have subpackages",		!y.hasSubpackages());

	IPackageFragment java = getPackageFragment("JavaProjectTests", getExternalJCLPath(), "java");
	IPackageFragment lang= getPackageFragment("JavaProjectTests", getExternalJCLPath(), "java.lang");

	assertTrue("java should have subpackages",					java.hasSubpackages());
	assertTrue("java.lang  should NOT have subpackages",			!lang.hasSubpackages());
}
/**
 * Test getting the non-java resources from a package fragment.
 */
public void testPackageFragmentNonJavaResources() throws JavaModelException {
	// regular source package with resources
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "", "x");
	Object[] resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 1)", 2, resources.length);

	// regular source package without resources
	pkg = getPackageFragment("JavaProjectTests", "", "x.y");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 2)", 0, resources.length);

	// source default package with potentialy resources
	pkg = getPackageFragment("JavaProjectTests", "", "");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 3)", 0, resources.length);

	// regular zip package with resources
	// TO DO

	// regular zip package without resources
	pkg = getPackageFragment("JavaProjectTests", "lib.jar", "p");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 5)", 0, resources.length);

	// zip default package with potentialy resources
	// TO DO
	
	// zip default package with potentialy no resources
	pkg = getPackageFragment("JavaProjectTests", "lib.jar", "");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 7)", 0, resources.length);
	
}
/**
 * Tests that after a package "foo" has been renamed into "bar", it is possible to recreate
 * a "foo" package.
 * @see 1FWX0HY: ITPCORE:WIN98 - Problem after renaming a Java package
 */
public void testPackageFragmentRenameAndCreate() throws JavaModelException, CoreException {
	IPackageFragment y = getPackageFragment("JavaProjectTests", "", "x.y");
	IFolder yFolder = (IFolder) y.getUnderlyingResource();
	IPath yPath = yFolder.getFullPath();
	IPath fooPath = yPath.removeLastSegments(1).append("foo");
	
	yFolder.move(fooPath, true, null);
	try {
		yFolder.create(true, true, null);
	} catch (Throwable e) {
		e.printStackTrace();
		assertTrue("should be able to recreate the y folder", false);
	}
	// restore the original state
	yFolder.delete(true, null);
	IPackageFragment foo = getPackageFragment("JavaProjectTests", "", "x.foo");
	IFolder fooFolder = (IFolder) foo.getUnderlyingResource();
	fooFolder.move(yPath, true, null);
}
/**
 * Test that a package fragment root (non-external, non-jar, non-default root)
 * has a corresponding resource.
 */
public void testPackageFragmentRootCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot element= getPackageFragmentRoot("JavaProjectTests", "");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests");
	assertTrue("incorrect corresponding resource", corr.equals(res));
	assertEquals("Project incorrect for folder resource", "JavaProjectTests", corr.getProject().getName());
}
/**
 * Test getting the non-java resources from a package fragment root.
 */
public void testPackageFragmentRootNonJavaResources() throws JavaModelException {
	// source package fragment root with resources
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "");
	Object[] resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 1)", 2, resources.length);

	// source package fragment root without resources
 	root = getPackageFragmentRoot("JavaProjectSrcTests", "src");
	resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 2)", 0, resources.length);

	// zip package fragment root with resources
	// TO DO
	
	// zip package fragment root without resources
	root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 4)", 0, resources.length);
}
/**
 * Tests that closing and opening a project triggers the correct deltas.
 */
public void testProjectClose() throws JavaModelException, CoreException {
	IJavaProject jproject= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot[] originalRoots = jproject.getPackageFragmentRoots();
	IProject project= jproject.getProject();

	try {
		startDeltas();
		project.close(null);
		IJavaElementDelta delta= getDeltaFor(jproject);
		assertTrue("should be a removed delta", delta != null && delta.getKind() == IJavaElementDelta.REMOVED);
	} finally {
		try {
			clearDeltas();
			
			project.open(null);
			IJavaElementDelta delta= getDeltaFor(jproject);
			assertTrue("should be an added delta", delta != null && delta.getKind() == IJavaElementDelta.ADDED);

			IPackageFragmentRoot[] openRoots = jproject.getPackageFragmentRoots();
			assertTrue("should have same number of roots", openRoots.length == originalRoots.length);
			for (int i = 0; i < openRoots.length; i++) {
				assertTrue("root not the same", openRoots[i].equals(originalRoots[i]));
			}
		} finally {
			stopDeltas();
		}
	}
}
/**
 * Test that a project has a corresponding resource.
 */
public void testProjectCorrespondingResource() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IResource corr= project.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that the correct children exist in a project
 */
public void testProjectGetChildren() throws JavaModelException {
	IJavaProject project = getJavaProject("JavaProjectTests");
	IJavaElement[] roots= project.getChildren();
	assertTrue("should be package 3 package fragment root children in 'JavaProjectTests', were " + roots.length , roots.length == 3);
}
/**
 * Test that the correct package fragments exist in the project.
 */
public void testProjectGetPackageFragments() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragment[] fragments= project.getPackageFragments();
	assertTrue("should be package 12 package fragments in 'JavaProjectTests', were " + fragments.length , fragments.length == 12);
}
/**
 * Test that the correct package fragments exist in the project.
 */
public void testRootGetPackageFragments() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IJavaElement[] fragments= root.getChildren();
	assertTrue("should be package 5 package fragments in source root, were " + fragments.length , fragments.length == 5);

	root= getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	fragments= root.getChildren();	
	assertTrue("should be package 3 package fragments in lib.jar, were " + fragments.length , fragments.length == 3);
}
/**
 * Test that a method
 * has no corresponding resource.
 */
public void testSourceMethodCorrespondingResource() throws JavaModelException {
	ICompilationUnit element= getCompilationUnit("JavaProjectTests", "", "q", "A.java");
	IMethod[] methods = element.getType("A").getMethods();
	assertTrue("missing methods", methods.length > 0);
	IResource corr= methods[0].getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
}
