package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * Basic tests of the image builder.
 */
public class OutputFolderTests extends Tests {
	private static String[] EXCLUDED_TESTS = {
		"OutputFolderTests", "testDeleteOutputFolder",
		"OutputFolderTests", "testChangeOutputFolder"
		};
	
	public OutputFolderTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(OutputFolderTests.class);
		return suite;
	}
	
	public void testDeleteOutputFolder() {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		IPath root = env.getPackageFragmentRootPath(projectPath, "");
		IPath bin = env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "Test",
			"public class Test {\n"+
			"}\n"
			);
			
		env.addFile(root, "Test.txt", "");
		
			
		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin,
			bin.append("Test.class"),
			bin.append("Test.txt")
		});
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.removeFolder(bin);
		
		incrementalBuild();
		expectingPresenceOf(new IPath[]{
			bin,
			bin.append("Test.class"),
			bin.append("Test.txt")
		});
	}
	
	public void testChangeOutputFolder() {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin1 = env.setOutputFolder(projectPath, "bin1");

		env.addClass(root, "p", "Test",
			"package p;\n" +
			"public class Test {\n"+
			"}\n"
			);
			

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin1,
			bin1.append("p").append("Test.class"),
		});
		
		//----------------------------
		//           Step 2
		//----------------------------
		IPath bin2 = env.setOutputFolder(projectPath, "bin2");
		
		incrementalBuild();
		
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin2,
			bin2.append("p").append("Test.class"),
		});
		expectingNoPresenceOf(new IPath[]{
			bin1,
			bin1.append("p").append("Test.class"),
		});
	}
	/*
	 * Ensures that changing the output to be the project (when the project has a source folder src)
	 * doesn't scrub the project on exit/restart.
	 * (regression test for bug 32588 Error saving changed source files; all files in project deleted)
	 */
	public void testInvalidOutput() throws JavaModelException {
		// setup project with 1 src folder and 1 output folder
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
	
		// add cu and build
		env.addClass(projectPath, "src", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		fullBuild();
		expectingNoProblems();

		// set invalid  output foder by editing the .classpath file
		env.addFile(
			projectPath, 
			".classpath",  //$NON-NLS-1$
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
			"<classpath>\n" + //$NON-NLS-1$
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" + //$NON-NLS-1$
			"    <classpathentry kind=\"var\" path=\"" + Util.getJavaClassLib() + "\"/>\n" + //$NON-NLS-1$ //$NON-NLS-2$
			"    <classpathentry kind=\"output\" path=\"\"/>\n" + //$NON-NLS-1$
			"</classpath>"
		);
		
		// simulate exit/restart
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		JavaProject project = (JavaProject)manager.getJavaModel().getJavaProject("P"); //$NON-NLS-1$
		manager.removePerProjectInfo(project);
		
		// change cu and build
		IPath cuPath = env.addClass(projectPath, "src", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A { String s;}" //$NON-NLS-1$
			);		
		incrementalBuild();

		expectingPresenceOf(new IPath[] {cuPath});
	}
}