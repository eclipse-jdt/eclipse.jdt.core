package org.eclipse.jdt.core.tests.eval;

import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.eval.EvaluationContext;
import org.eclipse.jdt.internal.eval.GlobalVariable;
import org.eclipse.jdt.internal.eval.InstallException;

public class SimpleVariableTest extends SimpleTest {
void evaluateVariable() throws TargetException, InstallException {
	startEvaluationContext();
	GlobalVariable var = getVariable(context);
	INameEnvironment env = getEnv();
	this.context.evaluateVariables(env, null, this.requestor, getProblemFactory());
	this.context.deleteVariable(var);
	stopEvaluationContext();
}
public GlobalVariable getVariable(EvaluationContext context) {
	return context.newVariable(
		"int".toCharArray(),
		"var".toCharArray(),
		"1".toCharArray());
}
public static void main(String[] args) throws TargetException, InstallException {
	SimpleVariableTest test = new SimpleVariableTest();
	test.evaluateVariable();
}
}
