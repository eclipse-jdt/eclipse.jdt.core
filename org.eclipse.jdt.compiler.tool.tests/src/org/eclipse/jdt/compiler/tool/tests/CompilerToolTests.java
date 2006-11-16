package org.eclipse.jdt.compiler.tool.tests;

import java.util.ServiceLoader;

import javax.tools.JavaCompiler;

import org.eclipse.jdt.compiler.tool.EclipseCompiler;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompilerToolTests {

	private static JavaCompiler Compiler;

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
}
