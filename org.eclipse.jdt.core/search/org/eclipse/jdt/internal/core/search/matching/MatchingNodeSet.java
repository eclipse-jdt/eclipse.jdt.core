/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfLong;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.util.SimpleSet;

/**
 * A set of matches and potential matches.
 */
public class MatchingNodeSet {

MatchLocator locator;
int matchContainer;

/**
 * Map of matching ast nodes that don't need to be resolved to their accuracy level.
 * Each node is removed as it is reported.
 */
SimpleLookupTable matchingNodes = new SimpleLookupTable(3);
HashtableOfLong matchingNodesKeys = new HashtableOfLong(3);
static Integer EXACT_MATCH = new Integer(IJavaSearchResultCollector.EXACT_MATCH);
static Integer POTENTIAL_MATCH = new Integer(IJavaSearchResultCollector.POTENTIAL_MATCH);

/**
 * Set of potential matching ast nodes. They need to be resolved
 * to determine if they really match the search pattern.
 */
SimpleSet potentialMatchingNodesSet = new SimpleSet(7);
HashtableOfLong potentialMatchingNodesKeys = new HashtableOfLong(7);

/**
 * An ast visitor that visits local type declarations.
 */
public class LocalDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	IJavaElement enclosingElement;
	public LocalDeclarationVisitor(IJavaElement enclosingElement) {
		this.enclosingElement = enclosingElement;
	}
	public boolean visit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration, BlockScope scope) {
		try {
			reportMatching(anonymousTypeDeclaration, enclosingElement);
			return false; // don't visit members as this was done during reportMatching(...)
		} catch (CoreException e) {
			throw new WrappedCoreException(e);
		}
	}
	public boolean visit(LocalTypeDeclaration typeDeclaration, BlockScope scope) {
		try {
			// check type declaration
			Integer level = (Integer) matchingNodes.removeKey(typeDeclaration);
			if (level != null)
				locator.reportTypeDeclaration(typeDeclaration, enclosingElement, level.intValue());

			// check inside type declaration
			reportMatching(typeDeclaration, enclosingElement);
			return false; // don't visit members as this was done during reportMatching(...)
		} catch (CoreException e) {
			throw new WrappedCoreException(e);
		}
	}
	public boolean visit(MemberTypeDeclaration typeDeclaration, ClassScope scope) {
		try {
			reportMatching(typeDeclaration, enclosingElement);
			return false; // don't visit members as this was done during reportMatching(...)
		} catch (CoreException e) {
			throw new WrappedCoreException(e);
		}
	}
}	

public class WrappedCoreException extends RuntimeException {
	public CoreException coreException;
	public WrappedCoreException(CoreException coreException) {
		this.coreException = coreException;
	}
}

public MatchingNodeSet(MatchLocator locator) {
	this.locator = locator;
	this.matchContainer = locator.patternLocator.matchContainer();
}
public void addMatch(AstNode node, int matchLevel) {
	switch (matchLevel) {
		case PatternLocator.POTENTIAL_MATCH:
			addPossibleMatch(node);
			break;
		case PatternLocator.ACCURATE_MATCH:
			addTrustedMatch(node);
	}
}
public void addPossibleMatch(AstNode node) {
	// remove existing node at same position from set
	// (case of recovery that created the same node several time
	// see http://bugs.eclipse.org/bugs/show_bug.cgi?id=29366)
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	AstNode existing = (AstNode) this.potentialMatchingNodesKeys.get(key);
	if (existing != null && existing.getClass().equals(node.getClass()))
		this.potentialMatchingNodesSet.remove(existing);

	// add node to set
	this.potentialMatchingNodesSet.add(node);
	this.potentialMatchingNodesKeys.put(key, node);
}
public void addTrustedMatch(AstNode node) {
	// remove existing node at same position from set
	// (case of recovery that created the same node several time
	// see http://bugs.eclipse.org/bugs/show_bug.cgi?id=29366)
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	AstNode existing = (AstNode) this.matchingNodesKeys.get(key);
	if (existing != null && existing.getClass().equals(node.getClass()))
		this.matchingNodes.removeKey(existing);
	
	// map node to its accuracy level
	this.matchingNodes.put(node, EXACT_MATCH);
	this.matchingNodesKeys.put(key, node);
}
private boolean hasPotentialNodes(int start, int end) {
	Object[] nodes = this.potentialMatchingNodesSet.values;
	for (int i = 0, l = nodes.length; i < l; i++) {
		AstNode node = (AstNode) nodes[i];
		if (node != null && start <= node.sourceStart && node.sourceEnd <= end)
			return true;
	}
	return false;
}
/**
 * Returns the matching nodes that are in the given range in the source order.
 */
private AstNode[] matchingNodes(int start, int end) {
	ArrayList nodes = null;
	Object[] keyTable = this.matchingNodes.keyTable;
	for (int i = 0, l = keyTable.length; i < l; i++) {
		AstNode node = (AstNode) keyTable[i];
		if (node != null && start <= node.sourceStart && node.sourceEnd <= end) {
			if (nodes == null) nodes = new ArrayList();
			nodes.add(node);
		}
	}
	if (nodes == null) return null;

	AstNode[] result = new AstNode[nodes.size()];
	nodes.toArray(result);

	// sort nodes by source starts
	Util.Comparer comparer = new Util.Comparer() {
		public int compare(Object o1, Object o2) {
			return ((AstNode) o1).sourceStart - ((AstNode) o2).sourceStart;
		}
	};
	Util.sort(result, comparer);
	return result;
}
private void purgeMethodStatements(TypeDeclaration type, boolean checkEachMethod) {
	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		if (checkEachMethod) {
			for (int j = 0, k = methods.length; j < k; j++) {
				AbstractMethodDeclaration method = methods[j];
				if (!hasPotentialNodes(method.declarationSourceStart, method.declarationSourceEnd))
					method.statements = null;
			}
		} else {
			for (int j = 0, k = methods.length; j < k; j++)
				methods[j].statements = null;
		}
	}

	MemberTypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0, l = memberTypes.length; i < l; i++) {
			TypeDeclaration memberType = memberTypes[i];
			boolean alsoHasMatchingMethods = checkEachMethod &&
				hasPotentialNodes(memberType.declarationSourceStart, memberType.declarationSourceEnd);
			purgeMethodStatements(memberType, alsoHasMatchingMethods);
		}
	}
}
/**
 * Called prior to the unit being resolved. Reduce the parse tree where possible.
 */
public void reduceParseTree(CompilationUnitDeclaration unit) {
	// remove statements from methods that have no potential matching nodes
	TypeDeclaration[] types = unit.types;
	for (int i = 0, l = types.length; i < l; i++) {
		TypeDeclaration type = types[i];
		purgeMethodStatements(type, hasPotentialNodes(type.declarationSourceStart, type.declarationSourceEnd));
	}
}
public Object removePossibleMatch(AstNode node) {
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	this.potentialMatchingNodesKeys.put(key, null);
	return this.potentialMatchingNodesSet.remove(node);
}
public Object removeTrustedMatch(AstNode node) {
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	this.matchingNodesKeys.put(key, null);
	return this.matchingNodes.removeKey(node);
}
/**
 * Visit the given method declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the method declaration has already been checked.
 */
private void reportMatching(AbstractMethodDeclaration method, IJavaElement parent, boolean typeInHierarchy) throws CoreException {
	// declaration in this method
	// (NB: declarations must be searched first (see bug 20631 Declaration of local binary type not found)
	if ((method.bits & AstNode.HasLocalTypeMASK) != 0) {
		LocalDeclarationVisitor localDeclarationVisitor = new LocalDeclarationVisitor(
			(parent instanceof IType)
				? this.locator.createMethodHandle(method, (IType) parent)
				: parent);
		try {
			method.traverse(localDeclarationVisitor, (ClassScope)null);
		} catch (WrappedCoreException e) {
			throw e.coreException;
		}
	}

	// references in this method
	if (typeInHierarchy) {
		AstNode[] nodes = matchingNodes(method.declarationSourceStart, method.declarationSourceEnd);
		if (nodes != null) {
			for (int i = 0, l = nodes.length; i < l; i++) {
				AstNode node = nodes[i];
				Integer level = (Integer) this.matchingNodes.removeKey(node);
				if ((this.matchContainer & PatternLocator.METHOD_CONTAINER) != 0)
					this.locator.reportReference(node, method, parent, level.intValue());
			}
		}
	}
}
/**
 * Visit the given resolved parse tree and report the nodes that match the search pattern.
 */
public void reportMatching(CompilationUnitDeclaration unit, boolean mustResolve) throws CoreException {
	if (mustResolve) {
		// move the potential matching nodes that exactly match the search pattern to the matching nodes set
		Object[] nodes = this.potentialMatchingNodesSet.values;
		for (int i = 0, l = nodes.length; i < l; i++) {
			AstNode node = (AstNode) nodes[i];
			if (node == null) continue;
			if (node instanceof ImportReference) {
				// special case for import refs: they don't know their binding
				// import ref cannot be in the hirarchy of a type
				if (this.locator.hierarchyResolver != null) continue;

				ImportReference importRef = (ImportReference) node;
				Binding binding = importRef.onDemand
					? unit.scope.getTypeOrPackage(CharOperation.subarray(importRef.tokens, 0, importRef.tokens.length))
					: unit.scope.getTypeOrPackage(importRef.tokens);
				this.locator.patternLocator.matchLevelAndReportImportRef(importRef, binding, this.locator);
			} else {
				int level = this.locator.patternLocator.resolveLevel(node);
				if (level == PatternLocator.ACCURATE_MATCH)
					this.matchingNodes.put(node, EXACT_MATCH);
				else if (level == PatternLocator.INACCURATE_MATCH)
					this.matchingNodes.put(node, POTENTIAL_MATCH);
			}
		}
		this.potentialMatchingNodesSet = new SimpleSet();
	}

	if (this.matchingNodes.elementSize == 0) return; // no matching nodes were found

	boolean searchInsideCompilationUnits = (this.matchContainer & PatternLocator.COMPILATION_UNIT_CONTAINER) != 0;
	ImportReference pkg = unit.currentPackage;
	if (pkg != null && this.matchingNodes.removeKey(pkg) != null)
		if (searchInsideCompilationUnits)
			this.locator.reportPackageDeclaration(pkg);

	ImportReference[] imports = unit.imports;
	if (imports != null) {
		for (int i = 0, l = imports.length; i < l; i++) {
			ImportReference importRef = imports[i];
			Integer level = (Integer) this.matchingNodes.removeKey(importRef);
			if (level != null && searchInsideCompilationUnits)
				this.locator.reportImport(importRef, level.intValue());
		}
	}

	TypeDeclaration[] types = unit.types;
	if (types != null) {
		for (int i = 0, l = types.length; i < l; i++) {
			if (this.matchingNodes.elementSize == 0) return; // reported all the matching nodes
			TypeDeclaration type = types[i];
			Integer level = (Integer) this.matchingNodes.removeKey(type);
			if (level != null && searchInsideCompilationUnits)
				this.locator.reportTypeDeclaration(type, null, level.intValue());
			reportMatching(type, null);
		}
	}
}
/**
 * Visit the given field declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the field declaration has already been checked.
 */
private void reportMatching(FieldDeclaration field, IJavaElement parent, TypeDeclaration type, boolean typeInHierarchy) throws CoreException {
	// handle the nodes for the local type first
	if ((field.bits & AstNode.HasLocalTypeMASK) != 0) {
		LocalDeclarationVisitor localDeclarationVisitor = new LocalDeclarationVisitor(
			(parent instanceof IType)
				? (field.isField()
					? (IJavaElement) this.locator.createFieldHandle(field, (IType) parent)
					: (IJavaElement) this.locator.createInitializerHandle(type, field, (IType) parent))
				: parent);
		try {
			field.traverse(localDeclarationVisitor, null);
		} catch (WrappedCoreException e) {
			throw e.coreException;
		}
	}

	if (typeInHierarchy) {
		AstNode[] nodes = matchingNodes(field.declarationSourceStart, field.declarationSourceEnd);
		if (nodes != null) {
			for (int i = 0, l = nodes.length; i < l; i++) {
				AstNode node = nodes[i];
				Integer level = (Integer) this.matchingNodes.removeKey(node);
				if ((this.matchContainer & PatternLocator.FIELD_CONTAINER) != 0)
					this.locator.reportReference(node, type, field, parent, level.intValue());
			}
		}
	}
}
/**
 * Visit the given type declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the type declaration has already been checked.
 */
public void reportMatching(TypeDeclaration type, IJavaElement parent) throws CoreException {
	// create type handle
	IJavaElement enclosingElement;
	if (parent == null) {
		enclosingElement = this.locator.createTypeHandle(type.name);
	} else if (parent instanceof IType) {
		enclosingElement = this.locator.createTypeHandle((IType) parent, type.name);
		if (enclosingElement == null) return;
	} else {
		enclosingElement = parent;
	}

	// super types
	boolean searchInsideTypes = (this.matchContainer & PatternLocator.CLASS_CONTAINER) != 0;
	if (type instanceof AnonymousLocalTypeDeclaration) {
		TypeReference superType = ((AnonymousLocalTypeDeclaration) type).allocation.type;
		if (superType != null) {
			Integer level = (Integer) this.matchingNodes.removeKey(superType);
			if (level != null && searchInsideTypes)
				this.locator.reportSuperTypeReference(superType, enclosingElement, level.intValue());
		}
	} else {
		TypeReference superClass = type.superclass;
		if (superClass != null) {
			Integer level = (Integer) this.matchingNodes.removeKey(superClass);
			if (level != null && searchInsideTypes)
				this.locator.reportSuperTypeReference(superClass, enclosingElement, level.intValue());
		}
		TypeReference[] superInterfaces = type.superInterfaces;
		if (superInterfaces != null) {
			for (int i = 0, l = superInterfaces.length; i < l; i++) {
				TypeReference superInterface = superInterfaces[i];
				Integer level = (Integer) this.matchingNodes.removeKey(superInterface);
				if (level != null && searchInsideTypes)
					this.locator.reportSuperTypeReference(superInterface, enclosingElement, level.intValue());
			}
		}
	}

	// filter out element not in hierarchy scope
	boolean typeInHierarchy = type.binding == null || this.locator.typeInHierarchy(type.binding);

	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		if (this.matchingNodes.elementSize == 0) return; // reported all the matching nodes
		for (int i = 0, l = fields.length; i < l; i++) {
			FieldDeclaration field = fields[i];
			Integer level = (Integer) this.matchingNodes.removeKey(field);
			if (level != null && typeInHierarchy && searchInsideTypes)
				this.locator.reportFieldDeclaration(field, enclosingElement, level.intValue());
			reportMatching(field, enclosingElement, type, typeInHierarchy);
		}
	}

	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		if (this.matchingNodes.elementSize == 0) return; // reported all the matching nodes
		for (int i = 0, l = methods.length; i < l; i++) {
			AbstractMethodDeclaration method = methods[i];
			Integer level = (Integer) this.matchingNodes.removeKey(method);
			if (level != null && typeInHierarchy && searchInsideTypes)
				this.locator.reportMethodDeclaration(method, enclosingElement, level.intValue());
			reportMatching(method, enclosingElement, typeInHierarchy);
		}
	}

	MemberTypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0, l = memberTypes.length; i < l; i++) {
			if (this.matchingNodes.elementSize == 0) return; // reported all the matching nodes
			MemberTypeDeclaration memberType = memberTypes[i];
			Integer level = (Integer) this.matchingNodes.removeKey(memberType);
			if (level != null && typeInHierarchy && searchInsideTypes)
				this.locator.reportTypeDeclaration(memberType, enclosingElement, level.intValue());
			reportMatching(memberType, enclosingElement);
		}
	}
}
public String toString() {
	StringBuffer result = new StringBuffer();
	result.append("Exact matches:"); //$NON-NLS-1$
	Object[] keyTable = this.matchingNodes.keyTable;
	Object[] valueTable = this.matchingNodes.valueTable;
	for (int i = 0, l = keyTable.length; i < l; i++) {
		AstNode node = (AstNode) keyTable[i];
		if (node == null) continue;
		result.append("\n\t"); //$NON-NLS-1$
		result.append(valueTable[i] == EXACT_MATCH
			? "ACCURATE_MATCH: " //$NON-NLS-1$
			: "INACCURATE_MATCH: "); //$NON-NLS-1$
		node.print(0, result);
	}

	result.append("\nPotential matches:"); //$NON-NLS-1$
	Object[] nodes = this.potentialMatchingNodesSet.values;
	for (int i = 0, l = nodes.length; i < l; i++) {
		AstNode node = (AstNode) nodes[i];
		if (node == null) continue;
		result.append("\nPOTENTIAL_MATCH: "); //$NON-NLS-1$
		node.print(0, result);
	}
	return result.toString();
}
}
