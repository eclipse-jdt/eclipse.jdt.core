/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.*;
import junit.framework.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class ParticipantBuildTests extends Tests {
	public ParticipantBuildTests(String name) {
		super(name);
	}

	public void tearDown() throws Exception {
		TestBuilderParticipant.PARTICIPANT = null;
		super.tearDown();
	}

	public static Test suite() {
		if (false) {
			TestSuite suite = new TestSuite(ParticipantBuildTests.class.getName());
			suite.addTest(new ParticipantBuildTests("testTags"));
			return suite;
		}
		return new TestSuite(ParticipantBuildTests.class);
	}

	static class BuildTestParticipant extends CompilationParticipant {
		BuildTestParticipant() {
			TestBuilderParticipant.PARTICIPANT = this;
		}
	}

	public void testBuildStarting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Test extends GeneratedType {}\n" //$NON-NLS-1$
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			boolean createFile = true;
			public void buildStarting(ICompilationParticipantResult[] files, boolean isBatchBuild) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				if (!this.createFile) return;
				this.createFile = false;
				ICompilationParticipantResult result = files[0];
				IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedType.java")); //$NON-NLS-1$
				try {
					genedType.create(new ByteArrayInputStream("public class GeneratedType {}".getBytes()), true, null); //$NON-NLS-1$
				} catch (CoreException e) {
					e.printStackTrace();
				}
				result.recordAddedGeneratedFiles(new IFile[] {genedType});
			}
		};
		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testProcessAnnotationDeclarations() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"@interface TestAnnotation {}\n" + //$NON-NLS-1$
			"public class Test extends GeneratedType {}\n" //$NON-NLS-1$
			);

		env.addClass(root, "", "Other", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Other { MissingAnnotation m; }\n" //$NON-NLS-1$
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			int count = 2;
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(ICompilationParticipantResult[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				if (this.count == 2) {
					this.count--;
					ICompilationParticipantResult result = files[0];
					IFile genedType = result.getFile().getParent().getFile(new Path("MissingAnnotation.java")); //$NON-NLS-1$
					try {
						genedType.create(new ByteArrayInputStream("public @interface MissingAnnotation {}".getBytes()), true, null); //$NON-NLS-1$
					} catch (CoreException e) {
						e.printStackTrace();
					}
					result.recordAddedGeneratedFiles(new IFile[] {genedType});
				} else if (this.count == 1) {
					this.count--;
					ICompilationParticipantResult result = files[0];
					IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedType.java")); //$NON-NLS-1$
					try {
						genedType.create(new ByteArrayInputStream("public class GeneratedType {}".getBytes()), true, null); //$NON-NLS-1$
					} catch (CoreException e) {
						e.printStackTrace();
					}
					result.recordAddedGeneratedFiles(new IFile[] {genedType});
				}
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testProcessAnnotationReferences() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"@GeneratedAnnotation\n" + //$NON-NLS-1$
			"public class Test {}\n" //$NON-NLS-1$
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(ICompilationParticipantResult[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				ICompilationParticipantResult result = files[0];
				IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedAnnotation.java")); //$NON-NLS-1$
				if (genedType.exists()) return;
				try {
					genedType.create(new ByteArrayInputStream("@interface GeneratedAnnotation {}".getBytes()), true, null); //$NON-NLS-1$
				} catch (CoreException e) {
					e.printStackTrace();
				}
				result.recordAddedGeneratedFiles(new IFile[] {genedType});
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testProcessAnnotationReferences2() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"@GeneratedAnnotation\n" + //$NON-NLS-1$
			"public class Test extends p.GeneratedType {}\n" //$NON-NLS-1$
			);

		env.addClass(root, "", "GeneratedAnnotation", //$NON-NLS-1$ //$NON-NLS-2$
			"@interface GeneratedAnnotation{}\n"
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(ICompilationParticipantResult[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				ICompilationParticipantResult result = files[0];
				if (!"Test.java".equals(result.getFile().getName())) return; //$NON-NLS-1$
				IFile genedType = null;
				try {
					IFolder genedFolder = result.getFile().getParent().getFolder(new Path("p"));
					if (!genedFolder.exists())
						genedFolder.create(true, false, null);
					genedType = genedFolder.getFile(new Path("GeneratedType.java")); //$NON-NLS-1$
					if (genedType.exists()) return;
					genedType.create(new ByteArrayInputStream("package p; public class GeneratedType {}".getBytes()), true, null); //$NON-NLS-1$
				} catch (CoreException e) {
					e.printStackTrace();
				}
				result.recordAddedGeneratedFiles(new IFile[] {genedType});
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}
}
