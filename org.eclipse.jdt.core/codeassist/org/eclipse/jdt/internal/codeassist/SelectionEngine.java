package org.eclipse.jdt.internal.codeassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Locale;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.codeassist.impl.*;
import org.eclipse.jdt.internal.codeassist.select.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.impl.*;

/**
 * The selection engine is intended to infer the nature of a selected name in some
 * source code. This name can be qualified.
 *
 * Selection is resolving context using a name environment (no need to search), assuming
 * the source where selection occurred is correct and will not perform any completion
 * attempt. If this was the desired behavior, a call to the CompletionEngine should be
 * performed instead.
 */
public final class SelectionEngine extends Engine implements ISearchRequestor {
	SelectionParser parser;
	ISearchableNameEnvironment nameEnvironment;
	ISelectionRequestor requestor;

	CompilationUnitScope unitScope;
	boolean acceptedAnswer;

	private int actualSelectionStart;
	private int actualSelectionEnd;
	private char[] qualifiedSelection;
	private char[] selectedIdentifier;
	/**
	 * The SelectionEngine is responsible for computing the selected object.
	 *
	 * It requires a searchable name environment, which supports some
	 * specific search APIs, and a requestor to feed back the results to a UI.
	 *
	 *  @param environment com.ibm.codeassist.java.api.INameEnvironment
	 *      used to resolve type/package references and search for types/packages
	 *      based on partial names.
	 *
	 *  @param requestor com.ibm.codeassist.java.api.ISelectionRequestor
	 *      since the engine might produce answers of various forms, the engine 
	 *      is associated with a requestor able to accept all possible completions.
	 *
	 *  @param options com.ibm.compiler.java.api.ConfigurableOptions
	 *		set of options used to configure the code assist engine.
	 */

	public SelectionEngine(
		ISearchableNameEnvironment nameEnvironment,
		ISelectionRequestor requestor,
		ConfigurableOption[] settings) {
		this.requestor = requestor;
		this.nameEnvironment = nameEnvironment;

		CompilerOptions options = new CompilerOptions(settings);
		ProblemReporter problemReporter =
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory(Locale.getDefault())) {
			public void record(IProblem problem, CompilationResult unitResult) {
				unitResult.record(problem);
				SelectionEngine.this.requestor.acceptError(problem);
			}
		};

		this.parser = new SelectionParser(problemReporter);
		this.lookupEnvironment =
			new LookupEnvironment(this, options, problemReporter, nameEnvironment);
	}

	/**
	 * One result of the search consists of a new class.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	public void acceptClass(char[] packageName, char[] className, int modifiers) {
		if (CharOperation.equals(className, selectedIdentifier)) {

			if (qualifiedSelection != null
				&& !CharOperation.equals(
					qualifiedSelection,
					CharOperation.concat(packageName, className, '.'))) {
				return;
			}

			requestor.acceptClass(
				packageName,
				className,
				mustQualifyType(CharOperation.splitOn('.', packageName), className));
			acceptedAnswer = true;
		}
	}

	/**
	 * One result of the search consists of a new interface.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.I".
	 *    The default package is represented by an empty array.
	 */
	public void acceptInterface(
		char[] packageName,
		char[] interfaceName,
		int modifiers) {
		if (CharOperation.equals(interfaceName, selectedIdentifier)) {

			if (qualifiedSelection != null
				&& !CharOperation.equals(
					qualifiedSelection,
					CharOperation.concat(packageName, interfaceName, '.'))) {
				return;
			}

			requestor.acceptInterface(
				packageName,
				interfaceName,
				mustQualifyType(CharOperation.splitOn('.', packageName), interfaceName));
			acceptedAnswer = true;
		}
	}

	/**
	 * One result of the search consists of a new package.
	 *
	 * NOTE - All package names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    The default package is represented by an empty array.
	 */
	public void acceptPackage(char[] packageName) {
	}

	/**
	 * One result of the search consists of a new type.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	public void acceptType(char[] packageName, char[] typeName) {
		acceptClass(packageName, typeName, 0);
	}

	private boolean checkSelection(
		char[] source,
		int selectionStart,
		int selectionEnd) {

		Scanner scanner = new Scanner();
		scanner.setSourceBuffer(source);
		scanner.resetTo(selectionStart, selectionEnd);

		int lastIdentifierStart = -1;
		int lastIdentifierEnd = -1;
		int token, identCount = 0;
		char[] lastIdentifier = null;
		boolean expectingIdentifier = true;
		StringBuffer entireSelection =
			new StringBuffer(selectionEnd - selectionStart + 1);
		do {
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
				return false;
			}
			switch (token) {
				case TerminalSymbols.TokenNameIdentifier :
					if (!expectingIdentifier)
						return false;
					entireSelection.append(lastIdentifier = scanner.getCurrentIdentifierSource());
					lastIdentifierStart = scanner.startPosition;
					lastIdentifierEnd = scanner.currentPosition - 1;
					identCount++;
					expectingIdentifier = false;
					break;
				case TerminalSymbols.TokenNameDOT :
					if (expectingIdentifier)
						return false;
					entireSelection.append('.');
					expectingIdentifier = true;
					break;
				case TerminalSymbols.TokenNameEOF :
					if (expectingIdentifier)
						return false;
					break;
				default :
					return false;
			}
		} while (token != TerminalSymbols.TokenNameEOF);
		if (lastIdentifierStart > 0) {
			actualSelectionStart = lastIdentifierStart;
			actualSelectionEnd = lastIdentifierEnd;
			selectedIdentifier = lastIdentifier;
			if (identCount > 1)
				qualifiedSelection = entireSelection.toString().toCharArray();
			return true;
		}
		return false;
	}

	public AssistParser getParser() {
		return parser;
	}

	private boolean mustQualifyType(
		char[][] packageName,
		char[] readableTypeName) {
		// If there are no types defined into the current CU yet.
		if (unitScope == null)
			return true;
		if (CharOperation.equals(unitScope.fPackage.compoundName, packageName))
			return false;

		ImportBinding[] imports = unitScope.imports;
		for (int i = 0, length = imports.length; i < length; i++) {
			if (imports[i].onDemand) {
				if (CharOperation.equals(imports[i].compoundName, packageName))
					return false; // how do you match p1.p2.A.* ?
			} else
				if (CharOperation.equals(imports[i].readableName(), readableTypeName)) {
					return false;
				}
		}
		return true;
	}

	/**
	 * Ask the engine to compute the selection at the specified position
	 * of the given compilation unit.
	 *
	 *  @return void
	 *      the selection result is answered through a requestor.
	 *
	 *  @param unit com.ibm.compiler.java.api.env.ICompilationUnit
	 *      the source of the current compilation unit.
	 *
	 *  @param selectionSourceStart int
	 *  @param selectionSourceEnd int
	 *      a range in the source where the selection is.
	 */
	public void select(
		ICompilationUnit sourceUnit,
		int selectionSourceStart,
		int selectionSourceEnd) {

		char[] source = sourceUnit.getContents();
		if (!checkSelection(source, selectionSourceStart, selectionSourceEnd - 1))
			return;
		try {
			acceptedAnswer = false;
			CompilationResult result = new CompilationResult(sourceUnit, 1, 1);
			CompilationUnitDeclaration parsedUnit =
				parser.dietParse(sourceUnit, result, actualSelectionStart, actualSelectionEnd);

			if (parsedUnit != null) {
				// scan the package & import statements first
				if (parsedUnit.currentPackage instanceof SelectionOnPackageReference) {
					char[][] tokens =
						((SelectionOnPackageReference) parsedUnit.currentPackage).tokens;
					requestor.acceptPackage(CharOperation.concatWith(tokens, '.'));
					return;
				}
				ImportReference[] imports = parsedUnit.imports;
				if (imports != null) {
					for (int i = 0, length = imports.length; i < length; i++) {
						ImportReference importReference = imports[i];
						if (importReference instanceof SelectionOnImportReference) {
							char[][] tokens = ((SelectionOnImportReference) importReference).tokens;
							requestor.acceptPackage(CharOperation.concatWith(tokens, '.'));
							nameEnvironment.findTypes(CharOperation.concatWith(tokens, '.'), this);
							if (!acceptedAnswer)
								nameEnvironment.findTypes(selectedIdentifier, this);
							// try with simple type name
							return;
						}
					}
				}

				if (parsedUnit.types != null) {
					lookupEnvironment.buildTypeBindings(parsedUnit);
					if (parsedUnit.scope != null) {
						try {
							lookupEnvironment.completeTypeBindings(parsedUnit, true);
							parsedUnit.scope.faultInTypes();
							parseMethod(parsedUnit, selectionSourceStart);
							parsedUnit.resolve();
						} catch (SelectionNodeFound e) {
							if (e.binding != null) {
								// if null then we found a problem in the selection node
								selectFrom(e.binding);
							}
						}
					}
				}
			}

			// only reaches here if no selection could be derived from the parsed tree
			// thus use the selected source and perform a textual type search
			if (!acceptedAnswer) {
				nameEnvironment.findTypes(selectedIdentifier, this);
			}
		} catch (IndexOutOfBoundsException e) { // work-around internal failure - 1GEMF6D		
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
		} finally {
			reset();
		}
	}

	private void selectFrom(Binding binding) {
		if (binding instanceof ReferenceBinding) {
			ReferenceBinding typeBinding = (ReferenceBinding) binding;
			if (qualifiedSelection != null
				&& !CharOperation.equals(qualifiedSelection, typeBinding.readableName())) {
				return;
			}
			if (typeBinding.isInterface())
				requestor.acceptInterface(
					typeBinding.qualifiedPackageName(),
					typeBinding.qualifiedSourceName(),
					false);
			else
				requestor.acceptClass(
					typeBinding.qualifiedPackageName(),
					typeBinding.qualifiedSourceName(),
					false);
			acceptedAnswer = true;
		} else
			if (binding instanceof MethodBinding) {
				MethodBinding methodBinding = (MethodBinding) binding;
				TypeBinding[] parameterTypes = methodBinding.parameters;
				int length = parameterTypes.length;
				char[][] parameterPackageNames = new char[length][];
				char[][] parameterTypeNames = new char[length][];
				for (int i = 0; i < length; i++) {
					parameterPackageNames[i] = parameterTypes[i].qualifiedPackageName();
					parameterTypeNames[i] = parameterTypes[i].qualifiedSourceName();
				}
				requestor.acceptMethod(
					methodBinding.declaringClass.qualifiedPackageName(),
					methodBinding.declaringClass.qualifiedSourceName(),
					methodBinding.isConstructor()
						? methodBinding.declaringClass.sourceName()
						: methodBinding.selector,
					parameterPackageNames,
					parameterTypeNames);
				acceptedAnswer = true;
			} else
				if (binding instanceof FieldBinding) {
					FieldBinding fieldBinding = (FieldBinding) binding;
					if (fieldBinding.declaringClass != null) { // arraylength
						requestor.acceptField(
							fieldBinding.declaringClass.qualifiedPackageName(),
							fieldBinding.declaringClass.qualifiedSourceName(),
							fieldBinding.name);
						acceptedAnswer = true;
					}
				} else
					if (binding instanceof LocalVariableBinding) {
						selectFrom(((LocalVariableBinding) binding).type);
						// open on the type of the variable
					} else
						if (binding instanceof ArrayBinding) {
							selectFrom(((ArrayBinding) binding).leafComponentType);
							// open on the type of the array
						} else
							if (binding instanceof PackageBinding) {
								PackageBinding packageBinding = (PackageBinding) binding;
								requestor.acceptPackage(packageBinding.readableName());
								acceptedAnswer = true;
							}
	}

	/**
	 * Asks the engine to compute the selection of the given type
	 * from the source type.
	 *
	 *  @return void
	 *      the selection result is answered through a requestor.
	 *
	 *  @param sourceType com.ibm.compiler.java.api.env.ISourceType
	 *      a source form of the current type in which code assist is invoked.
	 *
	 *  @param typeName char[]
	 *      a type name which is to be resolved in the context of a compilation unit.
	 *		NOTE: the type name is supposed to be correctly reduced (no whitespaces, no unicodes left)
	 */

	public void selectType(ISourceType sourceType, char[] typeName) {
		try {
			acceptedAnswer = false;

			// find the outer most type
			ISourceType outerType = sourceType;
			ISourceType parent = sourceType.getEnclosingType();
			while (parent != null) {
				outerType = parent;
				parent = parent.getEnclosingType();
			}

			// compute parse tree for this most outer type
			CompilationResult result = new CompilationResult(outerType.getFileName(), 1, 1);
				CompilationUnitDeclaration parsedUnit =
					SourceTypeConverter.buildCompilationUnit(outerType, false,
			// don't need field and methods
	this.parser.problemReporter(), result);

			if (parsedUnit != null && parsedUnit.types != null) {
				// find the type declaration that corresponds to the original source type
				char[] packageName = sourceType.getPackageName();
				char[] sourceTypeName = sourceType.getQualifiedName();
				// the fully qualified name without the package name
				if (packageName != null) {
					// remove the package name if necessary
					sourceTypeName =
						CharOperation.subarray(
							sourceType.getQualifiedName(),
							packageName.length + 1,
							sourceTypeName.length);
				};
				TypeDeclaration typeDecl =
					parsedUnit.declarationOfType(CharOperation.splitOn('.', sourceTypeName));
				if (typeDecl != null) {

					// add fake field with the type we're looking for
					// note: since we didn't ask for fields above, there is no field defined yet
					FieldDeclaration field = new FieldDeclaration();
					int dot;
					if ((dot = CharOperation.lastIndexOf('.', typeName)) == -1) {
						this.selectedIdentifier = typeName;
						field.type = new SelectionOnSingleTypeReference(typeName, -1);
						// position not used
					} else {
						char[][] previousIdentifiers = CharOperation.splitOn('.', typeName, 0, dot - 1);
						char[] selectionIdentifier =
							CharOperation.subarray(typeName, dot + 1, typeName.length);
						this.selectedIdentifier = selectionIdentifier;
						field.type =
							new SelectionOnQualifiedTypeReference(
								previousIdentifiers,
								selectionIdentifier,
								new long[previousIdentifiers.length + 1]);
					}
					field.name = "<fakeField>".toCharArray();
					typeDecl.fields = new FieldDeclaration[] { field };

					// build bindings
					lookupEnvironment.buildTypeBindings(parsedUnit);
					if ((this.unitScope = parsedUnit.scope) != null) {
						try {
							// build fields
							// note: this builds fields only in the parsed unit (the buildFieldsAndMethods flag is not passed along)
							this.lookupEnvironment.completeTypeBindings(parsedUnit, true);

							// resolve
							parsedUnit.scope.faultInTypes();
							parsedUnit.resolve();
						} catch (SelectionNodeFound e) {
							if (e.binding != null) {
								// if null then we found a problem in the selection node
								selectFrom(e.binding);
							}
						}
					}
				}
			}

			// only reaches here if no selection could be derived from the parsed tree
			// thus use the selected source and perform a textual type search
			if (!acceptedAnswer) {
				if (this.selectedIdentifier != null) {
					nameEnvironment.findTypes(typeName, this);
				}
			}
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
		} finally {
			reset();
		}
	}

}
