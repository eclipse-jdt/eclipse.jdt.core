/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added #createScanner allowing to make comment check stricter
 ******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.IClassFileDisassembler;

/**
 * Backward compatibility with 2.0.
 * TODO: Remove this class when IClassFileDisassembler is removed.
 * @deprecated
 */
public class DeprecatedDisassembler extends Disassembler implements IClassFileDisassembler {

}
