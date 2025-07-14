/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 377883 - NPE on open Call Hierarchy
 *     Microsoft Corporation - Contribution for bug 575562 - improve completion search performance
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import static org.eclipse.jdt.internal.core.JavaModelManager.trace;
import static org.eclipse.jdt.internal.core.search.DOMASTNodeUtils.insideDocComment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.IJavaSearchDelegate;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.ModuleDeclarationMatch;
import org.eclipse.jdt.core.search.ModuleReferenceMatch;
import org.eclipse.jdt.core.search.PackageReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeParameterReferenceMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.ModularClassFile;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.search.matching.ClassFileMatchLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMLocalVariableLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMPatternLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchingNodeSet;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.matching.ModularClassFileMatchLocator;
import org.eclipse.jdt.internal.core.search.matching.NodeSetWrapper;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.Util;

public class DOMJavaSearchDelegate implements IJavaSearchDelegate {
	private Map<PossibleMatch, NodeSetWrapper> matchToWrapper = new HashMap<>();
	public DOMJavaSearchDelegate() {

	}

	@Override
	public void locateMatches(MatchLocator locator, IJavaProject javaProject, PossibleMatch[] possibleMatches, int start,
			int length) throws CoreException {
		locator.initialize((JavaProject)javaProject, length);
		for( int i = 0; i < possibleMatches.length; i++ ) {
			matchToWrapper.put(possibleMatches[i], wrapNodeSet(possibleMatches[i].nodeSet));
		}


		Map<String, String> map = javaProject.getOptions(true);
		map.put(CompilerOptions.OPTION_TaskTags, org.eclipse.jdt.internal.compiler.util.Util.EMPTY_STRING);
		locator.options = new CompilerOptions(map);

		// Original implementation used Map throughout, however,
		// PossibleMatch was determined to be a bad / non-unique key where the
		// hashCode and equals methods would let two different matches overlap.
		// So we have to use Arrays and Lists, like a bunch of barbarians.
		org.eclipse.jdt.core.ICompilationUnit[] unitArray = new org.eclipse.jdt.core.ICompilationUnit[possibleMatches.length];

		Map<org.eclipse.jdt.core.ICompilationUnit, PossibleMatch> cuToMatch = new HashMap<>();
		for (int i = 0; i < possibleMatches.length; i++) {
			var currentPossibleMatch = possibleMatches[i];
			locator.currentPossibleMatch = currentPossibleMatch;
			if (!skipMatch(locator, javaProject, currentPossibleMatch)) {
				org.eclipse.jdt.core.ICompilationUnit u = findUnitForPossibleMatch(locator, javaProject, possibleMatches[i]);
				unitArray[i] = u;
				if (u != null) {
					cuToMatch.put(u, possibleMatches[i]);
				} else if (currentPossibleMatch.openable instanceof IClassFile classFile) {
					locateMatchesForBinary(currentPossibleMatch, locator);
				}
			}
		}
		org.eclipse.jdt.core.ICompilationUnit[] nonNullUnits = Arrays.asList(unitArray).stream().filter(Objects::nonNull)
				.toArray(org.eclipse.jdt.core.ICompilationUnit[]::new);
		if (nonNullUnits.length == 0) {
			return;
		}

		Set<WorkingCopyOwner> ownerSet = new HashSet<>();
		for (int i = 0; i < nonNullUnits.length; i++) {
			if (nonNullUnits[i].getOwner() != null) {
				ownerSet.add(nonNullUnits[i].getOwner());
			}
		}
		WorkingCopyOwner owner = null;
		if (ownerSet.size() == 1) {
			owner = ownerSet.toArray(new WorkingCopyOwner[ownerSet.size()])[0];
		}

		ASTParser astParser = ASTParser.newParser(AST.getJLSLatest());
		astParser.setCompilerOptions(javaProject.getOptions(true));
		astParser.setProject(javaProject);
		astParser.setStatementsRecovery(true);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		if (owner != null)
			astParser.setWorkingCopyOwner(owner);

		org.eclipse.jdt.core.dom.CompilationUnit[] domUnits = new org.eclipse.jdt.core.dom.CompilationUnit[possibleMatches.length];

		List<Integer> nonNullDomIndexes = new ArrayList<>();
		astParser.createASTs(nonNullUnits, new String[0], new ASTRequestor() {
			@Override
			public void acceptAST(org.eclipse.jdt.core.ICompilationUnit source,
					org.eclipse.jdt.core.dom.CompilationUnit ast) {
				PossibleMatch pm = cuToMatch.get(source);
				if (pm != null) {
					for (int i = 0; i < possibleMatches.length; i++) {
						if (possibleMatches[i] == pm) {
//							String s = CharOperation.toString(pm.compoundName);
//							if( "g1.t.s.ref.R1".equals(s)) {
//							if( "g3.t.ref.R4".equals(s)) {
//								int z = 5; z++; if( z == 3 ) {}
//							}
							domUnits[i] = ast;
							nonNullDomIndexes.add(i);
							locator.currentPossibleMatch = pm;
							NodeSetWrapper wrapper = matchToWrapper.get(possibleMatches[i]);
							ast.accept(new PatternLocatorVisitor(locator, wrapper));
							return;
						}
					}
				}
			}
			// todo, use a subprogressmonitor or slice it
		}, locator.progressMonitor);

		Collections.sort(nonNullDomIndexes);
		for (int x : nonNullDomIndexes) {
			PossibleMatch possibleMatch = possibleMatches[x];
			locator.currentPossibleMatch = possibleMatch;
			NodeSetWrapper wrapper = this.matchToWrapper.get(possibleMatch);
			for (org.eclipse.jdt.core.dom.ASTNode node : wrapper.trustedASTNodeLevels.keySet()) {
				int level = wrapper.trustedASTNodeLevels.get(node);
				SearchMatch match = toMatch(locator, node, level, possibleMatch);
				if (match != null && match.getElement() != null) {
					DOMPatternLocator locator2 = DOMPatternLocatorFactory.createWrapper(locator.patternLocator, locator.pattern);
					locator2.setCurrentMatch(match);
					locator2.setCurrentNode(node);
					locator2.reportSearchMatch(locator, node, match);
				}
			}
		}
	}

	private NodeSetWrapper wrapNodeSet(MatchingNodeSet nodeSet) {
		return new NodeSetWrapper(nodeSet.mustResolve);
	}

	private boolean skipMatch(MatchLocator locator, IJavaProject javaProject, PossibleMatch possibleMatch) {
		if (locator.options.sourceLevel >= ClassFileConstants.JDK9) {
			char[] pModuleName = possibleMatch.getModuleName();
			if (pModuleName != null && locator.lookupEnvironment.getModule(pModuleName) == null)
				return true;
		}
		return false;
	}

	private org.eclipse.jdt.core.ICompilationUnit findUnitForPossibleMatch(MatchLocator locator, IJavaProject jp, PossibleMatch match) {
		if (!skipMatch(locator, jp, match)) {
			if (match.openable instanceof org.eclipse.jdt.core.ICompilationUnit cu) {
				return cu;
			} else if (match.openable instanceof ITypeRoot tr) {
				ITypeRoot toOpen = tr;
				try {
					// If this is a nested class like p/X$Y, it won't work. When it gets to
					// the bindings for p/X, it thinks it is in p/X$Y.class file. :|
					String n = tr.getElementName();
					if (n.toLowerCase().endsWith(".class") && n.contains("$")) {
						String enclosingSourceFile = n.substring(0, n.indexOf("$")) + ".class";
						IJavaElement parent = tr.getParent();
						if (parent instanceof IPackageFragment ipf) {
							IOpenable open2 = ipf.getClassFile(enclosingSourceFile);
							if (open2 instanceof ITypeRoot tr2) {
								toOpen = tr2;
							}
						}
					}
					if (toOpen instanceof IClassFile classFile && classFile.getBuffer() == null) {
						return null;
					}
					org.eclipse.jdt.core.ICompilationUnit ret = toOpen.getWorkingCopy(null, new NullProgressMonitor());
					return ret;
				} catch (JavaModelException jme) {
					// Ignore for now
				}
			}
		}
		return null;
	}

	private SearchMatch toMatch(MatchLocator locator, org.eclipse.jdt.core.dom.ASTNode node, int accuracy, PossibleMatch possibleMatch) {
		if (node == null) {
			return null;
		}
		SearchMatch sm = toCoreMatch(locator, node, accuracy, possibleMatch);
		if( accuracy == SearchPattern.R_ERASURE_MATCH) {
			sm.setRule(SearchPattern.R_ERASURE_MATCH);
		}
		return sm;
	}

	private SearchMatch toCoreMatch(MatchLocator locator, org.eclipse.jdt.core.dom.ASTNode node, int accuracy, PossibleMatch possibleMatch) {
		IResource resource = possibleMatch.resource;
		if (node instanceof MethodDeclaration || node instanceof AbstractTypeDeclaration
				|| node instanceof VariableDeclaration || node instanceof AnnotationTypeMemberDeclaration) {
			IJavaElement javaElement = DOMASTNodeUtils.getDeclaringJavaElement(node);
			if (javaElement != null) {
				ISourceRange range = new SourceRange(node.getStartPosition(), node.getLength());
				if (javaElement instanceof NamedMember named) {
					try {
						range = named.getNameRange();
					} catch (JavaModelException ex) {
						ILog.get().error(ex.getMessage(), ex);
					}
				}
				return locator.newDeclarationMatch(javaElement, null, accuracy, range.getOffset(), range.getLength());
			}
		}
		if (node instanceof MethodInvocation method) {
			IJavaElement enclosing = DOMASTNodeUtils.getEnclosingJavaElement(node.getParent());
			IMethodBinding mb = method.resolveMethodBinding();
			boolean isSynthetic = mb != null && mb.isSynthetic();
			var res = new MethodReferenceMatch(enclosing, accuracy, method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					isSynthetic, (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof SuperMethodInvocation method) {
			IMethodBinding mb = method.resolveMethodBinding();
			var res = new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node.getParent()), accuracy,
					method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					mb.isSynthetic(), (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator),
					resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof ClassInstanceCreation newInstance) {
			IMethodBinding mb = newInstance.resolveConstructorBinding();
			var res = new MethodReferenceMatch(
					DOMASTNodeUtils.getEnclosingJavaElement(
							node.getParent().getParent()) /* we don't want the variable decl */,
					accuracy, newInstance.getStartPosition(), newInstance.getLength(), true,
					mb.isSynthetic(), (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node),
					getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof ConstructorInvocation newInstance) {
			IMethodBinding mb = newInstance.resolveConstructorBinding();
			var res = new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node), accuracy,
					newInstance.getStartPosition(), newInstance.getLength(), true,
					mb.isSynthetic(), (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node),
					getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof MethodRef method && method.resolveBinding() instanceof IMethodBinding mb) {
			IJavaElement enclosing = DOMASTNodeUtils.getEnclosingJavaElement(node.getParent());
			boolean isSynthetic = mb != null && mb.isSynthetic();
			var res = new MethodReferenceMatch(enclosing, accuracy, method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					isSynthetic, (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof SuperConstructorInvocation newInstance) {
			IMethodBinding mb = newInstance.resolveConstructorBinding();
			var res = new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node), accuracy,
					newInstance.getStartPosition(), newInstance.getLength(), true,
					mb.isSynthetic(), (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node),
					getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof CreationReference constructorRef) {
			IMethodBinding mb = constructorRef.resolveMethodBinding();
			var res = new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node), accuracy,
					constructorRef.getStartPosition(), constructorRef.getLength(), true,
					mb.isSynthetic(), (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator),
					resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof ExpressionMethodReference method) {
			IJavaElement enclosing = DOMASTNodeUtils.getEnclosingJavaElement(node.getParent());
			IMethodBinding mb = method.resolveMethodBinding();
			boolean isSynthetic = mb != null && mb.isSynthetic();
			var res = new MethodReferenceMatch(enclosing, accuracy, method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					isSynthetic, (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof TypeMethodReference method) {
			IJavaElement enclosing = DOMASTNodeUtils.getEnclosingJavaElement(node.getParent());
			IMethodBinding mb = method.resolveMethodBinding();
			boolean isSynthetic = mb != null && mb.isSynthetic();
			var res = new MethodReferenceMatch(enclosing, accuracy, method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					isSynthetic, (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof SuperMethodReference method) {
			IJavaElement enclosing = DOMASTNodeUtils.getEnclosingJavaElement(node.getParent());
			IMethodBinding mb = method.resolveMethodBinding();
			boolean isSynthetic = mb != null && mb.isSynthetic();
			var res = new MethodReferenceMatch(enclosing, accuracy, method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					isSynthetic, (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node.getLocationInParent() == SingleMemberAnnotation.VALUE_PROPERTY && locator.pattern instanceof MethodPattern) {
			var res = new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node), accuracy,
					node.getStartPosition(), node.getLength(), true,
					false, (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator),
					resource);
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof EnumConstantDeclaration enumConstantDeclaration) {
			int start = enumConstantDeclaration.getStartPosition();
			int len = enumConstantDeclaration.getLength();
			if (enumConstantDeclaration.getAnonymousClassDeclaration() != null) {
				len = enumConstantDeclaration.getAnonymousClassDeclaration().getStartPosition() - start;
			}
			return new FieldDeclarationMatch(DOMASTNodeUtils.getDeclaringJavaElement(node), accuracy, start, len,
					getParticipant(locator), resource);
		}
		if (node instanceof Type nt) {
			//IBinding b = DOMASTNodeUtils.getBinding(nt);
			IJavaElement element = DOMASTNodeUtils.getEnclosingJavaElement(node);
			if (element instanceof LocalVariable) {
				element = element.getParent();
			}
			TypeReferenceMatch ret = new TypeReferenceMatch(element, accuracy, node.getStartPosition(),
					node.getLength(), DOMASTNodeUtils.insideDocComment(node), getParticipant(locator), resource);
			if (nt.isParameterizedType()) {
				if (((ParameterizedType) nt).typeArguments().size() == 0) {
					ret.setRaw(true);
				}
			}
			IJavaElement localJavaElement = DOMASTNodeUtils.getLocalJavaElement(node);
			if (!Objects.equals(localJavaElement, element)) {
				ret.setLocalElement(localJavaElement);
			}
			if (node.getLocationInParent() == VariableDeclarationStatement.TYPE_PROPERTY && node.getParent() instanceof VariableDeclarationStatement stmt && stmt.fragments().size() > 1) {
				ret.setOtherElements(((List<VariableDeclarationFragment>)stmt.fragments())
						.subList(1, stmt.fragments().size())
						.stream()
						.map(VariableDeclarationFragment::resolveBinding)
						.filter(x -> x != null)
						.map(IVariableBinding::getJavaElement)
						.filter(x -> x != null)
						.toArray(IJavaElement[]::new));
			}
			if (node.getLocationInParent() == FieldDeclaration.TYPE_PROPERTY && node.getParent() instanceof FieldDeclaration stmt && stmt.fragments().size() > 1) {
				ret.setOtherElements(((List<VariableDeclarationFragment>)stmt.fragments())
						.subList(1, stmt.fragments().size())
						.stream()
						.map(VariableDeclarationFragment::resolveBinding)
						.filter(x -> x != null)
						.map(IVariableBinding::getJavaElement)
						.filter(x -> x != null)
						.toArray(IJavaElement[]::new));
			}
			return ret;
		}
		if (node instanceof org.eclipse.jdt.core.dom.TypeParameter nodeTP) {
			IJavaElement element = DOMASTNodeUtils.getEnclosingJavaElement(node);
			return new TypeParameterReferenceMatch(element, accuracy, nodeTP.getName().getStartPosition(),
					nodeTP.getName().getLength(), DOMASTNodeUtils.insideDocComment(node), getParticipant(locator), resource);
		}
		if (node instanceof LambdaExpression lambda) {
			IJavaElement enclosing = DOMASTNodeUtils.getLocalJavaElement(node);
			IMethodBinding mb = lambda.resolveMethodBinding();
			boolean isSynthetic = mb != null && mb.isSynthetic();
			int arrowEnd = lambda.getBody().getStartPosition() - 1;
			try {
				if (enclosing != null && enclosing.getAncestor(IJavaElement.COMPILATION_UNIT) instanceof ICompilationUnit unit) {
					IBuffer buffer = unit.getBuffer();
					while (arrowEnd > 0 && buffer.getChar(arrowEnd) != '>') {
						arrowEnd--;
					}
					arrowEnd++;
				}
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			var res = new MethodReferenceMatch(enclosing, accuracy, lambda.getStartPosition(),
					arrowEnd - lambda.getStartPosition(), false,
					isSynthetic, (accuracy & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0, insideDocComment(node), getParticipant(locator), resource);
			res.setRaw(mb != null && mb.isRawMethod());
			res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
			return res;
		}
		if (node instanceof Name name) {
			IBinding b = name.resolveBinding();
			IJavaElement enclosing = DOMASTNodeUtils.getEnclosingJavaElement(node);
//		if( b == null ) {
//			// This fixes some issues but causes even more failures
//			return new SearchMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(), getParticipant(locator), resource);
//		}
			if (b instanceof ITypeBinding btb) {
				TypeReferenceMatch ref = new TypeReferenceMatch(enclosing, accuracy, node.getStartPosition(),
						node.getLength(), insideDocComment(node), getParticipant(locator), resource);
				if (btb.isRawType())
					ref.setRaw(true);
				ref.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
				return ref;
			}
			if (b instanceof IVariableBinding variable) {
				if (variable.isField()) {
					var res = new FieldReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(),
							DOMLocalVariableLocator.isRead(name), DOMLocalVariableLocator.isWrite(name),
							insideDocComment(node), getParticipant(locator), resource);
					res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
					return res;
				}
				return new LocalVariableReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(),
						DOMLocalVariableLocator.isRead(name), DOMLocalVariableLocator.isWrite(name),
						insideDocComment(node), getParticipant(locator), resource);
			}
			if (b instanceof IPackageBinding) {
				var res = new PackageReferenceMatch(enclosing, accuracy, name.getStartPosition(), name.getLength(),
						insideDocComment(name), getParticipant(locator), resource);
				res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
				return res;
			}
			if (b instanceof IMethodBinding) {
				var res = new MethodReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(),
						insideDocComment(node), getParticipant(locator), resource);
				res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
				return res;
			}
			if (b instanceof IModuleBinding) {
				var res = new ModuleReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(),
						insideDocComment(node), getParticipant(locator), resource);
				res.setLocalElement(DOMASTNodeUtils.getLocalJavaElement(node));
				return res;
			}
			// more...?
		}
		if (node instanceof ModuleDeclaration mod) {
			return new ModuleDeclarationMatch(DOMASTNodeUtils.getDeclaringJavaElement(mod), accuracy, mod.getName().getStartPosition(), mod.getName().getLength(), null, resource);
		}
		if (node.getLocationInParent() == SimpleType.NAME_PROPERTY
			|| node.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
			// more...?
			return toMatch(locator, node.getParent(), accuracy, possibleMatch);
		}
		return null;
	}
	public SearchParticipant getParticipant(MatchLocator locator) {
		return locator.currentPossibleMatch.document.getParticipant();
	}

	private void locateMatchesForBinary(PossibleMatch possibleMatch, MatchLocator locator) {
		if (possibleMatch.openable instanceof ClassFile classFile) {
			IBinaryType info = null;
			try {
				info = getBinaryInfo(classFile, classFile.resource());
			} catch (CoreException ce) {
				ILog.get().error(ce.getMessage(), ce);
			}
			if (info != null) {
				try {
					new ClassFileMatchLocator().locateMatches(locator, classFile, info);
				} catch (CoreException e) {
					ILog.get().error(e.getMessage(), e);
				}
			}
		} else if (possibleMatch.openable instanceof ModularClassFile modularClassFile) { // no source
			try {
				new ModularClassFileMatchLocator().locateMatches(locator, modularClassFile);
			} catch (CoreException e) {
				ILog.get().error(e.getMessage(), e);
			}
		}
	}

	protected IBinaryType getBinaryInfo(ClassFile classFile, IResource resource) throws CoreException {
		BinaryType binaryType = (BinaryType) classFile.getType();
		if (classFile.isOpen())
			return binaryType.getElementInfo(); // reuse the info from the java model cache

		// create a temporary info
		IBinaryType info;
		try {
			PackageFragment pkg = (PackageFragment) classFile.getParent();
			PackageFragmentRoot root = (PackageFragmentRoot) pkg.getParent();
			if (root.isArchive()) {
				// class file in a jar
				String classFileName = classFile.getElementName();
				String classFilePath = Util.concatWith(pkg.names, classFileName, '/');
				ZipFile zipFile = null;
				try {
					zipFile = ((JarPackageFragmentRoot) root).getJar();
					info = ClassFileReader.read(zipFile, classFilePath);
				} finally {
					JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
				}
			} else {
				// class file in a directory
				info = Util.newClassFileReader(resource);
			}
			if (info == null) throw binaryType.newNotPresentException();
			return info;
		} catch (ClassFormatException e) {
			if (JobManager.VERBOSE) {
				trace("", e); //$NON-NLS-1$
			}
			return null;
		} catch (java.io.IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}

}
