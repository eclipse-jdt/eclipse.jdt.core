/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.*;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.codeassist.impl.*;
import org.eclipse.jdt.internal.codeassist.select.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.core.SelectionRequestor;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.util.ASTNodeFinder;
import org.eclipse.jdt.internal.core.util.ElementInfoConverter;

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

	public static boolean DEBUG = false;
	
	SelectionParser parser;
	ISelectionRequestor requestor;

	boolean acceptedAnswer;

	private int actualSelectionStart;
	private int actualSelectionEnd;
	private char[] qualifiedSelection;
	private char[] selectedIdentifier;
	
	private char[][][] acceptedClasses;
	private char[][][] acceptedInterfaces;
	int acceptedClassesCount;
	int acceptedInterfacesCount;
	
	boolean noProposal = true;
	IProblem problem = null;

	/**
	 * The SelectionEngine is responsible for computing the selected object.
	 *
	 * It requires a searchable name environment, which supports some
	 * specific search APIs, and a requestor to feed back the results to a UI.
	 *
	 *  @param nameEnvironment org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment
	 *      used to resolve type/package references and search for types/packages
	 *      based on partial names.
	 *
	 *  @param requestor org.eclipse.jdt.internal.codeassist.ISelectionRequestor
	 *      since the engine might produce answers of various forms, the engine 
	 *      is associated with a requestor able to accept all possible completions.
	 *
	 *  @param settings java.util.Map
	 *		set of options used to configure the code assist engine.
	 */
	public SelectionEngine(
		ISearchableNameEnvironment nameEnvironment,
		ISelectionRequestor requestor,
		Map settings) {

		super(settings);

		this.requestor = requestor;
		this.nameEnvironment = nameEnvironment;

		ProblemReporter problemReporter =
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.compilerOptions,
				new DefaultProblemFactory(Locale.getDefault())) {
					
			public IProblem createProblem(
				char[] fileName,
				int problemId,
				String[] problemArguments,
				String[] messageArguments,
				int severity,
				int problemStartPosition,
				int problemEndPosition,
				int lineNumber) {
				IProblem pb =  super.createProblem(
					fileName,
					problemId,
					problemArguments,
					messageArguments,
					severity,
					problemStartPosition,
					problemEndPosition,
					lineNumber);
					if(SelectionEngine.this.problem == null && pb.isError() && (pb.getID() & IProblem.Syntax) == 0) {
						SelectionEngine.this.problem = pb;
					}

					return pb;
			}
		};
		this.lookupEnvironment =
			new LookupEnvironment(this, this.compilerOptions, problemReporter, nameEnvironment);
		this.parser = new SelectionParser(problemReporter);
	}

	/**
	 * One result of the search consists of a new class.
	 * @param packageName char[]
	 * @param className char[]
	 * @param modifiers int
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
			
			if(mustQualifyType(packageName, className)) {
				char[][] acceptedClass = new char[2][];
				acceptedClass[0] = packageName;
				acceptedClass[1] = className;
				
				if(acceptedClasses == null) {
					acceptedClasses = new char[10][][];
					acceptedClassesCount = 0;
				}
				int length = acceptedClasses.length;
				if(length == acceptedClassesCount) {
					System.arraycopy(acceptedClasses, 0, acceptedClasses = new char[(length + 1)* 2][][], 0, length);
				}
				acceptedClasses[acceptedClassesCount++] = acceptedClass;
				
			} else {
				noProposal = false;
				requestor.acceptClass(
					packageName,
					className,
					false);
				this.acceptedAnswer = true;
			}
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
			
			if(mustQualifyType(packageName, interfaceName)) {
				char[][] acceptedInterface= new char[2][];
				acceptedInterface[0] = packageName;
				acceptedInterface[1] = interfaceName;
				
				if(acceptedInterfaces == null) {
					acceptedInterfaces = new char[10][][];
					acceptedInterfacesCount = 0;
				}
				int length = acceptedInterfaces.length;
				if(length == acceptedInterfacesCount) {
					System.arraycopy(acceptedInterfaces, 0, acceptedInterfaces = new char[(length + 1) * 2][][], 0, length);
				}
				acceptedInterfaces[acceptedInterfacesCount++] = acceptedInterface;
				
			} else {
				noProposal = false;
				this.requestor.acceptInterface(
					packageName,
					interfaceName,
					false);
				this.acceptedAnswer = true;
			}
		}
	}

	/**
	 * One result of the search consists of a new package.
	 * @param packageName char[]
	 * 
	 * NOTE - All package names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    The default package is represented by an empty array.
	 */
	public void acceptPackage(char[] packageName) {
		// implementation of interface method
	}

	private void acceptQualifiedTypes() {
		if(acceptedClasses != null){
			this.acceptedAnswer = true;
			for (int i = 0; i < acceptedClassesCount; i++) {
				noProposal = false;
				this.requestor.acceptClass(
					acceptedClasses[i][0],
					acceptedClasses[i][1],
					true);
			}
			acceptedClasses = null;
			acceptedClassesCount = 0;
		}
		if(acceptedInterfaces != null){
			this.acceptedAnswer = true;
			for (int i = 0; i < acceptedInterfacesCount; i++) {
				noProposal = false;
				this.requestor.acceptInterface(
					acceptedInterfaces[i][0],
					acceptedInterfaces[i][1],
					true);
			}
			acceptedInterfaces = null;
			acceptedInterfacesCount = 0;
		}
	}
	
	/**
	 * One result of the search consists of a new type.
	 * @param packageName char[]
	 * @param typeName char[]
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
		scanner.setSource(source);
		
		int lastIdentifierStart = -1;
		int lastIdentifierEnd = -1;
		char[] lastIdentifier = null;
		int token, identCount = 0;
		StringBuffer entireSelection = new StringBuffer(selectionEnd - selectionStart + 1);
		
		if(selectionStart > selectionEnd){
			
			// compute start position of current line
			int currentPosition = selectionStart - 1;
			int nextCharacterPosition = selectionStart;
			char currentCharacter = ' ';
			try {
				while(currentPosition > 0){
					
					if(source[currentPosition] == '\\' && source[currentPosition+1] == 'u') {
						int pos = currentPosition + 2;
						int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
						while (source[pos] == 'u') {
							pos++;
						}
						if ((c1 = Character.getNumericValue(source[pos++])) > 15
							|| c1 < 0
							|| (c2 = Character.getNumericValue(source[pos++])) > 15
							|| c2 < 0
							|| (c3 = Character.getNumericValue(source[pos++])) > 15
							|| c3 < 0
							|| (c4 = Character.getNumericValue(source[pos++])) > 15
							|| c4 < 0) {
							return false;
						} else {
							currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
							nextCharacterPosition = pos;
						}
					} else {
						currentCharacter = source[currentPosition];
						nextCharacterPosition = currentPosition+1;
					}
					
					if(currentCharacter == '\r' || currentCharacter == '\n') {
						break;
					}
					currentPosition--;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				return false;
			}
			
			// compute start and end of the last token
			scanner.resetTo(nextCharacterPosition, selectionEnd + 1 == source.length ? selectionEnd : selectionEnd + 1);
			do {
				try {
					token = scanner.getNextToken();
				} catch (InvalidInputException e) {
					return false;
				}
				if((token == TerminalTokens.TokenNamethis ||
					token == TerminalTokens.TokenNamesuper ||
					token == TerminalTokens.TokenNameIdentifier) &&
					scanner.startPosition <= selectionStart &&
					selectionStart <= scanner.currentPosition) {
					lastIdentifierStart = scanner.startPosition;
					lastIdentifierEnd = scanner.currentPosition - 1;
					lastIdentifier = scanner.getCurrentTokenSource();
				}
			} while (token != TerminalTokens.TokenNameEOF);
		} else {
			scanner.resetTo(selectionStart, selectionEnd);
	
			boolean expectingIdentifier = true;
			
			do {
				try {
					token = scanner.getNextToken();
				} catch (InvalidInputException e) {
					return false;
				}
				switch (token) {
					case TerminalTokens.TokenNamethis :
					case TerminalTokens.TokenNamesuper :
					case TerminalTokens.TokenNameIdentifier :
						if (!expectingIdentifier)
							return false;
						lastIdentifier = scanner.getCurrentTokenSource();
						lastIdentifierStart = scanner.startPosition;
						lastIdentifierEnd = scanner.currentPosition - 1;
						if(lastIdentifierEnd > selectionEnd) {
							lastIdentifierEnd = selectionEnd;
							lastIdentifier = CharOperation.subarray(lastIdentifier, 0,lastIdentifierEnd - lastIdentifierStart + 1);
						}
						entireSelection.append(lastIdentifier);
							
						identCount++;
						expectingIdentifier = false;
						break;
					case TerminalTokens.TokenNameDOT :
						if (expectingIdentifier)
							return false;
						entireSelection.append('.');
						expectingIdentifier = true;
						break;
					case TerminalTokens.TokenNameEOF :
						if (expectingIdentifier)
							return false;
						break;
					default :
						return false;
				}
			} while (token != TerminalTokens.TokenNameEOF);
		}
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

	/*
	 * Returns whether the given binding is a local/anonymous reference binding, or if its declaring class is
	 * local.
	 */
	private boolean isLocal(ReferenceBinding binding) {
		if (!(binding instanceof SourceTypeBinding)) return false;
		if (binding instanceof LocalTypeBinding) return true;
		if (binding instanceof MemberTypeBinding) {
			return isLocal(((MemberTypeBinding)binding).enclosingType);
		}
		return false;
	}

	/**
	 * Ask the engine to compute the selection at the specified position
	 * of the given compilation unit.

	 *  @param sourceUnit org.eclipse.jdt.internal.compiler.env.ICompilationUnit
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
		
		if(DEBUG) {
			System.out.print("SELECTION IN "); //$NON-NLS-1$
			System.out.print(sourceUnit.getFileName());
			System.out.print(" FROM "); //$NON-NLS-1$
			System.out.print(selectionSourceStart);
			System.out.print(" TO "); //$NON-NLS-1$
			System.out.println(selectionSourceEnd);
			System.out.println("SELECTION - Source :"); //$NON-NLS-1$
			System.out.println(source);
		}
		if (!checkSelection(source, selectionSourceStart, selectionSourceEnd))
			return;
		try {
			this.acceptedAnswer = false;
			CompilationResult result = new CompilationResult(sourceUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
			CompilationUnitDeclaration parsedUnit =
				parser.dietParse(sourceUnit, result, actualSelectionStart, actualSelectionEnd);

			if (parsedUnit != null) {
				if(DEBUG) {
					System.out.println("SELECTION - Diet AST :"); //$NON-NLS-1$
					System.out.println(parsedUnit.toString());
				}
				
				// scan the package & import statements first
				if (parsedUnit.currentPackage instanceof SelectionOnPackageReference) {
					char[][] tokens =
						((SelectionOnPackageReference) parsedUnit.currentPackage).tokens;
					noProposal = false;
					requestor.acceptPackage(CharOperation.concatWith(tokens, '.'));
					return;
				}
				ImportReference[] imports = parsedUnit.imports;
				if (imports != null) {
					for (int i = 0, length = imports.length; i < length; i++) {
						ImportReference importReference = imports[i];
						if (importReference instanceof SelectionOnImportReference) {
							char[][] tokens = ((SelectionOnImportReference) importReference).tokens;
							noProposal = false;
							requestor.acceptPackage(CharOperation.concatWith(tokens, '.'));
							nameEnvironment.findTypes(CharOperation.concatWith(tokens, '.'), this);
							// accept qualified types only if no unqualified type was accepted
							if(!acceptedAnswer) {
								acceptQualifiedTypes();
								if (!acceptedAnswer) {
									nameEnvironment.findTypes(selectedIdentifier, this);
									// try with simple type name
									if(!acceptedAnswer) {
										acceptQualifiedTypes();
									}
								}
							}
							if(noProposal && problem != null) {
								requestor.acceptError(problem);
							}
							return;
						}
					}
				}
				if (parsedUnit.types != null) {
					lookupEnvironment.buildTypeBindings(parsedUnit);
					if ((this.unitScope = parsedUnit.scope)  != null) {
						try {
							lookupEnvironment.completeTypeBindings(parsedUnit, true);
							parsedUnit.scope.faultInTypes();
							selectDeclaration(parsedUnit);
							ASTNode node = parseBlockStatements(parsedUnit, selectionSourceStart);
							if(DEBUG) {
								System.out.println("SELECTION - AST :"); //$NON-NLS-1$
								System.out.println(parsedUnit.toString());
							}
							parsedUnit.resolve();
							if (node != null) {
								selectLocalDeclaration(node);
							}
						} catch (SelectionNodeFound e) {
							if (e.binding != null) {
								if(DEBUG) {
									System.out.println("SELECTION - Selection binding:"); //$NON-NLS-1$
									System.out.println(e.binding.toString());
								}
								// if null then we found a problem in the selection node
								selectFrom(e.binding, parsedUnit, e.isDeclaration);
							}
						}
					}
				}
			}
			// only reaches here if no selection could be derived from the parsed tree
			// thus use the selected source and perform a textual type search
			if (!acceptedAnswer) {
				nameEnvironment.findTypes(selectedIdentifier, this);
				
				// accept qualified types only if no unqualified type was accepted
				if(!acceptedAnswer) {
					acceptQualifiedTypes();
				}
			}
			if(noProposal && problem != null) {
				requestor.acceptError(problem);
			}
		} catch (IndexOutOfBoundsException e) { // work-around internal failure - 1GEMF6D		
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
		} finally {
			reset();
		}
	}

	private void selectFrom(Binding binding, CompilationUnitDeclaration parsedUnit, boolean isDeclaration) {
		if (binding instanceof ReferenceBinding) {
			ReferenceBinding typeBinding = (ReferenceBinding) binding;
			if (qualifiedSelection != null
				&& !CharOperation.equals(qualifiedSelection, typeBinding.readableName())) {
				return;
			}
			if (typeBinding.isInterface()) {
				noProposal = false;
				if (isLocal(typeBinding) && this.requestor instanceof SelectionRequestor) {
					((SelectionRequestor)this.requestor).acceptLocalType(
						(SourceTypeBinding)typeBinding,
						parsedUnit);
				} else {
					this.requestor.acceptInterface(
						typeBinding.qualifiedPackageName(),
						typeBinding.qualifiedSourceName(),
						false);
				}
			} else if(typeBinding instanceof ProblemReferenceBinding){
				ReferenceBinding original = ((ProblemReferenceBinding) typeBinding).original;
				if(original == null) return;
				noProposal = false;
				if (isLocal(original) && this.requestor instanceof SelectionRequestor) {
					((SelectionRequestor)this.requestor).acceptLocalType(
						(SourceTypeBinding)original,
						parsedUnit);
				} else {
					this.requestor.acceptClass(
						original.qualifiedPackageName(),
						original.qualifiedSourceName(),
						false);
				}
			} else {
				noProposal = false;
				if (isLocal(typeBinding) && this.requestor instanceof SelectionRequestor) {
					((SelectionRequestor)this.requestor).acceptLocalType(
						(SourceTypeBinding)typeBinding,
						parsedUnit);
				} else {
					this.requestor.acceptClass(
						typeBinding.qualifiedPackageName(),
						typeBinding.qualifiedSourceName(),
						false);
				}
			}
			this.acceptedAnswer = true;
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
				noProposal = false;
				ReferenceBinding declaringClass = methodBinding.declaringClass;
				if (isLocal(declaringClass) && this.requestor instanceof SelectionRequestor) {
					((SelectionRequestor)this.requestor).acceptLocalMethod(
						(SourceTypeBinding)declaringClass,
						methodBinding.isConstructor()
							? declaringClass.sourceName()
							: methodBinding.selector,
						parameterPackageNames,
						parameterTypeNames,
						methodBinding.isConstructor(),
						parsedUnit,
						isDeclaration,
						actualSelectionStart,
						actualSelectionEnd);
				} else {
					this.requestor.acceptMethod(
						declaringClass.qualifiedPackageName(),
						declaringClass.qualifiedSourceName(),
						methodBinding.isConstructor()
							? declaringClass.sourceName()
							: methodBinding.selector,
						parameterPackageNames,
						parameterTypeNames,
						methodBinding.isConstructor(), 
						isDeclaration,
						actualSelectionStart,
						actualSelectionEnd);
				}
				this.acceptedAnswer = true;
			} else
				if (binding instanceof FieldBinding) {
					FieldBinding fieldBinding = (FieldBinding) binding;
					ReferenceBinding declaringClass = fieldBinding.declaringClass;
					if (declaringClass != null) { // arraylength
						noProposal = false;
						if (isLocal(declaringClass) && this.requestor instanceof SelectionRequestor) {
							((SelectionRequestor)this.requestor).acceptLocalField(
								(SourceTypeBinding)declaringClass,
								fieldBinding.name,
								parsedUnit);
						} else {
							this.requestor.acceptField(
								declaringClass.qualifiedPackageName(),
								declaringClass.qualifiedSourceName(),
								fieldBinding.name);
						}
						this.acceptedAnswer = true;
					}
				} else
					if (binding instanceof LocalVariableBinding) {
						if (this.requestor instanceof SelectionRequestor) {
							((SelectionRequestor)this.requestor).acceptLocalVariable(
								(LocalVariableBinding)binding,
								parsedUnit);
							this.acceptedAnswer = true;
						} else {
							// open on the type of the variable
							selectFrom(((LocalVariableBinding) binding).type, parsedUnit, false);
						}
					} else
						if (binding instanceof ArrayBinding) {
							selectFrom(((ArrayBinding) binding).leafComponentType, parsedUnit, false);
							// open on the type of the array
						} else
							if (binding instanceof PackageBinding) {
								PackageBinding packageBinding = (PackageBinding) binding;
								noProposal = false;
								requestor.acceptPackage(packageBinding.readableName());
								this.acceptedAnswer = true;
							} else
								if(binding instanceof BaseTypeBinding) {
									this.acceptedAnswer = true;
								}
	}
	
	/*
	 * Checks if a local declaration got selected in this method/initializer/field.
	 */
	private void selectLocalDeclaration(ASTNode node) {
		// the selected identifier is not identical to the parser one (equals but not identical),
		// for traversing the parse tree, the parser assist identifier is necessary for identitiy checks
		final char[] assistIdentifier = this.getParser().assistIdentifier();
		if (assistIdentifier == null) return;
		
		class Visitor extends ASTVisitor {
			public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
				if (constructorDeclaration.selector == assistIdentifier){
					if (constructorDeclaration.binding != null) {
						throw new SelectionNodeFound(constructorDeclaration.binding);
					} else {
						if (constructorDeclaration.scope != null) {
							throw new SelectionNodeFound(new MethodBinding(constructorDeclaration.modifiers, constructorDeclaration.selector, null, null, null, constructorDeclaration.scope.referenceType().binding));
						}
					}
				}
				return true;
			}
			public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
				if (fieldDeclaration.name == assistIdentifier){
					throw new SelectionNodeFound(fieldDeclaration.binding);
				}
				return true;
			}
			public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
				if (localTypeDeclaration.name == assistIdentifier) {
					throw new SelectionNodeFound(localTypeDeclaration.binding);
				}
				return true;
			}
			public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
				if (memberTypeDeclaration.name == assistIdentifier) {
					throw new SelectionNodeFound(memberTypeDeclaration.binding);
				}
				return true;
			}
			public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
				if (methodDeclaration.selector == assistIdentifier){
					if (methodDeclaration.binding != null) {
						throw new SelectionNodeFound(methodDeclaration.binding);
					} else {
						if (methodDeclaration.scope != null) {
							throw new SelectionNodeFound(new MethodBinding(methodDeclaration.modifiers, methodDeclaration.selector, null, null, null, methodDeclaration.scope.referenceType().binding));
						}
					}
				}
				return true;
			}
			public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
				if (typeDeclaration.name == assistIdentifier) {
					throw new SelectionNodeFound(typeDeclaration.binding);
				}
				return true;
			}
		}
		
		if (node instanceof AbstractMethodDeclaration) {
			((AbstractMethodDeclaration)node).traverse(new Visitor(), (ClassScope)null);
		} else {
			((FieldDeclaration)node).traverse(new Visitor(), (MethodScope)null);
		}
	}

	/**
	 * Asks the engine to compute the selection of the given type
	 * from the source type.
	 *
	 *  @param sourceType org.eclipse.jdt.internal.compiler.env.ISourceType
	 *      a source form of the current type in which code assist is invoked.
	 *
	 *  @param typeName char[]
	 *      a type name which is to be resolved in the context of a compilation unit.
	 *		NOTE: the type name is supposed to be correctly reduced (no whitespaces, no unicodes left)
	 * 
	 * @param topLevelTypes SourceTypeElementInfo[]
	 *      a source form of the top level types of the compilation unit in which code assist is invoked.

	 *  @param searchInEnvironment
	 * 	if <code>true</code> and no selection could be found in context then search type in environment.
	 */
	public void selectType(ISourceType sourceType, char[] typeName, SourceTypeElementInfo[] topLevelTypes, boolean searchInEnvironment) {
		try {
			this.acceptedAnswer = false;

			// find the outer most type
			ISourceType outerType = sourceType;
			ISourceType parent = sourceType.getEnclosingType();
			while (parent != null) {
				outerType = parent;
				parent = parent.getEnclosingType();
			}
			// compute parse tree for this most outer type
			CompilationResult result = new CompilationResult(outerType.getFileName(), 1, 1, this.compilerOptions.maxProblemsPerUnit);
			if (!(sourceType instanceof SourceTypeElementInfo)) return;
			SourceType typeHandle = (SourceType) ((SourceTypeElementInfo)sourceType).getHandle();
			CompilationUnitDeclaration parsedUnit =
				ElementInfoConverter.buildCompilationUnit(
						topLevelTypes,
						typeHandle.isAnonymous() || typeHandle.isLocal(),
						this.parser.problemReporter(), 
						result);

			if (parsedUnit != null && parsedUnit.types != null) {
				if(DEBUG) {
					System.out.println("SELECTION - Diet AST :"); //$NON-NLS-1$
					System.out.println(parsedUnit.toString());
				}
				// find the type declaration that corresponds to the original source type
				TypeDeclaration typeDecl = new ASTNodeFinder(parsedUnit).findType(typeHandle);

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
						char[][] previousIdentifiers = CharOperation.splitOn('.', typeName, 0, dot);
						char[] selectionIdentifier =
							CharOperation.subarray(typeName, dot + 1, typeName.length);
						this.selectedIdentifier = selectionIdentifier;
						field.type =
							new SelectionOnQualifiedTypeReference(
								previousIdentifiers,
								selectionIdentifier,
								new long[previousIdentifiers.length + 1]);
					}
					field.name = "<fakeField>".toCharArray(); //$NON-NLS-1$
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
								if(DEBUG) {
									System.out.println("SELECTION - Selection binding :"); //$NON-NLS-1$
									System.out.println(e.binding.toString());
								}
								// if null then we found a problem in the selection node
								selectFrom(e.binding, parsedUnit, e.isDeclaration);
							}
						}
					}
				}
			}
			// only reaches here if no selection could be derived from the parsed tree
			// thus use the selected source and perform a textual type search
			if (!acceptedAnswer && searchInEnvironment) {
				if (this.selectedIdentifier != null) {
					nameEnvironment.findTypes(typeName, this);
					
					// accept qualified types only if no unqualified type was accepted
					if(!acceptedAnswer) {
						acceptQualifiedTypes();
					}
				}
			}
			if(noProposal && problem != null) {
				requestor.acceptError(problem);
			}
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
		} finally {
			reset();
		}
	}

	// Check if a declaration got selected in this unit
	private void selectDeclaration(CompilationUnitDeclaration compilationUnit){

		// the selected identifier is not identical to the parser one (equals but not identical),
		// for traversing the parse tree, the parser assist identifier is necessary for identitiy checks
		char[] assistIdentifier = this.getParser().assistIdentifier();
		if (assistIdentifier == null) return;
		
		// iterate over the types
		TypeDeclaration[] types = compilationUnit.types;
		for (int i = 0, length = types == null ? 0 : types.length; i < length; i++){
			selectDeclaration(types[i], assistIdentifier);
		}
	}

	// Check if a declaration got selected in this type
	private void selectDeclaration(TypeDeclaration typeDeclaration, char[] assistIdentifier){
	
		if (typeDeclaration.name == assistIdentifier){
			throw new SelectionNodeFound(typeDeclaration.binding, true);
		}
		TypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
		for (int i = 0, length = memberTypes == null ? 0 : memberTypes.length; i < length; i++){
			selectDeclaration(memberTypes[i], assistIdentifier);
		}
		FieldDeclaration[] fields = typeDeclaration.fields;
		for (int i = 0, length = fields == null ? 0 : fields.length; i < length; i++){
			if (fields[i].name == assistIdentifier){
				throw new SelectionNodeFound(fields[i].binding, true);
			}
		}
		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		for (int i = 0, length = methods == null ? 0 : methods.length; i < length; i++){
			AbstractMethodDeclaration method = methods[i];
			if (method.selector == assistIdentifier){
				if(method.binding != null) {
					throw new SelectionNodeFound(method.binding, true);
				} else {
					if(method.scope != null) {
						throw new SelectionNodeFound(new MethodBinding(method.modifiers, method.selector, null, null, null, method.scope.referenceType().binding), true);
					}
				}
			}
		}
	}
}
