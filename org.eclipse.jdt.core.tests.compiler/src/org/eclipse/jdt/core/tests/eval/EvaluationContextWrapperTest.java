/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.eval.EvaluationContextWrapper;
import org.eclipse.jdt.internal.eval.InstallException;

import junit.framework.Test;

public class EvaluationContextWrapperTest extends EvaluationTest {

	private static final String SOURCE_DIRECTORY = "src";
	private static final String BIN_DIR = "bin";

	private IJavaProject project;

	static {
//		TESTS_NAMES = new String[] { "testBug573589_StaticImport" };
	}

	public EvaluationContextWrapperTest(String name) {
		super(name);
	}

	public static Test setupSuite(Class clazz) {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(clazz);
		return buildAllCompliancesTestSuite(clazz, DebugEvaluationSetup.class, testClasses);
	}
	public static Test suite() {
		return setupSuite(testClass());
	}
	public static Class<?> testClass() {
		return EvaluationContextWrapperTest.class;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.project = createProject("EvaluationContextWrapperTest");
	}

	@Override
	protected void tearDown() throws Exception {
		delete(this.project);
		this.project = null;
		super.tearDown();
	}

	public void testBug573589_StaticImport() throws JavaModelException, InstallException {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			return;
		}
		try {
			String str = """
				import static java.lang.Math.max;
				class Bug573589 {
						public int fooMax() {
							return max(10, 11);
						}
						public static void call() {
							new Bug573589().fooMax();
						}
				}
				""";
			compileAndDeploy15(str, "Bug573589");
			refreshProject();

			Optional<IMarker> problem = evaluate("Bug573589", "1+1");
			assertTrue("Evaluation should not have problems : "
					.concat(problem.map(p -> p.getAttribute(IMarker.MESSAGE, "")).orElse("")),
					problem.isEmpty());
		} finally {
			removeTempClass("Bug573589");
		}
	}

	public void testBug573589_StaticImport_AttachedSource() throws JavaModelException, InstallException {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			return;
		}
		try {
			String str = """
				import static java.lang.Math.max;
				class Bug573589 {
						public int fooMax() {
							return max(10, 11);
						}
						public static void call() {
							new Bug573589().fooMax();
						}
				}
				""";
			Map<String, String> result = compileAndDeploy15(str, "Bug573589", "attached");
			addLibrary(this.project, result.get(BIN_DIR), result.get(SOURCE_DIRECTORY));
			refreshProject();

			Optional<IMarker> problem = evaluate("Bug573589", "1+1");
			assertTrue("Evaluation should not have problems : "
					.concat(problem.map(p -> p.getAttribute(IMarker.MESSAGE, "")).orElse("")),
					problem.isEmpty());
		} finally {
			removeTempClass("Bug573589", "attached");
		}
	}

	private void compileAndDeploy15(String source, String className) {
		compileAndDeploy15(source, className, "");
	}

	private Map<String, String> compileAndDeploy15(String source, String className, String locationPrefix) {
		resetEnv(); // needed to reinitialize the caches
		String srcDir = this.project.getProject().getLocation().toFile().getAbsolutePath() + File.separator + SOURCE_DIRECTORY.concat(locationPrefix);
		String binDir = this.project.getProject().getLocation().toFile().getAbsolutePath() + File.separator + BIN_DIR.concat(locationPrefix);
		Map<String, String> result = new HashMap<>();
		result.put(SOURCE_DIRECTORY, srcDir);
		result.put(BIN_DIR, binDir);

		File directory = new File(srcDir);
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				System.out.println("Could not create " + srcDir);
				return result;
			}
		}

		String fileName = srcDir + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
			return result;
		}
		StringBuilder buffer = new StringBuilder();
		buffer
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(binDir)
			.append("\" -nowarn -1.5 -g -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(srcDir)
			.append("\"");
		BatchCompiler.compile(buffer.toString(), new PrintWriter(System.out), new PrintWriter(System.err), null/*progress*/);
		return result;
	}

	private void refreshProject() {
		try {
			this.project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	private void removeTempClass(String className) {
		removeTempClass(className, "");
	}

	private void removeTempClass(String className, String locationPrefix) {
		resetEnv();
		String srcDir = this.project.getProject().getLocation().toFile().getAbsolutePath() + File.separator + SOURCE_DIRECTORY.concat(locationPrefix);
		String binDir = this.project.getProject().getLocation().toFile().getAbsolutePath() + File.separator + BIN_DIR.concat(locationPrefix);

		Util.delete(srcDir + File.separator + className + ".java");
		Util.delete(binDir + File.separator + className + ".class");
	}

	private Optional<IMarker> evaluate(String declaringTypeName, String snippet) throws InstallException, JavaModelException {
		IType type = this.project.findType(declaringTypeName);
		assertNotNull("declaringType is not compiled", type);

		String source = type.getSource();
		assertNotNull("declaringType source mapper is not ready.", source);
		assertFalse("declaringType source mapper is not ready.", source.isEmpty());

		IMarker[] problem = new IMarker[1];
		ICodeSnippetRequestor requestor = new ICodeSnippetRequestor() {

			@Override
			public void acceptProblem(IMarker problemMarker, String fragmentSource, int fragmentKind) {
				if (problemMarker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) >= IMarker.SEVERITY_ERROR) {
					problem[0] = problemMarker;
				}
			}

			@Override
			public boolean acceptClassFiles(byte[][] classFileBytes, String[][] classFileCompoundNames,
					String codeSnippetClassName) {
				return true;
			}
		};

		EvaluationContextWrapper wrapper = new EvaluationContextWrapper(this.context, (JavaProject) this.project);
		wrapper.evaluateCodeSnippet(
			snippet,
			new String[0],
			new String[0],
			new int[0],
			type,
			true,
			false,
			requestor,
			null);
		return Optional.ofNullable(problem[0]);
	}

	@Override
	public Map<String, String> getCompilerOptions() {
		Map<String, String> options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE);
		return options;
	}

	private IJavaProject createProject(String name) throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject proj = root.getProject(name);
		if (proj.exists()) {
        	try {
        		proj.delete(true, true, null);
        	}
        	catch(Exception e) {}
        }
		proj = root.getProject(name);
		proj.create(null);
		proj.open(null);

		// disable build and set nature
		IProjectDescription description = proj.getDescription();
		description.setNatureIds(new String[] {JavaCore.NATURE_ID});
		description.setBuildConfigs(new String[0]);
		description.setBuildSpec(new ICommand[0]);
		proj.setDescription(description, IResource.FORCE, null);

		IFolder binFolder= proj.getFolder(BIN_DIR);
		if (!binFolder.exists()) {
			binFolder.create(false, true, null);
		}
		IPath outputLocation = binFolder.getFullPath();
		IJavaProject jproject= JavaCore.create(proj);
		jproject.setRawClasspath(new IClasspathEntry[0], null);
		jproject.setOutputLocation(outputLocation, null);

		Map<String, String> map = JavaCore.getOptions();
		map.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		map.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		map.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		jproject.setOptions(map);


        addSourceContainer(jproject, SOURCE_DIRECTORY);
        for (String path : Util.getJavaClassLibs()) {
            addLibrary(jproject, path, null);
        }
        return jproject;
	}

	private void addSourceContainer(IJavaProject prj, String name) {
		IFolder folder = prj.getProject().getFolder(name);
		if(!folder.exists()) {
			try {
				folder.create(false, true, null);
			} catch (CoreException e) {
				throw new AssertionError(e);
			}
		}
		IClasspathEntry entry = JavaCore.newSourceEntry(prj.getPackageFragmentRoot(folder).getPath());
		try {
			prj.setRawClasspath(addToClasspath(prj.getRawClasspath(), entry), null);
		} catch (JavaModelException e) {
			throw new AssertionError(e);
		}
	}

	private void addLibrary(IJavaProject prj, String path, String sourcePath) {
		IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(path), Optional.ofNullable(sourcePath).map(Path::new).orElse(null),
				Optional.ofNullable(sourcePath).map(p -> new Path(path)).orElse(null));
		try {
			prj.setRawClasspath(addToClasspath(prj.getRawClasspath(), entry), null);
		} catch (JavaModelException e) {
			throw new AssertionError(e);
		}
	}

	private static IClasspathEntry[] addToClasspath(IClasspathEntry[] original, IClasspathEntry add) {
		IClasspathEntry[] copy = new ClasspathEntry[original.length + 1];
		System.arraycopy(original, 0, copy, 0, original.length);
		copy[copy.length - 1] = add;
		return copy;
	}

	private static void delete(IJavaProject jproject) throws CoreException {
		jproject.setRawClasspath(new ClasspathEntry[0], jproject.getProject().getFullPath(), null);
		jproject.getProject().delete(true, true, null);
	}


}
