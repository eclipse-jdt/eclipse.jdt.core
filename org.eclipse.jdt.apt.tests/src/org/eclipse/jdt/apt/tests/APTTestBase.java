/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

/** 
 * Setup a project for common APT testing.
 */
public abstract class APTTestBase extends Tests{
	
	public APTTestBase(final String name)
	{
		super(name);
	}
	
	/**
	 * Set up a basic project with the following properties.
	 * - java compliances level is 1.5  
	 * - 'src' is the source folder
	 * - 'bin' is the output folder	  
	 * - add java class library into teh build class path
	 * - create and add an annotation jar.
	 */
	public void setUp() throws Exception
	{	
		ProcessorTestStatus.reset();
		
		super.setUp();

		env.resetWorkspace();
		TestUtil.enableAutoBuild(false);

		// project will be deleted by super-class's tearDown() method
		final String projectName = getProjectName();
		if( projectName == null )
			throw new IllegalStateException();
		IPath projectPath = env.addProject( getProjectName(), "1.5" );
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		//fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$
		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$
		
		AptConfig.setEnabled(null, true);
		TestUtil.createAndAddAnnotationJar( env.getJavaProject( projectPath ) );
	}
	
	protected void tearDown()
		throws Exception
	{
		AptPlugin.trace("Tearing down " + getProjectName() );
		super.tearDown();
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
	
	protected void expectingMarkers(String[] messages)
	{	
		final IMarker[] markers = getAPTBuildMarkerFor(env.getWorkspaceRootPath());
		final Set<String> expectedMessages = new HashSet<String>();
		for(String msg : messages ){
			expectedMessages.add(msg);
		}
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
		final IMarker[] markers = getAPTBuildMarkerFor(path);
		
		if( markers != null && markers.length != 0 ){
			try{
				assertTrue("unexpected marker(s) : " + markers[0].getAttribute(IMarker.MESSAGE), false); //$NON-NLS-1$
			}
			catch(CoreException ce){
				assertTrue("unexpected core exception" + ce.getMessage(), false); //$NON-NLS-1$
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected IMarker[] getAPTBuildMarkerFor(IPath path){
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
			return resource.findMarkers("org.eclipse.jdt.apt.core.marker", true, IResource.DEPTH_INFINITE);
		} catch(CoreException e){
			return null;
		}
	}
}
