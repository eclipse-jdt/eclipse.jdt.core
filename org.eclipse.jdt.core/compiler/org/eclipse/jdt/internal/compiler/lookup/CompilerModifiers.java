/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public interface CompilerModifiers extends ClassFileConstants { // modifier constant
	// those constants are depending upon ClassFileConstants (relying that classfiles only use the 16 lower bits)
	final int AccDefault = 0;
	final int AccJustFlag = 0xFFFF;
	final int AccCatchesExceptions = 0x10000; // bit17
	final int AccThrowsExceptions = 0x20000; // bit18 - also IConstants.AccSynthetic
	final int AccProblem = 0x40000; // bit19
	final int AccFromClassFile = 0x80000; // bit20
	final int AccIsConstantValue = 0x80000;	 // bit20
	final int AccDefaultAbstract = 0x80000; // bit20
	// bit21 - IConstants.AccDeprecated
	final int AccDeprecatedImplicitly = 0x200000; // bit22 to record whether deprecated itself or contained by a deprecated type
	final int AccAlternateModifierProblem = 0x400000; // bit23
	final int AccModifierProblem = 0x800000; // bit24
	final int AccSemicolonBody = 0x1000000; // bit25
	final int AccUnresolved = 0x2000000; // bit26
	final int AccClearPrivateModifier = 0x4000000; // bit27 might be requested during private access emulation
	final int AccBlankFinal = 0x4000000; // bit27 for blank final variables
	final int AccPrivateUsed = 0x8000000; // bit28 used to diagnose unused private members
	final int AccVisibilityMASK = AccPublic | AccProtected | AccPrivate;
	
	final int AccOverriding = 0x10000000; // bit29 to record fact a method overrides another one
	final int AccImplementing = 0x20000000; // bit30 to record fact a method implements another one (it is concrete and overrides an abstract one)
}
