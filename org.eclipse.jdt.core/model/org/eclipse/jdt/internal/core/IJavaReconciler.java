package org.eclipse.jdt.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * An <code>IJavaReconciler</code> is used to incrementally update
 * the structure of a working copy compilation unit as edits take
 * place in its buffer. As each contiguous edit is performed on the
 * buffer, calls are made to the reconciler which will update the
 * internal structure of the working copy (i.e. add/remove elements),
 * and report structural changes as <code>IJavaElementDelta</code>s.
 * <p>
 * Deltas are distributed via the <code>IElementChangedListener</code>
 * mechanism. Interested listeners must register with the
 * <code>IJavaModelManager</code>.
 */
public interface IJavaReconciler {
/**
 * Reconciles a deletion of contiguous text.
 *
 * <p>Reports deltas for any elements added/removed from the compilation
 * unit. If a container (<code>IParent</code>), is added/removed, deltas
 * are not reported for any of its children - only the top level
 * container. For example, if a type is removed, deltas are not
 * provided for its deleted members.
 *
 * <p>Updates children properties and source ranges for elements
 * in the compilation unit.
 *
 * <p>It is assumed that the <code>IBuffer</code> of the 
 * <code>ICompilationUnit</code> used in the creation of this 
 * <code>IncrementalReconciler</code> has already been updated with
 * the text deletion, but the structure of the <code>ICompilationUnit</code>
 * has not.
 */
public void textDeleted(int position, int length);
/**
 * Reconciles a insertion of contiguous text.
 *
 * <p>Reports deltas for any elements added/removed from the compilation
 * unit. If a container (<code>IParent</code>), is added/removed, deltas
 * are not reported for any of its children - only the top level
 * container. For example, if a type is removed, deltas are not
 * provided for its deleted members.
 *
 * <p>Updates children properties and source ranges for elements
 * in the compilation unit.
 *
 * <p>It is assumed that the <code>IBuffer</code> of the 
 * <code>ICompilationUnit</code> used in the creation of this 
 * <code>IncrementalReconciler</code> has already been updated with
 * the text deletion, but the structure of the <code>ICompilationUnit</code>
 * has not.
 */
public void textInserted(int position, int length);
}
