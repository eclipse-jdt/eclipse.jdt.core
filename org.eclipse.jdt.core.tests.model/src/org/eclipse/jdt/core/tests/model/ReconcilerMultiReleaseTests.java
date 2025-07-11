package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

public class ReconcilerMultiReleaseTests extends ModifyingResourceTests {

	public static Test suite() {
		return buildModelTestSuite(ReconcilerMultiReleaseTests.class);
	}

	private ProblemRequestor problemRequestor;
	private ICompilationUnit workingCopy;

	public ReconcilerMultiReleaseTests(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.problemRequestor = new ProblemRequestor();
		this.wcOwner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return ReconcilerMultiReleaseTests.this.problemRequestor;
			}
		};
		this.workingCopy = getCompilationUnit("ReconcilerMR/src21/p/D.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		startDeltas();
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		IJavaProject mrproject = createJavaProject("ReconcilerMR", new String[] { "src", "src9", "src21" },
				new String[] { "JCL18_LIB" }, "bin");
		createFolder("/ReconcilerMR/src/p");
		createFolder("/ReconcilerMR/src9/p");
		createFolder("/ReconcilerMR/src21/p");
		IClasspathEntry[] classpath = mrproject.getRawClasspath();
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if (entry.getPath().toString().endsWith("src9")) {
					classpath[i]= JavaCore.newSourceEntry(entry.getPath(), null, null, null,
							new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE,
									org.eclipse.jdt.core.JavaCore.VERSION_9) });
				} else if (entry.getPath().toString().endsWith("src21")) {
					classpath[i] = JavaCore.newSourceEntry(entry.getPath(), null, null, null,
							new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE,
									org.eclipse.jdt.core.JavaCore.VERSION_21) });
				}
			}
		}
		mrproject.setRawClasspath(classpath, new NullProgressMonitor());
		createFile("/ReconcilerMR/src/p/A.java", """
				package p;
				public class A {

				}
				""");
		createFile("/ReconcilerMR/src/p/B.java", """
				package p;
				public class B {

				}
				""");
		createFile("/ReconcilerMR/src9/p/B.java", """
				package p;
				public class B {
					A fieldA;
				}
				""");
		createFile("/ReconcilerMR/src9/p/C.java", """
				package p;
				class C {

				}
				""");
		createFile("/ReconcilerMR/src21/p/B.java", """
				package p;
				public class B {
					A fieldA;
					C fieldC;
					public void m() {
						// nop
					}
				}
				""");
		createFile("/ReconcilerMR/src21/p/D.java", """
					package p;
					public class D {
						void m(B b) {
							b.m();
						}
					}
				""");
		mrproject.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		mrproject.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
		mrproject.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
	}

	@Override
	public void tearDown() throws Exception {
		TestCompilationParticipant.PARTICIPANT = null;
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
		}
		stopDeltas();
		super.tearDown();
	}
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("ReconcilerMR");
		super.tearDownSuite();
	}

	public void testNoErrorsAfterEdit() throws CoreException, InterruptedException {
		String contents = """
				package p;
				public class D {
					void m(B b) {
						// Calling method m only available from Java 21+
						b.m();
					}
				}
			""";
		setWorkingCopyContents(contents);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, false, null, null);
		assertNoProblem(contents.toCharArray(), this.workingCopy);
		clearDeltas();
	}

	void setWorkingCopyContents(String contents) throws JavaModelException {
		this.workingCopy.getBuffer().setContents(contents);
		this.problemRequestor.initialize(contents.toCharArray());
	}

	protected void assertNoProblem(char[] source, ICompilationUnit unit) throws InterruptedException, JavaModelException {
		IndexManager indexManager = JavaModelManager.getIndexManager();
		if (this.problemRequestor.problemCount > 0) {
			// If errors then wait for indexes to finish
			while (indexManager.awaitingJobsCount() > 0) {
				waitUntilIndexesReady();
			}
			// Reconcile again to see if error goes away
			this.problemRequestor.initialize(source);
			unit.getBuffer().setContents(source); // need to set contents again to be sure that following reconcile will be really done
			unit.reconcile(AST.getAllSupportedVersions().getFirst(),
				true, // force problem detection to see errors if any
				null,	// do not use working copy owner to not use working copies in name lookup
				null);
			if (this.problemRequestor.problemCount > 0) {
				assertEquals("Working copy should NOT have any problem!", "", this.problemRequestor.problems.toString());
			}
		}
	}

}
