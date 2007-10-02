/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/
/*
 * Target for annotation processing test.  Processing this
 * should result in generation of a class gen.HgcGen with
 * method public String foo().
 * @see org.eclipse.jdt.compiler.apt.tests.processors.genclass.GenClassProc.
 */
package targets.filer;

import org.eclipse.jdt.compiler.apt.tests.annotations.GenResource;

public class FilerTarget1 {
	@GenResource(pkg="resources", relativeName="txt/text.txt", stringContent="A generated string")
	interface A {}

	@GenResource(pkg="resources", relativeName="dat/binary.dat", binaryContent={102, 110, 111, 114, 100})
	interface B {}
}
