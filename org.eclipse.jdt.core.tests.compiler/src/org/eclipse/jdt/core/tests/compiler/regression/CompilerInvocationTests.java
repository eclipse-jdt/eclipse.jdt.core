/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import junit.framework.Test;

/**
 * This class is meant to gather test cases related to the invocation of the
 * compiler, be it at an API or non API level.
 */
public class CompilerInvocationTests extends AbstractRegressionTest {

public CompilerInvocationTests(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test001" };
//    	TESTS_NUMBERS = new int[] { 1 };   
//    	TESTS_RANGE = new int[] { 1, -1 }; 
//  	TESTS_RANGE = new int[] { 1, 2049 }; 
//  	TESTS_RANGE = new int[] { 449, 451 }; 
//    	TESTS_RANGE = new int[] { 900, 999 }; 
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}
  
public static Class testClass() {
    return CompilerInvocationTests.class;
}

// irritant vs warning token - check To/From symmetry
public void test001_irritant_warning_token() {
	String [] tokens = new String[64];
	Map matcher = new HashMap();
	long irritant;
	String token;
	for (int i = 0; i < 64; i++) {
		if ((token = tokens[i] = CompilerOptions.warningTokenFromIrritant(irritant = 1L << i)) != null) {
			matcher.put(token, token);
			assertTrue((irritant & CompilerOptions.warningTokenToIrritants(token)) != 0);
		}
	}
	String [] allTokens = CompilerOptions.warningTokens;
	int length = allTokens.length;
	matcher.put("all", "all"); // all gets undetected in the From/To loop
	assertEquals(allTokens.length, matcher.size());
	for (int i = 0; i < length; i++) {
		assertNotNull(matcher.get(allTokens[i]));
	}
}

// problem categories - check that none is left unspecified
// see also discussion in https://bugs.eclipse.org/bugs/show_bug.cgi?id=208383
public void test002_problem_categories() {
	try {
		Class iProblemClass;
		Field[] fields = (iProblemClass = IProblem.class).getFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (field.getType() == Integer.TYPE) {
				int problemId = field.getInt(iProblemClass), maskedProblemId = problemId & IProblem.IgnoreCategoriesMask;
				if (maskedProblemId != 0 && maskedProblemId != IProblem.IgnoreCategoriesMask
						&& ProblemReporter.getProblemCategory(ProblemSeverities.Error, problemId)
							== CategorizedProblem.CAT_UNSPECIFIED) {
					 fail("unspecified category for problem " + field.getName());
				}
			}
		}
	}
	catch (IllegalAccessException e) {
		fail("could not access members");
	}
}
class TasksReader implements ICompilerRequestor {
	CompilationResult result;
	public void acceptResult(CompilationResult compilationResult) {
		this.result = compilationResult;
	}
}
static String taskTagsAsCutAndPaste(CategorizedProblem tasks[]) {
	StringBuffer result = new StringBuffer();
	String arguments[];
	for (int i = 0; i < tasks.length - 1; i++) {
		arguments = tasks[i].getArguments();
		System.out.print("\t\t\"[");
		System.out.print(arguments[0]);
		System.out.print(',');
		System.out.print(arguments[1]);
		System.out.print(',');
		System.out.print(arguments[2]);
		System.out.println("]\\n\" +");
	}
	arguments = tasks[tasks.length - 1].getArguments();
	System.out.print("\t\t\"[");
	System.out.print(arguments[0]);
	System.out.print(',');
	System.out.print(arguments[1]);
	System.out.print(',');
	System.out.print(arguments[2]);
	System.out.println("]\\n\"");
	return result.toString();
}
static String taskTagsAsStrings(CategorizedProblem tasks[]) {
	StringBuffer result = new StringBuffer();
	String arguments[];
	for (int i = 0; i < tasks.length; i++) {
		arguments = tasks[i].getArguments();
		result.append('[');
		result.append(arguments[0]);
		result.append(',');
		result.append(arguments[1]);
		result.append(',');
		result.append(arguments[2]);
		result.append(']');
		result.append("\n");
	}
	return result.toString();
}
public void runTaskTagsOptionsTest(
		String[] testFiles,
		Map customOptions,
		String expectedTags) {
	TasksReader reader = new TasksReader();
	Map options = JavaCore.getDefaultOptions();
	if (customOptions != null) {
		options.putAll(customOptions);
	}
	this.runConformTest(
		testFiles,
		"",
		null /* no extra class libraries */, 
		true /* flush output directory */,
		null, /* no VM args */
		options,
		reader, 
		true /* skip javac */);
	String tags = taskTagsAsStrings(reader.result.tasks);
	if (! tags.equals(expectedTags)) {
		System.out.println(getClass().getName() + '#' + getName());
		System.out.println("Effective results:");
		System.out.println(tags);
		System.out.println("Cut and paste:");
		taskTagsAsCutAndPaste(reader.result.tasks);
		assertEquals(expectedTags, tags);
	}
} 
// Basic test on task tags: watch default behavior
public void test003_task_tags_options() {
	this.runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(X x) {\n" + 
			"    // FIXME TODO XXX message contents\n" + 
			"  }\n" + 
			"}\n"},
		null,
		"[FIXME,message contents,HIGH]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
} 
// effect of cancelling priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test004_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "");
	this.runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(X x) {\n" + 
			"    // FIXME TODO XXX message contents\n" + 
			"  }\n" + 
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
} 
// effect of cancelling priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test005_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, ",,");
	this.runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(X x) {\n" + 
			"    // FIXME TODO XXX message contents\n" + 
			"  }\n" + 
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
	// would expect an exception of some sort
} 
// effect of changing priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test006_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "A,B,C,D,E");
	this.runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(X x) {\n" + 
			"    // FIXME TODO XXX message contents\n" + 
			"  }\n" + 
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
	// would expect an exception of some sort	
} 
// effect of changing priorities
public void test007_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,NORMAL,NORMAL");
	this.runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(X x) {\n" + 
			"    // FIXME TODO XXX message contents\n" + 
			"  }\n" + 
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
} 
// effect of changing priorities
// reactivate when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=143402 is fixed
public void _test008_task_tags_options() {
	Map customOptions = new HashMap();
	customOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,NORMAL"); // one less than the number of tags
	this.runTaskTagsOptionsTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(X x) {\n" + 
			"    // FIXME TODO XXX message contents\n" + 
			"  }\n" + 
			"}\n"},
		customOptions,
		"[FIXME,message contents,NORMAL]\n" +
		"[TODO,message contents,NORMAL]\n" +
		"[XXX,message contents,NORMAL]\n");
} 
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206423
// that bug showed that we had no coverage in the area of missing message
// templates, which can occur downstream in the localization process (assuming
// that we always release the English version right)
public void test009_missing_message_templates() {
	assertEquals("Unable to retrieve the error message for problem id: 16777215. Check compiler resources.", 
			new DefaultProblemFactory().getLocalizedMessage(Integer.MAX_VALUE, new String[]{}));
} 
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206423
public void test010_missing_elaboration_templates() {
	assertEquals("Unable to retrieve the error message elaboration for elaboration id: 1073741823. Check compiler resources.", 
			new DefaultProblemFactory().getLocalizedMessage(0, Integer.MAX_VALUE / 2, new String[]{"Zork"}));
} 
// problem categories - check that categories match expected ones
// see also discussion in https://bugs.eclipse.org/bugs/show_bug.cgi?id=208383
public void test011_problem_categories() {
	try {
		Class iProblemClass;
		class ProblemAttributes {
			boolean deprecated;
			int category;
			ProblemAttributes(int category) {
				this.category = category;
			}
			ProblemAttributes(boolean deprecated) {
				this.deprecated = deprecated;
			}
		}
		ProblemAttributes DEPRECATED = new ProblemAttributes(true);		
		Map expectedProblemAttributes = new HashMap();
		expectedProblemAttributes.put("ObjectHasNoSuperclass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UndefinedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("NotVisibleType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("AmbiguousType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UsingDeprecatedType", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("InternalTypeNameProvided", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnusedPrivateType", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("IncompatibleTypesInEqualityOperator", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncompatibleTypesInConditionalOperator", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IndirectAccessToStaticType", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MissingEnclosingInstanceForConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MissingEnclosingInstance", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectEnclosingInstanceReference", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalEnclosingInstanceSpecification", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotDefineStaticInitializerInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("OuterLocalMustBeFinal", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotDefineInterfaceInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalPrimitiveOrArrayTypeForEnclosingInstance", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("EnclosingInstanceInConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnonymousClassCannotExtendFinalClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotDefineAnnotationInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotDefineEnumInLocalType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonStaticContextForEnumMemberType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TypeHidingType", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("UndefinedName", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UninitializedLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("VariableTypeCannotBeVoid", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("VariableTypeCannotBeVoidArray", DEPRECATED);
		expectedProblemAttributes.put("CannotAllocateVoidArray", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("RedefinedLocal", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("RedefinedArgument", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DuplicateFinalLocalInitialization", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonBlankFinalLocalAssignment", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ParameterAssignment", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("FinalOuterLocalAssignment", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("LocalVariableIsNeverUsed", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("ArgumentIsNeverUsed", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("BytecodeExceeds64KLimit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("BytecodeExceeds64KLimitForClinit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyArgumentSlots", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyLocalVariableSlots", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManySyntheticArgumentSlots", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyArrayDimensions", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("BytecodeExceeds64KLimitForConstructor", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UndefinedField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UsingDeprecatedField", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("NonStaticFieldFromStaticInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ReferenceToForwardField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NonStaticAccessToStaticField", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UnusedPrivateField", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("IndirectAccessToStaticField", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UnqualifiedFieldAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("FinalFieldAssignment", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UninitializedBlankFinalField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateBlankFinalFieldInitialization", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("LocalVariableHidingLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("LocalVariableHidingField", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("FieldHidingLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("FieldHidingField", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("ArgumentHidingLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("ArgumentHidingField", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("MissingSerialVersion", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("UndefinedMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UsingDeprecatedMethod", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("DirectInvocationOfAbstractMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("VoidMethodReturnsValue", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodReturnsVoid", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodRequiresBody", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ShouldReturnValue", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodButWithConstructorName", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MissingReturnType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("BodyForNativeMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("BodyForAbstractMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NoMessageSendOnBaseType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ParameterMismatch", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NoMessageSendOnArrayType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NonStaticAccessToStaticMethod", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UnusedPrivateMethod", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("IndirectAccessToStaticMethod", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UndefinedConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UsingDeprecatedConstructor", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("UnusedPrivateConstructor", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("InstanceFieldDuringConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InstanceMethodDuringConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("RecursiveConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ThisSuperDuringConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InvalidExplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UndefinedConstructorInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleConstructorInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousConstructorInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UndefinedConstructorInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("NotVisibleConstructorInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AmbiguousConstructorInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnhandledExceptionInDefaultConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnhandledExceptionInImplicitConstructorCall", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ArrayReferenceRequired", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NoImplicitStringConversionForCharArrayExpression", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("StringConstantIsExceedingUtf8Limit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonConstantExpression", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NumericValueOutOfRange", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalCast", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidClassInstantiation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotDefineDimensionExpressionsWithInit", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("MustDefineEitherDimensionExpressionsOrInitializer", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidOperator", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CodeCannotBeReached", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotReturnInInitializer", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InitializerMustCompleteNormally", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidVoidExpression", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("MaskedCatch", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("DuplicateDefaultCase", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UnreachableCatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnhandledException", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectSwitchType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateCase", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateLabel", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidBreak", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidContinue", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UndefinedLabel", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidTypeToSynchronized", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidNullToSynchronized", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotThrowNull", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AssignmentHasNoEffect", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("PossibleAccidentalBooleanAssignment", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("SuperfluousSemicolon", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("UnnecessaryCast", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnnecessaryArgumentCast", DEPRECATED);
		expectedProblemAttributes.put("UnnecessaryInstanceof", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("FinallyMustCompleteNormally", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("UnusedMethodDeclaredThrownException", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedConstructorDeclaredThrownException", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("InvalidCatchBlockSequence", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("EmptyControlFlowStatement", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("UnnecessaryElse", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("NeedToEmulateFieldReadAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NeedToEmulateFieldWriteAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NeedToEmulateMethodAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("NeedToEmulateConstructorAccess", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("FallthroughCase", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("InheritedMethodHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InheritedFieldHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InheritedTypeHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalUsageOfQualifiedTypeReference", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnusedLabel", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("ThisInStaticContext", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("StaticMethodRequested", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalDimension", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidTypeExpression", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ParsingError", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorNoSuggestion", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUnaryExpression", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InterfaceCannotHaveConstructors", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ArrayConstantsOnlyInArrayInitializers", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorOnKeyword", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorOnKeywordNoSuggestion", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnmatchedBracket", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("NoFieldOnBaseType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InvalidExpressionAsStatement", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ExpressionShouldBeAVariable", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("MissingSemiColon", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidParenthesizedExpression", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertTokenBefore", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertTokenAfter", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorDeleteToken", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorDeleteTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorMergeTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInvalidToken", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorMisplacedConstruct", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorReplaceTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorNoSuggestionForTokens", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorUnexpectedEOF", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertToComplete", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertToCompleteScope", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("ParsingErrorInsertToCompletePhrase", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("EndOfSource", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidHexa", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidOctal", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidCharacterConstant", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidEscape", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidInput", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUnicodeEscape", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidFloat", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("NullSourceString", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnterminatedString", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnterminatedComment", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("NonExternalizedStringLiteral", new ProblemAttributes(CategorizedProblem.CAT_NLS));
		expectedProblemAttributes.put("InvalidDigit", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidLowSurrogate", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidHighSurrogate", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnnecessaryNLSTag", new ProblemAttributes(CategorizedProblem.CAT_NLS));
		expectedProblemAttributes.put("DiscouragedReference", new ProblemAttributes(CategorizedProblem.CAT_RESTRICTION));
		expectedProblemAttributes.put("InterfaceCannotHaveInitializers", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateModifierForType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForMemberClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForMemberInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForLocalClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ForbiddenReference", new ProblemAttributes(CategorizedProblem.CAT_RESTRICTION));
		expectedProblemAttributes.put("IllegalModifierCombinationFinalAbstractForClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalVisibilityModifierForInterfaceMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalStaticModifierForMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("SuperclassMustBeAClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ClassExtendFinalClass", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateSuperInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("SuperInterfaceMustBeAnInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("HierarchyCircularitySelfReference", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("HierarchyCircularity", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("HidingEnclosingType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateNestedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotThrowType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("PackageCollidesWithType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeCollidesWithPackage", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateTypes", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IsClassPathCorrect", new ProblemAttributes(CategorizedProblem.CAT_BUILDPATH));
		expectedProblemAttributes.put("PublicClassMustMatchFileName", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MustSpecifyPackage", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("HierarchyHasProblems", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("PackageIsNotExpectedPackage", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ObjectCannotHaveSuperTypes", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ObjectMustBeClass", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("RedundantSuperinterface", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("SuperclassNotFound", DEPRECATED);
		expectedProblemAttributes.put("SuperclassNotVisible", DEPRECATED);
		expectedProblemAttributes.put("SuperclassAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("SuperclassInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("SuperclassInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("InterfaceNotFound", DEPRECATED);
		expectedProblemAttributes.put("InterfaceNotVisible", DEPRECATED);
		expectedProblemAttributes.put("InterfaceAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("InterfaceInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("InterfaceInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("DuplicateField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateModifierForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForInterfaceField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierCombinationFinalVolatileForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnexpectedStaticModifierForField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("FieldTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("FieldTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("DuplicateMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForArgument", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateModifierForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForInterfaceMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalVisibilityModifierCombinationForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnexpectedStaticModifierForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalAbstractModifierCombinationForMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AbstractMethodInAbstractClass", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ArgumentTypeCannotBeVoid", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ArgumentTypeCannotBeVoidArray", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeCannotBeVoidArray", DEPRECATED);
		expectedProblemAttributes.put("NativeMethodsCannotBeStrictfp", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("DuplicateModifierForArgument", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("ArgumentTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ArgumentTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ExceptionTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeNotFound", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeNotVisible", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ReturnTypeInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("ConflictingImport", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("DuplicateImport", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("CannotImportPackage", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("UnusedImport", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("ImportNotFound", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("ImportNotVisible", DEPRECATED);
		expectedProblemAttributes.put("ImportAmbiguous", DEPRECATED);
		expectedProblemAttributes.put("ImportInternalNameProvided", DEPRECATED);
		expectedProblemAttributes.put("ImportInheritedNameHidesEnclosingName", DEPRECATED);
		expectedProblemAttributes.put("InvalidTypeForStaticImport", new ProblemAttributes(CategorizedProblem.CAT_IMPORT));
		expectedProblemAttributes.put("DuplicateModifierForVariable", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForVariable", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("LocalVariableCannotBeNull", DEPRECATED);
		expectedProblemAttributes.put("LocalVariableCanOnlyBeNull", DEPRECATED);
		expectedProblemAttributes.put("LocalVariableMayBeNull", DEPRECATED);
		expectedProblemAttributes.put("AbstractMethodMustBeImplemented", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("FinalMethodCannotBeOverridden", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IncompatibleExceptionInThrowsClause", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IncompatibleExceptionInInheritedMethodThrowsClause", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IncompatibleReturnType", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InheritedMethodReducesVisibility", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CannotOverrideAStaticMethodWithAnInstanceMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CannotHideAnInstanceMethodWithAStaticMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("StaticInheritedMethodConflicts", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MethodReducesVisibility", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("OverridingNonVisibleMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("AbstractMethodCannotBeOverridden", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("OverridingDeprecatedMethod", new ProblemAttributes(CategorizedProblem.CAT_DEPRECATION));
		expectedProblemAttributes.put("IncompatibleReturnTypeForNonInheritedInterfaceMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("IllegalVararg", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("OverridingMethodWithoutSuperInvocation", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("CodeSnippetMissingClass", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CodeSnippetMissingMethod", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotUseSuperInCodeSnippet", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyConstantsInConstantPool", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyBytesForStringConstant", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyFields", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("TooManyMethods", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UseAssertAsAnIdentifier", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UseEnumAsAnIdentifier", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("EnumConstantsCannotBeSurroundedByParenthesis", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("Task", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NullLocalVariableReference", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("PotentialNullLocalVariableReference", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("RedundantNullCheckOnNullLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NullLocalVariableComparisonYieldsFalse", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("RedundantLocalVariableNullAssignment", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NullLocalVariableInstanceofYieldsFalse", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("RedundantNullCheckOnNonNullLocalVariable", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("NonNullLocalVariableComparisonYieldsFalse", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("UndocumentedEmptyBlock", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("JavadocInvalidSeeUrlReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingTagDescription", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocHiddenReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidMemberTypeQualification", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingIdentifier", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNonStaticTypeFromStaticInvocation", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamTagTypeParameter", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUnexpectedTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingParamTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingParamName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateParamName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingReturnTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateReturnTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingThrowsTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingThrowsClassName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidThrowsClass", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocDuplicateThrowsClassName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidThrowsClassName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissingSeeReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeHref", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidSeeArgs", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMissing", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedField", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNoMessageSendOnBaseType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNoMessageSendOnArrayType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUndefinedType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNotVisibleType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUsingDeprecatedType", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInternalTypeNameProvided", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedMethodHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedFieldHidesEnclosingName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInheritedNameHidesEnclosingTypeName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocAmbiguousMethodReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUnterminatedInlineTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMalformedSeeReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocMessagePrefix", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("JavadocMissingHashCharacter", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocEmptyReturnTag", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidValueReference", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocUnexpectedText", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocInvalidParamTagName", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("DuplicateTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalTypeVariableSuperReference", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonStaticTypeFromStaticInvocation", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ObjectCannotBeGeneric", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonGenericType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectArityForParameterizedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateMethodErasure", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ReferenceToForwardTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("BoundMustBeAnInterface", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnsafeRawConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeRawMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeTypeConversion", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("InvalidTypeVariableExceptionType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidParameterizedExceptionType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalGenericArray", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnsafeRawFieldAssignment", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("FinalBoundForTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UndefinedTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("SuperInterfacesCollide", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("WildcardConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("WildcardMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("WildcardFieldAssignment", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("GenericMethodTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("GenericConstructorTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnsafeGenericCast", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("IllegalInstanceofParameterizedType", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("IllegalInstanceofTypeParameter", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("NonGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectArityForParameterizedMethod", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ParameterizedMethodArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("NonGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IncorrectArityForParameterizedConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("ParameterizedConstructorArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeArgumentsForRawGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeArgumentsForRawGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("SuperTypeUsingWildcard", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("GenericTypeCannotExtendThrowable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalClassLiteralForTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnsafeReturnTypeOverride", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("MethodNameClash", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("RawMemberTypeCannotBeParameterized", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MissingArgumentsForParameterizedMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("StaticMemberOfParameterizedType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("BoundHasConflictingArguments", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateParameterizedMethods", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalQualifiedParameterizedTypeAllocation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateBounds", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("BoundCannotBeArray", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnsafeRawGenericConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("UnsafeRawGenericMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("TypeParameterHidingType", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("RawTypeReference", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("NoAdditionalBoundAfterTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("UnsafeGenericArrayForVarargs", new ProblemAttributes(CategorizedProblem.CAT_UNCHECKED_RAW));
		expectedProblemAttributes.put("IllegalAccessFromTypeVariable", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("TypeHidingTypeParameterFromType", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("TypeHidingTypeParameterFromMethod", new ProblemAttributes(CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT));
		expectedProblemAttributes.put("InvalidUsageOfWildcard", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("UnusedTypeArgumentsForMethodInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IncompatibleTypesInForeach", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidTypeForCollection", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("InvalidUsageOfTypeParameters", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfStaticImports", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfForeachStatements", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfTypeArguments", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfEnumDeclarations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfVarargs", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfAnnotations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfAnnotationDeclarations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfTypeParametersForAnnotationDeclaration", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("InvalidUsageOfTypeParametersForEnumDeclaration", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("IllegalModifierForAnnotationMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalExtendedDimensions", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("InvalidFileNameForPackageAnnotations", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("IllegalModifierForAnnotationType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForAnnotationMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("InvalidAnnotationMemberType", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("AnnotationCircularitySelfReference", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("AnnotationCircularity", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateAnnotation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MissingValueForAnnotationMember", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("DuplicateAnnotationMember", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("UndefinedAnnotationMember", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AnnotationValueMustBeClassLiteral", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationValueMustBeConstant", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationFieldNeedConstantInitialization", DEPRECATED);
		expectedProblemAttributes.put("IllegalModifierForAnnotationField", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationCannotOverrideMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AnnotationMembersCannotHaveParameters", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationMembersCannotHaveTypeParameters", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveSuperclass", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveSuperinterfaces", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("DuplicateTargetInTargetAnnotation", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("DisallowedTargetForAnnotation", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("MethodMustOverride", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("AnnotationTypeDeclarationCannotHaveConstructor", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("AnnotationValueMustBeAnnotation", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationTypeUsedAsSuperInterface", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MissingOverrideAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("FieldMissingDeprecatedAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("MethodMissingDeprecatedAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("TypeMissingDeprecatedAnnotation", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UnhandledWarningToken", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("AnnotationValueMustBeArrayInitializer", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("AnnotationValueMustBeAnEnumConstant", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("MethodMustOverrideOrImplement", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("UnusedWarningToken", new ProblemAttributes(CategorizedProblem.CAT_UNNECESSARY_CODE));
		expectedProblemAttributes.put("UnusedTypeArgumentsForConstructorInvocation", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CorruptedSignature", new ProblemAttributes(CategorizedProblem.CAT_BUILDPATH));
		expectedProblemAttributes.put("InvalidEncoding", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("CannotReadSource", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("BoxingConversion", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("UnboxingConversion", new ProblemAttributes(CategorizedProblem.CAT_CODE_STYLE));
		expectedProblemAttributes.put("IllegalModifierForEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForEnumConstant", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForLocalEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("IllegalModifierForMemberEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotDeclareEnumSpecialMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalQualifiedEnumConstantLabel", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("CannotExtendEnum", new ProblemAttributes(CategorizedProblem.CAT_TYPE));
		expectedProblemAttributes.put("CannotInvokeSuperConstructorInEnum", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("EnumAbstractMethodMustBeImplemented", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("EnumSwitchCannotTargetField", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalModifierForEnumConstructor", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("MissingEnumConstantCase", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("EnumStaticFieldInInInitializerContext", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("EnumConstantMustImplementAbstractMethod", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("IllegalExtendedDimensionsForVarArgs", new ProblemAttributes(CategorizedProblem.CAT_SYNTAX));
		expectedProblemAttributes.put("MethodVarargsArgumentNeedCast", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("ConstructorVarargsArgumentNeedCast", new ProblemAttributes(CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM));
		expectedProblemAttributes.put("VarargsConflict", new ProblemAttributes(CategorizedProblem.CAT_MEMBER));
		expectedProblemAttributes.put("JavadocGenericMethodTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNonGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocIncorrectArityForParameterizedMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterizedMethodArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocTypeArgumentsForRawGenericMethod", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocGenericConstructorTypeArgumentMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocNonGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocIncorrectArityForParameterizedConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocParameterizedConstructorArgumentTypeMismatch", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("JavadocTypeArgumentsForRawGenericConstructor", new ProblemAttributes(CategorizedProblem.CAT_JAVADOC));
		expectedProblemAttributes.put("ExternalProblemNotFixable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		expectedProblemAttributes.put("ExternalProblemFixable", new ProblemAttributes(CategorizedProblem.CAT_INTERNAL));
		StringBuffer failures = new StringBuffer();
		Field[] fields = (iProblemClass = IProblem.class).getFields();
		boolean watchInternalCategory = false, printHeader = true;
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (field.getType() == Integer.TYPE) {
				int problemId = field.getInt(iProblemClass), maskedProblemId = problemId & IProblem.IgnoreCategoriesMask;
				if (maskedProblemId != 0 && maskedProblemId != IProblem.IgnoreCategoriesMask) {
					ProblemAttributes expectedAttributes = (ProblemAttributes) expectedProblemAttributes.get(field.getName());
					if (expectedAttributes == null) {
						failures.append("missing expected problem attributes for problem " + field.getName() + "\n");
						System.out.println("\t\texpectedProblemAttributes.put(\"" + field.getName() + "\", new ProblemAttributes(CategorizedProblem.CAT_UNSPECIFIED));");
					} else if (!expectedAttributes.deprecated) {
						int actualCategory = ProblemReporter.getProblemCategory(ProblemSeverities.Error, problemId);
						if (expectedAttributes.category != actualCategory) {
							failures.append("category mismatch for problem " + field.getName() + " (expected " + categoryName(expectedAttributes.category) + ", got " + categoryName(actualCategory) + ")\n");
							System.out.println("\t\texpectedProblemAttributes.put(\"" + field.getName() + "\", new ProblemAttributes(CategorizedProblem." + categoryName(actualCategory) + "));");
						}
						if (watchInternalCategory && actualCategory == CategorizedProblem.CAT_INTERNAL) {
							if (printHeader) {
								printHeader = false;
								System.err.println("CAT_INTERNAL for problems:");
							}
							System.err.println("\t" + field.getName());
						}
					}
				}
			}
		}
		assertEquals(failures.toString(), 0, failures.length());
	}
	catch (IllegalAccessException e) {
		fail("could not access members");
	}
}
private static Map categoryNames;
private String categoryName(int category) {
	if (categoryNames == null) {
		categoryNames = new HashMap();
		Field[] fields = CategorizedProblem.class.getFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (field.getType() == Integer.TYPE) {
				String name = field.getName();
				if (name.startsWith("CAT_")) {
					try {
						categoryNames.put(new Integer(field.getInt(CategorizedProblem.class)), name);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
				}
			}
		}
	}
	return (String) categoryNames.get(new Integer(category));
}
}
