package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.problem.AbortType;

public class CodeSnippetTypeDeclaration extends TypeDeclaration {
	/**
	 * Generic bytecode generation for type
	 */
	public void generateCode(ClassFile enclosingClassFile) {
		if (ignoreFurtherInvestigation) {
			if (binding == null)
				return;
			CodeSnippetClassFile.createProblemType(
				this,
				scope.referenceCompilationUnit().compilationResult);
			return;
		}
		try {
			// create the result for a compiled type
			ClassFile classFile =
				new CodeSnippetClassFile(binding, enclosingClassFile, false);
			// generate all fiels
			classFile.addFieldInfos();

			// record the inner type inside its own .class file to be able
			// to generate inner classes attributes
			if (binding.isMemberType())
				classFile.recordEnclosingTypeAttributes(binding);
			if (binding.isLocalType()) {
				enclosingClassFile.recordNestedLocalAttribute(binding);
				classFile.recordNestedLocalAttribute(binding);
			}
			if (memberTypes != null) {
				for (int i = 0, max = memberTypes.length; i < max; i++) {
					// record the inner type inside its own .class file to be able
					// to generate inner classes attributes
					classFile.recordNestedMemberAttribute(memberTypes[i].binding);
					memberTypes[i].generateCode(scope, classFile);
				}
			}
			// generate all methods
			classFile.setForMethodInfos();
			if (methods != null) {
				for (int i = 0, max = methods.length; i < max; i++) {
					methods[i].generateCode(scope, classFile);
				}
			}

			// generate all methods
			classFile.addSpecialMethods();

			if (ignoreFurtherInvestigation) { // trigger problem type generation for code gen errors
				throw new AbortType(scope.referenceCompilationUnit().compilationResult);
			}

			// finalize the compiled type result
			classFile.addAttributes();
			scope.referenceCompilationUnit().compilationResult.record(
				binding.constantPoolName(),
				classFile);
		} catch (AbortType e) {
			if (binding == null)
				return;
			CodeSnippetClassFile.createProblemType(
				this,
				scope.referenceCompilationUnit().compilationResult);
		}
	}

}
