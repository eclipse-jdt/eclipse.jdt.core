/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation.
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API.
 * The mirror implementation is using the backward compatible ITerminalSymbols constant
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens
 * which constant values reflect the latest parser generation state.
 */
/**
 * Maps each terminal symbol in the java-grammar into a unique integer.
 * This integer is used to represent the terminal when computing a parsing action.
 *
 * Disclaimer : These constant values are generated automatically using a Java
 * grammar, therefore their actual values are subject to change if new keywords
 * were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

	// special tokens not part of grammar - not autogenerated
	int TokenNameNotAToken = 0,
							TokenNameWHITESPACE = 1000,
							TokenNameCOMMENT_LINE = 1001,
							TokenNameCOMMENT_BLOCK = 1002,
							TokenNameCOMMENT_JAVADOC = 1003,
							TokenNameSingleQuoteStringLiteral = 1004;

	// BEGIN_AUTOGENERATED_REGION
	int TokenNameIdentifier = 19,
							TokenNameabstract = 42,
							TokenNameassert = 81,
							TokenNameboolean = 105,
							TokenNamebreak = 82,
							TokenNamebyte = 106,
							TokenNamecase = 91,
							TokenNamecatch = 107,
							TokenNamechar = 108,
							TokenNameclass = 70,
							TokenNamecontinue = 83,
							TokenNameconst = 135,
							TokenNamedefault = 76,
							TokenNamedo = 84,
							TokenNamedouble = 109,
							TokenNameelse = 121,
							TokenNameenum = 74,
							TokenNameextends = 92,
							TokenNamefalse = 52,
							TokenNamefinal = 43,
							TokenNamefinally = 116,
							TokenNamefloat = 110,
							TokenNamefor = 85,
							TokenNamegoto = 136,
							TokenNameif = 86,
							TokenNameimplements = 132,
							TokenNameimport = 111,
							TokenNameinstanceof = 17,
							TokenNameint = 112,
							TokenNameinterface = 73,
							TokenNamelong = 113,
							TokenNamenative = 44,
							TokenNamenew = 37,
							TokenNamenon_sealed = 45,
							TokenNamenull = 53,
							TokenNamepackage = 90,
							TokenNameprivate = 46,
							TokenNameprotected = 47,
							TokenNamepublic = 48,
							TokenNamereturn = 87,
							TokenNameshort = 114,
							TokenNamestatic = 38,
							TokenNamestrictfp = 49,
							TokenNamesuper = 34,
							TokenNameswitch = 63,
							TokenNamesynchronized = 40,
							TokenNamethis = 35,
							TokenNamethrow = 78,
							TokenNamethrows = 117,
							TokenNametransient = 50,
							TokenNametrue = 54,
							TokenNametry = 88,
							TokenNamevoid = 115,
							TokenNamevolatile = 51,
							TokenNamewhile = 79,
							TokenNamemodule = 118,
							TokenNameopen = 119,
							TokenNamerequires = 122,
							TokenNametransitive = 128,
							TokenNameexports = 123,
							TokenNameopens = 124,
							TokenNameto = 133,
							TokenNameuses = 125,
							TokenNameprovides = 126,
							TokenNamewith = 134,
							TokenNameIntegerLiteral = 55,
							TokenNameLongLiteral = 56,
							TokenNameFloatingPointLiteral = 57,
							TokenNameDoubleLiteral = 58,
							TokenNameCharacterLiteral = 59,
							TokenNameStringLiteral = 60,
							TokenNameTextBlock = 61,
							TokenNamePLUS_PLUS = 2,
							TokenNameMINUS_MINUS = 3,
							TokenNameEQUAL_EQUAL = 20,
							TokenNameLESS_EQUAL = 12,
							TokenNameGREATER_EQUAL = 13,
							TokenNameNOT_EQUAL = 21,
							TokenNameLEFT_SHIFT = 18,
							TokenNameRIGHT_SHIFT = 14,
							TokenNameUNSIGNED_RIGHT_SHIFT = 16,
							TokenNamePLUS_EQUAL = 93,
							TokenNameMINUS_EQUAL = 94,
							TokenNameMULTIPLY_EQUAL = 95,
							TokenNameDIVIDE_EQUAL = 96,
							TokenNameAND_EQUAL = 97,
							TokenNameOR_EQUAL = 98,
							TokenNameXOR_EQUAL = 99,
							TokenNameREMAINDER_EQUAL = 100,
							TokenNameLEFT_SHIFT_EQUAL = 101,
							TokenNameRIGHT_SHIFT_EQUAL = 102,
							TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 103,
							TokenNameOR_OR = 31,
							TokenNameAND_AND = 30,
							TokenNamePLUS = 4,
							TokenNameMINUS = 5,
							TokenNameNOT = 66,
							TokenNameREMAINDER = 9,
							TokenNameXOR = 25,
							TokenNameAND = 22,
							TokenNameMULTIPLY = 8,
							TokenNameOR = 28,
							TokenNameTWIDDLE = 67,
							TokenNameDIVIDE = 10,
							TokenNameGREATER = 15,
							TokenNameLESS = 11,
							TokenNameLPAREN = 23,
							TokenNameRPAREN = 26,
							TokenNameLBRACE = 39,
							TokenNameRBRACE = 33,
							TokenNameLBRACKET = 6,
							TokenNameRBRACKET = 69,
							TokenNameSEMICOLON = 24,
							TokenNameQUESTION = 29,
							TokenNameCOLON = 65,
							TokenNameCOMMA = 32,
							TokenNameDOT = 1,
							TokenNameEQUAL = 77,
							TokenNameAT = 36,
							TokenNameELLIPSIS = 120,
							TokenNameARROW = 104,
							TokenNameCOLON_COLON = 7,
							TokenNameBeginLambda = 62,
							TokenNameBeginIntersectionCast = 68,
							TokenNameBeginTypeArguments = 89,
							TokenNameElidedSemicolonAndRightBrace = 71,
							TokenNameAT308 = 27,
							TokenNameAT308DOTDOTDOT = 129,
							TokenNameBeginCaseExpr = 72,
							TokenNameRestrictedIdentifierYield = 80,
							TokenNameRestrictedIdentifierrecord = 75,
							TokenNameRestrictedIdentifiersealed = 41,
							TokenNameRestrictedIdentifierpermits = 127,
							TokenNameBeginCaseElement = 130,
							TokenNameRestrictedIdentifierWhen = 131,
							TokenNameEOF = 64,
							TokenNameERROR = 137;
}
