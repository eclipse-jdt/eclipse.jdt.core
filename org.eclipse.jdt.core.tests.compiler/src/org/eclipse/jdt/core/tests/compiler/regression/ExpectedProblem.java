package org.eclipse.jdt.core.tests.compiler.regression;

public class ExpectedProblem {
	String fileName;
	int id;
	String[] arguments;
public ExpectedProblem(String fileName, int id, String[] arguments) {
	this.fileName = fileName;
	this.id = id;
	this.arguments = arguments;
}
}
