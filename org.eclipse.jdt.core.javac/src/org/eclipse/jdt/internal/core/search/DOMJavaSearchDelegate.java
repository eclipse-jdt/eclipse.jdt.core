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

import static org.eclipse.jdt.internal.core.search.DOMASTNodeUtils.insideDocComment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.IJavaSearchDelegate;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.PackageReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.TypeParameterReferenceMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchingNodeSet;
import org.eclipse.jdt.internal.core.search.matching.NodeSetWrapper;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.search.matching.SearchMatchingUtility;

public class DOMJavaSearchDelegate implements IJavaSearchDelegate {
	private Map<PossibleMatch, NodeSetWrapper> matchToWrapper = new HashMap<>();
	public DOMJavaSearchDelegate() {

	}
	
	@Override
	public void locateMatches(MatchLocator locator, IJavaProject javaProject, PossibleMatch[] possibleMatches, int start,
			int length) throws CoreException {
		
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
			if (!skipMatch(locator, javaProject, possibleMatches[i])) {
				org.eclipse.jdt.core.ICompilationUnit u = findUnitForPossibleMatch(locator, javaProject, possibleMatches[i]);
				unitArray[i] = u;
				cuToMatch.put(u, possibleMatches[i]);
			}
		}
		org.eclipse.jdt.core.ICompilationUnit[] nonNullUnits = Arrays.asList(unitArray).stream().filter(x -> x != null)
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
							domUnits[i] = ast;
							nonNullDomIndexes.add(i);
							locator.currentPossibleMatch = pm;
							NodeSetWrapper wrapper = matchToWrapper.get(possibleMatches[i]);
							ast.accept(new PatternLocatorVisitor(locator.patternLocator,
									wrapper, locator));
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
					try {
						SearchMatchingUtility.reportSearchMatch(locator, match);
					} catch (CoreException ex) {
						ILog.get().error(ex.getMessage(), ex);
					}
				}
			}
		}
	}
	
	private NodeSetWrapper wrapNodeSet(MatchingNodeSet nodeSet) {
		return new NodeSetWrapper(nodeSet);
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
		IResource resource = possibleMatch.resource;
		if (node instanceof MethodDeclaration || node instanceof AbstractTypeDeclaration
				|| node instanceof VariableDeclaration) {
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
			return new MethodReferenceMatch(enclosing, accuracy, method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					isSynthetic, false, insideDocComment(node), getParticipant(locator), resource);
		}
		if (node instanceof SuperMethodInvocation method) {
			return new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node.getParent()), accuracy,
					method.getName().getStartPosition(),
					method.getStartPosition() + method.getLength() - method.getName().getStartPosition(), false,
					method.resolveMethodBinding().isSynthetic(), true, insideDocComment(node), getParticipant(locator),
					resource);
		}
		if (node instanceof ClassInstanceCreation newInstance) {
			return new MethodReferenceMatch(
					DOMASTNodeUtils.getEnclosingJavaElement(
							node.getParent().getParent()) /* we don't want the variable decl */,
					accuracy, newInstance.getStartPosition(), newInstance.getLength(), true,
					newInstance.resolveConstructorBinding().isSynthetic(), false, insideDocComment(node),
					getParticipant(locator), resource);
		}
		if (node instanceof SuperConstructorInvocation newInstance) {
			return new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node), accuracy,
					newInstance.getStartPosition(), newInstance.getLength(), true,
					newInstance.resolveConstructorBinding().isSynthetic(), false, insideDocComment(node),
					getParticipant(locator), resource);
		}
		if (node instanceof CreationReference constructorRef) {
			return new MethodReferenceMatch(DOMASTNodeUtils.getEnclosingJavaElement(node), accuracy,
					constructorRef.getStartPosition(), constructorRef.getLength(), true,
					constructorRef.resolveMethodBinding().isSynthetic(), true, insideDocComment(node), getParticipant(locator),
					resource);
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
			IBinding b = DOMASTNodeUtils.getBinding(nt);
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
			return ret;
		}
		if (node instanceof org.eclipse.jdt.core.dom.TypeParameter nodeTP) {
			IJavaElement element = DOMASTNodeUtils.getEnclosingJavaElement(node);
			return new TypeParameterReferenceMatch(element, accuracy, nodeTP.getName().getStartPosition(),
					nodeTP.getName().getLength(), DOMASTNodeUtils.insideDocComment(node), getParticipant(locator), resource);
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
				return ref;
			}
			if (b instanceof IVariableBinding variable) {
				if (variable.isField()) {
					return new FieldReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(), true,
							true, insideDocComment(node), getParticipant(locator), resource);
				}
				return new LocalVariableReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(),
						true, true, insideDocComment(node), getParticipant(locator), resource);
			}
			if (b instanceof IPackageBinding) {
				return new PackageReferenceMatch(enclosing, accuracy, name.getStartPosition(), name.getLength(),
						insideDocComment(name), getParticipant(locator), resource);
			}
			if (b instanceof IMethodBinding) {
				return new MethodReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(),
						insideDocComment(node), getParticipant(locator), resource);
			}
			// more...?
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

}
