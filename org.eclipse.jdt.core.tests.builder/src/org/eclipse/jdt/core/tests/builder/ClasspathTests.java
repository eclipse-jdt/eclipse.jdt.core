package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;

import java.util.*;

public class ClasspathTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};

	public ClasspathTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(ClasspathTests.class);
		return suite;
	}

	public void testClosedProject() {
		IPath project1Path = env.addProject("CP1");
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		IPath jarPath = env.addInternalJar(project1Path, "temp.jar", new byte[] {0});

		IPath project2Path = env.addProject("CP2");
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);

		IPath project3Path = env.addProject("CP3");
		env.addExternalJar(project3Path, Util.getJavaClassLib());
		env.addExternalJar(project3Path, jarPath.toString());

		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.closeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(new IPath[] {project2Path, project3Path});
		expectingOnlySpecificProblemsFor(project2Path,
			new Problem[] {
				new Problem("", "The project was not built due to classpath errors (incomplete or involved in cycle).", project2Path),
				new Problem("Build path", "Missing required Java project: CP1.", project2Path)
			}
		);
		expectingOnlySpecificProblemsFor(project3Path,
			new Problem[] {
				new Problem("", "The project was not built due to classpath errors (incomplete or involved in cycle).", project3Path),
				new Problem("Build path", "Missing required library: /CP1/temp.jar.", project3Path)
			}
		);

		env.openProject(project1Path);
		incrementalBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 3
		//----------------------------
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
		JavaCore.setOptions(options);
		env.closeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(new IPath[] {project2Path, project3Path});
		expectingOnlySpecificProblemFor(project2Path,
			new Problem("Build path", "Missing required Java project: CP1.", project2Path)
		);
		expectingOnlySpecificProblemFor(project3Path,
			new Problem("Build path", "Missing required library: /CP1/temp.jar.", project3Path)
		);

		env.openProject(project1Path);
		incrementalBuild();
		expectingNoProblems();

		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ABORT);
		JavaCore.setOptions(options);
	}

	public void testMissingProject() {
		IPath project1Path = env.addProject("MP1");
		env.addExternalJar(project1Path, Util.getJavaClassLib());

		IPath project2Path = env.addProject("MP2");
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);

		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.removeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(project2Path);
		expectingOnlySpecificProblemsFor(project2Path,
			new Problem[] {
				new Problem("", "The project was not built due to classpath errors (incomplete or involved in cycle).", project2Path),
				new Problem("Build path", "Missing required Java project: MP1.", project2Path)
			}
		);

		project1Path = env.addProject("MP1");
		env.addExternalJar(project1Path, Util.getJavaClassLib());

		incrementalBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 3
		//----------------------------
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
		JavaCore.setOptions(options);
		env.removeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(project2Path);
		expectingOnlySpecificProblemFor(project2Path,
			new Problem("Build path", "Missing required Java project: MP1.", project2Path)
		);

		project1Path = env.addProject("MP1");
		env.addExternalJar(project1Path, Util.getJavaClassLib());

		incrementalBuild();
		expectingNoProblems();

		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ABORT);
		JavaCore.setOptions(options);
	}

	public void testMissingLibrary() {
		IPath projectPath = env.addProject("Project");
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		IPath classTest1 = env.addClass(root, "p1", "Test1",
			"package p1;\n"+
			"public class Test1 {}"
		);
		env.addClass(root, "p2", "Test2",
			"package p2;\n"+
			"public class Test2 {}"
		);
		env.addClass(root, "p2", "Test3",
			"package p2;\n"+
			"public class Test3 {}"
		);

		fullBuild();
		expectingOnlyProblemsFor(new IPath[] {projectPath, classTest1});
		expectingOnlySpecificProblemsFor(projectPath,
			new Problem[] {
				new Problem("", "The project was not built since its classpath is incomplete. Can not find the class file for java.lang.Object. Fix the classpath then try rebuilding this project", projectPath),
				new Problem("p1", "This compilation unit indirectly references the missing type java.lang.Object (typically some required class file is referencing a type outside the classpath)", classTest1)
			}
		);

		//----------------------------
		//           Step 2
		//----------------------------	
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin.append("p1").append("Test1.class"),
			bin.append("p2").append("Test2.class"),
			bin.append("p2").append("Test3.class")
		});
	}
}