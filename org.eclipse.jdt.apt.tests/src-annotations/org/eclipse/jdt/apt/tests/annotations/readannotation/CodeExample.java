/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.readannotation;


public class CodeExample {

	public static final String PACKAGE_QUESTION = "question";

	public static final String ANNOTATION_TEST_CLASS = "AnnotationTest";
	public static final String ANNOTATION_TEST_CODE =
			"""
		package question;
		
		@Deprecated
		@RTVisibleAnno(anno=@SimpleAnnotation("test"), clazzes={})
		@RTInvisibleAnno("question")
		public interface AnnotationTest{
		
		    @RTVisibleAnno(name = "Foundation",
		                   boolValue   = false,\s
		                   byteValue   = 0x10, \s
		                   charValue   = 'c',\s
		                   doubleValue = 99.0,\s
		                   floatValue  = (float)9.0,\s
		                   intValue    = 999,\s
		                   longValue = 3333, \s
		                   shortValue = 3,   \s
		                   colors ={ Color.RED, Color.BLUE },\s
		                   anno = @SimpleAnnotation("core"),
		                   simpleAnnos = {@SimpleAnnotation("org"),   \s
		              	                   @SimpleAnnotation("eclipse"),
		        		                   @SimpleAnnotation("jdt") }, \s
		                   clazzes = {Object.class, String.class},
		 \
				            clazz = Object.class)
		 \
		    @RTInvisibleAnno("org.eclipse.jdt.core")
		 \
		    @Deprecated\s
		 \
		    public int field0 = 0;\s
		
		    @Deprecated\s
		    public int field1 = 1;\s
		
		    @RTVisibleAnno(anno=@SimpleAnnotation("field"), clazzes={})
		 \
		    @RTInvisibleAnno("2")\s
		 \
		    public int field2 = 2;\s
		
		    @RTInvisibleAnno("3")\s
		    public int field3 = 3;\s
		
		    @SimpleAnnotation("4")\s
		    public int field4 = 4;\s
		
		    @RTVisibleAnno(anno=@SimpleAnnotation("method0"), clazzes={})\s
		    @RTInvisibleAnno("0")\s
		    @Deprecated\s
		 \
		    public int method0(); \s
		 \
		
		    @Deprecated\s
		 \
		    public int method1();\s
		 \
		
		    @RTVisibleAnno(anno=@SimpleAnnotation("method2"), clazzes={})\s
		 \
		    @RTInvisibleAnno("2")\s
		    public int method2();\s
		
		   @RTInvisibleAnno("3")\s
		   public int method3();\s
		
		   @SimpleAnnotation("method4")\s
		   public int method4();\s
		
		   public int method5(int p0,\s
		   				   @Deprecated \s
		   				   int p1,\s
		   				   @RTVisibleAnno(anno=@SimpleAnnotation("param2"), clazzes={})\s
		   				   @RTInvisibleAnno("2")\s
		   				   int p2);\s
		
		   public int method6(int p0, int p1, int p2);\s
		
		   @RTVisibleAnno(name = "I'm \\"special\\": \\t\\\\\\n",
				          charValue = '\\'',
				          clazzes = {},
				          anno = @SimpleAnnotation(""))
		   public int method7();\
		
		 }""";

	public static final String COLOR_CLASS = "Color";
	public static final String COLOR_CODE =
		"""
		package question;
		
		public enum Color { RED, WHITE, BLUE; } """;

	public static final String PACKAGE_INFO_CLASS = "package-info";
	public static final String PACKAGE_INFO_CODE =
		"@Deprecated package question; ";

	public static final String RTINVISIBLE_CLASS = "RTInvisibleAnno";
	public static final String RTINVISIBLE_ANNOTATION_CODE =
		"""
		package question; \s
		
		   public @interface RTInvisibleAnno{ \s
		   	String value();\s
		   }""";

	public static final String RTVISIBLE_CLASS = "RTVisibleAnno";
	public static final String RTVISIBLE_ANNOTATION_CODE =
		"""
		package question;\s
		
		import java.lang.annotation.*;\s
		
		@Retention(value=RetentionPolicy.RUNTIME)\s
		public @interface RTVisibleAnno \s
		{\t
			String name() default "eclipse";\s
			boolean boolValue() default true;\s
			byte byteValue() default 0x0001;\s
			char charValue() default 'e';\s
			double doubleValue() default 0.0;\s
			float floatValue()  default 0;\s
			int intValue() default 17;\s
			long longValue() default 43;\s
			short shortValue() default 1;\s
			Color[] colors() default {Color.RED, Color.WHITE, Color.BLUE};\s
			SimpleAnnotation anno();\s
			SimpleAnnotation[] simpleAnnos() default { @SimpleAnnotation("org.eclipse.org") };\s
			Class<?>[] clazzes();\s
			Class<?> clazz() default java.lang.String.class;\s
		
		 }""";

	public static final String SIMPLE_ANNOTATION_CLASS = "SimpleAnnotation";
	public static final String SIMPLE_ANNOTATION_CODE =
		"""
		package question;\s
		
		import java.lang.annotation.Retention;\s
		import java.lang.annotation.RetentionPolicy;\s
		
		@Retention(value=RetentionPolicy.RUNTIME)\s
		public @interface SimpleAnnotation {\s
		
			String value();\s
		}""";

	public static final String PACKAGE_TRIGGER = "trigger";
	public static final String TRIGGER_CLASS = "Trigger";
	public static final String TRIGGER_CODE =
		"""
		package trigger;\s
		
		@MyMarkerAnnotation\s
		public class Trigger {}""";

	public static final String MYMARKERANNOTATION_CLASS = "MyMarkerAnnotation";
	public static final String MYMARKERANNOTATION_CODE =
		"""
		package trigger;\s
		
		public @interface MyMarkerAnnotation {}""";

	public static final String PACKAGE_NOTYPES = "notypes";
	public static final String PACKAGE_INFO_NOTYPES_CLASS = "package-info";
	public static final String PACKAGE_INFO_NOTYPES_CODE =
		"@question.SimpleAnnotation(\"foo\") package notypes;\n";

}
