package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.internal.compiler.util.*;

/**
 * An environment that wraps the client's name environment.
 * This wrapper always considers the wrapped environment then if the name is
 * not found, it search in the code snippet support. This includes the super class
 * org.eclipse.jdt.internal.eval.target.CodeSnippet as well as the global variable classes.
 */
public class CodeSnippetEnvironment implements INameEnvironment, EvaluationConstants {
	INameEnvironment env;
	EvaluationContext context;
/**
 * Creates a new wrapper for the given environment.
 */
public CodeSnippetEnvironment(INameEnvironment env, EvaluationContext context) {
	this.env = env;
	this.context = context;
}
/**
 * @see INameEnvironment
 */
public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
	NameEnvironmentAnswer result = this.env.findType(compoundTypeName);
	if (result != null) {
		return result;
	}
	if (CharOperation.equals(compoundTypeName, ROOT_COMPOUND_NAME)) {
		IBinaryType binary = this.context.getRootCodeSnippetBinary();
		if (binary == null) {
			return null;
		} else {
			return new NameEnvironmentAnswer(binary);
		}
	}
	VariablesInfo installedVars = this.context.installedVars;
	ClassFile[] classFiles = installedVars.classFiles;
	for (int i = 0; i < classFiles.length; i++) {
		ClassFile classFile = classFiles[i];
		if (CharOperation.equals(compoundTypeName, classFile.getCompoundName())) {
			ClassFileReader binary = null;
			try {
				binary = new ClassFileReader(classFile.getBytes(), null);
			} catch (ClassFormatException e) {
				e.printStackTrace();  // Should never happen since we compiled this type
				return null;
			}
			return new NameEnvironmentAnswer(binary);
		}
	}
	return null;
}
/**
 * @see INameEnvironment.
 */
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	NameEnvironmentAnswer result = this.env.findType(typeName, packageName);
	if (result != null) {
		return result;
	}
	return findType(CharOperation.arrayConcat(packageName, typeName));
}
/**
 * @see INameEnvironment.
 */
public boolean isPackage(char[][] parentPackageName, char[] packageName) {
	return this.env.isPackage(parentPackageName, packageName);
}
}
