package org.eclipse.jdt.internal.codeassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.codeassist.impl.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.complete.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.impl.*;

/**
 * This class is the entry point for source completions.
 * It contains two public APIs used to call CodeAssist on a given source with
 * a given environment, assisting position and storage (and possibly options).
 */
public final class CompletionEngine
	extends Engine
	implements ISearchRequestor, TypeConstants , TerminalSymbols {
		
	AssistOptions options;
	CompletionParser parser;
	ISearchableNameEnvironment nameEnvironment;
	ICompletionRequestor requestor;
	CompilationUnitScope unitScope;
	char[] source;
	boolean resolvingImports = false;
	boolean insideQualifiedReference = false;
	int startPosition, actualCompletionPosition, endPosition;
	HashtableOfObject knownPkgs = new HashtableOfObject(10);
	HashtableOfObject knownTypes = new HashtableOfObject(10);
	Scanner nameScanner;

	/*
		static final char[][] mainDeclarations =
			new char[][] {
				"package".toCharArray(),
				"import".toCharArray(),
				"abstract".toCharArray(),
				"final".toCharArray(),
				"public".toCharArray(),
				"class".toCharArray(),
				"interface".toCharArray()};
	
		static final char[][] modifiers = // may want field, method, type & member type modifiers
			new char[][] {
				"abstract".toCharArray(),
				"final".toCharArray(),
				"native".toCharArray(),
				"public".toCharArray(),
				"protected".toCharArray(),
				"private".toCharArray(),
				"static".toCharArray(),
				"strictfp".toCharArray(),
				"synchronized".toCharArray(),
				"transient".toCharArray(),
				"volatile".toCharArray()};
	*/
	static final char[][] baseTypes = new char[][] { 
		"boolean".toCharArray(), //$NON-NLS-1$
		"byte".toCharArray(), //$NON-NLS-1$
		"char".toCharArray(), //$NON-NLS-1$
		"double".toCharArray(), //$NON-NLS-1$
		"float".toCharArray(), //$NON-NLS-1$
		"int".toCharArray(), //$NON-NLS-1$
		"long".toCharArray(), //$NON-NLS-1$
		"short".toCharArray(), //$NON-NLS-1$
		"void".toCharArray(), //$NON-NLS-1$
	};
		
	static final char[] classField = "class".toCharArray();  //$NON-NLS-1$
	static final char[] lengthField = "length".toCharArray();  //$NON-NLS-1$
	static final char[] THIS = "this".toCharArray();  //$NON-NLS-1$
	static final char[] THROWS = "throws".toCharArray();  //$NON-NLS-1$
	
	static InvocationSite FakeInvocationSite = new InvocationSite(){
		public boolean isSuperAccess(){ return false; }
		public boolean isTypeAccess(){ return false; }
		public void setActualReceiverType(ReferenceBinding receiverType) {}
		public void setDepth(int depth){}
		public void setFieldIndex(int depth){}
	};

	/**
	 * The CompletionEngine is responsible for computing source completions.
	 *
	 * It requires a searchable name environment, which supports some
	 * specific search APIs, and a requestor to feed back the results to a UI.
	 *
	 *  @param environment com.ibm.codeassist.java.api.ISearchableNameEnvironment
	 *      used to resolve type/package references and search for types/packages
	 *      based on partial names.
	 *
	 *  @param requestor com.ibm.codeassist.java.api.ICompletionRequestor
	 *      since the engine might produce answers of various forms, the engine 
	 *      is associated with a requestor able to accept all possible completions.
	 *
	 *  @param options com.ibm.compiler.java.api.ConfigurableOptions
	 *		set of options used to configure the code assist engine.
	 */
	public CompletionEngine(
		ISearchableNameEnvironment nameEnvironment,
		ICompletionRequestor requestor,
		Map settings) {

		this.requestor = requestor;
		this.nameEnvironment = nameEnvironment;

		options = new AssistOptions(settings);
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		ProblemReporter problemReporter =
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				compilerOptions,
				new DefaultProblemFactory(Locale.getDefault())) {
			public void record(IProblem problem, CompilationResult unitResult) {
				if (problem.getID() != ProblemIrritants.UnmatchedBracket) {
					unitResult.record(problem);
					
					if (true) return; // work-around PR 1GD9RLP: ITPJCORE:WIN2000 - Code assist is slow
					if (problem.isWarning()) return;
					try {
						IMarker marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IJavaModelMarker.TRANSIENT_PROBLEM);
						marker.setAttribute(IJavaModelMarker.ID, problem.getID());
						marker.setAttribute(IMarker.CHAR_START, problem.getSourceStart());
						marker.setAttribute(IMarker.CHAR_END, problem.getSourceEnd() + 1);
						marker.setAttribute(IMarker.LINE_NUMBER, problem.getSourceLineNumber());
						//marker.setAttribute(IMarker.LOCATION, "#" + error.getSourceLineNumber());
						marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				
						CompletionEngine.this.requestor.acceptError(marker);
				
					} catch(CoreException e){
					}
				}
			}
		};
		this.parser =
			new CompletionParser(problemReporter, compilerOptions.assertMode);
		this.lookupEnvironment =
			new LookupEnvironment(this, compilerOptions, problemReporter, nameEnvironment);
		this.nameScanner =
			new Scanner(false, false, false, compilerOptions.assertMode);
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

		char[] completionName = CharOperation.concat(packageName, className, '.');
		
		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);
		
		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, new char[] { ';' });
		} else
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, className)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != NoCharChar)
							return; // ignore types from the default package from outside it
				} else {
					completionName = className;
				}
			}

		requestor.acceptClass(
			packageName,
			className,
			completionName,
			modifiers,
			startPosition,
			endPosition);
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

		char[] completionName = CharOperation.concat(packageName, interfaceName, '.');

		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);

		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, new char[] { ';' });
		} else
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, interfaceName)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != NoCharChar)
							return; // ignore types from the default package from outside it
				} else {
					completionName = interfaceName;
				}
			}

		requestor.acceptInterface(
			packageName,
			interfaceName,
			completionName,
			modifiers,
			startPosition,
			endPosition);
	}

	/**
	 * One result of the search consists of a new package.
	 *
	 * NOTE - All package names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    The default package is represented by an empty array.
	 */
	public void acceptPackage(char[] packageName) {

		if (this.knownPkgs.containsKey(packageName)) return;

		this.knownPkgs.put(packageName, this);

		requestor.acceptPackage(
			packageName,
			resolvingImports
				? CharOperation.concat(packageName, new char[] { '.', '*', ';' })
				: packageName,
			startPosition,
			endPosition);
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

		char[] completionName = CharOperation.concat(packageName, typeName, '.');
		
		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);

		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, new char[] { ';' });
		} else
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, typeName)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != NoCharChar)
							return; // ignore types from the default package from outside it
				} else {
					completionName = typeName;
				}
			}

		requestor.acceptType(
			packageName,
			typeName,
			completionName,
			startPosition,
			endPosition);
	}

	private void complete(AstNode astNode, Binding qualifiedBinding, Scope scope) {

		setSourceRange(astNode.sourceStart, astNode.sourceEnd);

		// defaults... some nodes will change these
		if (astNode instanceof CompletionOnFieldType) {

			CompletionOnFieldType field = (CompletionOnFieldType) astNode;
			CompletionOnSingleTypeReference type = (CompletionOnSingleTypeReference) field.type;
			char[] token = type.token;
			setSourceRange(type.sourceStart, type.sourceEnd);
			//		findKeywords(token, modifiers, scope); // could be the start of a field, method or member type
			findTypesAndPackages(token, scope);
			
			if(!field.isLocalVariable && field.modifiers == CompilerModifiers.AccDefault) {
				findMethods(token,null,scope.enclosingSourceType(),scope,new ObjectVector(),false,false,true,null,null,false);
			}
		} else {

			if (astNode instanceof CompletionOnSingleNameReference) {

				char[] token = ((CompletionOnSingleNameReference) astNode).token;
				findVariablesAndMethods(
					token,
					scope,
					(CompletionOnSingleNameReference) astNode,
					scope);
				// can be the start of a qualified type name
				findTypesAndPackages(token, scope);

			} else {

				if (astNode instanceof CompletionOnSingleTypeReference) {

					char[] token = ((CompletionOnSingleTypeReference) astNode).token;

					// can be the start of a qualified type name
					if (qualifiedBinding == null) {
						findTypesAndPackages(token, scope);
						} else {
							findMemberTypes(
							token,
							(ReferenceBinding) qualifiedBinding,
							scope,
							scope.enclosingSourceType());
					}
				} else {
					
					if (astNode instanceof CompletionOnQualifiedNameReference) {

						insideQualifiedReference = true;
						CompletionOnQualifiedNameReference ref =
							(CompletionOnQualifiedNameReference) astNode;
						char[] token = ref.completionIdentifier;
						long completionPosition = ref.sourcePositions[ref.sourcePositions.length - 1];

						if (qualifiedBinding instanceof VariableBinding) {

							setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
							TypeBinding receiverType = ((VariableBinding) qualifiedBinding).type;
							if (receiverType != null) {
								findFieldsAndMethods(token, receiverType, scope, ref, scope,false);
							}

						} else {

							if (qualifiedBinding instanceof ReferenceBinding) {

								ReferenceBinding receiverType = (ReferenceBinding) qualifiedBinding;
								setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);

								findMemberTypes(token, receiverType, scope, scope.enclosingSourceType());

								findClassField(token, (TypeBinding) qualifiedBinding);

								findFields(
									token,
									receiverType,
									scope,
									new ObjectVector(),
									new ObjectVector(),
									true,
									ref,
									scope,
									false);

								findMethods(
									token,
									null,
									receiverType,
									scope,
									new ObjectVector(),
									true,
									false,
									false,
									ref,
									scope,
									false);

							} else {

								if (qualifiedBinding instanceof PackageBinding) {

									setSourceRange(astNode.sourceStart, (int) completionPosition);
									// replace to the end of the completion identifier
									findTypesAndSubpackages(token, (PackageBinding) qualifiedBinding);
								}
							}
						}

					} else {

							if (astNode instanceof CompletionOnQualifiedTypeReference) {

							insideQualifiedReference = true;
							CompletionOnQualifiedTypeReference ref =
								(CompletionOnQualifiedTypeReference) astNode;
							char[] token = ref.completionIdentifier;
							long completionPosition = ref.sourcePositions[ref.tokens.length];

							// get the source positions of the completion identifier
							if (qualifiedBinding instanceof ReferenceBinding) {

								setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
								findMemberTypes(
									token,
									(ReferenceBinding) qualifiedBinding,
									scope,
									scope.enclosingSourceType());

							} else {

								if (qualifiedBinding instanceof PackageBinding) {

									setSourceRange(astNode.sourceStart, (int) completionPosition);
									// replace to the end of the completion identifier
									findTypesAndSubpackages(token, (PackageBinding) qualifiedBinding);
								}
							}

						} else {

							if (astNode instanceof CompletionOnMemberAccess) {

								CompletionOnMemberAccess access = (CompletionOnMemberAccess) astNode;
								long completionPosition = access.nameSourcePosition;
								setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);

								findFieldsAndMethods(
									access.token,
									(TypeBinding) qualifiedBinding,
									scope,
									access,
									scope,
									false);

							} else {

								if (astNode instanceof CompletionOnMessageSend) {

									CompletionOnMessageSend messageSend = (CompletionOnMessageSend) astNode;
									TypeBinding[] argTypes =
										computeTypes(messageSend.arguments, (BlockScope) scope);
									if (qualifiedBinding == null) {

										findImplicitMessageSends(messageSend.selector, argTypes, scope, messageSend, scope);
									} else {

										findMethods(
											messageSend.selector,
											argTypes,
											(ReferenceBinding) qualifiedBinding,
											scope,
											new ObjectVector(),
											false,
											true,
											false,
											messageSend,
											scope,
											false);
									}

								} else {

									if (astNode instanceof CompletionOnExplicitConstructorCall) {

										CompletionOnExplicitConstructorCall constructorCall =
											(CompletionOnExplicitConstructorCall) astNode;
										TypeBinding[] argTypes =
											computeTypes(constructorCall.arguments, (BlockScope) scope);
										findConstructors(
											(ReferenceBinding) qualifiedBinding,
											argTypes,
											scope,
											constructorCall,
											false);

									} else {

										if (astNode instanceof CompletionOnQualifiedAllocationExpression) {

											CompletionOnQualifiedAllocationExpression allocExpression =
												(CompletionOnQualifiedAllocationExpression) astNode;
											TypeBinding[] argTypes =
												computeTypes(allocExpression.arguments, (BlockScope) scope);
											
											ReferenceBinding ref = (ReferenceBinding) qualifiedBinding;
											if(ref.isClass()) {
												if(!ref.isAbstract()) {
													findConstructors(
														ref,
														argTypes,
														scope,
														allocExpression,
														false);
												}
											}
											if(!ref.isFinal()){
												findAnonymousType(
													ref,
													argTypes,
													scope,
													allocExpression);
											}

										} else {

											if (astNode instanceof CompletionOnClassLiteralAccess) {
												CompletionOnClassLiteralAccess access = (CompletionOnClassLiteralAccess) astNode;
												setSourceRange(access.classStart, access.sourceEnd);
								
												findClassField(access.completionIdentifier, (TypeBinding) qualifiedBinding);
											} else {
												if(astNode instanceof CompletionOnMethodName) {
													CompletionOnMethodName method = (CompletionOnMethodName) astNode;
														
													setSourceRange(method.sourceStart, method.selectorEnd);
														
													FieldBinding[] fields = scope.enclosingSourceType().fields();
													char[][] excludeNames = new char[fields.length][];
													for(int i = 0 ; i < fields.length ; i++){
														excludeNames[i] = fields[i].name;
													}
														
													findVariableNames(method.selector, method.returnType, excludeNames);
												} else {
													if (astNode instanceof CompletionOnFieldName) {
														CompletionOnFieldName field = (CompletionOnFieldName) astNode;
														
														FieldBinding[] fields = scope.enclosingSourceType().fields();
														char[][] excludeNames = new char[fields.length][];
														for(int i = 0 ; i < fields.length ; i++){
															excludeNames[i] = fields[i].name;
														}
														
														findVariableNames(field.realName, field.type, excludeNames);
													} else {
														if (astNode instanceof CompletionOnLocalName ||
															astNode instanceof CompletionOnArgumentName){
															LocalDeclaration variable = (LocalDeclaration) astNode;
															
															LocalVariableBinding[] locals = ((BlockScope)scope).locals;
															char[][] excludeNames = new char[locals.length][];
															int localCount = 0;
															for(int i = 0 ; i < locals.length ; i++){
																if(locals[i] != null) {
																	excludeNames[localCount++] = locals[i].name;
																}
															}
															System.arraycopy(excludeNames, 0, excludeNames = new char[localCount][], 0, localCount);
															
															char[] name;
															if(variable instanceof CompletionOnLocalName){
																name = ((CompletionOnLocalName) variable).realName;
															} else {
																name = ((CompletionOnArgumentName) variable).realName;
															}
															findVariableNames(name, variable.type, excludeNames);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Ask the engine to compute a completion at the specified position
	 * of the given compilation unit.
	 *
	 *  @return void
	 *      completion results are answered through a requestor.
	 *
	 *  @param unit com.ibm.compiler.java.api.env.ICompilationUnit
	 *      the source of the current compilation unit.
	 *
	 *  @param completionPosition int
	 *      a position in the source where the completion is taking place. 
	 *      This position is relative to the source provided.
	 */
	public void complete(ICompilationUnit sourceUnit, int completionPosition) {

		try {
			actualCompletionPosition = completionPosition - 1;
			// for now until we can change the UI.
			CompilationResult result = new CompilationResult(sourceUnit, 1, 1);
			CompilationUnitDeclaration parsedUnit = parser.dietParse(sourceUnit, result, actualCompletionPosition);

			//		boolean completionNodeFound = false;
			if (parsedUnit != null) {

				// scan the package & import statements first
				if (parsedUnit.currentPackage instanceof CompletionOnPackageReference) {
					findPackages((CompletionOnPackageReference) parsedUnit.currentPackage);
					return;
				}

				ImportReference[] imports = parsedUnit.imports;
				if (imports != null) {
					for (int i = 0, length = imports.length; i < length; i++) {
						ImportReference importReference = imports[i];
						if (importReference instanceof CompletionOnImportReference) {
							findImports((CompletionOnImportReference) importReference);
							return;
						}
					}
				}

				if (parsedUnit.types != null) {
					try {
						lookupEnvironment.buildTypeBindings(parsedUnit);

						if ((unitScope = parsedUnit.scope) != null) {
							source = sourceUnit.getContents();
							lookupEnvironment.completeTypeBindings(parsedUnit, true);
							parsedUnit.scope.faultInTypes();
							parseMethod(parsedUnit, actualCompletionPosition);
							parsedUnit.resolve();
						}
					} catch (CompletionNodeFound e) {
						//					completionNodeFound = true;
						if (e.astNode != null)
							// if null then we found a problem in the completion node
							complete(e.astNode, e.qualifiedBinding, e.scope);
					}
				}
			}

			/* Ignore package, import, class & interface keywords for now...
					if (!completionNodeFound) {
						if (parsedUnit == null || parsedUnit.types == null) {
							// this is not good enough... can still be trying to define a second type
							CompletionScanner scanner = (CompletionScanner) parser.scanner;
							setSourceRange(scanner.completedIdentifierStart, scanner.completedIdentifierEnd);
							findKeywords(scanner.completionIdentifier, mainDeclarations, null);
						}
						// currently have no way to know if extends/implements are possible keywords
					}
			*/
		} catch (IndexOutOfBoundsException e) { // work-around internal failure - 1GEMF6D
		} catch (InvalidCursorLocation e) { // may eventually report a usefull error
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
		} catch (CompletionNodeFound e){ // internal failure - bugs 5618
		} finally {
			reset();
		}
	}

	private TypeBinding[] computeTypes(Expression[] arguments, BlockScope scope) {

		if (arguments == null)
			return null;

		int argsLength = arguments.length;
		TypeBinding[] argTypes = new TypeBinding[argsLength];
		for (int a = argsLength; --a >= 0;)
			argTypes[a] = arguments[a].resolveType(scope);
		return argTypes;
	}
	
	private void findAnonymousType(
		ReferenceBinding currentType,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite) {

		if (currentType.isInterface()) {
			char[] completion = TypeConstants.NoChar;
			// nothing to insert - do not want to replace the existing selector & arguments
			if (source == null
				|| source.length <= endPosition
				|| source[endPosition] != ')')
				completion = new char[] { ')' };
			
			requestor.acceptAnonymousType(
				currentType.qualifiedPackageName(),
				currentType.qualifiedSourceName(),
				TypeConstants.NoCharChar,
				TypeConstants.NoCharChar,
				TypeConstants.NoCharChar,
				completion,
				IConstants.AccPublic,
				endPosition,
				endPosition);
		} else {
			findConstructors(
				currentType,
				argTypes,
				scope,
				invocationSite,
				true);
		}
	}

	private void findClassField(char[] token, TypeBinding receiverType) {

		if (token == null)
			return;

		if (token.length <= classField.length
			&& CharOperation.prefixEquals(token, classField, false /* ignore case */
			))
			requestor.acceptField(
				NoChar,
				NoChar,
				classField,
				NoChar,
				NoChar,
				classField,
				CompilerModifiers.AccStatic | CompilerModifiers.AccPublic,
				startPosition,
				endPosition);
	}

	private void findConstructors(
		ReferenceBinding currentType,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite,
		boolean forAnonymousType) {

		// No visibility checks can be performed without the scope & invocationSite
		MethodBinding[] methods = currentType.availableMethods();
		int minArgLength = argTypes == null ? 0 : argTypes.length;
		next : for (int f = methods.length; --f >= 0;) {
			MethodBinding constructor = methods[f];
			if (constructor.isConstructor()) {
				
				if (constructor.isSynthetic()) continue next;
					
				if (options.checkVisibility
					&& !constructor.canBeSeenBy(invocationSite, scope)) continue next;

				TypeBinding[] parameters = constructor.parameters;
				int paramLength = parameters.length;
				if (minArgLength > paramLength)
					continue next;
				for (int a = minArgLength; --a >= 0;)
					if (argTypes[a] != null) // can be null if it could not be resolved properly
						if (!scope.areTypesCompatible(argTypes[a], constructor.parameters[a]))
							continue next;

				char[][] parameterPackageNames = new char[paramLength][];
				char[][] parameterTypeNames = new char[paramLength][];
				for (int i = 0; i < paramLength; i++) {
					TypeBinding type = parameters[i];
					parameterPackageNames[i] = type.qualifiedPackageName();
					parameterTypeNames[i] = type.qualifiedSourceName();
				}
				char[][] parameterNames = findMethodParameterNames(constructor,parameterTypeNames);
				
				char[] completion = TypeConstants.NoChar;
				// nothing to insert - do not want to replace the existing selector & arguments
				if (source == null
					|| source.length <= endPosition
					|| source[endPosition] != ')')
					completion = new char[] { ')' };
					
				if(forAnonymousType){
					requestor.acceptAnonymousType(
						currentType.qualifiedPackageName(),
						currentType.qualifiedSourceName(),
						parameterPackageNames,
						parameterTypeNames,
						parameterNames,
						completion,
						constructor.modifiers,
						endPosition,
						endPosition);
				} else {
					requestor.acceptMethod(
						currentType.qualifiedPackageName(),
						currentType.qualifiedSourceName(),
						currentType.sourceName(),
						parameterPackageNames,
						parameterTypeNames,
						parameterNames,
						TypeConstants.NoChar,
						TypeConstants.NoChar,
						completion,
						constructor.modifiers,
						endPosition,
						endPosition);
				}
			}
		}
	}
	
	// Helper method for findFields(char[], ReferenceBinding, Scope, ObjectVector, boolean)
	private void findFields(
		char[] fieldName,
		FieldBinding[] fields,
		Scope scope,
		ObjectVector fieldsFound,
		ObjectVector localsFound,
		boolean onlyStaticFields,
		ReferenceBinding receiverType,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		// Inherited fields which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		
		int fieldLength = fieldName.length;
		next : for (int f = fields.length; --f >= 0;) {			
			FieldBinding field = fields[f];

			if (field.isSynthetic())	continue next;

			if (onlyStaticFields && !field.isStatic()) continue next;

			if (fieldLength > field.name.length) continue next;

			if (!CharOperation.prefixEquals(fieldName, field.name, false /* ignore case */))	continue next;

			if (options.checkVisibility
				&& !field.canBeSeenBy(receiverType, invocationSite, scope))	continue next;

			boolean prefixRequired = false;

			for (int i = fieldsFound.size; --i >= 0;) {
				FieldBinding otherField = (FieldBinding) fieldsFound.elementAt(i);
				if (field == otherField)
					continue next;
				if (CharOperation.equals(field.name, otherField.name, true)) {
					if (field.declaringClass.isSuperclassOf(otherField.declaringClass))
						continue next;
					if (otherField.declaringClass.isInterface())
						if (field.declaringClass.implementsInterface(otherField.declaringClass, true))
							continue next;
					prefixRequired = true;
				}
			}

			for (int l = localsFound.size; --l >= 0;) {
				LocalVariableBinding local = (LocalVariableBinding) localsFound.elementAt(l);	

				if (CharOperation.equals(field.name, local.name, true)) {
					SourceTypeBinding declarationType = scope.enclosingSourceType();
					if (declarationType.isAnonymousType() && declarationType != invocationScope.enclosingSourceType()) {
						continue next;
					}
					prefixRequired = true;
					break;
				}
			}
			
			fieldsFound.add(field);
			
			char[] completion = field.name;
			
			if(prefixRequired || options.forceImplicitQualification){
				char[] prefix = computePrefix(scope.enclosingSourceType(), invocationScope.enclosingSourceType(), field.isStatic());
				completion = CharOperation.concat(prefix,completion,'.');
			}

			requestor
				.acceptField(
					field.declaringClass.qualifiedPackageName(),
					field.declaringClass.qualifiedSourceName(),
					field.name,
					field.type.qualifiedPackageName(),
					field.type.qualifiedSourceName(),
					completion,
			// may include some qualification to resolve ambiguities
			field.modifiers, startPosition, endPosition);
		}
	}

	private void findFields(
		char[] fieldName,
		ReferenceBinding receiverType,
		Scope scope,
		ObjectVector fieldsFound,
		ObjectVector localsFound,
		boolean onlyStaticFields,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		if (fieldName == null)
			return;

		ReferenceBinding currentType = receiverType;
		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;
		do {

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {

				if (interfacesToVisit == null)
					interfacesToVisit = new ReferenceBinding[5][];

				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(
						interfacesToVisit,
						0,
						interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
						0,
						lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}

			findFields(
				fieldName,
				currentType.availableFields(),
				scope,
				fieldsFound,
				localsFound,
				onlyStaticFields,
				receiverType,
				invocationSite,
				invocationScope,
				implicitCall);
			currentType = currentType.superclass();
		} while (currentType != null);

		if (interfacesToVisit != null) {
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {

					ReferenceBinding anInterface = interfaces[j];
					if ((anInterface.tagBits & TagBits.InterfaceVisited) == 0) {
						// if interface as not already been visited
						anInterface.tagBits |= TagBits.InterfaceVisited;

						findFields(
							fieldName,
							anInterface.availableFields(),
							scope,
							fieldsFound,
							localsFound,
							onlyStaticFields,
							receiverType,
							invocationSite,
							invocationScope,
							implicitCall);

						ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {
							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++)
					interfaces[j].tagBits &= ~TagBits.InterfaceVisited;
			}
		}
	}

	private void findFieldsAndMethods(
		char[] token,
		TypeBinding receiverType,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		if (token == null)
			return;

		if (receiverType.isBaseType())
			return; // nothing else is possible with base types

		if (receiverType.isArrayType()) {
			if (token.length <= lengthField.length
				&& CharOperation.prefixEquals(token, lengthField, false /* ignore case */
				))

				requestor.acceptField(
					NoChar,
					NoChar,
					lengthField,
					NoChar,
					NoChar,
					lengthField,
					CompilerModifiers.AccPublic,
					startPosition,
					endPosition);

			receiverType = scope.getJavaLangObject();
		}

		findFields(
			token,
			(ReferenceBinding) receiverType,
			scope,
			new ObjectVector(),
			new ObjectVector(),
			false,
			invocationSite,
			invocationScope,
			implicitCall);

		findMethods(
			token,
			null,
			(ReferenceBinding) receiverType,
			scope,
			new ObjectVector(),
			false,
			false,
			false,
			invocationSite,
			invocationScope,
			implicitCall);
	}

	private void findImports(CompletionOnImportReference importReference) {
		char[][] tokens = importReference.tokens;
			
		char[] importName = CharOperation.concatWith(tokens, '.');
		
		if (importName.length == 0)
			return;
			
		char[] token = tokens[tokens.length - 1];
		if(token != null && token.length == 0)
			importName = CharOperation.concat(importName, new char[]{'.'});

		resolvingImports = true;
		setSourceRange(
			importReference.sourceStart,
			importReference.declarationSourceEnd);
		// want to replace the existing .*;
		nameEnvironment.findPackages(importName, this);
		nameEnvironment.findTypes(importName, this);
	}

	// what about onDemand types? Ignore them since it does not happen!
	// import p1.p2.A.*;
	private void findKeywords(char[] keyword, char[][] choices, Scope scope) {

		int length = keyword.length;
		if (length > 0)
			for (int i = 0; i < choices.length; i++)
				if (length <= choices[i].length
					&& CharOperation.prefixEquals(keyword, choices[i], false /* ignore case */
					))
					requestor.acceptKeyword(choices[i], startPosition, endPosition);
	}

	// Helper method for findMemberTypes(char[], ReferenceBinding, Scope)
	private void findMemberTypes(
		char[] typeName,
		ReferenceBinding[] memberTypes,
		ObjectVector typesFound,
		ReferenceBinding receiverType,
		SourceTypeBinding invocationType) {

		// Inherited member types which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		int typeLength = typeName.length;
		next : for (int m = memberTypes.length; --m >= 0;) {
			ReferenceBinding memberType = memberTypes[m];
			//		if (!wantClasses && memberType.isClass()) continue next;
			//		if (!wantInterfaces && memberType.isInterface()) continue next;
			if (typeLength > memberType.sourceName.length)
				continue next;

			if (!CharOperation.prefixEquals(typeName, memberType.sourceName, false
				/* ignore case */
				))
				continue next;

			if (options.checkVisibility
				&& !memberType.canBeSeenBy(receiverType, invocationType))
				continue next;

			for (int i = typesFound.size; --i >= 0;) {
				ReferenceBinding otherType = (ReferenceBinding) typesFound.elementAt(i);

				if (memberType == otherType)
					continue next;

				if (CharOperation.equals(memberType.sourceName, otherType.sourceName, true)) {

					if (memberType.enclosingType().isSuperclassOf(otherType.enclosingType()))
						continue next;

					if (otherType.enclosingType().isInterface())

						if (memberType.enclosingType()
							.implementsInterface(otherType.enclosingType(), true))
							continue next;
				}
			}

			typesFound.add(memberType);

			if (memberType.isClass()) {
				requestor.acceptClass(
					memberType.qualifiedPackageName(),
					memberType.qualifiedSourceName(),
					memberType.sourceName(),
					memberType.modifiers,
					startPosition,
					endPosition);

			} else {

				requestor.acceptInterface(
					memberType.qualifiedPackageName(),
					memberType.qualifiedSourceName(),
					memberType.sourceName(),
					memberType.modifiers,
					startPosition,
					endPosition);
			}
		}
	}

	private void findMemberTypes(
		char[] typeName,
		ReferenceBinding receiverType,
		Scope scope,
		SourceTypeBinding typeInvocation) {

		ReferenceBinding currentType = receiverType;
		if (typeName == null)
			return;

		if (currentType.superInterfaces() == null)
			return; // we're trying to find a supertype

		ObjectVector typesFound = new ObjectVector();
		if (insideQualifiedReference
			|| typeName.length == 0) { // do not search up the hierarchy

			findMemberTypes(
				typeName,
				currentType.memberTypes(),
				typesFound,
				receiverType,
				typeInvocation);
			return;
		}

		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;

		do {

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {

				if (interfacesToVisit == null)
					interfacesToVisit = new ReferenceBinding[5][];

				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(
						interfacesToVisit,
						0,
						interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
						0,
						lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}

			findMemberTypes(
				typeName,
				currentType.memberTypes(),
				typesFound,
				receiverType,
				typeInvocation);
			currentType = currentType.superclass();

		} while (currentType != null);

		if (interfacesToVisit != null) {
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {

					ReferenceBinding anInterface = interfaces[j];
					if ((anInterface.tagBits & TagBits.InterfaceVisited) == 0) {
						// if interface as not already been visited
						anInterface.tagBits |= TagBits.InterfaceVisited;

						findMemberTypes(
							typeName,
							anInterface.memberTypes(),
							typesFound,
							receiverType,
							typeInvocation);

						ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {

							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++)
					interfaces[j].tagBits &= ~TagBits.InterfaceVisited;
			}
		}
	}

	private void findIntefacesMethods(
		char[] selector,
		TypeBinding[] argTypes,
		ReferenceBinding receiverType,
		ReferenceBinding[] itsInterfaces,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		boolean isCompletingDeclaration,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		if (selector == null)
			return;

		if (itsInterfaces != NoSuperInterfaces) {
			ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
			int lastPosition = 0;
			interfacesToVisit[lastPosition] = itsInterfaces;
			
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];

				for (int j = 0, length = interfaces.length; j < length; j++) {
					ReferenceBinding currentType = interfaces[j];

					if ((currentType.tagBits & TagBits.InterfaceVisited) == 0) {
						// if interface as not already been visited
						currentType.tagBits |= TagBits.InterfaceVisited;

						if(isCompletingDeclaration){

							findLocalMethodDeclarations(
								selector,
								currentType.availableMethods(),
								scope,
								methodsFound,
								onlyStaticMethods,
								exactMatch,
								receiverType);

						} else {

							findLocalMethods(
								selector,
								argTypes,
								currentType.availableMethods(),
								scope,
								methodsFound,
								onlyStaticMethods,
								exactMatch,
								receiverType,
								invocationSite,
								invocationScope,
								implicitCall);
						}

						itsInterfaces = currentType.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {

							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];

				for (int j = 0, length = interfaces.length; j < length; j++){
					interfaces[j].tagBits &= ~TagBits.InterfaceVisited;
				}
			}
		}
	}
	
	private void findImplicitMessageSends(
		char[] token,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope) {

		if (token == null)
			return;

		boolean staticsOnly = false;
		// need to know if we're in a static context (or inside a constructor)
		ObjectVector methodsFound = new ObjectVector();

		done : while (true) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (scope.kind) {

				case Scope.METHOD_SCOPE :
					// handle the error case inside an explicit constructor call (see MethodScope>>findField)
					MethodScope methodScope = (MethodScope) scope;
					staticsOnly |= methodScope.isStatic | methodScope.isConstructorCall;
					break;

				case Scope.CLASS_SCOPE :
					ClassScope classScope = (ClassScope) scope;
					SourceTypeBinding enclosingType = classScope.referenceContext.binding;
					findMethods(
						token,
						argTypes,
						enclosingType,
						classScope,
						methodsFound,
						staticsOnly,
						true,
						false,
						invocationSite,
						invocationScope,
						true);
					staticsOnly |= enclosingType.isStatic();
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					break done;
			}
			scope = scope.parent;
		}
	}

	// Helper method for findMethods(char[], TypeBinding[], ReferenceBinding, Scope, ObjectVector, boolean, boolean, boolean)
	private void findLocalMethods(
		char[] methodName,
		TypeBinding[] argTypes,
		MethodBinding[] methods,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		ReferenceBinding receiverType,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		// Inherited methods which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite

		int methodLength = methodName.length;
		int minArgLength = argTypes == null ? 0 : argTypes.length;

		next : for (int f = methods.length; --f >= 0;) {
			MethodBinding method = methods[f];

			if (method.isSynthetic()) continue next;

			if (method.isDefaultAbstract())	continue next;

			if (method.isConstructor()) continue next;

			//		if (noVoidReturnType && method.returnType == BaseTypes.VoidBinding) continue next;
			if (onlyStaticMethods && !method.isStatic()) continue next;

			if (options.checkVisibility
				&& !method.canBeSeenBy(receiverType, invocationSite, scope)) continue next;

			if (exactMatch) {
				if (!CharOperation.equals(methodName, method.selector, false /* ignore case */
					))
					continue next;

			} else {

				if (methodLength > method.selector.length)
					continue next;

				if (!CharOperation.prefixEquals(methodName, method.selector, false
					/* ignore case */
					))
					continue next;
			}
			if (minArgLength > method.parameters.length)
				continue next;

			for (int a = minArgLength; --a >= 0;){
				if (argTypes[a] != null){ // can be null if it could not be resolved properly
					if (!scope.areTypesCompatible(argTypes[a], method.parameters[a])) {
						continue next;
					}
				}
			}
			
			boolean prefixRequired = false;
			
			for (int i = methodsFound.size; --i >= 0;) {

				MethodBinding otherMethod = (MethodBinding) methodsFound.elementAt(i);
				if (method == otherMethod)
					continue next;

				if (CharOperation.equals(method.selector, otherMethod.selector, true)
					&& method.areParametersEqual(otherMethod)) {

					if (method.declaringClass.isSuperclassOf(otherMethod.declaringClass))
						continue next;

					if (otherMethod.declaringClass.isInterface())
						if (method
							.declaringClass
							.implementsInterface(otherMethod.declaringClass, true))
							continue next;

					if (method.declaringClass.isInterface())
						if(otherMethod
							.declaringClass
							.implementsInterface(method.declaringClass,true))
							continue next;
					prefixRequired = true;
				}
			}

			methodsFound.add(method);
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];

			for (int i = 0; i < length; i++) {
				TypeBinding type = method.parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}
			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);

			char[] completion = TypeConstants.NoChar;
			
			int previousStartPosition = startPosition;
			
			// nothing to insert - do not want to replace the existing selector & arguments
			if (!exactMatch) {
				if (source != null
					&& source.length > endPosition
					&& source[endPosition] == '(')
					completion = method.selector;
				else
					completion = CharOperation.concat(method.selector, new char[] { '(', ')' });
			} else {
				if(prefixRequired && (source != null)) {
					completion = CharOperation.subarray(source, startPosition, endPosition);
				} else {
					startPosition = endPosition;
				}
			}
			
			if(prefixRequired || options.forceImplicitQualification){
				char[] prefix = computePrefix(scope.enclosingSourceType(), invocationScope.enclosingSourceType(), method.isStatic());
				completion = CharOperation.concat(prefix,completion,'.');
			}

			requestor.acceptMethod(
				method.declaringClass.qualifiedPackageName(),
				method.declaringClass.qualifiedSourceName(),
				method.selector,
				parameterPackageNames,
				parameterTypeNames,
				parameterNames,
				method.returnType.qualifiedPackageName(),
				method.returnType.qualifiedSourceName(),
				completion,
				method.modifiers,
				startPosition,
				endPosition);
			startPosition = previousStartPosition;
		}
	}

	// Helper method for findMethods(char[], MethodBinding[], Scope, ObjectVector, boolean, boolean, boolean, TypeBinding)
	private void findLocalMethodDeclarations(
		char[] methodName,
		MethodBinding[] methods,
		Scope scope,
		ObjectVector methodsFound,
		//	boolean noVoidReturnType, how do you know?
		boolean onlyStaticMethods,
		boolean exactMatch,
		ReferenceBinding receiverType) {

		// Inherited methods which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		int methodLength = methodName.length;
		next : for (int f = methods.length; --f >= 0;) {

			MethodBinding method = methods[f];
			if (method.isSynthetic())	continue next;
				
			if (method.isDefaultAbstract()) continue next;
			
			if (method.isConstructor()) continue next;
				
			if (method.isFinal()) continue next;

			//		if (noVoidReturnType && method.returnType == BaseTypes.VoidBinding) continue next;
			if (onlyStaticMethods && !method.isStatic()) continue next;

			if (options.checkVisibility
				&& !method.canBeSeenBy(receiverType, FakeInvocationSite , scope)) continue next;

			if (exactMatch) {
				if (!CharOperation.equals(methodName, method.selector, false /* ignore case */
					))
					continue next;

			} else {

				if (methodLength > method.selector.length)
					continue next;

				if (!CharOperation.prefixEquals(methodName, method.selector, false
					/* ignore case */
					))
					continue next;
			}

			MethodBinding[] existingMethods = receiverType.availableMethods();
			for(int i =0, length = existingMethods == null ? 0 : existingMethods.length; i < length ; i++){
				MethodBinding existingMethod = existingMethods[i];
				if (CharOperation.equals(method.selector, existingMethod.selector, true)
					&& method.areParametersEqual(existingMethod)){
					continue next;	
				}
			}

			for (int i = methodsFound.size; --i >= 0;) {

				MethodBinding otherMethod = (MethodBinding) methodsFound.elementAt(i);
				if (method == otherMethod)
					continue next;

				if (CharOperation.equals(method.selector, otherMethod.selector, true)
					&& method.areParametersEqual(otherMethod)) {

					if (method.declaringClass.isSuperclassOf(otherMethod.declaringClass))
						continue next;

					if (otherMethod.declaringClass.isInterface())
						if (method
							.declaringClass
							.implementsInterface(otherMethod.declaringClass, true))
							continue next;
							
					if (method.declaringClass.isInterface())
						if(otherMethod
							.declaringClass
							.implementsInterface(method.declaringClass,true))
							continue next;
				}
			}

			methodsFound.add(method);
			
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];
			
			for (int i = 0; i < length; i++) {
				TypeBinding type = method.parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}

			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);
			
			StringBuffer completion = new StringBuffer(10);
			// flush uninteresting modifiers
			int insertedModifiers = method.modifiers & ~(CompilerModifiers.AccNative | CompilerModifiers.AccAbstract);

			if (!exactMatch) {
				if(insertedModifiers != CompilerModifiers.AccDefault){
					completion.append(AstNode.modifiersString(insertedModifiers));
				}
				char[] returnPackageName = method.returnType.qualifiedPackageName();
				char[] returnTypeName = method.returnType.qualifiedSourceName();
				if(mustQualifyType(returnPackageName, returnTypeName)) {
					completion.append(CharOperation.concat(returnPackageName, returnTypeName,'.'));
				} else {
					completion.append(method.returnType.sourceName());
				}
				completion.append(' ');
				completion.append(method.selector);
				completion.append('(');

				for(int i = 0; i < length ; i++){
					if(mustQualifyType(parameterPackageNames[i], parameterTypeNames[i])){
						completion.append(CharOperation.concat(parameterPackageNames[i], parameterTypeNames[i], '.'));
					} else {
						completion.append(parameterTypeNames[i]);
					}
					completion.append(' ');
					if(parameterNames != null){
						completion.append(parameterNames[i]);
					} else {
						completion.append('%');
					}
					if(i != (length - 1))
						completion.append(',');	
				}
				completion.append(')');
				
				ReferenceBinding[] exceptions = method.thrownExceptions;
				
				if (exceptions != null && exceptions.length > 0){
					completion.append(' ');
					completion.append(THROWS);
					completion.append(' ');
					for(int i = 0; i < exceptions.length ; i++){
						ReferenceBinding exception = exceptions[i];

						char[] exceptionPackageName = exception.qualifiedPackageName();
						char[] exceptionTypeName = exception.qualifiedSourceName();
						
						if(i != 0){
							completion.append(',');
							completion.append(' ');
						}
						
						if(mustQualifyType(exceptionPackageName, exceptionTypeName)){
							completion.append(CharOperation.concat(exceptionPackageName, exceptionTypeName));
						} else {
							completion.append(exception.sourceName());
						}
					}
				}
			}

			requestor.acceptMethodDeclaration(
				method.declaringClass.qualifiedPackageName(),
				method.declaringClass.qualifiedSourceName(),
				method.selector,
				parameterPackageNames,
				parameterTypeNames,
				parameterNames,
				method.returnType.qualifiedPackageName(),
				method.returnType.qualifiedSourceName(),
				completion.toString().toCharArray(),
				method.modifiers,
				startPosition,
				endPosition);
		}
	}
	private void findMethods(
		char[] selector,
		TypeBinding[] argTypes,
		ReferenceBinding receiverType,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		boolean isCompletingDeclaration,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		if (selector == null)
			return;
			
		ReferenceBinding currentType = receiverType;
		if (receiverType.isInterface()) {
			if(isCompletingDeclaration) {
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					currentType.superInterfaces(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
			} else {
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					new ReferenceBinding[]{currentType},
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
			}
			
			currentType = scope.getJavaLangObject();
		} else {
			if(isCompletingDeclaration){
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					currentType.superInterfaces(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
				
				currentType = receiverType.superclass();
			}
		}
		boolean hasPotentialDefaultAbstractMethods = true;
		while (currentType != null) {

			if(isCompletingDeclaration){

				findLocalMethodDeclarations(
					selector,
					currentType.availableMethods(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					receiverType);
			} else{

				findLocalMethods(
					selector,
					argTypes,
					currentType.availableMethods(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					receiverType,
					invocationSite,
					invocationScope,
					implicitCall);
			}
			
			if(hasPotentialDefaultAbstractMethods && currentType.isAbstract()){
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					currentType.superInterfaces(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
			} else {
				hasPotentialDefaultAbstractMethods = false;
			}
			currentType = currentType.superclass();
		}
	}
	private char[][] findMethodParameterNames(MethodBinding method, char[][] parameterTypeNames){
		ReferenceBinding bindingType = method.declaringClass;

		char[][] parameterNames = null;
		
		int length = parameterTypeNames.length;

		if (length == 0){
			return TypeConstants.NoCharChar;
		}
		// look into the corresponding unit if it is available
		if (bindingType instanceof SourceTypeBinding){
			SourceTypeBinding sourceType = (SourceTypeBinding) bindingType;

			if (sourceType.scope != null){
				TypeDeclaration parsedType;

				if ((parsedType = sourceType.scope.referenceContext) != null){
					AbstractMethodDeclaration methodDecl = parsedType.declarationOf(method);

					if (methodDecl != null){
						Argument[] arguments = methodDecl.arguments;
						parameterNames = new char[length][];

						for(int i = 0 ; i < length ; i++){
							parameterNames[i] = arguments[i].name;
						}
					}
				}
			}
		}
		// look into the model		
		if(parameterNames == null){
			NameEnvironmentAnswer answer = nameEnvironment.findType(bindingType.compoundName);

			if(answer != null){
				if(answer.isSourceType()) {
					ISourceType sourceType = answer.getSourceTypes()[0];
					ISourceMethod[] sourceMethods = sourceType.getMethods();
					int len = sourceMethods == null ? 0 : sourceMethods.length;
					for(int i = 0; i < len ; i++){
						ISourceMethod sourceMethod = sourceMethods[i];
						char[][] argTypeNames = sourceMethod.getArgumentTypeNames();

						if(argTypeNames != null &&
							CharOperation.equals(method.selector,sourceMethod.getSelector()) &&
							CharOperation.equals(argTypeNames,parameterTypeNames)){
							parameterNames = sourceMethod.getArgumentNames();
							break;
						}
					}
				} 
			}
		}
		return parameterNames;
	}
	
	private void findNestedTypes(
		char[] typeName,
		SourceTypeBinding currentType,
		Scope scope) {
		if (typeName == null)
			return;

		int typeLength = typeName.length;

		while (scope != null) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (scope.kind) {

				case Scope.METHOD_SCOPE :
				case Scope.BLOCK_SCOPE :
					BlockScope blockScope = (BlockScope) scope;

					next : for (int i = 0, length = blockScope.scopeIndex; i < length; i++) {

						if (blockScope.subscopes[i] instanceof ClassScope) {
							SourceTypeBinding localType =
								((ClassScope) blockScope.subscopes[i]).referenceContext.binding;

							if (!localType.isAnonymousType()) {
								if (typeLength > localType.sourceName.length)
									continue next;
								if (!CharOperation.prefixEquals(typeName, localType.sourceName, false
									/* ignore case */
									))
									continue next;

								requestor.acceptClass(
									localType.qualifiedPackageName(),
									localType.sourceName,
									localType.sourceName,
									localType.modifiers,
									startPosition,
									endPosition);
							}
						}
					}
					break;

				case Scope.CLASS_SCOPE :
					findMemberTypes(typeName, scope.enclosingSourceType(), scope, currentType);
					if (typeLength == 0)
						return; // do not search outside the class scope if no prefix was provided
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					return;
			}
			scope = scope.parent;
		}
	}

	private void findPackages(CompletionOnPackageReference packageStatement) {

		char[] packageName = CharOperation.concatWith(packageStatement.tokens, '.');
		if (packageName.length == 0)
			return;

		setSourceRange(packageStatement.sourceStart, packageStatement.sourceEnd);
		nameEnvironment.findPackages(CharOperation.toLowerCase(packageName), this);
	}

	private void findTypesAndPackages(char[] token, Scope scope) {

		if (token == null)
			return;

		if (scope.enclosingSourceType() != null)
			findNestedTypes(token, scope.enclosingSourceType(), scope);

		if (unitScope != null) {
			int typeLength = token.length;
			SourceTypeBinding[] types = unitScope.topLevelTypes;

			for (int i = 0, length = types.length; i < length; i++) {
				SourceTypeBinding sourceType = types[i];

				if (typeLength > sourceType.sourceName.length)
					continue;

				if (!CharOperation.prefixEquals(token, sourceType.sourceName, false
					/* ignore case */
					))
					continue;

				requestor.acceptType(
					sourceType.qualifiedPackageName(),
					sourceType.sourceName(),
					sourceType.sourceName(),
					startPosition,
					endPosition);
			}
		}

		if (token.length == 0)
			return;

		findKeywords(token, baseTypes, scope);
		nameEnvironment.findTypes(token, this);
		nameEnvironment.findPackages(token, this);
	}

	private void findTypesAndSubpackages(
		char[] token,
		PackageBinding packageBinding) {

		char[] qualifiedName =
			CharOperation.concatWith(packageBinding.compoundName, token, '.');

		if (token == null || token.length == 0) {
			int length = qualifiedName.length;
			System.arraycopy(
				qualifiedName,
				0,
				qualifiedName = new char[length + 1],
				0,
				length);
			qualifiedName[length] = '.';
		}
		nameEnvironment.findTypes(qualifiedName, this);
		nameEnvironment.findPackages(qualifiedName, this);
	}

	private void findVariablesAndMethods(
		char[] token,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope) {

		if (token == null)
			return;

		// Should local variables hide fields from the receiver type or any of its enclosing types?
		// we know its an implicit field/method access... see BlockScope getBinding/getImplicitMethod

		boolean staticsOnly = false;
		// need to know if we're in a static context (or inside a constructor)
		int tokenLength = token.length;

		ObjectVector localsFound = new ObjectVector();
		ObjectVector fieldsFound = new ObjectVector();
		ObjectVector methodsFound = new ObjectVector();

		Scope currentScope = scope;

		done1 : while (true) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (currentScope.kind) {

				case Scope.METHOD_SCOPE :
					// handle the error case inside an explicit constructor call (see MethodScope>>findField)
					MethodScope methodScope = (MethodScope) currentScope;
					staticsOnly |= methodScope.isStatic | methodScope.isConstructorCall;

				case Scope.BLOCK_SCOPE :
					BlockScope blockScope = (BlockScope) currentScope;

					next : for (int i = 0, length = blockScope.locals.length; i < length; i++) {
						LocalVariableBinding local = blockScope.locals[i];

						if (local == null)
							break next;

						if (tokenLength > local.name.length)
							continue next;

						if (!CharOperation.prefixEquals(token, local.name, false /* ignore case */
							))
							continue next;

						if (local.isSecret())
							continue next;

						for (int f = 0; f < localsFound.size; f++) {
							LocalVariableBinding otherLocal =
								(LocalVariableBinding) localsFound.elementAt(f);
							if (CharOperation.equals(otherLocal.name, local.name, false /* ignore case */
								))
								continue next;
						}
						localsFound.add(local);

						requestor.acceptLocalVariable(
							local.name,
							NoChar,
							local.type == null
								? local.declaration.type.toString().toCharArray()
								: local.type.qualifiedSourceName(),
							local.modifiers,
							startPosition,
							endPosition);
					}
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					break done1;
			}
			currentScope = currentScope.parent;
		}

		currentScope = scope;

		done2 : while (true) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (currentScope.kind) {

				case Scope.CLASS_SCOPE :
					ClassScope classScope = (ClassScope) currentScope;
					SourceTypeBinding enclosingType = classScope.referenceContext.binding;
					/*				if (tokenLength == 0) { // only search inside the type itself if no prefix was provided
										findFields(token, enclosingType.fields(), classScope, fieldsFound, staticsOnly);
										findMethods(token, enclosingType.methods(), classScope, methodsFound, staticsOnly, false);
										break done;
									} else { */
					findFields(
						token,
						enclosingType,
						classScope,
						fieldsFound,
						localsFound,
						staticsOnly,
						invocationSite,
						invocationScope,
						true);

					findMethods(
						token,
						null,
						enclosingType,
						classScope,
						methodsFound,
						staticsOnly,
						false,
						false,
						invocationSite,
						invocationScope,
						true);
					staticsOnly |= enclosingType.isStatic();
					//				}
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					break done2;
			}
			currentScope = currentScope.parent;
		}
	}

	// Helper method for private void findVariableNames(char[] name, TypeReference type )
	private void findVariableName(char[] token, char[] qualifiedPackageName, char[] qualifiedSourceName, char[] sourceName, char[][] excludeNames, boolean forArray){
			if(sourceName == null || sourceName.length == 0)
				return;
			if(forArray) {
				sourceName = CharOperation.subarray(sourceName, 0, sourceName.length - 2);
			}

			char[] name = null;
			
			// compute variable name for base type
			try{
				nameScanner.setSourceBuffer(sourceName);
				switch (nameScanner.getNextToken()) {
					case TokenNameint :
					case TokenNamebyte :
					case TokenNameshort :
					case TokenNamechar :
					case TokenNamelong :
					case TokenNamefloat :
					case TokenNamedouble :
						if(token != null && token.length != 0)
							return;
						name = computeBaseNames(sourceName[0], excludeNames);
						break;
					case TokenNameboolean :
						if(token != null && token.length != 0)
							return;
						name = computeBaseNames('z', excludeNames);
						break;
				}
				if(name != null) {
					// accept result
					requestor.acceptVariableName(
						qualifiedPackageName,
						qualifiedSourceName,
						name,
						name,
						startPosition,
						endPosition);
					return;
				}
			} catch(InvalidInputException e){
			}
			
			// compute variable name for non base type
			char[][] names = computeNames(sourceName, forArray);
			next : for(int i = 0 ; i < names.length ; i++){
				name = names[i];
				
				if (!CharOperation.prefixEquals(token, name, false))
					continue next;
				
				// completion must be an identifier (not a keyword, ...).
				try{
					nameScanner.setSourceBuffer(name);
					if(nameScanner.getNextToken() != TokenNameIdentifier)
						continue next;
				} catch(InvalidInputException e){
					continue next;
				}
				
				int count = 2;
				char[] originalName = name;
				for(int j = 0 ; j < excludeNames.length ; j++){
					if(CharOperation.equals(name, excludeNames[j], false)) {
						name = CharOperation.concat(originalName, String.valueOf(count++).toCharArray());
						j = 0;
					}	
				}
				
				// accept result
				requestor.acceptVariableName(
					qualifiedPackageName,
					qualifiedSourceName,
					name,
					name,
					startPosition,
					endPosition);
			}
	}

	private void findVariableNames(char[] name, TypeReference type , char[][] excludeNames){
		if(
			type != null &&
			type.binding != null &&
			type.binding.problemId() == Binding.NoError){
			TypeBinding tb = type.binding;
			findVariableName(
				name,
				tb.qualifiedPackageName(),
				tb.qualifiedSourceName(),
				tb.sourceName(),
				excludeNames,
				type.dimensions() != 0);
		} else {
			char[][] typeName = type.getTypeName();
			findVariableName(
				name,
				NoChar,
				CharOperation.concatWith(typeName, '.'),
				typeName[typeName.length - 1],
				excludeNames,
				type.dimensions() != 0);
		}
	}
	
	public AssistParser getParser() {

		return parser;
	}

	private boolean mustQualifyType(
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
								if(CharOperation.endsWith(imports[j].readableName(), typeName)) {
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
		return true;
	}

	protected void reset() {

		super.reset();
		this.knownPkgs = new HashtableOfObject(10);
		this.knownTypes = new HashtableOfObject(10);
	}

	private void setSourceRange(int start, int end) {

		this.startPosition = start;
		this.endPosition = end + 1;
	}
	
	private char[] computeBaseNames(char firstName, char[][] excludeNames){
		char[] name = new char[]{firstName};
		
		for(int i = 0 ; i < excludeNames.length ; i++){
			if(CharOperation.equals(name, excludeNames[i], false)) {
				name[0]++;
				if(name[0] > 'z')
					name[0] = 'a';
				if(name[0] == firstName)
					return null;
				i = 0;
			}	
		}
		
		return name;
	}
	
	private char[][] computeNames(char[] sourceName, boolean forArray){
		char[][] names = new char[5][];
		int nameCount = 0;
		boolean previousIsUpperCase = false;
		for(int i = sourceName.length - 1 ; i >= 0 ; i--){
			boolean isUpperCase = Character.isUpperCase(sourceName[i]);
			if(isUpperCase && !previousIsUpperCase){
				char[] name = CharOperation.subarray(sourceName,i,sourceName.length);
				if(name.length > 1){
					if(nameCount == names.length) {
						System.arraycopy(names, 0, names = new char[nameCount * 2][], 0, nameCount);
					}
					name[0] = Character.toLowerCase(name[0]);
					
					if(forArray) {
						int length = name.length;
						System.arraycopy(name, 0, name = new char[length + 1], 0, length);
						name[length] = 's';
					}
					
					names[nameCount++] = name;
				}
			}
			previousIsUpperCase = isUpperCase;
		}
		if(nameCount == 0){
			names[nameCount++] = CharOperation.toLowerCase(sourceName);
		}
		System.arraycopy(names, 0, names = new char[nameCount][], 0, nameCount);
		return names;
	}
	
	private char[] computePrefix(SourceTypeBinding declarationType, SourceTypeBinding invocationType, boolean isStatic){
		
		StringBuffer completion = new StringBuffer(10);

		if (isStatic) {
			completion.append(declarationType.sourceName());
			
		} else if (declarationType == invocationType) {
			completion.append(THIS);
			
		} else {
			
			if (!declarationType.isNestedType()) {
				
				completion.append(declarationType.sourceName());
				completion.append('.');
				completion.append(THIS);

			} else if (!declarationType.isAnonymousType()) {
				
				completion.append(declarationType.sourceName());
				completion.append('.');
				completion.append(THIS);
				
			}
		}
		
		return completion.toString().toCharArray();
	}
	
	private boolean isEnclosed(ReferenceBinding possibleEnclosingType, ReferenceBinding type){
		if(type.isNestedType()){
			ReferenceBinding enclosing = type.enclosingType();
			while(enclosing != null ){
				if(possibleEnclosingType == enclosing)
					return true;
				enclosing = enclosing.enclosingType();
			}
		}
		return false;
	}

}
