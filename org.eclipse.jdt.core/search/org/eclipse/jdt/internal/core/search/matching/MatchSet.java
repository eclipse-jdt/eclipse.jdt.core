package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;

import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.search.matching.*;

import java.util.*;

/**
 * A set of matches and potential matches.
 */
public class MatchSet {
	
	private MatchLocator locator;
	int matchContainer;
	boolean cuHasBeenResolved = false;

	/**
	 * Set of matching ast nodes that don't need to be resolved.
	 */
	private Hashtable matchingNodes = new Hashtable(5);

	/**
	 * Set of potential matching ast nodes. They need to be resolved
	 * to determine if they really match the search pattern.
	 */
	private Hashtable potentialMatchingNodes = new Hashtable(5);
	
/**
 * An ast visitor that visits local type declarations.
 */
public class LocalDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	IJavaElement enclosingElement;
	public boolean visit(
			AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
			BlockScope scope) {
		try {
			reportMatching(anonymousTypeDeclaration, enclosingElement);
		} catch (CoreException e) {
			throw new WrappedCoreException(e);
		}
		return false; // don't visit members as this was done during reportMatching(...)
	}
	public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
		try {
			reportMatching(typeDeclaration, enclosingElement);
			return false; // don't visit members as this was done during reportMatching(...)
		} catch (CoreException e) {
			throw new WrappedCoreException(e);
		}
	}
	public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
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

public MatchSet(MatchLocator locator) {
	this.locator = locator;
	this.matchContainer = locator.pattern.matchContainer();
}
public void addPossibleMatch(AstNode node) {
	this.potentialMatchingNodes.put(node, new Integer(SearchPattern.POSSIBLE_MATCH));
}
public void addTrustedMatch(AstNode node) {
	this.matchingNodes.put(node, new Integer(SearchPattern.ACCURATE_MATCH));
}
public void checkMatching(AstNode node) {
	int matchLevel = this.locator.pattern.matchLevel(node, false);
	switch (matchLevel) {
		case SearchPattern.POSSIBLE_MATCH:
			this.addPossibleMatch(node);
			break;
		case SearchPattern.ACCURATE_MATCH:
			this.addTrustedMatch(node);
	}
}
/**
 * Returns the matching nodes that are in the given range.
 */
private AstNode[] matchingNodes(int start, int end) {
	return this.nodesInRange(start, end, this.matchingNodes);
}
public boolean needsResolve() {
	return this.potentialMatchingNodes.size() > 0;
}
/**
 * Returns the matching nodes that are in the given range in the source order.
 */
private AstNode[] nodesInRange(int start, int end, Hashtable set) {
	// collect nodes in the given range
	Vector nodes = new Vector();
	for (Enumeration keys = set.keys(); keys.hasMoreElements();) {
		AstNode node = (AstNode)keys.nextElement();
		if (start <= node.sourceStart && node.sourceEnd <= end) {
			nodes.addElement(node);
		}
	}
	AstNode[] result = new AstNode[nodes.size()];
	nodes.copyInto(result);

	// sort nodes by source starts
	Util.Comparer comparer = new Util.Comparer() {
		public int compare(Object o1, Object o2) {
			AstNode node1 = (AstNode) o1;
			AstNode node2 = (AstNode) o2;
			return node1.sourceStart - node2.sourceStart;
		}
	};
	Util.sort(result, comparer);
		
	return result;
}
/**
 * Returns the potential matching nodes that are in the given range.
 */
private AstNode[] potentialMatchingNodes(int start, int end) {
	return this.nodesInRange(start, end, this.potentialMatchingNodes);
}
/**
 * Visit the given method declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the method declaration has already been checked.
 */
private void reportMatching(AbstractMethodDeclaration method, IJavaElement parent) throws CoreException {
	// references in this method
	AstNode[] nodes = this.matchingNodes(method.declarationSourceStart, method.declarationSourceEnd);
	for (int i = 0; i < nodes.length; i++) {
		AstNode node = nodes[i];
		Integer level = (Integer)this.matchingNodes.get(node);
		if ((this.matchContainer & SearchPattern.METHOD) != 0) {
			this.locator.reportReference(
				node, 
				method, 
				parent, 
				level.intValue() == SearchPattern.ACCURATE_MATCH ?
					IJavaSearchResultCollector.EXACT_MATCH :
					IJavaSearchResultCollector.POTENTIAL_MATCH);
			this.matchingNodes.remove(node);
		}
	}
	if ((method.bits & AstNode.HasLocalTypeMASK) != 0) {
		LocalDeclarationVisitor localDeclarationVisitor = new LocalDeclarationVisitor();
		localDeclarationVisitor.enclosingElement = 
			(parent instanceof IType) ?
				this.locator.createMethodHandle(method, (IType)parent) :
				parent;
		try {
			method.traverse(localDeclarationVisitor, (ClassScope)null);
		} catch (WrappedCoreException e) {
			throw e.coreException;
		}
	}
	if (this.potentialMatchingNodes(method.declarationSourceStart, method.declarationSourceEnd).length == 0) {
		// no need to resolve the statements in the method
		method.statements = null;
	}
}
/**
 * Visit the given parse tree and report the nodes that match exactly the
 * search pattern.
 */
public void reportMatching(CompilationUnitDeclaration unit) throws CoreException {
	if (this.cuHasBeenResolved) {
		// move the potential matching nodes that exactly match the search pattern to the matching nodes set
		for (Enumeration potentialMatches = this.potentialMatchingNodes.keys(); potentialMatches.hasMoreElements();) {
			AstNode node = (AstNode) potentialMatches.nextElement();
			int level = this.locator.pattern.matchLevel(node, true);
			if (level == SearchPattern.ACCURATE_MATCH || level == SearchPattern.INACCURATE_MATCH) {
				this.matchingNodes.put(node, new Integer(level));
			}
		}
		this.potentialMatchingNodes = new Hashtable();
	}
	
	// package declaration
	ImportReference pkg = unit.currentPackage;
	Integer level;
	if (pkg != null && (level = (Integer)this.matchingNodes.remove(pkg)) != null) {
		if ((this.matchContainer & SearchPattern.COMPILATION_UNIT) != 0) {
			this.locator.reportPackageDeclaration(pkg);
		}
	}

	// import declarations
	ImportReference[] imports = unit.imports;
	if (imports != null) {
		for (int i = 0; i < imports.length; i++) {
			ImportReference importRef = imports[i];
			if ((level = (Integer)this.matchingNodes.remove(importRef)) != null) {
				if ((this.matchContainer & SearchPattern.COMPILATION_UNIT) != 0) {
					this.locator.reportImport(
						importRef, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
		}
	}

	// types
	TypeDeclaration[] types = unit.types;
	if (types != null) {
		for (int i = 0; i < types.length; i++) {
			TypeDeclaration type = types[i];
			if ((level = (Integer)this.matchingNodes.remove(type)) != null) {
				if ((this.matchContainer & SearchPattern.COMPILATION_UNIT) != 0) {
					this.locator.reportTypeDeclaration(
						type, 
						null, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(type, null);
		}
	}
}
/**
 * Visit the given field declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the field declaration has already been checked.
 */
private void reportMatching(FieldDeclaration field, IJavaElement parent, TypeDeclaration type) throws CoreException {
	AstNode[] nodes = this.matchingNodes(field.declarationSourceStart, field.declarationSourceEnd);
	for (int i = 0; i < nodes.length; i++) {
		AstNode node = nodes[i];
		Integer level = (Integer)this.matchingNodes.get(node);
		if ((this.matchContainer & SearchPattern.FIELD) != 0) {
			this.locator.reportReference(
				node, 
				type, 
				field, 
				parent, 
				level.intValue() == SearchPattern.ACCURATE_MATCH ?
					IJavaSearchResultCollector.EXACT_MATCH :
					IJavaSearchResultCollector.POTENTIAL_MATCH);
			this.matchingNodes.remove(node);
		}
	}
	if ((field.bits & AstNode.HasLocalTypeMASK) != 0) {
		LocalDeclarationVisitor localDeclarationVisitor = new LocalDeclarationVisitor();
		localDeclarationVisitor.enclosingElement = 
			(parent instanceof IType) ?
				(field.isField() ?
					(IJavaElement)this.locator.createFieldHandle(field, (IType)parent) :
					(IJavaElement)this.locator.createInitializerHandle(type, field, (IType)parent)) :
				parent;
		try {
			field.traverse(localDeclarationVisitor, (BlockScope)null);
		} catch (WrappedCoreException e) {
			throw e.coreException;
		}
	}
}
/**
 * Visit the given type declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the type declaration has already been checked.
 */
public void reportMatching(TypeDeclaration type, IJavaElement parent) throws CoreException {
	IJavaElement enclosingElement;
	if (parent == null) {
		enclosingElement = this.locator.createTypeHandle(type.name);
	} else if (parent instanceof IType) {
		enclosingElement = this.locator.createTypeHandle((IType)parent, type.name);
		if (enclosingElement == null) return;
	} else {
		enclosingElement = parent;
	}
	Integer level;
	
	// fields
	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		for (int i = 0; i < fields.length; i++) {
			FieldDeclaration field = fields[i];
			if ((level = (Integer)this.matchingNodes.remove(field)) != null) {
				if ((this.matchContainer & SearchPattern.CLASS) != 0) {
					this.locator.reportFieldDeclaration(
						field, 
						enclosingElement, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(field, enclosingElement, type);
		}
	}

	// methods
	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		for (int i = 0; i < methods.length; i++) {
			AbstractMethodDeclaration method = methods[i];
			if ((level = (Integer)this.matchingNodes.remove(method)) != null) {
				if ((this.matchContainer & SearchPattern.CLASS) != 0) {
					this.locator.reportMethodDeclaration(
						method, 
						enclosingElement, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(method, enclosingElement);
		}
	}

	// member types
	MemberTypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0; i < memberTypes.length; i++) {
			MemberTypeDeclaration memberType = memberTypes[i];
			if ((level = (Integer)this.matchingNodes.remove(memberType)) != null) {
				if ((this.matchContainer & SearchPattern.CLASS) != 0) {
					this.locator.reportTypeDeclaration(
						memberType, 
						enclosingElement, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(memberType, enclosingElement);
		}
	}

	// super types
	if (type instanceof AnonymousLocalTypeDeclaration) {
		TypeReference superType = ((AnonymousLocalTypeDeclaration)type).allocation.type;
		if (superType != null && (level = (Integer)this.matchingNodes.remove(superType)) != null) {
			if ((this.matchContainer & SearchPattern.CLASS) != 0) {
				this.locator.reportSuperTypeReference(
					superType, 
					enclosingElement, 
					level.intValue() == SearchPattern.ACCURATE_MATCH ?
						IJavaSearchResultCollector.EXACT_MATCH :
						IJavaSearchResultCollector.POTENTIAL_MATCH);
			}
		}
	} else {
		TypeReference superClass = type.superclass;
		if (superClass != null && (level = (Integer)this.matchingNodes.remove(superClass)) != null) {
			if ((this.matchContainer & SearchPattern.CLASS) != 0) {
				this.locator.reportSuperTypeReference(
					superClass, 
					enclosingElement, 
					level.intValue() == SearchPattern.ACCURATE_MATCH ?
						IJavaSearchResultCollector.EXACT_MATCH :
						IJavaSearchResultCollector.POTENTIAL_MATCH);
			}
		}
		TypeReference[] superInterfaces = type.superInterfaces;
		if (superInterfaces != null) {
			for (int i = 0; i < superInterfaces.length; i++) {
				TypeReference superInterface = superInterfaces[i];
				if ((level = (Integer)this.matchingNodes.get(superInterface)) != null) {
					if ((this.matchContainer & SearchPattern.CLASS) != 0) {
						this.locator.reportSuperTypeReference(
							superInterface, 
							enclosingElement, 
							level.intValue() == SearchPattern.ACCURATE_MATCH ?
								IJavaSearchResultCollector.EXACT_MATCH :
								IJavaSearchResultCollector.POTENTIAL_MATCH);
					}
				}
			}
		}
	}
}
public String toString() {
	StringBuffer result = new StringBuffer();
	result.append("Exact matches:"); //$NON-NLS-1$
	for (Enumeration enum = this.matchingNodes.keys(); enum.hasMoreElements();) {
		result.append("\n"); //$NON-NLS-1$
		AstNode node = (AstNode)enum.nextElement();
		Object value = this.matchingNodes.get(node);
		if (value instanceof Integer) {
			result.append('\t');
			int accuracy = ((Integer)value).intValue();
			switch (accuracy) {
				case SearchPattern.IMPOSSIBLE_MATCH:
					result.append("IMPOSSIBLE_MATCH: "); //$NON-NLS-1$
					break;
				case SearchPattern.POSSIBLE_MATCH:
					result.append("POSSIBLE_MATCH: "); //$NON-NLS-1$
					break;
				case SearchPattern.INACCURATE_MATCH:
					result.append("INACCURATE_MATCH: "); //$NON-NLS-1$
					break;
				case SearchPattern.ACCURATE_MATCH:
					result.append("ACCURATE_MATCH: "); //$NON-NLS-1$
					break;
			}
		} 
		result.append(node.toString(0));
	}
	result.append("\nPotential matches:"); //$NON-NLS-1$
	for (Enumeration enum = this.potentialMatchingNodes.keys(); enum.hasMoreElements();) {
		result.append("\n"); //$NON-NLS-1$
		AstNode node = (AstNode)enum.nextElement();
		Object value = this.potentialMatchingNodes.get(node);
		if (value instanceof Integer) {
			result.append("\t"); //$NON-NLS-1$
			int accuracy = ((Integer)value).intValue();
			switch (accuracy) {
				case SearchPattern.IMPOSSIBLE_MATCH:
					result.append("IMPOSSIBLE_MATCH: "); //$NON-NLS-1$
					break;
				case SearchPattern.POSSIBLE_MATCH:
					result.append("POSSIBLE_MATCH: "); //$NON-NLS-1$
					break;
				case SearchPattern.INACCURATE_MATCH:
					result.append("INACCURATE_MATCH: "); //$NON-NLS-1$
					break;
				case SearchPattern.ACCURATE_MATCH:
					result.append("ACCURATE_MATCH: "); //$NON-NLS-1$
					break;
			}
		}
		result.append(node.toString(0));
	}
	return result.toString();
}
}
