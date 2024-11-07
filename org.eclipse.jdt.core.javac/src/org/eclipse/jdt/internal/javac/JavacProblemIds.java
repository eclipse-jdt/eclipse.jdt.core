/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.jdt.internal.javac;

/**
 * Constant IDs for problems that don't have a corresponding diagnostic in ECJ.
 */
public class JavacProblemIds {

	private JavacProblemIds() {}

	/// some large number, I chose this from the Wikipedia prime page for fun
	private static final int BaseJavacProblemId = 39916801;

	public static final int PossibleThisEscape = BaseJavacProblemId + 1;
	public static final int PossibleThisEscapeLocation = BaseJavacProblemId + 2;
	public static final int PossibleLossOfPrescision = BaseJavacProblemId + 3;
	public static final int AuxiliaryClassAccessedOutsideItsSourceFile = BaseJavacProblemId + 4;

}