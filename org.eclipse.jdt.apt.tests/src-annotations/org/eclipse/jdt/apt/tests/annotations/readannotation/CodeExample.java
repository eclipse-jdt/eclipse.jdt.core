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
			"package question;\n" +
	        "\n" +
	        "@Deprecated\n" +
	        "@RTVisibleAnno(anno=@SimpleAnnotation(\"test\"), clazzes={})\n" +
	        "@RTInvisibleAnno(\"question\")\n" +
	        "public interface AnnotationTest{\n" +
	        "\n" +
	        "    @RTVisibleAnno(name = \"Foundation\",\n" +
	        "                   boolValue   = false, \n"  +
	        "                   byteValue   = 0x10,  \n"  +
	        "                   charValue   = 'c', \n"    +
	        "                   doubleValue = 99.0, \n"   +
	        "                   floatValue  = (float)9.0, \n"    +
	        "                   intValue    = 999, \n"    +
	        "                   longValue = 3333,  \n"    +
	        "                   shortValue = 3,    \n"    +
	        "                   colors ={ Color.RED, Color.BLUE }, \n"    +
	        "                   anno = @SimpleAnnotation(\"core\"),\n"    +
	        "                   simpleAnnos = {@SimpleAnnotation(\"org\"),    \n" +
			"              	                   @SimpleAnnotation(\"eclipse\"),\n" +
			"        		                   @SimpleAnnotation(\"jdt\") },  \n" +
			"                   clazzes = {Object.class, String.class},\n " +
			"		            clazz = Object.class)\n " +
	        "    @RTInvisibleAnno(\"org.eclipse.jdt.core\")\n " +
		    "    @Deprecated \n " +
		    "    public int field0 = 0; \n" +
	        "\n" +
	        "    @Deprecated \n" +
	    	"    public int field1 = 1; \n" +
	    	"\n" +
	    	"    @RTVisibleAnno(anno=@SimpleAnnotation(\"field\"), clazzes={})\n " +
	    	"    @RTInvisibleAnno(\"2\") \n " +
	    	"    public int field2 = 2; \n" +
	    	"\n" +
	    	"    @RTInvisibleAnno(\"3\") \n" +
	    	"    public int field3 = 3; \n"  +
	    	"\n" +
	    	"    @SimpleAnnotation(\"4\") \n" +
	    	"    public int field4 = 4; \n" +
	    	"\n" +
	    	"    @RTVisibleAnno(anno=@SimpleAnnotation(\"method0\"), clazzes={}) \n" +
	    	"    @RTInvisibleAnno(\"0\") \n" +
	    	"    @Deprecated \n " +
	    	"    public int method0();  \n " +
	    	"\n" +
	    	"    @Deprecated \n " +
	    	"    public int method1(); \n " +
	    	"\n" +
	    	"    @RTVisibleAnno(anno=@SimpleAnnotation(\"method2\"), clazzes={}) \n " +
	    	"    @RTInvisibleAnno(\"2\") \n" +
	    	"    public int method2(); \n" +
	    	"\n" +
	    	"   @RTInvisibleAnno(\"3\") \n" +
	    	"   public int method3(); \n" +
	    	"\n" +
	    	"   @SimpleAnnotation(\"method4\") \n" +
	    	"   public int method4(); \n" +
	    	"\n" +
	    	"   public int method5(int p0, \n" +
	    	"   				   @Deprecated  \n" +
	    	"   				   int p1, \n" +
	    	"   				   @RTVisibleAnno(anno=@SimpleAnnotation(\"param2\"), clazzes={}) \n" +
	    	"   				   @RTInvisibleAnno(\"2\") \n" +
	    	"   				   int p2); \n" +
	    	"\n" +
	    	"   public int method6(int p0, int p1, int p2); \n" +
	    	"\n" +
	    	"   @RTVisibleAnno(name = \"I'm \\\"special\\\": \\t\\\\\\n\",\n" +
	    	"		          charValue = '\\'',\n" +
	    	"		          clazzes = {},\n" +
	    	"		          anno = @SimpleAnnotation(\"\"))\n" +
	    	"   public int method7();" +
	    	"\n }";

	public static final String COLOR_CLASS = "Color";
	public static final String COLOR_CODE =
		"package question;\n" +
		"\n" +
		"public enum Color { RED, WHITE, BLUE; } ";

	public static final String PACKAGE_INFO_CLASS = "package-info";
	public static final String PACKAGE_INFO_CODE =
		"@Deprecated package question; ";

	public static final String RTINVISIBLE_CLASS = "RTInvisibleAnno";
	public static final String RTINVISIBLE_ANNOTATION_CODE =
		"package question;  \n" +
		"\n" +
		"   public @interface RTInvisibleAnno{  \n" +
		"   	String value(); \n" +
		"   }";

	public static final String RTVISIBLE_CLASS = "RTVisibleAnno";
	public static final String RTVISIBLE_ANNOTATION_CODE =
		"package question; \n" +
		"\n" +
		"import java.lang.annotation.*; \n" +
		"\n" +
		"@Retention(value=RetentionPolicy.RUNTIME) \n" +
		"public @interface RTVisibleAnno  \n" +
		"{	\n" +
		"	String name() default \"eclipse\"; \n" +
		"	boolean boolValue() default true; \n" +
		"	byte byteValue() default 0x0001; \n" +
		"	char charValue() default 'e'; \n" +
		"	double doubleValue() default 0.0; \n" +
		"	float floatValue()  default 0; \n" +
		"	int intValue() default 17; \n" +
		"	long longValue() default 43; \n" +
		"	short shortValue() default 1; \n" +
		"	Color[] colors() default {Color.RED, Color.WHITE, Color.BLUE}; \n" +
		"	SimpleAnnotation anno(); \n" +
		"	SimpleAnnotation[] simpleAnnos() default { @SimpleAnnotation(\"org.eclipse.org\") }; \n" +
		"	Class<?>[] clazzes(); \n" +
		"	Class<?> clazz() default java.lang.String.class; \n" +
		"\n }";

	public static final String SIMPLE_ANNOTATION_CLASS = "SimpleAnnotation";
	public static final String SIMPLE_ANNOTATION_CODE =
		"package question; \n" +
		"\n" +
		"import java.lang.annotation.Retention; \n" +
		"import java.lang.annotation.RetentionPolicy; \n" +
		"\n" +
		"@Retention(value=RetentionPolicy.RUNTIME) \n" +
		"public @interface SimpleAnnotation { \n" +
		"\n" +
		"	String value(); \n" +
		"}";

	public static final String PACKAGE_TRIGGER = "trigger";
	public static final String TRIGGER_CLASS = "Trigger";
	public static final String TRIGGER_CODE =
		"package trigger; \n" +
		"\n" +
		"@MyMarkerAnnotation \n" +
		"public class Trigger {}";

	public static final String MYMARKERANNOTATION_CLASS = "MyMarkerAnnotation";
	public static final String MYMARKERANNOTATION_CODE =
		"package trigger; \n" +
		"\n" +
		"public @interface MyMarkerAnnotation {}";

	public static final String PACKAGE_NOTYPES = "notypes";
	public static final String PACKAGE_INFO_NOTYPES_CLASS = "package-info";
	public static final String PACKAGE_INFO_NOTYPES_CODE =
		"@question.SimpleAnnotation(\"foo\") package notypes;\n";

}
