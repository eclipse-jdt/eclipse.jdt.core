package org.eclipse.jdt.compiler.tool.tests;

import java.util.ServiceLoader;

import javax.tools.JavaCompiler;

import org.eclipse.jdt.compiler.tool.EclipseCompiler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompilerToolTests {

	private static JavaCompiler Compiler;
	private static String[] ONE_ARG_OPTIONS = {
		"-cp",
		"-classpath",
		"-bootclasspath",
		"-sourcepath",
		"-extdirs",
		"-endorseddirs",
		"-d",
		"-encoding",
		"-source",
		"-target",
		"-maxProblems",
		"-log",
		"-repeat"
	};
	private static String[] ZERO_ARG_OPTIONS = {
		"-1.3",
		"-1.4",
		"-1.5",
		"-1.6",
		"-6",
		"-6.0",
		"-5",
		"-5.0",
		"-deprecation",
		"-nowarn",
		"-warn:none",
		"-?:warn",
		"-g",
		"-g:lines",
		"-g:source",
		"-g:vars",
		"-g:lines,vars",
		"-g:none",
		"-preserveAllLocals",
		"-X",
		"-O",
		"-proceedOnError",
		"-verbose",
		"-referenceInfo",
		"-progress",
		"-time",
		"-noExit",
		"-inlineJSR",
		"-enableJavadoc",
		"-Xemacs",
		"-?",
		"-help",
		"-v",
		"-version",
		"-showversion"
	};

	@BeforeClass
	public static void initializeJavaCompiler() {
		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class);
		int compilerCounter = 0;
		for (JavaCompiler javaCompiler : javaCompilerLoader) {
			compilerCounter++;
			if (javaCompiler instanceof EclipseCompiler) {
				Compiler = javaCompiler;
			}
	     }
		assertEquals("Only one compiler available", 1, compilerCounter);
	}
	
	@Test
	public void checkPresence() {
		// test that the service provided by org.eclipse.jdt.compiler.tool is there
		assertNotNull("No compiler found", Compiler);
	}
	
	@Test
	public void checkOptions() {
		for (String option : ONE_ARG_OPTIONS) {
			assertEquals(option + " requires 1 argument", 1, Compiler.isSupportedOption(option));
		}
		for (String option : ZERO_ARG_OPTIONS) {
			assertEquals(option + " requires no argument", 0, Compiler.isSupportedOption(option));
		}
		assertEquals("-Jignore requires no argument", 0, Compiler.isSupportedOption("-Jignore"));
		assertEquals("-Xignore requires no argument", 0, Compiler.isSupportedOption("-Xignore"));
	}

	@AfterClass
	public static void cleanUp() {
		Compiler = null;
	}
}
