/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public interface CompilerModifiers extends ClassFileConstants { // modifier constant
	// those constants are depending upon ClassFileConstants (relying that classfiles only use the 16 lower bits)
	final int AccDefault = 0;
	final int AccJustFlag = // 16 lower bits
	    	ASTNode.Bit1|ASTNode.Bit2|ASTNode.Bit3|ASTNode.Bit4|ASTNode.Bit5|ASTNode.Bit6|ASTNode.Bit7|ASTNode.Bit8|
			ASTNode.Bit9|ASTNode.Bit10|ASTNode.Bit11|ASTNode.Bit12|ASTNode.Bit13|ASTNode.Bit14|ASTNode.Bit15|ASTNode.Bit16;

	final int AccRestrictedAccess = ASTNode.Bit19; 
	final int AccFromClassFile = ASTNode.Bit20; 
	final int AccIsConstantValue = ASTNode.Bit20;
	final int AccDefaultAbstract = ASTNode.Bit20; 
	// bit21 - IConstants.AccDeprecated
	final int AccDeprecatedImplicitly = ASTNode.Bit22; // record whether deprecated itself or contained by a deprecated type
	final int AccAlternateModifierProblem = ASTNode.Bit23; 
	final int AccModifierProblem = ASTNode.Bit24; 
	final int AccSemicolonBody = ASTNode.Bit25; 
	final int AccUnresolved = ASTNode.Bit26; 
	final int AccClearPrivateModifier = ASTNode.Bit27; // might be requested during private access emulation
	final int AccBlankFinal = ASTNode.Bit27; // for blank final variables
	final int AccIsDefaultConstructor = ASTNode.Bit27; // for default constructor
	final int AccPrivateUsed = ASTNode.Bit28; // used to diagnose unused private members
	final int AccVisibilityMASK = AccPublic | AccProtected | AccPrivate;
	
	final int AccOverriding = ASTNode.Bit29; // record fact a method overrides another one
	final int AccImplementing = ASTNode.Bit30; // record fact a method implements another one (it is concrete and overrides an abstract one)
	final int AccGenericSignature = ASTNode.Bit31; // record fact a type/method/field involves generics in its signature (and need special signature attr)
}
