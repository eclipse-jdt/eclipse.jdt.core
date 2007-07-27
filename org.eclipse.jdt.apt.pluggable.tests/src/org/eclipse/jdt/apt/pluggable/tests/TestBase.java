package org.eclipse.jdt.apt.pluggable.tests;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;

public class TestBase extends BuilderTests
{

	protected static final String JAVA_16_COMPLIANCE = "1.6";
	
	protected String _projectName;
	protected static int _projectSerial = 0; // used to create unique project names, to avoid resource deletion problems
	
	public TestBase(String name) {
		super(name);
	}

	/**
	 * Create a java project with java libraries and test annotations on classpath
	 * (compiler level is 1.6). Use "src" as source folder and "bin" as output folder.
	 * APT is not enabled.
	 * 
	 * @param projectName
	 * @return a java project that has been added to the current workspace.
	 * @throws Exception
	 */
	protected IJavaProject createJavaProject(final String projectName) throws Exception
	{
			IPath projectPath = env.addProject( projectName, JAVA_16_COMPLIANCE );
			env.addExternalJars( projectPath, Util.getJavaClassLibs() );
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$
			env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
			env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$
			final IJavaProject javaProj = env.getJavaProject( projectPath );
	//		TestUtil.createAndAddAnnotationJar( javaProj );
			return javaProj;
		}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_projectName = String.format("testproj%04d", ++_projectSerial);
	}
	
}