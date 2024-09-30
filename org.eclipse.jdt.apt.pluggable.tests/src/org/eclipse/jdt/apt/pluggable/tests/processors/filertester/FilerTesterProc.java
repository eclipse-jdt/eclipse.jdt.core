/*******************************************************************************
 * Copyright (c) 2007 - 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.filertester;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.eclipse.jdt.apt.pluggable.tests.ProcessorTestStatus;
import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;
import org.eclipse.jdt.internal.apt.pluggable.core.filer.IdeOutputClassFileObject;

/**
 * Testing annotation processors through JUnit in the IDE is complex, because each test requires
 * something different of the processor and all processors must coexist in the plugin registry, and
 * because the processor has very limited communication with the rest of the IDE. So, we make one
 * processor run many tests. The JUnit tests specify which test to run by passing its name in to the
 * FilerTest annotation. Test failures are reported via the Messager interface.
 *
 * @since 3.4
 */
@SupportedAnnotationTypes( { "org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( {})
public class FilerTesterProc extends AbstractProcessor {

	private ProcessingEnvironment _processingEnv;
	private Filer _filer;
	public static int roundNo = 0;
	public static byte[] classContent = null;

	public static final String resource01FileContents =
		"package g;\n" +
		"public class Test {}\n";
	public static final String resource01Name =
		".apt_generated/g/Test.java";

	public static final String resource02FileContents =
		"This is some test text\n";
	public static final String resource02Name =
		"bin/t/Test.txt";

	public static final String helloStr = "Hello world";
	public static final String javaStr = "package g;\nclass G {}\n";

	/**
	 * @return a string representing a large Java class.
	 */
	public static String largeJavaClass() {
		StringBuilder sb = new StringBuilder();
		sb.append("package g;\n");
		sb.append("public class Test {\n");
		sb.append("    public static final String bigString = \n");
		for (int i = 0; i < 500; ++i) {
			sb.append("        \"the quick brown dog jumped over the lazy fox, in a peculiar reversal\\n\" +\n");
		}
		sb.append("    \"\";\n");
		sb.append("\n");
		sb.append("    /** This file is at least this big */\n");
		sb.append("    public static final int SIZE = ");
		sb.append(sb.length());
		sb.append(";\n");
		sb.append("}\n");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_processingEnv = processingEnv;
		_filer = _processingEnv.getFiler();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 *      javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		ProcessorTestStatus.setProcessorRan();
		if (!roundEnv.processingOver() && !annotations.isEmpty()) {
			round(annotations, roundEnv);
		}
		return true;
	}

	/**
	 * Perform a round of processing: for a given annotation instance, determine what test method it
	 * specifies, and invoke that method, passing in the annotated element.
	 */
	private void round(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement filerTesterAnno = annotations.iterator().next();
		Set<? extends Element> annotatedEls = roundEnv.getElementsAnnotatedWith(filerTesterAnno);
		for (Element annotatedEl : annotatedEls) {
			FilerTestTrigger filerTesterMirror = annotatedEl.getAnnotation(FilerTestTrigger.class);
			String testMethodName = filerTesterMirror.test();
			String arg0 = filerTesterMirror.arg0();
			String arg1 = filerTesterMirror.arg1();
			if (null != testMethodName && testMethodName.length() > 0) {
				try {
					Method testMethod = FilerTesterProc.class.getMethod(testMethodName,
							Element.class, String.class, String.class);
					testMethod.invoke(this, annotatedEl, arg0, arg1);
				} catch (Exception e) {
					Throwable t;
					t = (e instanceof InvocationTargetException) ? t = e.getCause() : e;
					t.printStackTrace();
					// IllegalStateException probably means test method called ProcessorTestStatus.fail()
					String msg = (t instanceof IllegalStateException) ?
							t.getMessage() :
							t.getClass().getSimpleName() + " invoking test method " +
							testMethodName + " - see console for details";
					ProcessorTestStatus.fail(msg);
				}
			}
		}
	}

	/**
	 * Attempt to get an existing resource from the SOURCE_OUTPUT.
	 */
	public void testGetResource01(Element e, String arg0, String arg1) throws Exception {
		FileObject resource = _filer.getResource(StandardLocation.SOURCE_OUTPUT, arg0, arg1);
		checkResourceContents01(resource, resource01Name, resource01FileContents);
	}

	/**
	 * Attempt to get an existing resource from the CLASS_OUTPUT.
	 */
	public void testGetResource02(Element e, String arg0, String arg1) throws Exception {
		FileObject resource = _filer.getResource(StandardLocation.CLASS_OUTPUT, arg0, arg1);
		checkResourceContents01(resource, resource02Name, resource02FileContents);
	}

	/**
	 * Attempt to create a new resource in SOURCE_OUTPUT.
	 */
	public void testCreateNonSourceFile(Element e, String pkg, String relName) throws Exception {
		FileObject fo = _filer.createResource(StandardLocation.SOURCE_OUTPUT,
				pkg, relName, e);
		try (PrintWriter pw = new PrintWriter(fo.openWriter())) {
			pw.println("Hello world");
		}
		String name = fo.getName().toString();
		// JSR269 spec does not make strict requirements about what getName() returns,
		// but we can at least expect it to include the relative name.
		if (!name.contains(relName)) {
			ProcessorTestStatus.fail("File object getName() returned " + name +
					", expected it to contain " + relName);
		}
	}

	/**
	 * Attempt to create new resources with null parentage.
	 * See <a href="http://bugs.eclipse.org/285838">Bug 285838</a>.
	 */
	public void testNullParents(Element e, String pkg, String relName) throws Exception {
		FileObject fo = _filer.createResource(StandardLocation.SOURCE_OUTPUT,
				pkg, relName + ".txt", (Element[])null);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fo.openWriter());
			pw.println("Hello world");
		} finally {
			if (pw != null)
				pw.close();
		}

		JavaFileObject jfo = _filer.createSourceFile(pkg + "/" + relName, (Element[])null);
		pw = null;
		try {
			pw = new PrintWriter(jfo.openWriter());
			pw.println("package " + pkg + ";\npublic class " + relName + "{ }");
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	/**
	 * Test the toUri() method on various file objects.
	 */
	public void testURI(Element e, String pkg, String relName) throws Exception {

		// Generated non-source file
		FileObject foGenNonSrc = _filer.createResource(StandardLocation.SOURCE_OUTPUT,
				pkg, relName, e);
		checkGenUri(foGenNonSrc, relName, helloStr, "generated non-source file");

		// Generated source file
		FileObject foGenSrc = _filer.createSourceFile("g.G", e);
		checkGenUri(foGenSrc, "G", javaStr, "generated source file");
	}

	public void testBug534979(Element e, String pkg, String relName) throws Exception {
		JavaFileObject jfo = _filer.createSourceFile(pkg + "." + relName);
		try (PrintWriter pw = new PrintWriter(jfo.openWriter())) {
			pw.println("package " + pkg + ";\npublic class " + relName + "{ }");
		}
	}
	public void testBug542090a(Element e, String pkg, String relName) throws Exception {
		if (++roundNo > 1)
			return;
		JavaFileObject jfo = _filer.createSourceFile(pkg + "." + relName);
		try (PrintWriter pw = new PrintWriter(jfo.openWriter())) {
			pw.println("package " + pkg + ";\npublic class " + relName + "{ }");
		}
	}
	public void testBug542090b(Element e, String pkg, String relName) throws Exception {
		JavaFileObject jfo = _filer.createSourceFile(pkg + "." + relName);
		try (PrintWriter pw = new PrintWriter(jfo.openWriter())) {
			pw.println("package " + pkg + ";\npublic class " + relName + "{ }");
		}
	}
	public void testBug534979InModule(Element e, String pkg, String relName) throws Exception {
		JavaFileObject jfo = _filer.createSourceFile(pkg+"."+relName, e.getEnclosingElement());
		try (PrintWriter pw = new PrintWriter(jfo.openWriter())) {
			pw.println("package " + pkg + ";\npublic class " + relName + "{ }");
		}
	}
	public void testCreateClass1(Element e, String pkg, String relName) throws Exception {
		Filer filer = processingEnv.getFiler();
		try {
			if (++roundNo == 1)
				return;
			if (roundNo == 2) {
				JavaFileObject jfo = filer.createSourceFile("p/Test", e.getEnclosingElement());
				try (PrintWriter pw = new PrintWriter(jfo.openWriter())) {
					pw.write("package p;\n " +
							"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
							"@FilerTestTrigger(test = \"testCreateClass1\", arg0 = \"p\", arg1 = \"Test.java\")" +
							"public class Test {}");
				}
			} else if(roundNo == 3) {
					if (classContent == null) {
						throw new IOException("Class file should have been present");
					}
					IdeOutputClassFileObject jfo = (IdeOutputClassFileObject) filer.createClassFile("p/Trigger");
					try (OutputStream out = jfo.openOutputStream()) {
						out.write(classContent);
					} catch (Exception ex) {
					}
			}
		} finally {
		}
	}
	public void testCreateClass2(Element e, String pkg, String relName) throws Exception {
		Filer filer = processingEnv.getFiler();
		try {
			if (++roundNo == 1)
				return;
			if (roundNo == 2) {
				JavaFileObject jfo = filer.createSourceFile("p/Test", e.getEnclosingElement());
				try (PrintWriter pw = new PrintWriter(jfo.openWriter())) {
					pw.write("package p;\n " +
							"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
							"@FilerTestTrigger(test = \"testCreateClass1\", arg0 = \"p\", arg1 = \"Test.java\")" +
							"public class Test {}");
				}
			} else if(roundNo == 3) {
					if (classContent == null) {
						throw new IOException("Class file should have been present");
					}
					IdeOutputClassFileObject jfo = (IdeOutputClassFileObject) filer.createClassFile("p.Trigger");
					try (OutputStream out = jfo.openOutputStream()) {
						out.write(classContent);
					} catch (Exception ex) {
					}
			}
		} finally {
		}
	}

	private void checkGenUri(FileObject fo, String name, String content, String category) throws Exception {
		try (PrintWriter pw = new PrintWriter(fo.openWriter())) {
			pw.print(content);
		}
		URI uri = fo.toUri();
		if (!uri.toString().contains(name)) {
			ProcessorTestStatus.fail("toUri() on " + category + " returned " + uri.toString() +
					", expected it to contain " + name);
		}
		char buf[] = new char[256];
		Reader r = null;
		int len = 0;
		try {
			r = new InputStreamReader(uri.toURL().openStream());
			len = r.read(buf);
		} finally {
			if (r != null)
				r.close();
		}
		buf = Arrays.copyOf(buf, len);
		if (!Arrays.equals(buf, content.toCharArray())) {
			ProcessorTestStatus.fail("toUri() on " + category + " returned " + uri.toString() +
					", but reading that URI produced \"" + new String(buf) + "\" instead of expected \"" + content + "\"");
		}
	}

	/**
	 * Attempt to get an existing resource from the SOURCE_OUTPUT.
	 */
	public void testGetCharContentLarge(Element e, String arg0, String arg1) throws Exception {
		FileObject resource = _filer.getResource(StandardLocation.SOURCE_OUTPUT, arg0, arg1);
		CharSequence actualCharContent = resource.getCharContent(true);
		String expectedContents = largeJavaClass();
		if (!expectedContents.equals(actualCharContent.toString())) {
			System.out.println("Expected getCharContent to return:\n" + expectedContents);
			System.out.println("Actual getCharContent returned:\n" + actualCharContent);
			ProcessorTestStatus.fail("getCharContent() did not return expected contents");
		}
	}

	/**
	 * Check that the resource can be opened, examined, and its contents match
	 * {@link #checkResourceContents01(FileObject)}getResource01FileContents
	 */
	private void checkResourceContents01(FileObject resource, String expectedName, String expectedContents) throws Exception {

		long modTime = resource.getLastModified();
		if (modTime <= 0) {
			ProcessorTestStatus.fail("resource had unexpected mod time: " + modTime);
		}

		String actualName = resource.getName();
		if (!expectedName.equals(actualName)) {
			System.out.println("Resource had unexpected name.  Expected " + expectedName +
					", actual was " + actualName);
			ProcessorTestStatus.fail("Resource had unexpected name");
		}

		InputStream stream = resource.openInputStream();
		if (stream.available() <= 0) {
			ProcessorTestStatus.fail("stream contained no data");
		}
		byte actualBytes[] = new byte[512];
		int length = stream.read(actualBytes);
		String actualStringContents = new String(actualBytes, 0, length);
		if (!expectedContents.equals(actualStringContents)) {
			System.out.println("Expected stream contents:\n" + expectedContents);
			System.out.println("Actual contents were:\n" + actualStringContents);
			ProcessorTestStatus.fail("stream did not contain expected contents");
		}
		stream.close();

		char actualChars[] = new char[512];
		Reader reader = resource.openReader(true);
		length = reader.read(actualChars, 0, actualChars.length);
		actualStringContents = new String(actualChars, 0, length);
		if (!expectedContents.equals(actualStringContents)) {
			System.out.println("Expected reader contents:\n" + expectedContents);
			System.out.println("Actual contents were:\n" + actualStringContents);
			ProcessorTestStatus.fail("reader did not contain expected contents");
		}
		reader.close();

		CharSequence actualCharContent = resource.getCharContent(true);
		if (!expectedContents.equals(actualCharContent.toString())) {
			System.out.println("Expected getCharContent to return:\n" + expectedContents);
			System.out.println("Actual getCharContent returned:\n" + actualCharContent);
			ProcessorTestStatus.fail("getCharContent() did not return expected contents");
		}
	}
}
