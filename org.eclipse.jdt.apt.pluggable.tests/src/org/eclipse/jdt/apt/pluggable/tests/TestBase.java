package org.eclipse.jdt.apt.pluggable.tests;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.util.AptConfig;
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
	 * Ensure that there are no Java 5 processors on the factory path, as they can cause
	 * units to be multiply compiled, which can mess up tests that expect a certain number
	 * of compilations to occur.
	 * @param jproj the project whose factory path will be edited
	 * @throws CoreException
	 */
	protected void disableJava5Factories(IJavaProject jproj) throws CoreException {
		FactoryPath fp = (FactoryPath) AptConfig.getFactoryPath(jproj);
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : fp.getAllContainers().entrySet()) {
			if (entry.getKey().getType() == FactoryType.PLUGIN) {
				String id = entry.getKey().getId();
				if (!Apt6TestsPlugin.PLUGIN_ID.equals(id)) {
					fp.disablePlugin(id);
				}
			}
		}
		AptConfig.setFactoryPath(jproj, fp);
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
		boolean exists = file.exists();
		// work around a timing bug in some versions of JRE 1.6 on Linux:
		// Before assuming the test has failed, wait half a second and try again.
		// This delay is not encountered when the test is passing normally.
		if (exists) {
			Thread.sleep(500);
			exists = file.exists();
		}
		assertTrue("File " + fileName + " was expected to not exist", file == null || !exists);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		env.setAutoBuilding(false);
		_projectName = String.format("testproj%04d", ++_projectSerial);
	}
	
}