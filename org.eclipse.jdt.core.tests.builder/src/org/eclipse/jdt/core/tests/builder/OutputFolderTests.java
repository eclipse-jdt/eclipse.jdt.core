package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;

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
		env.addExternalJar(projectPath, env.getMinimalJarPath());
		
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
		env.addExternalJar(projectPath, env.getMinimalJarPath());
		
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
}