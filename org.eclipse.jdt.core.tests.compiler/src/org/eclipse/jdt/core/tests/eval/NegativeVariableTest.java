package org.eclipse.jdt.core.tests.eval;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.eval.GlobalVariable;
/**
 * Negative tests for variables. Only compilation problems should be reported in
 * these tests.
 */
public class NegativeVariableTest extends EvaluationTest implements ProblemSeverities, ProblemReasons {
/**
 * Creates a new NegativeVariableTest.
 */
public NegativeVariableTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public static Class testClass() {
	return NegativeVariableTest.class;
}
/**
 * Test a variable that has a problem in its initializer.
 */
public void testInitializerProblem() {
	// Problem in first variable
	GlobalVariable var = null;
	try {
		var = context.newVariable("int".toCharArray(), "i".toCharArray(), buildCharArray(new String[] {
			"(1 + 1) *",
			"(j + 2)"}));
		evaluateWithExpectedProblem(
			var, 
			newProblem(IProblem.UndefinedName, Error, 11, 11, 2)); // j cannot be resolved
	} finally {
		if (var != null) {
			context.deleteVariable(var);
		}
	}

	// Problem in second variable
	GlobalVariable var1 = null;
	GlobalVariable var2 = null;
	try {
		var1 = context.newVariable("Object".toCharArray(), "o".toCharArray(), "new Object()".toCharArray());
		var2 = context.newVariable("int".toCharArray(), "i".toCharArray(), buildCharArray(new String[] {
			"(1 + 1) *",
			"(1 ++ 2)"}));
		evaluateWithExpectedProblem(
			var2, 
			newProblem(IProblem.InvalidUnaryExpression, Error, 11, 11, 2)); // Invalid argument to operation ++/--
	} finally {
		if (var1 != null) {
			context.deleteVariable(var1);
		}
		if (var2 != null) {
			context.deleteVariable(var2);
		}
	}
	
}
/**
 * Test a variable that has a problem in its name.
 */
public void testInvalidName() {
	// Problem in first variable
	GlobalVariable var = null;
	try {
		var = context.newVariable("int".toCharArray(), "!@#$%^&*()_".toCharArray(), "1".toCharArray());
		evaluateWithExpectedProblem(
			var, 
			newProblem(IProblem.ParsingError, Error, 0, 0, 0)); // Syntax error on token "!", "Identifier" expected
	} finally {
		if (var != null) {
			context.deleteVariable(var);
		}
	}

	// Problem in second variable
	GlobalVariable var1 = null;
	GlobalVariable var2 = null;
	try {
		var1 = context.newVariable("String".toCharArray(), "foo".toCharArray(), "\"bar\"".toCharArray());
		var2 = context.newVariable("int".toCharArray(), "!@#$%^&*()_".toCharArray(), "1".toCharArray());
		evaluateWithExpectedProblem(
			var2, 
			newProblem(IProblem.ParsingError, Error, 0, 0, 0)); // Syntax error on token "!", "Identifier" expected
	} finally {
		if (var1 != null) {
			context.deleteVariable(var1);
		}
		if (var2 != null) {
			context.deleteVariable(var2);
		}
	}
}
/**
 * Test a variable that has a problem in its type declaration.
 */
public void testUnknownType() {
	// Problem in first variable
	GlobalVariable var = null;
	try {
		var = context.newVariable("foo.Bar".toCharArray(), "var".toCharArray(), null);
		evaluateWithExpectedProblem(
			var, 
			newProblem(IProblem.FieldTypeNotFound, Error, 0, 6, -1)); // The type foo is undefined for the field GlobalVariables_1.var
	} finally {
		if (var != null) {
			context.deleteVariable(var);
		}
	}

	// Problem in second variable
	GlobalVariable var1 = null;
	GlobalVariable var2 = null;
	try {
		var1 = context.newVariable("int".toCharArray(), "x".toCharArray(), null);
		var2 = context.newVariable("foo.Bar".toCharArray(), "var".toCharArray(), null);
		evaluateWithExpectedProblem(
			var2, 
			newProblem(IProblem.FieldTypeNotFound, Error, 0, 6, -1)); // The type foo is undefined for the field GlobalVariables_1.var
	} finally {
		if (var1 != null) {
			context.deleteVariable(var1);
		}
		if (var2 != null) {
			context.deleteVariable(var2);
		}
	}
}
}
