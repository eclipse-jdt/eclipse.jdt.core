/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.internal.tools.disassembler;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.util.Util;

public class Disassembler {

	public static void main(String[] args) throws IOException, ClassFormatException {
		if (args.length != 1) {
			System.out.println("Usage: Disassembler <path to a .class file>"); //$NON-NLS-1$
			return;
		}
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = Util.getFileByteContent(new File(args[0]));
		System.out.println(disassembler.disassemble(classFileBytes, System.getProperty("line.separator"), org.eclipse.jdt.core.util.ClassFileBytesDisassembler.SYSTEM)); //$NON-NLS-1$
	}

}
