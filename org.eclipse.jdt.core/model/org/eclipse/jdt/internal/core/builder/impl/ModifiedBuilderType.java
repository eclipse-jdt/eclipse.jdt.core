package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.compiler.util.*;

/**
 * A type that exists in both old and new states.
 * This type definitly has been or will be
 * compiled during this incremental build.  These
 * types must hang onto their old structure because
 * the builder will overwrite it with the new structure
 * after compilation.
 */
public class ModifiedBuilderType extends BuilderType {
	/**
	 * The tsEntry in the new state
	 */
	protected TypeStructureEntry fNewTSEntry;

	/**
	 * The tsEntry for this type in the old state
	 */
	protected TypeStructureEntry fOldTSEntry;

	/**
	 * The structure of this type in the old state
	 */
	protected IBinaryType fOldStructure;
	/**
	 * Creates a new ModifiedBuilderType.  The new tsEntry is not
	 * yet known because the type hasn't yet been compiled.
	 */
	public ModifiedBuilderType(
		IncrementalImageBuilder builder,
		TypeStructureEntry oldEntry,
		IBinaryType oldBinary) {
		super(builder, false, false);
		fOldTSEntry = oldEntry;
		fOldStructure = oldBinary;
	}

	/**
	 * Adds the indictments for the descriptor's type, methods,
	 * and fields.  Usually used when a type has been added or removed.
	 */
	public void computeAllIndictments(IndictmentSet set) {

		/* indictment for the type */
		IType type = fOldTSEntry.getType();
		set.add(Indictment.createTypeIndictment(fOldStructure));

		/* indictments for all fields */
		IBinaryField[] fields = fOldStructure.getFields();
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				set.add(Indictment.createFieldIndictment(fields[i]));
			}
		}

		/* indictments for all methods */
		IBinaryMethod[] methods = fOldStructure.getMethods();
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				set.add(Indictment.createMethodIndictment(type, fOldStructure, methods[i]));
			}
		}
	}

	/**
	 * Computes the indictments for the fields of this type
	 */
	public void computeFieldIndictments(
		IndictmentSet indictments,
		IBinaryType newType,
		IType typeHandle) {

		/* create a set of all old fields */
		HashtableOfObject oldFieldsTable = new HashtableOfObject(11);
		IBinaryField[] oldFields = fOldStructure.getFields();
		if (oldFields != null) {
			for (int i = 0; i < oldFields.length; i++) {
				oldFieldsTable.put(oldFields[i].getName(), oldFields[i]);
			}
		}

		/* check if each new field was in old field list */
		IBinaryField[] newFields = newType.getFields();
		if (newFields != null) {
			for (int i = 0; i < newFields.length; i++) {
				IBinaryField newField = newFields[i];
				IBinaryField oldField = (IBinaryField) oldFieldsTable.get(newField.getName());
				if (oldField == null) {
					/* it's a new field -- create a indictment */
					indictments.add(Indictment.createFieldIndictment(newField));
				} else {
					/* if it has it changed, issue an indictment */
					oldFieldsTable.put(oldField.getName(), null);
					// TBD: there is no remove on HashtableOfObject
					if (!BinaryStructure.compare(oldField, newField)) {
						indictments.add(Indictment.createFieldIndictment(oldField));
					}
				}
			}
		}

		/* remaining old fields have been deleted -- create indictments */
		Object[] remaining = oldFieldsTable.valueTable;
		for (int i = remaining.length; i-- > 0;) {
			if (remaining[i] != null) {
				IBinaryField oldField = (IBinaryField) remaining[i];
				indictments.add(Indictment.createFieldIndictment(oldField));
			}
		}
	}

	/**
	 * Computes the indictments for this type
	 */
	public void computeIndictments(IndictmentSet indictments) {
		if (getOldBinaryType() == null) {
			// Don't know old structure.  Must convict all dependents.
			// Needed for 1FVQGL1: ITPJCORE:WINNT - SEVERE - Error saving java file
			indictments.convictAll();
			return;
		}

		if (fNewTSEntry == null) {
			/* a new type could not be generated */
			computeAllIndictments(indictments);
			return;
		}

		IBinaryType newType = getNewState().getBinaryType(fNewTSEntry);
		IType typeHandle = (IType) fOldTSEntry.getType();

		/* see if there's a change to supertype hierarchy */
		boolean hierarchyChange = detectHierarchyChange();

		/* add the hierarchy indictment if there were any changes */
		if (hierarchyChange) {
			indictments.add(Indictment.createHierarchyIndictment(newType));
		}

		/* if type modifiers (including deprecated flag) have changed */
		if (fOldStructure.getModifiers() != newType.getModifiers()) {
			/* issue a type collaborator indictment */
			indictments.add(Indictment.createTypeIndictment(newType));
		}

		/* compute indictments for members */
		computeMethodIndictments(indictments, newType, typeHandle);
		computeFieldIndictments(indictments, newType, typeHandle);
	}

	/**
	 * Computes the method indictments for this type
	 */
	public void computeMethodIndictments(
		IndictmentSet indictments,
		IBinaryType newType,
		IType typeHandle) {

		boolean issueAbstractMethodIndictment = false;
		if ((fOldStructure.getModifiers() & IConstants.AccAbstract)
			!= (newType.getModifiers() & IConstants.AccAbstract)) {
			issueAbstractMethodIndictment = true;
		}

		/* create a set of all old methods */
		HashtableOfObject oldMethodsTable = new HashtableOfObject(21);
		IBinaryMethod[] oldMethods = fOldStructure.getMethods();
		if (oldMethods != null) {
			for (int i = 0; i < oldMethods.length; i++) {
				IBinaryMethod oldMethod = oldMethods[i];
				char[] sig =
					CharOperation.concat(oldMethod.getSelector(), oldMethod.getMethodDescriptor());
				oldMethodsTable.put(sig, oldMethod);
			}
		}

		/* check if each new method was in old method list */
		IBinaryMethod[] newMethods = newType.getMethods();
		if (newMethods != null) {
			for (int i = 0; i < newMethods.length; i++) {
				IBinaryMethod newMethod = (IBinaryMethod) newMethods[i];
				char[] sig =
					CharOperation.concat(newMethod.getSelector(), newMethod.getMethodDescriptor());
				IBinaryMethod oldMethod = (IBinaryMethod) oldMethodsTable.get(sig);
				if (oldMethod == null) {
					/* it's a new method -- issue an indictment */
					indictments.add(
						Indictment.createMethodIndictment(typeHandle, newType, newMethod));

					/* if the new method is abstract, issue an abstract method indictment too */
					if ((newMethod.getModifiers() & IConstants.AccAbstract) != 0) {
						issueAbstractMethodIndictment = true;
					}
				} else {
					/* if it has it changed, issue an indictment */
					oldMethodsTable.put(sig, null);
					// TBD: there is no remove on HashtableOfObject
					if (!BinaryStructure.compare(oldMethod, newMethod)) {
						indictments.add(
							Indictment.createMethodIndictment(typeHandle, fOldStructure, oldMethod));
					}
					/* if an existing method changes its abstract state, issue an abstract method indictment too */
					if ((oldMethod.getModifiers() & IConstants.AccAbstract)
						!= (newMethod.getModifiers() & IConstants.AccAbstract)) {
						issueAbstractMethodIndictment = true;
					}
				}
			}
		}

		/* remaining old methods have been deleted -- issue indictments */
		Object[] remaining = oldMethodsTable.valueTable;
		for (int i = remaining.length; i-- > 0;) {
			if (remaining[i] != null) {
				IBinaryMethod oldMethod = (IBinaryMethod) remaining[i];
				indictments.add(
					Indictment.createMethodIndictment(typeHandle, fOldStructure, oldMethod));

				/* if old method is abstract, issue an abstract method indictment too */
				if ((oldMethod.getModifiers() & IConstants.AccAbstract) != 0) {
					issueAbstractMethodIndictment = true;
				}
			}
		}

		/* issue abstract method indictment now, if need be */
		if (issueAbstractMethodIndictment) {
			indictments.add(Indictment.createAbstractMethodIndictment(typeHandle));
		}
	}

	/**
	 * Returns the tsEntry in the new state
	 */
	public TypeStructureEntry getNewTypeStructureEntry() {
		if (fNewTSEntry == null) {
			if (fOldTSEntry != null) {
				fNewTSEntry =
					fBuilder.fNewState.getTypeStructureEntry(fOldTSEntry.getType(), false);
			}
		}
		return fNewTSEntry;
	}

	/**
	 * Returns the binary type in the old state
	 */
	public IBinaryType getOldBinaryType() {
		return fOldStructure;
	}

	/**
	 * Returns the old tsEntry
	 */
	public TypeStructureEntry getOldTypeStructureEntry() {
		return fOldTSEntry;
	}

	/**
	 * Sets the tsEntry in the new state
	 */
	public void setNewTypeStructureEntry(TypeStructureEntry newEntry) {
		fNewTSEntry = newEntry;
	}

	/**
	 * For debugging only
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ModifiedBuilderType(");
		return buf.append(fOldTSEntry.getType().getName()).append(")").toString();
	}

}
