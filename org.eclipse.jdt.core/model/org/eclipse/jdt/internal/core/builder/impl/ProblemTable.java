package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.internal.core.builder.IProblemDetail;
import org.eclipse.jdt.internal.core.builder.IProblemReporter;

import java.util.*;
import org.eclipse.jdt.internal.core.builder.IDevelopmentContext;

/**
 * A table of all the problems in the state, keyed by SourceEntry, and
 * a vector of IProblemDetails as the values.
 */
public class ProblemTable extends StateTables implements IProblemReporter {
	Hashtable fTable = new Hashtable(20);
	Vector fImageProblems = new Vector(11);
	/**
	 * Creates a copy of the table.
	 */
	public IProblemReporter copy() {
		try {
			ProblemTable copy = (ProblemTable) super.clone();
			copy.fTable = new Hashtable(fTable.size() * 2 + 1);
			for (Enumeration e = fTable.keys(); e.hasMoreElements();) {
				SourceEntry sEntry = (SourceEntry) e.nextElement();
				Vector v = (Vector) fTable.get(sEntry);
				copy.fTable.put(sEntry, v.clone());
			}
			return copy;
		} catch (CloneNotSupportedException e) {
			// Should not happen.
			throw new Error();
		}
	}

	/**
	 * Returns an enumeration of all problems with elements of the image,
	 * but not with the image itself.
	 */
	public Enumeration getAllProblems() {

		/* this is a very slow way of doing it.. is there a faster way? */
		Vector allProblems = new Vector();
		for (Enumeration e = fTable.elements(); e.hasMoreElements();) {
			Vector problemVector = (Vector) e.nextElement();
			for (Enumeration ee = problemVector.elements(); ee.hasMoreElements();) {
				allProblems.addElement(ee.nextElement());
			}
		}
		return allProblems.elements();
	}

	/**
	 * Returns the problems with the image itself.  Returns null if no
	 * problems exist for the image.  An example of such a problem is
	 * duplicate types in a package.
	 */
	public Enumeration getImageProblems() {
		return fImageProblems.elements();
	}

	/**
	 * Returns an enumeration of problem table keys (SourceEntry objects)
	 */
	public Enumeration getProblemKeys() {
		return fTable.keys();
	}

	/**
	 * Returns an enumeration of problems for a given source entry. 
	 */
	public Enumeration getProblems(Object entry) {
		Vector vProblems = (Vector) fTable.get(entry);
		if (vProblems == null) {
			return new Vector().elements();
		}
		return vProblems.elements();
	}

	/**
	 * Returns a vector of problems for a given source entry,
	 * or null if no problems.
	 */
	public Vector getProblemVector(Object entry) {
		return (Vector) fTable.get(entry);
	}

	/**
	 * Returns whether the given entry has any problems.
	 */
	public boolean hasProblems(Object entry) {
		return fTable.get(entry) != null;
	}

	/**
	 * Adds a problem to the problem table.  If the problem is a duplicate, it is
	 * not added and an error message is generated.
	 */
	public void putProblem(Object entry, IProblemDetail problem) {
		Vector problems = (Vector) fTable.get(entry);
		if (problems == null) {
			problems = new Vector();
			fTable.put(entry, problems);
		}
		/*	for (int i = 0; i < problems.size(); ++i) {
				if (problem.equals(problems.elementAt(i))) {
					System.err.println("DBG: Same error reported twice for " + entry + ": " + problem);
					return;
				}
			}
		*/
		problems.addElement(problem);
	}

	/**
	 * Removes all problems except syntax errors for the given source entry.
	 */
	public void removeNonSyntaxErrors(Object entry) {
		Vector problems = (Vector) fTable.get(entry);
		if (problems != null) {
			int i = 0;
			while (i < problems.size()) {
				IProblemDetail problem = (IProblemDetail) problems.elementAt(i);
				if ((problem.getSeverity() & ProblemDetailImpl.S_SYNTAX_ERROR) == 0) {
					problems.removeElementAt(i);
				} else {
					i++;
				}
			}
			if (problems.isEmpty()) {
				fTable.remove(entry);
			}
		}
	}

	/**
	 * Removes all problems for the given source entry
	 */
	public void removeProblems(Object entry) {
		fTable.remove(entry);
	}

	/**
	 * Removes all syntax errors for the given source entry.
	 */
	public void removeSyntaxErrors(Object entry) {
		Vector problems = (Vector) fTable.get(entry);
		if (problems != null) {
			int i = 0;
			while (i < problems.size()) {
				IProblemDetail problem = (IProblemDetail) problems.elementAt(i);
				if ((problem.getSeverity() & ProblemDetailImpl.S_SYNTAX_ERROR) != 0) {
					problems.removeElementAt(i);
				} else {
					i++;
				}
			}
			if (problems.isEmpty()) {
				fTable.remove(entry);
			}
		}
	}

	/**
	 * Returns the total number of problems in the table.
	 */
	int size() {
		int size = fImageProblems.size();
		for (Enumeration e = fTable.elements(); e.hasMoreElements();) {
			Vector v = (Vector) e.nextElement();
			size += v.size();
		}
		return size;
	}

	/**
	 * Returns a String that represents the value of this object.
	 * @return a string representation of the receiver
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ProblemTable(\n\t");
		buf.append("type problems: ");
		buf.append(fTable);
		buf.append("\n\timage problems: ");
		buf.append(fImageProblems);
		buf.append(")");
		return buf.toString();
	}

	/**
	 * @see org.eclipse.jdt.internal.core.builder.IProblemReporter
	 */
	public void initialize(IProject project, IDevelopmentContext dc) {
	}

}
