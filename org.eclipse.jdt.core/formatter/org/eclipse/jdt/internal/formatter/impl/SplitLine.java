package org.eclipse.jdt.internal.formatter.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.parser.*;

/** Represents a split line: contains an operator and all substrings
*/
public class SplitLine implements TerminalSymbols{
	public int[] operators; // the operator on which the string was split.
	public String[] substrings;
	public int[] startSubstringsIndexes;
/**
 * SplitLine constructor comment.
 */
public SplitLine(int[] operators, String[] substrings) {
	this(operators, substrings, null);
}
/**
 * SplitLine constructor comment.
 */
public SplitLine(int[] operators, String[] substrings, int[] startIndexes) {
	super();
	this.operators=operators;
	this.substrings=substrings;
	this.startSubstringsIndexes = startIndexes;
}
/**
 * Prints a nice representation of the receiver
 * @return java.lang.String
 */
public String toString() {
	StringBuffer result=new StringBuffer();
	String operatorString = new String();
		
	for (int i=0,max=substrings.length;i<max;i++){
		int currentOperator = operators[i];
		String currentString = substrings[i];
		boolean placeOperatorAhead = currentOperator != TerminalSymbols.TokenNameCOMMA && currentOperator != TerminalSymbols.TokenNameSEMICOLON;
		boolean placeOperatorBehind = currentOperator == TerminalSymbols.TokenNameCOMMA || currentOperator == TerminalSymbols.TokenNameSEMICOLON;
		


	switch (currentOperator){
		case TokenNameextends:
			operatorString="extends"/*nonNLS*/;
			break;
		case TokenNameimplements:
			operatorString="implements"/*nonNLS*/;
			break;
		case TokenNamethrows:
			operatorString="throws"/*nonNLS*/;
			break;
		case TokenNameSEMICOLON : // ;
			operatorString=";"/*nonNLS*/;
			break;
		case TokenNameCOMMA : // ,
			operatorString=","/*nonNLS*/;
			break;
		case TokenNameEQUAL : // =
			operatorString="="/*nonNLS*/;
			break;
		case TokenNameAND_AND : // && (15.22)
			operatorString="&&"/*nonNLS*/;
			break;
		case TokenNameOR_OR : // || (15.23)
			operatorString="||"/*nonNLS*/;
			break;
		case TokenNameQUESTION : // ? (15.24)
			operatorString="?"/*nonNLS*/;
			break;

		case TokenNameCOLON : // : (15.24)
			operatorString=":"/*nonNLS*/;
			break;
		case TokenNameEQUAL_EQUAL : // == (15.20, 15.20.1, 15.20.2, 15.20.3)
			operatorString="=="/*nonNLS*/;
			break;

		case TokenNameNOT_EQUAL : // != (15.20, 15.20.1, 15.20.2, 15.20.3)
			operatorString="!="/*nonNLS*/;
			break;

		case TokenNameLESS : // < (15.19.1)
			operatorString="<"/*nonNLS*/;
			break;

		case TokenNameLESS_EQUAL : // <= (15.19.1)
			operatorString="<="/*nonNLS*/;
			break;

		case TokenNameGREATER : // > (15.19.1)
			operatorString=">"/*nonNLS*/;
			break;

		case TokenNameGREATER_EQUAL : // >= (15.19.1)
			operatorString=">="/*nonNLS*/;
			break;

		case TokenNameinstanceof : // instanceof
			operatorString="instanceof"/*nonNLS*/;
			break;
		case TokenNamePLUS : // + (15.17, 15.17.2)
			operatorString="+"/*nonNLS*/;
			break;

		case TokenNameMINUS : // - (15.17.2)
			operatorString="-"/*nonNLS*/;
			break;
		case TokenNameMULTIPLY : // * (15.16.1)
			operatorString="*"/*nonNLS*/;
			break;

		case TokenNameDIVIDE : // / (15.16.2)
			operatorString="/"/*nonNLS*/;
			break;

		case TokenNameREMAINDER : // % (15.16.3)
			operatorString="%"/*nonNLS*/;
			break;
		case TokenNameLEFT_SHIFT : // << (15.18)
			operatorString="<<"/*nonNLS*/;
			break;

		case TokenNameRIGHT_SHIFT : // >> (15.18)
			operatorString=">>"/*nonNLS*/;
			break;

		case TokenNameUNSIGNED_RIGHT_SHIFT : // >>> (15.18)
			operatorString=">>>"/*nonNLS*/;
			break;
		case TokenNameAND : // & (15.21, 15.21.1, 15.21.2)
			operatorString="&"/*nonNLS*/;
			break;

		case TokenNameOR : // | (15.21, 15.21.1, 15.21.2)
			operatorString="|"/*nonNLS*/;
			break;

		case TokenNameXOR : // ^ (15.21, 15.21.1, 15.21.2)
			operatorString="^"/*nonNLS*/;
			break;
		case TokenNameMULTIPLY_EQUAL : // *= (15.25.2)
			operatorString="*="/*nonNLS*/;
			break;

		case TokenNameDIVIDE_EQUAL : // /= (15.25.2)
			operatorString="/="/*nonNLS*/;
			break;
		case TokenNameREMAINDER_EQUAL : // %= (15.25.2)
			operatorString="%="/*nonNLS*/;
			break;

		case TokenNamePLUS_EQUAL : // += (15.25.2)
			operatorString="+="/*nonNLS*/;
			break;

		case TokenNameMINUS_EQUAL : // -= (15.25.2)
			operatorString="-="/*nonNLS*/;
			break;

		case TokenNameLEFT_SHIFT_EQUAL : // <<= (15.25.2)
			operatorString="<<="/*nonNLS*/;
			break;

		case TokenNameRIGHT_SHIFT_EQUAL : // >>= (15.25.2)
			operatorString=">>="/*nonNLS*/;
			break;

		case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL : // >>>= (15.25.2)
			operatorString=">>>="/*nonNLS*/;
			break;

		case TokenNameAND_EQUAL : // &= (15.25.2)
			operatorString="&="/*nonNLS*/;
			break;

		case TokenNameXOR_EQUAL : // ^= (15.25.2)
			operatorString="^="/*nonNLS*/;
			break;

		case TokenNameOR_EQUAL : // |= (15.25.2)
			operatorString="|="/*nonNLS*/;
			break;
		case TokenNameDOT : // .
			operatorString="."/*nonNLS*/;
			break;

		default:
			operatorString=""/*nonNLS*/;
	}
		if (placeOperatorAhead){
			result.append(operatorString);
		}
		result.append(currentString);
		if (placeOperatorBehind){
			result.append(operatorString);
		}
		result.append('\n');
	}
	return ""/*nonNLS*/;
}
}
