package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.util.Util;


/**
 * Basic errors tests of the image builder.
 */
public class ErrorsTests extends Tests {
	public ErrorsTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(ErrorsTests.class);
	}
	
	public void testErrors() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,"");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"}\n"
			);
			
		IPath collaboratorPath =  env.addClass(root, "p2", "Collaborator",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Collaborator extends Indicted{\n"+
			"}\n"
			);
		
		fullBuild(projectPath);
		expectingNoProblems();
		
		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"   public abstract void foo();\n"+
			"}\n"
			);
			
		incrementalBuild(projectPath);

		expectingOnlyProblemsFor(collaboratorPath);
		expectingOnlySpecificProblemFor(collaboratorPath, new Problem("Collaborator", "Class must implement the inherited abstract method Indicted.foo()", collaboratorPath));
	}
}