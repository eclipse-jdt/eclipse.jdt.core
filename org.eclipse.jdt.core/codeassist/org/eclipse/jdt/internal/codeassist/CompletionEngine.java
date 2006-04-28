/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.codeassist.complete.*;
import org.eclipse.jdt.internal.codeassist.impl.AssistParser;
import org.eclipse.jdt.internal.codeassist.impl.Engine;
import org.eclipse.jdt.internal.codeassist.impl.Keywords;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.INamingRequestor;
import org.eclipse.jdt.internal.core.InternalNamingConventions;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceMethodElementInfo;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.BinaryTypeConverter;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

/**
 * This class is the entry point for source completions.
 * It contains two public APIs used to call CodeAssist on a given source with
 * a given environment, assisting position and storage (and possibly options).
 */
public final class CompletionEngine
	extends Engine
	implements ISearchRequestor, TypeConstants , TerminalTokens , RelevanceConstants, SuffixConstants {
	
	public HashtableOfObject typeCache;
	
	public static boolean DEBUG = false;
	public static boolean PERF = false;
	
	// temporary constants to quickly disabled polish features if necessary
	public final static boolean NO_TYPE_COMPLETION_ON_EMPTY_TOKEN = true;
	public final static boolean PROPOSE_MEMBER_TYPES = true;
	
	private final static char[] ERROR_PATTERN = "*error*".toCharArray();  //$NON-NLS-1$
	private final static char[] EXCEPTION_PATTERN = "*exception*".toCharArray();  //$NON-NLS-1$
	private final static char[] SEMICOLON = new char[] { ';' };
	
	private final static char[] CLASS = "Class".toCharArray();  //$NON-NLS-1$
	private final static char[] VOID = "void".toCharArray();  //$NON-NLS-1$
	private final static char[] INT = "int".toCharArray();  //$NON-NLS-1$
	private final static char[] INT_SIGNATURE = new char[]{Signature.C_INT};
	private final static char[] VALUE = "value".toCharArray();  //$NON-NLS-1$
	private final static char[] EXTENDS = "extends".toCharArray();  //$NON-NLS-1$
	private final static char[] SUPER = "super".toCharArray();  //$NON-NLS-1$
	
	private final static int SUPERTYPE = 1;
	private final static int SUBTYPE = 2;
	
	private final static int FIELD = 0;
	private final static int LOCAL = 1;
	private final static int ARGUMENT = 2;

	int expectedTypesPtr = -1;
	TypeBinding[] expectedTypes = new TypeBinding[1];
	int expectedTypesFilter;
	boolean hasJavaLangObjectAsExpectedType = false;
	int uninterestingBindingsPtr = -1;
	Binding[] uninterestingBindings = new Binding[1];
	int forbbidenBindingsPtr = -1;
	Binding[] forbbidenBindings = new Binding[1];
	
	boolean assistNodeIsClass;
	boolean assistNodeIsEnum;
	boolean assistNodeIsException;
	boolean assistNodeIsInterface;
	boolean assistNodeIsAnnotation;
	boolean assistNodeIsConstructor;
	int  assistNodeInJavadoc = 0;
	boolean assistNodeCanBeSingleMemberAnnotation = false;
	
	IJavaProject javaProject;
	CompletionParser parser;
	CompletionRequestor requestor;
	ProblemReporter problemReporter;
	char[] source;
	char[] completionToken;
	char[] qualifiedCompletionToken;
	boolean resolvingImports = false;
	boolean resolvingStaticImports = false;
	boolean insideQualifiedReference = false;
	boolean noProposal = true;
	CategorizedProblem problem = null;
	char[] fileName = null;
	int startPosition, actualCompletionPosition, endPosition, offset;
	int javadocTagPosition; // Position of previous tag while completing in javadoc
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
	static final BaseTypeBinding[] BASE_TYPES = {
		TypeBinding.BOOLEAN,
		TypeBinding.BYTE,
		TypeBinding.CHAR,
		TypeBinding.DOUBLE,
		TypeBinding.FLOAT,
		TypeBinding.INT,
		TypeBinding.LONG,
		TypeBinding.SHORT,
		TypeBinding.VOID
	};
	static final int BASE_TYPES_LENGTH = BASE_TYPES.length;
	static final char[][] BASE_TYPE_NAMES = new char[BASE_TYPES_LENGTH][];
	static { 
		for (int i=0; i<BASE_TYPES_LENGTH; i++) {
			BASE_TYPE_NAMES[i] = BASE_TYPES[i].simpleName;
		}
	}
		
	static final char[] classField = "class".toCharArray();  //$NON-NLS-1$
	static final char[] lengthField = "length".toCharArray();  //$NON-NLS-1$
	static final char[] cloneMethod = "clone".toCharArray();  //$NON-NLS-1$
	static final char[] THIS = "this".toCharArray();  //$NON-NLS-1$
	static final char[] THROWS = "throws".toCharArray();  //$NON-NLS-1$
	
	static InvocationSite FakeInvocationSite = new InvocationSite(){
		public TypeBinding[] genericTypeArguments() { return null; }
		public boolean isSuperAccess(){ return false; }
		public boolean isTypeAccess(){ return false; }
		public void setActualReceiverType(ReferenceBinding receiverType) {}
		public void setDepth(int depth){}
		public void setFieldIndex(int depth){}
		public int sourceStart() { return 0; 	}
		public int sourceEnd() { return 0; 	}
	};
	
	private class AcceptedType {
		public AcceptedType(
			char[] packageName,
			char[] simpleTypeName,
			char[][] enclosingTypeNames,
			int modifiers,
			int accessibility) {
			this.packageName = packageName;
			this.simpleTypeName = simpleTypeName;
			this.enclosingTypeNames = enclosingTypeNames;
			this.modifiers = modifiers;
			this.accessibility = accessibility;
		}
		public char[] packageName;
		public char[] simpleTypeName;
		public char[][] enclosingTypeNames;
		public int modifiers;
		public int accessibility;
		
		public boolean mustBeQualified = false;
		public char[] fullyQualifiedName = null;
		public char[] qualifiedTypeName = null;
		
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append('{');
			buffer.append(packageName);
			buffer.append(',');
			buffer.append(simpleTypeName);
			buffer.append(',');
			buffer.append(CharOperation.concatWith(enclosingTypeNames, '.'));
			buffer.append('}');
			return buffer.toString();
		}
	}
	
	private ObjectVector acceptedTypes;

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
			SearchableEnvironment nameEnvironment,
			CompletionRequestor requestor,
			Map settings,
			IJavaProject javaProject) {
		super(settings);
		this.javaProject = javaProject;
		this.requestor = requestor;
		this.nameEnvironment = nameEnvironment;
		this.typeCache = new HashtableOfObject(5);

		this.problemReporter = new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.compilerOptions,
				new DefaultProblemFactory(Locale.getDefault()) {
					int lastErrorStart;
					
					public CategorizedProblem createProblem(
						char[] originatingFileName,
						int problemId,
						String[] problemArguments,
						String[] messageArguments,
						int severity,
						int start,
						int end,
						int lineNumber) {
						
						CategorizedProblem pb = super.createProblem(
							originatingFileName,
							problemId,
							problemArguments,
							messageArguments,
							severity,
							start,
							end,
							lineNumber);
						if(CompletionEngine.this.actualCompletionPosition > start
							&& this.lastErrorStart < start
							&& pb.isError()
							&& (pb.getID() & IProblem.Syntax) == 0
							&& (CompletionEngine.this.fileName == null || CharOperation.equals(CompletionEngine.this.fileName, originatingFileName))) {
								
							CompletionEngine.this.problem = pb;
							this.lastErrorStart = start;
						}
						
						return pb;
					}

				});
		this.lookupEnvironment =
			new LookupEnvironment(this, this.compilerOptions, this.problemReporter, nameEnvironment);
		this.parser =
			new CompletionParser(this.problemReporter);
		this.nameScanner =
			new Scanner(
				false /*comment*/, 
				false /*whitespace*/, 
				false /*nls*/, 
				this.compilerOptions.sourceLevel, 
				null /*taskTags*/, 
				null/*taskPriorities*/,
				true/*taskCaseSensitive*/);
	}

	/**
	 * One result of the search consists of a new type.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.I".
	 *    The default package is represented by an empty array.
	 */
	public void acceptType(
		char[] packageName,
		char[] simpleTypeName,
		char[][] enclosingTypeNames,
		int modifiers,
		AccessRestriction accessRestriction) {

		if (this.options.checkDeprecation && (modifiers & ClassFileConstants.AccDeprecated) != 0) return;
		
		if (this.options.checkVisibility) {
			if((modifiers & ClassFileConstants.AccPublic) == 0) {
				if((modifiers & ClassFileConstants.AccPrivate) != 0) return;
				
				char[] currentPackage = CharOperation.concatWith(this.unitScope.fPackage.compoundName, '.');
				if(!CharOperation.equals(packageName, currentPackage)) return;
			}
		}
		
		int accessibility = IAccessRule.K_ACCESSIBLE;
		if(accessRestriction != null) {
			switch (accessRestriction.getProblemId()) {
				case IProblem.ForbiddenReference:
					if (this.options.checkForbiddenReference) {
						return;
					}
					accessibility = IAccessRule.K_NON_ACCESSIBLE;
					break;
				case IProblem.DiscouragedReference:
					if (this.options.checkDiscouragedReference) {
						return;
					}
					accessibility = IAccessRule.K_DISCOURAGED;
					break;
			}
		}
		
		if(acceptedTypes == null) {
			acceptedTypes = new ObjectVector();
		}
		acceptedTypes.add(new AcceptedType(packageName, simpleTypeName, enclosingTypeNames, modifiers, accessibility));
	}
	
	private void acceptTypes() {
		if(this.acceptedTypes == null) return;
		
		int length = this.acceptedTypes.size();
		
		if(length == 0) return;
		
		HashtableOfObject onDemandFound = new HashtableOfObject();
		
		next : for (int i = 0; i < length; i++) {
			AcceptedType acceptedType = (AcceptedType)this.acceptedTypes.elementAt(i);
			char[] packageName = acceptedType.packageName;
			char[] simpleTypeName = acceptedType.simpleTypeName;
			char[][] enclosingTypeNames = acceptedType.enclosingTypeNames;
			int modifiers = acceptedType.modifiers;
			int accessibility = acceptedType.accessibility;
			
			char[] typeName;
			char[] flatEnclosingTypeNames;
			if(enclosingTypeNames == null || enclosingTypeNames.length == 0) {
				flatEnclosingTypeNames = null;
				typeName = simpleTypeName;
			} else {
				flatEnclosingTypeNames = CharOperation.concatWith(acceptedType.enclosingTypeNames, '.');
				typeName = CharOperation.concat(flatEnclosingTypeNames, simpleTypeName, '.');
			}
			char[] fullyQualifiedName = CharOperation.concat(packageName, typeName, '.');
			
			if (this.knownTypes.containsKey(fullyQualifiedName)) continue next;
	
			this.knownTypes.put(fullyQualifiedName, this);
			
			if (this.resolvingImports) {
				char[] completionName;
				
				if(this.resolvingStaticImports) {
					if(enclosingTypeNames == null || enclosingTypeNames.length == 0) {
						completionName = CharOperation.concat(fullyQualifiedName, new char[] { '.' });
					} else if ((modifiers & ClassFileConstants.AccStatic) == 0) {
						continue next;
					} else {
						completionName = CharOperation.concat(fullyQualifiedName, new char[] { ';' });
					}
				} else {
					completionName = CharOperation.concat(fullyQualifiedName, new char[] { ';' });
				}
				
				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForRestrictions(accessibility);
				if(insideQualifiedReference) {
					relevance += computeRelevanceForCaseMatching(this.completionToken, fullyQualifiedName);
				} else {
					relevance += computeRelevanceForCaseMatching(this.completionToken, simpleTypeName);
				}
				
				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
					createTypeProposal(packageName, typeName, modifiers, accessibility, completionName, relevance);
				}
			} else {
				if(!this.importCachesInitialized) {
					this.initializeImportCaches();
				}
			
				for (int j = 0; j < this.importCacheCount; j++) {
					char[][] importName = this.importsCache[j];
					if(CharOperation.equals(typeName, importName[0])) {
						proposeType(
								packageName,
								simpleTypeName,
								modifiers,
								accessibility,
								typeName,
								fullyQualifiedName,
								!CharOperation.equals(fullyQualifiedName, importName[1]));
						continue next;
					}
				}
				
				
				if ((enclosingTypeNames == null || enclosingTypeNames.length == 0 ) && CharOperation.equals(this.currentPackageName, packageName)) {
					proposeType(
							packageName,
							simpleTypeName,
							modifiers,
							accessibility,
							typeName,
							fullyQualifiedName,
							false);
					continue next;
				} else {
					char[] fullyQualifiedEnclosingTypeOrPackageName = null;
			
					AcceptedType foundType = null;
					if((foundType = (AcceptedType)onDemandFound.get(simpleTypeName)) == null) {
						for (int j = 0; j < this.onDemandImportCacheCount; j++) {
							ImportBinding importBinding = this.onDemandImportsCache[j];

							char[][] importName = importBinding.compoundName;
							char[] importFlatName = CharOperation.concatWith(importName, '.');
						
							if(fullyQualifiedEnclosingTypeOrPackageName == null) {
								if(enclosingTypeNames != null && enclosingTypeNames.length != 0) {
									fullyQualifiedEnclosingTypeOrPackageName =
										CharOperation.concat(
												packageName,
												flatEnclosingTypeNames,
												'.');
								} else {
									fullyQualifiedEnclosingTypeOrPackageName =
										packageName;
								}
							}
							if(CharOperation.equals(fullyQualifiedEnclosingTypeOrPackageName, importFlatName)) {
								if(importBinding.isStatic()) {
									if((modifiers & ClassFileConstants.AccStatic) != 0) {
										acceptedType.qualifiedTypeName = typeName;
										acceptedType.fullyQualifiedName = fullyQualifiedName;
										onDemandFound.put(
												simpleTypeName,
												acceptedType);
										continue next;
									}
								} else {
									acceptedType.qualifiedTypeName = typeName;
									acceptedType.fullyQualifiedName = fullyQualifiedName;
									onDemandFound.put(
											simpleTypeName,
											acceptedType);
									continue next;
								}
							}
						}
					} else if(!foundType.mustBeQualified){
						done : for (int j = 0; j < this.onDemandImportCacheCount; j++) {
							ImportBinding importBinding = this.onDemandImportsCache[j];

							char[][] importName = importBinding.compoundName;
							char[] importFlatName = CharOperation.concatWith(importName, '.');
						
							if(fullyQualifiedEnclosingTypeOrPackageName == null) {
								if(enclosingTypeNames != null && enclosingTypeNames.length != 0) {
									fullyQualifiedEnclosingTypeOrPackageName =
										CharOperation.concat(
												packageName,
												flatEnclosingTypeNames,
												'.');
								} else {
									fullyQualifiedEnclosingTypeOrPackageName =
										packageName;
								}
							}
							if(CharOperation.equals(fullyQualifiedEnclosingTypeOrPackageName, importFlatName)) {
								if(importBinding.isStatic()) {
									if((modifiers & ClassFileConstants.AccStatic) != 0) {
										foundType.mustBeQualified = true;
										break done;
									}
								} else {
									foundType.mustBeQualified = true;
									break done;
								}
							}
						}
					}
					proposeType(
							packageName,
							simpleTypeName,
							modifiers,
							accessibility,
							typeName,
							fullyQualifiedName,
							true);
				}
			}
		}
		char[][] keys = onDemandFound.keyTable;
		Object[] values = onDemandFound.valueTable;
		int max = keys.length;
		for (int i = 0; i < max; i++) {
			if(keys[i] != null) {
				AcceptedType value = (AcceptedType) values[i];
				if(value != null) {
					proposeType(
							value.packageName,
							value.simpleTypeName,
							value.modifiers,
							value.accessibility,
							value.qualifiedTypeName,
							value.fullyQualifiedName,
							value.mustBeQualified);
				}
			}
		}
		this.acceptedTypes = null; // reset
	}

	private void proposeType(char[] packageName, char[] simpleTypeName, int modifiers, int accessibility, char[] typeName, char[] fullyQualifiedName, boolean isQualified) {
		if(PROPOSE_MEMBER_TYPES) {
			if(this.assistNodeIsClass) {
				if((modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0 ) return;
			} else if(this.assistNodeIsInterface) {
				if((modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) == 0) return;
			} else if (this.assistNodeIsAnnotation) {
				if((modifiers & ClassFileConstants.AccAnnotation) == 0) return;
			}
		}
		
		char[] completionName = fullyQualifiedName;
		if(isQualified) {
			if (packageName == null || packageName.length == 0)
				if (this.unitScope != null && this.unitScope.fPackage.compoundName != CharOperation.NO_CHAR_CHAR)
					return; // ignore types from the default package from outside it
		} else {
			completionName = simpleTypeName;
		}

		int relevance = computeBaseRelevance();
		relevance += computeRelevanceForInterestingProposal();
		relevance += computeRelevanceForRestrictions(accessibility);
		relevance += computeRelevanceForCaseMatching(this.completionToken, simpleTypeName);
		relevance += computeRelevanceForExpectingType(packageName, simpleTypeName);
		relevance += computeRelevanceForQualification(isQualified);
		
		int kind = modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccEnum | ClassFileConstants.AccAnnotation);
		switch (kind) {
			case ClassFileConstants.AccAnnotation:
			case ClassFileConstants.AccAnnotation | ClassFileConstants.AccInterface:
				relevance += computeRelevanceForAnnotation();
				relevance += computeRelevanceForInterface();
				break;
			case ClassFileConstants.AccEnum:
				relevance += computeRelevanceForEnum();
				break;
			case ClassFileConstants.AccInterface:
				relevance += computeRelevanceForInterface();
				break;
			default:
				relevance += computeRelevanceForClass();
				relevance += computeRelevanceForException(simpleTypeName);
				break;
		}
		
		this.noProposal = false;
		if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
			createTypeProposal(packageName, typeName, modifiers, accessibility, completionName, relevance);
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

		if (this.knownPkgs.containsKey(packageName)) return;

		this.knownPkgs.put(packageName, this);
		
		char[] completion;
		if(this.resolvingImports) {
			if(this.resolvingStaticImports) {
				completion = CharOperation.concat(packageName, new char[] { '.' });
			} else {
				completion = CharOperation.concat(packageName, new char[] { '.', '*', ';' });
			}
		} else {
			completion = packageName;
		}
								
		int relevance = computeBaseRelevance();
		relevance += computeRelevanceForInterestingProposal();
		relevance += computeRelevanceForCaseMatching(this.qualifiedCompletionToken == null ? this.completionToken : this.qualifiedCompletionToken, packageName);
		if(!this.resolvingImports) {
			relevance += computeRelevanceForQualification(true);
		}
		relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
		
		this.noProposal = false;
		if(!this.requestor.isIgnored(CompletionProposal.PACKAGE_REF)) {
			CompletionProposal proposal = this.createProposal(CompletionProposal.PACKAGE_REF, this.actualCompletionPosition);
			proposal.setDeclarationSignature(packageName);
			proposal.setPackageName(packageName);
			proposal.setCompletion(completion);
			proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance);
			this.requestor.accept(proposal);
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
	}
		
	private void buildContext(
			ASTNode astNode,
			ASTNode astNodeParent,
			Binding qualifiedBinding,
			Scope scope) {
		CompletionContext context = new CompletionContext();
		
		// build expected types context
		if (this.expectedTypesPtr > -1) {
			int length = this.expectedTypesPtr + 1;
			char[][] expTypes = new char[length][];
			char[][] expKeys = new char[length][];
			for (int i = 0; i < length; i++) {
				expTypes[i] = getSignature(this.expectedTypes[i]);
				expKeys[i] = this.expectedTypes[i].computeUniqueKey();
			}
			context.setExpectedTypesSignatures(expTypes);
			context.setExpectedTypesKeys(expKeys);
		}
		
		context.setOffset(this.actualCompletionPosition + 1 - this.offset);
		
		// Set javadoc info
		if (astNode instanceof CompletionOnJavadoc) {
			this.assistNodeInJavadoc = ((CompletionOnJavadoc)astNode).getCompletionFlags();
			context.setJavadoc(this.assistNodeInJavadoc);
		}
		
		if (!(astNode instanceof CompletionOnJavadoc)) {
			CompletionScanner scanner = (CompletionScanner)this.parser.scanner;
			context.setToken(scanner.completionIdentifier);
			context.setTokenRange(
					scanner.completedIdentifierStart - this.offset,
					scanner.completedIdentifierEnd - this.offset,
					scanner.endOfEmptyToken - this.offset);
		} else if(astNode instanceof CompletionOnJavadocTag) {
			CompletionOnJavadocTag javadocTag = (CompletionOnJavadocTag) astNode;
			context.setToken(CharOperation.concat(new char[]{'@'}, javadocTag.token));
			context.setTokenRange(
					javadocTag.tagSourceStart - this.offset,
					javadocTag.tagSourceEnd - this.offset,
					((CompletionScanner)this.parser.javadocParser.scanner).endOfEmptyToken - this.offset);
		} else {
			CompletionScanner scanner = (CompletionScanner)this.parser.javadocParser.scanner;
			context.setToken(scanner.completionIdentifier);
			context.setTokenRange(
					scanner.completedIdentifierStart - this.offset,
					scanner.completedIdentifierEnd - this.offset,
					scanner.endOfEmptyToken - this.offset);
		}
		
		if(astNode instanceof CompletionOnStringLiteral) {
			context.setTokenKind(CompletionContext.TOKEN_KIND_STRING_LITERAL);
		} else {
			context.setTokenKind(CompletionContext.TOKEN_KIND_NAME);
		}
		
		if(DEBUG) {
			System.out.println(context.toString());
		}
		this.requestor.acceptContext(context);
	}
	
	private boolean complete(ASTNode astNode, ASTNode astNodeParent, Binding qualifiedBinding, Scope scope, boolean insideTypeAnnotation) {

		setSourceRange(astNode.sourceStart, astNode.sourceEnd);

		scope = computeForbiddenBindings(astNode, astNodeParent, scope);
		computeUninterestingBindings(astNodeParent, scope);
		if(astNodeParent != null) {
			if(!isValidParent(astNodeParent, astNode, scope)) return false;
			computeExpectedTypes(astNodeParent, astNode, scope);
		}
		
		buildContext(astNode, astNodeParent, qualifiedBinding, scope);
		
		if (astNode instanceof CompletionOnFieldType) {

			CompletionOnFieldType field = (CompletionOnFieldType) astNode;
			CompletionOnSingleTypeReference type = (CompletionOnSingleTypeReference) field.type;
			this.completionToken = type.token;
			setSourceRange(type.sourceStart, type.sourceEnd);
			
			findTypesAndPackages(this.completionToken, scope);
			if (!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
				findKeywordsForMember(this.completionToken, field.modifiers);
			}
			
			if (!field.isLocalVariable && field.modifiers == ClassFileConstants.AccDefault) {
				SourceTypeBinding enclosingType = scope.enclosingSourceType();
				if (!enclosingType.isAnnotationType()) {
					if (!this.requestor.isIgnored(CompletionProposal.METHOD_DECLARATION)) {
						findMethods(this.completionToken,null,null,enclosingType,scope,new ObjectVector(),false,false,true,null,null,false,false,true);
					}
					if (!this.requestor.isIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION)) {
						proposeNewMethod(this.completionToken, enclosingType);
					}
				}
			}
		} else if (astNode instanceof CompletionOnMethodReturnType) {

			CompletionOnMethodReturnType method = (CompletionOnMethodReturnType) astNode;
			SingleTypeReference type = (CompletionOnSingleTypeReference) method.returnType;
			this.completionToken = type.token;
			setSourceRange(type.sourceStart, type.sourceEnd);
			findTypesAndPackages(this.completionToken, scope.parent);
			if (!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
				findKeywordsForMember(this.completionToken, method.modifiers);
			}

			if (method.modifiers == ClassFileConstants.AccDefault) {
				SourceTypeBinding enclosingType = scope.enclosingSourceType();
				if (!enclosingType.isAnnotationType()) {
					if (!this.requestor.isIgnored(CompletionProposal.METHOD_DECLARATION)) {
						findMethods(this.completionToken,null,null,scope.enclosingSourceType(),scope,new ObjectVector(),false,false,true,null,null,false,false,true);
					}
					if (!this.requestor.isIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION)) {
						proposeNewMethod(this.completionToken, scope.enclosingSourceType());
					}
				}
			}
		} else if (astNode instanceof CompletionOnSingleNameReference) {

			CompletionOnSingleNameReference singleNameReference = (CompletionOnSingleNameReference) astNode;
			this.completionToken = singleNameReference.token;
			SwitchStatement switchStatement = astNodeParent instanceof SwitchStatement ? (SwitchStatement) astNodeParent : null;
			if (switchStatement != null
					&& switchStatement.expression.resolvedType != null
					&& switchStatement.expression.resolvedType.isEnum()) {
				if (!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
					this.assistNodeIsEnum = true;
					this.findEnumConstant(this.completionToken, (SwitchStatement) astNodeParent);
				}
			} else if (this.expectedTypesPtr > -1 && this.expectedTypes[0].isAnnotationType()) {
				findTypesAndPackages(this.completionToken, scope);
			} else {
				findVariablesAndMethods(
					this.completionToken,
					scope,
					singleNameReference,
					scope,
					insideTypeAnnotation,
					singleNameReference.isInsideAnnotationAttribute);
				// can be the start of a qualified type name
				findTypesAndPackages(this.completionToken, scope);
				if (!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
					if (this.completionToken != null && this.completionToken.length != 0) {
						findKeywords(this.completionToken, singleNameReference.possibleKeywords, false);
					} else {
						findTrueOrFalseKeywords(singleNameReference.possibleKeywords);
					}
				}
				if (singleNameReference.canBeExplicitConstructor && !this.requestor.isIgnored(CompletionProposal.METHOD_REF)){
					if (CharOperation.prefixEquals(this.completionToken, Keywords.THIS, false)) {
						ReferenceBinding ref = scope.enclosingSourceType();
						findExplicitConstructors(Keywords.THIS, ref, (MethodScope)scope, singleNameReference);
					} else if (CharOperation.prefixEquals(this.completionToken, Keywords.SUPER, false)) {
						ReferenceBinding ref = scope.enclosingSourceType();
						findExplicitConstructors(Keywords.SUPER, ref.superclass(), (MethodScope)scope, singleNameReference);
					}
				}
			}

		} else if (astNode instanceof CompletionOnSingleTypeReference) {

			CompletionOnSingleTypeReference singleRef = (CompletionOnSingleTypeReference) astNode;
			
			this.completionToken = singleRef.token;

			this.assistNodeIsClass = singleRef.isClass();
			this.assistNodeIsException = singleRef.isException();
			this.assistNodeIsInterface = singleRef.isInterface();
			this.assistNodeIsConstructor = singleRef.isConstructorType;

			// can be the start of a qualified type name
			if (qualifiedBinding == null) {
				if (this.completionToken.length == 0 &&
						(astNodeParent instanceof ParameterizedSingleTypeReference ||
								astNodeParent instanceof ParameterizedQualifiedTypeReference)) {
					this.setSourceRange(astNode.sourceStart, astNode.sourceStart - 1, false);
					
					findParameterizedType((TypeReference)astNodeParent, scope);
				} else { 
					findTypesAndPackages(this.completionToken, scope);
				}
			} else if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
				findMemberTypes(
					this.completionToken,
					(ReferenceBinding) qualifiedBinding,
					scope,
					scope.enclosingSourceType(),
					false,
					false,
					false,
					!this.assistNodeIsConstructor,
					null,
					new ObjectVector());
			}
		} else if (astNode instanceof CompletionOnQualifiedNameReference) {

			this.insideQualifiedReference = true;
			CompletionOnQualifiedNameReference ref =
				(CompletionOnQualifiedNameReference) astNode;
			this.completionToken = ref.completionIdentifier;
			long completionPosition = ref.sourcePositions[ref.sourcePositions.length - 1];

			if (qualifiedBinding instanceof VariableBinding) {

				setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
				TypeBinding receiverType = ((VariableBinding) qualifiedBinding).type;
				if (receiverType != null) {
					findFieldsAndMethods(this.completionToken, receiverType.capture(scope, ref.sourceEnd), scope, ref, scope,false,false);
				}

			} else if (qualifiedBinding instanceof ReferenceBinding && !(qualifiedBinding instanceof TypeVariableBinding)) {
				boolean isInsideAnnotationAttribute = ref.isInsideAnnotationAttribute;
				ReferenceBinding receiverType = (ReferenceBinding) qualifiedBinding;
				setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);

				if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
					findMemberTypes(
							this.completionToken,
							receiverType,
							scope,
							scope.enclosingSourceType(),
							false,
							new ObjectVector());
				}
				if (!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
					findClassField(this.completionToken, (TypeBinding) qualifiedBinding, scope);
				}
				
				MethodScope methodScope = null;
				if (!isInsideAnnotationAttribute &&
						!this.requestor.isIgnored(CompletionProposal.KEYWORD) &&
						((scope instanceof MethodScope && !((MethodScope)scope).isStatic)
						|| ((methodScope = scope.enclosingMethodScope()) != null && !methodScope.isStatic))) {
					if (this.completionToken.length > 0) {
						findKeywords(this.completionToken, new char[][]{Keywords.THIS}, false);
					} else {
						int relevance = computeBaseRelevance();
						relevance += computeRelevanceForInterestingProposal();
						relevance += computeRelevanceForCaseMatching(this.completionToken, Keywords.THIS);
						relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for keywords
						this.noProposal = false;
						if (!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
							CompletionProposal proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition);
							proposal.setName(Keywords.THIS);
							proposal.setCompletion(Keywords.THIS);
							proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
							proposal.setRelevance(relevance);
							this.requestor.accept(proposal);
							if (DEBUG) {
								this.printDebug(proposal);
							}
						}
					}
				}

				if (!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
					findFields(
						this.completionToken,
						receiverType,
						scope,
						new ObjectVector(),
						new ObjectVector(),
						true,
						ref,
						scope,
						false,
						true);
				}

				if (!isInsideAnnotationAttribute && !this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
					findMethods(
						this.completionToken,
						null,
						null,
						receiverType,
						scope,
						new ObjectVector(),
						true,
						false,
						false,
						ref,
						scope,
						false,
						false,
						true);
				}

			} else if (qualifiedBinding instanceof PackageBinding) {

				setSourceRange(astNode.sourceStart, (int) completionPosition);
				// replace to the end of the completion identifier
				findTypesAndSubpackages(this.completionToken, (PackageBinding) qualifiedBinding, scope);
			}
		} else if (astNode instanceof CompletionOnQualifiedTypeReference) {

			this.insideQualifiedReference = true;
			
			CompletionOnQualifiedTypeReference ref =
				(CompletionOnQualifiedTypeReference) astNode;
			
			this.assistNodeIsClass = ref.isClass();
			this.assistNodeIsException = ref.isException();
			this.assistNodeIsInterface = ref.isInterface();
			
			this.completionToken = ref.completionIdentifier;
			long completionPosition = ref.sourcePositions[ref.tokens.length];

			// get the source positions of the completion identifier
			if (qualifiedBinding instanceof ReferenceBinding && !(qualifiedBinding instanceof TypeVariableBinding)) {
				if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
					setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
					findMemberTypes(
						this.completionToken,
						(ReferenceBinding) qualifiedBinding,
						scope,
						scope.enclosingSourceType(),
						false,
						new ObjectVector());
				}
			} else if (qualifiedBinding instanceof PackageBinding) {

				setSourceRange(astNode.sourceStart, (int) completionPosition);
				// replace to the end of the completion identifier
				findTypesAndSubpackages(this.completionToken, (PackageBinding) qualifiedBinding, scope);
			}
		} else if (astNode instanceof CompletionOnMemberAccess) {
			this.insideQualifiedReference = true;
			CompletionOnMemberAccess access = (CompletionOnMemberAccess) astNode;
			long completionPosition = access.nameSourcePosition;
			setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);

			this.completionToken = access.token;
			
			if (!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
				findKeywords(this.completionToken, new char[][]{Keywords.NEW}, false);
			}
			
			findFieldsAndMethods(
				this.completionToken,
				((TypeBinding) qualifiedBinding).capture(scope, access.receiver.sourceEnd),
				scope,
				access,
				scope,
				false,
				access.receiver instanceof SuperReference);

		} else if (astNode instanceof CompletionOnMessageSend) {
			setSourceRange(astNode.sourceStart, astNode.sourceEnd, false);
			
			CompletionOnMessageSend messageSend = (CompletionOnMessageSend) astNode;
			TypeBinding[] argTypes = computeTypes(messageSend.arguments);
			this.completionToken = messageSend.selector;
			if (qualifiedBinding == null) {
				if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
					findImplicitMessageSends(this.completionToken, argTypes, scope, messageSend, scope);
				}
			} else  if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
				findMethods(
					this.completionToken,
					null,
					argTypes,
					(ReferenceBinding)((ReferenceBinding) qualifiedBinding).capture(scope, messageSend.receiver.sourceEnd),
					scope,
					new ObjectVector(),
					false,
					true,
					false,
					messageSend,
					scope,
					false,
					messageSend.receiver instanceof SuperReference,
					true);
			}
		} else if (astNode instanceof CompletionOnExplicitConstructorCall) {
			if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
				setSourceRange(astNode.sourceStart, astNode.sourceEnd, false);
				
				CompletionOnExplicitConstructorCall constructorCall =
					(CompletionOnExplicitConstructorCall) astNode;
				TypeBinding[] argTypes = computeTypes(constructorCall.arguments);
				findConstructors(
					(ReferenceBinding) qualifiedBinding,
					argTypes,
					scope,
					constructorCall,
					false);
									}
		} else if (astNode instanceof CompletionOnQualifiedAllocationExpression) {
			setSourceRange(astNode.sourceStart, astNode.sourceEnd, false);
			
			CompletionOnQualifiedAllocationExpression allocExpression =
				(CompletionOnQualifiedAllocationExpression) astNode;
			TypeBinding[] argTypes = computeTypes(allocExpression.arguments);
			
			ReferenceBinding ref = (ReferenceBinding) qualifiedBinding;
			if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)
					&& ref.isClass()
					&& !ref.isAbstract()) {
					findConstructors(
						ref,
						argTypes,
						scope,
						allocExpression,
						false);
			}
			if (!this.requestor.isIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION)
					&& !ref.isFinal()
					&& !ref.isEnum()){
				findAnonymousType(
					ref,
					argTypes,
					scope,
					allocExpression);
			}
		} else if (astNode instanceof CompletionOnClassLiteralAccess) {
			if (!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
				CompletionOnClassLiteralAccess access = (CompletionOnClassLiteralAccess) astNode;
				setSourceRange(access.classStart, access.sourceEnd);

				this.completionToken = access.completionIdentifier;

				findClassField(this.completionToken, (TypeBinding) qualifiedBinding, scope);
			}
		} else if (astNode instanceof CompletionOnMethodName) {
			if (!this.requestor.isIgnored(CompletionProposal.VARIABLE_DECLARATION)) {
				CompletionOnMethodName method = (CompletionOnMethodName) astNode;
					
				setSourceRange(method.sourceStart, method.selectorEnd);
					
				FieldBinding[] fields = scope.enclosingSourceType().fields();
				char[][] excludeNames = new char[fields.length][];
				for(int i = 0 ; i < fields.length ; i++){
					excludeNames[i] = fields[i].name;
				}
				
				this.completionToken = method.selector;
				
				findVariableNames(this.completionToken, method.returnType, excludeNames, FIELD, method.modifiers);
			}
		} else if (astNode instanceof CompletionOnFieldName) {
			if (!this.requestor.isIgnored(CompletionProposal.VARIABLE_DECLARATION)) {
				CompletionOnFieldName field = (CompletionOnFieldName) astNode;
				
				FieldBinding[] fields = scope.enclosingSourceType().fields();
				char[][] excludeNames = new char[fields.length][];
				for(int i = 0 ; i < fields.length ; i++){
					excludeNames[i] = fields[i].name;
				}
				
				this.completionToken = field.realName;
				
				findVariableNames(field.realName, field.type, excludeNames, FIELD, field.modifiers);
			}
		} else if (astNode instanceof CompletionOnLocalName || astNode instanceof CompletionOnArgumentName) {
			if (!this.requestor.isIgnored(CompletionProposal.VARIABLE_DECLARATION)) {
				LocalDeclaration variable = (LocalDeclaration) astNode;
				
				LocalVariableBinding[] locals = ((BlockScope)scope).locals;
				char[][] excludeNames = new char[locals.length][];
				int localCount = 0;
				for(int i = 0 ; i < locals.length ; i++){
					if (locals[i] != null) {
						excludeNames[localCount++] = locals[i].name;
					}
				}
				System.arraycopy(excludeNames, 0, excludeNames = new char[localCount][], 0, localCount);
				
				if (variable instanceof CompletionOnLocalName){
					this.completionToken = ((CompletionOnLocalName) variable).realName;
					findVariableNames(this.completionToken, variable.type, excludeNames, LOCAL, variable.modifiers);
				} else {
					CompletionOnArgumentName arg = (CompletionOnArgumentName) variable;
					this.completionToken = arg.realName;
					findVariableNames(this.completionToken, variable.type, excludeNames, arg.isCatchArgument ? LOCAL : ARGUMENT, variable.modifiers);
				}
			}
		} else if (astNode instanceof CompletionOnKeyword) {
			if (!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
				CompletionOnKeyword keyword = (CompletionOnKeyword)astNode;
				findKeywords(keyword.getToken(), keyword.getPossibleKeywords(), keyword.canCompleteEmptyToken());
			}
		} else if (astNode instanceof CompletionOnParameterizedQualifiedTypeReference) {
			if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
				CompletionOnParameterizedQualifiedTypeReference ref = (CompletionOnParameterizedQualifiedTypeReference) astNode;
				
				this.insideQualifiedReference = true;

				this.assistNodeIsClass = ref.isClass();
				this.assistNodeIsException = ref.isException();
				this.assistNodeIsInterface = ref.isInterface();
				
				this.completionToken = ref.completionIdentifier;
				long completionPosition = ref.sourcePositions[ref.tokens.length];
				setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
				findMemberTypes(
					this.completionToken,
					(ReferenceBinding) qualifiedBinding,
					scope,
					scope.enclosingSourceType(),
					false,
					new ObjectVector());
			}
		} else if (astNode instanceof CompletionOnMarkerAnnotationName) {
			CompletionOnMarkerAnnotationName annot = (CompletionOnMarkerAnnotationName) astNode;
			
			this.assistNodeIsAnnotation = true;
			if (annot.type instanceof CompletionOnSingleTypeReference) {
				CompletionOnSingleTypeReference type = (CompletionOnSingleTypeReference) annot.type;
				this.completionToken = type.token;
				setSourceRange(type.sourceStart, type.sourceEnd);
				
				findTypesAndPackages(this.completionToken, scope);
			} else if (annot.type instanceof CompletionOnQualifiedTypeReference) {
				this.insideQualifiedReference = true;
				
				CompletionOnQualifiedTypeReference type = (CompletionOnQualifiedTypeReference) annot.type;
				this.completionToken = type.completionIdentifier;
				long completionPosition = type.sourcePositions[type.tokens.length];
				if (qualifiedBinding instanceof PackageBinding) {

					setSourceRange(astNode.sourceStart, (int) completionPosition);
					// replace to the end of the completion identifier
					findTypesAndSubpackages(this.completionToken, (PackageBinding) qualifiedBinding, scope);
				} else {
					setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);

					findMemberTypes(
						this.completionToken,
						(ReferenceBinding) qualifiedBinding,
						scope,
						scope.enclosingSourceType(),
						false,
						new ObjectVector());
				}
			}
		} else if (astNode instanceof CompletionOnMemberValueName) {
			CompletionOnMemberValueName memberValuePair = (CompletionOnMemberValueName) astNode;
			Annotation annotation = (Annotation) astNodeParent;
			
			this.completionToken = memberValuePair.name;
			
			if (this.completionToken.length == 0) {
				if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
					this.setSourceRange(astNode.sourceStart, astNode.sourceStart - 1, false);

					findAnnotationReference(annotation.type, scope);
				}
			} else {
				if (!this.requestor.isIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF)) {
					this.findAnnotationAttributes(this.completionToken, annotation.memberValuePairs(), (ReferenceBinding)annotation.resolvedType);
				}
				if (this.assistNodeCanBeSingleMemberAnnotation) {
					if (this.expectedTypesPtr > -1 && this.expectedTypes[0].isAnnotationType()) {
						findTypesAndPackages(this.completionToken, scope);
					} else {
						findVariablesAndMethods(
							this.completionToken,
							scope,
							FakeInvocationSite,
							scope,
							insideTypeAnnotation,
							true);
						// can be the start of a qualified type name
						findTypesAndPackages(this.completionToken, scope);
					}
				}
			}
		} else if(astNode instanceof CompletionOnBrankStatementLabel) {
			if (!this.requestor.isIgnored(CompletionProposal.LABEL_REF)) {
				CompletionOnBrankStatementLabel label = (CompletionOnBrankStatementLabel) astNode;
				
				this.completionToken = label.label;
				
				this.findLabels(this.completionToken, label.possibleLabels);
			}
		} else if(astNode instanceof CompletionOnMessageSendName) {
			if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
				CompletionOnMessageSendName messageSend = (CompletionOnMessageSendName) astNode;
				
				this.insideQualifiedReference = true;
				this.completionToken = messageSend.selector;
				boolean onlyStatic = false;
				TypeBinding receiverType = null;
				if(qualifiedBinding instanceof VariableBinding) {
					receiverType = ((VariableBinding)qualifiedBinding).type;
				} else if(qualifiedBinding instanceof MethodBinding) {
					receiverType = ((MethodBinding)qualifiedBinding).returnType;
				} else if(qualifiedBinding instanceof ReferenceBinding && !(qualifiedBinding instanceof TypeVariableBinding)) {
					onlyStatic = true;
					receiverType = (TypeBinding)qualifiedBinding;
				}
				if(receiverType != null && receiverType instanceof ReferenceBinding) {
					TypeBinding[] typeArgTypes = computeTypesIfCorrect(messageSend.typeArguments);
					if(typeArgTypes != null) {
						this.findMethods(
								this.completionToken,
								typeArgTypes,
								null,
								(ReferenceBinding)receiverType.capture(scope, messageSend.receiver.sourceEnd),
								scope,
								new ObjectVector(),
								onlyStatic,
								false,
								false,
								messageSend,
								scope,
								false,
								false,
								true);
					}
				}
			}
		// Completion on Javadoc nodes
		} else if ((astNode.bits & ASTNode.InsideJavadoc) != 0) {
			if (astNode instanceof CompletionOnJavadocSingleTypeReference) {

				CompletionOnJavadocSingleTypeReference typeRef = (CompletionOnJavadocSingleTypeReference) astNode;
				this.completionToken = typeRef.token;
				this.javadocTagPosition = typeRef.tagSourceStart;
				setSourceRange(typeRef.sourceStart, typeRef.sourceEnd);
				findTypesAndPackages(this.completionToken, scope);

			} else if (astNode instanceof CompletionOnJavadocQualifiedTypeReference) {

				this.insideQualifiedReference = true;

				CompletionOnJavadocQualifiedTypeReference typeRef = (CompletionOnJavadocQualifiedTypeReference) astNode;
				this.completionToken = typeRef.completionIdentifier;
				long completionPosition = typeRef.sourcePositions[typeRef.tokens.length];
				this.javadocTagPosition = typeRef.tagSourceStart;

				// get the source positions of the completion identifier
				if (qualifiedBinding instanceof ReferenceBinding && !(qualifiedBinding instanceof TypeVariableBinding)) {
					if (!this.requestor.isIgnored(CompletionProposal.JAVADOC_TYPE_REF)) {
						int rangeStart = typeRef.completeInText() ? typeRef.sourceStart : (int) (completionPosition >>> 32);
						setSourceRange(rangeStart, (int) completionPosition);
						findMemberTypes(this.completionToken,
							(ReferenceBinding) qualifiedBinding,
							scope,
							scope.enclosingSourceType(),
							false,
							new ObjectVector());
					}
				} else if (qualifiedBinding instanceof PackageBinding) {

					setSourceRange(astNode.sourceStart, (int) completionPosition);
					// replace to the end of the completion identifier
					findTypesAndSubpackages(this.completionToken, (PackageBinding) qualifiedBinding, scope);
				}
			} else if (astNode instanceof CompletionOnJavadocFieldReference) {

				this.insideQualifiedReference = true;
				CompletionOnJavadocFieldReference fieldRef = (CompletionOnJavadocFieldReference) astNode;
				this.completionToken = fieldRef.token;
				long completionPosition = fieldRef.nameSourcePosition;
				this.javadocTagPosition = fieldRef.tagSourceStart;

				if (fieldRef.receiverType != null && fieldRef.receiverType.isValidBinding()) {
					ReferenceBinding receiverType = (ReferenceBinding) fieldRef.receiverType;
					int rangeStart = (int) (completionPosition >>> 32);
					if (fieldRef.receiver.isThis()) {
						if (fieldRef.completeInText()) {
							rangeStart = fieldRef.separatorPosition;
						}
					} else if (fieldRef.completeInText()) {
						rangeStart = fieldRef.receiver.sourceStart;
					}
					setSourceRange(rangeStart, (int) completionPosition);

					if (!this.requestor.isIgnored(CompletionProposal.FIELD_REF)
							|| !this.requestor.isIgnored(CompletionProposal.JAVADOC_FIELD_REF)) {
						findFields(this.completionToken,
							receiverType,
							scope,
							new ObjectVector(),
							new ObjectVector(),
							false, /*not only static */
							fieldRef,
							scope,
							false,
							true);
					}

					if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)
							|| !this.requestor.isIgnored(CompletionProposal.JAVADOC_METHOD_REF)) {
						findMethods(this.completionToken,
							null,
							null,
							receiverType,
							scope,
							new ObjectVector(),
							false, /*not only static */
							false,
							false,
							fieldRef,
							scope,
							false,
							false,
							true);
						if (fieldRef.receiverType instanceof ReferenceBinding) {
							ReferenceBinding refBinding = (ReferenceBinding)fieldRef.receiverType;
							if (this.completionToken == null
									|| CharOperation.prefixEquals(this.completionToken, refBinding.sourceName)
									|| (this.options.camelCaseMatch && CharOperation.camelCaseMatch(this.completionToken, refBinding.sourceName))) {
								findConstructors(refBinding, null, scope, fieldRef, false);
							}
						}
					}
				}
			} else if (astNode instanceof CompletionOnJavadocMessageSend) {

				CompletionOnJavadocMessageSend messageSend = (CompletionOnJavadocMessageSend) astNode;
				TypeBinding[] argTypes = null; //computeTypes(messageSend.arguments);
				this.completionToken = messageSend.selector;
				this.javadocTagPosition = messageSend.tagSourceStart;

				// Set source range
				int rangeStart = astNode.sourceStart;
				if (messageSend.receiver.isThis()) {
					if (messageSend.completeInText()) {
						rangeStart = messageSend.separatorPosition;
					}
				} else if (messageSend.completeInText()) {
					rangeStart = messageSend.receiver.sourceStart;
				}
				setSourceRange(rangeStart, astNode.sourceEnd, false);

				if (qualifiedBinding == null) {
					if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
						findImplicitMessageSends(this.completionToken, argTypes, scope, messageSend, scope);
					}
				} else if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
					findMethods(this.completionToken,
						null,
						argTypes,
						(ReferenceBinding) ((ReferenceBinding) qualifiedBinding).capture(scope, messageSend.receiver.sourceEnd),
						scope,
						new ObjectVector(),
						false,
						false/* prefix match */,
						false,
						messageSend,
						scope,
						false,
						messageSend.receiver instanceof SuperReference,
						true);
				}
			} else if (astNode instanceof CompletionOnJavadocAllocationExpression) {
//				setSourceRange(astNode.sourceStart, astNode.sourceEnd, false);

				CompletionOnJavadocAllocationExpression allocExpression = (CompletionOnJavadocAllocationExpression) astNode;
				this.javadocTagPosition = allocExpression.tagSourceStart;
				int rangeStart = astNode.sourceStart;
				if (allocExpression.type.isThis()) {
					if (allocExpression.completeInText()) {
						rangeStart = allocExpression.separatorPosition;
					}
				} else if (allocExpression.completeInText()) {
					rangeStart = allocExpression.type.sourceStart;
				}
				setSourceRange(rangeStart, astNode.sourceEnd, false);
				TypeBinding[] argTypes = computeTypes(allocExpression.arguments);

				ReferenceBinding ref = (ReferenceBinding) qualifiedBinding;
				if (!this.requestor.isIgnored(CompletionProposal.METHOD_REF) && ref.isClass()) {
					findConstructors(ref, argTypes, scope, allocExpression, false);
				}
			} else if (astNode instanceof CompletionOnJavadocParamNameReference) {
				if (!this.requestor.isIgnored(CompletionProposal.JAVADOC_PARAM_REF)) {
					CompletionOnJavadocParamNameReference paramRef = (CompletionOnJavadocParamNameReference) astNode;
					setSourceRange(paramRef.tagSourceStart, paramRef.tagSourceEnd);
					findJavadocParamNames(paramRef.token, paramRef.missingParams, false);
					findJavadocParamNames(paramRef.token, paramRef.missingTypeParams, true);
				}
			} else if (astNode instanceof CompletionOnJavadocTypeParamReference) {
				if (!this.requestor.isIgnored(CompletionProposal.JAVADOC_PARAM_REF)) {
					CompletionOnJavadocTypeParamReference paramRef = (CompletionOnJavadocTypeParamReference) astNode;
					setSourceRange(paramRef.tagSourceStart, paramRef.tagSourceEnd);
					findJavadocParamNames(paramRef.token, paramRef.missingParams, true);
				}
			} else if (astNode instanceof CompletionOnJavadocTag) {
				CompletionOnJavadocTag javadocTag = (CompletionOnJavadocTag) astNode;
				setSourceRange(javadocTag.tagSourceStart, javadocTag.sourceEnd);
				findJavadocBlockTags(javadocTag);
				findJavadocInlineTags(javadocTag);
			}
		}
		return true;
	}
	
	public void complete(IType type, char[] snippet, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic){	
		if(this.requestor != null){
			this.requestor.beginReporting();
		}
		boolean contextAccepted = false;
		IType topLevelType = type;
		while(topLevelType.getDeclaringType() != null) {
			topLevelType = topLevelType.getDeclaringType();
		}
		
		this.fileName = topLevelType.getParent().getElementName().toCharArray();
		CompilationResult compilationResult = new CompilationResult(this.fileName, 1, 1, this.compilerOptions.maxProblemsPerUnit);
	
		CompilationUnitDeclaration compilationUnit = null;
	
		try {
			// TypeConverter is used instead of SourceTypeConverter because the type
			// to convert can be a binary type or a source type
			TypeDeclaration typeDeclaration = null;
			if (type instanceof SourceType) {
				SourceType sourceType = (SourceType) type;
				ISourceType info = (ISourceType) sourceType.getElementInfo();
				compilationUnit = SourceTypeConverter.buildCompilationUnit(
					new ISourceType[] {info},//sourceTypes[0] is always toplevel here
					SourceTypeConverter.FIELD_AND_METHOD // need field and methods
					| SourceTypeConverter.MEMBER_TYPE, // need member types
					// no need for field initialization
					this.problemReporter,
					compilationResult);
				if (compilationUnit.types != null)
					typeDeclaration = compilationUnit.types[0];
			} else {
				compilationUnit = new CompilationUnitDeclaration(this.problemReporter, compilationResult, 0);
				typeDeclaration = BinaryTypeConverter.buildTypeDeclaration(type, compilationUnit, compilationResult);
			}
		
			if(typeDeclaration != null) {	
				// build AST from snippet
				Initializer fakeInitializer = parseSnippeInitializer(snippet, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic);
				
				// merge AST
				FieldDeclaration[] oldFields = typeDeclaration.fields;
				FieldDeclaration[] newFields = null;
				if (oldFields != null) {
					newFields = new FieldDeclaration[oldFields.length + 1];
					System.arraycopy(oldFields, 0, newFields, 0, oldFields.length);
					newFields[oldFields.length] = fakeInitializer;
				} else {
					newFields = new FieldDeclaration[] {fakeInitializer};
				}
				typeDeclaration.fields = newFields;
		
				if(DEBUG) {
					System.out.println("SNIPPET COMPLETION AST :"); //$NON-NLS-1$
					System.out.println(compilationUnit.toString());
				}
				
				if (compilationUnit.types != null) {
					try {
						this.lookupEnvironment.buildTypeBindings(compilationUnit, null /*no access restriction*/);
				
						if ((this.unitScope = compilationUnit.scope) != null) {
							this.lookupEnvironment.completeTypeBindings(compilationUnit, true);
							compilationUnit.scope.faultInTypes();
							compilationUnit.resolve();
						}
					} catch (CompletionNodeFound e) {
						//					completionNodeFound = true;
						if (e.astNode != null) {
							// if null then we found a problem in the completion node
							contextAccepted = complete(e.astNode, this.parser.assistNodeParent, e.qualifiedBinding, e.scope, e.insideTypeAnnotation);
						}
					}
				}
				if(this.noProposal && this.problem != null) {
					if(!contextAccepted) {
						contextAccepted = true;
						this.requestor.acceptContext(new CompletionContext());
					}
					this.requestor.completionFailure(this.problem);
					if(DEBUG) {
						this.printDebug(this.problem);
					}
				}
			}
		}  catch (IndexOutOfBoundsException e) { // work-around internal failure - 1GEMF6D (added with fix of 99629)
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} catch (InvalidCursorLocation e) { // may eventually report a usefull error (added to fix 99629)
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object (added with fix of 99629)
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} catch (CompletionNodeFound e){ // internal failure - bugs 5618 (added with fix of 99629)
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} catch(JavaModelException e) {
			// Do nothing
		}
		if(!contextAccepted) {
			contextAccepted = true;
			this.requestor.acceptContext(new CompletionContext());
		}
		if(this.requestor != null){
			this.requestor.endReporting();
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
			ASTNode.printModifiers(localVariableModifiers[i], prefix);
			prefix.append(' ');
			prefix.append(localVariableTypeNames[i]);
			prefix.append(' ');
			prefix.append(localVariableNames[i]);
			prefix.append(';');
		}
		
		char[] fakeSource = CharOperation.concat(prefix.toString().toCharArray(), snippet, "}}".toCharArray());//$NON-NLS-1$ 
		this.offset = prefix.length();
		
		String encoding = this.compilerOptions.defaultEncoding;
		BasicCompilationUnit fakeUnit = new BasicCompilationUnit(
			fakeSource, 
			null,
			"FakeType.java", //$NON-NLS-1$
			encoding); 
			
		this.actualCompletionPosition = prefix.length() + position - 1;
			
		CompilationResult fakeResult = new CompilationResult(fakeUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration fakeAST = this.parser.dietParse(fakeUnit, fakeResult, this.actualCompletionPosition);
		
		parseBlockStatements(fakeAST, this.actualCompletionPosition);
		
		return (Initializer)fakeAST.types[0].fields[0];
	}

	/**
	 * Ask the engine to compute a completion at the specified position
	 * of the given compilation unit.
	 *
	 *  No return
	 *      completion results are answered through a requestor.
	 *
	 *  @param sourceUnit org.eclipse.jdt.internal.compiler.env.ICompilationUnit
	 *      the source of the current compilation unit.
	 *
	 *  @param completionPosition int
	 *      a position in the source where the completion is taking place. 
	 *      This position is relative to the source provided.
	 */
	public void complete(ICompilationUnit sourceUnit, int completionPosition, int pos) {

		if(DEBUG) {
			System.out.print("COMPLETION IN "); //$NON-NLS-1$
			System.out.print(sourceUnit.getFileName());
			System.out.print(" AT POSITION "); //$NON-NLS-1$
			System.out.println(completionPosition);
			System.out.println("COMPLETION - Source :"); //$NON-NLS-1$
			System.out.println(sourceUnit.getContents());
		}
		this.requestor.beginReporting();
		boolean contextAccepted = false;
		try {
			this.fileName = sourceUnit.getFileName();
			this.actualCompletionPosition = completionPosition - 1;
			this.offset = pos;
			// for now until we can change the UI.
			CompilationResult result = new CompilationResult(sourceUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
			CompilationUnitDeclaration parsedUnit = this.parser.dietParse(sourceUnit, result, this.actualCompletionPosition);

			//		boolean completionNodeFound = false;
			if (parsedUnit != null) {
				if(DEBUG) {
					System.out.println("COMPLETION - Diet AST :"); //$NON-NLS-1$
					System.out.println(parsedUnit.toString());
				}

				// scan the package & import statements first
				if (parsedUnit.currentPackage instanceof CompletionOnPackageReference) {
					contextAccepted = true;
					this.buildContext(parsedUnit.currentPackage, null, null, null);
					if(!this.requestor.isIgnored(CompletionProposal.PACKAGE_REF)) {
						findPackages((CompletionOnPackageReference) parsedUnit.currentPackage);
					}
					if(this.noProposal && this.problem != null) {
						this.requestor.completionFailure(this.problem);
						if(DEBUG) {
							this.printDebug(this.problem);
						}
					}
					return;
				}

				ImportReference[] imports = parsedUnit.imports;
				if (imports != null) {
					for (int i = 0, length = imports.length; i < length; i++) {
						ImportReference importReference = imports[i];
						if (importReference instanceof CompletionOnImportReference) {
							this.lookupEnvironment.buildTypeBindings(parsedUnit, null /*no access restriction*/);
							if ((this.unitScope = parsedUnit.scope) != null) {
								contextAccepted = true;
								this.buildContext(importReference, null, null, null);
								
								setSourceRange(
									importReference.sourceStart,
									importReference.declarationSourceEnd);
								
								char[][] oldTokens = importReference.tokens;
								int tokenCount = oldTokens.length;
								if (tokenCount == 1) {
									findImports((CompletionOnImportReference)importReference, true);
								} else if(tokenCount > 1){
									this.insideQualifiedReference = true;
									
									char[] lastToken = oldTokens[tokenCount - 1];
									char[][] qualifierTokens = CharOperation.subarray(oldTokens, 0, tokenCount - 1);
									
									Binding binding = this.unitScope.getTypeOrPackage(qualifierTokens);
									if(binding != null) {
										if(binding instanceof PackageBinding) {
											findImports((CompletionOnImportReference)importReference, false);
										} else {
											ReferenceBinding ref = (ReferenceBinding) binding;
											if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
												this.findImportsOfMemberTypes(lastToken, ref, importReference.isStatic());
											}
											if(importReference.isStatic()) {
												if(!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
													this.findImportsOfStaticFields(lastToken, ref);
												}
												if(!this.requestor.isIgnored(CompletionProposal.METHOD_NAME_REFERENCE)) {
													this.findImportsOfStaticMethods(lastToken, ref);
												}
											}
										}
									}
								}
								
								if(this.noProposal && this.problem != null) {
									this.requestor.completionFailure(this.problem);
									if(DEBUG) {
										this.printDebug(this.problem);
									}
								}
							}
							return;
						} else if(importReference instanceof CompletionOnKeyword) {
							contextAccepted = true;
							this.buildContext(importReference, null, null, null);
							if(!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
								setSourceRange(importReference.sourceStart, importReference.sourceEnd);
								CompletionOnKeyword keyword = (CompletionOnKeyword)importReference;
								findKeywords(keyword.getToken(), keyword.getPossibleKeywords(), false);
							}
							if(this.noProposal && this.problem != null) {
								this.requestor.completionFailure(this.problem);
								if(DEBUG) {
									this.printDebug(this.problem);
								}
							}
							return;
						}
					}
				}

				if (parsedUnit.types != null) {
					try {
						this.lookupEnvironment.buildTypeBindings(parsedUnit, null /*no access restriction*/);

						if ((this.unitScope = parsedUnit.scope) != null) {
							this.source = sourceUnit.getContents();
							this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
							parsedUnit.scope.faultInTypes();
							parseBlockStatements(parsedUnit, this.actualCompletionPosition);
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
								if(this.parser.assistNodeParent != null) {
									System.out.print("COMPLETION - Parent Node : ");  //$NON-NLS-1$
									System.out.println(this.parser.assistNodeParent);
								}
							}
							// if null then we found a problem in the completion node
							contextAccepted = complete(e.astNode, this.parser.assistNodeParent, e.qualifiedBinding, e.scope, e.insideTypeAnnotation);
						}
					}
				}
			}
			
			if(this.noProposal && this.problem != null) {
				if(!contextAccepted) {
					contextAccepted = true;
					CompletionContext context = new CompletionContext();
					context.setOffset(completionPosition);
					context.setTokenKind(CompletionContext.TOKEN_KIND_UNKNOWN);
					this.requestor.acceptContext(context);
				}
				this.requestor.completionFailure(this.problem);
				if(DEBUG) {
					this.printDebug(this.problem);
				}
			}
			/* Ignore package, import, class & interface keywords for now...
					if (!completionNodeFound) {
						if (parsedUnit == null || parsedUnit.types == null) {
							// this is not good enough... can still be trying to define a second type
							CompletionScanner scanner = (CompletionScanner) this.parser.scanner;
							setSourceRange(scanner.completedIdentifierStart, scanner.completedIdentifierEnd);
							findKeywords(scanner.completionIdentifier, mainDeclarations, null);
						}
						// currently have no way to know if extends/implements are possible keywords
					}
			*/
		} catch (IndexOutOfBoundsException e) { // work-around internal failure - 1GEMF6D
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} catch (InvalidCursorLocation e) { // may eventually report a usefull error
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} catch (CompletionNodeFound e){ // internal failure - bugs 5618
			if(DEBUG) {
				System.out.println("Exception caught by CompletionEngine:"); //$NON-NLS-1$
				e.printStackTrace(System.out);
			}
		} finally {
			reset();
			if(!contextAccepted) {
				contextAccepted = true;
				CompletionContext context = new CompletionContext();
				context.setTokenKind(CompletionContext.TOKEN_KIND_UNKNOWN);
				context.setOffset(completionPosition);
				this.requestor.acceptContext(context);
			}
			this.requestor.endReporting();
		}
	}

	private TypeBinding[] computeTypes(Expression[] arguments) {
		if (arguments == null) return null;
		int argsLength = arguments.length;
		TypeBinding[] argTypes = new TypeBinding[argsLength];
		for (int a = argsLength; --a >= 0;) {
			argTypes[a] = arguments[a].resolvedType;
		}
		return argTypes;
	}
	
	private TypeBinding[] computeTypesIfCorrect(Expression[] arguments) {
		if (arguments == null) return null;
		int argsLength = arguments.length;
		TypeBinding[] argTypes = new TypeBinding[argsLength];
		for (int a = argsLength; --a >= 0;) {
			TypeBinding typeBinding = arguments[a].resolvedType;
			if(typeBinding == null || !typeBinding.isValidBinding()) return null;
			argTypes[a] = typeBinding;
		}
		return argTypes;
	}
	
	private void findAnnotationAttributes(char[] token, MemberValuePair[] attributesFound, ReferenceBinding annotation) {
		MethodBinding[] methods = annotation.availableMethods();
		nextAttribute: for (int i = 0; i < methods.length; i++) {
			MethodBinding method = methods[i];
			
			if(!CharOperation.prefixEquals(token, method.selector, false)
					&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(token, method.selector))) continue nextAttribute;
			
			int length = attributesFound == null ? 0 : attributesFound.length;
			for (int j = 0; j < length; j++) {
				if(CharOperation.equals(method.selector, attributesFound[j].name, false)) continue nextAttribute;
			}
			
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal(method);
			relevance += computeRelevanceForCaseMatching(token, method.selector);
			relevance += computeRelevanceForQualification(false);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
			
			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF)) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.ANNOTATION_ATTRIBUTE_REF, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(method.declaringClass));
				proposal.setSignature(getSignature(method.returnType));
				proposal.setName(method.selector);
				proposal.setCompletion(method.selector);
				proposal.setFlags(method.modifiers);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
		}
	}
	private void findAnnotationReference(TypeReference ref, Scope scope) {
		ReferenceBinding refBinding = (ReferenceBinding) ref.resolvedType;
		if(refBinding != null) {
			char[] typeName = refBinding.qualifiedSourceName();
			
			if (this.options.checkDeprecation &&
					refBinding.isViewedAsDeprecated() &&
					!scope.isDefinedInSameUnit(refBinding)) {
				return;
			}
			
			int accessibility = IAccessRule.K_ACCESSIBLE;
			if(refBinding.hasRestrictedAccess()) {
				AccessRestriction accessRestriction = lookupEnvironment.getAccessRestriction(refBinding);
				if(accessRestriction != null) {
					switch (accessRestriction.getProblemId()) {
						case IProblem.ForbiddenReference:
							if (this.options.checkForbiddenReference) {
								return;
							}
							accessibility = IAccessRule.K_NON_ACCESSIBLE;
							break;
						case IProblem.DiscouragedReference:
							if (this.options.checkDiscouragedReference) {
								return;
							}
							accessibility = IAccessRule.K_DISCOURAGED;
							break;
					}
				}
			}

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(refBinding.sourceName, refBinding.sourceName);
			relevance += computeRelevanceForExpectingType(refBinding);
			relevance += computeRelevanceForQualification(false);
			relevance += computeRelevanceForRestrictions(accessibility); // no access restriction for type in the current unit
			
			if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
				createTypeProposal(refBinding, typeName, accessibility, CharOperation.NO_CHAR, relevance);
			}
		}
	}
	private void findAnonymousType(
		ReferenceBinding currentType,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite) {

		if (currentType.isInterface()) {
			char[] completion = CharOperation.NO_CHAR;
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
			
			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION)) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(currentType));
				proposal.setDeclarationKey(currentType.computeUniqueKey());
				proposal.setSignature(
						createMethodSignature(
								CharOperation.NO_CHAR_CHAR,
								CharOperation.NO_CHAR_CHAR,
								CharOperation.NO_CHAR,
								CharOperation.NO_CHAR));
				//proposal.setOriginalSignature(null);
				//proposal.setUniqueKey(null);
				proposal.setDeclarationPackageName(currentType.qualifiedPackageName());
				proposal.setDeclarationTypeName(currentType.qualifiedSourceName());
				//proposal.setParameterPackageNames(null);
				//proposal.setParameterTypeNames(null);
				//proposal.setPackageName(null);
				//proposal.setTypeName(null);
				proposal.setCompletion(completion);
				proposal.setFlags(Flags.AccPublic);
				proposal.setReplaceRange(this.endPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
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

		if (token == null) return;

		if (token.length <= classField.length
			&& CharOperation.prefixEquals(token, classField, false /* ignore case */
		)) {
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(token, classField);
			relevance += computeRelevanceForExpectingType(scope.getJavaLangClass());
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); //no access restriction for class field 
			
			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition);
				//proposal.setDeclarationSignature(null);
				char[] signature = 
					createNonGenericTypeSignature(
						CharOperation.concatWith(JAVA_LANG, '.'),
						CLASS);
				if (this.compilerOptions.sourceLevel > ClassFileConstants.JDK1_4) {
					// add type argument
					char[] typeArgument = getTypeSignature(receiverType);
					int oldLength = signature.length;
					int argumentLength = typeArgument.length;
					int newLength = oldLength + argumentLength + 2;
					System.arraycopy(signature, 0, signature = new char[newLength], 0, oldLength - 1);
					signature[oldLength - 1] = '<';
					System.arraycopy(typeArgument, 0, signature, oldLength , argumentLength);
					signature[newLength - 2] = '>';
					signature[newLength - 1] = ';';
				}
				proposal.setSignature(signature);
				//proposal.setDeclarationPackageName(null);
				//proposal.setDeclarationTypeName(null);
				proposal.setPackageName(CharOperation.concatWith(JAVA_LANG, '.'));
				proposal.setTypeName(CLASS);
				proposal.setName(classField);
				proposal.setCompletion(classField);
				proposal.setFlags(Flags.AccStatic | Flags.AccPublic);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
		}
	}
	private void findEnumConstant(char[] enumConstantName, SwitchStatement switchStatement) {
		TypeBinding expressionType = switchStatement.expression.resolvedType;
		if(expressionType != null && expressionType.isEnum()) {
			ReferenceBinding enumType = (ReferenceBinding) expressionType;
			
			CaseStatement[] cases = switchStatement.cases;
			
			char[][] alreadyUsedConstants = new char[switchStatement.caseCount][];
			int alreadyUsedConstantCount = 0;
			for (int i = 0; i < switchStatement.caseCount; i++) {
				if(cases[i].isEnumConstant && cases[i].constantExpression instanceof SingleNameReference) {
					alreadyUsedConstants[alreadyUsedConstantCount++] = ((SingleNameReference)cases[i].constantExpression).token;
				}
			}
			
			FieldBinding[] fields = enumType.fields();
			
			int enumConstantLength = enumConstantName.length;
			next : for (int f = fields.length; --f >= 0;) {			
				FieldBinding field = fields[f];

				if (field.isSynthetic()) continue next;

				if ((field.modifiers & Flags.AccEnum) == 0) continue next;

				if (enumConstantLength > field.name.length) continue next;

				if (!CharOperation.prefixEquals(enumConstantName, field.name, false /* ignore case */)
						&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(enumConstantName, field.name)))	continue next;
				
				char[] completion = field.name;
				
				for (int i = 0; i < alreadyUsedConstantCount; i++) {
					if(CharOperation.equals(alreadyUsedConstants[i], completion)) continue next;
				}

				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal(field);
				relevance += computeRelevanceForEnum();
				relevance += computeRelevanceForCaseMatching(enumConstantName, field.name);
				relevance += computeRelevanceForExpectingType(field.type);
				relevance += computeRelevanceForQualification(false);
				relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
				
				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
					CompletionProposal proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition);
					proposal.setDeclarationSignature(getSignature(field.declaringClass));
					proposal.setSignature(getSignature(field.type));
					proposal.setDeclarationPackageName(field.declaringClass.qualifiedPackageName());
					proposal.setDeclarationTypeName(field.declaringClass.qualifiedSourceName());
					proposal.setPackageName(field.type.qualifiedPackageName());
					proposal.setTypeName(field.type.qualifiedSourceName()); 
					proposal.setName(field.name);
					proposal.setCompletion(completion);
					proposal.setFlags(field.modifiers);
					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
					proposal.setRelevance(relevance);
					this.requestor.accept(proposal);
					if(DEBUG) {
						this.printDebug(proposal);
					}
				}
			}
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
						
					if (this.options.checkDeprecation &&
							constructor.isViewedAsDeprecated() &&
							!scope.isDefinedInSameUnit(constructor.declaringClass))
						continue next;
					
					if (this.options.checkVisibility
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
					if (this.source != null
						&& this.source.length > this.endPosition
						&& this.source[this.endPosition] == '(')
						completion = name;
					else
						completion = CharOperation.concat(name, new char[] { '(', ')' });
					
					int relevance = computeBaseRelevance();
					relevance += computeRelevanceForInterestingProposal();
					relevance += computeRelevanceForCaseMatching(this.completionToken, name);
					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
					
					this.noProposal = false;
					if(!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
						CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_REF, this.actualCompletionPosition);
						proposal.setDeclarationSignature(getSignature(currentType));
						proposal.setSignature(getSignature(constructor));
						MethodBinding original = constructor.original();
						if(original != constructor) {
							proposal.setOriginalSignature(getSignature(original));
						}
						proposal.setDeclarationPackageName(currentType.qualifiedPackageName());
						proposal.setDeclarationTypeName(currentType.qualifiedSourceName());
						proposal.setParameterPackageNames(parameterPackageNames);
						proposal.setParameterTypeNames(parameterTypeNames);
						//proposal.setPackageName(null);
						//proposal.setTypeName(null);
						proposal.setName(name);
						proposal.setIsContructor(true);
						proposal.setCompletion(completion);
						proposal.setFlags(constructor.modifiers);
						proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
						proposal.setRelevance(relevance);
						if(parameterNames != null) proposal.setParameterNames(parameterNames);
						this.requestor.accept(proposal);
						if(DEBUG) {
							this.printDebug(proposal);
						}
					}
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
						
					if (this.options.checkDeprecation &&
							constructor.isViewedAsDeprecated() &&
							!scope.isDefinedInSameUnit(constructor.declaringClass))
						continue next;
					
					if (this.options.checkVisibility
						&& !constructor.canBeSeenBy(invocationSite, scope)) {
						if(!forAnonymousType || !constructor.isProtected())
							continue next;
					}
					
					TypeBinding[] parameters = constructor.parameters;
					int paramLength = parameters.length;
					if (minArgLength > paramLength)
						continue next;
					for (int a = minArgLength; --a >= 0;)
						if (argTypes[a] != null) { // can be null if it could not be resolved properly
							if (!argTypes[a].isCompatibleWith(constructor.parameters[a]))
								continue next;
						}
	
					char[][] parameterPackageNames = new char[paramLength][];
					char[][] parameterTypeNames = new char[paramLength][];
					for (int i = 0; i < paramLength; i++) {
						TypeBinding type = parameters[i];
						parameterPackageNames[i] = type.qualifiedPackageName();
						parameterTypeNames[i] = type.qualifiedSourceName();
					}
					char[][] parameterNames = findMethodParameterNames(constructor,parameterTypeNames);
					
					char[] completion = CharOperation.NO_CHAR;
					if(forAnonymousType){
						int relevance = computeBaseRelevance();
						relevance += computeRelevanceForInterestingProposal();
						relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
						
						this.noProposal = false;
						if(!this.requestor.isIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION)) {
							CompletionProposal proposal = this.createProposal(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, this.actualCompletionPosition);
							proposal.setDeclarationSignature(getSignature(currentType));
							proposal.setDeclarationKey(currentType.computeUniqueKey());
							proposal.setSignature(getSignature(constructor));
							MethodBinding original = constructor.original();
							if(original != constructor) {
								proposal.setOriginalSignature(getSignature(original));
							}
							proposal.setKey(constructor.computeUniqueKey());
							proposal.setDeclarationPackageName(currentType.qualifiedPackageName());
							proposal.setDeclarationTypeName(currentType.qualifiedSourceName());
							proposal.setParameterPackageNames(parameterPackageNames);
							proposal.setParameterTypeNames(parameterTypeNames);
							//proposal.setPackageName(null);
							//proposal.setTypeName(null);
							proposal.setCompletion(completion);
							proposal.setFlags(constructor.modifiers);
							proposal.setReplaceRange(this.endPosition - this.offset, this.endPosition - this.offset);
							proposal.setRelevance(relevance);
							if(parameterNames != null) proposal.setParameterNames(parameterNames);
							this.requestor.accept(proposal);
							if(DEBUG) {
								this.printDebug(proposal);
							}
						}
					} else {
						int relevance = computeBaseRelevance();
						relevance += computeRelevanceForInterestingProposal();
						relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);

						// Special case for completion in javadoc
						if (this.assistNodeInJavadoc > 0) {
							Expression receiver = null;
							char[] selector = null;
							if (invocationSite instanceof CompletionOnJavadocAllocationExpression) {
								CompletionOnJavadocAllocationExpression alloc = (CompletionOnJavadocAllocationExpression) invocationSite;
								receiver = alloc.type;
							} else if (invocationSite instanceof CompletionOnJavadocFieldReference) {
								CompletionOnJavadocFieldReference fieldRef = (CompletionOnJavadocFieldReference) invocationSite;
								receiver = fieldRef.receiver;
							}
							if (receiver != null) {
								StringBuffer javadocCompletion = new StringBuffer();
								if (receiver.isThis()) {
									selector = (((JavadocImplicitTypeReference)receiver).token);
									if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0) {
										javadocCompletion.append('#');
									}
								} else if (receiver instanceof JavadocSingleTypeReference) {
									JavadocSingleTypeReference typeRef = (JavadocSingleTypeReference) receiver;
									selector = typeRef.token;
									if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0) {
										javadocCompletion.append(typeRef.token);
										javadocCompletion.append('#');
									}
								} else if (receiver instanceof JavadocQualifiedTypeReference) {
									JavadocQualifiedTypeReference typeRef = (JavadocQualifiedTypeReference) receiver;
									selector = typeRef.tokens[typeRef.tokens.length-1];
									if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0) {
										javadocCompletion.append(CharOperation.concatWith(typeRef.tokens, '.'));
										javadocCompletion.append('#');
									}
								}
								// Append parameters types
								javadocCompletion.append(selector);
								javadocCompletion.append('(');
								if (constructor.parameters != null) {
									for (int p=0, ln=constructor.parameters.length; p<ln; p++) {
										if (p>0) javadocCompletion.append(", "); //$NON-NLS-1$
										TypeBinding argTypeBinding = constructor.parameters[p];
										createType(argTypeBinding.erasure(), javadocCompletion);
									}
								}
								javadocCompletion.append(')');
								completion = javadocCompletion.toString().toCharArray();
							}
						} 
						
						// Create standard proposal
						this.noProposal = false;
						if(!this.requestor.isIgnored(CompletionProposal.METHOD_REF) && (this.assistNodeInJavadoc & CompletionOnJavadoc.ONLY_INLINE_TAG) == 0) {
							CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_REF, this.actualCompletionPosition);
							proposal.setDeclarationSignature(getSignature(currentType));
							proposal.setSignature(getSignature(constructor));
							MethodBinding original = constructor.original();
							if(original != constructor) {
								proposal.setOriginalSignature(getSignature(original));
							}
							proposal.setDeclarationPackageName(currentType.qualifiedPackageName());
							proposal.setDeclarationTypeName(currentType.qualifiedSourceName());
							proposal.setParameterPackageNames(parameterPackageNames);
							proposal.setParameterTypeNames(parameterTypeNames);
							//proposal.setPackageName(null);
							//proposal.setTypeName(null);
							proposal.setName(currentType.sourceName());
							proposal.setIsContructor(true);
							proposal.setCompletion(completion);
							proposal.setFlags(constructor.modifiers);
							int start = (this.assistNodeInJavadoc > 0) ? this.startPosition : this.endPosition;
							proposal.setReplaceRange(start - this.offset, this.endPosition - this.offset);
							proposal.setRelevance(relevance);
							if(parameterNames != null) proposal.setParameterNames(parameterNames);
							this.requestor.accept(proposal);
							if(DEBUG) {
								this.printDebug(proposal);
							}
						}
						if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0 && !this.requestor.isIgnored(CompletionProposal.JAVADOC_METHOD_REF)) {
							char[] javadocCompletion = inlineTagCompletion(completion, JavadocTagConstants.TAG_LINK);
							CompletionProposal proposal = this.createProposal(CompletionProposal.JAVADOC_METHOD_REF, this.actualCompletionPosition);
							proposal.setDeclarationSignature(getSignature(currentType));
							proposal.setSignature(getSignature(constructor));
							MethodBinding original = constructor.original();
							if(original != constructor) {
								proposal.setOriginalSignature(getSignature(original));
							}
							proposal.setDeclarationPackageName(currentType.qualifiedPackageName());
							proposal.setDeclarationTypeName(currentType.qualifiedSourceName());
							proposal.setParameterPackageNames(parameterPackageNames);
							proposal.setParameterTypeNames(parameterTypeNames);
							//proposal.setPackageName(null);
							//proposal.setTypeName(null);
							proposal.setName(currentType.sourceName());
							proposal.setIsContructor(true);
							proposal.setCompletion(javadocCompletion);
							proposal.setFlags(constructor.modifiers);
							int start = (this.assistNodeInJavadoc > 0) ? this.startPosition : this.endPosition;
							if ((this.assistNodeInJavadoc & CompletionOnJavadoc.REPLACE_TAG) != 0) start = this.javadocTagPosition;
							proposal.setReplaceRange(start - this.offset, this.endPosition - this.offset);
							proposal.setRelevance(relevance+R_INLINE_TAG);
							if(parameterNames != null) proposal.setParameterNames(parameterNames);
							this.requestor.accept(proposal);
							if(DEBUG) {
								this.printDebug(proposal);
							}
						}
					}
				}
			}
		}
	}
	
	private char[][] findEnclosingTypeNames(Scope scope){
		char[][] excludedNames = new char[10][];
		int excludedNameCount = 0;
		
		Scope currentScope = scope;
		while(currentScope != null) {
			switch (currentScope.kind) {
				case Scope.CLASS_SCOPE :
					ClassScope classScope = (ClassScope) currentScope;
					
					TypeDeclaration typeDeclaration = classScope.referenceContext;
					
					if(excludedNameCount == excludedNames.length) {
						System.arraycopy(excludedNames, 0, excludedNames = new char[excludedNameCount * 2][], 0, excludedNameCount);
					}
					excludedNames[excludedNameCount++] = typeDeclaration.name;
					
					TypeParameter[] classTypeParameters = typeDeclaration.typeParameters;
					if(classTypeParameters != null) {
						for (int i = 0; i < classTypeParameters.length; i++) {
							TypeParameter typeParameter = classTypeParameters[i];
							if(excludedNameCount == excludedNames.length) {
								System.arraycopy(excludedNames, 0, excludedNames = new char[excludedNameCount * 2][], 0, excludedNameCount);
							}
							excludedNames[excludedNameCount++] = typeParameter.name;
						}
					}
					break;
				case Scope.METHOD_SCOPE :
					MethodScope methodScope = (MethodScope) currentScope;
					if(methodScope.referenceContext instanceof AbstractMethodDeclaration) {
						TypeParameter[] methodTypeParameters = ((AbstractMethodDeclaration)methodScope.referenceContext).typeParameters();
						if(methodTypeParameters != null) {
							for (int i = 0; i < methodTypeParameters.length; i++) {
								TypeParameter typeParameter = methodTypeParameters[i];
								if(excludedNameCount == excludedNames.length) {
									System.arraycopy(excludedNames, 0, excludedNames = new char[excludedNameCount * 2][], 0, excludedNameCount);
								}
								excludedNames[excludedNameCount++] = typeParameter.name;
							}
						}
					}
					break;
			}
			
			currentScope = currentScope.parent;
		}
		
		if(excludedNameCount == 0) {
			return CharOperation.NO_CHAR_CHAR;
		}
		System.arraycopy(excludedNames, 0, excludedNames = new char[excludedNameCount][], 0, excludedNameCount);
		return excludedNames;
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
		boolean implicitCall,
		boolean canBePrefixed) {

		ObjectVector newFieldsFound = new ObjectVector();
		// Inherited fields which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		
		int fieldLength = fieldName.length;
		next : for (int f = fields.length; --f >= 0;) {			
			FieldBinding field = fields[f];

			if (field.isSynthetic())	continue next;
			
			if (onlyStaticFields && !field.isStatic()) continue next;

			if (fieldLength > field.name.length) continue next;

			if (!CharOperation.prefixEquals(fieldName, field.name, false /* ignore case */)
					&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(fieldName, field.name)))	continue next;

			if (this.options.checkDeprecation &&
					field.isViewedAsDeprecated() &&
					!scope.isDefinedInSameUnit(field.declaringClass))
				continue next;
			
			if (this.options.checkVisibility
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
					if(canBePrefixed) {
						prefixRequired = true;
					} else {
						continue next;
					}
				}
			}

			for (int l = localsFound.size; --l >= 0;) {
				LocalVariableBinding local = (LocalVariableBinding) localsFound.elementAt(l);	

				if (CharOperation.equals(field.name, local.name, true)) {
					SourceTypeBinding declarationType = scope.enclosingSourceType();
					if (declarationType.isAnonymousType() && declarationType != invocationScope.enclosingSourceType()) {
						continue next;
					}
					if(canBePrefixed) {
						prefixRequired = true;
					} else {
						continue next;
					}
					break;
				}
			}
			
			newFieldsFound.add(new Object[]{field, receiverType});
			
			char[] completion = field.name;
			
			if(prefixRequired || this.options.forceImplicitQualification){
				char[] prefix = computePrefix(scope.enclosingSourceType(), invocationScope.enclosingSourceType(), field.isStatic());
				completion = CharOperation.concat(prefix,completion,'.');
			}

			// Special case for javadoc completion
			if (this.assistNodeInJavadoc > 0) {
				if (invocationSite instanceof CompletionOnJavadocFieldReference) {
					CompletionOnJavadocFieldReference fieldRef = (CompletionOnJavadocFieldReference) invocationSite;
					if (fieldRef.receiver.isThis()) {
						if (fieldRef.completeInText()) {
							completion = CharOperation.concat(new char[] { '#' }, field.name);
						}
					} else if (fieldRef.completeInText()) {
						if (fieldRef.receiver instanceof JavadocSingleTypeReference) {
							JavadocSingleTypeReference typeRef = (JavadocSingleTypeReference) fieldRef.receiver;
							completion = CharOperation.concat(typeRef.token, field.name, '#');
						} else if (fieldRef.receiver instanceof JavadocQualifiedTypeReference) {
							JavadocQualifiedTypeReference typeRef = (JavadocQualifiedTypeReference) fieldRef.receiver;
							completion = CharOperation.concat(CharOperation.concatWith(typeRef.tokens, '.'), field.name, '#');
						}
					}
				}
			}

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal(field);
			if (fieldName != null) relevance += computeRelevanceForCaseMatching(fieldName, field.name);
			relevance += computeRelevanceForExpectingType(field.type);
			relevance += computeRelevanceForStatic(onlyStaticFields, field.isStatic());
			relevance += computeRelevanceForQualification(prefixRequired);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
			
			this.noProposal = false;
			// Standard proposal
			if (!this.requestor.isIgnored(CompletionProposal.FIELD_REF) && (this.assistNodeInJavadoc & CompletionOnJavadoc.ONLY_INLINE_TAG) == 0) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(field.declaringClass));
				proposal.setSignature(getSignature(field.type));
				proposal.setDeclarationPackageName(field.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(field.declaringClass.qualifiedSourceName());
				proposal.setPackageName(field.type.qualifiedPackageName());
				proposal.setTypeName(field.type.qualifiedSourceName()); 
				proposal.setName(field.name);
				proposal.setCompletion(completion);
				proposal.setFlags(field.modifiers);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}

			// Javadoc completions
			if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0 && !this.requestor.isIgnored(CompletionProposal.JAVADOC_FIELD_REF)) {
				char[] javadocCompletion = inlineTagCompletion(completion, JavadocTagConstants.TAG_LINK);
				CompletionProposal proposal = this.createProposal(CompletionProposal.JAVADOC_FIELD_REF, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(field.declaringClass));
				proposal.setSignature(getSignature(field.type));
				proposal.setDeclarationPackageName(field.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(field.declaringClass.qualifiedSourceName());
				proposal.setPackageName(field.type.qualifiedPackageName());
				proposal.setTypeName(field.type.qualifiedSourceName()); 
				proposal.setName(field.name);
				proposal.setCompletion(javadocCompletion);
				proposal.setFlags(field.modifiers);
				int start = (this.assistNodeInJavadoc & CompletionOnJavadoc.REPLACE_TAG) != 0 ? this.javadocTagPosition : this.startPosition;
				proposal.setReplaceRange(start - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance+R_INLINE_TAG);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
				// Javadoc value completion for static fields
				if (field.isStatic() && !this.requestor.isIgnored(CompletionProposal.JAVADOC_VALUE_REF)) {
					javadocCompletion = inlineTagCompletion(completion, JavadocTagConstants.TAG_VALUE);
					CompletionProposal valueProposal = this.createProposal(CompletionProposal.JAVADOC_VALUE_REF, this.actualCompletionPosition);
					valueProposal.setDeclarationSignature(getSignature(field.declaringClass));
					valueProposal.setSignature(getSignature(field.type));
					valueProposal.setDeclarationPackageName(field.declaringClass.qualifiedPackageName());
					valueProposal.setDeclarationTypeName(field.declaringClass.qualifiedSourceName());
					valueProposal.setPackageName(field.type.qualifiedPackageName());
					valueProposal.setTypeName(field.type.qualifiedSourceName()); 
					valueProposal.setName(field.name);
					valueProposal.setCompletion(javadocCompletion);
					valueProposal.setFlags(field.modifiers);
					valueProposal.setReplaceRange(start - this.offset, this.endPosition - this.offset);
					valueProposal.setRelevance(relevance+R_VALUE_TAG);
					this.requestor.accept(valueProposal);
					if(DEBUG) {
						this.printDebug(valueProposal);
					}
				}
			}
		}
		
		fieldsFound.addAll(newFieldsFound);
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
		boolean implicitCall,
		boolean canBePrefixed) {

		boolean notInJavadoc = this.assistNodeInJavadoc == 0;
		if (fieldName == null && notInJavadoc)
			return;

		ReferenceBinding currentType = receiverType;
		ReferenceBinding[] interfacesToVisit = null;
		int nextPosition = 0;
		do {
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (notInJavadoc && itsInterfaces != Binding.NO_SUPERINTERFACES) {
				if (interfacesToVisit == null) {
					interfacesToVisit = itsInterfaces;
					nextPosition = interfacesToVisit.length;
				} else {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}

			FieldBinding[] fields = currentType.availableFields();
			if(fields != null && fields.length > 0) {
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
					implicitCall,
					canBePrefixed);
			}
			currentType = currentType.superclass();
		} while (notInJavadoc && currentType != null);

		if (notInJavadoc && interfacesToVisit != null) {
			for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding anInterface = interfacesToVisit[i];
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
						implicitCall,
						canBePrefixed);
				}

				ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
				if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
		}
	}

	private void findFieldsAndMethods(
		char[] token,
		TypeBinding receiverType,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall,
		boolean superCall) {

		if (token == null)
			return;

		if (receiverType.isBaseType())
			return; // nothing else is possible with base types
		
		boolean proposeField = !this.requestor.isIgnored(CompletionProposal.FIELD_REF);
		boolean proposeMethod = !this.requestor.isIgnored(CompletionProposal.METHOD_REF);
		
		ObjectVector methodsFound = new ObjectVector();
		
		if (receiverType.isArrayType()) {
			if (proposeField
				&& token.length <= lengthField.length
				&& CharOperation.prefixEquals(token, lengthField, false /* ignore case */
			)) {
				
				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(token,lengthField);
				relevance += computeRelevanceForExpectingType(TypeBinding.INT);
				relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for length field
				
				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
					CompletionProposal proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition);
					proposal.setDeclarationSignature(getSignature(receiverType));
					proposal.setSignature(INT_SIGNATURE);
					//proposal.setDeclarationPackageName(null);
					//proposal.setDeclarationTypeName(null);
					//proposal.setPackageName(null);
					proposal.setTypeName(INT);
					proposal.setName(lengthField);
					proposal.setCompletion(lengthField);
					proposal.setFlags(Flags.AccPublic);
					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
					proposal.setRelevance(relevance);
					this.requestor.accept(proposal);
					if(DEBUG) {
						this.printDebug(proposal);
					}
				}
			}
			if (proposeMethod
				&& token.length <= cloneMethod.length
				&& CharOperation.prefixEquals(token, cloneMethod, false /* ignore case */)
			) {
				ReferenceBinding objectRef = scope.getJavaLangObject();
				
				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(token, cloneMethod);
				relevance += computeRelevanceForExpectingType(objectRef);
				relevance += computeRelevanceForStatic(false, false);
				relevance += computeRelevanceForQualification(false);
				relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for clone() method
				
				char[] completion;
				if (this.source != null
					&& this.source.length > this.endPosition
					&& this.source[this.endPosition] == '(') {
					completion = cloneMethod;
					} else {
					completion = CharOperation.concat(cloneMethod, new char[] { '(', ')' });
				}
				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
					CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_REF, this.actualCompletionPosition);
					proposal.setDeclarationSignature(getSignature(receiverType));
					proposal.setSignature(
							createMethodSignature(
									CharOperation.NO_CHAR_CHAR,
									CharOperation.NO_CHAR_CHAR,
									CharOperation.concatWith(JAVA_LANG, '.'),
									OBJECT));
					//proposal.setOriginalSignature(null);
					//proposal.setDeclarationPackageName(null);
					//proposal.setDeclarationTypeName(null);
					//proposal.setParameterPackageNames(null);
					//proposal.setParameterTypeNames(null);
					proposal.setPackageName(CharOperation.concatWith(JAVA_LANG, '.'));
					proposal.setTypeName(OBJECT);
					proposal.setName(cloneMethod);
					proposal.setCompletion(completion);
					proposal.setFlags(Flags.AccPublic);
					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
					proposal.setRelevance(relevance);
					this.requestor.accept(proposal);
					if(DEBUG) {
						this.printDebug(proposal);
					}
				}
				methodsFound.add(new Object[]{objectRef.getMethods(cloneMethod)[0], objectRef});
			}
			
			receiverType = scope.getJavaLangObject();
		}

		if(proposeField) {
			findFields(
				token,
				(ReferenceBinding) receiverType,
				scope,
				new ObjectVector(),
				new ObjectVector(),
				false,
				invocationSite,
				invocationScope,
				implicitCall,
				true);
		}

		if(proposeMethod) {
			findMethods(
				token,
				null,
				null,
				(ReferenceBinding) receiverType,
				scope,
				methodsFound,
				false,
				false,
				false,
				invocationSite,
				invocationScope,
				implicitCall,
				superCall,
				true);
		}
	}

	private void findImports(CompletionOnImportReference importReference, boolean findMembers) {
		char[][] tokens = importReference.tokens;
			
		char[] importName = CharOperation.concatWith(tokens, '.');
		
		if (importName.length == 0)
			return;
			
		char[] lastToken = tokens[tokens.length - 1];
		if(lastToken != null && lastToken.length == 0)
			importName = CharOperation.concat(importName, new char[]{'.'});

		this.resolvingImports = true;
		this.resolvingStaticImports = importReference.isStatic();
			
		this.completionToken =  importName;
		// want to replace the existing .*;
		if(!this.requestor.isIgnored(CompletionProposal.PACKAGE_REF)) {
			this.nameEnvironment.findPackages(importName, this);
		}
		if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
			this.nameEnvironment.findTypes(
					importName,
					findMembers && PROPOSE_MEMBER_TYPES, 
					this.options.camelCaseMatch,
					this);
			acceptTypes();
		}
	}
	
	private void findImportsOfMemberTypes(char[] typeName,	ReferenceBinding ref, boolean onlyStatic) {
		ReferenceBinding[] memberTypes = ref.memberTypes();
		
		int typeLength = typeName.length;
		next : for (int m = memberTypes.length; --m >= 0;) {
			ReferenceBinding memberType = memberTypes[m];
			//		if (!wantClasses && memberType.isClass()) continue next;
			//		if (!wantInterfaces && memberType.isInterface()) continue next;
			
			if (onlyStatic && !memberType.isStatic())
				continue next;
			
			if (typeLength > memberType.sourceName.length)
				continue next;

			if (!CharOperation.prefixEquals(typeName, memberType.sourceName, false/* ignore case */)
					&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(typeName, memberType.sourceName)))
				continue next;

			if (this.options.checkDeprecation && memberType.isViewedAsDeprecated()) continue next;
			
			if (this.options.checkVisibility
				&& !memberType.canBeSeenBy(this.unitScope.fPackage))
				continue next;
			
			char[] completionName = CharOperation.concat(
					memberType.qualifiedPackageName(),
					memberType.qualifiedSourceName(),
					'.');
			
			completionName = CharOperation.concat(completionName, SEMICOLON);
			
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(typeName, memberType.sourceName);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);

			if (memberType.isClass()) {
				relevance += computeRelevanceForClass();
			} else if(memberType.isEnum()) {
				relevance += computeRelevanceForEnum();
			} else if (memberType.isInterface()) {
				relevance += computeRelevanceForInterface();
			}
			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
				createTypeProposal(memberType, memberType.qualifiedSourceName(), IAccessRule.K_ACCESSIBLE, completionName, relevance);
			}
		}
	}
	
	private void findImportsOfStaticFields(char[] fieldName, ReferenceBinding ref) {
		FieldBinding[] fields = ref.fields();
		
		int fieldLength = fieldName.length;
		next : for (int m = fields.length; --m >= 0;) {
			FieldBinding field = fields[m];

			if (fieldLength > field.name.length)
				continue next;
			
			if (field.isSynthetic())
				continue next;

			if (!field.isStatic())
				continue next;

			if (!CharOperation.prefixEquals(fieldName, field.name, false/* ignore case */)
				&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(fieldName, field.name)))
				continue next;

			if (this.options.checkDeprecation && field.isViewedAsDeprecated()) continue next;
			
			if (this.options.checkVisibility
				&& !field.canBeSeenBy(this.unitScope.fPackage))
				continue next;
			
			char[] completionName = CharOperation.concat(
					field.declaringClass.qualifiedPackageName(),
					'.',
					field.declaringClass.qualifiedSourceName(),
					'.',
					field.name);
			
			completionName = CharOperation.concat(completionName, SEMICOLON);
			
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(fieldName, field.name);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);

			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.FIELD_REF)) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(field.declaringClass));
				proposal.setSignature(getSignature(field.type));
				proposal.setDeclarationPackageName(field.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(field.declaringClass.qualifiedSourceName());
				proposal.setPackageName(field.type.qualifiedPackageName());
				proposal.setTypeName(field.type.qualifiedSourceName()); 
				proposal.setName(field.name);
				proposal.setCompletion(completionName);
				proposal.setFlags(field.modifiers);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
		}
	}
	
	private void findImportsOfStaticMethods(char[] methodName, ReferenceBinding ref) {
		MethodBinding[] methods = ref.methods();
		
		int methodLength = methodName.length;
		next : for (int m = methods.length; --m >= 0;) {
			MethodBinding method = methods[m];

			if (method.isSynthetic()) continue next;

			if (method.isDefaultAbstract())	continue next;

			if (method.isConstructor()) continue next;

			if (!method.isStatic()) continue next;

			if (this.options.checkDeprecation && method.isViewedAsDeprecated()) continue next;
			
			if (this.options.checkVisibility
				&& !method.canBeSeenBy(this.unitScope.fPackage)) continue next;
			
			if (methodLength > method.selector.length)
				continue next;

			if (!CharOperation.prefixEquals(methodName, method.selector, false/* ignore case */)
					&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(methodName, method.selector)))
				continue next;
			
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];

			for (int i = 0; i < length; i++) {
				TypeBinding type = method.original().parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}
			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);

			
			char[] completionName = CharOperation.concat(
					method.declaringClass.qualifiedPackageName(),
					'.',
					method.declaringClass.qualifiedSourceName(),
					'.',
					method.selector);
			
			completionName = CharOperation.concat(completionName, SEMICOLON);
			
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(methodName, method.selector);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);

			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.METHOD_NAME_REFERENCE)) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_NAME_REFERENCE, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(method.declaringClass));
				proposal.setSignature(getSignature(method));
				proposal.setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
				proposal.setParameterPackageNames(parameterPackageNames);
				proposal.setParameterTypeNames(parameterTypeNames);
				proposal.setPackageName(method.returnType.qualifiedPackageName());
				proposal.setTypeName(method.returnType.qualifiedSourceName());
				proposal.setName(method.selector);
				proposal.setCompletion(completionName);
				proposal.setFlags(method.modifiers);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				if(parameterNames != null) proposal.setParameterNames(parameterNames);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
		}
	}

	/*
	 * Find javadoc block tags for a given completion javadoc tag node
	 */
	private void findJavadocBlockTags(CompletionOnJavadocTag javadocTag) {
		char[][] possibleTags = javadocTag.getPossibleBlockTags();
		if (possibleTags == null) return;
		int length = possibleTags.length;
		for (int i=0; i<length; i++) {
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for keywors
			
			this.noProposal = false;
			if (!this.requestor.isIgnored(CompletionProposal.JAVADOC_BLOCK_TAG)) {
				char[] possibleTag = possibleTags[i];
				CompletionProposal proposal = this.createProposal(CompletionProposal.JAVADOC_BLOCK_TAG, this.actualCompletionPosition);
				proposal.setName(possibleTag);
				int tagLength = possibleTag.length;
				char[] completion = new char[1+tagLength];
				completion[0] = '@';
				System.arraycopy(possibleTag, 0, completion, 1, tagLength);
				proposal.setCompletion(completion);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				this.requestor.accept(proposal);
				if (DEBUG) {
					this.printDebug(proposal);
				}
			}
		}
	}

	/*
	 * Find javadoc inline tags for a given completion javadoc tag node
	 */
	private void findJavadocInlineTags(CompletionOnJavadocTag javadocTag) {
		char[][] possibleTags = javadocTag.getPossibleInlineTags();
		if (possibleTags == null) return;
		int length = possibleTags.length;
		for (int i=0; i<length; i++) {
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for keywors
			
			this.noProposal = false;
			if (!this.requestor.isIgnored(CompletionProposal.JAVADOC_INLINE_TAG)) {
				char[] possibleTag = possibleTags[i];
				CompletionProposal proposal = this.createProposal(CompletionProposal.JAVADOC_INLINE_TAG, this.actualCompletionPosition);
				proposal.setName(possibleTag);
				int tagLength = possibleTag.length;
//				boolean inlineTagStarted = javadocTag.completeInlineTagStarted();
				char[] completion = new char[2+tagLength+1];
				completion[0] = '{';
				completion[1] = '@';
				System.arraycopy(possibleTag, 0, completion, 2, tagLength);
				// do not add space at end of inline tag (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=121026)
				//completion[tagLength+2] = ' ';
				completion[tagLength+2] = '}';
				proposal.setCompletion(completion);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				this.requestor.accept(proposal);
				if (DEBUG) {
					this.printDebug(proposal);
				}
			}
		}
	}

	// what about onDemand types? Ignore them since it does not happen!
	// import p1.p2.A.*;
	private void findKeywords(char[] keyword, char[][] choices, boolean canCompleteEmptyToken) {
		if(choices == null || choices.length == 0) return;
		
		int length = keyword.length;
		if (canCompleteEmptyToken || length > 0)
			for (int i = 0; i < choices.length; i++)
				if (length <= choices[i].length
					&& CharOperation.prefixEquals(keyword, choices[i], false /* ignore case */
				)){
					int relevance = computeBaseRelevance();
					relevance += computeRelevanceForInterestingProposal();
					relevance += computeRelevanceForCaseMatching(keyword, choices[i]);
					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for keywors
					
					if(CharOperation.equals(choices[i], Keywords.TRUE) || CharOperation.equals(choices[i], Keywords.FALSE)) {
						relevance += computeRelevanceForExpectingType(TypeBinding.BOOLEAN);
						relevance += computeRelevanceForQualification(false);
					}
					this.noProposal = false;
					if(!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
						CompletionProposal proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition);
						proposal.setName(choices[i]);
						proposal.setCompletion(choices[i]);
						proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
						proposal.setRelevance(relevance);
						this.requestor.accept(proposal);
						if(DEBUG) {
							this.printDebug(proposal);
						}
					}
				}
	}
	private void findTrueOrFalseKeywords(char[][] choices) {
		if(choices == null || choices.length == 0) return;
		
		if(this.expectedTypesPtr != 0 || this.expectedTypes[0] != TypeBinding.BOOLEAN) return;
		
		for (int i = 0; i < choices.length; i++) {
			if (CharOperation.equals(choices[i], Keywords.TRUE) ||
					CharOperation.equals(choices[i], Keywords.FALSE)
			){
				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(CharOperation.NO_CHAR, choices[i]);
				relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for keywors
				relevance += computeRelevanceForExpectingType(TypeBinding.BOOLEAN);
				relevance += computeRelevanceForQualification(false);
				relevance += R_TRUE_OR_FALSE;

				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
					CompletionProposal proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition);
					proposal.setName(choices[i]);
					proposal.setCompletion(choices[i]);
					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
					proposal.setRelevance(relevance);
					this.requestor.accept(proposal);
					if(DEBUG) {
						this.printDebug(proposal);
					}
				}
			}
		}
	}
	
	private void findKeywordsForMember(char[] token, int modifiers) {
		char[][] keywords = new char[Keywords.COUNT][];
		int count = 0;
				
		// visibility
		if((modifiers & ClassFileConstants.AccPrivate) == 0
			&& (modifiers & ClassFileConstants.AccProtected) == 0
			&& (modifiers & ClassFileConstants.AccPublic) == 0) {
			keywords[count++] = Keywords.PROTECTED;
			keywords[count++] = Keywords.PUBLIC;
			if((modifiers & ClassFileConstants.AccAbstract) == 0) {
				keywords[count++] = Keywords.PRIVATE;
			}
		}
		
		if((modifiers & ClassFileConstants.AccAbstract) == 0) {
			// abtract
			if((modifiers & ~(ExtraCompilerModifiers.AccVisibilityMASK | ClassFileConstants.AccStatic)) == 0) {
				keywords[count++] = Keywords.ABSTRACT;
			}
			
			// final
			if((modifiers & ClassFileConstants.AccFinal) == 0) {
				keywords[count++] = Keywords.FINAL;
			}
			
			// static
			if((modifiers & ClassFileConstants.AccStatic) == 0) {
				keywords[count++] = Keywords.STATIC;
			}
			
			boolean canBeField = true;
			boolean canBeMethod = true;
			boolean canBeType = true;
			if((modifiers & ClassFileConstants.AccNative) != 0
				|| (modifiers & ClassFileConstants.AccStrictfp) != 0
				|| (modifiers & ClassFileConstants.AccSynchronized) != 0) {
				canBeField = false;
				canBeType = false;
			}
			
			if((modifiers & ClassFileConstants.AccTransient) != 0
				|| (modifiers & ClassFileConstants.AccVolatile) != 0) {
				canBeMethod = false;
				canBeType = false;
			}
			
			if(canBeField) {
				// transient
				if((modifiers & ClassFileConstants.AccTransient) == 0) {
					keywords[count++] = Keywords.TRANSIENT;
				}
				
				// volatile
				if((modifiers & ClassFileConstants.AccVolatile) == 0) {
					keywords[count++] = Keywords.VOLATILE;
				}
			}
			
			if(canBeMethod) {
				// native
				if((modifiers & ClassFileConstants.AccNative) == 0) {
					keywords[count++] = Keywords.NATIVE;
				}
	
				// strictfp
				if((modifiers & ClassFileConstants.AccStrictfp) == 0) {
					keywords[count++] = Keywords.STRICTFP;
				}
				
				// synchronized
				if((modifiers & ClassFileConstants.AccSynchronized) == 0) {
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
		
		findKeywords(token, keywords, false);
	}

	// Helper method for findMemberTypes(char[], ReferenceBinding, Scope)
	private void findMemberTypes(
		char[] typeName,
		ReferenceBinding[] memberTypes,
		ObjectVector typesFound,
		ReferenceBinding receiverType,
		SourceTypeBinding invocationType,
		boolean staticOnly,
		boolean fromStaticImport,
		boolean checkQualification,
		Scope scope) {

		// Inherited member types which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		int typeLength = typeName.length;
		next : for (int m = memberTypes.length; --m >= 0;) {
			ReferenceBinding memberType = memberTypes[m];
			//		if (!wantClasses && memberType.isClass()) continue next;
			//		if (!wantInterfaces && memberType.isInterface()) continue next;
			
			if (staticOnly && !memberType.isStatic()) continue next;
			
			if (isForbidden(memberType)) continue next;
			
			if (typeLength > memberType.sourceName.length)
				continue next;

			if (!CharOperation.prefixEquals(typeName, memberType.sourceName, false/* ignore case */)
					&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(typeName, memberType.sourceName)))
				continue next;

			if (this.options.checkDeprecation &&
					memberType.isViewedAsDeprecated() &&
					!scope.isDefinedInSameUnit(memberType))
				continue next;
			
			if (this.options.checkVisibility) {
				if (invocationType != null && !memberType.canBeSeenBy(receiverType, invocationType)) {
					continue next;
				} else if(invocationType == null && !memberType.canBeSeenBy(this.unitScope.fPackage)) {
					continue next;
				}
			}

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

			if(!this.insideQualifiedReference && PROPOSE_MEMBER_TYPES) {
				if(this.assistNodeIsClass) {
					if(!memberType.isClass()) continue next;
				} else if(this.assistNodeIsInterface) {
					if(!memberType.isInterface() && !memberType.isAnnotationType()) continue next;
				} else if (this.assistNodeIsAnnotation) {
					if(!memberType.isAnnotationType()) continue next;
				}
			}
			
			char[] completionName = memberType.sourceName();
			
			boolean isQualified = false;
			if(checkQualification && !fromStaticImport) {
				char[] memberPackageName = memberType.qualifiedPackageName();
				char[] memberTypeName = memberType.sourceName();
				char[] memberEnclosingTypeNames = memberType.enclosingType().qualifiedSourceName();
				if (mustQualifyType(memberPackageName, memberTypeName, memberEnclosingTypeNames, memberType.modifiers)) {
					if (memberPackageName == null || memberPackageName.length == 0)
						if (this.unitScope != null && this.unitScope.fPackage.compoundName != CharOperation.NO_CHAR_CHAR)
							break next; // ignore types from the default package from outside it
					isQualified = true;
					completionName =
						CharOperation.concat(
								memberPackageName,
								CharOperation.concat(
										memberEnclosingTypeNames,
										memberTypeName,
										'.'),
								'.');
				}
			}
			
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(typeName, memberType.sourceName);
			relevance += computeRelevanceForExpectingType(memberType);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
			if(!insideQualifiedReference) {
				relevance += computeRelevanceForQualification(isQualified);
			}

			if (memberType.isClass()) {
				relevance += computeRelevanceForClass();
				relevance += computeRelevanceForException(memberType.sourceName);
			} else if(memberType.isEnum()) {
				relevance += computeRelevanceForEnum();
			} else if(memberType.isInterface()) {
				relevance += computeRelevanceForInterface();
			}
				
			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
				createTypeProposal(memberType, memberType.qualifiedSourceName(), IAccessRule.K_ACCESSIBLE, completionName, relevance);
			}
		}
	}

	private void findMemberTypes(
		char[] typeName,
		ReferenceBinding receiverType,
		Scope scope,
		SourceTypeBinding typeInvocation,
		boolean staticOnly,
		ObjectVector typesFound)  {
		findMemberTypes(
				typeName,
				receiverType,
				scope,
				typeInvocation,
				staticOnly,
				false,
				false,
				false,
				null,
				typesFound);
	}
	private void findMemberTypes(
		char[] typeName,
		ReferenceBinding receiverType,
		Scope scope,
		SourceTypeBinding typeInvocation,
		boolean staticOnly,
		boolean fromStaticImport,
		boolean checkQualification,
		boolean proposeAllMemberTypes,
		SourceTypeBinding typeToIgnore,
		ObjectVector typesFound) {

		ReferenceBinding currentType = receiverType;
		if (typeName == null)
			return;

		if (currentType.superInterfaces() == null)
			return; // we're trying to find a supertype

		if (this.insideQualifiedReference
			|| typeName.length == 0) { // do not search up the hierarchy

			findMemberTypes(
				typeName,
				currentType.memberTypes(),
				typesFound,
				receiverType,
				typeInvocation,
				staticOnly,
				fromStaticImport,
				checkQualification,
				scope);
			return;
		}

		ReferenceBinding[] interfacesToVisit = null;
		int nextPosition = 0;

		do {
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
				if (interfacesToVisit == null) {
					interfacesToVisit = itsInterfaces;
					nextPosition = interfacesToVisit.length;
				} else {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
			
			findMemberTypes(
				typeName,
				currentType.memberTypes(),
				typesFound,
				receiverType,
				typeInvocation,
				staticOnly,
				fromStaticImport,
				checkQualification,
				scope);
			
			currentType = currentType.superclass();
		} while (currentType != null);

		if(proposeAllMemberTypes) {
			ReferenceBinding[] memberTypes = receiverType.memberTypes();
			for (int i = 0; i < memberTypes.length; i++) {
				if(memberTypes[i] != typeToIgnore) {
					findSubMemberTypes(
						typeName,
						memberTypes[i],
						scope,
						typeInvocation,
						staticOnly,
						fromStaticImport,
						typesFound);
				}
			}
		}

		if (interfacesToVisit != null) {
			for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding anInterface = interfacesToVisit[i];
				findMemberTypes(
					typeName,
					anInterface.memberTypes(),
					typesFound,
					receiverType,
					typeInvocation,
					staticOnly,
					fromStaticImport,
					checkQualification,
					scope);
						
				ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
				if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
		}
	}

	/*
	 * Find javadoc parameter names.
	 */
	private void findJavadocParamNames(char[] token, char[][] missingParams, boolean isTypeParam) {

		if (missingParams == null) return;

		// Get relevance
		int relevance = computeBaseRelevance();
		relevance += computeRelevanceForInterestingProposal();
		relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for param name
		if (!isTypeParam) relevance += R_INTERESTING;

		// Propose missing param
		int length = missingParams.length;
		relevance += length;
		for (int i=0; i<length; i++) {
			char[] argName = missingParams[i];
			if (token == null || CharOperation.prefixEquals(token, argName)) {
				
				this.noProposal = false;
				if (!this.requestor.isIgnored(CompletionProposal.JAVADOC_PARAM_REF)) {
					CompletionProposal proposal = this.createProposal(CompletionProposal.JAVADOC_PARAM_REF, this.actualCompletionPosition);
					proposal.setName(argName);
					char[] completion = isTypeParam ? CharOperation.concat('<', argName, '>') : argName;
					proposal.setCompletion(completion);
					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
					proposal.setRelevance(--relevance);
					this.requestor.accept(proposal);
					if (DEBUG) {
						this.printDebug(proposal);
					}
				}
			}
		}
	}

	private void findSubMemberTypes(
		char[] typeName,
		ReferenceBinding receiverType,
		Scope scope,
		SourceTypeBinding typeInvocation,
		boolean staticOnly,
		boolean fromStaticImport,
		ObjectVector typesFound) {

		ReferenceBinding currentType = receiverType;
		if (typeName == null || typeName.length == 0)
			return;

		if (currentType.superInterfaces() == null)
			return; // we're trying to find a supertype
		
		findMemberTypes(
				typeName,
				currentType.memberTypes(),
				typesFound,
				receiverType,
				typeInvocation,
				staticOnly,
				fromStaticImport,
				true,
				scope);
		
		ReferenceBinding[] memberTypes = receiverType.memberTypes();
		next : for (int i = 0; i < memberTypes.length; i++) {
			if (this.options.checkVisibility) {
				if (typeInvocation != null && !memberTypes[i].canBeSeenBy(receiverType, typeInvocation)) {
					continue next;
				} else if(typeInvocation == null && !memberTypes[i].canBeSeenBy(this.unitScope.fPackage)) {
					continue next;
				}
			}
			findSubMemberTypes(
				typeName,
				memberTypes[i],
				scope,
				typeInvocation,
				staticOnly,
				fromStaticImport,
				typesFound);
		}
	}

	private void findInterfacesMethods(
		char[] selector,
		TypeBinding[] typeArgTypes,
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
		boolean implicitCall,
		boolean superCall,
		boolean canBePrefixed) {

		if (selector == null)
			return;

		if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
			ReferenceBinding[] interfacesToVisit = itsInterfaces;
			int nextPosition = interfacesToVisit.length;

			for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding currentType = interfacesToVisit[i];
				MethodBinding[] methods = currentType.availableMethods();
				if(methods != null) {
					if(isCompletingDeclaration) {
						findLocalMethodDeclarations(
							selector,
							methods,
							scope,
							methodsFound,
							exactMatch,
							receiverType);
					} else {
						findLocalMethods(
							selector,
							typeArgTypes,
							argTypes,
							methods,
							scope,
							methodsFound,
							onlyStaticMethods,
							exactMatch,
							receiverType,
							invocationSite,
							invocationScope,
							implicitCall,
							superCall,
							canBePrefixed);
					}
				}

				if ((itsInterfaces = currentType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
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
						null,
						argTypes,
						enclosingType,
						classScope,
						methodsFound,
						staticsOnly,
						true,
						false,
						invocationSite,
						invocationScope,
						true,
						false,
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
		TypeBinding[] typeArgTypes,
		TypeBinding[] argTypes,
		MethodBinding[] methods,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		ReferenceBinding receiverType,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall,
		boolean superCall,
		boolean canBePrefixed) {

		ObjectVector newMethodsFound =  new ObjectVector();
		// Inherited methods which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite

		int methodLength = methodName.length;
		int minTypeArgLength = typeArgTypes == null ? 0 : typeArgTypes.length;
		int minArgLength = argTypes == null ? 0 : argTypes.length;

		next : for (int f = methods.length; --f >= 0;) {
			MethodBinding method = methods[f];

			if (method.isSynthetic()) continue next;

			if (method.isDefaultAbstract())	continue next;

			if (method.isConstructor()) continue next;
			
			if (this.options.checkDeprecation &&
					method.isViewedAsDeprecated() &&
					!scope.isDefinedInSameUnit(method.declaringClass))
				continue next;

			//TODO (david) perhaps the relevance of a void method must be lesser than other methods
			//if (expectedTypesPtr > -1 && method.returnType == BaseTypes.VoidBinding) continue next;
			
			if (onlyStaticMethods && !method.isStatic()) continue next;

			if (this.options.checkVisibility
				&& !method.canBeSeenBy(receiverType, invocationSite, scope)) continue next;

			if(superCall && method.isAbstract()) {
				methodsFound.add(new Object[]{method, receiverType});
				continue next;
			}

			if (exactMatch) {
				if (!CharOperation.equals(methodName, method.selector, false /* ignore case */)) {
					continue next;
				}
			} else {
				if (methodLength > method.selector.length) continue next;
				if (!CharOperation.prefixEquals(methodName, method.selector, false /* ignore case */)
						&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(methodName, method.selector))) {
					continue next;
				}
			}
			
			if (minTypeArgLength != 0 && minTypeArgLength != method.typeVariables.length)
				continue next;
			
			if (minTypeArgLength != 0) {
				method = scope.environment().createParameterizedGenericMethod(method, typeArgTypes);
			}
			
			if (minArgLength > method.parameters.length)
				continue next;

			for (int a = minArgLength; --a >= 0;){
				if (argTypes[a] != null) { // can be null if it could not be resolved properly
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
					&& lookupEnvironment.methodVerifier().doesMethodOverride(otherMethod, method)) {

					if (method.declaringClass.isSuperclassOf(otherMethod.declaringClass))
						continue next;

					if (otherMethod.declaringClass.isInterface()) {
						if(method.declaringClass == scope.getJavaLangObject())
							continue next;
						
						if (method.declaringClass.isInterface())
							continue next;
						
						if (!superCall && method
							.declaringClass
							.implementsInterface(otherMethod.declaringClass, true))
							continue next;
					}

					if (method.declaringClass.isInterface())
						if(otherMethod
							.declaringClass
							.implementsInterface(method.declaringClass,true))
							continue next;
						
					if(receiverType.isAnonymousType()) continue next;
					
					if(!superCall) {
						if(canBePrefixed) {
							prefixRequired = true;
						} else {
							continue next;
						}
					}
				}
			}

			newMethodsFound.add(new Object[]{method, receiverType});
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];

			for (int i = 0; i < length; i++) {
				TypeBinding type = method.original().parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}
			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);

			char[] completion = CharOperation.NO_CHAR;
			
			int previousStartPosition = this.startPosition;

			// Special case for completion in javadoc
			if (this.assistNodeInJavadoc > 0) {
				Expression receiver = null;
				if (invocationSite instanceof CompletionOnJavadocMessageSend) {
					CompletionOnJavadocMessageSend msg = (CompletionOnJavadocMessageSend) invocationSite;
					receiver = msg.receiver;
				} else if (invocationSite instanceof CompletionOnJavadocFieldReference) {
					CompletionOnJavadocFieldReference fieldRef = (CompletionOnJavadocFieldReference) invocationSite;
					receiver = fieldRef.receiver;
				}
				if (receiver != null) {
					StringBuffer javadocCompletion = new StringBuffer();
					if (receiver.isThis()) {
						if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0) {
							javadocCompletion.append('#');
						}
					} else if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0) {
						if (receiver instanceof JavadocSingleTypeReference) {
							JavadocSingleTypeReference typeRef = (JavadocSingleTypeReference) receiver;
							javadocCompletion.append(typeRef.token);
							javadocCompletion.append('#');
						} else if (receiver instanceof JavadocQualifiedTypeReference) {
							JavadocQualifiedTypeReference typeRef = (JavadocQualifiedTypeReference) receiver;
							completion = CharOperation.concat(CharOperation.concatWith(typeRef.tokens, '.'), method.selector, '#');
							for (int t=0,nt =typeRef.tokens.length; t<nt; t++) {
								if (t>0) javadocCompletion.append('.');
								javadocCompletion.append(typeRef.tokens[t]);
							}
							javadocCompletion.append('#');
						}
					}
					javadocCompletion.append(method.selector);
					// Append parameters types
					javadocCompletion.append('(');
					if (method.parameters != null) {
						for (int p=0, ln=method.parameters.length; p<ln; p++) {
							if (p>0) javadocCompletion.append(", "); //$NON-NLS-1$
							TypeBinding argTypeBinding = method.parameters[p];
							createType(argTypeBinding.erasure(), javadocCompletion);
						}
					}
					javadocCompletion.append(')');
					completion = javadocCompletion.toString().toCharArray();
				}
			} else {
				// nothing to insert - do not want to replace the existing selector & arguments
				if (!exactMatch) {
					if (this.source != null
						&& this.source.length > this.endPosition
						&& this.source[this.endPosition] == '(')
						completion = method.selector;
					else
						completion = CharOperation.concat(method.selector, new char[] { '(', ')' });
				} else {
					if(prefixRequired && (this.source != null)) {
						completion = CharOperation.subarray(this.source, this.startPosition, this.endPosition);
					} else {
						this.startPosition = this.endPosition;
					}
				}
				
				if(prefixRequired || this.options.forceImplicitQualification){
					char[] prefix = computePrefix(scope.enclosingSourceType(), invocationScope.enclosingSourceType(), method.isStatic());
					completion = CharOperation.concat(prefix,completion,'.');
				}
			}

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			if (methodName != null) relevance += computeRelevanceForCaseMatching(methodName, method.selector);
			relevance += computeRelevanceForExpectingType(method.returnType);
			relevance += computeRelevanceForStatic(onlyStaticMethods, method.isStatic());
			relevance += computeRelevanceForQualification(prefixRequired);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
			
			this.noProposal = false;
			// Standard proposal
			if(!this.requestor.isIgnored(CompletionProposal.METHOD_REF) && (this.assistNodeInJavadoc & CompletionOnJavadoc.ONLY_INLINE_TAG) == 0) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_REF, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(method.declaringClass));
				proposal.setSignature(getSignature(method));
				MethodBinding original = method.original();
				if(original != method) {
					proposal.setOriginalSignature(getSignature(original));
				}
				proposal.setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
				proposal.setParameterPackageNames(parameterPackageNames);
				proposal.setParameterTypeNames(parameterTypeNames);
				proposal.setPackageName(method.returnType.qualifiedPackageName());
				proposal.setTypeName(method.returnType.qualifiedSourceName());
				proposal.setName(method.selector);
				proposal.setCompletion(completion);
				proposal.setFlags(method.modifiers);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				if(parameterNames != null) proposal.setParameterNames(parameterNames);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}

			// Javadoc proposal
			if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0 && !this.requestor.isIgnored(CompletionProposal.JAVADOC_METHOD_REF)) {
				char[] javadocCompletion = inlineTagCompletion(completion, JavadocTagConstants.TAG_LINK);
				CompletionProposal proposal = this.createProposal(CompletionProposal.JAVADOC_METHOD_REF, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(method.declaringClass));
				proposal.setSignature(getSignature(method));
				MethodBinding original = method.original();
				if(original != method) {
					proposal.setOriginalSignature(getSignature(original));
				}
				proposal.setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
				proposal.setParameterPackageNames(parameterPackageNames);
				proposal.setParameterTypeNames(parameterTypeNames);
				proposal.setPackageName(method.returnType.qualifiedPackageName());
				proposal.setTypeName(method.returnType.qualifiedSourceName());
				proposal.setName(method.selector);
				proposal.setCompletion(javadocCompletion);
				proposal.setFlags(method.modifiers);
				int start = (this.assistNodeInJavadoc & CompletionOnJavadoc.REPLACE_TAG) != 0 ? this.javadocTagPosition : this.startPosition;
				proposal.setReplaceRange(start - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance+R_INLINE_TAG);
				if(parameterNames != null) proposal.setParameterNames(parameterNames);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
			this.startPosition = previousStartPosition;
		}
		
		methodsFound.addAll(newMethodsFound);
	}
	
	// Helper method for findMethods(char[], TypeBinding[], ReferenceBinding, Scope, ObjectVector, boolean, boolean, boolean)
	private void findLocalMethodsOfStaticImports(
		char[] methodName,
		MethodBinding[] methods,
		Scope scope,
		ReferenceBinding receiverType,
		InvocationSite invocationSite) {

		next : for (int f = methods.length; --f >= 0;) {
			MethodBinding method = methods[f];

			if (method.isSynthetic()) continue next;

			if (method.isDefaultAbstract())	continue next;

			if (method.isConstructor()) continue next;

			if (!method.isStatic()) continue next;
			
			if (this.options.checkDeprecation &&
					method.isViewedAsDeprecated() &&
					!scope.isDefinedInSameUnit(method.declaringClass))
				continue next;
			
			if (this.options.checkVisibility
				&& !method.canBeSeenBy(receiverType, invocationSite, scope)) continue next;

			if (!CharOperation.equals(methodName, method.selector, false /* ignore case */)
					&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(methodName, method.selector)))
				continue next;

			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];

			for (int i = 0; i < length; i++) {
				TypeBinding type = method.original().parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}
			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);

			char[] completion = CharOperation.NO_CHAR;
			
			int previousStartPosition = this.startPosition;
			
			// nothing to insert - do not want to replace the existing selector & arguments
			if (this.source != null
				&& this.source.length > this.endPosition
				&& this.source[this.endPosition] == '(') {
				completion = method.selector;
			} else {
				completion = CharOperation.concat(method.selector, new char[] { '(', ')' });
			}
			
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(methodName, method.selector);
			relevance += computeRelevanceForExpectingType(method.returnType);
			relevance += computeRelevanceForStatic(true, method.isStatic());
			relevance += computeRelevanceForQualification(false);
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
			
			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.METHOD_REF)) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_REF, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(method.declaringClass));
				proposal.setSignature(getSignature(method));
				MethodBinding original = method.original();
				if(original != method) {
					proposal.setOriginalSignature(getSignature(original));
				}
				proposal.setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
				proposal.setParameterPackageNames(parameterPackageNames);
				proposal.setParameterTypeNames(parameterTypeNames);
				proposal.setPackageName(method.returnType.qualifiedPackageName());
				proposal.setTypeName(method.returnType.qualifiedSourceName());
				proposal.setName(method.selector);
				proposal.setCompletion(completion);
				proposal.setFlags(method.modifiers);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				if(parameterNames != null) proposal.setParameterNames(parameterNames);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
			this.startPosition = previousStartPosition;
		}
	}
	int computeRelevanceForCaseMatching(char[] token, char[] proposalName){
		if (this.options.camelCaseMatch) {
			if(CharOperation.equals(token, proposalName, true /* do not ignore case */)) {
				return R_CASE + R_EXACT_NAME;
			} else if (CharOperation.prefixEquals(token, proposalName, true /* do not ignore case */)) {
				return R_CASE;
			} else if (CharOperation.camelCaseMatch(token, proposalName)){
				return R_CAMEL_CASE;
			} else if(CharOperation.equals(token, proposalName, false /* ignore case */)) {
				return R_EXACT_NAME;
			}
		} else if (CharOperation.prefixEquals(token, proposalName, true /* do not ignore case */)) {
			if(CharOperation.equals(token, proposalName, true /* do not ignore case */)) {
				return R_CASE + R_EXACT_NAME;
			} else {
				return R_CASE;
			}
		} else if(CharOperation.equals(token, proposalName, false /* ignore case */)) {
			return R_EXACT_NAME;
		}
		return 0;
	}
	private int computeRelevanceForAnnotation(){
		if(this.assistNodeIsAnnotation) {
			return R_ANNOTATION;
		}
		return 0;
	}
	private int computeRelevanceForClass(){
		if(this.assistNodeIsClass) {
			return R_CLASS;
		}
		return 0;
	}
	private int computeRelevanceForEnum(){
		if(this.assistNodeIsEnum) {
			return R_ENUM;
		}
		return 0;
	}
	private int computeRelevanceForInterface(){
		if(this.assistNodeIsInterface) {
			return R_INTERFACE;
		}
		return 0;
	}
	private int computeRelevanceForQualification(boolean prefixRequired) {
		if(!prefixRequired && !this.insideQualifiedReference) {
			return R_UNQUALIFIED;
		}
		
		if(prefixRequired && this.insideQualifiedReference) {
			return R_QUALIFIED;
		}
		return 0;
	}
	int computeRelevanceForRestrictions(int accessRuleKind) {
		if(accessRuleKind == IAccessRule.K_ACCESSIBLE) {
			return R_NON_RESTRICTED;
		}
		return 0;
	}
	private int computeRelevanceForStatic(boolean onlyStatic, boolean isStatic) {
		if(this.insideQualifiedReference && !onlyStatic && !isStatic) {
			return R_NON_STATIC;
		}
		return 0;
	}
	private int computeRelevanceForException(char[] proposalName){
		
		if((this.assistNodeIsException || (this.assistNodeInJavadoc & CompletionOnJavadoc.EXCEPTION) != 0 )&&
			(CharOperation.match(EXCEPTION_PATTERN, proposalName, false) ||
			CharOperation.match(ERROR_PATTERN, proposalName, false))) { 
			return R_EXCEPTION;
		}
		return 0;
	}
	private int computeRelevanceForExpectingType(TypeBinding proposalType){
		if(this.expectedTypes != null && proposalType != null) {
			for (int i = 0; i <= this.expectedTypesPtr; i++) {
                int relevance = R_EXPECTED_TYPE;
				if(CharOperation.equals(this.expectedTypes[i].qualifiedPackageName(), proposalType.qualifiedPackageName()) &&
					CharOperation.equals(this.expectedTypes[i].qualifiedSourceName(), proposalType.qualifiedSourceName())) {
                    relevance = R_EXACT_EXPECTED_TYPE;
				}
				if((this.expectedTypesFilter & SUBTYPE) != 0
					&& proposalType.isCompatibleWith(this.expectedTypes[i])) {
						return relevance;
				}
				if((this.expectedTypesFilter & SUPERTYPE) != 0
					&& this.expectedTypes[i].isCompatibleWith(proposalType)) {
					return relevance;
				}
			}
		} 
		return 0;
	}
	private int computeRelevanceForExpectingType(char[] packageName, char[] typeName){
		if(this.expectedTypes != null) {
			for (int i = 0; i <= this.expectedTypesPtr; i++) {
				if(CharOperation.equals(this.expectedTypes[i].qualifiedPackageName(), packageName) &&
					CharOperation.equals(this.expectedTypes[i].qualifiedSourceName(), typeName)) {
					return R_EXACT_EXPECTED_TYPE;
				}
			}
			if(this.hasJavaLangObjectAsExpectedType) {
				return R_EXPECTED_TYPE;
			}
		} 
		return 0;
	}
	int computeRelevanceForInterestingProposal(){
		return computeRelevanceForInterestingProposal(null);
	}
	private int computeRelevanceForInterestingProposal(Binding binding){
		if(this.uninterestingBindings != null) {
			for (int i = 0; i <= this.uninterestingBindingsPtr; i++) {
				if(this.uninterestingBindings[i] == binding) {
					return 0;
				}
			}
		}
		return R_INTERESTING;
	}
	private void computeUninterestingBindings(ASTNode parent, Scope scope){
		if(parent instanceof LocalDeclaration) {
			addUninterestingBindings(((LocalDeclaration)parent).binding);
		} else if (parent instanceof FieldDeclaration) {
			addUninterestingBindings(((FieldDeclaration)parent).binding);
		} 
	}
	
	private void findLabels(char[] label, char[][] choices) {
		if(choices == null || choices.length == 0) return;
		
		int length = label.length;
		for (int i = 0; i < choices.length; i++) {
			if (length <= choices[i].length
				&& CharOperation.prefixEquals(label, choices[i], false /* ignore case */
			)){
				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(label, choices[i]);
				relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for keywors
				
				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.LABEL_REF)) {
					CompletionProposal proposal = this.createProposal(CompletionProposal.LABEL_REF, this.actualCompletionPosition);
					proposal.setName(choices[i]);
					proposal.setCompletion(choices[i]);
					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
					proposal.setRelevance(relevance);
					this.requestor.accept(proposal);
					if(DEBUG) {
						this.printDebug(proposal);
					}
				}
			}
		}
	}
	
	// Helper method for findMethods(char[], MethodBinding[], Scope, ObjectVector, boolean, boolean, boolean, TypeBinding)
	private void findLocalMethodDeclarations(
		char[] methodName,
		MethodBinding[] methods,
		Scope scope,
		ObjectVector methodsFound,
		//	boolean noVoidReturnType, how do you know?
		boolean exactMatch,
		ReferenceBinding receiverType) {

		ObjectVector newMethodsFound =  new ObjectVector();
		// Inherited methods which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		int methodLength = methodName.length;
		next : for (int f = methods.length; --f >= 0;) {

			MethodBinding method = methods[f];
			if (method.isSynthetic())	continue next;
				
			if (method.isDefaultAbstract()) continue next;
			
			if (method.isConstructor()) continue next;
				
			if (method.isFinal()) {
                newMethodsFound.add(method);
                continue next;
            }
			
			if (this.options.checkDeprecation &&
					method.isViewedAsDeprecated() &&
					!scope.isDefinedInSameUnit(method.declaringClass))
				continue next;

			//		if (noVoidReturnType && method.returnType == BaseTypes.VoidBinding) continue next;
			if(method.isStatic()) continue next;

			if (!method.canBeSeenBy(receiverType, FakeInvocationSite , scope)) continue next;

			if (exactMatch) {
				if (!CharOperation.equals(methodName, method.selector, false /* ignore case */
					))
					continue next;

			} else {

				if (methodLength > method.selector.length)
					continue next;

				if (!CharOperation.prefixEquals(methodName, method.selector, false/* ignore case */)
						&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(methodName, method.selector)))
					continue next;
			}

			for (int i = methodsFound.size; --i >= 0;) {
				MethodBinding otherMethod = (MethodBinding) methodsFound.elementAt(i);
				if (method == otherMethod)
					continue next;

				if (CharOperation.equals(method.selector, otherMethod.selector, true)
						&& lookupEnvironment.methodVerifier().doesMethodOverride(otherMethod, method)) {
					continue next;
				}
			}

			newMethodsFound.add(method);
			
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterFullTypeNames = new char[length][];
			
			for (int i = 0; i < length; i++) {
				TypeBinding type = method.parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterFullTypeNames[i] = type.qualifiedSourceName();
			}

			char[][] parameterNames = findMethodParameterNames(method, parameterFullTypeNames);
			
			if(method.typeVariables != null && method.typeVariables.length > 0) {
				char[][] excludedNames = findEnclosingTypeNames(scope);
				char[][] substituedParameterNames = substituteMethodTypeParameterNames(method.typeVariables, excludedNames);
				if(substituedParameterNames != null) {
					method = new ParameterizedMethodBinding(
								method.declaringClass,
								method,
								substituedParameterNames,
								scope.environment());
				}
			}
			
			StringBuffer completion = new StringBuffer(10);
			if (!exactMatch) {
				createMethod(method, parameterPackageNames, parameterFullTypeNames, parameterNames, completion);
			}

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(methodName, method.selector);
			relevance += R_METHOD_OVERIDE;
			if(method.isAbstract()) relevance += R_ABSTRACT_METHOD;
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
			
			this.noProposal = false;
			if(!this.requestor.isIgnored(CompletionProposal.METHOD_DECLARATION)) {
				CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_DECLARATION, this.actualCompletionPosition);
				proposal.setDeclarationSignature(getSignature(method.declaringClass));
				proposal.setDeclarationKey(method.declaringClass.computeUniqueKey());
				proposal.setSignature(getSignature(method));
				MethodBinding original = method.original();
				if(original != method) {
					proposal.setOriginalSignature(getSignature(original));
				}
				proposal.setKey(method.computeUniqueKey());
				proposal.setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
				proposal.setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
				proposal.setParameterPackageNames(parameterPackageNames);
				proposal.setParameterTypeNames(parameterFullTypeNames);
				proposal.setPackageName(method.returnType.qualifiedPackageName());
				proposal.setTypeName(method.returnType.qualifiedSourceName());
				proposal.setCompletion(completion.toString().toCharArray());
				proposal.setName(method.selector);
				proposal.setFlags(method.modifiers);
				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
				proposal.setRelevance(relevance);
				if(parameterNames != null) proposal.setParameterNames(parameterNames);
				this.requestor.accept(proposal);
				if(DEBUG) {
					this.printDebug(proposal);
				}
			}
		}
		methodsFound.addAll(newMethodsFound);
	}
	
	private void createTypeVariable(TypeVariableBinding typeVariable, StringBuffer completion) {
		completion.append(typeVariable.sourceName);
		
		if (typeVariable.superclass != null && typeVariable.firstBound == typeVariable.superclass) {
		    completion.append(' ');
		    completion.append(EXTENDS);
		    completion.append(' ');
		    createType(typeVariable.superclass, completion);
		}
		if (typeVariable.superInterfaces != null && typeVariable.superInterfaces != Binding.NO_SUPERINTERFACES) {
		   if (typeVariable.firstBound != typeVariable.superclass) {
			   completion.append(' ');
			   completion.append(EXTENDS);
			   completion.append(' ');
		   }
		   for (int i = 0, length = typeVariable.superInterfaces.length; i < length; i++) {
			   if (i > 0 || typeVariable.firstBound == typeVariable.superclass) {
				   completion.append(' ');
				   completion.append(EXTENDS);
				   completion.append(' ');
			   }
			   createType(typeVariable.superInterfaces[i], completion);
		   }
		}
	}
	
	private void createType(TypeBinding type, StringBuffer completion) {
		if (type.isBaseType()) {
			completion.append(type.sourceName());
		} else if (type.isTypeVariable()) {
			completion.append(type.sourceName());
		} else if (type.isWildcard()) {
			WildcardBinding wildcardBinding = (WildcardBinding) type;
			completion.append('?');
			switch (wildcardBinding.boundKind) {
				case Wildcard.EXTENDS:
					completion.append(' ');
					completion.append(EXTENDS);
					completion.append(' ');
					createType(wildcardBinding.bound, completion);
					if(wildcardBinding.otherBounds != null) {
						
						int length = wildcardBinding.otherBounds.length;
						for (int i = 0; i < length; i++) {
							completion.append(' ');
							completion.append('&');
							completion.append(' ');
							createType(wildcardBinding.otherBounds[i], completion);
						}
					}
					break;
				case Wildcard.SUPER:
					completion.append(' ');
					completion.append(SUPER);
					completion.append(' ');
					createType(wildcardBinding.bound, completion);
					break;
			}
		} else if (type.isArrayType()) {
			createType(type.leafComponentType(), completion);
			int dim = type.dimensions();
			for (int i = 0; i < dim; i++) {
				completion.append('[');
				completion.append(']');
			}
		} else if (type.isParameterizedType()) {
			ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) type;
			if (type.isMemberType()) {
				createType(parameterizedType.enclosingType(), completion);
				completion.append('.');
				completion.append(parameterizedType.sourceName);
			} else {
				completion.append(CharOperation.concatWith(parameterizedType.type.compoundName, '.'));
			}	    
			if (parameterizedType.arguments != null) {
				completion.append('<');
			    for (int i = 0, length = parameterizedType.arguments.length; i < length; i++) {
			        if (i != 0) completion.append(',');
			        createType(parameterizedType.arguments[i], completion);
			    }
			    completion.append('>');
			}
		} else {
			char[] packageName = type.qualifiedPackageName();
			char[] typeName = type.qualifiedSourceName();
			if(mustQualifyType(
					packageName,
					type.sourceName(),
					type.isMemberType() ? type.enclosingType().qualifiedSourceName() : null,
					((ReferenceBinding)type).modifiers)) {
				completion.append(CharOperation.concat(packageName, typeName,'.'));
			} else {
				completion.append(type.sourceName());
			}
		}
	}
	private void createMethod(MethodBinding method, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, StringBuffer completion) {
		//// Modifiers
		// flush uninteresting modifiers
		int insertedModifiers = method.modifiers & ~(ClassFileConstants.AccNative | ClassFileConstants.AccAbstract);	
		if(insertedModifiers != ClassFileConstants.AccDefault){
			ASTNode.printModifiers(insertedModifiers, completion);
		}
		
		//// Type parameters
		
		TypeVariableBinding[] typeVariableBindings = method.typeVariables;
		if(typeVariableBindings != null && typeVariableBindings.length != 0) {
			completion.append('<');
			for (int i = 0; i < typeVariableBindings.length; i++) {
				if(i != 0) {
					completion.append(',');
					completion.append(' ');
				}
				createTypeVariable(typeVariableBindings[i], completion);
			}
			completion.append('>');
			completion.append(' ');
		}
		
		//// Return type
		createType(method.returnType, completion);
		completion.append(' ');
		
		//// Selector
		completion.append(method.selector);
		
		completion.append('(');

		////Parameters
		TypeBinding[] parameterTypes = method.parameters;
		int length = parameterTypes.length;
		for (int i = 0; i < length; i++) {
			if(i != 0) {
				completion.append(',');
				completion.append(' ');
			}
			createType(parameterTypes[i], completion);
			completion.append(' ');
			if(parameterNames != null){
				completion.append(parameterNames[i]);
			} else {
				completion.append('%');
			}
		}
		
		completion.append(')');
		
		//// Exceptions
		ReferenceBinding[] exceptions = method.thrownExceptions;
		
		if (exceptions != null && exceptions.length > 0){
			completion.append(' ');
			completion.append(THROWS);
			completion.append(' ');
			for(int i = 0; i < exceptions.length ; i++){
				if(i != 0) {
					completion.append(' ');
					completion.append(',');
				}
				createType(exceptions[i], completion);
			}
		}
	}

	private void findMethods(
		char[] selector,
		TypeBinding[] typeArgTypes,
		TypeBinding[] argTypes,
		ReferenceBinding receiverType,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		boolean isCompletingDeclaration,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall,
		boolean superCall,
		boolean canBePrefixed) {

		boolean notInJavadoc = this.assistNodeInJavadoc == 0;
		if (selector == null && notInJavadoc) {
			return;
		}
		
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
		if (notInJavadoc) {
			if (receiverType.isInterface()) {
				if (isCompletingDeclaration) {
					findInterfacesMethods(
						selector,
						typeArgTypes,
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
						implicitCall,
						superCall,
						canBePrefixed);
				} else {
					findInterfacesMethods(
						selector,
						typeArgTypes,
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
						implicitCall,
						superCall,
						canBePrefixed);
				}
				
				currentType = scope.getJavaLangObject();
			} else {
				if (isCompletingDeclaration){
					findInterfacesMethods(
						selector,
						typeArgTypes,
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
						implicitCall,
						superCall,
						canBePrefixed);
					
					currentType = receiverType.superclass();
				}
			}
		}
		boolean hasPotentialDefaultAbstractMethods = true;
		while (currentType != null) {
			
			MethodBinding[] methods = currentType.availableMethods();
			if (methods != null) {
				if (isCompletingDeclaration){
					findLocalMethodDeclarations(
						selector,
						methods,
						scope,
						methodsFound,
						exactMatch,
						receiverType);
				} else{
					findLocalMethods(
						selector,
						typeArgTypes,
						argTypes,
						methods,
						scope,
						methodsFound,
						onlyStaticMethods,
						exactMatch,
						receiverType,
						invocationSite,
						invocationScope,
						implicitCall,
						superCall,
						canBePrefixed);
				}
			}
			
			if (notInJavadoc && hasPotentialDefaultAbstractMethods && (currentType.isAbstract() || currentType.isTypeVariable())){
				findInterfacesMethods(
					selector,
					typeArgTypes,
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
					implicitCall,
					superCall,
					canBePrefixed);
			} else {
				hasPotentialDefaultAbstractMethods = false;
			}
			currentType = currentType.superclass();
		}
	}
	private char[][] findMethodParameterNames(MethodBinding method, char[][] parameterTypeNames){
		TypeBinding erasure =  method.declaringClass.erasure();
		if(!(erasure instanceof ReferenceBinding)) return null;

		char[][] parameterNames = null;
		
		int length = parameterTypeNames.length;

		if (length == 0){
			return CharOperation.NO_CHAR_CHAR;
		}
		// look into the corresponding unit if it is available
		if (erasure instanceof SourceTypeBinding){
			SourceTypeBinding sourceType = (SourceTypeBinding) erasure;

			if (sourceType.scope != null){
				TypeDeclaration parsedType;

				if ((parsedType = sourceType.scope.referenceContext) != null){
					AbstractMethodDeclaration methodDecl = parsedType.declarationOf(method.original());

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
			
			ReferenceBinding bindingType = (ReferenceBinding)erasure;
			
			char[] compoundName = CharOperation.concatWith(bindingType.compoundName, '.');
			Object type = this.typeCache.get(compoundName);
			
			ISourceType sourceType = null;
			if(type != null) {
				if(type instanceof ISourceType) {
					sourceType = (ISourceType) type;
				}
			} else {
				NameEnvironmentAnswer answer = this.nameEnvironment.findType(bindingType.compoundName);
				if(answer != null && answer.isSourceType()) {
					sourceType = answer.getSourceTypes()[0];
					this.typeCache.put(compoundName, sourceType);
				}
			}
			
			if(sourceType != null) {
				IType typeHandle = ((SourceTypeElementInfo) sourceType).getHandle();
				
				String[] parameterTypeSignatures = new String[length];
				for (int i = 0; i < length; i++) {
					parameterTypeSignatures[i] = Signature.createTypeSignature(parameterTypeNames[i], false);
				}
				IMethod searchedMethod = typeHandle.getMethod(String.valueOf(method.selector), parameterTypeSignatures);
				IMethod[] foundMethods = typeHandle.findMethods(searchedMethod);
				
				if(foundMethods != null) {
					int len = foundMethods.length;
					if(len == 1) {
						try {
							SourceMethod sourceMethod = (SourceMethod) foundMethods[0];
							parameterNames = ((SourceMethodElementInfo) sourceMethod.getElementInfo()).getArgumentNames();
						} catch (JavaModelException e) {
							// method doesn't exist: ignore
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
		Scope scope,
		boolean proposeAllMemberTypes,
		ObjectVector typesFound) {
		if (typeName == null)
			return;

		int typeLength = typeName.length;

		SourceTypeBinding nextTypeToIgnore = null;
		while (scope != null) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (scope.kind) {

				case Scope.METHOD_SCOPE :
				case Scope.BLOCK_SCOPE :
					BlockScope blockScope = (BlockScope) scope;

					next : for (int i = 0, length = blockScope.subscopeCount; i < length; i++) {

						if (blockScope.subscopes[i] instanceof ClassScope) {
							SourceTypeBinding localType =
								((ClassScope) blockScope.subscopes[i]).referenceContext.binding;

							if (!localType.isAnonymousType()) {
								if (this.isForbidden(localType))
									continue next;
								
								if (typeLength > localType.sourceName.length)
									continue next;
								if (!CharOperation.prefixEquals(typeName, localType.sourceName, false/* ignore case */)
										&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(typeName, localType.sourceName)))
									continue next;
								
								if(PROPOSE_MEMBER_TYPES) {
									if(this.assistNodeIsClass) {
										if(!localType.isClass()) continue next;
									} else if(this.assistNodeIsInterface) {
										if(!localType.isInterface() && !localType.isAnnotationType()) continue next;
									} else if (this.assistNodeIsAnnotation) {
										if(!localType.isAnnotationType()) continue next;
									}
								}

								int relevance = computeBaseRelevance();
								relevance += computeRelevanceForInterestingProposal();
								relevance += computeRelevanceForCaseMatching(typeName, localType.sourceName);
								relevance += computeRelevanceForExpectingType(localType);
								relevance += computeRelevanceForException(localType.sourceName);
								relevance += computeRelevanceForClass();
								relevance += computeRelevanceForQualification(false);
								relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for nested type
								
								this.noProposal = false;
								if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
									createTypeProposal(localType, localType.sourceName, IAccessRule.K_ACCESSIBLE, localType.sourceName, relevance);
								}
							}
						}
					}
					break;

				case Scope.CLASS_SCOPE :
					SourceTypeBinding enclosingSourceType = scope.enclosingSourceType();
					findMemberTypes(typeName, enclosingSourceType, scope, currentType, false, false, false, proposeAllMemberTypes, nextTypeToIgnore, typesFound);
					nextTypeToIgnore = enclosingSourceType;
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

		this.completionToken = CharOperation.concatWith(packageStatement.tokens, '.');
		if (this.completionToken.length == 0)
			return;

		setSourceRange(packageStatement.sourceStart, packageStatement.sourceEnd);
		this.nameEnvironment.findPackages(CharOperation.toLowerCase(this.completionToken), this);
	}

	private void findParameterizedType(TypeReference ref, Scope scope) {
		ReferenceBinding refBinding = (ReferenceBinding) ref.resolvedType;
		if(refBinding != null) {
			if (this.options.checkDeprecation &&
					refBinding.isViewedAsDeprecated() &&
					!scope.isDefinedInSameUnit(refBinding))
				return;
			
			int accessibility = IAccessRule.K_ACCESSIBLE;
			if(refBinding.hasRestrictedAccess()) {
				AccessRestriction accessRestriction = lookupEnvironment.getAccessRestriction(refBinding);
				if(accessRestriction != null) {
					switch (accessRestriction.getProblemId()) {
						case IProblem.ForbiddenReference:
							if (this.options.checkForbiddenReference) {
								return;
							}
							accessibility = IAccessRule.K_NON_ACCESSIBLE;
							break;
						case IProblem.DiscouragedReference:
							if (this.options.checkDiscouragedReference) {
								return;
							}
							accessibility = IAccessRule.K_DISCOURAGED;
							break;
					}
				}
			}

			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForCaseMatching(refBinding.sourceName, refBinding.sourceName);
			relevance += computeRelevanceForExpectingType(refBinding);
			relevance += computeRelevanceForQualification(false);
			relevance += computeRelevanceForRestrictions(accessibility); // no access restriction for type in the current unit
			
			if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
				createTypeProposal(refBinding, refBinding.qualifiedSourceName(), IAccessRule.K_ACCESSIBLE, CharOperation.NO_CHAR, relevance);
			}
		}
	}
	private void findTypeParameters(char[] token, Scope scope) {
		if (this.compilerOptions.sourceLevel < ClassFileConstants.JDK1_5) return;
		
		TypeParameter[] typeParameters = null;
		while (scope != null) { // done when a COMPILATION_UNIT_SCOPE is found
			typeParameters = null;
			switch (scope.kind) {
				case Scope.METHOD_SCOPE :
					MethodScope methodScope = (MethodScope) scope;
					if(methodScope.referenceContext instanceof MethodDeclaration) {
						MethodDeclaration methodDeclaration = (MethodDeclaration) methodScope.referenceContext;
						typeParameters = methodDeclaration.typeParameters;
					} else if(methodScope.referenceContext instanceof ConstructorDeclaration) {
						ConstructorDeclaration methodDeclaration = (ConstructorDeclaration) methodScope.referenceContext;
						typeParameters = methodDeclaration.typeParameters;
					}
					break;
				case Scope.CLASS_SCOPE :
					ClassScope classScope = (ClassScope) scope;
					typeParameters = classScope.referenceContext.typeParameters;
					break;
				case Scope.COMPILATION_UNIT_SCOPE :
					return;
			}
			if(typeParameters != null) {
				for (int i = 0; i < typeParameters.length; i++) {
					int typeLength = token.length;
					TypeParameter typeParameter = typeParameters[i];
					
					if(typeParameter.binding == null) continue;
					
					if (typeLength > typeParameter.name.length) continue;
					
					if (!CharOperation.prefixEquals(token, typeParameter.name, false)
							&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(token, typeParameter.name))) continue;
	
					int relevance = computeBaseRelevance();
					relevance += computeRelevanceForInterestingProposal();
					relevance += computeRelevanceForCaseMatching(token, typeParameter.name);
					relevance += computeRelevanceForExpectingType(typeParameter.type == null ? null :typeParameter.type.resolvedType);
					relevance += computeRelevanceForQualification(false);
					relevance += computeRelevanceForException(typeParameter.name);
					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction fot type parameter
					
					this.noProposal = false;
					if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
						createTypeParameterProposal(typeParameter, relevance);
					}
				}
			}
			scope = scope.parent;
		}
	}
	
	private void findTypesAndPackages(char[] token, Scope scope) {

		if (token == null)
			return;
		
		// do not propose type if completion token is empty
		boolean skip = false;
		if (token.length == 0 && NO_TYPE_COMPLETION_ON_EMPTY_TOKEN) {
			if(!assistNodeIsConstructor && (this.assistNodeInJavadoc & CompletionOnJavadoc.EXCEPTION) == 0) {
				return;
			}
			skip = true;
		}
		
		boolean proposeType = !this.requestor.isIgnored(CompletionProposal.TYPE_REF);
		
		boolean proposeAllMemberTypes = !this.assistNodeIsConstructor && PROPOSE_MEMBER_TYPES;
		
		ObjectVector typesFound = new ObjectVector();
		
		if (!skip && proposeType && scope.enclosingSourceType() != null) {
			findNestedTypes(token, scope.enclosingSourceType(), scope, proposeAllMemberTypes, typesFound);
			if(!assistNodeIsConstructor) {
				// don't propose type parameters if the completion is a constructor ('new |')
				findTypeParameters(token, scope);
			}
		}

		if (!skip && proposeType && this.unitScope != null) {
			ReferenceBinding outerInvocationType = scope.enclosingSourceType();
			if(outerInvocationType != null) {
				ReferenceBinding temp = outerInvocationType.enclosingType();
				while(temp != null) {
					outerInvocationType = temp;
					temp = temp.enclosingType();
				}
			}
			
			int typeLength = token.length;
			SourceTypeBinding[] types = this.unitScope.topLevelTypes;

			for (int i = 0, length = types.length; i < length; i++) {
				SourceTypeBinding sourceType = types[i]; 
				
				if(isForbidden(sourceType)) continue;
				
				if(proposeAllMemberTypes &&
					sourceType != outerInvocationType &&
					PROPOSE_MEMBER_TYPES) {
					findSubMemberTypes(
							token,
							sourceType,
							scope,
							scope.enclosingSourceType(),
							false,
							false,
							typesFound);
				}
				
				if (sourceType.sourceName == CompletionParser.FAKE_TYPE_NAME) continue;
				if (sourceType.sourceName == TypeConstants.PACKAGE_INFO_NAME) continue;

				if (typeLength > sourceType.sourceName.length) continue;
				
				if (!CharOperation.prefixEquals(token, sourceType.sourceName, false)
						&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(token, sourceType.sourceName))) continue;
	
				this.knownTypes.put(CharOperation.concat(sourceType.qualifiedPackageName(), sourceType.sourceName(), '.'), this);
				
				if(PROPOSE_MEMBER_TYPES) {
					if(this.assistNodeIsClass) {
						if(!sourceType.isClass()) continue;
					} else if(this.assistNodeIsInterface) {
						if(!sourceType.isInterface() && !sourceType.isAnnotationType()) continue;
					} else if (this.assistNodeIsAnnotation) {
						if(!sourceType.isAnnotationType()) continue;
					}
				}
				
				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(token, sourceType.sourceName);
				relevance += computeRelevanceForExpectingType(sourceType);
				relevance += computeRelevanceForQualification(false);
				relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for type in the current unit

				if (sourceType.isAnnotationType()) {
					relevance += computeRelevanceForAnnotation();
				} else if (sourceType.isInterface()) {
					relevance += computeRelevanceForInterface();
				} else if(sourceType.isClass()){
					relevance += computeRelevanceForClass();
					relevance += computeRelevanceForException(sourceType.sourceName);
				}
				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
					char[] typeName = sourceType.sourceName();
					createTypeProposal(sourceType, typeName, IAccessRule.K_ACCESSIBLE, typeName, relevance);
				}
			}
		}
		
		if(!skip && proposeType) {
			this.findTypesFromStaticImports(token, scope, proposeAllMemberTypes, typesFound);
		}
		
		if (token.length == 0) {
			if(proposeType && this.expectedTypesPtr > -1) {
				next : for (int i = 0; i <= this.expectedTypesPtr; i++) {
					if(this.expectedTypes[i] instanceof ReferenceBinding) {
						ReferenceBinding refBinding = (ReferenceBinding)this.expectedTypes[i];
						
						if(refBinding.isTypeVariable() && assistNodeIsConstructor) {
							// don't propose type variable if the completion is a constructor ('new |')
							continue next;
						}
						if (this.options.checkDeprecation &&
								refBinding.isViewedAsDeprecated() &&
								!scope.isDefinedInSameUnit(refBinding))
							continue next;
						
						int accessibility = IAccessRule.K_ACCESSIBLE;
						if(refBinding.hasRestrictedAccess()) {
							AccessRestriction accessRestriction = lookupEnvironment.getAccessRestriction(refBinding);
							if(accessRestriction != null) {
								switch (accessRestriction.getProblemId()) {
									case IProblem.ForbiddenReference:
										if (this.options.checkForbiddenReference) {
											continue next;
										}
										accessibility = IAccessRule.K_NON_ACCESSIBLE;
										break;
									case IProblem.DiscouragedReference:
										if (this.options.checkDiscouragedReference) {
											continue next;
										}
										accessibility = IAccessRule.K_DISCOURAGED;
										break;
								}
							}
						}
						
						boolean inSameUnit = this.unitScope.isDefinedInSameUnit(refBinding);
						
						// top level types of the current unit are already proposed.
						if(skip || !inSameUnit || (inSameUnit && refBinding.isMemberType())) {
							char[] packageName = refBinding.qualifiedPackageName();
							char[] typeName = refBinding.sourceName();
							char[] completionName = typeName;
							
							boolean isQualified = false;
							if (!this.insideQualifiedReference && !refBinding.isMemberType()) {
								if (mustQualifyType(packageName, typeName, null, refBinding.modifiers)) {
									if (packageName == null || packageName.length == 0)
										if (this.unitScope != null && this.unitScope.fPackage.compoundName != CharOperation.NO_CHAR_CHAR)
											continue next; // ignore types from the default package from outside it
									completionName = CharOperation.concat(packageName, typeName, '.');
									isQualified = true;
								}
							}
							
							if(PROPOSE_MEMBER_TYPES) {
								if(this.assistNodeIsClass) {
									if(!refBinding.isClass()) continue next;
								} else if(this.assistNodeIsInterface) {
									if(!refBinding.isInterface() && !refBinding.isAnnotationType()) continue next;
								} else if (this.assistNodeIsAnnotation) {
									if(!refBinding.isAnnotationType()) continue next;
								}
							}
							
							int relevance = computeBaseRelevance();
							relevance += computeRelevanceForInterestingProposal();
							relevance += computeRelevanceForCaseMatching(token, typeName);
							relevance += computeRelevanceForExpectingType(refBinding);
							relevance += computeRelevanceForQualification(isQualified);
							relevance += computeRelevanceForRestrictions(accessibility);
							
							if(refBinding.isClass()) {
								relevance += computeRelevanceForClass();
							} else if(refBinding.isEnum()) {
								relevance += computeRelevanceForEnum();
							} else if(refBinding.isInterface()) {
								relevance += computeRelevanceForInterface();
							}
								
							this.noProposal = false;
							if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
								CompletionProposal proposal = this.createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition);
								proposal.setDeclarationSignature(packageName);
								proposal.setSignature(getSignature(refBinding));
								proposal.setPackageName(packageName);
								proposal.setTypeName(typeName);
								proposal.setCompletion(completionName);
								proposal.setFlags(refBinding.modifiers);
								proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
								proposal.setRelevance(relevance);
								proposal.setAccessibility(accessibility);
								this.requestor.accept(proposal);
								if(DEBUG) {
									this.printDebug(proposal);
								}
							}
						}
					}
				}
			} 
		} else {
			if(!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
				if (this.assistNodeInJavadoc == 0 || (this.assistNodeInJavadoc & CompletionOnJavadoc.BASE_TYPES) != 0) {
					findKeywords(token, BASE_TYPE_NAMES, false);
				}
			}
			if(proposeType) {
				int l = typesFound.size();
				for (int i = 0; i < l; i++) {
					ReferenceBinding typeFound = (ReferenceBinding) typesFound.elementAt(i);
					char[] fullyQualifiedTypeName =
						CharOperation.concat(
								typeFound.qualifiedPackageName(),
								typeFound.qualifiedSourceName(), 
								'.');
					this.knownTypes.put(fullyQualifiedTypeName, this);
				}
				this.nameEnvironment.findTypes(
						token,
						proposeAllMemberTypes,
						this.options.camelCaseMatch,
						this);
				acceptTypes();
			}
			if(!this.requestor.isIgnored(CompletionProposal.PACKAGE_REF)) {
				this.nameEnvironment.findPackages(token, this);
			}
		}
	}

	private void findTypesAndSubpackages(
		char[] token,
		PackageBinding packageBinding,
		Scope scope) {

		boolean proposeType = !this.requestor.isIgnored(CompletionProposal.TYPE_REF);
		
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
		
		this.qualifiedCompletionToken = qualifiedName;
		
		if (proposeType && this.unitScope != null) {
			int typeLength = qualifiedName.length;
			SourceTypeBinding[] types = this.unitScope.topLevelTypes;

			for (int i = 0, length = types.length; i < length; i++) {
				SourceTypeBinding sourceType = types[i]; 
	
				char[] qualifiedSourceTypeName = CharOperation.concatWith(sourceType.compoundName, '.');
				
				if (sourceType.sourceName == CompletionParser.FAKE_TYPE_NAME) continue;
				if (sourceType.sourceName == TypeConstants.PACKAGE_INFO_NAME) continue;
				if (typeLength > qualifiedSourceTypeName.length) continue;
				if (!(packageBinding == sourceType.getPackage())) continue;

				if (!CharOperation.prefixEquals(qualifiedName, qualifiedSourceTypeName, false)
						&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(token, sourceType.sourceName)))	continue;
				
				if (this.options.checkDeprecation &&
						sourceType.isViewedAsDeprecated() &&
						!scope.isDefinedInSameUnit(sourceType))
					continue;
				
				int accessibility = IAccessRule.K_ACCESSIBLE;
				if(sourceType.hasRestrictedAccess()) {
					AccessRestriction accessRestriction = lookupEnvironment.getAccessRestriction(sourceType);
					if(accessRestriction != null) {
						switch (accessRestriction.getProblemId()) {
							case IProblem.ForbiddenReference:
								if (this.options.checkForbiddenReference) {
									continue;
								}
								accessibility = IAccessRule.K_NON_ACCESSIBLE;
								break;
							case IProblem.DiscouragedReference:
								if (this.options.checkDiscouragedReference) {
									continue;
								}
								accessibility = IAccessRule.K_DISCOURAGED;
								break;
						}
					}
				}
				
				this.knownTypes.put(CharOperation.concat(sourceType.qualifiedPackageName(), sourceType.sourceName(), '.'), this);

				int relevance = computeBaseRelevance();
				relevance += computeRelevanceForInterestingProposal();
				relevance += computeRelevanceForCaseMatching(qualifiedName, qualifiedSourceTypeName);
				relevance += computeRelevanceForExpectingType(sourceType);
				relevance += computeRelevanceForQualification(false);
				relevance += computeRelevanceForRestrictions(accessibility);
				
				if (sourceType.isAnnotationType()) {
					relevance += computeRelevanceForAnnotation();
				} else if (sourceType.isInterface()) {
					relevance += computeRelevanceForInterface();
				} else if (sourceType.isClass()) {
					relevance += computeRelevanceForClass();
					relevance += computeRelevanceForException(sourceType.sourceName);
				}
				this.noProposal = false;
				if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
					char[] typeName = sourceType.sourceName();
					createTypeProposal(sourceType, typeName, IAccessRule.K_ACCESSIBLE, typeName, relevance);
				}
			}
		}
		
		if(proposeType) {
			this.nameEnvironment.findTypes(
					qualifiedName,
					false,
					this.options.camelCaseMatch,
					this);
			acceptTypes();
		}
		if(!this.requestor.isIgnored(CompletionProposal.PACKAGE_REF)) {
			this.nameEnvironment.findPackages(qualifiedName, this);
		}
	}

	private void findTypesFromStaticImports(char[] token, Scope scope, boolean proposeAllMemberTypes, ObjectVector typesFound) {
		ImportBinding[] importBindings = scope.compilationUnitScope().imports;
		for (int i = 0; i < importBindings.length; i++) {
			ImportBinding importBinding = importBindings[i];
			if(importBinding.isValidBinding() && importBinding.isStatic()) {
				Binding binding = importBinding.resolvedImport;
				if(binding != null && binding.isValidBinding()) {
					if(importBinding.onDemand) {
						if((binding.kind() & Binding.TYPE) != 0) {
							this.findMemberTypes(
									token,
									(ReferenceBinding) binding,
									scope,
									scope.enclosingSourceType(),
									true,
									true,
									true,
									proposeAllMemberTypes,
									null,
									typesFound);
						}
					} else {
						if ((binding.kind() & Binding.TYPE) != 0) {
							ReferenceBinding typeBinding = (ReferenceBinding) binding;
							int typeLength = token.length;
							
							if (!typeBinding.isStatic()) continue;
							
							if (typeLength > typeBinding.sourceName.length)	continue;
							
							if (!CharOperation.prefixEquals(token, typeBinding.sourceName, false)
									&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(token, typeBinding.sourceName)))	continue;
							
							if (typesFound.contains(typeBinding))  continue;
							
							typesFound.add(typeBinding);
							
							if(PROPOSE_MEMBER_TYPES) {
								if(this.assistNodeIsClass) {
									if(!typeBinding.isClass()) continue;
								} else if(this.assistNodeIsInterface) {
									if(!typeBinding.isInterface() && !typeBinding.isAnnotationType()) continue;
								} else if (this.assistNodeIsAnnotation) {
									if(!typeBinding.isAnnotationType()) continue;
								}
							}
							
							int relevance = computeBaseRelevance();
							relevance += computeRelevanceForInterestingProposal();
							relevance += computeRelevanceForCaseMatching(token, typeBinding.sourceName);
							relevance += computeRelevanceForExpectingType(typeBinding);
							relevance += computeRelevanceForQualification(false);
							relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
							
							if (typeBinding.isClass()) {
								relevance += computeRelevanceForClass();
								relevance += computeRelevanceForException(typeBinding.sourceName);
							} else if(typeBinding.isEnum()) {
								relevance += computeRelevanceForEnum();
							} else if(typeBinding.isInterface()) {
								relevance += computeRelevanceForInterface();
							}
								
							this.noProposal = false;
							if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
								CompletionProposal proposal = this.createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition);
								proposal.setDeclarationSignature(typeBinding.qualifiedPackageName());
								proposal.setSignature(getSignature(typeBinding));
								proposal.setPackageName(typeBinding.qualifiedPackageName());
								proposal.setTypeName(typeBinding.qualifiedSourceName());
								proposal.setCompletion(typeBinding.sourceName());
								proposal.setFlags(typeBinding.modifiers);
								proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
								proposal.setRelevance(relevance);
								this.requestor.accept(proposal);
								if(DEBUG) {
									this.printDebug(proposal);
								}
							}
						}
					}
				}
			}
		}
	}
	private void findVariablesAndMethods(
		char[] token,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean insideTypeAnnotation,
		boolean insideAnnotationAttribute) {

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

		if (!this.requestor.isIgnored(CompletionProposal.LOCAL_VARIABLE_REF)) {
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
	
							if (!CharOperation.prefixEquals(token, local.name, false /* ignore case */)
									&& !(this.options.camelCaseMatch && CharOperation.camelCaseMatch(token, local.name)))
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
							relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for local variable
							this.noProposal = false;
							if(!this.requestor.isIgnored(CompletionProposal.LOCAL_VARIABLE_REF)) {
								CompletionProposal proposal = this.createProposal(CompletionProposal.LOCAL_VARIABLE_REF, this.actualCompletionPosition);
								proposal.setSignature(
									local.type == null
									? createTypeSignature(
											CharOperation.NO_CHAR,
											local.declaration.type.toString().toCharArray())
									: getSignature(local.type));
								if(local.type == null) {
									//proposal.setPackageName(null);
									proposal.setTypeName(local.declaration.type.toString().toCharArray());
								} else {
									proposal.setPackageName(local.type.qualifiedPackageName());
									proposal.setTypeName(local.type.qualifiedSourceName());
								}
								proposal.setName(local.name);
								proposal.setCompletion(local.name);
								proposal.setFlags(local.modifiers);
								proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
								proposal.setRelevance(relevance);
								this.requestor.accept(proposal);
								if(DEBUG) {
									this.printDebug(proposal);
								}
							}
						}
						break;
	
					case Scope.COMPILATION_UNIT_SCOPE :
						break done1;
				}
				currentScope = currentScope.parent;
			}
		}
		
		boolean proposeField = !this.requestor.isIgnored(CompletionProposal.FIELD_REF);
		boolean proposeMethod = !this.requestor.isIgnored(CompletionProposal.METHOD_REF);
		
		staticsOnly = false;
		currentScope = scope;

		if(proposeField || proposeMethod) {
			done2 : while (true) { // done when a COMPILATION_UNIT_SCOPE is found
	
				switch (currentScope.kind) {
					case Scope.METHOD_SCOPE :
						// handle the error case inside an explicit constructor call (see MethodScope>>findField)
						MethodScope methodScope = (MethodScope) currentScope;
						staticsOnly |= methodScope.isStatic | methodScope.isConstructorCall;
						break;
					case Scope.CLASS_SCOPE :
						ClassScope classScope = (ClassScope) currentScope;
						SourceTypeBinding enclosingType = classScope.referenceContext.binding;
						/*				if (tokenLength == 0) { // only search inside the type itself if no prefix was provided
											findFields(token, enclosingType.fields(), classScope, fieldsFound, staticsOnly);
											findMethods(token, enclosingType.methods(), classScope, methodsFound, staticsOnly, false);
											break done;
										} else { */
						if(!insideTypeAnnotation) {
							if(proposeField) {
								findFields(
									token,
									enclosingType,
									classScope,
									fieldsFound,
									localsFound,
									staticsOnly,
									invocationSite,
									invocationScope,
									true,
									true);
							}
							if(proposeMethod && !insideAnnotationAttribute) {
								findMethods(
									token,
									null,
									null,
									enclosingType,
									classScope,
									methodsFound,
									staticsOnly,
									false,
									false,
									invocationSite,
									invocationScope,
									true,
									false,
									true);
							}
						}
						staticsOnly |= enclosingType.isStatic();
						insideTypeAnnotation = false;
						//				}
						break;
	
					case Scope.COMPILATION_UNIT_SCOPE :
						break done2;
				}
				currentScope = currentScope.parent;
			}
			
			ImportBinding[] importBindings = scope.compilationUnitScope().imports;
			for (int i = 0; i < importBindings.length; i++) {
				ImportBinding importBinding = importBindings[i];
				if(importBinding.isValidBinding() && importBinding.isStatic()) {
					Binding binding = importBinding.resolvedImport;
					if(binding != null && binding.isValidBinding()) {
						if(importBinding.onDemand) {
							if((binding.kind() & Binding.TYPE) != 0) {
								if(proposeField) {
									findFields(
										token,
										(ReferenceBinding)binding,
										scope,
										fieldsFound,
										localsFound,
										true,
										invocationSite,
										invocationScope,
										true,
										false);
								}
								if(proposeMethod && !insideAnnotationAttribute) {
									findMethods(
										token,
										null,
										null,
										(ReferenceBinding)binding,
										scope,
										methodsFound,
										true,
										false,
										false,
										invocationSite,
										invocationScope,
										true,
										false,
										false);
								}
							}
						} else {
							if ((binding.kind() & Binding.FIELD) != 0) {
								if(proposeField) {
										findFields(
												token,
												new FieldBinding[]{(FieldBinding)binding},
												scope,
												fieldsFound,
												localsFound,
												true,
												((FieldBinding)binding).declaringClass,
												invocationSite,
												invocationScope,
												true,
												false);
								}
							} else if ((binding.kind() & Binding.METHOD) != 0) {
								if(proposeMethod && !insideAnnotationAttribute) {
									MethodBinding methodBinding = (MethodBinding)binding;
									if(CharOperation.prefixEquals(token, methodBinding.selector))
										
									findLocalMethodsOfStaticImports(
											methodBinding.selector,
											methodBinding.declaringClass.methods(),
											scope,
											methodBinding.declaringClass,
											invocationSite);
								}
							}
						}
					}
				}
			}
		}
	}
		// Helper method for private void findVariableNames(char[] name, TypeReference type )
	private void findVariableName(
		char[] token,
		char[] qualifiedPackageName,
		char[] qualifiedSourceName,
		char[] sourceName,
		final TypeBinding typeBinding,
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
			public void acceptNameWithPrefixAndSuffix(char[] name, boolean isFirstPrefix, boolean isFirstSuffix, int reusedCharacters) {
				accept(
						name,
						(isFirstPrefix ? R_NAME_FIRST_PREFIX : R_NAME_PREFIX) + (isFirstSuffix ? R_NAME_FIRST_SUFFIX : R_NAME_SUFFIX),
						reusedCharacters);
			}

			public void acceptNameWithPrefix(char[] name, boolean isFirstPrefix, int reusedCharacters) {
				accept(name, isFirstPrefix ? R_NAME_FIRST_PREFIX :  R_NAME_PREFIX, reusedCharacters);
			}

			public void acceptNameWithSuffix(char[] name, boolean isFirstSuffix, int reusedCharacters) {
				accept(name, isFirstSuffix ? R_NAME_FIRST_SUFFIX : R_NAME_SUFFIX, reusedCharacters);
			}

			public void acceptNameWithoutPrefixAndSuffix(char[] name,int reusedCharacters) {
				accept(name, 0, reusedCharacters);
			}
			void accept(char[] name, int prefixAndSuffixRelevance, int reusedCharacters){
				if (CharOperation.prefixEquals(t, name, false)) {
					int relevance = computeBaseRelevance();
					relevance += computeRelevanceForInterestingProposal();
					relevance += computeRelevanceForCaseMatching(t, name);
					relevance += prefixAndSuffixRelevance;
					if(reusedCharacters > 0) relevance += R_NAME_LESS_NEW_CHARACTERS;
					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for variable name
					
					// accept result
					CompletionEngine.this.noProposal = false;
					if(!CompletionEngine.this.requestor.isIgnored(CompletionProposal.VARIABLE_DECLARATION)) {
						CompletionProposal proposal = CompletionEngine.this.createProposal(CompletionProposal.VARIABLE_DECLARATION, CompletionEngine.this.actualCompletionPosition);
						proposal.setSignature(getSignature(typeBinding));
						proposal.setPackageName(q);
						proposal.setTypeName(displayName);
						proposal.setName(name);
						proposal.setCompletion(name);
						//proposal.setFlags(Flags.AccDefault);
						proposal.setReplaceRange(CompletionEngine.this.startPosition - CompletionEngine.this.offset, CompletionEngine.this.endPosition - CompletionEngine.this.offset);
						proposal.setRelevance(relevance);
						CompletionEngine.this.requestor.accept(proposal);
						if(DEBUG) {
							CompletionEngine.this.printDebug(proposal);
						}
					}
				}
			}
		};
		
		switch (kind) {
			case FIELD :
				InternalNamingConventions.suggestFieldNames(
					this.javaProject,
					qualifiedPackageName,
					qualifiedSourceName,
					dim,
					modifiers,
					token,
					excludeNames,
					namingRequestor);
				break;
			case LOCAL :
				InternalNamingConventions.suggestLocalVariableNames(
					this.javaProject,
					qualifiedPackageName,
					qualifiedSourceName,
					dim,
					token,
					excludeNames,
					namingRequestor);
				break;
			case ARGUMENT :
				InternalNamingConventions.suggestArgumentNames(
					this.javaProject,
					qualifiedPackageName,
					qualifiedSourceName,
					dim,
					token,
					excludeNames,
					namingRequestor);
				break;
		}
	}

	private void findVariableNames(char[] name, TypeReference type , char[][] excludeNames, int kind, int modifiers){

		if(type != null &&
			type.resolvedType != null &&
			type.resolvedType.problemId() == ProblemReasons.NoError){
			TypeBinding tb = type.resolvedType;
			findVariableName(
				name,
				tb.leafComponentType().qualifiedPackageName(),
				tb.leafComponentType().qualifiedSourceName(),
				tb.leafComponentType().sourceName(),
				tb,
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

		return this.parser;
	}

	protected void reset() {

		super.reset();
		this.knownPkgs = new HashtableOfObject(10);
		this.knownTypes = new HashtableOfObject(10);
	}
	
	private void setSourceRange(int start, int end) {
		this.setSourceRange(start, end, true);
	}

	private void setSourceRange(int start, int end, boolean emptyTokenAdjstment) {		
		this.startPosition = start;
		if(emptyTokenAdjstment) {
			int endOfEmptyToken = ((CompletionScanner)this.parser.scanner).endOfEmptyToken;
			this.endPosition = endOfEmptyToken > end ? endOfEmptyToken + 1 : end + 1;
		} else {
			this.endPosition = end + 1;
		}
	}
	int computeBaseRelevance(){
		return R_DEFAULT;
	}
	private void computeExpectedTypes(ASTNode parent, ASTNode node, Scope scope){
		
		// default filter
		this.expectedTypesFilter = SUBTYPE;
		this.hasJavaLangObjectAsExpectedType = false;
		
		// find types from parent
		if(parent instanceof AbstractVariableDeclaration) {
			AbstractVariableDeclaration variable = (AbstractVariableDeclaration)parent;
			TypeBinding binding = variable.type.resolvedType;
			if(binding != null) {
				if(!(variable.initialization instanceof ArrayInitializer)) {
					addExpectedType(binding, scope);
				}
			}
		} else if(parent instanceof Assignment) {
			TypeBinding binding = ((Assignment)parent).lhs.resolvedType;
			if(binding != null) {
				addExpectedType(binding, scope);
			}
		} else if(parent instanceof ReturnStatement) {
			if(scope.methodScope().referenceContext instanceof AbstractMethodDeclaration) {
				MethodBinding methodBinding = ((AbstractMethodDeclaration) scope.methodScope().referenceContext).binding;
				TypeBinding binding = methodBinding  == null ? null : methodBinding.returnType;
				if(binding != null) {
					addExpectedType(binding, scope);
				}
			}
		} else if(parent instanceof CastExpression) {
			Expression e = ((CastExpression)parent).type;
			TypeBinding binding = e.resolvedType;
			if(binding != null){
				addExpectedType(binding, scope);
				this.expectedTypesFilter = SUBTYPE | SUPERTYPE;
			}
		} else if(parent instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) parent;

			if(messageSend.actualReceiverType instanceof ReferenceBinding) {
				ReferenceBinding binding = (ReferenceBinding)messageSend.actualReceiverType;
				boolean isStatic = messageSend.receiver.isTypeReference();
				
				while(binding != null) {	
					computeExpectedTypesForMessageSend(
						binding,
						messageSend.selector,
						messageSend.arguments,
						(ReferenceBinding)messageSend.actualReceiverType,
						scope,
						messageSend,
						isStatic);
					computeExpectedTypesForMessageSendForInterface(
						binding,
						messageSend.selector,
						messageSend.arguments,
						(ReferenceBinding)messageSend.actualReceiverType,
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
			int operator = (parent.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
			if(parent instanceof ConditionalExpression) {
				// for future use
			} else if(parent instanceof InstanceOfExpression) {
				InstanceOfExpression e = (InstanceOfExpression) parent;
				TypeBinding binding = e.expression.resolvedType;
				if(binding != null){
					addExpectedType(binding, scope);
					this.expectedTypesFilter = SUBTYPE | SUPERTYPE;
				}
			} else if(parent instanceof BinaryExpression) {
				switch(operator) {
					case OperatorIds.PLUS :
						addExpectedType(TypeBinding.SHORT, scope);
						addExpectedType(TypeBinding.INT, scope);
						addExpectedType(TypeBinding.LONG, scope);
						addExpectedType(TypeBinding.FLOAT, scope);
						addExpectedType(TypeBinding.DOUBLE, scope);
						addExpectedType(TypeBinding.CHAR, scope);
						addExpectedType(TypeBinding.BYTE, scope);
						addExpectedType(scope.getJavaLangString(), scope);
						break;
					case OperatorIds.AND_AND :
					case OperatorIds.OR_OR :
					case OperatorIds.XOR :
						addExpectedType(TypeBinding.BOOLEAN, scope);
						break;
					default :
						addExpectedType(TypeBinding.SHORT, scope);
						addExpectedType(TypeBinding.INT, scope);
						addExpectedType(TypeBinding.LONG, scope);
						addExpectedType(TypeBinding.FLOAT, scope);
						addExpectedType(TypeBinding.DOUBLE, scope);
						addExpectedType(TypeBinding.CHAR, scope);
						addExpectedType(TypeBinding.BYTE, scope);
						break;
				}
				BinaryExpression binaryExpression = (BinaryExpression) parent;
				if(operator == OperatorIds.LESS) {
					if(binaryExpression.left instanceof SingleNameReference){
						SingleNameReference name = (SingleNameReference) binaryExpression.left;
						Binding b = scope.getBinding(name.token, Binding.VARIABLE | Binding.TYPE, name, false);
						if(b instanceof ReferenceBinding) {
							TypeVariableBinding[] typeVariableBindings =((ReferenceBinding)b).typeVariables();
							if(typeVariableBindings != null && typeVariableBindings.length > 0) {
								addExpectedType(typeVariableBindings[0].firstBound, scope);
							}
							
						}
					}
				}
			} else if(parent instanceof UnaryExpression) {
				switch(operator) {
					case OperatorIds.NOT :
						addExpectedType(TypeBinding.BOOLEAN, scope);
						break;
					case OperatorIds.TWIDDLE :
						addExpectedType(TypeBinding.SHORT, scope);
						addExpectedType(TypeBinding.INT, scope);
						addExpectedType(TypeBinding.LONG, scope);
						addExpectedType(TypeBinding.CHAR, scope);
						addExpectedType(TypeBinding.BYTE, scope);
						break;
					case OperatorIds.PLUS :
					case OperatorIds.MINUS :
					case OperatorIds.PLUS_PLUS :
					case OperatorIds.MINUS_MINUS :
						addExpectedType(TypeBinding.SHORT, scope);
						addExpectedType(TypeBinding.INT, scope);
						addExpectedType(TypeBinding.LONG, scope);
						addExpectedType(TypeBinding.FLOAT, scope);
						addExpectedType(TypeBinding.DOUBLE, scope);
						addExpectedType(TypeBinding.CHAR, scope);
						addExpectedType(TypeBinding.BYTE, scope);
						break;
				}
			}
		} else if(parent instanceof ArrayReference) {
			addExpectedType(TypeBinding.SHORT, scope);
			addExpectedType(TypeBinding.INT, scope);
			addExpectedType(TypeBinding.LONG, scope);
		} else if(parent instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference ref = (ParameterizedSingleTypeReference) parent;
			TypeVariableBinding[] typeVariables = ((ReferenceBinding)ref.resolvedType).typeVariables();
			int length = ref.typeArguments == null ? 0 : ref.typeArguments.length;
			if(typeVariables != null && typeVariables.length >= length) {
				int index = length - 1;
				while(index > -1 && ref.typeArguments[index] != node) index--;
				
				TypeBinding bound = typeVariables[index].firstBound;
				addExpectedType(bound == null ? scope.getJavaLangObject() : bound, scope);
			}
		} else if(parent instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference ref = (ParameterizedQualifiedTypeReference) parent;
			TypeVariableBinding[] typeVariables = ((ReferenceBinding)ref.resolvedType).typeVariables();
			TypeReference[][] arguments = ref.typeArguments;
			if(typeVariables != null) {
				int iLength = arguments == null ? 0 : arguments.length;
				done: for (int i = 0; i < iLength; i++) {
					int jLength = arguments[i] == null ? 0 : arguments[i].length;
					for (int j = 0; j < jLength; j++) {
						if(arguments[i][j] == node && typeVariables.length > j) {
							TypeBinding bound = typeVariables[j].firstBound;
							addExpectedType(bound == null ? scope.getJavaLangObject() : bound, scope);
							break done;
						}
					}
				}
			}
		} else if(parent instanceof MemberValuePair) {
			MemberValuePair memberValuePair = (MemberValuePair) parent;
			if(memberValuePair.binding != null) {
				addExpectedType(memberValuePair.binding.returnType, scope);
			}
		} else if (parent instanceof NormalAnnotation) {
			NormalAnnotation annotation = (NormalAnnotation) parent;
			MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
			if(memberValuePairs == null || memberValuePairs.length == 0) {
				if(annotation.resolvedType instanceof ReferenceBinding) {
					MethodBinding[] methodBindings =
						((ReferenceBinding)annotation.resolvedType).availableMethods();
					if (methodBindings != null &&
							methodBindings.length > 0 &&
							CharOperation.equals(methodBindings[0].selector, VALUE)) {
						boolean canBeSingleMemberAnnotation = true;
						done : for (int i = 1; i < methodBindings.length; i++) {
							if((methodBindings[i].modifiers & ClassFileConstants.AccAnnotationDefault) == 0) {
								canBeSingleMemberAnnotation = false;
								break done;
							}
						}
						if (canBeSingleMemberAnnotation) {
							this.assistNodeCanBeSingleMemberAnnotation = canBeSingleMemberAnnotation;
							addExpectedType(methodBindings[0].returnType, scope);
						}
					}
				}
			}
		// Expected types for javadoc
		} else if (parent instanceof Javadoc) {
			if (scope.kind == Scope.METHOD_SCOPE) {
				MethodScope methodScope = (MethodScope) scope;
				AbstractMethodDeclaration methodDecl = methodScope.referenceMethod();
				if (methodDecl != null && methodDecl.binding != null) {
					ReferenceBinding[] exceptions = methodDecl.binding.thrownExceptions;
					if (exceptions != null) {
						for (int i = 0; i < exceptions.length; i++) {
							addExpectedType(exceptions[i], scope);
						}
					}
				}
			}
		}
		
		if(this.expectedTypesPtr + 1 != this.expectedTypes.length) {
			System.arraycopy(this.expectedTypes, 0, this.expectedTypes = new TypeBinding[this.expectedTypesPtr + 1], 0, this.expectedTypesPtr + 1);
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
			
			if (this.options.checkVisibility && !method.canBeSeenBy(invocationSite, scope)) continue nextMethod;
			
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
				addExpectedType(expectedType, scope);
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
		if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
			ReferenceBinding[] interfacesToVisit = itsInterfaces;
			int nextPosition = interfacesToVisit.length;

			for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding currentType = interfacesToVisit[i];
				computeExpectedTypesForMessageSend(
					currentType,
					selector,
					arguments,
					receiverType,
					scope,
					invocationSite,
					isStatic);

				if ((itsInterfaces = currentType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
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
			
			if (this.options.checkVisibility && !method.canBeSeenBy(receiverType, invocationSite, scope)) continue nextMethod;
			
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
				addExpectedType(expectedType, scope);
			}
		}
	}
	private void addExpectedType(TypeBinding type, Scope scope){
		if (type == null || !type.isValidBinding()) return;

		int length = this.expectedTypes.length;
		if (++this.expectedTypesPtr >= length)
			System.arraycopy(this.expectedTypes, 0, this.expectedTypes = new TypeBinding[length * 2], 0, length);
		this.expectedTypes[this.expectedTypesPtr] = type;
		
		if(type == scope.getJavaLangObject()) {
			this.hasJavaLangObjectAsExpectedType = true;
		}
	}
	private void addForbiddenBindings(Binding binding){
		if (binding == null) return;

		int length = this.forbbidenBindings.length;
		if (++this.forbbidenBindingsPtr >= length)
			System.arraycopy(this.forbbidenBindings, 0, this.forbbidenBindings = new Binding[length * 2], 0, length);
		this.forbbidenBindings[this.forbbidenBindingsPtr] = binding;
	}
	private void addUninterestingBindings(Binding binding){
		if (binding == null) return;

		int length = this.uninterestingBindings.length;
		if (++this.uninterestingBindingsPtr >= length)
			System.arraycopy(this.uninterestingBindings, 0, this.uninterestingBindings = new Binding[length * 2], 0, length);
		this.uninterestingBindings[this.uninterestingBindingsPtr] = binding;
	}

	private Scope computeForbiddenBindings(ASTNode astNode, ASTNode astNodeParent, Scope scope) {
		if(scope instanceof ClassScope) {
			TypeDeclaration typeDeclaration = ((ClassScope)scope).referenceContext;
			if(typeDeclaration.superclass == astNode) {
				this.addForbiddenBindings(typeDeclaration.binding);
				return scope.parent;
			}
			TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
			int length = superInterfaces == null ? 0 : superInterfaces.length;
			for (int i = 0; i < length; i++) {
				if(superInterfaces[i] == astNode) {
					this.addForbiddenBindings(typeDeclaration.binding);
					return scope.parent;
				}
			}
		}
//		else if(scope instanceof MethodScope) {
//			MethodScope methodScope = (MethodScope) scope;
//			if(methodScope.insideTypeAnnotation) {
//				return methodScope.parent.parent;
//			}
//		}
		return scope;
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
	
	private void proposeNewMethod(char[] token, ReferenceBinding reference) {
		if(!this.requestor.isIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION)) {
			int relevance = computeBaseRelevance();
			relevance += computeRelevanceForInterestingProposal();
			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for new method
			
			CompletionProposal proposal = this.createProposal(CompletionProposal.POTENTIAL_METHOD_DECLARATION, this.actualCompletionPosition);
			proposal.setDeclarationSignature(getSignature(reference));
			proposal.setSignature(
					createMethodSignature(
							CharOperation.NO_CHAR_CHAR,
							CharOperation.NO_CHAR_CHAR,
							CharOperation.NO_CHAR,
							VOID));
			proposal.setDeclarationPackageName(reference.qualifiedPackageName());
			proposal.setDeclarationTypeName(reference.qualifiedSourceName());
			
			//proposal.setPackageName(null);
			proposal.setTypeName(VOID);
			proposal.setName(token);
			//proposal.setParameterPackageNames(null);
			//proposal.setParameterTypeNames(null);
			//proposal.setPackageName(null);
			proposal.setCompletion(token);
			proposal.setFlags(Flags.AccPublic);
			proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance);
			this.requestor.accept(proposal);
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
	}
	private boolean isForbidden(Binding binding) {
		for (int i = 0; i <= this.forbbidenBindingsPtr; i++) {
			if(this.forbbidenBindings[i] == binding) {
				return true;
			}
		}
		return false;
	}
	private boolean isValidParent(ASTNode parent, ASTNode node, Scope scope){
		
		if(parent instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference ref = (ParameterizedSingleTypeReference) parent;
			TypeVariableBinding[] typeVariables = ((ReferenceBinding)ref.resolvedType).typeVariables();
			int length = ref.typeArguments == null ? 0 : ref.typeArguments.length;
			int nodeIndex = -1;
			for(int i = length - 1 ; i > -1 ; i--) {
				if(node == ref.typeArguments[i]) {
					nodeIndex = i;
					break;
				}
			}
			if(nodeIndex > -1 && (typeVariables == null || typeVariables.length < nodeIndex + 1)) {
				TypeBinding[] typeBindings = new TypeBinding[nodeIndex + 1];
				for(int i = 0; i < nodeIndex; i++) {
					typeBindings[i] = ref.typeArguments[i].resolvedType;
				}
				typeBindings[nodeIndex] = scope.getJavaLangObject();
				if(typeVariables == null || typeVariables.length == 0) {
					scope.problemReporter().nonGenericTypeCannotBeParameterized(ref, ref.resolvedType, typeBindings);
				} else {
					scope.problemReporter().incorrectArityForParameterizedType(ref, ref.resolvedType, typeBindings);
				}
				return false;
			}
		} else if(parent instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference ref = (ParameterizedQualifiedTypeReference) parent;
			TypeVariableBinding[] typeVariables = ((ReferenceBinding)ref.resolvedType).typeVariables();
			TypeReference[][] arguments = ref.typeArguments;
			int iLength = arguments == null ? 0 : arguments.length;
			for (int i = 0; i < iLength; i++) {
				int jLength = arguments[i] == null ? 0 : arguments[i].length;
				for (int j = 0; j < jLength; j++) {
					if(arguments[i][j] == node && (typeVariables == null || typeVariables.length <= j)) {
						TypeBinding[] typeBindings = new TypeBinding[j + 1];
						for(int k = 0; k < j; k++) {
							typeBindings[k] = ref.typeArguments[i][k].resolvedType;
						}
						typeBindings[j] = scope.getJavaLangObject();
						if(typeVariables == null || typeVariables.length == 0) {
							scope.problemReporter().nonGenericTypeCannotBeParameterized(ref, ref.resolvedType, typeBindings);
						} else {
							scope.problemReporter().incorrectArityForParameterizedType(ref, ref.resolvedType, typeBindings);
						}
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static char[] createNonGenericTypeSignature(char[] qualifiedPackageName, char[] qualifiedTypeName) {
		return Signature.createCharArrayTypeSignature(
				CharOperation.concat(
						qualifiedPackageName,
						CharOperation.replaceOnCopy(qualifiedTypeName, '.', '$'), '.'), true);
	}
	public static char[] createTypeSignature(char[] qualifiedPackageName, char[] qualifiedTypeName) {
		char[] name = new char[qualifiedTypeName.length];
		System.arraycopy(qualifiedTypeName, 0, name, 0, qualifiedTypeName.length);
		
		int depth = 0;
		int length = name.length;
		for (int i = length -1; i >= 0; i--) {
			switch (name[i]) {
				case '.':
					if (depth == 0 && name[i - 1] != '>') {
						name[i] = '$';
					}
					break;
				case '<':
					depth--;
					break;
				case '>':
					depth++;
					break;
			}
		}
		return Signature.createCharArrayTypeSignature(
				CharOperation.concat(
						qualifiedPackageName,
						name, '.'), true);
	}
	
	public static char[] createMethodSignature(char[][] parameterPackageNames, char[][] parameterTypeNames, char[] returnPackagename, char[] returnTypeName) {
		char[][] parameterTypeSignature = new char[parameterTypeNames.length][];
		for (int i = 0; i < parameterTypeSignature.length; i++) {
			parameterTypeSignature[i] = 
				Signature.createCharArrayTypeSignature(
						CharOperation.concat(
								parameterPackageNames[i],
								CharOperation.replaceOnCopy(parameterTypeNames[i], '.', '$'), '.'), true);
		}
		
		char[] returnTypeSignature =
			returnTypeName == null || returnTypeName.length == 0
			? Signature.createCharArrayTypeSignature(VOID, true)
			: Signature.createCharArrayTypeSignature(
					CharOperation.concat(
							returnPackagename,
							CharOperation.replaceOnCopy(returnTypeName, '.', '$'), '.'), true);
			
		return Signature.createMethodSignature(
				parameterTypeSignature,
				returnTypeSignature);
	}
	
	protected CompletionProposal createProposal(int kind, int completionOffset) {
		CompletionProposal proposal = CompletionProposal.create(kind, completionOffset - this.offset);
		proposal.nameLookup = this.nameEnvironment.nameLookup;
		proposal.completionEngine = this;
		return proposal;
	}

	/*
	 * Create a completion proposal for a type.
	 */
	private void createTypeProposal(char[] packageName, char[] typeName, int modifiers, int accessibility, char[] completionName, int relevance) {

		// Create standard type proposal
		if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF) && (this.assistNodeInJavadoc & CompletionOnJavadoc.ONLY_INLINE_TAG) == 0) {
			CompletionProposal proposal = CompletionProposal.create(CompletionProposal.TYPE_REF, this.actualCompletionPosition - this.offset);
			proposal.nameLookup = this.nameEnvironment.nameLookup;
			proposal.completionEngine = this;
			proposal.setDeclarationSignature(packageName);
			proposal.setSignature(createNonGenericTypeSignature(packageName, typeName));
			proposal.setPackageName(packageName);
			proposal.setTypeName(typeName);
			proposal.setCompletion(completionName);
			proposal.setFlags(modifiers);
			proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance);
			proposal.setAccessibility(accessibility);
			this.requestor.accept(proposal);	
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
		
		// Create javadoc text proposal if necessary
		if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0 && !this.requestor.isIgnored(CompletionProposal.JAVADOC_TYPE_REF)) {
			char[] javadocCompletion= inlineTagCompletion(completionName, JavadocTagConstants.TAG_LINK);
			CompletionProposal proposal = CompletionProposal.create(CompletionProposal.JAVADOC_TYPE_REF, this.actualCompletionPosition - this.offset);
			proposal.nameLookup = this.nameEnvironment.nameLookup;
			proposal.completionEngine = this;
			proposal.setDeclarationSignature(packageName);
			proposal.setSignature(createNonGenericTypeSignature(packageName, typeName));
			proposal.setPackageName(packageName);
			proposal.setTypeName(typeName);
			proposal.setCompletion(javadocCompletion);
			proposal.setFlags(modifiers);
			int start = (this.assistNodeInJavadoc & CompletionOnJavadoc.REPLACE_TAG) != 0 ? this.javadocTagPosition : this.startPosition;
			proposal.setReplaceRange(start - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance+R_INLINE_TAG);
			proposal.setAccessibility(accessibility);
			this.requestor.accept(proposal);
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
	}

	/*
	 * Create a completion proposal for a member type.
	 */
	private void createTypeProposal(ReferenceBinding refBinding, char[] typeName, int accessibility, char[] completionName, int relevance) {

		// Create standard type proposal
		if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF) && (this.assistNodeInJavadoc & CompletionOnJavadoc.ONLY_INLINE_TAG) == 0) {
			CompletionProposal proposal = CompletionProposal.create(CompletionProposal.TYPE_REF, this.actualCompletionPosition - this.offset);
			proposal.nameLookup = this.nameEnvironment.nameLookup;
			proposal.completionEngine = this;
			proposal.setDeclarationSignature(refBinding.qualifiedPackageName());
			proposal.setSignature(getSignature(refBinding));
			proposal.setPackageName(refBinding.qualifiedPackageName());
			proposal.setTypeName(typeName);
			proposal.setCompletion(completionName);
			proposal.setFlags(refBinding.modifiers);
			proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance);
			this.requestor.accept(proposal);
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
		
		// Create javadoc text proposal if necessary
		if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0 && !this.requestor.isIgnored(CompletionProposal.JAVADOC_TYPE_REF)) {
			char[] javadocCompletion= inlineTagCompletion(completionName, JavadocTagConstants.TAG_LINK);
			CompletionProposal proposal = CompletionProposal.create(CompletionProposal.JAVADOC_TYPE_REF, this.actualCompletionPosition - this.offset);
			proposal.nameLookup = this.nameEnvironment.nameLookup;
			proposal.completionEngine = this;
			proposal.setDeclarationSignature(refBinding.qualifiedPackageName());
			proposal.setSignature(getSignature(refBinding));
			proposal.setPackageName(refBinding.qualifiedPackageName());
			proposal.setTypeName(typeName);
			proposal.setCompletion(javadocCompletion);
			proposal.setFlags(refBinding.modifiers);
			int start = (this.assistNodeInJavadoc & CompletionOnJavadoc.REPLACE_TAG) != 0 ? this.javadocTagPosition : this.startPosition;
			proposal.setReplaceRange(start - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance+R_INLINE_TAG);
			this.requestor.accept(proposal);
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
	}

	/*
	 * Create a completion proposal for a member type.
	 */
	private void createTypeParameterProposal(TypeParameter typeParameter, int relevance) {
		char[] completionName = typeParameter.name;

		// Create standard type proposal
		if(!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
			CompletionProposal proposal = CompletionProposal.create(CompletionProposal.TYPE_REF, this.actualCompletionPosition - this.offset);
			proposal.nameLookup = this.nameEnvironment.nameLookup;
			proposal.completionEngine = this;
			proposal.setSignature(getSignature(typeParameter.binding));
			proposal.setTypeName(completionName);
			proposal.setCompletion(completionName);
			proposal.setFlags(typeParameter.modifiers);
			proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance);
			this.requestor.accept(proposal);
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
		
		// Create javadoc text proposal if necessary
		if ((this.assistNodeInJavadoc & CompletionOnJavadoc.TEXT) != 0 && !this.requestor.isIgnored(CompletionProposal.JAVADOC_TYPE_REF)) {
			char[] javadocCompletion= inlineTagCompletion(completionName, JavadocTagConstants.TAG_LINK);
			CompletionProposal proposal = CompletionProposal.create(CompletionProposal.JAVADOC_TYPE_REF, this.actualCompletionPosition - this.offset);
			proposal.nameLookup = this.nameEnvironment.nameLookup;
			proposal.completionEngine = this;
			proposal.setSignature(getSignature(typeParameter.binding));
			proposal.setTypeName(javadocCompletion);
			proposal.setCompletion(javadocCompletion);
			proposal.setFlags(typeParameter.modifiers);
			proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
			proposal.setRelevance(relevance+R_INLINE_TAG);
			this.requestor.accept(proposal);
			if(DEBUG) {
				this.printDebug(proposal);
			}
		}
	}

	/**
	 * Returns completion string inserted inside a specified inline tag.
	 * @param completionName
	 * @return char[] Completion text inclunding specified inline tag
	 */
	private char[] inlineTagCompletion(char[] completionName, char[] inlineTag) {
		int tagLength= inlineTag.length;
		int completionLength = completionName.length;
		int inlineLength = 2+tagLength+1+completionLength+1;
		char[] inlineCompletion = new char[inlineLength];
		inlineCompletion[0] = '{';
		inlineCompletion[1] = '@';
		System.arraycopy(inlineTag, 0, inlineCompletion, 2, tagLength);
		inlineCompletion[tagLength+2] = ' ';
		System.arraycopy(completionName, 0, inlineCompletion, tagLength+3, completionLength);
		// do not add space at end of inline tag (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=121026)
		//inlineCompletion[inlineLength-2] = ' '; 
		inlineCompletion[inlineLength-1] = '}';
		return inlineCompletion;
	}

	protected void printDebug(CategorizedProblem error) {
		if(CompletionEngine.DEBUG) {
			System.out.print("COMPLETION - completionFailure("); //$NON-NLS-1$
			System.out.print(error);
			System.out.println(")"); //$NON-NLS-1$
		}
	}
	
	protected void printDebug(CompletionProposal proposal){
		StringBuffer buffer = new StringBuffer();
		buffer.append("COMPLETION - "); //$NON-NLS-1$
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				buffer.append("ANONYMOUS_CLASS_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.FIELD_REF :
				buffer.append("FIELD_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.KEYWORD :
				buffer.append("KEYWORD"); //$NON-NLS-1$
				break;
			case CompletionProposal.LABEL_REF :
				buffer.append("LABEL_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.LOCAL_VARIABLE_REF :
				buffer.append("LOCAL_VARIABLE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_DECLARATION :
				buffer.append("METHOD_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_REF :
				buffer.append("METHOD_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.PACKAGE_REF :
				buffer.append("PACKAGE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.TYPE_REF :
				buffer.append("TYPE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.VARIABLE_DECLARATION :
				buffer.append("VARIABLE_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION :
				buffer.append("POTENTIAL_METHOD_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_NAME_REFERENCE :
				buffer.append("METHOD_NAME_REFERENCE"); //$NON-NLS-1$
				break;
			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF :
				buffer.append("ANNOTATION_ATTRIBUT_REF"); //$NON-NLS-1$
				break;
			default :
				buffer.append("PROPOSAL"); //$NON-NLS-1$
				break;
				
		}
		
		buffer.append("{\n");//$NON-NLS-1$
		buffer.append("\tCompletion[").append(proposal.getCompletion() == null ? "null".toCharArray() : proposal.getCompletion()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buffer.append("\tDeclarationSignature[").append(proposal.getDeclarationSignature() == null ? "null".toCharArray() : proposal.getDeclarationSignature()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buffer.append("\tDeclarationKey[").append(proposal.getDeclarationKey() == null ? "null".toCharArray() : proposal.getDeclarationKey()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buffer.append("\tSignature[").append(proposal.getSignature() == null ? "null".toCharArray() : proposal.getSignature()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buffer.append("\tKey[").append(proposal.getKey() == null ? "null".toCharArray() : proposal.getKey()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		buffer.append("\tDeclarationPackage[").append(proposal.getDeclarationPackageName() == null ? "null".toCharArray() : proposal.getDeclarationPackageName()).append("]\n");
//		buffer.append("\tDeclarationType[").append(proposal.getDeclarationTypeName() == null ? "null".toCharArray() : proposal.getDeclarationTypeName()).append("]\n");
//		buffer.append("\tPackage[").append(proposal.getPackageName() == null ? "null".toCharArray() : proposal.getPackageName()).append("]\n");
//		buffer.append("\tType[").append(proposal.getTypeName() == null ? "null".toCharArray() : proposal.getTypeName()).append("]\n");
		buffer.append("\tName[").append(proposal.getName() == null ? "null".toCharArray() : proposal.getName()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		buffer.append("\tFlags[");//$NON-NLS-1$
		int flags = proposal.getFlags();
		buffer.append(Flags.toString(flags));
		if((flags & Flags.AccInterface) != 0) buffer.append("interface ");//$NON-NLS-1$
		if((flags & Flags.AccEnum) != 0) buffer.append("enum ");//$NON-NLS-1$
		buffer.append("]\n"); //$NON-NLS-1$
		
		buffer.append("\tCompletionLocation[").append(proposal.getCompletionLocation()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
		int start = proposal.getReplaceStart();
		int end = proposal.getReplaceEnd();
		buffer.append("\tReplaceStart[").append(start).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("-ReplaceEnd[").append(end).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
		if (this.source != null)
			buffer.append("\tReplacedText[").append(this.source, start, end-start).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\tTokenStart[").append(proposal.getTokenStart()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("-TokenEnd[").append(proposal.getTokenEnd()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\tRelevance[").append(proposal.getRelevance()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$

		buffer.append("}\n");//$NON-NLS-1$
		System.out.println(buffer.toString());
	}
	
	private char[][] substituteMethodTypeParameterNames(TypeVariableBinding[] typeVariables, char[][] excludedNames) {
		char[][] substituedParameterNames = new char[typeVariables.length][];
		
		for (int i = 0; i < substituedParameterNames.length; i++) {
			substituedParameterNames[i] = typeVariables[i].sourceName;
		}
		
		boolean foundConflicts = false;
		
		nextTypeParameter : for (int i = 0; i < typeVariables.length; i++) {
			TypeVariableBinding typeVariableBinding = typeVariables[i];
			char[] methodParameterName = typeVariableBinding.sourceName;
			
			for (int j = 0; j < excludedNames.length; j++) {
				char[] typeParameterName = excludedNames[j];
				if(CharOperation.equals(typeParameterName, methodParameterName, false)) {
					char[] substitution;
					if(methodParameterName.length == 1) {
						if(ScannerHelper.isUpperCase(methodParameterName[0])) {
							substitution = substituteMethodTypeParameterName(methodParameterName[0], 'A', 'Z', excludedNames, substituedParameterNames);
						} else {
							substitution = substituteMethodTypeParameterName(methodParameterName[0], 'a', 'z', excludedNames, substituedParameterNames);				
						}
					} else {
						substitution = substituteMethodTypeParameterName(methodParameterName, excludedNames, substituedParameterNames);
					}
					substituedParameterNames[i] = substitution;
					
					foundConflicts = true;
					continue nextTypeParameter;
				}
			}
		}
		
		if(foundConflicts) return substituedParameterNames;
		return null;
	}
	
	private char[] substituteMethodTypeParameterName(char firstName, char startChar, char endChar, char[][] excludedNames, char[][] otherParameterNames) {
		char name = firstName;
		next : while (true) {
			for (int i = 0 ; i < excludedNames.length ; i++){
				if(excludedNames[i].length == 1 && ScannerHelper.toLowerCase(excludedNames[i][0]) == ScannerHelper.toLowerCase(name)) {
					name++;
					if(name > endChar)
						name = startChar;
					if(name == firstName)
						return substituteMethodTypeParameterName(new char[]{firstName}, excludedNames, otherParameterNames);
					continue next;
				}
			}
			
			for (int i = 0; i < otherParameterNames.length; i++) {
				if(otherParameterNames[i].length == 1 && ScannerHelper.toLowerCase(otherParameterNames[i][0]) == ScannerHelper.toLowerCase(name)) {
					name++;
					if(name > endChar)
						name = startChar;
					if(name == firstName)
						return substituteMethodTypeParameterName(new char[]{firstName}, excludedNames, otherParameterNames);
					continue next;
				}
			}
			break next;
		}
		return new char[]{name};
	}
	
	private char[] substituteMethodTypeParameterName(char[] firstName, char[][] excludedNames, char[][] otherParameterNames) {
		char[] name = firstName;
		int count = 2;
		next : while(true) {
			for(int k = 0 ; k < excludedNames.length ; k++){
				if(CharOperation.equals(name, excludedNames[k], false)) {
					name = CharOperation.concat(firstName, String.valueOf(count++).toCharArray());
					continue next;
				}
			}
			for (int i = 0; i < otherParameterNames.length; i++) {
				if(CharOperation.equals(name, otherParameterNames[i], false)) {
					name = CharOperation.concat(firstName, String.valueOf(count++).toCharArray());
					continue next;
				}
			}
			break next;
		}
		return name;
	}
}