package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * Common protocol for Java elements that support source code assist and code
 * resolve.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ICodeAssist {
/**
 * Performs code completion at the given offset position in this compilation unit,
 * reporting results to the given completion requestor. The <code>offset</code>
 * is the 0-based index of the character, after which code assist is desired.
 * An <code>offset</code> of -1 indicates to code assist at the beginning of this
 * compilation unit.
 *
 * @exception JavaModelException if code assist could not be performed. Reasons include:<ul>
 *  <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 *  <li> The position specified is < -1 or is greater than this compilation unit's
 *      source length (INDEX_OUT_OF_BOUNDS)
 * </ul>
 *
 * @exception IllegalArgumentException if <code>requestor</code> is <code>null</code>
 */
 void codeComplete(int offset, ICodeCompletionRequestor requestor) throws JavaModelException;    
/**
 * Performs code selection on the given selected text in this compilation unit,
 * reporting results to the given selection requestor. The <code>offset</code>
 * is the 0-based index of the first selected character. The <code>length</code> 
 * is the number of selected characters.
 *
 * @exception JavaModelException if code resolve could not be performed. Reasons include:
 *  <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 *  <li> The range specified is not within this element's
 *      source range (INDEX_OUT_OF_BOUNDS)
 * </ul>
 *
 */
IJavaElement[] codeSelect(int offset, int length) throws JavaModelException;
}
