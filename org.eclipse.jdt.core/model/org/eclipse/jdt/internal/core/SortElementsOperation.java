/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Comparator;
import java.util.Locale;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.builder.ProblemFactory;

/**
 * This operation is used to sort elements in a compilation unit according to
 * certain criteria.
 * 
 * @since 2.1
 */
public class SortElementsOperation extends JavaModelOperation {
	
	Comparator comparator;
	boolean hasChanged;
	int[] positions;
	
	/**
	 * Constructor for SortElementsOperation.
	 * @param elements
	 */
	public SortElementsOperation(IJavaElement[] elements, int[] positions, Comparator comparator) {
		super(elements);
		this.comparator = comparator;
		this.positions = positions;
	}

	/**
	 * Returns the amount of work for the main task of this operation for
	 * progress reporting.
	 */
	protected int getMainAmountOfWork(){
		return fElementsToProcess.length;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.core.JavaModelOperation#executeOperation()
	 */
	protected void executeOperation() throws JavaModelException {
		try {
			beginTask(Util.bind("operation.sortelements"), getMainAmountOfWork()); //$NON-NLS-1$
			for (int i = 0, max = fElementsToProcess.length; i < max; i++) {
				ICompilationUnit unit = ((JavaElement) fElementsToProcess[i]).getCompilationUnit();
				if (unit == null) {
					return;
				}
				IBuffer buffer = unit.getBuffer();
				if (buffer  == null) { 
					return;
				}
				char[] bufferContents = buffer.getCharacters();
				processElement(unit,bufferContents);
				if (this.hasChanged) {
					unit.save(null, false);
					boolean isWorkingCopy = unit.isWorkingCopy();
					worked(1);
					 // if unit is working copy, then save will have already fired the delta
					if (!isWorkingCopy
						&& !Util.isExcluded(unit)
						&& unit.getParent().exists()) {
							JavaElementDelta delta = newJavaElementDelta();
							delta.changed(unit, IJavaElementDelta.F_CHILDREN);
							addDelta(delta);
					}
				}
			}
		} finally {
			done();
		}
	}

	/**
	 * Method processElement.
	 * @param unit
	 * @param bufferContents
	 */
	private void processElement(ICompilationUnit unit, char[] source) throws JavaModelException {
		subTask("Sort " + unit.getElementName()); //$NON-NLS-1$
		this.hasChanged = false;
		SortElementBuilder builder = new SortElementBuilder(source, comparator);
		SourceElementParser parser = new SourceElementParser(builder,
			ProblemFactory.getProblemFactory(Locale.getDefault()), new CompilerOptions(JavaCore.getOptions()), true);
		
		IPackageFragment packageFragment = (IPackageFragment)unit.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		char[][] expectedPackageName = null;
		if (packageFragment != null){
			expectedPackageName = CharOperation.splitOn('.', packageFragment.getElementName().toCharArray());
		}
		parser.parseCompilationUnit(
			new BasicCompilationUnit(
				source,
				expectedPackageName,
				unit.getElementName(),
				unit.getJavaProject().getOption(JavaCore.CORE_ENCODING, true)),
			false);
		String result = builder.getSource();
		this.hasChanged = !CharOperation.equals(result.toCharArray(), source);
		if (this.hasChanged) {
			unit.getBuffer().setContents(result);
		}
	}

	/**
	 * Possible failures:
	 * <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the operation is <code>null</code></li>.
	 *  <li>INVALID_ELEMENT_TYPES - the supplied elements are not an instance of IWorkingCopy</li>.
	 * </ul>
	 * @see IJavaModelStatus
	 * @see JavaConventions
	 */
	public IJavaModelStatus verify() {
		if (fElementsToProcess.length <= 0) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		for (int i = 0, max = fElementsToProcess.length; i < max; i++) {
			if (fElementsToProcess[i] == null) {
				return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
			}
			if (!(fElementsToProcess[i] instanceof IWorkingCopy)) {
				return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, fElementsToProcess[i]);
			}
		}
		return JavaModelStatus.VERIFIED_OK;
	}
}
