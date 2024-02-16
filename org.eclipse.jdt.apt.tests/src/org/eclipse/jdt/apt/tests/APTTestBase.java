/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.generic.GenericFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.Util;

import com.sun.mirror.apt.AnnotationProcessor;

/**
 * Setup a project for common APT testing.
 */
public abstract class APTTestBase extends BuilderTests{

	private IJavaProject _jproj;

	public APTTestBase(final String name)
	{
		super(name);
	}

	/**
	 * Set up a basic project with the following properties.
	 * - java compliance level is 1.5
	 * - 'src' is the source folder
	 * - 'bin' is the output folder
	 * - add java class library into the build class path
	 * - create and add an annotation jar.
	 */
	public void setUp() throws Exception
	{
		runFinalizers();
		ProcessorTestStatus.reset();
		TestUtil.enableAutoBuild(false);

		super.setUp();

		env.resetWorkspace();
		// project will be deleted by super-class's tearDown() method
		final String projectName = getProjectName();
		if( projectName == null ) {
			throw new IllegalStateException();
		}
		AptPlugin.cleanProjectCache();
		_jproj = createJavaProject(projectName);
		AptConfig.setEnabled(_jproj, true);
		assertTrue(AptConfig.isEnabled(_jproj));
		TestUtil.waitForBuildEvents();
	}

	/**
	 * @return the java project created in setUp().  Note that some tests may
	 * create more than one project; this method only returns the one named
	 * by getProjectName().
	 */
	protected IJavaProject getCurrentJavaProject() {
		return _jproj;
	}

	/**
	 * Create a java project with java libraries and test annotations on classpath
	 * (compiler level is 1.5). Use "src" as source folder and "bin" as output folder.
	 * APT is not enabled.
	 *
	 * @return a java project that has been added to the current workspace.
	 */
	protected IJavaProject createJavaProject(final String projectName )
		throws Exception
	{
		IPath projectPath = env.addProject( projectName, "1.5" );
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$
		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$
		final IJavaProject javaProj = env.getJavaProject( projectPath );
		TestUtil.createAndAddAnnotationJar( javaProj );
		return javaProj;
	}

	/**
	 * Add a test source folder to the current project and return it
	 * @return IPath
	 */
	protected IPath addTestSourceFolder() throws JavaModelException {
		IJavaProject currentJavaProject = getCurrentJavaProject();
		return env.addPackageFragmentRoot(currentJavaProject.getPath(), "src-tests", null, null,
				"bin-tests", true);
	}

	protected void tearDown()
		throws Exception
	{
		AptPlugin.trace("Tearing down " + getProjectName() );
		runFinalizers();
		GenericFactory.PROCESSOR = null;
		super.tearDown();
	}

	private static void runFinalizers() {
        // GC in an attempt to release file lock on Classes.jar
		System.gc();
		System.runFinalization();
		System.gc();
		System.runFinalization();
	}

	public String getProjectName()
	{
		return this.getClass().getName() + "Project"; //$NON-NLS-1$
	}

	public IPath getSourcePath()
	{
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}

	private String concate(String[] messages){
		final int len = messages == null ? 0 : messages.length;
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<len; i++ ){
			buffer.append(messages[i]);
			buffer.append('\n');
		}
		return buffer.toString();
	}

	private String concate(IMarker[] markers){
		final int len = markers == null ? 0 : markers.length;
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<len; i++ ){
			try{
				buffer.append(markers[i].getAttribute(IMarker.MESSAGE));
			}
			catch(CoreException ce){
				assertTrue("unexpected core exception" + ce.getMessage(), false); //$NON-NLS-1$
			}
			buffer.append('\n');
		}
		return buffer.toString();
	}

	protected void clearProcessorResult(Class<? extends AnnotationProcessor> processor) {
		String propertyName = BaseProcessor.getPropertyName(processor);
		System.clearProperty(propertyName);
	}

	/*
	 * Processors can set a result message with BaseProcessor.reportError() or reportSuccess().
	 * This method will cause the test to fail if the processor reported an error.  The result
	 * value will be cleared regardless of success or failure.
	 */
	protected String checkProcessorResult(Class<? extends AnnotationProcessor> processor) {
		String propertyName = BaseProcessor.getPropertyName(processor);
		String result = System.getProperty(propertyName);
		System.clearProperty(propertyName);
		if (!BaseProcessor.SUCCESS.equals(result)) {
			fail(result);
		}
		return result;
	}

	/*
	 * Processors can set a result message with BaseProcessor.reportError() or reportSuccess().
	 * This method returns the message reported by the processor, and clears the result value.
	 */
	protected String getProcessorResult(Class<? extends AnnotationProcessor> processor) {
		String propertyName = BaseProcessor.getPropertyName(processor);
		String result = System.getProperty(propertyName);
		System.clearProperty(propertyName);
		return result;
	}

	protected void expectingMarkers(String[] messages)
	{
		final IMarker[] markers = getAllAPTMarkers(env.getWorkspaceRootPath());
		final Set<String> expectedMessages = Stream.of(messages).collect(Collectors.toSet());
		boolean fail = false;
		try{
			for( IMarker marker : markers ){
				final String markerMsg = (String)marker.getAttribute(IMarker.MESSAGE);
				if( expectedMessages.contains(markerMsg) )
					expectedMessages.remove(markerMsg);
				else{
					fail = true;
					break;
				}
			}
			if( !expectedMessages.isEmpty() )
				fail = true;
		}catch(CoreException ce){
			assertTrue("unexpected core exception" + ce.getMessage(), false); //$NON-NLS-1$
		}
		if( fail )
			assertEquals(concate(messages), concate(markers));
	}

	protected void expectingNoMarkers() {
		expectingNoMarkers(env.getWorkspaceRootPath());
	}

	protected void expectingNoMarkers(IPath path)
	{
		final IMarker[] markers = getAllAPTMarkers(path);

		if( markers != null && markers.length != 0 ){
			try{
				assertTrue("unexpected marker(s) : " + markers[0].getAttribute(IMarker.MESSAGE), false); //$NON-NLS-1$
			}
			catch(CoreException ce){
				assertTrue("unexpected core exception" + ce.getMessage(), false); //$NON-NLS-1$
			}
		}
	}

	protected IMarker[] getAllAPTMarkers(IPath path){
		IResource resource;
		if(path.equals(env.getWorkspaceRootPath())){
			resource = env.getWorkspace().getRoot();
		} else {
			IProject p = env.getProject(path);
			if(p != null && path.equals(p.getFullPath())) {
				resource = env.getProject(path.lastSegment());
			} else if(path.getFileExtension() == null) {
				resource = env.getWorkspace().getRoot().getFolder(path);
			} else {
				resource = env.getWorkspace().getRoot().getFile(path);
			}
		}
		try {
			IMarker[] markers = null;
			int total = 0;
			final IMarker[] processorMarkers = resource.findMarkers(AptPlugin.APT_BATCH_PROCESSOR_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			total = processorMarkers.length;
			markers = processorMarkers;

			final IMarker[] factoryPathMarkers = resource.findMarkers(AptPlugin.APT_LOADER_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( factoryPathMarkers.length != 0 ){
				if( total != 0 ){
					final int len = factoryPathMarkers.length;
					final IMarker[] temp = new IMarker[len + total ];
					System.arraycopy(markers, 0, temp, 0, total);
					System.arraycopy(factoryPathMarkers, 0, temp, total, len);
					markers = temp;
					total += len;
				}
				else
					markers = factoryPathMarkers;
			}
			final IMarker[] configMarkers = resource.findMarkers(AptPlugin.APT_CONFIG_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( configMarkers.length != 0 ){
				if( total != 0 ){
					final int len = configMarkers.length;
					final IMarker[] temp = new IMarker[len + total];
					System.arraycopy(markers, 0, temp, 0, total);
					System.arraycopy(configMarkers, 0, temp, total, len);
					markers = temp;
					total += len;
				}
				else
					markers = configMarkers;
			}
			return markers;
		} catch(CoreException e){
			return null;
		}
	}

	/**
	 * Verifies that the given element has specifics problems and
	 * only the given problems.
	 */
	protected void expectingOnlySpecificProblemsFor(IPath root, ExpectedProblem[] expectedProblems) {
		if (DEBUG)
			printProblemsFor(root);

		Problem[] rootProblems = env.getProblemsFor(root);

		for (int i = 0; i < expectedProblems.length; i++) {
			ExpectedProblem expectedProblem = expectedProblems[i];
			boolean found = false;
			for (int j = 0; j < rootProblems.length; j++) {
				if(expectedProblem.equalsProblem(rootProblems[j])) {
					found = true;
					rootProblems[j] = null;
					break;
				}
			}
			if (!found) {
				printProblemsFor(root);
			}
			assertTrue("problem not found: " + expectedProblem.toString(), found); //$NON-NLS-1$
		}
		for (int i = 0; i < rootProblems.length; i++) {
			if(rootProblems[i] != null) {
				printProblemsFor(root);
				assertTrue("unexpected problem: " + rootProblems[i].toString(), false); //$NON-NLS-1$
			}
		}
	}

	/** Verifies that the given element has specific problems.
	 */
	protected void expectingSpecificProblemsFor(IPath root, ExpectedProblem[] problems) {
		if (DEBUG)
			printProblemsFor(root);

		Problem[] rootProblems = env.getProblemsFor(root);
		next : for (int i = 0; i < problems.length; i++) {
			ExpectedProblem problem = problems[i];
			for (int j = 0; j < rootProblems.length; j++) {
				Problem rootProblem = rootProblems[j];
				if (rootProblem != null) {
					if (problem.equalsProblem(rootProblem)) {
						rootProblems[j] = null;
						continue next;
					}
				}
			}
			for (int j = 0; j < rootProblems.length; j++) {
				Problem pb = rootProblems[j];
				if (pb == null) continue;
				System.out.print("got pb:		new Problem(\"" + pb.getLocation() + "\", \"" + pb.getMessage() + "\", \"" + pb.getResourcePath() + "\"");
				System.out.print(", " + pb.getStart() + ", " + pb.getEnd() +  ", " + pb.getCategoryId());
				System.out.println(")");
			}
			assertTrue("missing expected problem : " + problem, false);
		}
	}

	/** Verifies that the given element has a specific problem and
	 * only the given problem.
	 */
	protected void expectingOnlySpecificProblemFor(IPath root, ExpectedProblem problem) {
		expectingOnlySpecificProblemsFor(root, new ExpectedProblem[] { problem });
	}

	protected static void sleep( long millis )
	{
		long end = System.currentTimeMillis() + millis;
		while ( millis > 0 )
		{
			try
			{
				Thread.sleep( millis );
			}
			catch ( InterruptedException ie )
			{}
			millis = end - System.currentTimeMillis();
		}
	}



}
