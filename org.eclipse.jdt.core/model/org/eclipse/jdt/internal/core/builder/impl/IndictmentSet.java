package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.lookup.ReferenceInfo;

import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.core.Util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Represents a set of indictments that reside on a node
 * in the dependency graph.  The keys are strings of the form:
 * 	"<indictment name>", values are indictment objects.
 */
public class IndictmentSet {
	/* the tables of indictments */
	protected HashtableOfObject fTypesTable;
	protected HashtableOfObject fMethodsTable;
	protected HashtableOfObject fFieldsTable;

	protected IType[] fMethodOwnersArray;

	protected Hashtable fAbstractMethodTable;
	protected Hashtable fMethodOwners;
	protected boolean fHasConstructorIndictments;

	/* whether there is an upstream hierarchy change */
	private boolean fHierarchyChange = false;
	public void add(Indictment i) {
		// allow null, since certain methods are not indicted (class initializers)
		if (i != null) {
			switch (i.getKind()) {
				case Indictment.K_HIERARCHY :
					fHierarchyChange = true;
					break;
				case Indictment.K_TYPE :
					if (fTypesTable == null)
						fTypesTable = new HashtableOfObject(11);
					fTypesTable.put(i.getKey(), i);
					break;
				case Indictment.K_METHOD :
					if (fMethodsTable == null)
						fMethodsTable = new HashtableOfObject(11);
					fMethodsTable.put(i.getKey(), i);
					IType owner = ((MethodCollaboratorIndictment) i).getOwner();
					if (fMethodOwners == null)
						fMethodOwners = new Hashtable(11);
					fMethodOwners.put(owner, owner);
					fMethodOwnersArray = null;
					if (i.getName().startsWith("<")) {
						fHasConstructorIndictments = true;
					}
					break;
				case Indictment.K_FIELD :
					if (fFieldsTable == null)
						fFieldsTable = new HashtableOfObject(11);
					fFieldsTable.put(i.getKey(), i);
					break;
				case Indictment.K_ABSTRACT_METHOD :
					if (fAbstractMethodTable == null)
						fAbstractMethodTable = new Hashtable(11);
					fAbstractMethodTable.put(i.getKey(), i);
					break;
				default :
					Assert.isTrue(false, "Unexpected kind of indictment");
					break;
			}
		}
	}

	/**
	 * Maximum conservatism.  Convict all dependents.
	 */
	public void convictAll() {
		// TBD: Should have separate flag for this.
		fHierarchyChange = true;
	}

	/**
	 * Returns the originators of all abstract method indictments.  
	 * If any of these returned types are direct supertypes of the 
	 * type on trial, it is found guilty.
	 */
	public IType[] getAbstractMethodOriginators() {
		if (fAbstractMethodTable == null) {
			return new IType[0];
		}
		int length = fAbstractMethodTable.size();
		if (length == 0) {
			return new IType[0];
		}
		IType[] toReturn = new IType[length];
		int i = 0;
		for (Enumeration e = fAbstractMethodTable.elements();
			e.hasMoreElements();
			i++) {
			toReturn[i] =
				((AbstractMethodCollaboratorIndictment) e.nextElement()).getType();
		}

		return toReturn;
	}

	/**
	 * Returns the types for which method indictments were issued.
	 */
	public IType[] getMethodIndictmentOwners() {
		if (fMethodOwnersArray == null) {
			if (fMethodsTable == null) {
				fMethodOwnersArray = new IType[0];
			} else {
				fMethodOwnersArray = new IType[fMethodOwners.size()];
				int count = 0;
				for (Enumeration e = fMethodOwners.elements(); e.hasMoreElements();) {
					fMethodOwnersArray[count++] = (IType) e.nextElement();
				}
			}
		}
		return fMethodOwnersArray;
	}

	/**
	 * Returns true if there are any constructor indictments, false otherwise.
	 */
	public boolean hasConstructorIndictments() {
		return fHasConstructorIndictments;
	}

	/**
	 * Returns true if there is a type hierarchy indictment, false otherwise.
	 */
	public boolean hasHierarchyIndictment() {
		return fHierarchyChange;
	}

	/**
	 * Returns true if the indictment set has no indictments, false otherwise.
	 */
	public boolean isEmpty() {
		return fMethodsTable == null
			&& fFieldsTable == null
			&& fTypesTable == null
			&& fAbstractMethodTable == null
			&& !fHierarchyChange;
	}

	/**
	 * Resets the contents of the indictment set.  Allows re-use of objects
	 * and saves on garbage.
	 */
	public void reset() {
		fMethodsTable = null;
		fFieldsTable = null;
		fTypesTable = null;
		fAbstractMethodTable = null;
		fHierarchyChange = false;
	}

	/**
	 * Returns a string representation of the instance.
	 */
	public String toString() {
		return "IndictmentSet("
			+ "\n  hierarchyChange: "
			+ fHierarchyChange
			+ "\n  types:\n"
			+ fTypesTable
			+ "\n  interfaces:\n"
			+ fAbstractMethodTable
			+ "\n  methods:\n"
			+ fMethodsTable
			+ "\n  fields:\n"
			+ fFieldsTable
			+ "\n)";

	}

	/**
	 * Tries all the evidence in the given set of references against the indictments.
	 * Returns true if any evidence matches an indictment, and false otherwise.
	 */
	public boolean tryAllEvidence(ReferenceInfo references) {
		char[][] names = references.getNames();
		byte[] kinds = references.getKinds();
		int numRefs = names.length;

		/* try all references */
		for (int i = 0; i < numRefs; i++) {
			switch (kinds[i]) {
				case ReferenceInfo.REFTYPE_unknown :
				case ReferenceInfo.REFTYPE_class :
				case ReferenceInfo.REFTYPE_type :
					/* try type indictments */
					if (fTypesTable != null && fTypesTable.containsKey(names[i])) {
						return true;
					}
					if (kinds[i] != ReferenceInfo.REFTYPE_unknown)
						break;
				case ReferenceInfo.REFTYPE_var :
					/* try field indictments */
					if (fFieldsTable != null && fFieldsTable.containsKey(names[i])) {
						return true;
					}
					if (kinds[i] != ReferenceInfo.REFTYPE_unknown)
						break;
				case ReferenceInfo.REFTYPE_call :
					/* try method indictments */
					if (fMethodsTable != null && fMethodsTable.containsKey(names[i])) {
						return true;
					}
					if (kinds[i] != ReferenceInfo.REFTYPE_unknown)
						break;
			}
		}

		return false;
	}

	/**
	 * Tries an ambiguous name against the set of available indictments.  Returns
	 * true if there is a match (guilty), and false if there is no match (not
	 * guilty).
	 */
	public boolean tryAmbiguousEvidence(char[] name) {
		if (fFieldsTable == null && fTypesTable == null) {
			return false;
		}

		// Try all segments of name
		int i = 0;
		int j = CharOperation.indexOf('.', name);
		while (j >= 0) {
			char[] segment = CharOperation.subarray(name, i, j);
			if (tryFieldEvidence(segment) || tryTypeEvidence(segment)) {
				return true;
			}
			i = j + 1;
			j = CharOperation.indexOf('.', name, i);
		}
		char[] segment = CharOperation.subarray(name, i, name.length - 1);
		return tryFieldEvidence(segment) || tryTypeEvidence(segment);
	}

	/**
	 * Tries a field name against the set of available indictments.  Returns
	 * true if there is a match (guilty), and false if there is no match (not
	 * guilty).
	 */
	public boolean tryFieldEvidence(char[] name) {
		return fFieldsTable != null && fFieldsTable.containsKey(name);
	}

	/**
	 * Tries a method declaration against the set of available indictments.  
	 * Returns true if there is a match (guilty), and false if there is no match 
	 * (not guilty).
	 */
	public boolean tryMethodDeclaration(IBinaryMethod method) {
		if (fMethodsTable != null) {
			char[] key =
				Indictment.getMethodIndictmentKey(
					method.getSelector(),
					Util.getParameterCount(method.getMethodDescriptor()));
			if (fMethodsTable.get(key) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tries a method name against the set of available indictments.  Returns
	 * true if there is a match (guilty), and false if there is no match (not
	 * guilty).
	 * @parm name String of form: 
	 *		constructor: "<" + NameOfType + ">/" + NumberOfParameters
	 *		method: NameOfMethod + "/" + NumberOfParameters
	 */
	public boolean tryMethodEvidence(char[] name) {
		return fMethodsTable != null && fMethodsTable.containsKey(name);
	}

	/**
	 * Tries a type name against the set of available indictments.  Returns
	 * true if there is a match (guilty), and false if there is no match (not
	 * guilty).
	 */
	public boolean tryTypeEvidence(char[] name) {
		if (fTypesTable != null) {
			if (fTypesTable.containsKey(name)) {
				return true;
			}

			/* it may be a qualified name */
			int lastDot = CharOperation.lastIndexOf('.', name);
			if (lastDot != -1) {
				char[] key = CharOperation.subarray(name, lastDot + 1, name.length - 1);
				if (fTypesTable.containsKey(key)) {
					return true;
				}
			}
		}
		return false;
	}

}
