package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;

/**
 * Basic tests of the image builder.
 */
public class BasicBuildTests extends Tests {
	public BasicBuildTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(BasicBuildTests.class);
	}
	
	public void testBuild() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, env.getMinimalJarPath());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "Hello",
			"package p1;\n"+
			"public class Hello {\n"+
			"   public static void main(String args[]) {\n"+
			"      System.out.println(\"Hello world\");\n"+
			"   }\n"+
			"}\n"
			);
			
		incrementalBuild(projectPath);
	}
}