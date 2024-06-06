/*******************************************************************************
 * Copyright (c) 2011, 2020 IBM Corporation and others.
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
 *                          Bug 409236 - [1.8][compiler] Type annotations on intersection cast types dropped by code generator
 *                          Bug 409246 - [1.8][compiler] Type annotations on catch parameters not handled properly
 *                          Bug 409517 - [1.8][compiler] Type annotation problems on more elaborate array references
 *                          Bug 415821 - [1.8][compiler] CLASS_EXTENDS target type annotation missing for anonymous classes
 *                          Bug 426616 - [1.8][compiler] Type Annotations, multiple problems
 *        Stephan Herrmann - Contribution for
 *							Bug 415911 - [1.8][compiler] NPE when TYPE_USE annotated method with missing return type
 *							Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *         Jesper S Moller - Contributions for
 *                          Bug 416885 - [1.8][compiler]IncompatibleClassChange error (edit)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TypeAnnotationTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "testTypeVariable" };
	}
	public static Class testClass() {
		return TypeAnnotationTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public TypeAnnotationTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	@Override
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		return defaultOptions;
	}

	private static final String HELPER_CLASS =
		"import java.lang.annotation.*;\n"+
		"import java.lang.reflect.*;\n"+
		"class Helper {\n"+
		"\n"+
		// Print type annotations on super types
		"  public static void printTypeAnnotations(Class<?> clazz) {\n"+
		"    System.out.print(\"Annotations on superclass of \"+clazz.getName() +\"\\n\");\n"+
		"    AnnotatedType superat = clazz.getAnnotatedSuperclass();\n"+
		"    Helper.printAnnos(\"  \", superat.getType(),superat.getAnnotations());\n"+
		"    AnnotatedType[] superinterfaces = clazz.getAnnotatedInterfaces();\n"+
		"    if (superinterfaces.length!=0) {\n"+
		"      System.out.print(\"Annotations on superinterfaces of \"+clazz.getName() +\"\\n\");\n"+
		"      for (int j=0;j<superinterfaces.length;j++) {\n"+
		"        Helper.printAnnos(\"  \", superinterfaces[j].getType(),superinterfaces[j].getAnnotations());\n"+
		"      }\n"+
		"    }\n"+
		"  }\n"+
		// Print type annotations on a type
		"  public static void printTypeAnnotations2(Class<?> clazz) {\n"+
		"    System.out.print(clazz.getName()+\"<\");\n"+
		"    TypeVariable<?>[] tvs = clazz.getTypeParameters();\n"+
		"    for (int t=0;t<tvs.length;t++) {\n"+
		"      TypeVariable<?> tv = tvs[t];\n"+
		"      Annotation[] annos = tv.getAnnotations();\n"+
		"      for (int a=0;a<annos.length;a++) {\n"+
		"        System.out.print(toStringAnno(annos[a])+\" \");\n"+
		"      }\n"+
		"      System.out.print(tv.getName());\n"+
		"      if ((t+1)<tvs.length) System.out.print(\",\");\n"+
		"    }\n"+
		"    System.out.print(\">\\n\");\n"+
		"  }\n"+
		"  public static String toStringAnno(Annotation anno) {\n"+
		"    String s = anno.toString();\n"+
		"	 s = s.replace(\"\\\"\", \"\");\n" +
		"	 s = s.replace(\"'\", \"\");\n" +
		"    if (s.endsWith(\"()\")) return s.substring(0,s.length()-2); else return s;\n"+
		"  }\n"+
		"  \n"+
		"  public static void printAnnos(String header, Type t, Annotation[] annos) {\n"+
		"    if (annos.length==0) { System.out.print(header+t+\":no annotations\\n\"); return;} \n"+
		"    System.out.print(header+t+\":\");\n"+
		"    for (int i=0;i<annos.length;i++) {\n"+
		"      System.out.print(toStringAnno(annos[i])+\" \");\n"+
		"    }\n"+
		"    System.out.print(\"\\n\");\n"+
		"  }\n"+
		"}\n";

	// http://types.cs.washington.edu/jsr308/specification/java-annotation-design.pdf
	//		type_annotation {
	//			// New fields in JSR 308:
	//			u1 target_type; // the type of the targeted program element, see Section 3.2
	//			union {
	//				type_parameter_target;
	//				supertype_target;
	//				type_parameter_bound_target;
	//				empty_target;
	//				method_formal_parameter_target;
	//				throws_target;
	//				localvar_target;
	//				catch_target;
	//				offset_target;
	//				type_argument_target;
	//				method_reference_target;
	//			} target_info; // identifies the targeted program element, see Section 3.3
	//			type_path target_path; // identifies targeted type in a compound type (array, generic, etc.), see Section 3.4
	//			// Original fields from "annotation" structure:
	//			u2 type_index;
	//			u2 num_element_value_pairs;
	//			{
	//				u2 element_name_index;
	//				element_value value;
	//			} element_value_pairs[num_element_value_pairs];
	//			}

	public void test001_classTypeParameter() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",

					"Marker.java",
					"""
						import java.lang.annotation.*;
						@Retention(RetentionPolicy.RUNTIME)
						@Target(ElementType.TYPE_PARAMETER)
						@interface Marker {}""",
				},
				"");
		// javac-b81: 9[0 1 0 0 0 0 13 0 0]  (13=Marker annotation)
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @Marker(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 0
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test002_classTypeParameter_reflection() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X<@Marker T> {
						  public static void main(String[] argv) { Helper.printTypeAnnotations2(X.class);}
						}""",

					"Helper.java",HELPER_CLASS,
					"Marker.java",
					"""
						import java.lang.annotation.*;
						import static java.lang.annotation.ElementType.*;
						@Retention(RetentionPolicy.RUNTIME)
						@Target(TYPE_PARAMETER)
						@interface Marker {}""",
				},
				"X<@Marker T>");
	}

	public void test003_classTypeParameter() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X<@A1 T1,@A2 @A3 T2> {}",

					"A1.java",
					"""
						import java.lang.annotation.*;
						@Retention(RetentionPolicy.RUNTIME)
						@Target(ElementType.TYPE_PARAMETER)
						@interface A1 {}""",

					"A2.java",
					"""
						import java.lang.annotation.*;
						@Retention(RetentionPolicy.RUNTIME)
						@Target(ElementType.TYPE_PARAMETER)
						@interface A2 {}""",

					"A3.java",
					"""
						import java.lang.annotation.*;
						@Retention(RetentionPolicy.RUNTIME)
						@Target(ElementType.TYPE_PARAMETER)
						@interface A3 {}""",

				},
				"");
		// javac-b81: 9[0 1 0 0 0 0 13 0 0]  (13=Marker)
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A1(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 0
			    )
			    #22 @A2(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 1
			    )
			    #23 @A3(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 1
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test004_classTypeParameter_reflection() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X<@A1 T1,@A2 @A3 T2> {
							    public static void main(String[] argv) { Helper.printTypeAnnotations2(X.class); }
							}""",

						"Helper.java",HELPER_CLASS,
						"A1.java",
						"""
							import java.lang.annotation.*;
							@Retention(RetentionPolicy.RUNTIME)
							@Target(ElementType.TYPE_PARAMETER)
							@interface A1 {}""",
						"A2.java",
						"""
							import java.lang.annotation.*;
							import static java.lang.annotation.ElementType.*;
							@Retention(RetentionPolicy.RUNTIME)
							@Target(TYPE_PARAMETER)
							@interface A2 {}""",
						"A3.java",
						"""
							import java.lang.annotation.*;
							import static java.lang.annotation.ElementType.*;
							@Retention(RetentionPolicy.RUNTIME)
							@Target(TYPE_PARAMETER)
							@interface A3 {}""",
				},
				"X<@A1 T1,@A2 @A3 T2>");
	}

	public void test005_classTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"public class X<@A @B(3) T> {}",
		},
		"");
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #25 @A(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 0
			    )
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @B(
			      #22 value=(int) 3 (constant type)
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 0
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test006_classTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"public class X<T1,T2,@A @B(3) T3> {}",
		},
		"");
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #25 @A(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 2
			    )
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @B(
			      #22 value=(int) 3 (constant type)
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 2
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test007_methodTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						<@A @B(3) T> void foo(T t) {}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #27 @A(
			        target type = 0x1 METHOD_TYPE_PARAMETER
			        type parameter index = 0
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #23 @B(
			        #24 value=(int) 3 (constant type)
			        target type = 0x1 METHOD_TYPE_PARAMETER
			        type parameter index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test008_methodTypeParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_PARAMETER)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						<T1, @A @B(3) T2> void foo(T1 t1,T2 t2) {}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #29 @A(
			        target type = 0x1 METHOD_TYPE_PARAMETER
			        type parameter index = 1
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #25 @B(
			        #26 value=(int) 3 (constant type)
			        target type = 0x1 METHOD_TYPE_PARAMETER
			        type parameter index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test009_classExtends() throws Exception {
		this.runConformTest(
				new String[] {
					"Marker.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						@Target(TYPE_USE)
						@interface Marker {}""",
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"");
		// javac-b81 annotation contents: len:10[0 1 16 -1 -1 0 0 17 0 0]
		String expectedOutput =
			"""
			  RuntimeInvisibleTypeAnnotations:\s
			    #17 @Marker(
			      target type = 0x10 CLASS_EXTENDS
			      type index = -1
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test010_classExtends() throws Exception {
		this.runConformTest(
				new String[] {
					"Marker.java",
					"""
						import java.lang.annotation.*;
						import static java.lang.annotation.ElementType.*;
						@Retention(RetentionPolicy.RUNTIME)
						@Target(TYPE_USE)
						@interface Marker {}""",
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"");
		// Bytes:10[0 1 16 -1 -1 0 0 17 0 0]
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #17 @Marker(
			      target type = 0x10 CLASS_EXTENDS
			      type index = -1
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test011_classExtends_reflection() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {public static void main(String[] argv) {Helper.printTypeAnnotations(X.class);}}",
					"Helper.java",HELPER_CLASS,
					"Marker.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						@Target(TYPE_USE)
						@interface Marker {}"""
				},
				"Annotations on superclass of X\n"+
				"  class java.lang.Object:no annotations");
	}

	public void test012_classExtends_reflection() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {public static void main(String[] argv) {Helper.printTypeAnnotations(X.class);}}",
					"Helper.java",HELPER_CLASS,
					"Marker.java",
					"""
						import java.lang.annotation.*;
						import static java.lang.annotation.ElementType.*;
						@Target(TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface Marker {}"""
				},
				"Annotations on superclass of X\n"+
				"  class java.lang.Object:@Marker");
	}

	public void test013_classExtends_interfaces() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String id() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('(') J {}",
		},
		"");
		// Output from javac b81 lambda
		// RuntimeVisibleTypeAnnotations
		// Bytes:28[0 2 16 0 0 0 0 13 0 1 0 14 115 0 15 16 0 1 0 0 16 0 1 0 17 67 0 18]
		// RuntimeInvisibleTypeAnnotations
		// Bytes:10[0 1 16 0 1 0 0 20 0 0]
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #23 @A(
			      #24 id="Hello, World!" (constant type)
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			    )
			    #26 @C(
			      #27 value='(' (constant type)
			      target type = 0x10 CLASS_EXTENDS
			      type index = 1
			    )
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @B(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 1
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test014_classExtends_interfaces_reflection() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements @A I {public static void main(String[]argv) {Helper.printTypeAnnotations(X.class);}}",
				"Helper.java",HELPER_CLASS,
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
					}
					""",
				"I.java",
				"interface I {}\n"
		},
		"""
			Annotations on superclass of X
			  class java.lang.Object:no annotations
			Annotations on superinterfaces of X
			  interface I:@A""");
	}

	public void test015_classExtends_interfaces_reflection() throws Exception {
		String javaVersion = System.getProperty("java.version");
		int index = javaVersion.indexOf('.');
		if (index != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			index = javaVersion.indexOf('-');
			if (index != -1)
				javaVersion = javaVersion.substring(0, index);
		}
		int v = Integer.parseInt(javaVersion);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('i') J {public static void main(String[] argv) { Helper.printTypeAnnotations(X.class);}}",
				"Helper.java",HELPER_CLASS,
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String id() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
		},
		"Annotations on superclass of X\n" +
		"  class java.lang.Object:no annotations\n" +
		"Annotations on superinterfaces of X\n" +
		"  interface I:@A(id=Hello, World!) \n" +
		"  interface J:@C(" + (v < 14 ? "value=" : "") + "i)");
	}

	public void test016_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface B {
						int value() default -1;
					}""",
				"Y.java",
				"class Y<T> {}\n",
				"X.java",
				"public class X extends Y<@B String> {\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:12[0 1 16 -1 -1 1 3 0 0 13 0 0] // type path: 1,3,0
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #19 @B(
			      target type = 0x10 CLASS_EXTENDS
			      type index = -1
			      location = [TYPE_ARGUMENT(0)]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test017_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"Marker.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Marker { }
					""",
				"I.java",
				"interface I<T> {}\n",
				"X.java",
				"public class X implements I<@Marker String> {\n" +
				"}",
		},
		"");
		// javac-b81: Bytes:12[0 1 16 0 0 1 3 0 0 14 0 0] // type path: 1,3,0
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @Marker(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0)]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test018_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",

				"I.java",
				"interface I<T1,T2> {}\n",

				"X.java",
				"public class X implements I<Integer, @A String> {}\n"
		},
		"");
		// javac-b81: Bytes:12[0 1 16 0 0 1 3 1 0 14 0 0] // type path: 1,3,1
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(1)]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test019_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",

				"J.java",
				"interface J<T> {}\n",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<J<@A String>> {}\n"
		},
		"");
		// javac-b81: Bytes:14[0 1 16 0 0 2 3 0 3 0 0 14 0 0] // type path: 2,3,0,3,0
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test020_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<@A String[]> {}\n"
		},
		"");
		// javac-b81: Bytes:14[0 1 16 0 0 2 3 0 0 0 0 14 0 0] // type path: 2,3,0,0,0
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0), ARRAY]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test021_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<String @A[]> {}\n"
		},
		"");
		// javac-b81: Bytes:12[0 1 16 0 0 1 3 0 0 14 0 0] // type path: 1,3,0
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0)]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test022_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<String []@A[]> {}\n"
		},
		"");
		// javac-b81: Bytes:14[0 1 16 0 0 2 3 0 0 0 0 14 0 0] // type path: 2,3,0,0,0
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0), ARRAY]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test023_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",

				"I.java",
				"interface I<T> {}\n",

				"X.java",
				"public class X implements I<@A String [][][]> {}\n"
		},
		"");
		// javac-b81: Bytes:10[0 1 16 0 0 0 0 12 0 0] // type path: 4,3,0,0,0,0,0,0,0
		String expectedOutput =
				"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test024_classExtends() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I<T> {}\n",
				"J.java",
				"interface J<U,T> {}\n",
				"X.java",
				"public class X implements I<@A(\"Hello, World!\") String>, @B J<String, @C('(') Integer> {}",
		},
		"");
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #25 @A(
			      #26 value="Hello, World!" (constant type)
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0)]
			    )
			    #28 @C(
			      #26 value='(' (constant type)
			      target type = 0x10 CLASS_EXTENDS
			      type index = 1
			      location = [TYPE_ARGUMENT(1)]
			    )
			  RuntimeInvisibleTypeAnnotations:\s
			    #23 @B(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 1
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test025_classTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends @A String> {}",
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {}
					"""
		},
		"");
		// javac-b81: Bytes:10[0 1 17 0 0 0 0 13 0 0]
		// [17 0 0] is CLASS_PARAMETER_BOUND type_parameter_index=0 bound_index=0
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 0
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test026_classTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"public class X<T extends @A String & @B(3) Cloneable> {}",
		},
		"");
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #25 @A(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 0
			    )
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @B(
			      #22 value=(int) 3 (constant type)
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 1
			    )
			""" ;
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test027_classTypeParameterBound_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X<U, T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
		},
		"");
		// javac-b81:
		// Bytes:28[0 2 17 1 0 1 3 0 0 13 0 0 17 1 0 4 3 0 0 0 0 0 0 0 0 14 0 0]
		// Bytes:29[0 2 17 1 0 3 3 0 0 0 0 0 0 16 0 0 17 1 1 0 0 16 0 1 0 17 73 0 18]
		String expectedOutput =
			"""
			  RuntimeVisibleTypeAnnotations:\s
			    #25 @A(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 1 type parameter bound index = 0
			      location = [TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]
			    )
			    #26 @C(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 1 type parameter bound index = 0
			      location = [TYPE_ARGUMENT(0)]
			    )
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @B(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 1 type parameter bound index = 0
			      location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]
			    )
			    #21 @B(
			      #22 value=(int) 3 (constant type)
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 1 type parameter bound index = 1
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test028_methodTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"Z.java",
				"public class Z {}",
				"X.java",
				"""
					public class X {
						<T extends @A Z> void foo(T t) {}
					}""",
		},
		"");
		// javac-b81: Bytes:10[0 1 18 0 0 0 0 13 0 0]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #23 @A(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test029_methodTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"Z.java",
				"public class Z {}",
				"X.java",
				"""
					public class X {
						<T extends @A Z & @B(3) Cloneable> void foo(T t) {}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #27 @A(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 0
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #23 @B(
			        #24 value=(int) 3 (constant type)
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test030_methodTypeParameterBound() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"""
					public class X {
						<T extends Y<Z [][]@B[]> & Cloneable> void foo(T t) {}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #23 @B(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 0
			        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test031_methodTypeParameterBound_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"""
					public class X {
						<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}
					}""",
		},
		"");
		// javac-b81:
		// Bytes:28[0 2 18 0 0 1 3 0 0 13 0 0 18 0 0 4 3 0 0 0 0 0 0 0 0 14 0 0]
		// Bytes:29[0 2 18 0 0 3 3 0 0 0 0 0 0 16 0 0 18 0 1 0 0 16 0 1 0 17 73 0 18]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #27 @A(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 0
			        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]
			      )
			      #28 @C(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 0
			        location = [TYPE_ARGUMENT(0)]
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #23 @B(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 0
			        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]
			      )
			      #23 @B(
			        #24 value=(int) 3 (constant type)
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test032_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {}
					""",

				"X.java",
				"""
					public class X {
						@A int field;
					}""",
		},
		"");
		// javac-b81: Bytes:8[0 1 19 0 0 7 0 0]  19 = 0x13 (FIELD)
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #8 @A(
			        target type = 0x13 FIELD
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test033_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {}
					""",

				"X.java",
				"""
					public class X {
						java.util.List<@A String> field;
					}""",
		},
		"");
		// javac-b81: Bytes:10[0 1 19 1 3 0 0 9 0 0]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #10 @A(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test034_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						@B(3) @A int field;
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #12 @A(
			        target type = 0x13 FIELD
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @B(
			        #9 value=(int) 3 (constant type)
			        target type = 0x13 FIELD
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test035_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {}
					""",

				"X.java",
				"""
					public class X {
						java.util.Map<String, @A String> field;
					}""",
		},
		"");
		// javac-b81: Bytes:10[0 1 19 1 3 1 0 9 0 0]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #10 @A(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1)]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test036_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {}
					""",

				"X.java",
				"""
					public class X {
						java.util.List<String[][]@A[][]> field;
					}""",
		},
		"");
		// javac-b81: Bytes:14[0 1 19 3 3 0 0 0 0 0 0 9 0 0]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #10 @A(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test037_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						java.util.List<? extends @A Number> field;
					}""",
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {}
					""",
		},
		"");
		// javac-b81: Bytes:12[0 1 19 2 3 0 2 0 0 9 0 0]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #10 @A(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), WILDCARD]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test038_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class AA { class BB<T> {}}\
					class X {
					  AA.@A BB field;
					}
					""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeVisibleTypeAnnotations:\s
			      #8 @A(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test038a_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class AA { class BB<T> {}}\
					class X {
					  @B AA.@A BB[] @C[] field;
					}
					""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A { }
					""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B { }
					""",

				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C { }
					""",
		},
		"");

	String expectedOutput =
				"""
		    RuntimeVisibleTypeAnnotations:\s
		      #8 @B(
		        target type = 0x13 FIELD
		        location = [ARRAY, ARRAY]
		      )
		      #9 @A(
		        target type = 0x13 FIELD
		        location = [ARRAY, ARRAY, INNER_TYPE]
		      )
		      #10 @C(
		        target type = 0x13 FIELD
		        location = [ARRAY]
		      )
		""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test039_field() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						@A int [] @B(3) [] field;
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #12 @A(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY]
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @B(
			        #9 value=(int) 3 (constant type)
			        target type = 0x13 FIELD
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test040_field_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					import java.util.List;
					public class X {
						@H String @E[] @F[] @G[] field;
						@A Map<@B String, @C List<@D Object>> field2;
						@A Map<@B String, @H String @E[] @F[] @G[]> field3;
					}""",
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"D.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface D {
						String value() default "default";
					}
					""",
				"E.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface E {
						int value() default -1;
					}""",
				"F.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface F {
						char value() default '-';
					}
					""",
				"G.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface G {
						int value() default -1;
					}""",
				"H.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface H {
						char value() default '-';
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			  // Field descriptor #6 [[[Ljava/lang/String;
			  java.lang.String[][][] field;
			    RuntimeVisibleTypeAnnotations:\s
			      #11 @H(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY, ARRAY]
			      )
			      #12 @F(
			        target type = 0x13 FIELD
			        location = [ARRAY]
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @E(
			        target type = 0x13 FIELD
			      )
			      #9 @G(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY]
			      )
			 \s
			  // Field descriptor #14 Ljava/util/Map;
			  // Signature: Ljava/util/Map<Ljava/lang/String;Ljava/util/List<Ljava/lang/Object;>;>;
			  java.util.Map field2;
			    RuntimeVisibleTypeAnnotations:\s
			      #18 @A(
			        target type = 0x13 FIELD
			      )
			      #19 @C(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1)]
			      )
			      #20 @D(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #17 @B(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			 \s
			  // Field descriptor #14 Ljava/util/Map;
			  // Signature: Ljava/util/Map<Ljava/lang/String;[[[Ljava/lang/String;>;
			  java.util.Map field3;
			    RuntimeVisibleTypeAnnotations:\s
			      #18 @A(
			        target type = 0x13 FIELD
			      )
			      #11 @H(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), ARRAY, ARRAY, ARRAY]
			      )
			      #12 @F(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), ARRAY]
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #17 @B(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			      #8 @E(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1)]
			      )
			      #9 @G(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test041_field() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						java.lang.@H String @E[] @F[] @G[] field;
					}""",
				"E.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface E {
						int value() default -1;
					}""",
				"F.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface F {
						char value() default '-';
					}
					""",
				"G.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface G {
						int value() default -1;
					}""",
				"H.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface H {
						char value() default '-';
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #11 @H(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY, ARRAY]
			      )
			      #12 @F(
			        target type = 0x13 FIELD
			        location = [ARRAY]
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @E(
			        target type = 0x13 FIELD
			      )
			      #9 @G(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test042_methodReturnType() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						@B(3) @A(value="test") int foo() {
							return 1;
						}
					}""",
		},
		"");
		// javac-b81:
		// Bytes:13[0 1 20 0 0 11 0 1 0 12 115 0 13]
		// Bytes:13[0 1 20 0 0 15 0 1 0 12 73 0 16]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #21 @A(
			        #18 value="test" (constant type)
			        target type = 0x14 METHOD_RETURN
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #17 @B(
			        #18 value=(int) 3 (constant type)
			        target type = 0x14 METHOD_RETURN
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test043_methodReceiver() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						void foo(@B(3) X this) {}
					}""",
		},
		"");
		// javac-b81: Bytes:13[0 1 21 0 0 10 0 1 0 11 73 0 12]
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #16 @B(
			        #17 value=(int) 3 (constant type)
			        target type = 0x15 METHOD_RECEIVER
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test044_methodReceiver() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						void foo(X<@B(3) T> this) {}
					}""",
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
		},
		"");
		// javac-b81: Bytes:15[0 1 21 1 3 0 0 10 0 1 0 11 73 0 12]
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #18 @B(
			        #19 value=(int) 3 (constant type)
			        target type = 0x15 METHOD_RECEIVER
			        location = [TYPE_ARGUMENT(0)]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test045_methodParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						int foo(@B(3) String s) {
							return s.length();
						}
					}""",

				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
		},
		"");
		// javac-b81: Bytes:14[0 1 22 0 0 0 11 0 1 0 12 73 0 13]
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #25 @B(
			        #26 value=(int) 3 (constant type)
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test046_methodParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						int foo(int i, double d, @B(3) String s) {
							return s.length();
						}
					}""",

				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
		},
		"");
		// javac-b81: Bytes:14[0 1 22 1 0 0 11 0 1 0 12 73 0 13]
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #29 @B(
			        #30 value=(int) 3 (constant type)
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 2
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test047_methodParameterArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						int foo(String @A [] @B(3) [] s) {
							return s.length;
						}
					}""",
		},
		"");
		// javac-b81:
		// Bytes:9[0 1 22 0 0 0 11 0 0]
		// Bytes:16[0 1 22 0 1 0 0 0 13 0 1 0 14 73 0 15]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #23 @A(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #19 @B(
			        #20 value=(int) 3 (constant type)
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test048_throws() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"E.java",
				"""
					class E extends RuntimeException {
						private static final long serialVersionUID = 1L;
					}
					""",
				"E1.java",
				"""
					class E1 extends RuntimeException {
						private static final long serialVersionUID = 1L;
					}
					""",
				"E2.java",
				"""
					class E2 extends RuntimeException {
						private static final long serialVersionUID = 1L;
					}
					""",
				"X.java",
				"""
					public class X {
						void foo() throws @A("Hello, World!") E, E1, @B @C('(') E2 {}
					}""",
		},
		"");
		// javac-b81:
		// Bytes:28[0 2 23 0 0 0 0 14 0 1 0 15 115 0 16 23 0 2 0 0 17 0 1 0 15 67 0 18]
		// Bytes:10[0 1 23 0 2 0 0 20 0 0]
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #25 @A(
			        #26 value="Hello, World!" (constant type)
			        target type = 0x17 THROWS
			        throws index = 0
			      )
			      #28 @C(
			        #26 value='(' (constant type)
			        target type = 0x17 THROWS
			        throws index = 2
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #23 @B(
			        target type = 0x17 THROWS
			        throws index = 2
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test049_codeblocks_localVariable() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					public class X {
						public static void main(String[] args) {
							@B int j = 9;
							try {
								System.out.print("SUCCESS" + j);
							} catch(@A Exception e) {
							}
							@B int k = 3;
							System.out.println(k);
						}
					}""",
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						String value() default "default";
					}
					""",
		},
		"SUCCESS93");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #56 @B(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 3, pc: 39] index: 1
			      )
			      #56 @B(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 31, pc: 39] index: 2
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test050_codeblocks_localVariable() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"X.java",
				"""
					public class X {
						String[][] bar() {
							return new String[][] {};\
						}
						void foo(String s) {
							@C int i;
							@A String [] @B(3)[] tab = bar();
							if (tab != null) {
								i = 0;
								System.out.println(i + tab.length);
							} else {
								System.out.println(tab.length);
							}
							i = 4;
							System.out.println(-i + tab.length);
						}
					}""",
		},
		"");
		// javac-b81:
		// Bytes:34[0 2 64 0 1 0 34 0 12 0 2 0 0 19 0 0 64 0 1 0 5 0 41 0 3 2 0 0 0 0 0 20 0 0]
		// Bytes:23[0 1 64 0 1 0 5 0 41 0 3 1 0 0 0 22 0 1 0 23 73 0 24]
		// ECJ data varies a little here as it is splitting the range
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #45 @B(
			        #46 value=(int) 3 (constant type)
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 5, pc: 46] index: 3
			        location = [ARRAY]
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #49 @C(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 11, pc: 24] index: 2
			          [pc: 34, pc: 46] index: 2
			      )
			      #50 @A(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 5, pc: 46] index: 3
			        location = [ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test051_codeblocks_resourceVariable() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					import java.io.*;
					public class X {
					   public static void main(String[] argv) throws Exception {
					     try (@A BufferedReader br1 = new BufferedReader(new FileReader("a"));
					          @B(99) BufferedReader br2 = new BufferedReader(new FileReader("b"))) {
					       System.out.println(br1.readLine()+br2.readLine());
					     }
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #81 @B(
			        #82 value=(int) 99 (constant type)
			        target type = 0x41 RESOURCE_VARIABLE
			        local variable entries:
			          [pc: 39, pc: 94] index: 4
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #85 @A(
			        target type = 0x41 RESOURCE_VARIABLE
			        local variable entries:
			          [pc: 21, pc: 135] index: 3
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test052_codeblocks_exceptionParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					public class X {
						public static void main(String[] args) {
							Exception test = new Exception() {
								private static final long serialVersionUID = 1L;
								@Override
								public String toString() {
									return "SUCCESS";
								}
							};
							try {
								System.out.println(test);
							} catch(@A Exception e) {
								e.printStackTrace();
							}
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
		},
		"SUCCESS");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #44 @A(
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test053_codeblocks_exceptionParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					public class X {
						public static void main(String[] args) {
							@A Exception test = new Exception() {
								private static final long serialVersionUID = 1L;
								@Override
								public String toString() {
									return "SUCCESS";
								}
							};
							try {
								System.out.println(test);
							} catch(@A Exception e) {
								e.printStackTrace();
							}
						}
					}""",
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
		},
		"SUCCESS");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #44 @A(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 8, pc: 24] index: 1
			      )
			      #44 @A(
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test054_codeblocks_exceptionParameter() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					public class X {
						public static void main(String[] args) {
							try {
								System.out.println(42);
							} catch(@B(1) RuntimeException e) {
								e.printStackTrace();
							} catch(@B(2) Throwable t) {
								t.printStackTrace();
							}
						}
					}""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"42");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #44 @B(
			        #45 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #44 @B(
			        #45 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055_codeblocks_exceptionParameterMultiCatch() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"class Exc1 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc2 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"class Exc3 extends RuntimeException {" +
				"    private static final long serialVersionUID = 1L;\n" +
				"}\n"+
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(42);\n" +
				// @B(1) is attached to the argument, the others are attached to the type reference in the union type reference
				// During Parsing the @B(1) is moved from the argument to Exc1
				"		} catch(@B(1) Exc1 | Exc2 | @B(2) Exc3 t) {\n" +
				"			t.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"42");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #45 @B(
			        #46 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #45 @B(
			        #46 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 2
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test056_codeblocks_instanceof() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo(Object o) {
							if (o instanceof @A String) {
								String tab = (String) o;
								System.out.println(tab);
							}
							System.out.println(o);
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #38 @A(
			        target type = 0x43 INSTANCEOF
			        offset = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);

		expectedOutput = "     1  instanceof java.lang.String [16]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test057_codeblocks_new() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new @B(3) Object();
							return true;
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #35 @B(
			        #36 value=(int) 3 (constant type)
			        target type = 0x44 NEW
			        offset = 8
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test058_codeblocks_new2() throws Exception {
		this.runConformTest(
			new String[] {
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default -1;
					}""",

				"X.java",
				"""
					public class X {
						public void foo() {
					       Outer o = new Outer();
					       o.new @B(1) Inner();
						}
					}
					class Outer { class Inner {}}
					"""
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #30 @B(
			        #31 value=(int) 1 (constant type)
			        target type = 0x44 NEW
			        offset = 8
			        location = [INNER_TYPE]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test057_codeblocks_new3_415821() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface X { }
					
					class Foo {}
					""",
				"C.java",
				"class C { void m() { new @X Foo() {}; } }\n",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #21 @X(
			        target type = 0x44 NEW
			        offset = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C.class", "C", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
			"""
				  RuntimeVisibleTypeAnnotations:\s
				    #28 @X(
				      target type = 0x10 CLASS_EXTENDS
				      type index = -1
				    )
				""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C$1.class", "C$1", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test057_codeblocks_new4_415821() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface X { }
					
					""",
				"C.java",
				"class C { void m() { new @X Runnable() { public void run() {}}; } }\n",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #21 @X(
			        target type = 0x44 NEW
			        offset = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C.class", "C", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
			"""
				  RuntimeVisibleTypeAnnotations:\s
				    #31 @X(
				      target type = 0x10 CLASS_EXTENDS
				      type index = 0
				    )
				""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C$1.class", "C$1", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test059_codeblocks_new_newArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new @A String [1];
							return true;
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #37 @A(
			        target type = 0x44 NEW
			        offset = 9
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060_codeblocks_new_multiNewArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new @A String [2][3];
							return true;
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #37 @A(
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060a_codeblocks_new_newArrayWithInitializer() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							X[][] x = new @A X @B [] @C[]{ { null }, { null } };
							return true;
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface A {
						String value() default "default";
					}
					""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						String value() default "default";
					}
					""",

				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface C {
						String value() default "default";
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #37 @A(
			        target type = 0x44 NEW
			        offset = 9
			        location = [ARRAY, ARRAY]
			      )
			      #38 @B(
			        target type = 0x44 NEW
			        offset = 9
			      )
			      #39 @C(
			        target type = 0x44 NEW
			        offset = 9
			        location = [ARRAY]
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060b_codeblocks_new_multiNewArray() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							X[][] x = new @A X @B [1] @C[2];
							return true;
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface A {
						String value() default "default";
					}
					""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						String value() default "default";
					}
					""",

				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface C {
						String value() default "default";
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #36 @A(
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY, ARRAY]
			      )
			      #37 @B(
			        target type = 0x44 NEW
			        offset = 10
			      )
			      #38 @C(
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY]
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060c_codeblocks_new_multiNewArray() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							X [][][] x = new @A X @B[10] @C[10] @D[];
							return true;
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface A {
						String value() default "default";
					}
					""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						String value() default "default";
					}
					""",

				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface C {
						String value() default "default";
					}
					""",

				"D.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface D {
						String value() default "default";
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #36 @A(
			        target type = 0x44 NEW
			        offset = 12
			        location = [ARRAY, ARRAY, ARRAY]
			      )
			      #37 @B(
			        target type = 0x44 NEW
			        offset = 12
			      )
			      #38 @C(
			        target type = 0x44 NEW
			        offset = 12
			        location = [ARRAY]
			      )
			      #39 @D(
			        target type = 0x44 NEW
			        offset = 12
			        location = [ARRAY, ARRAY]
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060d_codeblocks_new_arraysWithNestedTypes() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new @B(1) Outer.@B(2) Inner @B(3) [2];
							return true;
						}
					}
					class Outer { class Inner {}}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #37 @B(
			        #38 value=(int) 1 (constant type)
			        target type = 0x44 NEW
			        offset = 9
			        location = [ARRAY]
			      )
			      #37 @B(
			        #38 value=(int) 2 (constant type)
			        target type = 0x44 NEW
			        offset = 9
			        location = [ARRAY, INNER_TYPE]
			      )
			      #37 @B(
			        #38 value=(int) 3 (constant type)
			        target type = 0x44 NEW
			        offset = 9
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060e_codeblocks_new_arraysWithNestedTypes() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new @B(1) Outer.@B(2) Inner @B(3) [2] @B(4)[4];
							return true;
						}
					}
					class Outer { class Inner {}}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #37 @B(
			        #38 value=(int) 1 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY, ARRAY]
			      )
			      #37 @B(
			        #38 value=(int) 2 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY, ARRAY, INNER_TYPE]
			      )
			      #37 @B(
			        #38 value=(int) 3 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			      )
			      #37 @B(
			        #38 value=(int) 4 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test060f_codeblocks_new_arraysWithQualifiedNestedTypes() throws Exception {
		this.runConformTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"""
					package org.foo.bar;
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new org.foo.bar.@B(1) Outer.@B(2) Inner @B(3) [2] @B(4)[4];
							return true;
						}
					}
					class Outer { class Inner {}}
					""",
				"B.java",
				"""
					package org.foo.bar;
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #37 @org.foo.bar.B(
			        #38 value=(int) 1 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY, ARRAY]
			      )
			      #37 @org.foo.bar.B(
			        #38 value=(int) 2 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY, ARRAY, INNER_TYPE]
			      )
			      #37 @org.foo.bar.B(
			        #38 value=(int) 3 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			      )
			      #37 @org.foo.bar.B(
			        #38 value=(int) 4 (constant type)
			        target type = 0x44 NEW
			        offset = 10
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "org" + File.separator + "foo" + File.separator + "bar" + File.separator + "X.class",
				"org.foo.bar.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test061_codeblocks_new_newArrayWithInitializer() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new @A String []{"xyz"};
							return true;
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #37 @A(
			        target type = 0x44 NEW
			        offset = 9
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test062_codeblocks_newArray() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new String @A[1];
							return true;
						}
					}""",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #37 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			// no type path expected here
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test062_codeblocks_newArrayWithInitializer() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"""
					public class X {
						public boolean foo(String s) {
							System.out.println("xyz");
							Object o = new String @A[] { "Hello" };
							return true;
						}
					}""",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #39 @A(\n" +
			"        target type = 0x44 NEW\n" +
			"        offset = 9\n" +
			// no type path expected here
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test063_codeblocks_new_instanceof() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"""
					public class X {
						public boolean foo(Object o) {
							boolean b = (o instanceof @C('_') Object[]);
							Object o1 = new @B(3) @A("new Object") Object[] {};
							return b;
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #24 @B(
			        #25 value=(int) 3 (constant type)
			        target type = 0x44 NEW
			        offset = 6
			        location = [ARRAY]
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #28 @C(
			        #25 value='_' (constant type)
			        target type = 0x43 INSTANCEOF
			        offset = 1
			        location = [ARRAY]
			      )
			      #30 @A(
			        #25 value="new Object" (constant type)
			        target type = 0x44 NEW
			        offset = 6
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test064_codeblocks_constructorReference() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					interface MR { X process(String input); }
					public class X<T> {
					   public X(T t) {}
					   public static <T> String foo(String bar) { return bar; }
						public void bar() {
					       System.out.println("abc");
					       MR ref = @A X::new;
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #48 @A(
			        target type = 0x45 CONSTRUCTOR_REFERENCE
			        offset = 8
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test065_codeblocks_methodReference() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					interface MR { String process(String input); }
					public class X<T> {
					   public static <T> String foo(String bar) { return bar; }
						public void bar() {
					       System.out.println("abc");
					       MR ref = @A X::foo;
					       ref.process("abc");
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #47 @A(
			        target type = 0x46 METHOD_REFERENCE
			        offset = 8
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test066_codeblocks_methodReference() throws Exception {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"""
					interface I {
					    Object copy(int [] ia);
					}
					public class X  {
					    public static void main(String [] args) {
					        I i = @B(1) int @B(2)[]::<String>clone;
					        i.copy(new int[10]);\s
					    }
					}
					""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default -1;
					}
					""",
			};
		if (this.complianceLevel < ClassFileConstants.JDK9) { // luckily introduction of ecj warning and javac crash coincide
			runner.runConformTest();
		} else {
			runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 6)
						I i = @B(1) int @B(2)[]::<String>clone;
						                          ^^^^^^
					Unused type arguments for the non generic method clone() of type Object; it should not be parameterized with arguments <String>
					----------
					""";
			runner.javacTestOptions = JavacHasABug.JavacThrowsAnExceptionForJava_since9_EclipseWarns;
			runner.runWarningTest();
		}

		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #30 @B(
			        #31 value=(int) 1 (constant type)
			        target type = 0x46 METHOD_REFERENCE
			        offset = 0
			        location = [ARRAY]
			      )
			      #30 @B(
			        #31 value=(int) 2 (constant type)
			        target type = 0x46 METHOD_REFERENCE
			        offset = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test067_codeblocks_constructorReferenceTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					interface MR { X process(String input); }
					public class X<T> {
					   public X(T s) {};
					   public static <T> String foo(String bar) { return bar; }
						public void bar() {
					       System.out.println("abc");
					       MR ref = X<String>::<@A String>new;
					       ref.process("abc");
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #53 @A(
			        target type = 0x4a CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT
			        offset = 8
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test068_codeblocks_methodReferenceTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					interface MR { String process(String input); }
					public class X<T> {
					   public static <T> String foo(String bar) { return bar; }
						public void bar() {
					       System.out.println("abc");
					       MR ref = X::<@A String>foo;
					       ref.process("abc");
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #47 @A(
			        target type = 0x4b METHOD_REFERENCE_TYPE_ARGUMENT
			        offset = 8
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test069_codeblocks_cast() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo(Object o) {
							if (o instanceof String) {
								String tab = (@A String) o;
								System.out.println(tab);
							}
							System.out.println(o);
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		// javac-b81: Bytes:11[0 1 71 0 7 0 0 0 16 0 0]
		// relevant numbers '71 0 7 0' which mean 0x47 (CAST) at offset 7
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #38 @A(
			        target type = 0x47 CAST
			        offset = 8
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070_codeblocks_cast_complex() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"""
					public class X {
						public void foo(Object o) {
							if (o instanceof String[][]) {
								String[][] tab = (@C('_') @B(3) String[] @A[]) o;
								System.out.println(tab.length);
							}
							System.out.println(o);
						}
					}""",
		},
		"");
		// javac-b81:
		// Bytes:31[0 2 71 0 7 0 1 0 0 0 16 0 0 71 0 7 0 2 0 0 0 0 0 17 0 1 0 18 67 0 19]
		// Bytes:20[0 1 71 0 7 0 2 0 0 0 0 0 21 0 1 0 18 73 0 22]
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #37 @B(
			        #38 value=(int) 3 (constant type)
			        target type = 0x47 CAST
			        offset = 8
			        type argument index = 0
			        location = [ARRAY, ARRAY]
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #41 @C(
			        #38 value='_' (constant type)
			        target type = 0x47 CAST
			        offset = 8
			        type argument index = 0
			        location = [ARRAY, ARRAY]
			      )
			      #43 @A(
			        target type = 0x47 CAST
			        offset = 8
			        type argument index = 0
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070a_codeblocks_castWithIntersectionCast() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"""
				import java.io.*;
				public class X {
				   public void foo(Object o) {
					  I i = (@B(1) I & J) o;
					  J j = (I & @B(2) J) o;
				   }
				}
				interface I {}
				interface J {}
				""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 1;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  // Method descriptor #15 (Ljava/lang/Object;)V
			  // Stack: 1, Locals: 4
			  public void foo(java.lang.Object o);
			     0  aload_1 [o]
			     1  checkcast J [16]
			     4  checkcast I [18]
			     7  astore_2 [i]
			     8  aload_1 [o]
			     9  checkcast J [16]
			    12  checkcast I [18]
			    15  astore_3 [j]
			    16  return
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 8, line: 5]
			        [pc: 16, line: 6]
			      Local variable table:
			        [pc: 0, pc: 17] local: this index: 0 type: X
			        [pc: 0, pc: 17] local: o index: 1 type: java.lang.Object
			        [pc: 8, pc: 17] local: i index: 2 type: I
			        [pc: 16, pc: 17] local: j index: 3 type: J
			    RuntimeVisibleTypeAnnotations:\s
			      #27 @B(
			        #28 value=(int) 1 (constant type)
			        target type = 0x47 CAST
			        offset = 4
			        type argument index = 0
			      )
			      #27 @B(
			        #28 value=(int) 2 (constant type)
			        target type = 0x47 CAST
			        offset = 9
			        type argument index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070b_codeblocks_castWithIntersectionCast() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"""
				import java.io.*;
				public class X {
				   public void foo(Object o) {
				     System.out.println(123);
					  I<String> i = (I<@B(1) String> & @B(2) J<String>) o;
				   }
				}
				interface I<T> {}
				interface J<T> {}
				""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 1;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  public void foo(java.lang.Object o);
			     0  getstatic java.lang.System.out : java.io.PrintStream [16]
			     3  bipush 123
			     5  invokevirtual java.io.PrintStream.println(int) : void [22]
			     8  aload_1 [o]
			     9  checkcast J [28]
			    12  checkcast I [30]
			    15  astore_2 [i]
			    16  return
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 8, line: 5]
			        [pc: 16, line: 6]
			      Local variable table:
			        [pc: 0, pc: 17] local: this index: 0 type: X
			        [pc: 0, pc: 17] local: o index: 1 type: java.lang.Object
			        [pc: 16, pc: 17] local: i index: 2 type: I
			      Local variable type table:
			        [pc: 16, pc: 17] local: i index: 2 type: I<java.lang.String>
			    RuntimeVisibleTypeAnnotations:\s
			      #39 @B(
			        #40 value=(int) 2 (constant type)
			        target type = 0x47 CAST
			        offset = 9
			        type argument index = 1
			      )
			      #39 @B(
			        #40 value=(int) 1 (constant type)
			        target type = 0x47 CAST
			        offset = 12
			        type argument index = 0
			        location = [TYPE_ARGUMENT(0)]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070c_codeblocks_castTwiceInExpression() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"""
				import java.io.*;
				public class X {
				   public void foo(Object o) {
				     System.out.println(123);
					  I i = (@B(1) I)(@B(2) J) o;
				   }
				}
				interface I {}
				interface J {}
				""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 1;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			     0  getstatic java.lang.System.out : java.io.PrintStream [16]
			     3  bipush 123
			     5  invokevirtual java.io.PrintStream.println(int) : void [22]
			     8  aload_1 [o]
			     9  checkcast J [28]
			    12  checkcast I [30]
			    15  astore_2 [i]
			    16  return
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 8, line: 5]
			        [pc: 16, line: 6]
			      Local variable table:
			        [pc: 0, pc: 17] local: this index: 0 type: X
			        [pc: 0, pc: 17] local: o index: 1 type: java.lang.Object
			        [pc: 16, pc: 17] local: i index: 2 type: I
			    RuntimeVisibleTypeAnnotations:\s
			      #37 @B(
			        #38 value=(int) 2 (constant type)
			        target type = 0x47 CAST
			        offset = 9
			        type argument index = 0
			      )
			      #37 @B(
			        #38 value=(int) 1 (constant type)
			        target type = 0x47 CAST
			        offset = 12
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test070d_codeblocks_castDoubleIntersectionCastInExpression() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
			"""
				import java.io.*;
				public class X {
				   public void foo(Object o) {
				     System.out.println(123);
					  I i = (@B(1) I & J)(K & @B(2) L) o;
				   }
				}
				interface I {}
				interface J {}
				interface K {}
				interface L {}
				""",

				"B.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 1;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  public void foo(java.lang.Object o);
			     0  getstatic java.lang.System.out : java.io.PrintStream [16]
			     3  bipush 123
			     5  invokevirtual java.io.PrintStream.println(int) : void [22]
			     8  aload_1 [o]
			     9  checkcast L [28]
			    12  checkcast K [30]
			    15  checkcast J [32]
			    18  checkcast I [34]
			    21  astore_2 [i]
			    22  return
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 8, line: 5]
			        [pc: 22, line: 6]
			      Local variable table:
			        [pc: 0, pc: 23] local: this index: 0 type: X
			        [pc: 0, pc: 23] local: o index: 1 type: java.lang.Object
			        [pc: 22, pc: 23] local: i index: 2 type: I
			    RuntimeVisibleTypeAnnotations:\s
			      #41 @B(
			        #42 value=(int) 2 (constant type)
			        target type = 0x47 CAST
			        offset = 9
			        type argument index = 1
			      )
			      #41 @B(
			        #42 value=(int) 1 (constant type)
			        target type = 0x47 CAST
			        offset = 18
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test071_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"X.java",
				"""
					public class X {
						<T> X(T t) {
						}
						public Object foo() {
							X x = new <@A @B(1) String>X(null);
							return x;
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #27 @B(
			        #28 value=(int) 1 (constant type)
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 5
			        type argument index = 0
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #31 @A(
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 5
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test072_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"X.java",
				"""
					public class X {
						<T, U> X(T t, U u) {
						}
						public Object foo() {
							X x = new <@A Integer, @A String @C [] @B(1)[]>X(null, null);
							return x;
						}
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #29 @B(
			        #30 value=(int) 1 (constant type)
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 6
			        type argument index = 1
			        location = [ARRAY]
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #33 @A(
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 6
			        type argument index = 0
			      )
			      #33 @A(
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 6
			        type argument index = 1
			        location = [ARRAY, ARRAY]
			      )
			      #34 @C(
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 6
			        type argument index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test073_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T1, T2> {
						public void bar() {
					       new <String, @A T2>X();
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #19 @A(
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 3
			        type argument index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test074_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T1,T2> {
					   public static void foo(int i) {}
						public void bar() {
					       new <java.util.List<@A String>, T2>X();
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #23 @A(
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 3
			        type argument index = 0
			        location = [TYPE_ARGUMENT(0)]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test075_codeblocks_constructorInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						public void bar() {
					       new <@A T>X();
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #19 @A(
			        target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			        offset = 3
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test076_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						static <T, U> T foo(T t, U u) {
							return t;
						}
						public static void main(String[] args) {
							System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{"SUCCESS"}, null)[0]);
						}
					}
					""",
				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
		},
		"SUCCESS");
		String expectedOutput =
			"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #48 @B(
			        #49 value=(int) 1 (constant type)
			        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT
			        offset = 13
			        type argument index = 0
			        location = [ARRAY]
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #52 @A(
			        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT
			        offset = 13
			        type argument index = 0
			        location = [ARRAY]
			      )
			      #53 @C(
			        #49 value='-' (constant type)
			        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT
			        offset = 13
			        type argument index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test077_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T1,T2> {
					   public static void foo(int i) {}
						public void bar() {
					       X.<String, @A T2>foo(42);
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #24 @A(
			        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT
			        offset = 2
			        type argument index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test078_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T1,T2> {
					   public static void foo(int i) {}
						public void bar() {
					       X.<java.util.List<@A String>, T2>foo(42);
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",

		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #24 @A(
			        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT
			        offset = 2
			        type argument index = 0
			        location = [TYPE_ARGUMENT(0)]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test079_codeblocks_methodInvocationTypeArgument() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					   public static void foo(int i) {}
						public void bar() {
					       X.<@A T>foo(42);
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface A {
						String value() default "default";
					}
					""",
		},
		"");
		// Example bytes:11[0 1 73 0 0 0 0 0 13 0 0] this would be for offset 0
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #24 @A(
			        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT
			        offset = 2
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	// Annotation should appear twice in this case
	public void test080_multiuseAnnotations() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						 @B(1) int foo() { return 0; }
					}""",
				"B.java",
				"""
					import java.lang.annotation.*;
					@Target({ElementType.METHOD, ElementType.TYPE_USE})
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeVisibleAnnotations:\s
			      #17 @B(
			        #18 value=(int) 1 (constant type)
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #17 @B(
			        #18 value=(int) 1 (constant type)
			        target type = 0x14 METHOD_RETURN
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test081_multiuseAnnotations() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target({ElementType.METHOD, ElementType.TYPE_USE})
					@interface Annot {
						int value() default 0;
					}
					public class X {
						@Annot(4) public String foo() { return "hello"; }\
					}""",
		},
		"");
		String expectedOutput =
			"""
			    RuntimeInvisibleAnnotations:\s
			      #17 @Annot(
			        #18 value=(int) 4 (constant type)
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #17 @Annot(
			        #18 value=(int) 4 (constant type)
			        target type = 0x14 METHOD_RETURN
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// When not annotated with any TYPE it assumes the Java7 set (i.e. not TYPE_USE/TYPE_PARAMETER)
	public void test082_multiuseAnnotations() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target({ElementType.METHOD, ElementType.TYPE_USE})
					@interface Annot {\r
						int value() default 0;\r
					}\r
					public class X {\r
						@Annot(4)\r
						public void foo() {\r
						}\r
					}""",
		},
		"");
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  public void foo();
			    0  return
			      Line numbers:
			        [pc: 0, line: 9]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			    RuntimeInvisibleAnnotations:\s
			      #16 @Annot(
			        #17 value=(int) 4 (constant type)
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// as of https://bugs.openjdk.java.net/browse/JDK-8231435 no-@Target annotations are legal also in TYPE_USE/TYPE_PARAMETER position
	public void test083_multiuseAnnotations() throws Exception {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target({ElementType.METHOD, ElementType.TYPE_USE})
					@interface Annot {
						int value() default 0;
					}
					public class X<@Annot(1) T> {
						java.lang. @Annot(2)String f;
						public void foo(String @Annot(3)[] args) {
						}
					}
					""",
			};
		runner.expectedCompilerLog = "";
		runner.runConformTest();

		String expectedOutput =
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  java.lang.String f;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Annot(\n" +
				"        #9 value=(int) 2 (constant type)\n" +  // <-2-
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #12 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [14]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X<T>\n" +
				"  \n" +
				"  // Method descriptor #23 ([Ljava/lang/String;)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  public void foo(java.lang.String[] args);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 9]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: args index: 1 type: java.lang.String[]\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Annot(\n" +
				"        #9 value=(int) 3 (constant type)\n" +  // <-3-
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #8 @Annot(\n" +
				"      #9 value=(int) 1 (constant type)\n" +  // <-1-
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 0\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100_pqtr() throws Exception { // PQTR (ParameterizedQualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  java.util.@B(2) List<String> field2;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100a_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  java.util.@B(2) List<String>[] field3;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100b_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  java.util.List<@B(3) String>[] field3;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, TYPE_ARGUMENT(0)]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100c_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  java.util.List<String> @B(3)[] field3;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100d_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  java.util.@B(2) List<@B(5) String> @B(3)[]@B(4)[] field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY]
				      )
				      #10 @B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				      )
				      #10 @B(
				        #11 value=(int) 4 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				      #10 @B(
				        #11 value=(int) 5 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, TYPE_ARGUMENT(0)]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test100e_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  java.util.Map.@B(2) Entry<String,String> field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100f_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"Foo.java",
					"class Foo {}\n",

					"Levels.java",
					"package one.two.three;\n" +
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"X.java",
					"""
						package one.two.three;
						class X {
						  one.two.three.Level1.Level2.@B(2) Level3.Level4.@B(3) Level5<String> instance;
						}
						""",

					"B.java",
					"""
						package one.two.three;
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @one.two.three.B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [INNER_TYPE]
				      )
				      #10 @one.two.three.B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator + "X.class", "one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100g_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"Foo.java",
					"class Foo {}\n",

					"Levels.java",
					"package one.two.three;\n" +
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"X.java",
					"""
						package one.two.three;
						class X {
						  one.two.three.Level1.Level2.@B(2) Level3.Level4.@B(3) Level5<String>[][] instance;
						}
						""",

					"B.java",
					"""
						package one.two.three;
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @one.two.three.B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE]
				      )
				      #10 @one.two.three.B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator + "X.class", "one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100h_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  Level1.Level2.@B(2) Level3.Level4.@B(3) Level5<String>[][] instance;
						}
						""",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE]
				      )
				      #10 @B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100i_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  Level1.Level2.Level3.Level4.Level5<@B(1) String>[][] instance;
						}
						""",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3 { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100j_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  Level1.Level2.Level3<@B(1) String>.Level4.Level5<@B(2) String>[][] instance;
						}
						""",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3<Q> { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]
				      )
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test100k_pqtr() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  Level1.@B(5) Level2.Level3<@B(1) String>.Level4.Level5<@B(2) String>[][] instance;
						}
						""",

					"Levels.java",
					"class Level1 { static class Level2 { class Level3<Q> { class Level4 { class Level5<T> { } } } } }\n",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 5 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY]
				      )
				      #10 @B(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]
				      )
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test101a_qtr() throws Exception { // QTR (QualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    com.foo.@B(2) List field2;
						}
						""",

					"List.java",
					"package com.foo;\n"+
					"public class List {}\n",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}


	public void test101b_qtr() throws Exception { // QTR (QualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						  java.util.Map.@B(2) Entry field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test101c_qtr() throws Exception { // QTR (QualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"Runner.java",
					"public class Runner {}\n",

					"B.java",
					"""
						package one.two.three;
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",

					"X.java",
					"""
						package one.two.three;
						class X {
						    one.two.three.Level1.Level2.@B(2) Level3.Level4.@B(3) Level5 instance;
						}
						""",

					"Level1.java",
					"package one.two.three;\n" +
					"public class Level1 { static class Level2 { class Level3 { class Level4 { class Level5 { } } } } }\n",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @one.two.three.B(
				        #9 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [INNER_TYPE]
				      )
				      #8 @one.two.three.B(
				        #9 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator + "X.class", "one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test102a_str() throws Exception { // STR (SingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    @B(1) X field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test102b_str() throws Exception { // STR (SingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    @B(1) int field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103a_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    @B(1) X[] field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103b_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    X @B(2)[] field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103c_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    X []@B(3)[] field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103d_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    X []@B(3)[][] field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test103e_atr() throws Exception { // ATR (ArrayTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						    @B(1) int []@B(3)[][] field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @B(
				        #9 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, ARRAY]
				      )
				      #8 @B(
				        #9 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test104a_pstr() throws Exception { // PSTR (ParameterizedSingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X<T1,T2,T3> {
						    @B(1) X<@B(2) String, @B(3) Integer, @B(4) Boolean> field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				      )
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [TYPE_ARGUMENT(0)]
				      )
				      #10 @B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [TYPE_ARGUMENT(1)]
				      )
				      #10 @B(
				        #11 value=(int) 4 (constant type)
				        target type = 0x13 FIELD
				        location = [TYPE_ARGUMENT(2)]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test104b_pstr() throws Exception { // PSTR (ParameterizedSingleTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X<T1> {
						    @B(1) X<@B(2) String> @B(3)[] field;
						}
						""",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				      #10 @B(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				      )
				      #10 @B(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, TYPE_ARGUMENT(0)]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test105a_aqtr() throws Exception { // AQTR (ArrayQualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"Y.java",
					"class Y {}",

					"X.java",
					"""
						package one.two.three;
						class X<T1> {
						    one.two.three.@B(1) List[] field;
						}
						""",

					"List.java",
					"package one.two.three;\n" +
					"class List {}\n",

					"B.java",
					"""
						package one.two.three;
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @one.two.three.B(
				        #9 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator +"X.class",
					"one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test105b_aqtr() throws Exception { // AQTR (ArrayQualifiedTypeReference)
		this.runConformTest(
				new String[] {
					"Y.java",
					"class Y {}",

					"X.java",
					"""
						package one.two.three;
						class X<T1> {
						    one.two.three.@B(2) List @B(3)[]@B(4)[] field;
						}
						""",

					"List.java",
					"package one.two.three;\n" +
					"class List {}\n",

					"B.java",
					"""
						package one.two.three;
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @one.two.three.B(
				        #9 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY]
				      )
				      #8 @one.two.three.B(
				        #9 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				      )
				      #8 @one.two.three.B(
				        #9 value=(int) 4 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "one" + File.separator + "two" + File.separator + "three" + File.separator +"X.class",
					"one.two.three.X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test106a_wtr() throws Exception { // WTR (WildcardTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.util.List;
						class X<T1> {
							 List<? extends @B(1) Number> field;
						}
						""",

					"List.java",
					"class List {}\n",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [TYPE_ARGUMENT(0), WILDCARD]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test106b_wtr() throws Exception { // WTR (WildcardTypeReference)
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.util.List;
						class X<T1> {
							 List<? extends @B(1) Number[]> field;
						}
						""",

					"List.java",
					"class List {}\n",

					"B.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface B { int value() default -1; }
						""",
			},
			"");
			String expectedOutput =
				"""
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @B(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [TYPE_ARGUMENT(0), WILDCARD, ARRAY]
				      )
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=409244, [1.8][compiler] Type annotations on redundant casts dropped.
	public void testAnnotatedRedundantCast() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
							 String s = (@NonNull String) "Hello";
						}
						""",

					"NonNull.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface NonNull {}
						""",
			},
			"");
			String expectedOutput =
							"""
				  // Method descriptor #8 ()V
				  // Stack: 2, Locals: 1
				  X();
				     0  aload_0 [this]
				     1  invokespecial java.lang.Object() [10]
				     4  aload_0 [this]
				     5  ldc <String "Hello"> [12]
				     7  checkcast java.lang.String [14]
				    10  putfield X.s : java.lang.String [16]
				    13  return
				      Line numbers:
				        [pc: 0, line: 1]
				        [pc: 4, line: 2]
				        [pc: 13, line: 1]
				      Local variable table:
				        [pc: 0, pc: 14] local: this index: 0 type: X
				    RuntimeVisibleTypeAnnotations:\s
				      #23 @NonNull(
				        target type = 0x47 CAST
				        offset = 7
				        type argument index = 0
				      )
				}""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=409244, [1.8][compiler] Type annotations on redundant casts dropped.
	public void testAnnotatedRedundantCast2() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
							 String s = (String) "Hello";
						}
						""",

					"NonNull.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface NonNull {}
						""",
			},
			"");
			String expectedOutput =
							"""
				  // Method descriptor #8 ()V
				  // Stack: 2, Locals: 1
				  X();
				     0  aload_0 [this]
				     1  invokespecial java.lang.Object() [10]
				     4  aload_0 [this]
				     5  ldc <String "Hello"> [12]
				     7  putfield X.s : java.lang.String [14]
				    10  return
				      Line numbers:
				        [pc: 0, line: 1]
				        [pc: 4, line: 2]
				        [pc: 10, line: 1]
				      Local variable table:
				        [pc: 0, pc: 11] local: this index: 0 type: X
				}""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055a_codeblocks_exceptionParameterNestedType() throws Exception {
 		this.runConformTest(
 			new String[] {
 				"X.java",
 				"""
					public class X {
						public static void main(String[] args) {
							try {
					         foo();
							} catch(@B(1) Outer.@B(2) MyException e) {
								e.printStackTrace();
							}
						}
					   static void foo() throws Outer.MyException {}
					}
					class Outer {
						class MyException extends Exception {
							private static final long serialVersionUID = 1L;
						}
					}""",

				"B.java",
 				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 0;
					}
					""",
 		},
 		"");
 		String expectedOutput =
 			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #30 @B(
			        #31 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #30 @B(
			        #31 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			        location = [INNER_TYPE]
			      )
			 \s
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
 	}

	public void test055b_codeblocks_exceptionParameterMultiCatchNestedType() throws Exception {
 		this.runConformTest(
 			new String[] {
 				"X.java",
 				"""
					public class X {
						public static void main(String[] args) {
							try {
					         foo();
							} catch(@B(1) Outer.@B(2) MyException | @B(3) Outer2.@B(4) MyException2 e) {
								e.printStackTrace();
							}
						}
					   static void foo() throws Outer.MyException, Outer2.MyException2 {}
					}
					class Outer {
						class MyException extends Exception {
							private static final long serialVersionUID = 1L;
						}
					}
					class Outer2 {
						class MyException2 extends Exception {
							private static final long serialVersionUID = 1L;
						}
					}""",
 				"B.java",
 				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B {
						int value() default 0;
					}
					""",
 		},
 		"");
 		String expectedOutput =
 			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #34 @B(
			        #35 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #34 @B(
			        #35 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			        location = [INNER_TYPE]
			      )
			      #34 @B(
			        #35 value=(int) 3 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 1
			      )
			      #34 @B(
			        #35 value=(int) 4 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 1
			        location = [INNER_TYPE]
			      )
			""";
 		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
 	}

	public void test055c_codeblocks_exceptionParameterMultiCatch() throws Exception {
 		this.runConformTest(
 			new String[] {
 				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					class Exc1 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc2 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc3 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					public class X {
						public static void main(String[] args) {
							try {
								System.out.println(42);
							} catch(Exc1 | @B(1) Exc2 | @B(2) Exc3 t) {
								t.printStackTrace();
							}
						}
					}""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
 		},
		"42");
 		String expectedOutput =
 			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #45 @B(
			        #46 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 1
			      )
			      #45 @B(
			        #46 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 2
			      )
			""";
 		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
 	}

	public void test055d_codeblocks_exceptionParameterMultiCatch() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					class Exc1 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc2 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc3 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					public class X {
						public static void main(String[] args) {
							try {
								System.out.println(42);
							} catch(@A(1) @B(2) Exc1 | Exc2 | @A(3) @B(4) Exc3 t) {
								t.printStackTrace();
							}
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						int value() default 99;
					}
					""",

				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"42");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #45 @A(
			        #46 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #48 @B(
			        #46 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #45 @A(
			        #46 value=(int) 3 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 2
			      )
			      #48 @B(
			        #46 value=(int) 4 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 2
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055e_codeblocks_exceptionParameterMultiCatch() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					class Exc1 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc2 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc3 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					public class X {
						public static void main(String[] args) {
							try {
								System.out.println(42);
							} catch(@A(1) @B(2) Exc1 | Exc2 | @A(3) @B(4) Exc3 t) {
								t.printStackTrace();
							}
						}
					}""",

				"A.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface A {
						int value() default 99;
					}
					""",

				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"42");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #45 @A(
			        #46 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #48 @B(
			        #46 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #45 @A(
			        #46 value=(int) 3 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 2
			      )
			      #48 @B(
			        #46 value=(int) 4 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 2
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test055f_codeblocks_exceptionParameterComplex() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					class Exc1 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc2 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					class Exc3 extends RuntimeException {\
					    private static final long serialVersionUID = 1L;
					}
					public class X {
						public static void main(String[] args) {
							try {
								System.out.println(42);
							} catch(@B(1) Exc1 | Exc2 | @B(2) Exc3 t) {
								t.printStackTrace();
							}
							try {
								System.out.println(43);
							} catch(@B(1) Exc1 t) {
								t.printStackTrace();
							}
							try {
								System.out.println(44);
							} catch(@B(1) Exc1 | @B(2) Exc2 t) {
								t.printStackTrace();
							}
						}
					}""",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface B {
						int value() default 99;
					}
					""",
		},
		"42\n43\n44");
		String expectedOutput =
			"""
			    RuntimeVisibleTypeAnnotations:\s
			      #47 @B(
			        #48 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #47 @B(
			        #48 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 2
			      )
			      #47 @B(
			        #48 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 3
			      )
			      #47 @B(
			        #48 value=(int) 1 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 4
			      )
			      #47 @B(
			        #48 value=(int) 2 (constant type)
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 5
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug415911() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					
					@Target(ElementType.TYPE_USE)
					@interface Marker {
					}
					
					public class X {
					    @Marker
					    foo(String s) {
					
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					foo(String s) {
					^^^^^^^^^^^^^
				Return type for the method is missing
				----------
				""");
	}

	public void testBug426616() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					@Retention(RUNTIME)
					@Target(TYPE_USE)
					@interface SizeHolder { Size[] value();}
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@Repeatable(SizeHolder.class)
					@interface Size { int max(); }
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface Nonnull {}
					
					public class X {
					   public static void main(String[]argv) {}
						public static String testArrays() {
							List<@Size(max = 41) CharSequence>[] @Size(max = 42) [] @Nonnull @Size(max = 43) [][] test = new @Size(max = 44) ArrayList @Size(max = 45) [10][][] @Size(max = 47) @Size(max = 48) [];
							return (@Size(max = 49) String) test[0][1][2][3].get(0);
						}
					}""",
		},
		"");
		// Javac output
		// 0: Size(45): NEW, offset=0
        // 1: SizeHolder([@Size(max=47),@Size(max=48)]): NEW, offset=0, location=[ARRAY, ARRAY, ARRAY]
        // 2: Size(44): NEW, offset=0, location=[ARRAY, ARRAY, ARRAY, ARRAY]
		// 3: Size(49): CAST, offset=6, type_index=0
        // 4: Size(42): LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY]
        // 5: NonNull: LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY, ARRAY]
        // 6: Size(43): LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY, ARRAY]
        // 7: Size(41): LOCAL_VARIABLE, {start_pc=6, length=19, index=0}, location=[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]

		String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +

				// X Maps to javac entry (2): location OK, target type OK, offset different, our offset is 2 and not 0
				"      #33 @Size(\n" +
				"        #34 max=(int) 44 (constant type)\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 2\n" +
				"        location = [ARRAY, ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +

				// X Maps to javac entry (0), location OK, target type OK, offset different, our offset is 2 and not 0
				"      #33 @Size(\n" +
				"        #34 max=(int) 45 (constant type)\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 2\n" +
				"      )\n" +

				// X Maps to javac entry (1), location OK, target type OK, offset different, our offset is 2 and not 0
				"      #37 @SizeHolder(\n" +
				"        #38 value=[\n" +
				"          annotation value =\n" +
				"              #33 @Size(\n" +
				"                #34 max=(int) 47 (constant type)\n" +
				"              )\n" +
				"          annotation value =\n" +
				"              #33 @Size(\n" +
				"                #34 max=(int) 48 (constant type)\n" +
				"              )\n" +
				"          ]\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 2\n" +
				"        location = [ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +

				// X Maps to javac entry (3), location OK, target type OK, offset different, our offset is 24 (not 6), type index OK
				"      #33 @Size(\n" +
				"        #34 max=(int) 49 (constant type)\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 24\n" +
				"        type argument index = 0\n" +
				"      )\n" +

				// Maps to javac entry (4), location OK, target type OK, lvar diff, slight position difference (we seem to have an extra CHECKCAST)
				"      #33 @Size(\n" +
				"        #34 max=(int) 42 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +

				// Maps to javac entry (5), location OK, taret type OK, lvar diff, slight position difference (we seem to have an extra CHECKCAST)
				"      #43 @Nonnull(\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +

				// Maps to javac entry (6), location OK, target type OK,  slight position difference (we seem to have an extra CHECKCAST)
				"      #33 @Size(\n" +
				"        #34 max=(int) 43 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +

				// Maps to javac entry (7), location OK, target type OK, slight position difference (we seem to have an extra CHECKCAST)
				"      #33 @Size(\n" +
				"        #34 max=(int) 41 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 28] index: 0\n" +
				"        location = [ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug426616a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					@Retention(RUNTIME)
					@Target(TYPE_USE)
					@interface SizeHolder { Size[] value();}
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@Repeatable(SizeHolder.class)
					@interface Size { int max(); }
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface Nonnull {}
					
					public class X {
					   List<@Size(max = 41) CharSequence>[] @Size(max = 42) [] @Nonnull @Size(max = 43) [][] test = new @Size(max = 44) ArrayList @Size(max = 45) [10][][] @Size(max = 47) @Size(max = 48) [];
					   public static void main(String[]argv) {}
					}""",
		},
		"");

		String expectedOutput =
				"""
			  // Field descriptor #6 [[[[Ljava/util/List;
			  // Signature: [[[[Ljava/util/List<Ljava/lang/CharSequence;>;
			  java.util.List[][][][] test;
			    RuntimeVisibleTypeAnnotations:\s
			      #10 @Size(
			        #11 max=(int) 42 (constant type)
			        target type = 0x13 FIELD
			        location = [ARRAY]
			      )
			      #13 @Nonnull(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY]
			      )
			      #10 @Size(
			        #11 max=(int) 43 (constant type)
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY]
			      )
			      #10 @Size(
			        #11 max=(int) 41 (constant type)
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]
			      )
			 \s
			  // Method descriptor #17 ()V
			  // Stack: 2, Locals: 1
			  public X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [19]
			     4  aload_0 [this]
			     5  bipush 10
			     7  anewarray java.util.ArrayList[][][] [21]
			    10  putfield X.test : java.util.List[][][][] [23]
			    13  return
			      Line numbers:
			        [pc: 0, line: 16]
			        [pc: 4, line: 17]
			        [pc: 13, line: 16]
			      Local variable table:
			        [pc: 0, pc: 14] local: this index: 0 type: X
			    RuntimeVisibleTypeAnnotations:\s
			      #10 @Size(
			        #11 max=(int) 44 (constant type)
			        target type = 0x44 NEW
			        offset = 7
			        location = [ARRAY, ARRAY, ARRAY, ARRAY]
			      )
			      #10 @Size(
			        #11 max=(int) 45 (constant type)
			        target type = 0x44 NEW
			        offset = 7
			      )
			      #31 @SizeHolder(
			        #32 value=[
			          annotation value =
			              #10 @Size(
			                #11 max=(int) 47 (constant type)
			              )
			          annotation value =
			              #10 @Size(
			                #11 max=(int) 48 (constant type)
			              )
			          ]
			        target type = 0x44 NEW
			        offset = 7
			        location = [ARRAY, ARRAY, ARRAY]
			      )
			 \s
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void testTypeVariable() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<@Missing T> {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<@Missing T> {
					                ^^^^^^^
				Missing cannot be resolved to a type
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417660, [1.8][compiler] Incorrect parsing of Annotations with array dimensions in arguments
	public void test417660() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.Documented;
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Retention;
						import java.lang.annotation.RetentionPolicy;
						import java.lang.annotation.Target;
						public class X {
						  int bar(int [] @TakeType(int[].class)[] x) {\s
							  return x[0][0];\s
						  }\s
						  public static void main(String[] args) {
							System.out.println(new X().bar(new int [][] { { 1234 }}));
						  }
						}
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@Documented
						@interface TakeType {
							Class value() default int[].class;
						}
						"""
				},
				"1234");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417660, [1.8][compiler] Incorrect parsing of Annotations with array dimensions in arguments
	public void test417660b() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.Documented;
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Retention;
						import java.lang.annotation.RetentionPolicy;
						import java.lang.annotation.Target;
						public class X {
						  int bar(int [][] @TakeType(int[].class)[][] x @TakeType(int[].class)[]) {\s
							  return x[0][0][0][0][0];\s
						  }\s
						  public static void main(String[] args) {
							System.out.println(new X().bar(new int [][][][][] { { { { { 1234 } } } } }));
						  }
						}
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@Documented
						@interface TakeType {
							Class value() default int[].class;
						}
						"""
				},
				"1234");
	}

	public void testAnnotatedExtendedDimensions() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							@NonNull String @Nullable [] f @NonNull [] = null;
							static @NonNull String @Nullable [] foo(@NonNull String @Nullable [] p @NonNull []) @NonNull [] {
								p = null;
								@NonNull String @Nullable [] l @NonNull [] = null;
						       return p;
							}
						}
						""",

					"NonNull.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface NonNull {}
						""",

					"Nullable.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface Nullable {}
						""",
			},
			"");
			String expectedOutput =
					"""
				  // Field descriptor #6 [[Ljava/lang/String;
				  java.lang.String[][] f;
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @NonNull(
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY]
				      )
				      #9 @Nullable(
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				      #8 @NonNull(
				        target type = 0x13 FIELD
				      )
				 \s
				  // Method descriptor #11 ()V
				  // Stack: 2, Locals: 1
				  public X();
				     0  aload_0 [this]
				     1  invokespecial java.lang.Object() [13]
				     4  aload_0 [this]
				     5  aconst_null
				     6  putfield X.f : java.lang.String[][] [15]
				     9  return
				      Line numbers:
				        [pc: 0, line: 1]
				        [pc: 4, line: 2]
				        [pc: 9, line: 1]
				      Local variable table:
				        [pc: 0, pc: 10] local: this index: 0 type: X
				 \s
				  // Method descriptor #22 ([[Ljava/lang/String;)[[Ljava/lang/String;
				  // Stack: 1, Locals: 2
				  static java.lang.String[][] foo(java.lang.String[][] p);
				    0  aconst_null
				    1  astore_0 [p]
				    2  aconst_null
				    3  astore_1 [l]
				    4  aload_0 [p]
				    5  areturn
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 2, line: 5]
				        [pc: 4, line: 6]
				      Local variable table:
				        [pc: 0, pc: 6] local: p index: 0 type: java.lang.String[][]
				        [pc: 4, pc: 6] local: l index: 1 type: java.lang.String[][]
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @NonNull(
				        target type = 0x40 LOCAL_VARIABLE
				        local variable entries:
				          [pc: 4, pc: 6] index: 1
				        location = [ARRAY, ARRAY]
				      )
				      #9 @Nullable(
				        target type = 0x40 LOCAL_VARIABLE
				        local variable entries:
				          [pc: 4, pc: 6] index: 1
				        location = [ARRAY]
				      )
				      #8 @NonNull(
				        target type = 0x40 LOCAL_VARIABLE
				        local variable entries:
				          [pc: 4, pc: 6] index: 1
				      )
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @NonNull(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				        location = [ARRAY, ARRAY]
				      )
				      #9 @Nullable(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				        location = [ARRAY]
				      )
				      #8 @NonNull(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				      )
				      #8 @NonNull(
				        target type = 0x14 METHOD_RETURN
				        location = [ARRAY, ARRAY]
				      )
				      #9 @Nullable(
				        target type = 0x14 METHOD_RETURN
				        location = [ARRAY]
				      )
				      #8 @NonNull(
				        target type = 0x14 METHOD_RETURN
				      )
				}""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void testPQTRArray() throws Exception {
		this.runConformTest(
				new String[] {
						"Outer.java",
						"""
							public class Outer<K>  {
								class Inner<P> {
								}
								public @T(1) Outer<@T(2) String>.@T(3) Inner<@T(4) Integer> @T(5) [] omi @T(6) [];
							}
							@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
							@interface T {
								int value();
							}
							""",
			},
			"");
			String expectedOutput =
					"""
				  public Outer$Inner[][] omi;
				    RuntimeInvisibleTypeAnnotations:\s
				      #10 @T(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY]
				      )
				      #10 @T(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE]
				      )
				      #10 @T(
				        #11 value=(int) 5 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				      #10 @T(
				        #11 value=(int) 6 (constant type)
				        target type = 0x13 FIELD
				      )
				      #10 @T(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, TYPE_ARGUMENT(0)]
				      )
				      #10 @T(
				        #11 value=(int) 4 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]
				      )
				 \s
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Outer.class", "Outer", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void testPQTRArray2() throws Exception {
		this.runConformTest(
				new String[] {
						"Outer.java",
						"""
							public class Outer<K1, K2>  {
								class Inner<P1, P2> {
								}
								public @T(1) Outer<@T(2) String, @T(3) Inner>.@T(4) Inner<@T(5) Integer, @T(6) Outer.@T(7) Inner> @T(7) [] omi @T(8) [];
							}
							@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
							@interface T {
								int value();
							}
							""",
			},
			"");
			String expectedOutput =
					"""
				  // Field descriptor #6 [[LOuter$Inner;
				  // Signature: [[LOuter<Ljava/lang/String;LOuter$Inner;>.Inner<Ljava/lang/Integer;LOuter$Inner;>;
				  public Outer$Inner[][] omi;
				    RuntimeInvisibleTypeAnnotations:\s
				      #10 @T(
				        #11 value=(int) 1 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY]
				      )
				      #10 @T(
				        #11 value=(int) 4 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE]
				      )
				      #10 @T(
				        #11 value=(int) 7 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY]
				      )
				      #10 @T(
				        #11 value=(int) 8 (constant type)
				        target type = 0x13 FIELD
				      )
				      #10 @T(
				        #11 value=(int) 2 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, TYPE_ARGUMENT(0)]
				      )
				      #10 @T(
				        #11 value=(int) 3 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, TYPE_ARGUMENT(1), INNER_TYPE]
				      )
				      #10 @T(
				        #11 value=(int) 5 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(0)]
				      )
				      #10 @T(
				        #11 value=(int) 6 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(1)]
				      )
				      #10 @T(
				        #11 value=(int) 7 (constant type)
				        target type = 0x13 FIELD
				        location = [ARRAY, ARRAY, INNER_TYPE, TYPE_ARGUMENT(1), INNER_TYPE]
				      )
				 \s
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Outer.class", "Outer", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void testConstructorResult() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							@Target(ElementType.TYPE_USE)
							@interface T {
							}
							public class X {
								@T X() {}
								class Y {
								 @T Y () {
								}
								}
							}
							""",
			},
			"");
			String expectedOutput =
					"""
				  // Method descriptor #6 ()V
				  // Stack: 1, Locals: 1
				  X();
				    0  aload_0 [this]
				    1  invokespecial java.lang.Object() [8]
				    4  return
				      Line numbers:
				        [pc: 0, line: 7]
				      Local variable table:
				        [pc: 0, pc: 5] local: this index: 0 type: X
				    RuntimeInvisibleTypeAnnotations:\s
				      #15 @T(
				        target type = 0x14 METHOD_RETURN
				      )
				
				""";
			String expectedOutForY =
					"""
				  // Method descriptor #8 (LX;)V
				  // Stack: 2, Locals: 2
				  X$Y(X arg0);
				     0  aload_0 [this]
				     1  aload_1 [arg0]
				     2  putfield X$Y.this$0 : X [10]
				     5  aload_0 [this]
				     6  invokespecial java.lang.Object() [12]
				     9  return
				      Line numbers:
				        [pc: 0, line: 9]
				        [pc: 9, line: 10]
				      Local variable table:
				        [pc: 0, pc: 10] local: this index: 0 type: X.Y
				    RuntimeInvisibleTypeAnnotations:\s
				      #20 @T(
				        target type = 0x14 METHOD_RETURN
				        location = [INNER_TYPE]
				      )
				
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutForY, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void test418347() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.*;
							import static java.lang.annotation.ElementType.*;
							@Target({TYPE_USE}) @interface P { }
							@Target({TYPE_USE}) @interface O { }
							@Target({TYPE_USE}) @interface I { }
							public abstract class X<T> {
								class Y<Q> {
								}
								void foo(@P Y<P> p) {}
							}
							""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeInvisibleTypeAnnotations:\s
				      #24 @P(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				        location = [INNER_TYPE]
				      )
				
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void test418347a() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.*;
							import static java.lang.annotation.ElementType.*;
							@Target({TYPE_USE}) @interface P { }
							@Target({TYPE_USE}) @interface O { }
							@Target({TYPE_USE}) @interface I { }
							public abstract class X {
								class Y {
									class Z {}
								}
								void foo(@P X.@O Y.@I Z[] p) {}
							}
							""",
			},
			"");
			String expectedOutput =
					"""
				    RuntimeInvisibleTypeAnnotations:\s
				      #19 @P(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				        location = [ARRAY]
				      )
				      #20 @O(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				        location = [ARRAY, INNER_TYPE]
				      )
				      #21 @I(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				        location = [ARRAY, INNER_TYPE, INNER_TYPE]
				      )
				
				""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418347,  [1.8][compiler] Type annotations dropped during code generation.
	public void test418347b() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						public abstract class X {
							java.util.List [][] l = new java.util.ArrayList @pkg.NonNull [0] @pkg.NonNull[];    \s
						}
						""",
					"pkg/NonNull.java",
					"""
						package pkg;
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Target;
						@Target(ElementType.TYPE_USE)
						public @interface NonNull {
						}
						"""
			},
			"");
			String expectedOutput =
					"""
				    RuntimeInvisibleTypeAnnotations:\s
				      #21 @pkg.NonNull(
				        target type = 0x44 NEW
				        offset = 6
				      )
				      #21 @pkg.NonNull(
				        target type = 0x44 NEW
				        offset = 6
				        location = [ARRAY]
				      )
				}""";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419331, [1.8][compiler] Weird error on forward reference to type annotations from type parameter declarations
	public void testForwardReference() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured,
			new String[] {
				"T.java",
				"""
					import java.lang.annotation.Annotation;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					
					@R(TC.class)
					@Target(ElementType.TYPE_PARAMETER)
					@interface T {
					}
					
					interface I<@T K> {
					}
					
					@Deprecated
					@interface TC {
					
					}
					
					@Target(ElementType.ANNOTATION_TYPE)
					@interface R {
					    Class<? extends Annotation> value();
					}
					""",
			},
			"");
	}
	public void testHybridTargets() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					@Target({ElementType.TYPE_USE, ElementType.PACKAGE})
					@interface T {
					}
					@T
					public class X {
					    @T
					    X() {}
					    @T String x;
					    @T\s
						int foo(@T int p) {\s
					      @T int l;
						   return 0;
					   }
					}
					""",
			},
			"");
		String expectedOutput =
				"""
			  // Field descriptor #6 Ljava/lang/String;
			  java.lang.String x;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @T(
			        target type = 0x13 FIELD
			      )
			 \s
			  // Method descriptor #10 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [12]
			    4  return
			      Line numbers:
			        [pc: 0, line: 9]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @T(
			        target type = 0x14 METHOD_RETURN
			      )
			 \s
			  // Method descriptor #19 (I)I
			  // Stack: 1, Locals: 2
			  int foo(int p);
			    0  iconst_0
			    1  ireturn
			      Line numbers:
			        [pc: 0, line: 14]
			      Local variable table:
			        [pc: 0, pc: 2] local: this index: 0 type: X
			        [pc: 0, pc: 2] local: p index: 1 type: int
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @T(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #8 @T(
			        target type = 0x14 METHOD_RETURN
			      )
			
			  RuntimeInvisibleAnnotations:\s
			    #8 @T(
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void testHybridTargets2() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					@Target({ ElementType.TYPE_USE, ElementType.METHOD })
					@interface SillyAnnotation {  }
					public class X {
					   @SillyAnnotation
					   X(@SillyAnnotation int x) {
					   }
						@SillyAnnotation
						void foo(@SillyAnnotation int x) {
						}
						@SillyAnnotation
						String goo(@SillyAnnotation int x) {
							return null;
						}
						@SillyAnnotation
						X field;
					}
					"""
			},
			"");
		String expectedOutput =
				"""
			  // Field descriptor #6 LX;
			  X field;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @SillyAnnotation(
			        target type = 0x13 FIELD
			      )
			 \s
			  // Method descriptor #10 (I)V
			  // Stack: 1, Locals: 2
			  X(int x);
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [12]
			    4  return
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 4, line: 8]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			        [pc: 0, pc: 5] local: x index: 1 type: int
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @SillyAnnotation(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #8 @SillyAnnotation(
			        target type = 0x14 METHOD_RETURN
			      )
			 \s
			  // Method descriptor #10 (I)V
			  // Stack: 0, Locals: 2
			  void foo(int x);
			    0  return
			      Line numbers:
			        [pc: 0, line: 11]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: x index: 1 type: int
			    RuntimeInvisibleAnnotations:\s
			      #8 @SillyAnnotation(
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @SillyAnnotation(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			 \s
			  // Method descriptor #23 (I)Ljava/lang/String;
			  // Stack: 1, Locals: 2
			  java.lang.String goo(int x);
			    0  aconst_null
			    1  areturn
			      Line numbers:
			        [pc: 0, line: 14]
			      Local variable table:
			        [pc: 0, pc: 2] local: this index: 0 type: X
			        [pc: 0, pc: 2] local: x index: 1 type: int
			    RuntimeInvisibleAnnotations:\s
			      #8 @SillyAnnotation(
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @SillyAnnotation(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #8 @SillyAnnotation(
			        target type = 0x14 METHOD_RETURN
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void testDeprecated() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					@Deprecated
					@Target(ElementType.TYPE_USE)
					@interface X {
						int value() default 0;
					}
					"""
			},
			"");
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, no super bit, deprecated)
			abstract @interface X extends java.lang.annotation.Annotation {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 class: #6 java/lang/annotation/Annotation
			    constant #6 utf8: "java/lang/annotation/Annotation"
			    constant #7 utf8: "value"
			    constant #8 utf8: "()I"
			    constant #9 utf8: "AnnotationDefault"
			    constant #10 integer: 0
			    constant #11 utf8: "SourceFile"
			    constant #12 utf8: "X.java"
			    constant #13 utf8: "Deprecated"
			    constant #14 utf8: "RuntimeVisibleAnnotations"
			    constant #15 utf8: "Ljava/lang/Deprecated;"
			    constant #16 utf8: "Ljava/lang/annotation/Target;"
			    constant #17 utf8: "Ljava/lang/annotation/ElementType;"
			    constant #18 utf8: "TYPE_USE"
			 \s
			  // Method descriptor #8 ()I
			  public abstract int value();
			    Annotation Default:\s
			      (int) 0 (constant type)
			
			  RuntimeVisibleAnnotations:\s
			    #15 @java.lang.Deprecated(
			    )
			    #16 @java.lang.annotation.Target(
			      #7 value=[
			        java.lang.annotation.ElementType.TYPE_USE(enum type #17.#18)
			        ]
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421148, [1.8][compiler] Verify error with annotated casts and unused locals.
	public void test421148() {

		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					@Target(ElementType.TYPE_USE) @interface T {}
					public class X {
						public static void main(String argv[]) {
							Object o = (@T Object) new Object();   \s
					       System.out.println("OK");
						}
					}
					"""
			},
			"OK",
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421620,  [1.8][compiler] wrong compile error with TYPE_USE annotation on exception
	public void test421620() {

		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Documented;
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.RetentionPolicy;
					class E1 extends Exception {
					    private static final long serialVersionUID = 1L;
					}
					
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@Documented
					@interface NonCritical { }
					public class X {
					    @NonCritical E1 e1; // looks like this field's type binding is reused
					//wrong error:
					//Cannot use the parameterized type E1 either in catch block or throws clause
					    void f1 (int a) throws /*@NonCritical*/ E1 {
					        throw new E1();
					    }
					    void foo() {
					        try {
					            f1(0);
					//wrong error: Unreachable catch block for E1.
					//             This exception is never thrown from the try statement body
					        } catch (@NonCritical final RuntimeException | @NonCritical E1 ex) {
					            System.out.println(ex);
					        }
					    }
					    public static void main(String[] args) {
							System.out.println("OK");
						}
					}
					"""
			},
			"OK",
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425599, [1.8][compiler] ISE when trying to compile qualified and annotated class instance creation
	public void _test425599() {

		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					public class X {
					    Object ax = new @A Outer().new Middle<String>();
					    public static void main(String args[]) {
					        System.out.println("OK");
					    }
					}
					@Target(ElementType.TYPE_USE) @interface A {}
					class Outer {
					    class Middle<E> {
					    	class Inner<I> {}
					    	@A Middle<Object>.@A Inner<Character> ax = new pack.@A Outer().new @A Middle<@A Object>().new @A Inner<@A Character>(null);
					    }
					}
					"""
			},
			"OK",
			customOptions);
	}
	public void testBug485386() {
		String javaVersion = System.getProperty("java.version");
		int index = javaVersion.indexOf('.');
		if (index != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			index = javaVersion.indexOf('-');
			if (index != -1)
				javaVersion = javaVersion.substring(0, index);
		}
		int v = Integer.parseInt(javaVersion);
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.lang.annotation.*;
					import java.lang.reflect.*;
					
					@Retention(value = RetentionPolicy.RUNTIME)
					@java.lang.annotation.Target(ElementType.TYPE_USE)
					@interface TestAnn1 {
					  String value() default "1";
					}
					
					public class Test {
					
					  class Inner {
					    public @TestAnn1() Inner() {
					      System.out.println("New");
					    }
					  }
					
					  public void test() throws SecurityException, NoSuchMethodException {
					    Executable f = Test.Inner.class.getDeclaredConstructor(Test.class);
					    AnnotatedType ae = f.getAnnotatedReturnType();
					    Object o = ae.getAnnotation(TestAnn1.class);
					    System.out.println(o);
					  }
					 \s
					  public static void main(String... args) throws Exception {
					    new Test().test();
					  }
					}
					"""
			},
			"@TestAnn1(" + (v < 14 ? "value=" : "") + decorateAnnotationValueLiteral("1") + ")");
	}
	public void testBug492322readFromClass() {
		runConformTest(
			new String[] {
				"test1/Base.java",
				"package test1;\n" +
				"\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE) @interface A2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A4 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A5 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A6 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A7 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A8 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C2 {}\n" +
				"\n" +
				"public abstract class Base {\n" +
				"  static public class Static {\n" +
				"   public class Middle1 {\n" +
				"     public class Middle2<M> {\n" +
				"       public class Middle3 {\n" +
				"        public class GenericInner<T> {\n" +
				"        }\n" +
				"       }\n" +
				"     }\n" +
				"   }\n" +
				"  }\n" +
				"\n" +
				"  public Object method1(Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String> nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method2(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method3(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"}\n" +
				"",
			}
		);

		// get compiled type via binarytypebinding
		Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		Compiler compiler = new Compiler(getNameEnvironment(new String[0], null), getErrorHandlingPolicy(),
				new CompilerOptions(customOptions), requestor, getProblemFactory());
		char [][] compoundName = new char [][] { "test1".toCharArray(), "Base".toCharArray()};
		ReferenceBinding type = compiler.lookupEnvironment.askForType(compoundName, compiler.lookupEnvironment.UnNamedModule);
		assertNotNull(type);
		MethodBinding[] methods1 = type.getMethods("method1".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String>",
				new String(methods1[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods2 = type.getMethods("method2".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 []",
				new String(methods2[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods3 = type.getMethods("method3".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 []",
				new String(methods3[0].parameters[0].annotatedDebugName()));
	}

	public void testBug492322readFromClassWithGenericBase() {
		runConformTest(
			new String[] {
				"test1/Base.java",
				"package test1;\n" +
				"\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE) @interface A2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A4 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A5 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A6 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A7 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface A8 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B2 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface B3 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C1 {}\n" +
				"@Target(ElementType.TYPE_USE) @interface C2 {}\n" +
				"\n" +
				"public abstract class Base<B> {\n" +
				"  static public class Static {\n" +
				"   public class Middle1 {\n" +
				"     public class Middle2<M> {\n" +
				"       public class Middle3 {\n" +
				"        public class GenericInner<T> {\n" +
				"        }\n" +
				"       }\n" +
				"     }\n" +
				"   }\n" +
				"  }\n" +
				"\n" +
				"  public Object method1(Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String> nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method2(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"  public Object method3(Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 [] nullable) {\n" +
				"    return new Object();\n" +
				"  }\n" +
				"}\n" +
				"",
			}
		);

		// get compiled type via binarytypebinding
		Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		Compiler compiler = new Compiler(getNameEnvironment(new String[0], null), getErrorHandlingPolicy(),
				new CompilerOptions(customOptions), requestor, getProblemFactory());
		char [][] compoundName = new char [][] { "test1".toCharArray(), "Base".toCharArray()};
		ReferenceBinding type = compiler.lookupEnvironment.askForType(compoundName, compiler.lookupEnvironment.UnNamedModule);
		assertNotNull(type);
		MethodBinding[] methods1 = type.getMethods("method1".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String>",
				new String(methods1[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods2 = type.getMethods("method2".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Object>.@A5 Middle3.@A6 GenericInner<@B2 String> @A7 [] @A8 []",
				new String(methods2[0].parameters[0].annotatedDebugName()));

		MethodBinding[] methods3 = type.getMethods("method3".toCharArray());
		assertEquals("Base.@A2 Static.@A3 Middle1.@A4 Middle2<@B1 Class<@C1 Object @C2 []> @B2 []>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 []",
				new String(methods3[0].parameters[0].annotatedDebugName()));
	}
	public void testBug492322WithOldBinary() {
			// bug492322-compiled-with-4.6.jar contains classes compiled with eclipse 4.6:
			/*-
				package test1;

				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				@Target(ElementType.TYPE_USE) @interface A2 {}
				@Target(ElementType.TYPE_USE) @interface A3 {}
				@Target(ElementType.TYPE_USE) @interface A4 {}
				@Target(ElementType.TYPE_USE) @interface A5 {}
				@Target(ElementType.TYPE_USE) @interface A6 {}
				@Target(ElementType.TYPE_USE) @interface A7 {}
				@Target(ElementType.TYPE_USE) @interface A8 {}
				@Target(ElementType.TYPE_USE) @interface B1 {}
				@Target(ElementType.TYPE_USE) @interface B2 {}
				@Target(ElementType.TYPE_USE) @interface B3 {}
				@Target(ElementType.TYPE_USE) @interface B4 {}
				@Target(ElementType.TYPE_USE) @interface C1 {}
				@Target(ElementType.TYPE_USE) @interface C2 {}

				public abstract class Base<B> {
				  static public class Static {
				    public static class Static2<X> {
				      public class Middle1 {
				        public class Middle2<M> {
				          public class Middle3 {
				            public class GenericInner<T> {
				            }
				          }
				        }
				      }
				    }
				  }

				  public Object method1(Static.@A2 Static2<Exception>.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String> nullable) {
					  return new Object();
				  }
				  public Object method2(Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Object>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 [] nullable) {
				    return new Object();
				  }
				  public Object method3(Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Class<@C1 Object @C2 []> @B3 []>.@A5 Middle3.@A6 GenericInner<@B4 String> @A7 [] @A8 [] nullable) {
				    return new Object();
				  }
				}
			 */
			// get compiled type via binarytypebinding
			Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
			Map customOptions = getCompilerOptions();
			customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			String[] defaultClassPaths = getDefaultClassPaths();
			String jarpath = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "bug492322-compiled-with-4.6.jar";
			String[] paths = new String[defaultClassPaths.length + 1];
			System.arraycopy(defaultClassPaths, 0, paths, 0, defaultClassPaths.length);
			paths[defaultClassPaths.length] = jarpath;
			Compiler compiler = new Compiler(getNameEnvironment(new String[0], paths), getErrorHandlingPolicy(),
					new CompilerOptions(customOptions), requestor, getProblemFactory());
			char [][] compoundName = new char [][] { "test1".toCharArray(), "Base".toCharArray()};
			ReferenceBinding type = compiler.lookupEnvironment.askForType(compoundName, compiler.lookupEnvironment.UnNamedModule);
			assertNotNull(type);
			MethodBinding[] methods1 = type.getMethods("method1".toCharArray());
			assertEquals("Base.Static.@A2 Static2<Exception>.@A3 Middle1.@A4 Middle2<Object>.@A5 Middle3.@A6 GenericInner<String>",
					new String(methods1[0].parameters[0].annotatedDebugName()));

			MethodBinding[] methods2 = type.getMethods("method2".toCharArray());
			assertEquals("Base.Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Object>.@A5 Middle3.@A6 GenericInner<@B3 String> @A7 [] @A8 []",
					new String(methods2[0].parameters[0].annotatedDebugName()));

			MethodBinding[] methods3 = type.getMethods("method3".toCharArray());
			assertEquals("Base.Static.@A2 Static2<@B1 Exception>.@A3 Middle1.@A4 Middle2<@B2 Class<@C1 Object @C2 []> @B3 []>.@A5 Middle3.@A6 GenericInner<@B4 String> @A7 [] @A8 []",
					new String(methods3[0].parameters[0].annotatedDebugName()));
	}

	public void testBug594561_ParameterizedTypeAnnotations() {
		runConformTest(new String[] {
			"p/C.java",
			"""
				package p;\
				@Deprecated
				abstract class A<T> {}
				class C extends A<String> {}
				""",
		});

		Requestor requestor = new Requestor(false, null, false, false);
		Map<String, String> customOptions = getCompilerOptions(); customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		Compiler compiler = new Compiler(getNameEnvironment(new String[0], null), getErrorHandlingPolicy(), new CompilerOptions(customOptions), requestor, getProblemFactory());

		ReferenceBinding type = compiler.lookupEnvironment.askForType(new char[][] {"p".toCharArray(), "C".toCharArray()}, compiler.lookupEnvironment.UnNamedModule);
		assertNotNull(type);

		AnnotationBinding[] annos = type.superclass().getAnnotations();
		assertEquals(1, annos.length);
		assertEquals("java.lang.Deprecated", annos[0].getAnnotationType().debugName());
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1096
	// ECJ out of sync with JLS 9.6.4.1
	public void testGH1096() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.*;
						@interface MTPA {}
						@Retention(RetentionPolicy.RUNTIME)
						@interface CTPA {}
						public class X<@CTPA K, T> {
						    <U, @MTPA V> void m(U arg1) {}
						}
						""",
				},
				"");
		String expectedOutput =
				"""
			RuntimeInvisibleTypeAnnotations:\s
			      #24 @MTPA(
			        target type = 0x1 METHOD_TYPE_PARAMETER
			        type parameter index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);

		expectedOutput =
				"""
					  RuntimeVisibleTypeAnnotations:\s
					    #29 @CTPA(
					      target type = 0x0 CLASS_TYPE_PARAMETER
					      type parameter index = 0
					    )
					}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=568240
	// Method's annotation attribute is compiled as annotation on return type
	public void testBug568240() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;
					import java.lang.reflect.AnnotatedType;
					import java.util.Arrays;
					
					public class X {
					
					    public void test() {
					
					        AnnotatedType annotatedReturnType = Foo.class.getMethods()[0].getAnnotatedReturnType();
					
					        // @Child is an attribute of the @Ann annotation. Not a TYPE_USE annotation on the return type.
					        if (!Arrays.asList(annotatedReturnType.getAnnotations()).isEmpty()) {
					        	throw new Error("Broken");
					        }
					
					    }
					   \s
					    public static void main(String[] args) {
							new X().test();
						}
					
						public static interface Foo {
					
					        @Ann(value = @Ann.Child(value = "foo"))
					        String get();
					
					    }
					
					    @Target(ElementType.METHOD)
					    @Retention(RetentionPolicy.RUNTIME)
					    public static @interface Ann {
					
					        Child value();
					
					        @Retention(RetentionPolicy.RUNTIME)
					        public static @interface Child {
					            String value();
					        }
					    }
					}
					""",
			},
			"");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=566803
	// field.getAnnotatedType().getAnnotations() broken in the latest ECJ version
	public void testBug566803() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.lang.reflect.AnnotatedType;
					import java.lang.reflect.Field;
					
					public class X  {
					
					  @TestAnn1("1-1")
					  @TestAnn1("1-2")
					  public static final @TestAnnFirst long aaa = 1;
					
					  public void broken() {
						  throw new Error("Broken");
					  }
					 \s
					  public static void main(String[] args) throws NoSuchFieldException, SecurityException {
						new X().test();
					}
					  public void test() throws NoSuchFieldException, SecurityException {
					    Field f = X.class.getDeclaredField("aaa");
					    AnnotatedType s = f.getAnnotatedType();
					
					    if (long.class != s.getType()) {
					    	broken();
					    }
					
					    Annotation[] as = s.getAnnotations();
					    for (int i = 0; i < as.length; i++) {
					      System.out.println(i + " @" + as[i].annotationType().getCanonicalName() + "()");
					    }
					
					    if (1 != as.length) {
					    	broken();
					    }
					    as = s.getAnnotationsByType(TestAnnFirst.class);
					    if (1 != as.length) {
					    	broken();
					    }
					  }
					
					  @Retention(RetentionPolicy.RUNTIME)
					  @java.lang.annotation.Target(ElementType.TYPE_USE)
					  public @interface TestAnnFirst {
					  }
					
					  @Retention(value = RetentionPolicy.RUNTIME)
					  @Inherited
					  @Repeatable(TestAnn1s.class)
					  public @interface TestAnn1 {
					    String value() default "1";
					  }
					
					  @Retention(value = RetentionPolicy.RUNTIME)
					  @Inherited
					  public @interface TestAnn1s {
					    TestAnn1[] value();
					  }
					}
					""",
			},
			"0 @X.TestAnnFirst()");
		}
}
