/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.NullAnnotationTests9;

import junit.framework.Test;

public class NullAnnotationModelTests9 extends ReconcilerTests {

	String ANNOTATION_LIB;

	public static Test suite() {
		return buildModelTestSuite(NullAnnotationModelTests9.class);
	}

	public NullAnnotationModelTests9(String name) {
		super(name);
	}

	static {
//		TESTS_NAMES = new String[] { "testNNBDOnOtherModule1" };
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
// TODO: switch to bundle, once it updates BREE to 9:
//		Bundle[] bundles = org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[2.2.0,3.0.0)");
//		File bundleFile = FileLocator.getBundleFile(bundles[0]);
//		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
		this.ANNOTATION_LIB = NullAnnotationTests9.createAnnotation_2_2_jar(getExternalPath(), getExternalJCLPathString("9"));
	}

	protected String testJarPath(String jarName) throws IOException {
		URL libEntry = Platform.getBundle("org.eclipse.jdt.core.tests.model").getEntry("/workspace/NullAnnotations/lib/"+jarName);
		return FileLocator.toFileURL(libEntry).getPath();
	}

	// assert that @NonNullByDefault on module mod.one is respected when regarding its API from another module
	public void testNNBDOnOtherModule1() throws CoreException, InterruptedException {
		IJavaProject p = null;
		IJavaProject p2 = null;
    	try {
			p = createJavaProject("mod.one", new String[] {"src"}, new String[] {"JCL19_LIB", this.ANNOTATION_LIB}, "bin", "9");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			createFolder("/mod.one/src/p/q");
			createFile("/mod.one/src/module-info.java",
					"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
					"module mod.one {\n" +
					"	requires org.eclipse.jdt.annotation;\n" +
					"	exports p.q;\n" +
					"}\n");

			createFile("/mod.one/src/p/q/API.java",
					"package p.q;\n" +
					"public class API {\n" +
					"	public String id(String in) { return in; }\n" +
					"}\n");

			p2 =  createJavaProject("mod.two", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			IClasspathAttribute[] attr = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(p2, JavaCore.newProjectEntry(p.getPath(), null, false, attr, false));
			addClasspathEntry(p2, JavaCore.newLibraryEntry(new Path(this.ANNOTATION_LIB), null, null, null, attr, false));
			p2.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			createFolder("/mod.two/src/client");
			createFile("/mod.two/src/module-info.java",
					"module mod.two {\n" +
					"		requires static org.eclipse.jdt.annotation;\n" +
					"		requires mod.one;\n" +
					"}\n");
			String clientSource =
					"package client;\n" +
					"import p.q.API;\n" +
					"public class Client {\n" +
					"    	void foo(API api) {\n" +
					"        api.id(api.id(\"\")); // OK\n" +
					"        api.id(null); // NOK\n" +
					"    	}\n" +
					"}\n";
			createFile("/mod.two/src/client/Client.java", clientSource);

			this.problemRequestor.initialize(clientSource.toCharArray());

			getCompilationUnit("/mod.two/src/client/Client.java").getWorkingCopy(this.wcOwner, null);

			assertProblems("Unexpected problems",
					"----------\n" +
					"1. ERROR in /mod.two/src/client/Client.java (at line 6)\n" +
					"	api.id(null); // NOK\n" +
					"	       ^^^^\n" +
					"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
					"----------\n");
    	} finally {
    		if (p != null)
    			deleteProject(p);
    		if (p2 != null)
    			deleteProject(p2);
    	}
    }

	// assert that @NonNullByDefault on module mod.one is respected when regarding its API from another module
	// - binary module
	public void testNNBDOnOtherModule2() throws CoreException, InterruptedException, IOException {
		IJavaProject p = null;
		IJavaProject p2 = null;
    	try {
			p2 =  createJavaProject("mod.two", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");

			Map<String,String> options = new HashMap<>();
    		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
    		createJar(new String[] {
    				"/mod.one/src/module-info.java",
					"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
					"module mod.one {\n" +
					"	requires org.eclipse.jdt.annotation;\n" +
					"	exports p.q;\n" +
					"}\n",
					"/mod.one/src/p/q/API.java",
					"package p.q;\n" +
					"public class API {\n" +
					"	public String id(String in) { return in; }\n" +
					"}\n"
    			},
				p2.getProject().getLocation().append("mod.one.jar").toOSString(),
    			new String[] {this.ANNOTATION_LIB, getExternalJCLPathString("9")},
    			"9",
    			options);
    		p2.getResource().refreshLocal(1, null);

			IClasspathAttribute[] attr = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(p2, JavaCore.newLibraryEntry(new Path("/mod.two/mod.one.jar"), null, null, null, attr, false));
			addClasspathEntry(p2, JavaCore.newLibraryEntry(new Path(this.ANNOTATION_LIB), null, null, null, attr, false));
			p2.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			createFolder("/mod.two/src/client");
			createFile("/mod.two/src/module-info.java",
					"module mod.two {\n" +
					"		requires static org.eclipse.jdt.annotation;\n" +
					"		requires mod.one;\n" +
					"}\n");
			String clientSource =
					"package client;\n" +
					"import p.q.API;\n" +
					"public class Client {\n" +
					"    	void foo(API api) {\n" +
					"        api.id(api.id(\"\")); // OK\n" +
					"        api.id(null); // NOK\n" +
					"    	}\n" +
					"}\n";
			createFile("/mod.two/src/client/Client.java", clientSource);

			// full build:
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers in mod.one",
					"Null type mismatch: required \'@NonNull String\' but the provided value is null",
					markers);

			// reconcile:
			this.problemRequestor.initialize(clientSource.toCharArray());
			getCompilationUnit("/mod.two/src/client/Client.java").getWorkingCopy(this.wcOwner, null);

			assertProblems("Unexpected problems",
					"----------\n" +
					"1. ERROR in /mod.two/src/client/Client.java (at line 6)\n" +
					"	api.id(null); // NOK\n" +
					"	       ^^^^\n" +
					"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
					"----------\n");
    	} finally {
    		if (p != null)
    			deleteProject(p);
    		if (p2 != null)
    			deleteProject(p2);
    	}
    }
}
