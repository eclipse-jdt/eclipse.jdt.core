package org.eclipse.jdt.internal.codeassist.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Locale;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.impl.*;

public abstract class Engine implements ITypeRequestor {
	public LookupEnvironment lookupEnvironment;
/**
 * Add an additional binary type
 */

public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
	lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
}
/**
 * Add an additional compilation unit.
 */

public void accept(ICompilationUnit sourceUnit) {
	CompilationResult result = new CompilationResult(sourceUnit, 1, 1);
	CompilationUnitDeclaration parsedUnit = this.getParser().dietParse(sourceUnit, result);

	lookupEnvironment.buildTypeBindings(parsedUnit);
	lookupEnvironment.completeTypeBindings(parsedUnit, true);
}
/**
 * Add an additional source type
 */

public void accept(ISourceType sourceType, PackageBinding packageBinding) {
	CompilationResult result = new CompilationResult(sourceType.getFileName(), 1, 1); // need to hold onto this
	CompilationUnitDeclaration unit =
		SourceTypeConverter.buildCompilationUnit(sourceType, true, true, lookupEnvironment.problemReporter, result);

	if (unit != null) {
		lookupEnvironment.buildTypeBindings(unit);
		lookupEnvironment.completeTypeBindings(unit, true);
	}
}
/**
 * Answer an array of descriptions for the configurable options.
 * The descriptions may be changed and passed back to a different
 * compiler.
 *
 *  @return ConfigurableOption[] - array of configurable options
 */
public static ConfigurableOption[] getDefaultOptions(Locale locale) {
	return Compiler.getDefaultOptions(locale);
}
public abstract AssistParser getParser();
protected void parseMethod(CompilationUnitDeclaration unit, int position) {
	for (int i = unit.types.length; --i >= 0;) {
		TypeDeclaration type = unit.types[i];
		if (type.declarationSourceStart < position && type.declarationSourceEnd >= position) {
			getParser().scanner.setSourceBuffer(unit.compilationResult.compilationUnit.getContents());
			parseMethod(type, unit, position);
			return;
		}
	}
}
private void parseMethod(TypeDeclaration type, CompilationUnitDeclaration unit, int position) {
	//members
	TypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = memberTypes.length; --i >= 0;) {
			TypeDeclaration memberType = memberTypes[i];
			if (memberType.bodyStart > position) continue;
			if (memberType.declarationSourceEnd >= position) {
				parseMethod(memberType, unit, position);
				return;
			}
		}
	}

	//methods
	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		for (int i = methods.length; --i >= 0;) {
			AbstractMethodDeclaration method = methods[i];
			if (method.bodyStart > position) continue;
			if (method.declarationSourceEnd >= position) {
				getParser().parseBlockStatements(method, unit);
				return;
			}
		}
	}

	//initializers
	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		for (int i = fields.length; --i >= 0;) {
			if (!(fields[i] instanceof Initializer)) continue;
			Initializer initializer = (Initializer) fields[i];
			if (initializer.bodyStart > position) continue;
			if (initializer.declarationSourceEnd>= position) {
				getParser().parseBlockStatements(initializer, type, unit);
				return;
			}
		}
	}
}
protected void reset() {
	lookupEnvironment.reset();
}
}
