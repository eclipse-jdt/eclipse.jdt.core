/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 , 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.codeassist.complete.*;
import org.eclipse.jdt.internal.codeassist.impl.AssistParser;
import org.eclipse.jdt.internal.codeassist.impl.Engine;
import org.eclipse.jdt.internal.codeassist.impl.Keywords;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.TypeConverter;

/**
 * This class is the entry point for source completions.
 * It contains two public APIs used to call CodeAssist on a given source with
 * a given environment, assisting position and storage (and possibly options).
 */
public final class CompletionEngine
	extends Engine
	implements ISearchRequestor, TypeConstants , TerminalTokens , RelevanceConstants {
	
	public static boolean DEBUG = false;

	private final static char[] ERROR_PATTERN = "*error*".toCharArray();  //$NON-NLS-1$
	private final static char[] EXCEPTION_PATTERN = "*exception*".toCharArray();  //$NON-NLS-1$
	private final static char[] SEMICOLON = new char[] { ';' };
	
	private final static int SUPERTYPE = 1;
	private final static int SUBTYPE = 2;
	
	private final static int FIELD = 0;
	private final static int LOCAL = 1;
	private final static int ARGUMENT = 2;
	
	int expectedTypesPtr = -1;
	TypeBinding[] expectedTypes = new TypeBinding[1];
	int expectedTypesFilter;
	int uninterestingBindingsPtr = -1;
	Binding[] uninterestingBindings = new Binding[1];
	
	boolean assistNodeIsClass;
	boolean assistNodeIsException;
	boolean assistNodeIsInterface;
	
	IJavaProject javaProject;
	CompletionParser parser;
	ICompletionRequestor requestor;
	ProblemReporter problemReporter;
	char[] source;
	char[] token;
	boolean resolvingImports = false;
	boolean insideQualifiedReference = false;
	int startPosition, actualCompletionPosition, endPosition, offset;
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
	 *  @param nameEnvironment org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment
	 *      used to resolve type/package references and search for types/packages
	 *      based on partial names.
	 *
	 *  @param requestor org.eclipse.jdt.internal.codeassist.ICompletionRequestor
	 *      since the engine might produce answers of various forms, the engine 
	 *      is associated with a requestor able to accept all possible completions.
	 *
	 *  @param settings java.util.Map
	 *		set of options used to configure the code assist engine.
	 */
	public CompletionEngine(
		ISearchableNameEnvironment nameEnvironment,
		ICompletionRequestor requestor,
		Map settings,
		IJavaProject javaProject) {

		super(settings);
		this.javaProject = javaProject;
		this.requestor = requestor;
		this.nameEnvironment = nameEnvironment;

		problemReporter = new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.compilerOptions,
				new DefaultProblemFactory(Locale.getDefault()) {
					public void record(IProblem problem, CompilationResult unitResult, ReferenceContext referenceContext) {
						if (problem.isError() && (problem.getID() & IProblem.Syntax) != 0) {
							CompletionEngine.this.requestor.acceptError(problem);
						}
					}
				});
		this.parser =
			new CompletionParser(problemReporter, this.compilerOptions.sourceLevel >= CompilerOptions.JDK1_4);
		this.lookupEnvironment =
			new LookupEnvironment(this, this.compilerOptions, problemReporter, nameEnvironment);
		this.nameScanner =
			new Scanner(
				false /*comment*/, 
				false /*whitespace*/, 
				false /*nls*/, 
				this.compilerOptions.sourceLevel >= CompilerOptions.JDK1_4 /*assert*/, 
				this.compilerOptions.complianceLevel >= CompilerOptions.JDK1_4 /*strict comment*/,
				null /*taskTags*/, 
				null/*taskPriorities*/);
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

		char[] fullyQualifiedName = CharOperation.concat(packageName, className, '.');
		char[] completionName = fullyQualifiedName;
		
		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);
		
		boolean isQualified = true;
		int relevance = computeBaseRelevance();
		relevance += computeRelevanceForInterestingProposal();
		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, SEMICOLON);
			relevance += computeRelevanceForCaseMatching(token, fullyQualifiedName);
		} else {
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, className)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != CharOperation.NO_CHAR_CHAR)
							return; // ignore types from the default package from outside it
				} else {
					completionName = className;
					isQualified = false;
				}
			}
			relevance += computeRelevanceForCaseMatching(token, className);
			relevance += computeRelevanceForExpectingType(packageName, className);
			relevance += computeRelevanceForClass();
			relevance += computeRelevanceForException(className);
			relevance += computeRelevanceForQualification(isQualified);
		}

		requestor.acceptClass(
			packageName,
			className,
			completionName,
			modifiers,
			startPosition - offset,
			endPosition - offset,
			relevance);
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

		char[] fullyQualifiedName = CharOperation.concat(packageName, interfaceName, '.');
		char[] completionName = fullyQualifiedName;

		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);

		boolean isQualified = true;
		int relevance = computeBaseRelevance();
		relevance += computeRelevanceForInterestingProposal();
		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, new char[] { ';' });
			relevance += computeRelevanceForCaseMatching(token, fullyQualifiedName);
		} else {
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, interfaceName)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != CharOperation.NO_CHAR_CHAR)
							return; // ignore types from the default package from outside it
				} else {
					completionName = interfaceName;
					isQualified = false;
				}
			}
			relevance += computeRelevanceForCaseMatching(token, interfaceName);
			relevance += computeRelevanceForExpectingType(packageName, interfaceName);
			relevance += computeRelevanceForInterface();
			relevance += computeRelevanceForQualification(isQualified);
		}
		
		requestor.acceptInterface(
			packageName,
			interfaceName,
			completionName,
			modifiers,
			startPosition - offset,
			endPosition - offset,
			relevance);
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
		
		int relevance = computeBaseRelevance();
		relevance += computeRelevanceForInterestingProposal();
		relevance += computeRelevanceForCaseMatching(token, packageName);

		requestor.acceptPackage(
			packageName,
			resolvingImports
				? CharOperation.concat(packageName, new char[] { '.', '*', ';' })
				: packageName,
			startPosition - offset,
			endPosition - offset,
			relevance);
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

		char[] fullyQualifiedName = CharOperation.concat(packageName, typeName, '.');
		char[] completionName = fullyQualifiedName;
		
		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);

		boolean isQualified = true;
		int relevance = computeBaseRelevance();
		relevance += computeRelevanceForInterestingProposal();
		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, new char[] { ';' });
			relevance += computeRelevanceForCaseMatching(token, fullyQualifiedName);
		} else {
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, typeName)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != CharOperation.NO_CHAR_CHAR)
							return; // ignore types from the default package from outside it
				} else {
					completionName = typeName;
					isQualified = false;
				}
			}
			relevance += computeRelevanceForCaseMatching(token, typeName);
			relevance += computeRelevanceForExpectingType(packageName, typeName);
			relevance += computeRelevanceForQualification(isQualified);
		}
		
		requestor.acceptType(
			packageName,
			typeName,
			completionName,
			startPosition - offset,
			endPosition - offset,
			relevance);
	}

	private void complete(AstNode astNode, AstNode astNodeParent, Binding qualifiedBinding, Scope scope) {

		setSourceRange(astNode.sourceStart, astNode.sourceEnd);
		
		computeUninterestingBindings(astNodeParent, scope);
		if(astNodeParent != null) {
			computeExpectedTypes(astNodeParent, scope);
		}
		
		if (astNode instanceof CompletionOnFieldType) {

			CompletionOnFieldType field = (CompletionOnFieldType) astNode;
			CompletionOnSingleTypeReference type = (CompletionOnSingleTypeReference) field.type;
			token = type.token;
			setSourceRange(type.sourceStart, type.sourceEnd);
			
			findTypesAndPackages(token, scope);
			findKeywordsForMember(token, field.modifiers);
			
			if(!field.isLocalVariable && field.modifiers == CompilerModifiers.AccDefault) {
				findMethods(token,null,scope.enclosingSourceType(),scope,new ObjectVector(),false,false,true,null,null,false);
			}
		} else {
			if(astNode instanceof CompletionOnMethodReturnType) {
				
				CompletionOnMethodReturnType method = (CompletionOnMethodReturnType) astNode;
				SingleTypeReference type = (CompletionOnSingleTypeReference) method.returnType;
				token = type.token;
				setSourceRange(type.sourceStart, type.sourceEnd);
				findTypesAndPackages(token, scope);
				findKeywordsForMember(token, method.modifiers);
			
				if(method.modifiers == CompilerModifiers.AccDefault) {
					findMethods(token,null,scope.enclosingSourceType(),scope,new ObjectVector(),false,false,true,null,null,false);
				}
			} else {
				
				if (astNode instanceof CompletionOnSingleNameReference) {
					CompletionOnSingleNameReference singleNameReference = (CompletionOnSingleNameReference) astNode;
					token = singleNameReference.token;
					findVariablesAndMethods(
						token,
						scope,
						(CompletionOnSingleNameReference) astNode,
						scope);
					// can be the start of a qualified type name
					findTypesAndPackages(token, scope);
					findKeywords(token, singleNameReference.possibleKeywords);
					if(singleNameReference.canBeExplicitConstructor){
						if(CharOperation.prefixEquals(token, Keywords.THIS, false)) {
							ReferenceBinding ref = scope.enclosingSourceType();
							findExplicitConstructors(Keywords.THIS, ref, (MethodScope)scope, singleNameReference);
						} else if(CharOperation.prefixEquals(token, Keywords.SUPER, false)) {
							ReferenceBinding ref = scope.enclosingSourceType();
							findExplicitConstructors(Keywords.SUPER, ref.superclass(), (MethodScope)scope, singleNameReference);
						}
					}
				} else {
	
					if (astNode instanceof CompletionOnSingleTypeReference) {
	
						token = ((CompletionOnSingleTypeReference) astNode).token;
						
						assistNodeIsClass = astNode instanceof CompletionOnClassReference;
						assistNodeIsException = astNode instanceof CompletionOnExceptionReference;
						assistNodeIsInterface = astNode instanceof CompletionOnInterfaceReference;
	
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
							token = ref.completionIdentifier;
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
	
									findClassField(token, (TypeBinding) qualifiedBinding, scope);
									
									MethodScope methodScope = null;
									if((scope instanceof MethodScope && !((MethodScope)scope).isStatic)
										|| ((methodScope = scope.enclosingMethodScope()) != null && !methodScope.isStatic)) {
										findKeywords(token, new char[][]{Keywords.THIS});
									}
	
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
								
								assistNodeIsClass = astNode instanceof CompletionOnQualifiedClassReference;
								assistNodeIsException = astNode instanceof CompletionOnQualifiedExceptionReference;
								assistNodeIsInterface = astNode instanceof CompletionOnQualifiedInterfaceReference;
								
								CompletionOnQualifiedTypeReference ref =
									(CompletionOnQualifiedTypeReference) astNode;
								token = ref.completionIdentifier;
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
									insideQualifiedReference = true;
									CompletionOnMemberAccess access = (CompletionOnMemberAccess) astNode;
									long completionPosition = access.nameSourcePosition;
									setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
					
									token = access.token;
									
									findKeywords(token, new char[][]{Keywords.NEW});
									
									findFieldsAndMethods(
										token,
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
										token = messageSend.selector;
										if (qualifiedBinding == null) {
											
											findImplicitMessageSends(token, argTypes, scope, messageSend, scope);
										} else {
	
											findMethods(
												token,
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
									
													token = access.completionIdentifier;
									
													findClassField(token, (TypeBinding) qualifiedBinding, scope);
												} else {
													if(astNode instanceof CompletionOnMethodName) {
														CompletionOnMethodName method = (CompletionOnMethodName) astNode;
															
														setSourceRange(method.sourceStart, method.selectorEnd);
															
														FieldBinding[] fields = scope.enclosingSourceType().fields();
														char[][] excludeNames = new char[fields.length][];
														for(int i = 0 ; i < fields.length ; i++){
															excludeNames[i] = fields[i].name;
														}
														
														token = method.selector;
														
														findVariableNames(token, method.returnType, excludeNames, FIELD, method.modifiers);
													} else {
														if (astNode instanceof CompletionOnFieldName) {
															CompletionOnFieldName field = (CompletionOnFieldName) astNode;
															
															FieldBinding[] fields = scope.enclosingSourceType().fields();
															char[][] excludeNames = new char[fields.length][];
															for(int i = 0 ; i < fields.length ; i++){
																excludeNames[i] = fields[i].name;
															}
															
															token = field.realName;
															
															findVariableNames(field.realName, field.type, excludeNames, FIELD, field.modifiers);
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
																
																if(variable instanceof CompletionOnLocalName){
																	token = ((CompletionOnLocalName) variable).realName;
																	findVariableNames(token, variable.type, excludeNames, LOCAL, variable.modifiers);
																} else {
																	CompletionOnArgumentName arg = (CompletionOnArgumentName) variable;
																	token = arg.realName;
																	findVariableNames(token, variable.type, excludeNames, arg.isCatchArgument ? LOCAL : ARGUMENT, variable.modifiers);
																}
															} else {
																if(astNode instanceof CompletionOnKeyword) {
																	CompletionOnKeyword keyword = (CompletionOnKeyword)astNode;
																	findKeywords(keyword.getToken(), keyword.getPossibleKeywords());
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
		}
	}
	
	public void complete(IType type, char[] snippet, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic){	

		IType topLevelType = type;
		while(topLevelType.getDeclaringType() != null) {
			topLevelType = topLevelType.getDeclaringType();
		}
		
		CompilationResult compilationResult = new CompilationResult((topLevelType.getElementName() + ".java").toCharArray(), 1, 1, this.compilerOptions.maxProblemsPerUnit); //$NON-NLS-1$
	
		CompilationUnitDeclaration compilationUnit = new CompilationUnitDeclaration(problemReporter, compilationResult, 0);
	
		try {
			TypeDeclaration typeDeclaration = TypeConverter.buildTypeDeclaration(type, compilationUnit, compilationResult, problemReporter);
		
			if(typeDeclaration != null) {	
				// build AST from snippet
				Initializer fakeInitializer = parseSnippeInitializer(snippet, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic);
				
				// merge AST
				FieldDeclaration[] oldFields = typeDeclaration.fields;
				FieldDeclaration[] newFields = new FieldDeclaration[oldFields.length + 1];
				System.arraycopy(oldFields, 0, newFields, 0, oldFields.length);
				newFields[oldFields.length] = fakeInitializer;
				typeDeclaration.fields = newFields;
		
				if(DEBUG) {
					System.out.println("SNIPPET COMPLETION AST :"); //$NON-NLS-1$
					System.out.println(compilationUnit.toString());
				}
				
				if (compilationUnit.types != null) {
					try {
						lookupEnvironment.buildTypeBindings(compilationUnit);
				
						if ((unitScope = compilationUnit.scope) != null) {
							lookupEnvironment.completeTypeBindings(compilationUnit, true);
							compilationUnit.scope.faultInTypes();
							compilationUnit.resolve();
						}
					} catch (CompletionNodeFound e) {
						//					completionNodeFound = true;
						if (e.astNode != null) {
							// if null then we found a problem in the completion node
							complete(e.astNode, parser.assistNodeParent, e.qualifiedBinding, e.scope);
						}
					}
				}
			}
		} catch(JavaModelException e) {
			// Do nothing
		}
	}
	
	private Initializer parseSnippeInitializer(char[] snippet, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic){
		StringBuffer prefix = new StringBuffer();
		prefix.append("public class FakeType {\n "); //$NON-NLS-1$
		if(isStatic) {
			prefix.append("static "); //$NON-NLS-1$
		}
		prefix.append("{\n"); //$NON-NLS-1$
		for (int i = 0; i < localVariableTypeNames.length; i++) {
			prefix.append(AstNode.modifiersString(localVariableModifiers[i]));
			prefix.append(' ');
			prefix.append(localVariableTypeNames[i]);
			prefix.append(' ');
			prefix.append(localVariableNames[i]);
			prefix.append(';');
		}
		
		char[] fakeSource = CharOperation.concat(prefix.toString().toCharArray(), snippet, "}}".toCharArray());//$NON-NLS-1$ 
		offset = prefix.length();
		
		String encoding = this.compilerOptions.defaultEncoding;
		BasicCompilationUnit fakeUnit = new BasicCompilationUnit(
			fakeSource, 
			null,
			"FakeType.java", //$NON-NLS-1$
			encoding); 
			
		actualCompletionPosition = prefix.length() + position - 1;
			
		CompilationResult fakeResult = new CompilationResult(fakeUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration fakeAST = parser.dietParse(fakeUnit, fakeResult, actualCompletionPosition);
		
		parseMethod(fakeAST, actualCompletionPosition);
		
		return (Initializer)fakeAST.types[0].fields[0];
	}

	/**
	 * Ask the engine to compute a completion at the specified position
	 * of the given compilation unit.
	 *
	 *  @return void
	 *      completion results are answered through a requestor.
	 *
	 *  @param sourceUnit org.eclipse.jdt.internal.compiler.env.ICompilationUnit
	 *      the source of the current compilation unit.
	 *
	 *  @param completionPosition int
	 *      a position in the source where the completion is taking place. 
	 *      This position is relative to the source provided.
	 */
	public void complete(ICompilationUnit sourceUnit, int completionPosition, int offset) {

		if(DEBUG) {
			System.out.print("COMPLETION IN "); //$NON-NLS-1$
			System.out.print(sourceUnit.getFileName());
			System.out.print(" AT POSITION "); //$NON-NLS-1$
			System.out.println(completionPosition);
			System.out.println("COMPLETION - Source :"); //$NON-NLS-1$
			System.out.println(sourceUnit.getContents());
		}
		try {
			actualCompletionPosition = completionPosition - 1;
			this.offset = offset;
			// for now until we can change the UI.
			CompilationResult result = new CompilationResult(sourceUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
			CompilationUnitDeclaration parsedUnit = parser.dietParse(sourceUnit, result, actualCompletionPosition);

			//		boolean completionNodeFound = false;
			if (parsedUnit != null) {
				if(DEBUG) {
					System.out.println("COMPLETION - Diet AST :"); //$NON-NLS-1$
					System.out.println(parsedUnit.toString());
				}

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
						} else if(importReference instanceof CompletionOnKeyword) {
							setSourceRange(importReference.sourceStart, importReference.sourceEnd);
							CompletionOnKeyword keyword = (CompletionOnKeyword)importReference;
							findKeywords(keyword.getToken(), keyword.getPossibleKeywords());
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
							if(DEBUG) {
								System.out.println("COMPLETION - AST :"); //$NON-NLS-1$
								System.out.println(parsedUnit.toString());
							}
							parsedUnit.resolve();
						}
					} catch (CompletionNodeFound e) {
						//					completionNodeFound = true;
						if (e.astNode != null) {
							if(DEBUG) {
								System.out.print("COMPLETION - Completion node : "); //$NON-NLS-1$
								System.out.println(e.astNode.toString());
								if(parser.assistNodeParent != null) {
									System.out.print("COMPLETION - Parent Node : ");  //$NON-NLS-1$
									System.out.println(parser.assistNodeParent);
								}
							}
							// if null then we found a problem in the completion node
							complete(e.astNode, parser.assistNodeParent, e.qualifiedBinding, e.scope);
						}
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
			argTypes[a] = arguments[a].resolvedType;
		return argTypes;
	}
	
	private void findAnonymousType(
		ReferenceBinding currentType,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite) {

		if (currentType.isInterface()) {
			char[] completion = CharOperation.NO_CHAR;
			// nothing to insert - do not want to replace the existing selector & arguments
			if (source == null
				|| source.length <= endPosition
				|| source[endPosition] != ')')
				completion = new char[] { ')' };
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			requestor.acceptAnonymousType(
				currentType.qualifiedPackageName(),
				currentType.qualifiedSourceName(),
				CharOperation.NO_CHAR_CHAR,
				CharOperation.NO_CHAR_CHAR,
				CharOperation.NO_CHAR_CHAR,
				completion,
				IConstants.AccPublic,
				endPosition - offset,
				endPosition - offset,
				relevance);
		} else {
			findConstructors(
				currentType,
				argTypes,
				scope,
				invocationSite,
				true);
		}
	}

	private void findClassField(char[] token, TypeBinding receiverType, Scope scope) {

		if (token == null)
			return;

		if (token.length <= classField.length
			&& CharOperation.prefixEquals(token, classField, false /* ignore case */
		)) {
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(token, classField);
			relevance += computeRelevanceForExpectingType(scope.getJavaLangClass());
				
			requestor.acceptField(
				CharOperation.NO_CHAR,
				CharOperation.NO_CHAR,
				classField,
				CharOperation.NO_CHAR,
				CharOperation.NO_CHAR,
				classField,
				CompilerModifiers.AccStatic | CompilerModifiers.AccPublic,
				startPosition - offset,
				endPosition - offset,
				relevance);
		}
	}

	private void findExplicitConstructors(
		char[] name,
		ReferenceBinding currentType,
		MethodScope scope,
		InvocationSite invocationSite) {
			
		ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration)scope.referenceContext;
		MethodBinding enclosingConstructor = constructorDeclaration.binding;

		// No visibility checks can be performed without the scope & invocationSite
		MethodBinding[] methods = currentType.availableMethods();
		if(methods != null) {
			next : for (int f = methods.length; --f >= 0;) {
				MethodBinding constructor = methods[f];
				if (constructor != enclosingConstructor && constructor.isConstructor()) {
					
					if (constructor.isSynthetic()) continue next;
						
					if (options.checkVisibility
						&& !constructor.canBeSeenBy(invocationSite, scope))	continue next;
	
					TypeBinding[] parameters = constructor.parameters;
					int paramLength = parameters.length;
	
					char[][] parameterPackageNames = new char[paramLength][];
					char[][] parameterTypeNames = new char[paramLength][];
					for (int i = 0; i < paramLength; i++) {
						TypeBinding type = parameters[i];
						parameterPackageNames[i] = type.qualifiedPackageName();
						parameterTypeNames[i] = type.qualifiedSourceName();
					}
					char[][] parameterNames = findMethodParameterNames(constructor,parameterTypeNames);
					
					char[] completion = CharOperation.NO_CHAR;
					if (source != null
						&& source.length > endPosition
						&& source[endPosition] == '(')
						completion = name;
					else
						completion = CharOperation.concat(name, new char[] { '(', ')' });
					
					int relevance = computeBaseRelevance();
					relevance += computeRelevanceForInterestingProposal();
					relevance += computeRelevanceForCaseMatching(token, name);
					requestor.acceptMethod(
						currentType.qualifiedPackageName(),
						currentType.qualifiedSourceName(),
						name,
						parameterPackageNames,
						parameterTypeNames,
						parameterNames,
						CharOperation.NO_CHAR,
						CharOperation.NO_CHAR,
						completion,
						constructor.modifiers,
						startPosition - offset,
						endPosition - offset,
						relevance);
				}
			}
		}
	}
	private void findConstructors(
		ReferenceBinding currentType,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite,
		boolean forAnonymousType) {

		// No visibility checks can be performed without the scope & invocationSite
		MethodBinding[] methods = currentType.availableMethods();
		if(methods != null) {
			int minArgLength = argTypes == null ? 0 : argTypes.length;
			next : for (int f = methods.length; --f >= 0;) {
				MethodBinding constructor = methods[f];
				if (constructor.isConstructor()) {
					
					if (constructor.isSynthetic()) continue next;
						
					if (options.checkVisibility
						&& !constructor.canBeSeenBy(invocationSite, scope)) {
						if(!forAnonymousType || !constructor.isProtected())
							continue next;
					}
	
					TypeBinding[] parameters = constructor.parameters;
					int paramLength = parameters.length;
					if (minArgLength > paramLength)
						continue next;
					for (int a = minArgLength; --a >= 0;)
						if (argTypes[a] != null) // can be null if it could not be resolved properly
							if (!argTypes[a].isCompatibleWith(constructor.parameters[a]))
								continue next;
	
					char[][] parameterPackageNames = new char[paramLength][];
					char[][] parameterTypeNames = new char[paramLength][];
					for (int i = 0; i < paramLength; i++) {
						TypeBinding type = parameters[i];
						parameterPackageNames[i] = type.qualifiedPackageName();
						parameterTypeNames[i] = type.qualifiedSourceName();
					}
					char[][] parameterNames = findMethodParameterNames(constructor,parameterTypeNames);
					
					char[] completion = CharOperation.NO_CHAR;
					// nothing to insert - do not want to replace the existing selector & arguments
					if (source == null
						|| source.length <= endPosition
						|| source[endPosition] != ')')
						completion = new char[] { ')' };
					
					if(forAnonymousType){
						int relevance = computeBaseRelevance();
						relevance += computeRelevanceForInterestingProposal();
						requestor.acceptAnonymousType(
							currentType.qualifiedPackageName(),
							currentType.qualifiedSourceName(),
							parameterPackageNames,
							parameterTypeNames,
							parameterNames,
							completion,
							constructor.modifiers,
							endPosition - offset,
							endPosition - offset,
							relevance);
					} else {
						int relevance = computeBaseRelevance();
						relevance += computeRelevanceForInterestingProposal();
						requestor.acceptMethod(
							currentType.qualifiedPackageName(),
							currentType.qualifiedSourceName(),
							currentType.sourceName(),
							parameterPackageNames,
							parameterTypeNames,
							parameterNames,
							CharOperation.NO_CHAR,
							CharOperation.NO_CHAR,
							completion,
							constructor.modifiers,
							endPosition - offset,
							endPosition - offset,
							relevance);
					}
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
				Object[] other = (Object[])fieldsFound.elementAt(i);
				FieldBinding otherField = (FieldBinding) other[0];
				ReferenceBinding otherReceiverType = (ReferenceBinding) other[1];
				if (field == otherField && receiverType == otherReceiverType)
					continue next;
				if (CharOperation.equals(field.name, otherField.name, true)) {
					if (field.declaringClass.isSuperclassOf(otherField.declaringClass))
						continue next;
					if (otherField.declaringClass.isInterface()) {
						if (field.declaringClass == scope.getJavaLangObject())
							continue next;
						if (field.declaringClass.implementsInterface(otherField.declaringClass, true))
							continue next;
					}
					if (field.declaringClass.isInterface())
						if (otherField.declaringClass.implementsInterface(field.declaringClass, true))
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
			
			fieldsFound.add(new Object[]{field, receiverType});
			
			char[] completion = field.name;
			
			if(prefixRequired || options.forceImplicitQualification){
				char[] prefix = computePrefix(scope.enclosingSourceType(), invocationScope.enclosingSourceType(), field.isStatic());
				completion = CharOperation.concat(prefix,completion,'.');
			}

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal(field);
			relevance += computeRelevanceForCaseMatching(fieldName, field.name);
			relevance += computeRelevanceForExpectingType(field.type);
			relevance += computeRelevanceForStatic(onlyStaticFields, field.isStatic());
			relevance += computeRelevanceForQualification(prefixRequired);

			requestor
				.acceptField(
					field.declaringClass.qualifiedPackageName(),
					field.declaringClass.qualifiedSourceName(),
					field.name,
					field.type.qualifiedPackageName(),
					field.type.qualifiedSourceName(),
					completion,
			// may include some qualification to resolve ambiguities
			field.modifiers, startPosition - offset, endPosition - offset,
			relevance);
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

			FieldBinding[] fields = currentType.availableFields();
			if(fields != null) {
				findFields(
					fieldName,
					fields,
					scope,
					fieldsFound,
					localsFound,
					onlyStaticFields,
					receiverType,
					invocationSite,
					invocationScope,
					implicitCall);
			}
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

						FieldBinding[] fields = anInterface.availableFields();
						if(fields !=  null) {
							findFields(
								fieldName,
								fields,
								scope,
								fieldsFound,
								localsFound,
								onlyStaticFields,
								receiverType,
								invocationSite,
								invocationScope,
								implicitCall);
						}

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
			)) {
				
				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(token,lengthField);
				relevance += computeRelevanceForExpectingType(BaseTypes.IntBinding);
				
				requestor.acceptField(
					CharOperation.NO_CHAR,
					CharOperation.NO_CHAR,
					lengthField,
					CharOperation.NO_CHAR,
					CharOperation.NO_CHAR,
					lengthField,
					CompilerModifiers.AccPublic,
					startPosition - offset,
					endPosition - offset,
					relevance);
			}
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
			
		char[] lastToken = tokens[tokens.length - 1];
		if(lastToken != null && lastToken.length == 0)
			importName = CharOperation.concat(importName, new char[]{'.'});

		resolvingImports = true;
		setSourceRange(
			importReference.sourceStart,
			importReference.declarationSourceEnd);
			
		token =  importName;
		// want to replace the existing .*;
		nameEnvironment.findPackages(importName, this);
		nameEnvironment.findTypes(importName, this);
	}

	// what about onDemand types? Ignore them since it does not happen!
	// import p1.p2.A.*;
	private void findKeywords(char[] keyword, char[][] choices) {
		if(choices == null || choices.length == 0) return;
		
		int length = keyword.length;
		if (length > 0)
			for (int i = 0; i < choices.length; i++)
				if (length <= choices[i].length
					&& CharOperation.prefixEquals(keyword, choices[i], false /* ignore case */
				)){
					int relevance = computeBaseRelevance();
					relevance += computeRelevanceForInterestingProposal();
					relevance += computeRelevanceForCaseMatching(keyword, choices[i]);
					
					requestor.acceptKeyword(choices[i], startPosition - offset, endPosition - offset,relevance);
				}
	}
	
	private void findKeywordsForMember(char[] token, int modifiers) {
		char[][] keywords = new char[Keywords.COUNT][];
		int count = 0;
				
		// visibility
		if((modifiers & CompilerModifiers.AccPrivate) == 0
			&& (modifiers & CompilerModifiers.AccProtected) == 0
			&& (modifiers & CompilerModifiers.AccPublic) == 0) {
			keywords[count++] = Keywords.PROTECTED;
			keywords[count++] = Keywords.PUBLIC;
			if((modifiers & CompilerModifiers.AccAbstract) == 0) {
				keywords[count++] = Keywords.PRIVATE;
			}
		}
		
		if((modifiers & CompilerModifiers.AccAbstract) == 0) {
			// abtract
			if((modifiers & ~(CompilerModifiers.AccVisibilityMASK | CompilerModifiers.AccStatic)) == 0) {
				keywords[count++] = Keywords.ABSTARCT;
			}
			
			// final
			if((modifiers & CompilerModifiers.AccFinal) == 0) {
				keywords[count++] = Keywords.FINAL;
			}
			
			// static
			if((modifiers & CompilerModifiers.AccStatic) == 0) {
				keywords[count++] = Keywords.STATIC;
			}
			
			boolean canBeField = true;
			boolean canBeMethod = true;
			boolean canBeType = true;
			if((modifiers & CompilerModifiers.AccNative) != 0
				|| (modifiers & CompilerModifiers.AccStrictfp) != 0
				|| (modifiers & CompilerModifiers.AccSynchronized) != 0) {
				canBeField = false;
				canBeType = false;
			}
			
			if((modifiers & CompilerModifiers.AccTransient) != 0
				|| (modifiers & CompilerModifiers.AccVolatile) != 0) {
				canBeMethod = false;
				canBeType = false;
			}
			
			if(canBeField) {
				// transient
				if((modifiers & CompilerModifiers.AccTransient) == 0) {
					keywords[count++] = Keywords.TRANSIENT;
				}
				
				// volatile
				if((modifiers & CompilerModifiers.AccVolatile) == 0) {
					keywords[count++] = Keywords.VOLATILE;
				}
			}
			
			if(canBeMethod) {
				// native
				if((modifiers & CompilerModifiers.AccNative) == 0) {
					keywords[count++] = Keywords.NATIVE;
				}
	
				// strictfp
				if((modifiers & CompilerModifiers.AccStrictfp) == 0) {
					keywords[count++] = Keywords.STRICTFP;
				}
				
				// synchronized
				if((modifiers & CompilerModifiers.AccSynchronized) == 0) {
					keywords[count++] = Keywords.SYNCHRONIZED;
				}
			}
			
			if(canBeType) {
				keywords[count++] = Keywords.CLASS;
				keywords[count++] = Keywords.INTERFACE;
			}
		} else {
			// class
			keywords[count++] = Keywords.CLASS;
			keywords[count++] = Keywords.INTERFACE;
		}
		System.arraycopy(keywords, 0, keywords = new char[count][], 0, count);
		
		findKeywords(token, keywords);
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

					if (memberType.enclosingType().isInterface())
						if (otherType.enclosingType()
							.implementsInterface(memberType.enclosingType(), true))
							continue next;
				}
			}

			typesFound.add(memberType);

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(typeName, memberType.sourceName);
			relevance += computeRelevanceForExpectingType(memberType);

			if (memberType.isClass()) {
				relevance += computeRelevanceForClass();
				relevance += computeRelevanceForException(memberType.sourceName);
				requestor.acceptClass(
					memberType.qualifiedPackageName(),
					memberType.qualifiedSourceName(),
					memberType.sourceName(),
					memberType.modifiers,
					startPosition - offset,
					endPosition - offset,
					relevance);

			} else {
				relevance += computeRelevanceForInterface();
				requestor.acceptInterface(
					memberType.qualifiedPackageName(),
					memberType.qualifiedSourceName(),
					memberType.sourceName(),
					memberType.modifiers,
					startPosition - offset,
					endPosition - offset,
					relevance);
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

						MethodBinding[] methods = currentType.availableMethods();
						if(methods != null) {
							if(isCompletingDeclaration){
	
								findLocalMethodDeclarations(
									selector,
									methods,
									scope,
									methodsFound,
									onlyStaticMethods,
									exactMatch,
									receiverType);
	
							} else {
	
								findLocalMethods(
									selector,
									argTypes,
									methods,
									scope,
									methodsFound,
									onlyStaticMethods,
									exactMatch,
									receiverType,
									invocationSite,
									invocationScope,
									implicitCall);
							}
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

			if (expectedTypesPtr > -1 && method.returnType == BaseTypes.VoidBinding) continue next;
			
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
					if (!argTypes[a].isCompatibleWith(method.parameters[a])) {
						continue next;
					}
				}
			}
			
			boolean prefixRequired = false;
			
			for (int i = methodsFound.size; --i >= 0;) {
				Object[] other = (Object[]) methodsFound.elementAt(i);
				MethodBinding otherMethod = (MethodBinding) other[0];
				ReferenceBinding otherReceiverType = (ReferenceBinding) other[1];
				if (method == otherMethod && receiverType == otherReceiverType)
					continue next;

				if (CharOperation.equals(method.selector, otherMethod.selector, true)
					&& method.areParametersEqual(otherMethod)) {

					if (method.declaringClass.isSuperclassOf(otherMethod.declaringClass))
						continue next;

					if (otherMethod.declaringClass.isInterface()) {
						if(method.declaringClass == scope.getJavaLangObject())
							continue next;
						
						if (method
							.declaringClass
							.implementsInterface(otherMethod.declaringClass, true))
							continue next;
					}

					if (method.declaringClass.isInterface())
						if(otherMethod
							.declaringClass
							.implementsInterface(method.declaringClass,true))
							continue next;
					prefixRequired = true;
				}
			}

			methodsFound.add(new Object[]{method, receiverType});
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];

			for (int i = 0; i < length; i++) {
				TypeBinding type = method.parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}
			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);

			char[] completion = CharOperation.NO_CHAR;
			
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

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(methodName, method.selector);
			relevance += computeRelevanceForExpectingType(method.returnType);
			relevance += computeRelevanceForStatic(onlyStaticMethods, method.isStatic());
			relevance += computeRelevanceForQualification(prefixRequired);
			
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
				startPosition - offset,
				endPosition - offset,
				relevance);
			startPosition = previousStartPosition;
		}
	}
	
	int computeRelevanceForCaseMatching(char[] token, char[] proposalName){
		if (CharOperation.prefixEquals(token, proposalName, true /* do not ignore case */)) {
			if(CharOperation.equals(token, proposalName, true /* do not ignore case */)) {
				return R_CASE + R_EXACT_NAME;
			} else {
				return R_CASE;
			}
		} else {
			return 0;
		}
	}
	private int computeRelevanceForClass(){
		if(assistNodeIsClass) {
			return R_CLASS;
		}
		return 0;
	}
	private int computeRelevanceForInterface(){
		if(assistNodeIsInterface) {
			return R_INTERFACE;
		}
		return 0;
	}
	private int computeRelevanceForQualification(boolean prefixRequired) {
		if(!prefixRequired) {
			return R_UNQUALIFIED;
		}
		return 0;
	}
	private int computeRelevanceForStatic(boolean onlyStatic, boolean isStatic) {
		if(insideQualifiedReference && !onlyStatic && !isStatic) {
			return R_NON_STATIC;
		}
		return 0;
	}
	private int computeRelevanceForException(char[] proposalName){
		
		if(assistNodeIsException &&
			(CharOperation.match(EXCEPTION_PATTERN, proposalName, false) ||
			CharOperation.match(ERROR_PATTERN, proposalName, false))) { 
			return R_EXCEPTION;
		}
		return 0;
	}
	private int computeRelevanceForExpectingType(TypeBinding proposalType){
		if(expectedTypes != null && proposalType != null) {
			for (int i = 0; i <= expectedTypesPtr; i++) {
				if(CharOperation.equals(expectedTypes[i].qualifiedPackageName(), proposalType.qualifiedPackageName()) &&
					CharOperation.equals(expectedTypes[i].qualifiedSourceName(), proposalType.qualifiedSourceName())) {
					return R_EXACT_EXPECTED_TYPE;
				}
				if((expectedTypesFilter & SUBTYPE) != 0
					&& proposalType.isCompatibleWith(expectedTypes[i])) {
						return R_EXPECTED_TYPE;
				}
				if((expectedTypesFilter & SUPERTYPE) != 0
					&& expectedTypes[i].isCompatibleWith(proposalType)) {
					return R_EXPECTED_TYPE;
				}
			}
		} 
		return 0;
	}
	private int computeRelevanceForExpectingType(char[] packageName, char[] typeName){
		if(expectedTypes != null) {
			for (int i = 0; i <= expectedTypesPtr; i++) {
				if(CharOperation.equals(expectedTypes[i].qualifiedPackageName(), packageName) &&
					CharOperation.equals(expectedTypes[i].qualifiedSourceName(), typeName)) {
					return R_EXACT_EXPECTED_TYPE;
				}
			}
		} 
		return 0;
	}
	int computeRelevanceForInterestingProposal(){
		return computeRelevanceForInterestingProposal(null);
	}
	private int computeRelevanceForInterestingProposal(Binding binding){
		if(uninterestingBindings != null) {
			for (int i = 0; i <= uninterestingBindingsPtr; i++) {
				if(uninterestingBindings[i] == binding) {
					return 0;
				}
			}
		}
		return R_INTERESTING;
	}
	private void computeUninterestingBindings(AstNode parent, Scope scope){
		if(parent instanceof LocalDeclaration) {
			addUninterestingBindings(((LocalDeclaration)parent).binding);
		} else if (parent instanceof FieldDeclaration) {
			addUninterestingBindings(((FieldDeclaration)parent).binding);
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

			for (int i = methodsFound.size; --i >= 0;) {
				MethodBinding otherMethod = (MethodBinding) methodsFound.elementAt(i);
				if (method == otherMethod)
					continue next;

				if (CharOperation.equals(method.selector, otherMethod.selector, true)
					&& method.areParametersEqual(otherMethod)) {
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
							completion.append(CharOperation.concat(exceptionPackageName, exceptionTypeName, '.'));
						} else {
							completion.append(exception.sourceName());
						}
					}
				}
			}

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(methodName, method.selector);
			if(method.isAbstract()) relevance += R_ABSTRACT_METHOD;

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
				startPosition - offset,
				endPosition - offset,
				relevance);
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
		
		if(isCompletingDeclaration) {
			MethodBinding[] methods = receiverType.availableMethods();
			if (methods != null){
				for (int i = 0; i < methods.length; i++) {
					if(!methods[i].isDefaultAbstract()) {
						methodsFound.add(methods[i]);
					}
				}
			}
		}
		
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
			
			MethodBinding[] methods = currentType.availableMethods();
			if(methods != null) {
				if(isCompletingDeclaration){
					findLocalMethodDeclarations(
						selector,
						methods,
						scope,
						methodsFound,
						onlyStaticMethods,
						exactMatch,
						receiverType);
				} else{
					findLocalMethods(
						selector,
						argTypes,
						methods,
						scope,
						methodsFound,
						onlyStaticMethods,
						exactMatch,
						receiverType,
						invocationSite,
						invocationScope,
						implicitCall);
				}
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
			return CharOperation.NO_CHAR_CHAR;
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

								int relevance = computeBaseRelevance();
								relevance += computeRelevanceForInterestingProposal();
								relevance += computeRelevanceForCaseMatching(typeName, localType.sourceName);
								relevance += computeRelevanceForExpectingType(localType);
								relevance += computeRelevanceForException(localType.sourceName);
								relevance += computeRelevanceForClass();
								
								requestor.acceptClass(
									localType.qualifiedPackageName(),
									localType.sourceName,
									localType.sourceName,
									localType.modifiers,
									startPosition - offset,
									endPosition - offset,
									relevance);
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

		token = CharOperation.concatWith(packageStatement.tokens, '.');
		if (token.length == 0)
			return;

		setSourceRange(packageStatement.sourceStart, packageStatement.sourceEnd);
		nameEnvironment.findPackages(CharOperation.toLowerCase(token), this);
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

				if (typeLength > sourceType.sourceName.length)	continue;
				
				if (!CharOperation.prefixEquals(token, sourceType.sourceName, false))	continue;

				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(token, sourceType.sourceName);
				relevance += computeRelevanceForExpectingType(sourceType);
				relevance += computeRelevanceForQualification(false);

				if (sourceType.isClass()){
					relevance += computeRelevanceForClass();
					relevance += computeRelevanceForException(sourceType.sourceName);
					requestor.acceptClass(
						sourceType.qualifiedPackageName(),
						sourceType.sourceName(),
						sourceType.sourceName(),
						sourceType.modifiers,
						startPosition - offset, 
						endPosition - offset,
						relevance);
				} else {
					relevance += computeRelevanceForInterface();
					requestor.acceptInterface(
						sourceType.qualifiedPackageName(),
						sourceType.sourceName(),
						sourceType.sourceName(),
						sourceType.modifiers,
						startPosition - offset,
						endPosition - offset,
						relevance);
				}
			}
		}
		
		if (token.length == 0) {
			if(expectedTypesPtr > -1) {
				next : for (int i = 0; i <= expectedTypesPtr; i++) {
					if(expectedTypes[i] instanceof ReferenceBinding) {
						ReferenceBinding refBinding = (ReferenceBinding)expectedTypes[i];
						boolean inSameUnit = unitScope.isDefinedInSameUnit(refBinding);
						
						// top level types of the current unit are already proposed.
						if(!inSameUnit || (inSameUnit && refBinding.isMemberType())) {
							char[] packageName = refBinding.qualifiedPackageName();
							char[] typeName = refBinding.sourceName();
							char[] completionName = typeName;
							
							boolean isQualified = false;
							if (!insideQualifiedReference && !refBinding.isMemberType()) {
								if (mustQualifyType(packageName, typeName)) {
									if (packageName == null || packageName.length == 0)
										if (unitScope != null && unitScope.fPackage.compoundName != CharOperation.NO_CHAR_CHAR)
											continue next; // ignore types from the default package from outside it
									completionName = CharOperation.concat(packageName, typeName, '.');
									isQualified = true;
								}
							}
							
							int relevance = computeBaseRelevance();
							relevance += computeRelevanceForInterestingProposal();
							relevance += computeRelevanceForCaseMatching(token, typeName);
							relevance += computeRelevanceForExpectingType(refBinding);
							relevance += computeRelevanceForQualification(isQualified);
							
							if(refBinding.isClass()) {
								relevance += computeRelevanceForClass();
								requestor.acceptClass(
									packageName,
									typeName,
									completionName,
									refBinding.modifiers,
									startPosition - offset, 
									endPosition - offset,
									relevance);
							} else if (refBinding.isInterface()) {
								relevance += computeRelevanceForInterface();
								requestor.acceptInterface(
									packageName,
									typeName,
									completionName,
									refBinding.modifiers,
									startPosition - offset, 
									endPosition - offset,
									relevance);
							}
						}
					}
				}
			} 
		} else {
			findKeywords(token, baseTypes);
			nameEnvironment.findTypes(token, this);
			nameEnvironment.findPackages(token, this);
		}
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
		
		if (unitScope != null) {
			int typeLength = qualifiedName.length;
			SourceTypeBinding[] types = unitScope.topLevelTypes;

			for (int i = 0, length = types.length; i < length; i++) {
				SourceTypeBinding sourceType = types[i]; 
	
				char[] qualifiedSourceTypeName = CharOperation.concatWith(sourceType.compoundName, '.');
				
				if (typeLength > qualifiedSourceTypeName.length)	continue;
				if (!CharOperation.prefixEquals(qualifiedName, qualifiedSourceTypeName, false))	continue;

				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(qualifiedName, qualifiedSourceTypeName);
				relevance += computeRelevanceForExpectingType(sourceType);
				relevance += computeRelevanceForQualification(false);

				if (sourceType.isClass()){
					relevance += computeRelevanceForClass();
					relevance += computeRelevanceForException(sourceType.sourceName);
					requestor.acceptClass(
						sourceType.qualifiedPackageName(),
						sourceType.sourceName(),
						sourceType.sourceName(),
						sourceType.modifiers,
						startPosition - offset, 
						endPosition - offset,
						relevance);
				} else {
					relevance += computeRelevanceForInterface();
					requestor.acceptInterface(
						sourceType.qualifiedPackageName(),
						sourceType.sourceName(),
						sourceType.sourceName(),
						sourceType.modifiers,
						startPosition - offset,
						endPosition - offset,
						relevance);
				}
			}
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
							if (CharOperation.equals(otherLocal.name, local.name, true))
								continue next;
						}
						localsFound.add(local);

						int relevance = computeBaseRelevance();
						relevance += computeRelevanceForInterestingProposal(local);
						relevance += computeRelevanceForCaseMatching(token, local.name);
						relevance += computeRelevanceForExpectingType(local.type);
						relevance += computeRelevanceForQualification(false);
						
						requestor.acceptLocalVariable(
							local.name,
							local.type == null 
								? CharOperation.NO_CHAR
								: local.type.qualifiedPackageName(),
							local.type == null
								? local.declaration.type.toString().toCharArray()
								: local.type.qualifiedSourceName(),
							local.modifiers,
							startPosition - offset,
							endPosition - offset,
							relevance);
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
	private void findVariableName(
		char[] token,
		char[] qualifiedPackageName,
		char[] qualifiedSourceName,
		char[] sourceName,
		char[][] excludeNames,
		int dim,
		int kind,
		int modifiers){
			
		if(sourceName == null || sourceName.length == 0)
			return;

		// compute variable name for non base type
		final char[] displayName;
		if (dim > 0){
			int l = qualifiedSourceName.length;
			displayName = new char[l+(2*dim)];
			System.arraycopy(qualifiedSourceName, 0, displayName, 0, l);
			for(int i = 0; i < dim; i++){
				displayName[l+(i*2)] = '[';
				displayName[l+(i*2)+1] = ']';
			}
		} else {
			displayName = qualifiedSourceName;
		}
		
		final char[] t = token;
		final char[] q = qualifiedPackageName;
		INamingRequestor namingRequestor = new INamingRequestor() {
			public void acceptNameWithPrefixAndSuffix(char[] name, boolean isFirstPrefix, boolean isFirstSuffix) {
				accept(	name,
					(isFirstPrefix ? R_NAME_FIRST_PREFIX : R_NAME_PREFIX) + (isFirstSuffix ? R_NAME_FIRST_SUFFIX : R_NAME_SUFFIX));
			}

			public void acceptNameWithPrefix(char[] name, boolean isFirstPrefix) {
				accept(name, isFirstPrefix ? R_NAME_FIRST_PREFIX :  R_NAME_PREFIX);
			}

			public void acceptNameWithSuffix(char[] name, boolean isFirstSuffix) {
				accept(name, isFirstSuffix ? R_NAME_FIRST_SUFFIX : R_NAME_SUFFIX);
			}

			public void acceptNameWithoutPrefixAndSuffix(char[] name) {
				accept(name, 0);
			}
			void accept(char[] name, int prefixAndSuffixRelevance){
				if (CharOperation.prefixEquals(t, name, false)) {
					int relevance = computeBaseRelevance();
					relevance += computeRelevanceForInterestingProposal();
					relevance += computeRelevanceForCaseMatching(t, name);
					relevance += prefixAndSuffixRelevance;

					// accept result
					requestor.acceptVariableName(
						q,
						displayName,
						name,
						name,
						startPosition - offset,
						endPosition - offset,
						relevance);
				}
			}
		};
		
		switch (kind) {
			case FIELD :
				InternalNamingConventions.suggestFieldNames(
					javaProject,
					qualifiedPackageName,
					qualifiedSourceName,
					dim,
					modifiers,
					excludeNames,
					namingRequestor);
				break;
			case LOCAL :
				InternalNamingConventions.suggestLocalVariableNames(
					javaProject,
					qualifiedPackageName,
					qualifiedSourceName,
					dim,
					excludeNames,
					namingRequestor);
				break;
			case ARGUMENT :
				InternalNamingConventions.suggestArgumentNames(
					javaProject,
					qualifiedPackageName,
					qualifiedSourceName,
					dim,
					excludeNames,
					namingRequestor);
				break;
		}
	}

	private void findVariableNames(char[] name, TypeReference type , char[][] excludeNames, int kind, int modifiers){

		if(type != null &&
			type.resolvedType != null &&
			type.resolvedType.problemId() == Binding.NoError){
			TypeBinding tb = type.resolvedType;
			findVariableName(
				name,
				tb.leafComponentType().qualifiedPackageName(),
				tb.leafComponentType().qualifiedSourceName(),
				tb.leafComponentType().sourceName(),
				excludeNames,
				type.dimensions(),
				kind,
				modifiers);
		}/*	else {
			char[][] typeName = type.getTypeName();
			findVariableName(
				name,
				NoChar,
				CharOperation.concatWith(typeName, '.'),
				typeName[typeName.length - 1],
				excludeNames,
				type.dimensions());
		}*/
	}
	
	public AssistParser getParser() {

		return parser;
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
	int computeBaseRelevance(){
		return R_DEFAULT;
	}
	private void computeExpectedTypes(AstNode parent, Scope scope){
		
		// default filter
		expectedTypesFilter = SUBTYPE;
		
		// find types from parent
		if(parent instanceof AbstractVariableDeclaration) {
			AbstractVariableDeclaration variable = (AbstractVariableDeclaration)parent;
			TypeBinding binding = variable.type.resolvedType;
			if(binding != null) {
				if(!(variable.initialization instanceof ArrayInitializer)) {
					addExpectedType(binding);
				}
			}
		} else if(parent instanceof Assignment) {
			TypeBinding binding = ((Assignment)parent).resolvedType;
			if(binding != null) {
				addExpectedType(binding);
			}
		} else if(parent instanceof ReturnStatement) {
			if(scope.methodScope().referenceContext instanceof AbstractMethodDeclaration) {
				MethodBinding methodBinding = ((AbstractMethodDeclaration) scope.methodScope().referenceContext).binding;
				TypeBinding binding = methodBinding  == null ? null : methodBinding.returnType;
				if(binding != null) {
					addExpectedType(binding);
				}
			}
		} else if(parent instanceof CastExpression) {
			Expression e = ((CastExpression)parent).type;
			TypeBinding binding = e.resolvedType;
			if(binding != null){
				addExpectedType(binding);
				expectedTypesFilter = SUBTYPE | SUPERTYPE;
			}
		} else if(parent instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) parent;

			if(messageSend.receiverType instanceof ReferenceBinding) {
				ReferenceBinding binding = (ReferenceBinding)messageSend.receiverType;
				boolean isStatic = messageSend.receiver.isTypeReference();
				
				while(binding != null) {	
					computeExpectedTypesForMessageSend(
						binding,
						messageSend.selector,
						messageSend.arguments,
						(ReferenceBinding)messageSend.receiverType,
						scope,
						messageSend,
						isStatic);
					computeExpectedTypesForMessageSendForInterface(
						binding,
						messageSend.selector,
						messageSend.arguments,
						(ReferenceBinding)messageSend.receiverType,
						scope,
						messageSend,
						isStatic);
					binding = binding.superclass();
				}
			}
		} else if(parent instanceof AllocationExpression) {
			AllocationExpression allocationExpression = (AllocationExpression) parent;
			
			ReferenceBinding binding = (ReferenceBinding)allocationExpression.type.resolvedType;

			if(binding != null) {	
				computeExpectedTypesForAllocationExpression(
					binding,
					allocationExpression.arguments,
					scope,
					allocationExpression);
			}
		} else if(parent instanceof OperatorExpression) {
			int operator = (parent.bits & AstNode.OperatorMASK) >> AstNode.OperatorSHIFT;
			if(parent instanceof ConditionalExpression) {
				// for future use
			} else if(parent instanceof InstanceOfExpression) {
				InstanceOfExpression e = (InstanceOfExpression) parent;
				TypeBinding binding = e.expression.resolvedType;
				if(binding != null){
					addExpectedType(binding);
					expectedTypesFilter = SUBTYPE | SUPERTYPE;
				}
			} else if(parent instanceof BinaryExpression) {
				switch(operator) {
					case OperatorIds.PLUS :
						addExpectedType(BaseTypes.ShortBinding);
						addExpectedType(BaseTypes.IntBinding);
						addExpectedType(BaseTypes.LongBinding);
						addExpectedType(BaseTypes.FloatBinding);
						addExpectedType(BaseTypes.DoubleBinding);
						addExpectedType(BaseTypes.CharBinding);
						addExpectedType(BaseTypes.ByteBinding);
						addExpectedType(scope.getJavaLangString());
						break;
					case OperatorIds.AND_AND :
					case OperatorIds.OR_OR :
					case OperatorIds.XOR :
						addExpectedType(BaseTypes.BooleanBinding);
						break;
					default :
						addExpectedType(BaseTypes.ShortBinding);
						addExpectedType(BaseTypes.IntBinding);
						addExpectedType(BaseTypes.LongBinding);
						addExpectedType(BaseTypes.FloatBinding);
						addExpectedType(BaseTypes.DoubleBinding);
						addExpectedType(BaseTypes.CharBinding);
						addExpectedType(BaseTypes.ByteBinding);
						break;
				}
			} else if(parent instanceof UnaryExpression) {
				switch(operator) {
					case OperatorIds.NOT :
						addExpectedType(BaseTypes.BooleanBinding);
						break;
					case OperatorIds.TWIDDLE :
						addExpectedType(BaseTypes.ShortBinding);
						addExpectedType(BaseTypes.IntBinding);
						addExpectedType(BaseTypes.LongBinding);
						addExpectedType(BaseTypes.CharBinding);
						addExpectedType(BaseTypes.ByteBinding);
						break;
					case OperatorIds.PLUS :
					case OperatorIds.MINUS :
					case OperatorIds.PLUS_PLUS :
					case OperatorIds.MINUS_MINUS :
						addExpectedType(BaseTypes.ShortBinding);
						addExpectedType(BaseTypes.IntBinding);
						addExpectedType(BaseTypes.LongBinding);
						addExpectedType(BaseTypes.FloatBinding);
						addExpectedType(BaseTypes.DoubleBinding);
						addExpectedType(BaseTypes.CharBinding);
						addExpectedType(BaseTypes.ByteBinding);
						break;
				}
			}
		} else if(parent instanceof ArrayReference) {
			addExpectedType(BaseTypes.ShortBinding);
			addExpectedType(BaseTypes.IntBinding);
			addExpectedType(BaseTypes.LongBinding);
		}
		
		if(expectedTypesPtr + 1 != expectedTypes.length) {
			System.arraycopy(expectedTypes, 0, expectedTypes = new TypeBinding[expectedTypesPtr + 1], 0, expectedTypesPtr + 1);
		}
	}
	
	private void computeExpectedTypesForAllocationExpression(
		ReferenceBinding binding,
		Expression[] arguments,
		Scope scope,
		InvocationSite invocationSite) {
			
		MethodBinding[] methods = binding.availableMethods();
		nextMethod : for (int i = 0; i < methods.length; i++) {
			MethodBinding method = methods[i];
			
			if (!method.isConstructor()) continue nextMethod;
			
			if (method.isSynthetic()) continue nextMethod;
			
			if (options.checkVisibility && !method.canBeSeenBy(invocationSite, scope)) continue nextMethod;
			
			TypeBinding[] parameters = method.parameters;
			if(parameters.length < arguments.length)
				continue nextMethod;
				
			int length = arguments.length - 1;
			
			for (int j = 0; j < length; j++) {
				Expression argument = arguments[j];
				TypeBinding argType = argument.resolvedType;
				if(argType != null && !argType.isCompatibleWith(parameters[j]))
					continue nextMethod;
			}
			
			TypeBinding expectedType = method.parameters[arguments.length - 1];
			if(expectedType != null) {
				addExpectedType(expectedType);
			}
		}
	}
	
	private void computeExpectedTypesForMessageSendForInterface(
		ReferenceBinding binding,
		char[] selector,
		Expression[] arguments,
		ReferenceBinding receiverType,
		Scope scope,
		InvocationSite invocationSite,
		boolean isStatic) {

		ReferenceBinding[] itsInterfaces = binding.superInterfaces();
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
	
						computeExpectedTypesForMessageSend(
							currentType,
							selector,
							arguments,
							receiverType,
							scope,
							invocationSite,
							isStatic);

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
	
	private void computeExpectedTypesForMessageSend(
		ReferenceBinding binding,
		char[] selector,
		Expression[] arguments,
		ReferenceBinding receiverType,
		Scope scope,
		InvocationSite invocationSite,
		boolean isStatic) {
			
		MethodBinding[] methods = binding.availableMethods();
		nextMethod : for (int i = 0; i < methods.length; i++) {
			MethodBinding method = methods[i];
			
			if (method.isSynthetic()) continue nextMethod;

			if (method.isDefaultAbstract())	continue nextMethod;

			if (method.isConstructor()) continue nextMethod;

			if (isStatic && !method.isStatic()) continue nextMethod;
			
			if (options.checkVisibility && !method.canBeSeenBy(receiverType, invocationSite, scope)) continue nextMethod;
			
			if(!CharOperation.equals(method.selector, selector)) continue nextMethod;
			
			TypeBinding[] parameters = method.parameters;
			if(parameters.length < arguments.length)
				continue nextMethod;
				
			int length = arguments.length - 1;
			
			for (int j = 0; j < length; j++) {
				Expression argument = arguments[j];
				TypeBinding argType = argument.resolvedType;
				if(argType != null && !argType.isCompatibleWith(parameters[j]))
					continue nextMethod;
			}
				
			TypeBinding expectedType = method.parameters[arguments.length - 1];
			if(expectedType != null) {
				addExpectedType(expectedType);
			}
		}
	}
	private void addExpectedType(TypeBinding type){
		if(type == null || !type.isValidBinding()) return;
		
		try {
			this.expectedTypes[++this.expectedTypesPtr] = type;
		} catch (IndexOutOfBoundsException e) {
			int oldLength = this.expectedTypes.length;
			TypeBinding oldTypes[] = this.expectedTypes;
			this.expectedTypes = new TypeBinding[oldLength * 2];
			System.arraycopy(oldTypes, 0, this.expectedTypes, 0, oldLength);
			this.expectedTypes[this.expectedTypesPtr] = type;
		}
	}
	private void addUninterestingBindings(Binding binding){
		if(binding == null) return;
		try {
			this.uninterestingBindings[++this.uninterestingBindingsPtr] = binding;
		} catch (IndexOutOfBoundsException e) {
			int oldLength = this.uninterestingBindings.length;
			Binding oldBindings[] = this.uninterestingBindings;
			this.uninterestingBindings = new Binding[oldLength * 2];
			System.arraycopy(oldBindings, 0, this.uninterestingBindings, 0, oldLength);
			this.uninterestingBindings[this.uninterestingBindingsPtr] = binding;
		}
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
}
