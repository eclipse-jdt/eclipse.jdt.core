package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.search.matching.*;
import org.eclipse.jdt.internal.core.Util;

import java.util.*;

/**
 * A set of matches and potential matches.
 */
public class MatchSet {
	
	private static final char[][] EMPTY_CHAR_CHAR = new char[0][];
	
	private MatchLocator locator;
	int matchContainer;
	boolean cuHasBeenResolved = false;
	int accuracy = IJavaSearchResultCollector.POTENTIAL_MATCH;

	/**
	 * Set of matching ast nodes that don't need to be resolved.
	 */
	private Hashtable matchingNodes = new Hashtable(5);

	/**
	 * Set of potential matching ast nodes. They need to be resolved
	 * to determine if they really match the search pattern.
	 */
	private Hashtable potentialMatchingNodes = new Hashtable(5);

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
private void reportMatching(AbstractMethodDeclaration method, char[][] definingTypeNames) throws CoreException {
	// references in this method
	AstNode[] nodes = this.matchingNodes(method.declarationSourceStart, method.declarationSourceEnd);
	for (int i = 0; i < nodes.length; i++) {
		AstNode node = nodes[i];
		Integer level = (Integer)this.matchingNodes.remove(node);
		if ((this.matchContainer & SearchPattern.METHOD) != 0) {
			this.locator.reportReference(
				node, 
				method, 
				definingTypeNames, 
				level.intValue() == SearchPattern.ACCURATE_MATCH ?
					IJavaSearchResultCollector.EXACT_MATCH :
					IJavaSearchResultCollector.POTENTIAL_MATCH);
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
						new char[][] {type.name}, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(type, EMPTY_CHAR_CHAR);
		}
	}
}
/**
 * Visit the given field declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the field declaration has already been checked.
 */
private void reportMatching(FieldDeclaration field, char[][] definingTypeNames, TypeDeclaration type) throws CoreException {
	AstNode[] nodes = this.matchingNodes(field.declarationSourceStart, field.declarationSourceEnd);
	for (int i = 0; i < nodes.length; i++) {
		AstNode node = nodes[i];
		Integer level = (Integer)this.matchingNodes.remove(node);
		if ((this.matchContainer & SearchPattern.FIELD) != 0) {
			this.locator.reportReference(
				node, 
				type, 
				field, 
				definingTypeNames, 
				level.intValue() == SearchPattern.ACCURATE_MATCH ?
					IJavaSearchResultCollector.EXACT_MATCH :
					IJavaSearchResultCollector.POTENTIAL_MATCH);
		}
	}
}
/**
 * Visit the given type declaration and report the nodes that match exactly the
 * search pattern (ie. the ones in the matching nodes set)
 * Note that the type declaration has already been checked.
 */
private void reportMatching(TypeDeclaration type, char[][] enclosingTypeNames) throws CoreException {
	char[][] definingTypeNames = CharOperation.arrayConcat(enclosingTypeNames, type.name);
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
						definingTypeNames, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(field, definingTypeNames, type);
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
						definingTypeNames, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(method, definingTypeNames);
		}
	}

	// member types
	MemberTypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0; i < memberTypes.length; i++) {
			MemberTypeDeclaration memberType = memberTypes[i];
			if ((level = (Integer)this.matchingNodes.remove(memberType)) != null) {
				if ((this.matchContainer & SearchPattern.CLASS) != 0) {
					char[][] memberTypeNames = CharOperation.arrayConcat(definingTypeNames, memberType.name);
					this.locator.reportTypeDeclaration(
						memberType, 
						memberTypeNames, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
			this.reportMatching(memberType, definingTypeNames);
		}
	}

	// super types
	TypeReference superClass = type.superclass;
	if (superClass != null && (level = (Integer)this.matchingNodes.remove(superClass)) != null) {
		if ((this.matchContainer & SearchPattern.CLASS) != 0) {
			this.locator.reportSuperTypeReference(
				superClass, 
				definingTypeNames, 
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
						definingTypeNames, 
						level.intValue() == SearchPattern.ACCURATE_MATCH ?
							IJavaSearchResultCollector.EXACT_MATCH :
							IJavaSearchResultCollector.POTENTIAL_MATCH);
				}
			}
		}
	}

}
public String toString() {
	StringBuffer result = new StringBuffer();
	result.append("Exact matches:"/*nonNLS*/);
	for (Enumeration enum = this.matchingNodes.keys(); enum.hasMoreElements();) {
		result.append("\n"/*nonNLS*/);
		AstNode node = (AstNode)enum.nextElement();
		Object value = this.matchingNodes.get(node);
		if (value instanceof Integer) {
			result.append("\t");
			int accuracy = ((Integer)value).intValue();
			switch (accuracy) {
				case SearchPattern.IMPOSSIBLE_MATCH:
					result.append("IMPOSSIBLE_MATCH: ");
					break;
				case SearchPattern.POSSIBLE_MATCH:
					result.append("POSSIBLE_MATCH: ");
					break;
				case SearchPattern.INACCURATE_MATCH:
					result.append("INACCURATE_MATCH: ");
					break;
				case SearchPattern.ACCURATE_MATCH:
					result.append("ACCURATE_MATCH: ");
					break;
			}
		} 
		result.append(node.toString(0));
	}
	result.append("\nPotential matches:"/*nonNLS*/);
	for (Enumeration enum = this.potentialMatchingNodes.keys(); enum.hasMoreElements();) {
		result.append("\n"/*nonNLS*/);
		AstNode node = (AstNode)enum.nextElement();
		Object value = this.potentialMatchingNodes.get(node);
		if (value instanceof Integer) {
			result.append("\t");
			int accuracy = ((Integer)value).intValue();
			switch (accuracy) {
				case SearchPattern.IMPOSSIBLE_MATCH:
					result.append("IMPOSSIBLE_MATCH: ");
					break;
				case SearchPattern.POSSIBLE_MATCH:
					result.append("POSSIBLE_MATCH: ");
					break;
				case SearchPattern.INACCURATE_MATCH:
					result.append("INACCURATE_MATCH: ");
					break;
				case SearchPattern.ACCURATE_MATCH:
					result.append("ACCURATE_MATCH: ");
					break;
			}
		}
		result.append(node.toString(0));
	}
	return result.toString();
}
}
