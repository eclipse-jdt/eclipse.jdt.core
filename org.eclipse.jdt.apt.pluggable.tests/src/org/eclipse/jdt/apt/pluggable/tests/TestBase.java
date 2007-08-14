package org.eclipse.jdt.apt.pluggable.tests;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
	 * Extract lib/annotations.jar from the test bundle and add it to the specified project
	 */
	private static void addAnnotationJar(IJavaProject jproj) throws Exception {
		final String resName = "lib/annotations.jar"; // name in bundle
		final String libName = resName; // name in destination project
		InputStream is = null;
		URL resURL = Apt6TestsPlugin.thePlugin().getBundle().getEntry(resName);
		is = resURL.openStream();
		IPath projPath = jproj.getPath();
		IProject proj = jproj.getProject();
		IFile libFile = proj.getFile(libName);
		env.addFolder(projPath, "lib");
		if (libFile.exists()) {
			libFile.setContents(is, true, false, null);
		} else {
			libFile.create(is, true, null);
		}
		env.addLibrary(projPath, libFile.getFullPath(), null, null);
	}
	
	/**
	 * Create a java project with java libraries and test annotations on classpath
	 * (compiler level is 1.6). Use "src" as source folder and "bin" as output folder. APT
	 * is not enabled.
	 * 
	 * @param projectName
	 * @return a java project that has been added to the current workspace.
	 * @throws Exception
	 */
	protected static IJavaProject createJavaProject(final String projectName) throws Exception
	{
		IPath projectPath = env.addProject(projectName, JAVA_16_COMPLIANCE);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		final IJavaProject javaProj = env.getJavaProject(projectPath);
		addAnnotationJar(javaProj);
		return javaProj;
	}
	
	/**
	 * Verify that an expected file exists within a project.
	 * @param fileName the filename relative to the project root.
	 */
	protected void expectingFile(IProject proj, String fileName) throws Exception
	{
		IPath path = proj.getLocation().append(fileName);
		File file = new File(path.toOSString());
		assertTrue("Expected file " + fileName + " was missing from project", file != null && file.exists());
	}
	
	/**
	 * Verify that an expected file exists within a project.
	 * @param fileName the filename relative to the project root.
	 */
	protected void expectingNoFile(IProject proj, String fileName) throws Exception
	{
		IPath path = proj.getLocation().append(fileName);
		File file = new File(path.toOSString());
		assertTrue("File " + fileName + " was expected to not exist", file == null || !file.exists());
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_projectName = String.format("testproj%04d", ++_projectSerial);
	}
	
}