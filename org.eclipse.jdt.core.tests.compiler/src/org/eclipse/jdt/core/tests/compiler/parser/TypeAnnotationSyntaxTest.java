/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TypeAnnotationSyntaxTest extends AbstractSyntaxTreeTest {

	private static String  jsr308TestScratchArea = "c:\\Jsr308TestScratchArea";
	private static String referenceCompiler = "C:\\jdk-7-ea-bin-b75-windows-i586-30_oct_2009\\jdk7\\bin\\javac.exe";

	static {
//		TESTS_NAMES = new String [] { "test0137" };
	}
	public static Class testClass() {
		return TypeAnnotationSyntaxTest.class;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	static final class LocationPrinterVisitor extends ASTVisitor {
		TypeReference enclosingReference;
		Map locations;

		public LocationPrinterVisitor() {
			this.locations = new HashMap();
		}

		public Map getLocations() {
			return this.locations;
		}
		public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
			this.enclosingReference = fieldDeclaration.type;
			return true;
		}
		public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
			TypeReference returnType = methodDeclaration.returnType;
			if (returnType != null) {
				this.enclosingReference = returnType;
				returnType.traverse(this, scope);
			}
			if (methodDeclaration.thrownExceptions != null) {
				int thrownExceptionsLength = methodDeclaration.thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++) {
					TypeReference typeReference = methodDeclaration.thrownExceptions[i];
					this.enclosingReference = typeReference;
					typeReference.traverse(this, scope);
				}
			}
			return false;
		}
		public boolean visit(Argument argument, ClassScope scope) {
			this.enclosingReference = argument.type;
			return true;
		}
		public boolean visit(Argument argument, BlockScope scope) {
			this.enclosingReference = argument.type;
			return true;
		}
		public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, annotation));
			}
			return false;
		}
		public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, annotation));
			}
			return false;
		}
		public boolean visit(NormalAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, annotation));
			}
			return false;
		}
		public void storeLocations(Annotation annotation, int[] tab) {
			String key = String.valueOf(annotation);
			if (this.locations.get(key) != null) {
				return;
			}
			if (tab == null) {
				this.locations.put(key, null);
				return;
			}

			StringBuilder buffer = new StringBuilder("[");
			for (int i = 0, max = tab.length; i < max; i += 2) {
				if (i > 0) {
					buffer.append(", ");
				}
				switch (tab[i]) {
				case 0:
					buffer.append("ARRAY");
					break;
				case 1:
					buffer.append("INNER_TYPE");
					break;
				case 2:
					buffer.append("WILDCARD");
					break;
				case 3:
					buffer.append("TYPE_ARGUMENT(").append(tab[i+1]).append(')');
					break;
				}
			}
			buffer.append(']');
			this.locations.put(key, String.valueOf(buffer));
		}

		public boolean visit(ArrayTypeReference arrayReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
		public boolean visit(ParameterizedSingleTypeReference typeReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
		public boolean visit(SingleTypeReference typeReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
	}
public TypeAnnotationSyntaxTest(String testName){
	super(testName, referenceCompiler, jsr308TestScratchArea);
	if (referenceCompiler != null) {
		File f = new File(jsr308TestScratchArea);
		if (!f.exists()) {
			f.mkdir();
		}
		if (f.exists()) {
			try {
				OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Marker.java")));
				w.write("@interface Marker {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Normal.java")));
				w.write("@interface Normal {\n\tint value() default 10;\n}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "SingleMember.java")));
				w.write("@interface SingleMember {\n\tint value() default 10;\n}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Positive.java")));
				w.write("@interface Positive {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Negative.java")));
				w.write("@interface Negative{}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Readonly.java")));
				w.write("@interface Readonly {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "NonNull.java")));
				w.write("@interface NonNull {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "HashMap.java")));
				w.write("class HashMap<X,Y> {\n class Iterator {}; \n}\n".toCharArray());
				w.close();
				CHECK_ALL |= CHECK_JAVAC_PARSER;
			} catch (IOException e) {
				// ignore
			}
		}
	}
}

static {
//	TESTS_NAMES = new String[] { "test0038", "test0039", "test0040a" };
//	TESTS_NUMBERS = new int[] { 133, 134, 135 };
	if (!(new File(referenceCompiler).exists())) {
		referenceCompiler = null;
		jsr308TestScratchArea = null;
	}
}
void traverse (File f) throws IOException {
	if (f.isDirectory()) {
		File [] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			traverse(files[i]);
		}
	} else {
		if (f.getName().endsWith(".java")) {
			System.out.println(f.getCanonicalPath());
			char [] contents = new char[(int) f.length()];
			FileInputStream fs = new FileInputStream(f);
			InputStreamReader isr = null;
			try {
				isr = new InputStreamReader(fs);
			} finally {
				if (isr != null) isr.close();
			}
			isr.read(contents);
			checkParse(contents, null, f.getCanonicalPath(), null);
		}
	}
}
public void _test000() throws IOException {
	traverse(new File("C:\\jsr308tests"));
}

public void test0001() throws IOException {
	String source = """
		@Marker class A extends String {}
		;\
		@Marker class B extends @Marker String {}
		@Marker class C extends @Marker @SingleMember(0) String {}
		@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}
		@Marker class E extends String {}
		;""";

	String expectedUnitToString =
		"""
		@Marker class A extends String {
		  A() {
		    super();
		  }
		}
		@Marker class B extends @Marker String {
		  B() {
		    super();
		  }
		}
		@Marker class C extends @Marker @SingleMember(0) String {
		  C() {
		    super();
		  }
		}
		@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {
		  D() {
		    super();
		  }
		}
		@Marker class E extends String {
		  E() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0001", expectedUnitToString);
}
public void test0002() throws IOException {
	String source = """
		class A extends String {}
		;\
		class B extends @Marker String {}
		class C extends @Marker @SingleMember(0) String {}
		class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}
		class E extends String {}
		;""";

	String expectedUnitToString =
		"""
		class A extends String {
		  A() {
		    super();
		  }
		}
		class B extends @Marker String {
		  B() {
		    super();
		  }
		}
		class C extends @Marker @SingleMember(0) String {
		  C() {
		    super();
		  }
		}
		class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {
		  D() {
		    super();
		  }
		}
		class E extends String {
		  E() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0002", expectedUnitToString);
}
public void test0003() throws IOException {
	String source = """
		@Marker class A implements Comparable, \
		                   @Marker Serializable,\
		                   Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A implements Comparable, @Marker Serializable, Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0003", expectedUnitToString);
}
public void test0004() throws IOException {
	String source = """
		@Marker class A implements Comparable, \
		                   @Marker @SingleMember(0) Serializable,\
		                   Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A implements Comparable, @Marker @SingleMember(0) Serializable, Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0004", expectedUnitToString);
}
public void test0005() throws IOException {
	String source = """
		@Marker class A implements Comparable, \
		                   @Marker @SingleMember(0) @Normal(Value=0) Serializable,\
		                   Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0005", expectedUnitToString);
}
public void test0006() throws IOException {
	String source = """
		@Marker class A implements @Marker Comparable, \
		                   @Marker @SingleMember(0) @Normal(Value=0) Serializable,\
		                   @Marker Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A implements @Marker Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, @Marker Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0006", expectedUnitToString);
}
public void test007() throws IOException {
	String source = """
		@Marker class A extends Object implements Comparable, \
		                   @Marker @SingleMember(10) @Normal(Value=0) Serializable,\
		                   Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A extends Object implements Comparable, @Marker @SingleMember(10) @Normal(Value = 0) Serializable, Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0007", expectedUnitToString);
}
public void test0008() throws IOException {
	String source = """
		@Marker class A extends @Marker Object implements Comparable, \
		                   @Marker @SingleMember(0) @Normal(Value=0) Serializable,\
		                   Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A extends @Marker Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008", expectedUnitToString);
}
public void test0009() throws IOException {
	String source = """
		@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, \
		                   @Marker @SingleMember(0) @Normal(Value=0) Serializable,\
		                   Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0009", expectedUnitToString);
}
public void test0010() throws IOException {
	String source = """
		@Marker class A extends @Marker @SingleMember(0) @Normal(Value=0) Object implements Comparable, \
		                   @Marker @SingleMember(0) @Normal(Value=0) Serializable,\
		                   Cloneable {
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class A extends @Marker @SingleMember(0) @Normal(Value = 0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {
		  A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0010", expectedUnitToString);
}
public void test0011() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker String[] @Marker[][] s[] @SingleMember(0)[][] @Normal(Value = 0)[][];
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker String[] @Marker [][][] @SingleMember(0) [][] @Normal(Value = 0) [][] s;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011", expectedUnitToString);
}
public void test0012() throws IOException {
	String source = """
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		    int[] f[];
		    @English String[] @NonNull[] s[] @Nullable[][];
		    float[] p[];
		public static void main(String args[]) {
		    @Readonly String @Nullable[] @NonNull[] s;
		    s = new @Readonly String @NonNull[5] @Nullable[];
		}
		}
		""";
	String expectedUnitToString =
		"""
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		  int[][] f;
		  @English String[] @NonNull [][] @Nullable [][] s;
		  float[][] p;
		  public A() {
		    super();
		  }
		  public static void main(String[] args) {
		    @Readonly String @Nullable [] @NonNull [] s;
		    s = new @Readonly String @NonNull [5] @Nullable [];
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012", expectedUnitToString);
}
public void test0013() throws IOException {
	String source = """
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		    int[] f[];
		    @English String[] @NonNull[] s[] @Nullable[][];
		    float[] p[];
		public static void main(String args[]) {
		    @Readonly String s;
			 s = new @Readonly String @NonNull[] @Nullable[] { {"Hello"}, {"World"}} [0][0];
		}
		}
		""";
	String expectedUnitToString =
		"""
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		  int[][] f;
		  @English String[] @NonNull [][] @Nullable [][] s;
		  float[][] p;
		  public A() {
		    super();
		  }
		  public static void main(String[] args) {
		    @Readonly String s;
		    s = new @Readonly String @NonNull [] @Nullable []{{"Hello"}, {"World"}}[0][0];
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013", expectedUnitToString);
}
public void test0014() throws IOException {
	String source = """
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		    int[] f[];
		    @English String[] @NonNull[] s[] @Nullable[][];
		    float[] p[];
		public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] {
		    @Readonly String @Nullable[] @NonNull[] s;
		    s = new @Readonly String @NonNull[5] @Nullable[];
		}
		}
		""";
	String expectedUnitToString =
		"""
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		  int[][] f;
		  @English String[] @NonNull [][] @Nullable [][] s;
		  float[][] p;
		  public A() {
		    super();
		  }
		  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) {
		    @Readonly String @Nullable [] @NonNull [] s;
		    s = new @Readonly String @NonNull [5] @Nullable [];
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014", expectedUnitToString);

}
public void test0015() throws IOException {
	String source = """
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		    int[] f[];
		    @English String[] @NonNull[] s[] @Nullable[][];
		    float[] p[];
		public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] {
		    @Readonly String @Nullable[] @NonNull[] s;
		    s = new @Readonly String @NonNull[5] @Nullable[];
		}
		@Marker public A () {}
		}
		""";
	String expectedUnitToString =
		"""
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		  int[][] f;
		  @English String[] @NonNull [][] @Nullable [][] s;
		  float[][] p;
		  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) {
		    @Readonly String @Nullable [] @NonNull [] s;
		    s = new @Readonly String @NonNull [5] @Nullable [];
		  }
		  public @Marker A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0015", expectedUnitToString);
}
// parameters
public void test0016() throws IOException {
	String source = """
		public class A {
		@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0016", expectedUnitToString);
}
public void test0017() throws IOException  {
	String source = """
		public class A {
		@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0017", expectedUnitToString);
}
public void test0018() throws IOException {
	String source = """
		public class A {
		@Marker public int[] @Marker[][] main(HashMap<String, Object>[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>[] @Normal(Value = 10) [][][] @SingleMember(10) [][] args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0018", expectedUnitToString);
}
public void test0019() throws IOException {
	String source = """
		public class A {
		@Marker public int[] @Marker [][] main(HashMap<String, Object>.Iterator[] @SingleMember(10) [][] args[] @Normal(Value = 10) [][])[] @Marker [][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>.Iterator[] @Normal(Value = 10) [][][] @SingleMember(10) [][] args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0019", expectedUnitToString);
}
// varargs annotation
public void test0020() throws IOException {
	String source = """
		public class A {
		@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][] @Marker ... args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0020", expectedUnitToString);
}
public void test0021() throws IOException {
	String source = """
		public class A {
		@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][] @Marker ... args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0021", expectedUnitToString);
}
public void test0022() throws IOException {
	String source = """
		public class A {
		@Marker public int[] @Marker[][] main(HashMap<Integer,String>[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>[] @SingleMember(10) [][] @Marker ... args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0022", expectedUnitToString);
}
public void test0023() throws IOException {
	String source = """
		public class A {
		@Marker public int[] @Marker[][] main(HashMap<Integer,String>.Iterator[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {
		}
		}""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>.Iterator[] @SingleMember(10) [][] @Marker ... args) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0023", expectedUnitToString);
}
// local variables
public void test0024() throws IOException {
	String source = """
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		public static void main(String args[]) {
		    int[] f[];
		    @English String[] @NonNull[] s[] @Nullable[][];
		    float[] p[];
		}
		}
		""";
	String expectedUnitToString =
		"""
		public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {
		  public A() {
		    super();
		  }
		  public static void main(String[] args) {
		    int[][] f;
		    @English String[] @NonNull [][] @Nullable [][] s;
		    float[][] p;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0024", expectedUnitToString);
}
// type parameter
public void test0025() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> void foo() {
		}
		}
		""";
	String expectedUnitToString =
		"""
		class A {
		  A() {
		    super();
		  }
		  public <Integer, @Positive Integer, @Negative Integer, Integer>void foo() {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0025", expectedUnitToString);
}
// Type
public void test0026() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int foo() {
		    return 0;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> int bar() {
		    return 0;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0026", null);
}
// Type
public void test0027() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String foo() {
		    return null;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> String bar () {
		    return null;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0027", null);
}
//Type
public void test0028() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object> foo() {
		    return null;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object> bar () {
		    return null;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0028", null);
}
// Type
public void test0029() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator foo() {
		    return null;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator bar () {
		    return null;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0029", null);
}
//Type
public void test0030() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator[] @NonEmpty[][] foo() {
		    return null;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator[] @NonEmpty[][] bar () {
		    return null;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0030", null);
}
//Type
public void test0031() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int[] @NonEmpty[][] foo() {
		    return 0;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> int[] @NonEmpty[][] bar() {
		    return 0;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0031", null);
}
// Type
public void test0032() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String[]@NonEmpty[][] foo() {
		    return null;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> String[]@NonEmpty[][] bar () {
		    return null;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0032", null);
}
//Type
public void test0033() throws IOException {
	String source = """
		class A {
		public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>[] @NonEmpty[][] foo() {
		    return null;
		}
		public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>[]@NonEmpty[][] bar () {
		    return null;
		}
		}
		""";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0033", null);
}
// Type0 field declaration.
public void test0034() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker int k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker int k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0034", expectedUnitToString);
}
//Type0 field declaration.
public void test0035() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker String k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker String k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0035", expectedUnitToString);
}
//Type0 field declaration.
public void test0036() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker HashMap<@Positive Integer, @Negative Integer> k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker HashMap<@Positive Integer, @Negative Integer> k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0036", expectedUnitToString);
}
//Type0 field declaration.
public void test0037() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0037", expectedUnitToString);
}
//Type0 field declaration.
public void test0038() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker int[] @NonEmpty[][] k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker int[] @NonEmpty [][] k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0038", expectedUnitToString);
}
//Type0 field declaration.
public void test0039() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker String[] @NonEmpty[][]k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker String[] @NonEmpty [][] k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0039", expectedUnitToString);
}
//Type0 field declaration.
public void test0040() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty[][] k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0040", expectedUnitToString);
}
//Type0 field declaration.
public void test0041() throws IOException {
	String source = """
		public class A {
		    int[] f[];
		    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] k;
		    float[] p[];
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  int[][] f;
		  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] k;
		  float[][] p;
		  public A() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0041", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0042() throws IOException {
	String source = """
		public class A {
		    public @Marker int foo() { return 0; }
		    public int bar() { return 0; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int foo() {
		    return 0;
		  }
		  public int bar() {
		    return 0;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0042", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0043() throws IOException {
	String source = """
		public class A {
		    public @Marker String foo() { return null; }
		    public String bar() { return null; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker String foo() {
		    return null;
		  }
		  public String bar() {
		    return null;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0043", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0044() throws IOException {
	String source = """
		public class A {
		    public @Marker HashMap<@Positive Integer, @Negative Integer> foo() { return null; }
		    public HashMap<@Positive Integer, @Negative Integer>  bar() { return null; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker HashMap<@Positive Integer, @Negative Integer> foo() {
		    return null;
		  }
		  public HashMap<@Positive Integer, @Negative Integer> bar() {
		    return null;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0044", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0045() throws IOException {
	String source = """
		public class A {
		    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() { return null; }
		    public HashMap<@Positive Integer, @Negative Integer>.Iterator  bar() { return null; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() {
		    return null;
		  }
		  public HashMap<@Positive Integer, @Negative Integer>.Iterator bar() {
		    return null;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0045", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0046() throws IOException {
	String source = """
		public class A {
		    public @Marker int[] foo() @NonEmpty[][] { return 0; }
		    public int[] @NonEmpty[][] bar() { return 0; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker int[] @NonEmpty [][] foo() {
		    return 0;
		  }
		  public int[] @NonEmpty [][] bar() {
		    return 0;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0046", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0047() throws IOException {
	String source = """
		public class A {
		    public @Marker String[]  foo() @NonEmpty[][] { return null; }
		    public String[] @NonEmpty[][] bar() { return null; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker String[] @NonEmpty [][] foo() {
		    return null;
		  }
		  public String[] @NonEmpty [][] bar() {
		    return null;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0047", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0048() throws IOException {
	String source = """
		public class A {
		    public @Marker HashMap<@Positive Integer, @Negative Integer>[] foo() @NonEmpty[][] { return null; }
		    public HashMap<@Positive Integer, @Negative Integer> [] @NonEmpty[][] bar() { return null; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker HashMap<@Positive Integer, @Negative Integer> @NonEmpty [][][] foo() {
		    return null;
		  }
		  public HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] bar() {
		    return null;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0048", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0049() throws IOException {
	String source = """
		public class A {
		    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[]  foo() @NonEmpty[][] { return null; }
		    public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] bar() { return null; }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator @NonEmpty [][][] foo() {
		    return null;
		  }
		  public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] bar() {
		    return null;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0049", expectedUnitToString);
}
//Type0 local variable declaration
public void test0050() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker int p;
		        int q;
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker int p;
		    int q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0050", expectedUnitToString);
}
//Type0 local variable declaration
public void test0051() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker String p;
		        String q;
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker String p;
		    String q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0051", expectedUnitToString);
}
//Type0 local variable declaration
public void test0052() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker HashMap<@Positive Integer, @Negative Integer> p;
		        HashMap<@Positive Integer, @Negative Integer> q;
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker HashMap<@Positive Integer, @Negative Integer> p;
		    HashMap<@Positive Integer, @Negative Integer> q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0052", expectedUnitToString);
}
//Type0 local variable declaration
public void test0053() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;
		        HashMap<@Positive Integer, @Negative Integer>.Iterator q;
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;
		    HashMap<@Positive Integer, @Negative Integer>.Iterator q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0053", expectedUnitToString);
}
//Type0 local variable declaration
public void test0054() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker int[] @NonNull[] p @NonEmpty[][];
		        int[] @NonNull[] q @NonEmpty[][];
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker int[] @NonNull [] @NonEmpty [][] p;
		    int[] @NonNull [] @NonEmpty [][] q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0054", expectedUnitToString);
}
//Type0 local variable declaration
public void test0055() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker String[] @NonNull[] p @NonEmpty[][];
		        String[] @NonNull[] q @NonEmpty[][];
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker String[] @NonNull [] @NonEmpty [][] p;
		    String[] @NonNull [] @NonEmpty [][] q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0055", expectedUnitToString);
}
//Type0 local variable declaration
public void test0056() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] p @NonEmpty[][];
		        HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] q @NonEmpty[][];
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker HashMap<@Positive Integer, @Negative Integer> @NonEmpty [][][] @NonNull [] p;
		    HashMap<@Positive Integer, @Negative Integer> @NonEmpty [][][] @NonNull [] q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0056", expectedUnitToString);
}
//Type0 local variable declaration
public void test0057() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] p @NonEmpty[][];
		        HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] @NonEmpty[][] q;
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator @NonEmpty [][][] @NonNull [] p;
		    HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull [] @NonEmpty [][] q;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0057", expectedUnitToString);
}
//Type0 foreach
public void test0058() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        String @NonNull[] @Marker[] s @Readonly[];
		    	 for (@Readonly String @NonNull[] si @Marker[] : s) {}
		    	 for (String @NonNull[] sii @Marker[] : s) {}
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    String @NonNull [] @Marker [] @Readonly [] s;
		    for (@Readonly String @NonNull [] @Marker [] si : s)\s
		      {
		      }
		    for (String @NonNull [] @Marker [] sii : s)\s
		      {
		      }
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0058", expectedUnitToString);
}
//Type0 foreach
public void test0059() throws IOException {
	String source = """
		public class A {
		    public void foo() {
		        int @NonNull[] @Marker[] s @Readonly[];
		    	 for (@Readonly int @NonNull[] si @Marker[] : s) {}
		    	 for (int @NonNull[] sii @Marker[] : s) {}
		    }
		}
		""";
	String expectedUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo() {
		    int @NonNull [] @Marker [] @Readonly [] s;
		    for (@Readonly int @NonNull [] @Marker [] si : s)\s
		      {
		      }
		    for (int @NonNull [] @Marker [] sii : s)\s
		      {
		      }
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0059", expectedUnitToString);
}
// cast expression
public void test0060() throws IOException {
	String source = """
		public class Clazz {
		public static void main(String[] args) {
		int x;
		x = (Integer)
		(@Readonly Object)
		(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )
		(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0)[][] )
		(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker[][] )
		(@Readonly Object)
		(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value=0)[][] )
		(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0)[][] )
		(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker[][] )
		(@Readonly Object)
		(@Readonly String[] @Normal(Value=0)[][] )
		(@Readonly String[] @SingleMember(0)[][] )
		(@Readonly String[] @Marker[][] )
		(@Readonly Object)
		(@Readonly int[] @Normal(Value=0)[][] )
		(@Readonly int[] @SingleMember(0)[][] )
		(@Readonly int[] @Marker[][] )
		(@Readonly Object)
		(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator)
		(@Readonly Object)
		(@Readonly HashMap<@Positive Integer, @Negative Integer>)
		(@Readonly Object)
		(@ReadOnly String)
		(@Readonly Object)
		(@Readonly int) 10;
		}
		}
		""";
	String expectedUnitToString =
		"""
		public class Clazz {
		  public Clazz() {
		    super();
		  }
		  public static void main(String[] args) {
		    int x;
		    x = (Integer) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker [][]) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value = 0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker [][]) (@Readonly Object) (@Readonly String[] @Normal(Value = 0) [][]) (@Readonly String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (@Readonly Object) (@Readonly int[] @Normal(Value = 0) [][]) (@Readonly int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>) (@Readonly Object) (@ReadOnly String) (@Readonly Object) (@Readonly int) 10;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0060", expectedUnitToString);
}
//cast expression
public void test0061() throws IOException {
	String source = """
		public class Clazz {
		public static void main(String[] args) {
		int x;
		x = (Integer)
		(Object)
		(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )
		(HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0)[][] )
		(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker[][] )
		(Object)
		(@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value=0)[][] )
		(HashMap<Integer, @Negative Integer>[] @SingleMember(0)[][] )
		(@Readonly HashMap<@Positive Integer, Integer>[] @Marker[][] )
		(Object)
		(@Readonly String[] @Normal(Value=0)[][] )
		(String[] @SingleMember(0)[][] )
		(@Readonly String[] @Marker[][] )
		(Object)
		(@Readonly int[] @Normal(Value=0)[][] )
		(int[] @SingleMember(0)[][] )
		(@Readonly int[] @Marker[][] )
		(Object)
		(@Readonly HashMap<Integer, @Negative Integer>.Iterator)
		(Object)
		(@Readonly HashMap<@Positive Integer, Integer>)
		(Object)
		(@ReadOnly String)
		(Object)
		(@Readonly int) 10;
		}
		}
		""";
	String expectedUnitToString =
		"""
		public class Clazz {
		  public Clazz() {
		    super();
		  }
		  public static void main(String[] args) {
		    int x;
		    x = (Integer) (Object) (@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) (HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0) [][]) (@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker [][]) (Object) (@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value = 0) [][]) (HashMap<Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, Integer>[] @Marker [][]) (Object) (@Readonly String[] @Normal(Value = 0) [][]) (String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (Object) (@Readonly int[] @Normal(Value = 0) [][]) (int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (Object) (@Readonly HashMap<Integer, @Negative Integer>.Iterator) (Object) (@Readonly HashMap<@Positive Integer, Integer>) (Object) (@ReadOnly String) (Object) (@Readonly int) 10;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0061", expectedUnitToString);
}
// instanceof checks
public void test0062() throws IOException {
	String source = """
		public class Clazz {
		public static void main(Object o) {
		if (o instanceof @Readonly String) {
		} else if (o instanceof @Readonly int[] @NonEmpty[][] ) {
		} else if (o instanceof @Readonly String[] @NonEmpty[][] ) {
		} else if (o instanceof @Readonly HashMap<?,?>[] @NonEmpty[][] ) {
		} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] ) {
		} else if (o instanceof @Readonly HashMap<?,?>) {
		} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) {
		}
		}
		}""";
	String expectedUnitToString =
		"""
		public class Clazz {
		  public Clazz() {
		    super();
		  }
		  public static void main(Object o) {
		    if ((o instanceof @Readonly String))
		        {
		        }
		    else
		        if ((o instanceof @Readonly int[] @NonEmpty [][]))
		            {
		            }
		        else
		            if ((o instanceof @Readonly String[] @NonEmpty [][]))
		                {
		                }
		            else
		                if ((o instanceof @Readonly HashMap<?, ?>[] @NonEmpty [][]))
		                    {
		                    }
		                else
		                    if ((o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][]))
		                        {
		                        }
		                    else
		                        if ((o instanceof @Readonly HashMap<?, ?>))
		                            {
		                            }
		                        else
		                            if ((o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator))
		                                {
		                                }
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0062", expectedUnitToString);
}
// assorted unclassified
public void test0063() throws IOException {
	String source = """
		import java.util.HashMap;
		import java.util.Map;\s
		
		public class Clazz <@A M extends @B String, @C N extends @D Comparable> extends
										@E Object implements @F Comparable <@G Object> {
		\t
		  Clazz(char[] ...args) {\s
		   }
		  \s
		  int @I[] f @J[], g, h[], i@K[];
		  int @L[][]@M[] f2;\s
		  \s
		  Clazz (int @N[] @O... a) {}
		 int @R[]@S[] aa() {}
		\s
		 int @T[]@U[]@V[] a () @W[]@X[]@Y[] { return null; }
		  \s
		  public void main(String @A[] @B ... args) throws @D Exception {
		  \t
		       HashMap<@E String, @F String> b1;
		     \s
		     int b; b = (@G int) 10;
		     \s
		     char @H[]@I[] ch; ch = (@K char @L[]@M[])(@N char @O[]@P[]) null;
		     \s
		      int[] i; i = new @Q int @R[10];
		      \s
		     \s
		   Integer w; w = new X<@S String, @T Integer>().get(new @U Integer(12));
		    throw new @V Exception("test");
		    boolean c; c  = null instanceof @W String;
			}\s
		 public <@X X, @Y Y> void foo(X x, Y @Z... y) { \s
		\t
		}
		\s
		 void foo(Map<? super @A Object, ? extends @B String> m){}
		 public int compareTo(Object arg0) {
		     return 0;
		 }
		
		}
		class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {
		\t
		  public Integer get(Integer integer) {
		       return null;
		   }
		}
		""";


	String expectedUnitToString = """
		import java.util.HashMap;
		import java.util.Map;
		public class Clazz<@A M extends @B String, @C N extends @D Comparable> extends @E Object implements @F Comparable<@G Object> {
		  int @I [] @J [] f;
		  int @I [] g;
		  int @I [][] h;
		  int @I [] @K [] i;
		  int @L [][] @M [] f2;
		  Clazz(char[]... args) {
		    super();
		  }
		  Clazz(int @N [] @O ... a) {
		    super();
		  }
		  int @R [] @S [] aa() {
		  }
		  int @T [] @U [] @V [] @W [] @X [] @Y [] a() {
		    return null;
		  }
		  public void main(String @A [] @B ... args) throws @D Exception {
		    HashMap<@E String, @F String> b1;
		    int b;
		    b = (@G int) 10;
		    char @H [] @I [] ch;
		    ch = (@K char @L [] @M []) (@N char @O [] @P []) null;
		    int[] i;
		    i = new @Q int @R [10];
		    Integer w;
		    w = new X<@S String, @T Integer>().get(new @U Integer(12));
		    throw new @V Exception("test");
		    boolean c;
		    c = (null instanceof @W String);
		  }
		  public <@X X, @Y Y>void foo(X x, Y @Z ... y) {
		  }
		  void foo(Map<? super @A Object, ? extends @B String> m) {
		  }
		  public int compareTo(Object arg0) {
		    return 0;
		  }
		}
		class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {
		  X() {
		    super();
		  }
		  public Integer get(Integer integer) {
		    return null;
		  }
		}
		""";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0063", expectedUnitToString);
}
//assorted unclassified
public void test0064() throws IOException {
	String source = "class X<T extends @E Object & @F Comparable<? super T>> {}\n";
	String expectedUnitToString = """
		class X<T extends @E Object & @F Comparable<? super T>> {
		  X() {
		    super();
		  }
		}
		""";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test064", expectedUnitToString);
}
//type class literal expression
public void test0066() throws IOException {
	String source = """
		public class X {
			<T extends Y<@A String @C[][]@B[]> & Cloneable> void foo(T t) {}
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  <T extends Y<@A String @C [][] @B []> & Cloneable>void foo(T t) {
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0066", expectedUnitToString);
}
//check locations
public void test0067() throws IOException {
	String source =
		"""
		public class X {
			@H String @E[] @F[] @G[] field;
			@A Map<@B String, @C List<@D Object>> field2;
			@A Map<@B String, @H String @E[] @F[] @G[]> field3;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @H String @E [] @F [] @G [] field;
		  @A Map<@B String, @C List<@D Object>> field2;
		  @A Map<@B String, @H String @E [] @F [] @G []> field3;
		  public X() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0067", expectedUnitToString);
}
//check locations
public void test0068() throws IOException {
	String source =
		"""
		public class X {
			@H String @E[] @F[] @G[] field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @H String @E [] @F [] @G [] field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0068", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0069() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B String, @H String> field3;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B String, @H String> field3;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0069", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 3, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@H"));
}
//check locations
public void test0070() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B String, @H String @E[] @F[] @G[]> field3;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B String, @H String @E [] @F [] @G []> field3;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0070", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0071() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B String, @C List<@H String @E[][] @G[]>> field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B String, @C List<@H String @E [][] @G []>> field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0071", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
}
//check locations
public void test0072() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B String, @C List<@H String @E[][] @G[]>>[] @I[] @J[] field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B String, @C List<@H String @E [][] @G []>>[] @I [] @J [] field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0072", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", "[ARRAY]", locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
}
//check locations
public void test0073() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B String, @C List<@H String @E[][] @G[]>> @I[][] @J[] field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B String, @C List<@H String @E [][] @G []>> @I [][] @J [] field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0073", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
}
//check locations
public void test0074() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@C List<@H String @E[][] @G[]>, String @B[] @D[]> @I[] @F[] @J[] field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@C List<@H String @E [][] @G []>, String @B [] @D []> @I [] @F [] @J [] field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0074", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 10, locations.size());
	assertEquals("Wrong location", null, locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), ARRAY]", locations.get("@D"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@B"));
}
//check locations
public void test0075() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@C List<@H String @E[][] @G[]>, @B List<String [] @D[]>> [] @I[] @F[] @J[] field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@C List<@H String @E [][] @G []>, @B List<String[] @D []>>[] @I [] @F [] @J [] field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0075", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 10, locations.size());
	assertEquals("Wrong location", "[ARRAY]", locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@B"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY]", locations.get("@D"));
}
//check locations
public void test0076() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B String, @C List<@D Object>> field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B String, @C List<@D Object>> field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0076", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@D"));
}
//check locations
public void test0077() throws IOException {
	String source =
		"""
		public class X {
			@H String @E[] @F[] @G[] field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @H String @E [] @F [] @G [] field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0077", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0078() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B Comparable<@C Object @D[] @E[] @F[]>, @G List<@H Document>> field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B Comparable<@C Object @D [] @E [] @F []>, @G List<@H Document>> field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0078", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@D"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@H"));
}
//check locations
public void test0079() throws IOException {
	String source =
		"""
		public class X {
			@A java.util.Map<@B Comparable<@C Object @D[] @E[] @F[]>, @G List<@H Document>> field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A java.util.Map<@B Comparable<@C Object @D [] @E [] @F []>, @G List<@H Document>> field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0079", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@D"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@H"));
}
//check locations
public void test0080() throws IOException {
	String source =
		"""
		public class X {
			@B Map<? extends Z, ? extends @A Z> field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @B Map<? extends Z, ? extends @A Z> field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0080", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 2, locations.size());
	assertEquals("Wrong location", null, locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), WILDCARD]", locations.get("@A"));
}
//check locations
public void test0081() throws IOException {
	String source =
		"""
		public class X {
			@H java.lang.String @E[] @F[] @G[] field;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @H java.lang.String @E [] @F [] @G [] field;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0081", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0082() throws IOException {
	String source =
		"""
		public class X {
			@A Map<@B java.lang.String, @H java.lang.String @E[] @F[] @G[]> field3;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A Map<@B java.lang.String, @H java.lang.String @E [] @F [] @G []> field3;
		  public X() {
		    super();
		  }
		}
		""";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0082", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
public void test0083() throws IOException {
	String source =
		"""
		@Marker class A {}
		;\
		@Marker class B extends @Marker A {}
		@Marker class C extends @Marker @SingleMember(0) A {}
		@Marker class D extends @Marker @SingleMember(0) @Normal(value = 0) A {}
		@Marker class E extends B {}
		;""";

	String expectedUnitToString =
		"""
		@Marker class A {
		  A() {
		    super();
		  }
		}
		@Marker class B extends @Marker A {
		  B() {
		    super();
		  }
		}
		@Marker class C extends @Marker @SingleMember(0) A {
		  C() {
		    super();
		  }
		}
		@Marker class D extends @Marker @SingleMember(0) @Normal(value = 0) A {
		  D() {
		    super();
		  }
		}
		@Marker class E extends B {
		  E() {
		    super();
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0083", expectedUnitToString);
}

// To test Parser.consumeAdditionalBound() with Type annotations
public void test0084() throws IOException {
	String source =
		"""
		@Marker interface I<@Negative T> {}
		@SingleMember(0) interface J<@Positive T> {}
		@Marker class A implements I<@SingleMember(0) A>, J<@Marker A> {}
		@Normal(value = 1) class X<E extends @Positive A & @Marker I<A> & @Marker @SingleMember(1) J<@Readonly A>>  {
		}""";
	String expectedUnitToString =
		"""
		@Marker interface I<@Negative T> {
		}
		@SingleMember(0) interface J<@Positive T> {
		}
		@Marker class A implements I<@SingleMember(0) A>, J<@Marker A> {
		  A() {
		    super();
		  }
		}
		@Normal(value = 1) class X<E extends @Positive A & @Marker I<A> & @Marker @SingleMember(1) J<@Readonly A>> {
		  X() {
		    super();
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0084", expectedUnitToString );
}

// To test Parser.consumeAdditionalBound() with Type annotations
public void test0085() throws IOException {
	String source =
		"""
		import java.io.Serializable;
		
		@SingleMember(10) class X<T extends @Marker Serializable & @Normal(value = 10) Runnable, V extends @Marker T> {
			@Negative T t;
			@Marker X(@Readonly T t) {
				this.t = t;
			}
			void foo(@Marker X this) {
				(this == null ? t : t).run();
				((@Marker V) t).run();
			}
			public static void main(@Readonly String @Marker [] args) {
				new @Marker  X<@Marker A, @Negative A>(new @Marker A()).foo();
			}
		}
		@Marker class A implements @Marker Serializable, @SingleMember(1) Runnable {
			public void run() {
				System.out.print("AA");
			}
		}
		""";
	String expectedUnitToString =
		"""
		import java.io.Serializable;
		@SingleMember(10) class X<T extends @Marker Serializable & @Normal(value = 10) Runnable, V extends @Marker T> {
		  @Negative T t;
		  @Marker X(@Readonly T t) {
		    super();
		    this.t = t;
		  }
		  void foo(@Marker X this) {
		    ((this == null) ? t : t).run();
		    ((@Marker V) t).run();
		  }
		  public static void main(@Readonly String @Marker [] args) {
		    new @Marker X<@Marker A, @Negative A>(new @Marker A()).foo();
		  }
		}
		@Marker class A implements @Marker Serializable, @SingleMember(1) Runnable {
		  A() {
		    super();
		  }
		  public void run() {
		    System.out.print("AA");
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0085", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0086() throws IOException {
	String source =
		"""
		class X {
			@Marker X() {
				System.out.print("new X created");
			}
		  	void f() throws @Marker InstantiationException {
		       X testX;
				testX = new @Readonly @Negative X();
				Double d;
				d = new @Marker @Positive Double(1.1);
		     	throw new @Positive @Normal(value = 10) InstantiationException("test");
		   }
		}""";
	String expectedUnitToString =
		"""
		class X {
		  @Marker X() {
		    super();
		    System.out.print("new X created");
		  }
		  void f() throws @Marker InstantiationException {
		    X testX;
		    testX = new @Readonly @Negative X();
		    Double d;
		    d = new @Marker @Positive Double(1.1);
		    throw new @Positive @Normal(value = 10) InstantiationException("test");
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0086", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0087() throws IOException {
	String source =
		"""
		class X {
			@Marker X() {
				System.out.print("new X created");
			}
			@Marker class Inner {
				@Normal(value = 10) Inner(){
					System.out.print("X.Inner created");
				}
			}
			public String getString(){
				return "hello";
			}
		  	void f(@Marker X this) {
		       String testString;
				testString = new @Readonly @Negative X().getString();
				X.Inner testInner;
				testInner = new @Readonly X.Inner();
				int i;
				for(i = 0; i < 10; i++)
					System.out.print("test");
		   }
		}""";
	String expectedUnitToString =
		"""
		class X {
		  @Marker class Inner {
		    @Normal(value = 10) Inner() {
		      super();
		      System.out.print("X.Inner created");
		    }
		  }
		  @Marker X() {
		    super();
		    System.out.print("new X created");
		  }
		  public String getString() {
		    return "hello";
		  }
		  void f(@Marker X this) {
		    String testString;
		    testString = new @Readonly @Negative X().getString();
		    X.Inner testInner;
		    testInner = new @Readonly X.Inner();
		    int i;
		    for (i = 0; (i < 10); i ++)\s
		      System.out.print("test");
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0087", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0088() throws IOException {
	String source =
		"""
		import java.io.Serializable;
		class X {
			public static void main(String[] args) {
				new @Marker Serializable() {
				};
				new @Positive @Marker Serializable() {
					public long serialVersion;
				};
			}
		}""";
	String expectedUnitToString =
		"""
		import java.io.Serializable;
		class X {
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    new @Marker Serializable() {
		    };
		    new @Positive @Marker Serializable() {
		      public long serialVersion;
		    };
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0088", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0089() throws IOException {
	String source =
		"""
		import java.io.Serializable;
		class X<T>{
			public void f() {
				X testX;
				testX = new @Marker @SingleMember(10) X<@Negative Integer>();
				System.out.print("object created");
			}
		}""";
	String expectedUnitToString =
		"""
		import java.io.Serializable;
		class X<T> {
		  X() {
		    super();
		  }
		  public void f() {
		    X testX;
		    testX = new @Marker @SingleMember(10) X<@Negative Integer>();
		    System.out.print("object created");
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0089", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0090() throws IOException {
	String source =
		"""
		class X <@Marker T extends @Readonly String> {
		    T foo(T t) {
		        return t;
		    }
		    public static void main(String[] args) {
		        new @Readonly X<String>().baz("SUCCESS");
		    }
		    void baz(final T t) {
		        new @Readonly @Marker Object() {
		            void print() {
		            }
		        }.print();
		    }
		}
		""";
	String expectedUnitToString =
		"""
		class X<@Marker T extends @Readonly String> {
		  X() {
		    super();
		  }
		  T foo(T t) {
		    return t;
		  }
		  public static void main(String[] args) {
		    new @Readonly X<String>().baz("SUCCESS");
		  }
		  void baz(final T t) {
		    new @Readonly @Marker Object() {
		  void print() {
		  }
		}.print();
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0090", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0091() throws IOException {
	String source =
		"""
		class X <@Marker T extends @Readonly String> {
		    public static void main(String[] args) {
				int [] x1;
				x1 = new int @Marker @SingleMember(2) [] {-1, -2};
		       Integer [][] x2;
				x2 = new @Positive Integer @Marker @SingleMember(3) [] @SingleMember(3) [] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
		    }
		}
		""";
	String expectedUnitToString =
		"""
		class X<@Marker T extends @Readonly String> {
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    int[] x1;
		    x1 = new int @Marker @SingleMember(2) []{(- 1), (- 2)};
		    Integer[][] x2;
		    x2 = new @Positive Integer @Marker @SingleMember(3) [] @SingleMember(3) []{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0091", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0092() throws IOException {
	String source =
		"""
		class X {
			static class T {
				public @Readonly Object @Normal(value = 10) [] f() {
					return new @Readonly Object @Normal(value = 10) [] {this, T.this};
				}
			}
		}""";
	String expectedUnitToString =
		"""
		class X {
		  static class T {
		    T() {
		      super();
		    }
		    public @Readonly Object @Normal(value = 10) [] f() {
		      return new @Readonly Object @Normal(value = 10) []{this, T.this};
		    }
		  }
		  X() {
		    super();
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0092", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0093() throws IOException {
	String source =
		"""
		class X {
		    public static void main(String[] args) {
		        java.util.Arrays.asList(new @Readonly Object @SingleMember(1) [] {"1"});
		    }
		}
		""";
	String expectedUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    java.util.Arrays.asList(new @Readonly Object @SingleMember(1) []{"1"});
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0093", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0094() throws IOException {
	String source =
		"""
		class X {
			public boolean test() {
				String[] s;
				s = foo(new @Marker String @SingleMember(1) []{"hello"});
				return s != null;
			}
			public <@Marker F> F @SingleMember(1) [] foo(F[] f) {
				return f;
			}
		}""";
	String expectedUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  public boolean test() {
		    String[] s;
		    s = foo(new @Marker String @SingleMember(1) []{"hello"});
		    return (s != null);
		  }
		  public <@Marker F>F @SingleMember(1) [] foo(F[] f) {
		    return f;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0094", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0095() throws IOException {
	String source =
		"""
		import java.util.Arrays;
		import java.util.List;
		@Marker class Deejay {
			@Marker class Counter<@Marker T> {}
			public void f(String[] args) {
				Counter<@Positive Integer> songCounter;
				songCounter = new Counter<@Positive Integer>();
				Counter<@Readonly String> genre;
				genre = new Counter<@Readonly String>();
				List<@Marker Counter<?>> list1;
				list1 = Arrays.asList(new @Marker Counter<?> @Normal(value = 2) @Marker [] {songCounter, genre});
			}
		}
		""";
	String expectedUnitToString =
		"""
		import java.util.Arrays;
		import java.util.List;
		@Marker class Deejay {
		  @Marker class Counter<@Marker T> {
		    Counter() {
		      super();
		    }
		  }
		  Deejay() {
		    super();
		  }
		  public void f(String[] args) {
		    Counter<@Positive Integer> songCounter;
		    songCounter = new Counter<@Positive Integer>();
		    Counter<@Readonly String> genre;
		    genre = new Counter<@Readonly String>();
		    List<@Marker Counter<?>> list1;
		    list1 = Arrays.asList(new @Marker Counter<?> @Normal(value = 2) @Marker []{songCounter, genre});
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0095", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0096() throws IOException {
	String source =
		"""
		class X <@Marker T extends @Readonly String> {
		    public static void main(String[] args) {
				int [] x1;
				x1 = new int @Marker @SingleMember(10) [10];
		       Integer [][] x2;
				x2 = new @Positive Integer @Marker [10] @Normal(value = 10) [10];
				char[][] tokens;
				tokens = new char @SingleMember(0) [0] @Normal(value = 10) @Marker [];
		    }
		}
		""";
	String expectedUnitToString =
		"""
		class X<@Marker T extends @Readonly String> {
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    int[] x1;
		    x1 = new int @Marker @SingleMember(10) [10];
		    Integer[][] x2;
		    x2 = new @Positive Integer @Marker [10] @Normal(value = 10) [10];
		    char[][] tokens;
		    tokens = new char @SingleMember(0) [0] @Normal(value = 10) @Marker [];
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0096", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0097() throws IOException {
	String source =
		"""
		class X {
			public @Readonly Object @Normal(value = 10) [] f(@Marker X this) {
				return new @Readonly Object @Normal(value = 10) [10];
			}
		}""";
	String expectedUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  public @Readonly Object @Normal(value = 10) [] f(@Marker X this) {
		    return new @Readonly Object @Normal(value = 10) [10];
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0097", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0098() throws IOException {
	String source =
		"""
		class X {
			public boolean test() {
				String[] s;
				s = foo(new @Marker String @SingleMember(1) [10]);
				return s != null;
			}
			public <@Marker F> F @SingleMember(1) [] foo(F[] f) {
				return f;
			}
		}""";
	String expectedUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  public boolean test() {
		    String[] s;
		    s = foo(new @Marker String @SingleMember(1) [10]);
		    return (s != null);
		  }
		  public <@Marker F>F @SingleMember(1) [] foo(F[] f) {
		    return f;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0098", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0099() throws IOException {
	String source =
		"""
		import java.util.Arrays;
		import java.util.List;
		class X<@Marker T> {
			public void test() {
				List<@Marker X<?>> a;
				a = Arrays.asList(new @Marker X<?> @SingleMember(0) [0]);
				String @Marker [] @SingleMember(1) [] x;
				x = new @Readonly String @Normal(value = 5) [5] @SingleMember(1) [1];
			}
		}""";
	String expectedUnitToString =
		"""
		import java.util.Arrays;
		import java.util.List;
		class X<@Marker T> {
		  X() {
		    super();
		  }
		  public void test() {
		    List<@Marker X<?>> a;
		    a = Arrays.asList(new @Marker X<?> @SingleMember(0) [0]);
		    String @Marker [] @SingleMember(1) [] x;
		    x = new @Readonly String @Normal(value = 5) [5] @SingleMember(1) [1];
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0099", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0100() throws IOException {
	String source =
		"""
		import java.util.*;
		class X {
		    public Integer[] getTypes() {
		        List<@Positive Integer> list;
				 list = new ArrayList<@Positive Integer>();
		        return list == null\s
		            ? new @Positive Integer @SingleMember(0) [0]\s
		            : list.toArray(new @Positive Integer @Marker [list.size()]);
		    }
		}""";
	String expectedUnitToString =
		"""
		import java.util.*;
		class X {
		  X() {
		    super();
		  }
		  public Integer[] getTypes() {
		    List<@Positive Integer> list;
		    list = new ArrayList<@Positive Integer>();
		    return ((list == null) ? new @Positive Integer @SingleMember(0) [0] : list.toArray(new @Positive Integer @Marker [list.size()]));
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0100", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0101() throws IOException {
	String source =
		"""
		import java.util.*;
		
		@Marker class X {
		    Vector<Object> data;
		    public void t() {
		        Vector<@Readonly Object> v;
		 		 v = (@Marker @SingleMember(0) Vector<@Readonly Object>) data.elementAt(0);
		    }
		}
		""";
	String expectedUnitToString =
		"""
		import java.util.*;
		@Marker class X {
		  Vector<Object> data;
		  X() {
		    super();
		  }
		  public void t() {
		    Vector<@Readonly Object> v;
		    v = (@Marker @SingleMember(0) Vector<@Readonly Object>) data.elementAt(0);
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0101", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
// To test Parser.consumeClassHeaderExtends() with Type Annotations
public void test0102() throws IOException {
	String source =
		"""
		class X<E> {
		    X<@Readonly String> bar() {
		    	return (@Marker AX<@Readonly String>) new X<@Readonly String>();
		    }
		    X<@Readonly String> bar(Object o) {
		    	return (@Marker AX<@Readonly String>) o;
		    }
		    X<@Negative E> foo(Object o) {
		    	return (@Marker @Normal(value = 10) AX<@Negative E>) o;
		    }   \s
		    X<E> baz(Object o) {
		    	return (@Marker AX<E>) null;
		    }
		    X<String> baz2(BX bx) {
		    	return (@Marker @SingleMember(10) X<String>) bx;
		    }
		}
		@Normal(value = 1) class AX<@Marker F> extends @Marker X<@SingleMember(10)F> {}
		@Normal(value = 2) class BX extends @Marker @SingleMember(1) AX<@Readonly String> {}
		""";
	String expectedUnitToString =
		"""
		class X<E> {
		  X() {
		    super();
		  }
		  X<@Readonly String> bar() {
		    return (@Marker AX<@Readonly String>) new X<@Readonly String>();
		  }
		  X<@Readonly String> bar(Object o) {
		    return (@Marker AX<@Readonly String>) o;
		  }
		  X<@Negative E> foo(Object o) {
		    return (@Marker @Normal(value = 10) AX<@Negative E>) o;
		  }
		  X<E> baz(Object o) {
		    return (@Marker AX<E>) null;
		  }
		  X<String> baz2(BX bx) {
		    return (@Marker @SingleMember(10) X<String>) bx;
		  }
		}
		@Normal(value = 1) class AX<@Marker F> extends @Marker X<@SingleMember(10) F> {
		  AX() {
		    super();
		  }
		}
		@Normal(value = 2) class BX extends @Marker @SingleMember(1) AX<@Readonly String> {
		  BX() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0102", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0103() throws IOException {
	String source =
		"""
		import java.lang.reflect.Array;
		@Marker class X<@Readonly T> {
			T @SingleMember(0) [] theArray;
			public X(Class<T> clazz) {
				theArray = (@Marker @SingleMember(0) T @Normal(value = 10) []) Array.newInstance(clazz, 10); // Compiler warning
			}
		}""";
	String expectedUnitToString =
		"""
		import java.lang.reflect.Array;
		@Marker class X<@Readonly T> {
		  T @SingleMember(0) [] theArray;
		  public X(Class<T> clazz) {
		    super();
		    theArray = (@Marker @SingleMember(0) T @Normal(value = 10) []) Array.newInstance(clazz, 10);
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0103", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0104() throws IOException {
	String source =
		"""
		import java.util.*;
		class X {
		    void method(Object o) {
				 if (o instanceof String[]){
					 String[] s;
					 s = (@Marker @Readonly String @Marker []) o;
				 }
		        if (o instanceof @Readonly List<?>[]) {
		            List<?>[] es;
					 es = (@Marker List<?> @SingleMember(0) []) o;
		        }
		    }
		}""";
	String expectedUnitToString =
		"""
		import java.util.*;
		class X {
		  X() {
		    super();
		  }
		  void method(Object o) {
		    if ((o instanceof String[]))
		        {
		          String[] s;
		          s = (@Marker @Readonly String @Marker []) o;
		        }
		    if ((o instanceof @Readonly List<?>[]))
		        {
		          List<?>[] es;
		          es = (@Marker List<?> @SingleMember(0) []) o;
		        }
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0104", expectedUnitToString );
}


// To test Parser.consumeCastExpressionWithPrimitiveType() with Type Annotations
public void test0105() throws IOException {
	String source =
		"""
		import java.util.HashMap;
		class X {
			public static void main(String[] args) {
				HashMap<Byte, Byte> subst;
				subst = new HashMap<Byte, Byte>();
				subst.put((@Marker byte)1, (@Positive byte)1);
				if (1 + subst.get((@Positive @Normal(value = 10) byte)1) > 0.f) {
					System.out.println("SUCCESS");
				}	\t
			}
		}
		""";
	String expectedUnitToString =
		"""
		import java.util.HashMap;
		class X {
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    HashMap<Byte, Byte> subst;
		    subst = new HashMap<Byte, Byte>();
		    subst.put((@Marker byte) 1, (@Positive byte) 1);
		    if (((1 + subst.get((@Positive @Normal(value = 10) byte) 1)) > 0.f))
		        {
		          System.out.println("SUCCESS");
		        }
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0105", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithPrimitiveType() with Type Annotations
public void test0106() throws IOException {
	String source =
		"""
		class X{
			private float x, y, z;
			float magnitude () {
				return (@Marker @Positive float) Math.sqrt((x*x) + (y*y) + (z*z));
			}
		}
		""";
	String expectedUnitToString =
		"""
		class X {
		  private float x;
		  private float y;
		  private float z;
		  X() {
		    super();
		  }
		  float magnitude() {
		    return (@Marker @Positive float) Math.sqrt((((x * x) + (y * y)) + (z * z)));
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0106", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithQualifiedGenericsArray() with Type Annotations
// Javac version b76 crashes on type annotations on type arguments to parameterized classes
// in a qualified generic reference
public void test0107() throws IOException {
	String source =
		"""
		class C1<T> {
			class C11 {	}
			@Marker class C12 {
				T t;
				C1<@Readonly T>.C11 m() {
					C1<@Readonly T>.C11[] ts;
					ts = (@Marker C1<@Readonly T>.C11[]) new @Marker C1<?>.C11 @Normal(value = 5) [5];
					return ts;
				}
			}
		}
		""";
	String expectedUnitToString =
		"""
		class C1<T> {
		  class C11 {
		    C11() {
		      super();
		    }
		  }
		  @Marker class C12 {
		    T t;
		    C12() {
		      super();
		    }
		    C1<@Readonly T>.C11 m() {
		      C1<@Readonly T>.C11[] ts;
		      ts = (@Marker C1<@Readonly T>.C11[]) new @Marker C1<?>.C11 @Normal(value = 5) [5];
		      return ts;
		    }
		  }
		  C1() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0107", expectedUnitToString );
}

// To test Parser.consumeFormalParameter() with Type Annotations
public void test0108() throws IOException {
	String source =
		"""
		class X {
			int field;\
			public void test(@Marker X x,@Positive int i){
				x.field = i;
			}
			public static void main(@Readonly String args @Normal(10) []){\
				System.exit(0);
			}
		}
		""";
	String expectedUnitToString =
		"""
		class X {
		  int field;
		  X() {
		    super();
		  }
		  public void test(@Marker X x, @Positive int i) {
		    x.field = i;
		  }
		  public static void main(@Readonly String @Normal(10) [] args) {
		    System.exit(0);
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0108", expectedUnitToString );
}

// To test Parser.consumeFormalParameter() with Type Annotations
public void test0109() throws IOException {
	String source =
		"""
		class X<@Marker T> {
			T field;\
			public void test(@Marker @SingleMember(1) X<? extends @Marker Object> x,@Positive T i){
			}
		}
		""";
	String expectedUnitToString =
		"""
		class X<@Marker T> {
		  T field;
		  X() {
		    super();
		  }
		  public void test(@Marker @SingleMember(1) X<? extends @Marker Object> x, @Positive T i) {
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0109", expectedUnitToString );
}

// To test Parser.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments()
// with Type Annotations
// Javac b76 crashes with type annotations in qualified class instance creation expression
public void test0110() throws IOException {
	String source =
		"""
		class X {
			class MX {
				@Marker <T> MX(T t){
					System.out.println(t);
				}
			}
			public static void main(String[] args) {
				new @Marker @SingleMember(10) X().new <@Readonly String> @Marker MX("SUCCESS");
			}
		}
		""";
	String expectedUnitToString =
			"""
		class X {
		  class MX {
		    @Marker <T>MX(T t) {
		      super();
		      System.out.println(t);
		    }
		  }
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    new @Marker @SingleMember(10) X().new <@Readonly String>@Marker MX("SUCCESS");
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0110", expectedUnitToString);
}

// To test Parser.consumeClassInstanceCreationExpressionWithTypeArguments()
// with Type Annotations
public void test0111() throws IOException {
	String source =
		"""
		class X {
			public <T> X(T t){
				System.out.println(t);
			}
			public static void main(String[] args) {
				new <@Readonly String> @Marker @SingleMember(0) X("SUCCESS");
			}
		}
		""";
	String expectedUnitToString =
			"""
		class X {
		  public <T>X(T t) {
		    super();
		    System.out.println(t);
		  }
		  public static void main(String[] args) {
		    new <@Readonly String>@Marker @SingleMember(0) X("SUCCESS");
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0111", expectedUnitToString);
}

// To test Parser.consumeEnhancedForStatementHeaderInit() with Type Annotations
public void test0112() throws IOException {
	String source =
		"""
		import java.util.*;
		class X {
		   List list() { return null; }
		   void m2() { for (@SingleMember(10) Iterator<@Marker X> i = list().iterator(); i.hasNext();); }
			void m3() {
				Integer [] array;
				array = new Integer [] {1, 2, 3};
				List<List<X>> xList;
				xList = null;
				for(@Positive @SingleMember(10) Integer i: array) {}
				for(@Marker @Normal(value = 5) List<@Readonly X> x: xList) {}
			}\
		}
		""";
	String expectedUnitToString =
		"""
		import java.util.*;
		class X {
		  X() {
		    super();
		  }
		  List list() {
		    return null;
		  }
		  void m2() {
		    for (@SingleMember(10) Iterator<@Marker X> i = list().iterator();; i.hasNext(); )\s
		      ;
		  }
		  void m3() {
		    Integer[] array;
		    array = new Integer[]{1, 2, 3};
		    List<List<X>> xList;
		    xList = null;
		    for (@Positive @SingleMember(10) Integer i : array)\s
		      {
		      }
		    for (@Marker @Normal(value = 5) List<@Readonly X> x : xList)\s
		      {
		      }
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_COMPLETION_PARSER & ~CHECK_SELECTION_PARSER & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0112", expectedUnitToString );
	expectedUnitToString =
		"""
			import java.util.*;
			class X {
			  X() {
			    super();
			  }
			  List list() {
			    return null;
			  }
			  void m2() {
			    for (@SingleMember(10) Iterator<@Marker X> i;; i.hasNext(); )\s
			      ;
			  }
			  void m3() {
			    Integer[] array;
			    array = new Integer[]{1, 2, 3};
			    List<List<X>> xList;
			    xList = null;
			    for (@Positive @SingleMember(10) Integer i : array)\s
			      {
			      }
			    for (@Marker @Normal(value = 5) List<@Readonly X> x : xList)\s
			      {
			      }
			  }
			}
			""";
	checkParse(CHECK_COMPLETION_PARSER & CHECK_SELECTION_PARSER, source.toCharArray(), null, "test0112", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0113() throws IOException {
	String source =
		"""
		@Marker class X {
		  void f(@Normal(value = 5) X this) {
		    new @Marker @SingleMember(10) Object() {
		      void foo(){
		        System.out.println("test");
		      }
		    }.foo();
		  }
		}""";
	String expectedUnitToString =
		"""
		@Marker class X {
		  X() {
		    super();
		  }
		  void f(@Normal(value = 5) X this) {
		    new @Marker @SingleMember(10) Object() {
		  void foo() {
		    System.out.println("test");
		  }
		}.foo();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test0113", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0114() throws IOException {
	String source =
		"""
		class Toplevel2{
		    public boolean foo(){
		    Toplevel2 o;
			 o = new @Marker @Normal(value = 5) Toplevel2() {\s
		              public boolean foo() {  return false; }  // no copy in fact
		              };
		    return o.foo();
		  }
		}""";
	String expectedUnitToString =
		"""
		class Toplevel2 {
		  Toplevel2() {
		    super();
		  }
		  public boolean foo() {
		    Toplevel2 o;
		    o = new @Marker @Normal(value = 5) Toplevel2() {
		  public boolean foo() {
		    return false;
		  }
		};
		    return o.foo();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test0114", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0115() throws IOException {
	String source =
		"""
		class X <T> {
		    T foo(T t) {
		        System.out.println(t);
		        return t;
		    }
		    public static void main(String @Normal(value =  5) [] args) {
		        new @Marker X<@SingleMember(10) @Normal(value = 5) XY>() {
		            void run() {
		                foo(new @Marker XY());
		            }
		        }.run();
		    }
		}
		@Marker class XY {
		    public String toString() {
		        return "SUCCESS";
		    }
		}
		""";
	String expectedUnitToString =
		"""
		class X<T> {
		  X() {
		    super();
		  }
		  T foo(T t) {
		    System.out.println(t);
		    return t;
		  }
		  public static void main(String @Normal(value = 5) [] args) {
		    new @Marker X<@SingleMember(10) @Normal(value = 5) XY>() {
		  void run() {
		    foo(new @Marker XY());
		  }
		}.run();
		  }
		}
		@Marker class XY {
		  XY() {
		    super();
		  }
		  public String toString() {
		    return "SUCCESS";
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test0115", expectedUnitToString );
}

// To test Parser.consumeInsideCastExpressionLL1() with Type Annotations
public void test0116() throws IOException {
	String source =
		"""
		class X{
		  public void test1(){
		    throw (@Marker Error) null;\s
		  } \s
		  public void test2(){
		    String s;
			 s = (@Marker @SingleMember(10) String) null;
			 byte b;
			 b = 0;
			 Byte i;
			 i = (@Positive Byte) b;
		  } \s
		  public void test3(java.io.Serializable name) {
		     Object temp;
			  temp = (Object)name;
		     System.out.println( (String)temp );
		  }
		}""";
	String expectedUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  public void test1() {
		    throw (@Marker Error) null;
		  }
		  public void test2() {
		    String s;
		    s = (@Marker @SingleMember(10) String) null;
		    byte b;
		    b = 0;
		    Byte i;
		    i = (@Positive Byte) b;
		  }
		  public void test3(java.io.Serializable name) {
		    Object temp;
		    temp = (Object) name;
		    System.out.println((String) temp);
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0116", expectedUnitToString );
}

// To test Parser.consumeInstanceOfExpression() with Type Annotations
public void test0117() throws IOException {
	String source =
		"""
		import java.util.*;
		class X <@NonNull T>{
		 	public void test1(Object obj) {
		   	if(obj instanceof @Marker @NonNull X) {
				 	X newX;
				 	newX = (@NonNull X) obj;
			 }
		   }
			@NonNull T foo(@NonNull T t) {
		       if (t instanceof @NonNull @Marker List<?> @Normal(value = 10) []) {
		           List<?> @SingleMember (10) [] es;
					es = (@Marker List<?> @SingleMember(10) []) t;
		       }
				if (t instanceof @Marker @Normal(value = 5) X<?>) {
					return t;
				}
				return t;
			}
		}""";
	String expectedUnitToString =
		"""
		import java.util.*;
		class X<@NonNull T> {
		  X() {
		    super();
		  }
		  public void test1(Object obj) {
		    if ((obj instanceof @Marker @NonNull X))
		        {
		          X newX;
		          newX = (@NonNull X) obj;
		        }
		  }
		  @NonNull T foo(@NonNull T t) {
		    if ((t instanceof @NonNull @Marker List<?> @Normal(value = 10) []))
		        {
		          List<?> @SingleMember(10) [] es;
		          es = (@Marker List<?> @SingleMember(10) []) t;
		        }
		    if ((t instanceof @Marker @Normal(value = 5) X<?>))
		        {
		          return t;
		        }
		    return t;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER , source.toCharArray(), null, "test0117", expectedUnitToString );
}

// To test Parser.consumeInstanceOfExpressionWithName() with Type Annotations
public void test0118() throws IOException {
	String source =
		"""
		class Outer<E> {
		  Inner inner;
		  class Inner {
		    E e;
		    @NonNull E getOtherElement(Object other) {
		      if (!(other instanceof @Marker @SingleMember(10) Outer<?>.Inner))
		       throw new @Marker IllegalArgumentException(String.valueOf(other));
		      Inner that;
			   that = (@Marker Inner) other;
		      return that.e;
		    }
		  }
		}""";
	String expectedUnitToString =
		"""
		class Outer<E> {
		  class Inner {
		    E e;
		    Inner() {
		      super();
		    }
		    @NonNull E getOtherElement(Object other) {
		      if ((! (other instanceof @Marker @SingleMember(10) Outer<?>.Inner)))
		          throw new @Marker IllegalArgumentException(String.valueOf(other));
		      Inner that;
		      that = (@Marker Inner) other;
		      return that.e;
		    }
		  }
		  Inner inner;
		  Outer() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER , source.toCharArray(), null, "test0118", expectedUnitToString );
}

// To test Parser.consumeTypeArgument() with Type Annotations
public void test0119() throws IOException {
	String source =
		"""
		class X<@SingleMember(1) Xp1 extends @Readonly String, @NonNull Xp2 extends @NonNull Comparable>  extends @Marker XS<@SingleMember(10) Xp2> {
		
		    public static void main(String @Marker [] args) {
		        Integer w;
		        w = new @Marker X<@Readonly @SingleMember(10) String,@Positive Integer>().get(new @Positive Integer(12));
		        System.out.println("SUCCESS");
			 }
		    Xp2 get(@Marker X this, Xp2 t) {
		        System.out.print("{X::get}");
		        return super.get(t);
		    }
		}
		@Marker class XS <@NonNull XSp1> {
		    XSp1 get(XSp1 t) {
				 @NonNull @SingleMember(10) Y.M mObject;
				 mObject = new @SingleMember(10) @NonNull Y.M();
		        System.out.print("{XS::get}");
		        return t;
		    }
		}
		class X2<T,E>{}
		@Marker class Y extends @Marker X2<@NonNull Y.M, @NonNull @SingleMember(1) Y.N> {
			static class M{}
			static class N extends M{}
		}
		""";
	String expectedUnitToString =
		"""
		class X<@SingleMember(1) Xp1 extends @Readonly String, @NonNull Xp2 extends @NonNull Comparable> extends @Marker XS<@SingleMember(10) Xp2> {
		  X() {
		    super();
		  }
		  public static void main(String @Marker [] args) {
		    Integer w;
		    w = new @Marker X<@Readonly @SingleMember(10) String, @Positive Integer>().get(new @Positive Integer(12));
		    System.out.println("SUCCESS");
		  }
		  Xp2 get(@Marker X this, Xp2 t) {
		    System.out.print("{X::get}");
		    return super.get(t);
		  }
		}
		@Marker class XS<@NonNull XSp1> {
		  XS() {
		    super();
		  }
		  XSp1 get(XSp1 t) {
		    @NonNull @SingleMember(10) Y.M mObject;
		    mObject = new @SingleMember(10) @NonNull Y.M();
		    System.out.print("{XS::get}");
		    return t;
		  }
		}
		class X2<T, E> {
		  X2() {
		    super();
		  }
		}
		@Marker class Y extends @Marker X2<@NonNull Y.M, @NonNull @SingleMember(1) Y.N> {
		  static class M {
		    M() {
		      super();
		    }
		  }
		  static class N extends M {
		    N() {
		      super();
		    }
		  }
		  Y() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0119", expectedUnitToString );
}

// To test Parser.consumeTypeArgument() with Type Annotations
public void test0120() throws IOException {
	String source =
		"""
		class X<A1, A2, A3, A4, A5, A6, A7, A8> {
		}
		class Y {
			@Marker X<int @Marker [], short @SingleMember(1) [] @Marker [], long[] @NonNull [][], float[] @Marker [] @Normal(value = 5) [][], double[][]@Marker [] @SingleMember(10) [][], boolean[][][][][][], char[] @Marker [][][][][][], Object[][]@Marker [] @SingleMember(10) [] @Normal(value = 5) [][][][][]> x;
		}
		""";
	String expectedUnitToString =
		"""
		class X<A1, A2, A3, A4, A5, A6, A7, A8> {
		  X() {
		    super();
		  }
		}
		class Y {
		  @Marker X<int @Marker [], short @SingleMember(1) [] @Marker [], long[] @NonNull [][], float[] @Marker [] @Normal(value = 5) [][], double[][] @Marker [] @SingleMember(10) [][], boolean[][][][][][], char[] @Marker [][][][][][], Object[][] @Marker [] @SingleMember(10) [] @Normal(value = 5) [][][][][]> x;
		  Y() {
		    super();
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0120", expectedUnitToString );
}

// To test Parser.consumeTypeArgumentReferenceType1() with Type Annotations
public void test0121() throws IOException {
	String source =
		"""
		@Marker class X <@NonNull T> {
		    protected T t;
		    @Marker X(@NonNull T t) {
		        this.t = t;
		    }
		    public static void main(String[] args) {
			  X<@Marker X<@Readonly @NonNull String>> xs;
			  xs = new @Marker X<@Marker X<@Readonly @NonNull String>>(new @Marker X<@Readonly @NonNull @SingleMember(10) String>("SUCCESS"));
			  System.out.println(xs.t.t);
		    }
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class X<@NonNull T> {
		  protected T t;
		  @Marker X(@NonNull T t) {
		    super();
		    this.t = t;
		  }
		  public static void main(String[] args) {
		    X<@Marker X<@Readonly @NonNull String>> xs;
		    xs = new @Marker X<@Marker X<@Readonly @NonNull String>>(new @Marker X<@Readonly @NonNull @SingleMember(10) String>("SUCCESS"));
		    System.out.println(xs.t.t);
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0121", expectedUnitToString );
}

// To test Parser.consumeTypeParameter1WithExtendsAndBounds() and Parser.consumeWildcardBoundsSuper() with
// Type Annotations
public void test0122() throws IOException {
	String source =
		"""
		@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable<@Marker Foo1> {
			public int compareTo(Foo1 arg0) {
				return 0;
			}
		}
		class Foo1 {}
		@Marker class X<@NonNull T extends @NonNull @Normal (value = 5) Object & @Marker Comparable<? super @NonNull T>> {
		    public static void main(String[] args) {
		        new @Marker @SingleMember(10) X<@Marker Foo>();
		    }
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable<@Marker Foo1> {
		  Foo() {
		    super();
		  }
		  public int compareTo(Foo1 arg0) {
		    return 0;
		  }
		}
		class Foo1 {
		  Foo1() {
		    super();
		  }
		}
		@Marker class X<@NonNull T extends @NonNull @Normal(value = 5) Object & @Marker Comparable<? super @NonNull T>> {
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    new @Marker @SingleMember(10) X<@Marker Foo>();
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0122", expectedUnitToString );
}

// To test Parser.consumeTypeParameter1WithExtendsAndBounds() with Type Annotations
public void test0123() throws IOException {
	String source =
		"""
		@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable {
			public int compareTo(Object arg0) {
				return 0;
			}
		}
		class Foo1 {}
		@Marker class X<@NonNull T extends @NonNull @Normal (value = 5) Object & @Marker Comparable, @NonNull V extends @Readonly Object> {
		    public static void main(String[] args) {
		        new @Marker @SingleMember(10) X<@Marker Foo, @SingleMember(0) Foo1>();
				 Class <@NonNull Foo> c;
		    }
		}
		""";
	String expectedUnitToString =
		"""
		@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable {
		  Foo() {
		    super();
		  }
		  public int compareTo(Object arg0) {
		    return 0;
		  }
		}
		class Foo1 {
		  Foo1() {
		    super();
		  }
		}
		@Marker class X<@NonNull T extends @NonNull @Normal(value = 5) Object & @Marker Comparable, @NonNull V extends @Readonly Object> {
		  X() {
		    super();
		  }
		  public static void main(String[] args) {
		    new @Marker @SingleMember(10) X<@Marker Foo, @SingleMember(0) Foo1>();
		    Class<@NonNull Foo> c;
		  }
		}
		""";
	checkParse(source.toCharArray(), null, "test0123", expectedUnitToString );
}
//To test type annotations on static class member access in a declaration
public void test0125() throws IOException {
	String source =
		"public class X extends @A(\"Hello, World!\") Y<@B @C('(') String[] @D[]> {}";
	String expectedUnitToString =
		"""
		public class X extends @A("Hello, World!") Y<@B @C(\'(\') String[] @D []> {
		  public X() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0125", expectedUnitToString );
}
//To test type annotations on static class member access in a declaration
public void test0126() throws IOException {
	String source =
		"""
		public class X {
			@A("Hello, World!") @B @C('(') String@E[] @D[] f;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A("Hello, World!") @B @C(\'(\') String @E [] @D [] f;
		  public X() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0126", expectedUnitToString );
}
//To test type annotations on static class member access in a declaration
public void test0127() throws IOException {
	String source =
		"""
		public class X {
			@A("Hello, World!") Y<@B @C('(') String[] @D[]> f;
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  @A("Hello, World!") Y<@B @C(\'(\') String[] @D []> f;
		  public X() {
		    super();
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0127", expectedUnitToString );
}
//type class literal expression
public void test0128() throws IOException {
	String source =
	"""
		public class X {
			public boolean foo(String s) {
				return (s instanceof @C('_') Object[]);
			}
			public Object foo1(String s) {
				return new @B(3) @A("new Object") Object[] {};
			}
			public Class foo2(String s) {
				return null;
			}
			public Class foo3(String s) {
				return null;
			}
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public boolean foo(String s) {
		    return (s instanceof @C(\'_\') Object[]);
		  }
		  public Object foo1(String s) {
		    return new @B(3) @A("new Object") Object[]{};
		  }
		  public Class foo2(String s) {
		    return null;
		  }
		  public Class foo3(String s) {
		    return null;
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0128", expectedUnitToString );
}
//instanceof checks
public void test0129() throws IOException {
	String source = """
		public class Clazz {
		public static void main(Object o) {
		if (o instanceof @Readonly String) {
		}
		}
		}""";
	String expectedUnitToString =
		"""
		public class Clazz {
		  public Clazz() {
		    super();
		  }
		  public static void main(Object o) {
		    if ((o instanceof @Readonly String))
		        {
		        }
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0129", expectedUnitToString);
}
//instanceof checks
public void test0130() throws IOException {
	String source = """
		public class Clazz {
		public static void foo() {
			if (o instanceof @Readonly String[]) {}\
		}
		}""";
	String expectedUnitToString =
		"""
		public class Clazz {
		  public Clazz() {
		    super();
		  }
		  public static void foo() {
		    if ((o instanceof @Readonly String[]))
		        {
		        }
		  }
		}
		""";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//cast
public void test0131() throws IOException {
	String source =
		"""
		public class X {
			public void foo(Object o) {
				if (o instanceof String[][]) {
					String[][] tab = (@C('_') @B(3) String[] @A[]) o;
					System.out.println(tab.length);
				}
				System.out.println(o);
			}
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo(Object o) {
		    if ((o instanceof String[][]))
		        {
		          String[][] tab = (@C(\'_\') @B(3) String[] @A []) o;
		          System.out.println(tab.length);
		        }
		    System.out.println(o);
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//cast
public void test0132() throws IOException {
	String source =
		"""
		public class X {
			public void foo(Object o) {
				if (o instanceof String[][]) {
					String[][] tab = (@C('_') @B(3) String@D[] @A[]) o;
					System.out.println(tab.length);
				}
				System.out.println(o);
			}
		}""";
	String expectedUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo(Object o) {
		    if ((o instanceof String[][]))
		        {
		          String[][] tab = (@C(\'_\') @B(3) String @D [] @A []) o;
		          System.out.println(tab.length);
		        }
		    System.out.println(o);
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//generic type arguments in a generic method invocation
public void test0133() throws IOException {
	String source =
		"""
		public class X {
			static <T, U> T foo(T t, U u) {
				return t;
			}
			public static void main(String[] args) {
				System.out.println(X.<@D() @A(value = "hello") String, @B X>foo("SUCCESS", null));
			}
		}
		""";
	String expectedUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  static <T, U>T foo(T t, U u) {
		    return t;
		  }
		  public static void main(String[] args) {
		    System.out.println(X.<@D() @A(value = "hello") String, @B X>foo("SUCCESS", null));
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//generic type arguments in a generic method invocation
public void test0134() throws IOException {
	String source =
		"""
		public class X {
		
			<T, U> T foo(T t, U u) {
				return t;
			}
			public static void main(String[] args) {
				X x = new X();
				System.out.println(x.<@D() @A(value = "hello") String, @B X>foo("SUCCESS", null));
			}
		}
		""";
	String expectedUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  <T, U>T foo(T t, U u) {
		    return t;
		  }
		  public static void main(String[] args) {
		    X x = new X();
		    System.out.println(x.<@D() @A(value = "hello") String, @B X>foo("SUCCESS", null));
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//generic type arguments in a generic constructor invocation
public void test0135() throws IOException {
	String source =
		"""
		public class X {
			<T, U> X(T t, U u) {
			}
			public static void main(String[] args) {
				X x = new <@D() @A(value = "hello") String, @B X> X();
				System.out.println(x);
			}
		}
		""";
	String expectedUnitToString =
		"""
		public class X {
		  <T, U>X(T t, U u) {
		    super();
		  }
		  public static void main(String[] args) {
		    X x = new <@D() @A(value = "hello") String, @B X>X();
		    System.out.println(x);
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383600 -- Receiver annotation - new syntax.
public void test0136() throws IOException {
	String source =
			"""
		public class X<T> {
		  public class Y<K> {
		    void foo(@Marker X<T> this) {
		    }
		    public class Z {
		      Z(@D() @A(value = "hello") X<T>.Y<K> X.Y.this) {
		      }
		    }
		  }
		  public static void main(String[] args) {
		    new X<String>().new Y<Integer>().new Z();
		  }
		}
		""";
	String expectedUnitToString =
			"""
		public class X<T> {
		  public class Y<K> {
		    public class Z {
		      Z(@D() @A(value = "hello") X<T>.Y<K> X.Y.this) {
		        super();
		      }
		    }
		    public Y() {
		      super();
		    }
		    void foo(@Marker X<T> this) {
		    }
		  }
		  public X() {
		    super();
		  }
		  public static void main(String[] args) {
		    new X<String>().new Y<Integer>().new Z();
		  }
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
// Support type annotations for wildcard
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388085
public void test0137() throws IOException {
	String source =
			"""
		class X {
			public void main(Four<@Marker ? super String, @Marker ? extends Object> param) {
				One<@Marker ? extends Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String,@Marker ? extends Object>>>> one = null;
				Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String,@Marker ? extends Object>>> two = null;
				Three<@Marker ? extends Four<@Marker ? super String,@Marker ? extends Object>> three = null;
				Four<@Marker ? super String,@Marker ? extends Object> four = param;
			}
		}
		class One<R> {}
		class Two<S> {}
		class Three<T> {}
		class Four<U, V> {}
		@interface Marker {}""";
	String expectedUnitToString =
			"""
		class X {
		  X() {
		    super();
		  }
		  public void main(Four<@Marker ? super String, @Marker ? extends Object> param) {
		    One<@Marker ? extends Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;
		    Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String, @Marker ? extends Object>>> two = null;
		    Three<@Marker ? extends Four<@Marker ? super String, @Marker ? extends Object>> three = null;
		    Four<@Marker ? super String, @Marker ? extends Object> four = param;
		  }
		}
		class One<R> {
		  One() {
		    super();
		  }
		}
		class Two<S> {
		  Two() {
		    super();
		  }
		}
		class Three<T> {
		  Three() {
		    super();
		  }
		}
		class Four<U, V> {
		  Four() {
		    super();
		  }
		}
		@interface Marker {
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0137", expectedUnitToString);
}
public void test0138() throws IOException {
	String source =
			"""
		import java.lang.annotation.Target;
		import static java.lang.annotation.ElementType.*;
		public class X {
			public void foo() {
				int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [bar()] @Marker @Marker2 [];
				int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [X.bar2(2)] @Marker @Marker2 [];
			}
			public int bar() {
				return 2;
			}
			public static int bar2(int k) {
				return k;
			}
		}
		@Target (java.lang.annotation.ElementType.TYPE_USE)
		@interface Marker {}
		@Target (java.lang.annotation.ElementType.TYPE_USE)
		@interface Marker2 {}
		""";
	String expectedUnitToString =
			"""
		import java.lang.annotation.Target;
		import static java.lang.annotation.ElementType.*;
		public class X {
		  public X() {
		    super();
		  }
		  public void foo() {
		    int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [bar()] @Marker @Marker2 [];
		    int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [X.bar2(2)] @Marker @Marker2 [];
		  }
		  public int bar() {
		    return 2;
		  }
		  public static int bar2(int k) {
		    return k;
		  }
		}
		@Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {
		}
		@Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker2 {
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0137", expectedUnitToString);
}
// Support for annotations on ellipsis in lambda expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432574
public void test0139() throws IOException {
	String source =
			"""
		import java.lang.annotation.ElementType;
		import java.lang.annotation.Target;
		public class X {
			FI fi = (String @T1[] @T1... x) -> {};
		}
		interface FI {
			void foo(String[]... x);
		}
		@Target(ElementType.TYPE_USE)
		@interface T1 {
		}
		""";
	String expectedUnitToString =
			"""
		import java.lang.annotation.ElementType;
		import java.lang.annotation.Target;
		public class X {
		  FI fi = (String @T1 [] @T1 ... x) ->   {
		  };
		  public X() {
		    super();
		  }
		}
		interface FI {
		  void foo(String[]... x);
		}
		@Target(ElementType.TYPE_USE) @interface T1 {
		}
		""";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0139", expectedUnitToString);
}
}
