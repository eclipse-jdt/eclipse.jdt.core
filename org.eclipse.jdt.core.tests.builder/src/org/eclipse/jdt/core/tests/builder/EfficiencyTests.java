package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic efficiency tests of the image builder.
 */
public class EfficiencyTests extends Tests {
	public EfficiencyTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(EfficiencyTests.class);
	}
	
	public void testEfficiency() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"}\n"
			);
			
		env.addClass(root, "p2", "Collaborator",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Collaborator extends Indicted{\n"+
			"}\n"
			);
		
		fullBuild(projectPath);
		
		env.addClass(root, "p1", "Indicted",
			"package p1;\n"+
			"public abstract class Indicted {\n"+
			"   public abstract void foo();\n"+
			"}\n"
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p2.Collaborator", "p1.Indicted"});
		expectingCompilingOrder(new String[]{"p1.Indicted", "p2.Collaborator"});
	}
	
	public void testMethodAddition() {

		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"	}\n" +
			"}\n"
			);
			
		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);
		
		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void bar(){}	\n" +
			"	void foo() {	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p2.Y"});
		expectingCompilingOrder(new String[]{"p1.X", "p2.Y" });
	}

	public void testLocalTypeAddition() {

		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"	}\n" +
			"}\n"
			);
			
		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);
		
		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"});
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1" });
	}

	public void testLocalTypeAddition2() {

		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);
			
		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);
		
		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1", "p1.X$2"});
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1", "p1.X$2" });
	}

	public void testLocalTypeRemoval() {

		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);
			
		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);
		
		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"	}\n" +
			"}\n"
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X"});
		expectingCompilingOrder(new String[]{"p1.X" });
	}

	public void testLocalTypeRemoval2() {

		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new Object(){	\n" +
			"		};	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);
			
		env.addClass(root, "p2", "Y",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Y extends X{\n"+
			"}\n"
			);
		
		env.addClass(root, "p3", "Z",
			"package p2;\n"+
			"import p1.*;\n"+
			"public class Z{\n"+
			"}\n"
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X",
			"package p1;\n"+
			"public class X {\n"+
			"	void foo() {	\n" +
			"		new X(){	\n" +
			"			void bar(){}	\n" +
			"		};	\n" +
			"	}\n" +
			"}\n"
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"});
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1" });
	}
}