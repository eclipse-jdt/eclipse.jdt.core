package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;

public class PackageTests extends Tests {
	private static String[] EXCLUDED_TESTS = {
	};
	
	public PackageTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(PackageTests.class);
		return suite;
	}
	
	/**
	 * Bugs 6564
	 */
	public void testPackageProblem(){
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, env.getMinimalJarPath());
		env.removePackageFragmentRoot(projectPath, "");
		IPath src = env.addPackageFragmentRoot(projectPath, "src");
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		
		IPath x = env.addClass(src, "pack", "X",
			"package pack;\n"+
			"public class X {\n"+
			"}\n"
			);
			
		env.addClass(src2, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"}\n"
			);
			
		env.addClass(src2, "p2", "Y",
			"package p2;\n"+
			"public class Y extends p1.X {\n"+
			"}\n"
			);
			
		env.addClass(src2, "p3", "Z",
			"package p3;\n"+
			"public class Z extends p2.Y {\n"+
			"}\n"
			);

		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.removeClass(env.getPackagePath(src, "pack"), "X");
		env.removePackage(src2, "p3");
			
		incrementalBuild();
		expectingNoProblems();
	}
}

