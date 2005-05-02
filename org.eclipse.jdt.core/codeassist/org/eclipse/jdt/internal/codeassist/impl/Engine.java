/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

public abstract class Engine implements ITypeRequestor {

	public LookupEnvironment lookupEnvironment;
	
	protected CompilationUnitScope unitScope;
	protected SearchableEnvironment nameEnvironment;

	public AssistOptions options;
	public CompilerOptions compilerOptions; 
	public boolean forbiddenReferenceIsError;
	public boolean discouragedReferenceIsError;
	
	public Engine(Map settings){
		this.options = new AssistOptions(settings);
		this.compilerOptions = new CompilerOptions(settings);
		this.forbiddenReferenceIsError =
			this.compilerOptions.getSeverity(CompilerOptions.ForbiddenReference) == ProblemSeverities.Error;
		this.discouragedReferenceIsError =
			this.compilerOptions.getSeverity(CompilerOptions.DiscouragedReference) == ProblemSeverities.Error;
	}
	
	/**
	 * Add an additional binary type
	 */
	public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	}

	/**
	 * Add an additional compilation unit.
	 */
	public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
		CompilationResult result = new CompilationResult(sourceUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit =
			this.getParser().dietParse(sourceUnit, result);

		lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		lookupEnvironment.completeTypeBindings(parsedUnit, true);
	}

	/**
	 * Add additional source types (the first one is the requested type, the rest is formed by the
	 * secondary types defined in the same compilation unit).
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,//sourceTypes[0] is always toplevel here
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE, // need member types
				// no need for field initialization
				lookupEnvironment.problemReporter,
				result);

		if (unit != null) {
			lookupEnvironment.buildTypeBindings(unit, accessRestriction);
			lookupEnvironment.completeTypeBindings(unit, true);
		}
	}

	public abstract AssistParser getParser();
	
	protected boolean mustQualifyType(
		char[] packageName,
		char[] typeName) {

		// If there are no types defined into the current CU yet.
		if (unitScope == null)
			return true;
			
		char[][] compoundPackageName = CharOperation.splitOn('.', packageName);
		char[] readableTypeName = CharOperation.concat(packageName, typeName, '.');

		if (CharOperation.equals(unitScope.fPackage.compoundName, compoundPackageName))
			return false;

		ImportBinding[] imports = unitScope.imports;
		if (imports != null){
			for (int i = 0, length = imports.length; i < length; i++) {
				if (imports[i].onDemand) {
					if (CharOperation.equals(imports[i].compoundName, compoundPackageName)) {
						for (int j = 0; j < imports.length; j++) {
							if(i != j){
								if(imports[j].onDemand) {
									if(nameEnvironment.findType(typeName, imports[j].compoundName) != null){
										return true;
									}
								} else {
									if(CharOperation.equals(CharOperation.lastSegment(imports[j].readableName(), '.'), typeName)
										&& !CharOperation.equals(imports[j].compoundName, CharOperation.splitOn('.', readableTypeName))) {
										return true;	
									}
								}
							}
						}
						return false; // how do you match p1.p2.A.* ?
					}
	
				} else
	
					if (CharOperation.equals(imports[i].readableName(), readableTypeName)) {
						return false;
					}
			}
		}
		return true;
	}

	/*
	 * Find the node (a field, a method or an initializer) at the given position 
	 * and parse its block statements if it is a method or an initializer.
	 * Returns the node or null if not found
	 */
	protected ASTNode parseBlockStatements(CompilationUnitDeclaration unit, int position) {
		int length = unit.types.length;
		for (int i = 0; i < length; i++) {
			TypeDeclaration type = unit.types[i];
			if (type.declarationSourceStart < position
				&& type.declarationSourceEnd >= position) {
				getParser().scanner.setSource(unit.compilationResult);
				return parseBlockStatements(type, unit, position);
			}
		}
		return null;
	}

	private ASTNode parseBlockStatements(
		TypeDeclaration type,
		CompilationUnitDeclaration unit,
		int position) {
		//members
		TypeDeclaration[] memberTypes = type.memberTypes;
		if (memberTypes != null) {
			int length = memberTypes.length;
			for (int i = 0; i < length; i++) {
				TypeDeclaration memberType = memberTypes[i];
				if (memberType.bodyStart > position)
					continue;
				if (memberType.declarationSourceEnd >= position) {
					return parseBlockStatements(memberType, unit, position);
				}
			}
		}
		//methods
		AbstractMethodDeclaration[] methods = type.methods;
		if (methods != null) {
			int length = methods.length;
			for (int i = 0; i < length; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.bodyStart > position)
					continue;
				
				if(method.isDefaultConstructor())
					continue;
				
				if (method.declarationSourceEnd >= position) {
					
					getParser().parseBlockStatements(method, unit);
					return method;
				}
			}
		}
		//initializers
		FieldDeclaration[] fields = type.fields;
		if (fields != null) {
			int length = fields.length;
			for (int i = 0; i < length; i++) {
				FieldDeclaration field = fields[i];
				if (field.sourceStart > position)
					continue;
				if (field.declarationSourceEnd >= position) {
					if (field instanceof Initializer) {
						getParser().parseBlockStatements((Initializer)field, type, unit);
					}
					return field;
				}
			}
		}
		return null;
	}

	protected void reset() {
		lookupEnvironment.reset();
	}
	
	public static char[] getSignature(Binding binding) {
		char[] result = null;
		if ((binding.kind() & Binding.TYPE) != 0) {
			TypeBinding typeBinding = (TypeBinding)binding;
			if(typeBinding.isLocalType()) {
				LocalTypeBinding localTypeBinding = (LocalTypeBinding)typeBinding;
				if(localTypeBinding.isAnonymousType()) {
					typeBinding = localTypeBinding.superclass();
				} else {
					localTypeBinding.setConstantPoolName(typeBinding.sourceName());
				}
			}
			result = typeBinding.genericTypeSignature();
		} else if ((binding.kind() & Binding.METHOD) != 0) {
			MethodBinding methodBinding = (MethodBinding)binding;
			int oldMod = methodBinding.modifiers;
			//TODO remove the next line when method from binary type will be able to generate generic siganute
			methodBinding.modifiers |= CompilerModifiers.AccGenericSignature;
			result = methodBinding.genericSignature(); 
			if(result == null) {
				result = methodBinding.signature();
			}
			methodBinding.modifiers = oldMod;
		}
		result = CharOperation.replaceOnCopy(result, '/', '.');
		return result;
	}
}
