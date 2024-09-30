package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;

public class JavaModelManagerTests extends AbstractJavaModelTests {

	private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();
	private static final String PROJECT_NAME = JavaModelManagerTests.class.getSimpleName();

	public static Test suite() {
		return buildModelTestSuite(JavaModelManagerTests.class);
	}

	public JavaModelManagerTests(String name) {
		super(name);
	}

	/**
	 * Test for Bug 548456 - Concurrency problem with JavaModelManager and JavaElement.openWhenClosed()
	 *
	 * We create a project with a jar on its class path and ask after {@link JavaElement#exists()}
	 * for a class in the jar. We do so from 2 different threads in parallel; with sufficient repetitions
	 * we hope to run into the race condition described in the bug.
	 */
	public void testBug548456_concurrentElementInfoAccess() throws Exception {
		int iterations = 20;
		int numberOfThreads = 10;
		for (int i = 0; i < iterations; ++i) {
			doTestBug548456_concurrentCallBinaryTypeExists(numberOfThreads);
		}
	}

	private void doTestBug548456_concurrentCallBinaryTypeExists(int numberOfThreads) throws Exception {
		final IJavaProject project = setUpJavaProject(PROJECT_NAME);
		try {
			buildProject(project);
			assertHasNoBuildProblems(project);

			CountDownLatch latch = new CountDownLatch(numberOfThreads);
			List<CheckIfTypeExists> runnables = new ArrayList<>(numberOfThreads);
			for (int i = 0; i < numberOfThreads; ++i) {
				CheckIfTypeExists runnable = new CheckIfTypeExists(project, latch);
				runnables.add(runnable);
			}

			List<Thread> threads = startThreads(runnables);

			boolean interrupted = false;
			for (Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					// we join all threads regardless of interruption, to be sure we don't leave running threads after this method done
					interrupted = true;
				}
			}
			assertFalse("waiting on test threads to finish must not be interrupted", interrupted);

			for (CheckIfTypeExists runnable : runnables) {
				assertTrue("expected type for test source to exist, despite concurrent access to JavaModelManager", runnable.typeExists);
			}
		} finally {
			deleteProject(PROJECT_NAME);
		}
	}

	private static List<Thread> startThreads(List<CheckIfTypeExists> runnables) {
		List<Thread> threads = new ArrayList<>(runnables.size());
		for (CheckIfTypeExists runnable : runnables) {
			Thread thread = new Thread(runnable);
			threads.add(thread);
			thread.start();
		}
		return threads;
	}

	static class CheckIfTypeExists implements Runnable {

		private final IJavaProject project;
		private final CountDownLatch latch;

		boolean typeExists = false;

		CheckIfTypeExists(IJavaProject project, CountDownLatch latch) {
			this.project = project;
			this.latch = latch;
		}

		@Override
		public void run() {
			IType type = retrieveTestType(this.project);
			// we use a CountDownLatch to ensure the thread calls to IJavaElement.exists() are running as close together as possible
			this.latch.countDown();
			this.typeExists = type.exists();
		}
	}

	static IType retrieveTestType(IJavaProject project) {
		IFile jarResource = project.getProject().getFile("lib.jar");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(jarResource);
		IPackageFragment packageFragment = root.getPackageFragment("p");
		IClassFile classFile = packageFragment.getClassFile("X.class");
		IOrdinaryClassFile ordinaryClassFile = (IOrdinaryClassFile) classFile;
		IType type = ordinaryClassFile.getType();
		return type;
	}

	private void buildProject(IJavaProject javaProject) throws Exception {
		IProject project = javaProject.getProject();
		refreshProject(project);
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, NULL_MONITOR);
		refreshProject(project);
		project.build(IncrementalProjectBuilder.FULL_BUILD, NULL_MONITOR);
		Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
		JavaModelManager.getIndexManager().waitForIndex(isIndexDisabledForTest(), null);
		refreshProject(project);
	}

	private void refreshProject(IProject project) throws CoreException {
		project.refreshLocal(IResource.DEPTH_INFINITE, NULL_MONITOR);
		waitForManualRefresh();
	}

	private static void assertHasNoBuildProblems(IJavaProject javaProject) throws Exception {
		IProject project = javaProject.getProject();
		List<IMarker> modelProblems = getTestProjectErrorMarkers(project);
		assertTrue("expected no problems when building, but got:" + System.lineSeparator() + toString(modelProblems),
				modelProblems.isEmpty());
	}

	private static List<IMarker> getTestProjectErrorMarkers(IProject project) throws Exception {
		IMarker[] problemMarkers = project.findMarkers(null, true, IResource.DEPTH_ZERO);
		List<IMarker> errorMarkers = new ArrayList<>();
		for (IMarker problemMarker : problemMarkers) {
			int severity = problemMarker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			if (severity == IMarker.SEVERITY_ERROR) {
				errorMarkers.add(problemMarker);
			}
		}
		return errorMarkers;
	}

	private static String toString(List<IMarker> markers) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (IMarker marker : markers) {
			sb.append("Message: ");
			sb.append((marker.getAttribute(IMarker.MESSAGE)));
			sb.append(System.lineSeparator());
			sb.append("Location: ");
			sb.append(marker.getAttribute(IMarker.LOCATION));
			sb.append(System.lineSeparator());
			sb.append("Line: ");
			sb.append(marker.getAttribute(IMarker.LINE_NUMBER));
			sb.append(System.lineSeparator());
		}
		String fatalProblemsAsText = sb.toString();
		return fatalProblemsAsText;
	}
}
