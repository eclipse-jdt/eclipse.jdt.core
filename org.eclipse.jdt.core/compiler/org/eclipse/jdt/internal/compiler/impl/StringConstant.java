package org.eclipse.jdt.internal.compiler.impl;

public class StringConstant extends Constant {
	public String value;

	public StringConstant(String value) {
		this.value = value;
	}

	public boolean compileTimeEqual(StringConstant right) {
		//String are intermed in th compiler==>thus if two string constant
		//get to be compared, it is an equal on the vale which is done

		return true;
	}

	public String stringValue() {
		//spec 15.17.11

		//the next line do not go into the toString() send....!
		return value;

		/*
		String s = value.toString() ;
		if (s == null)
			return "null";
		else
			return s;
		*/

	}

	public String toString() {

		return "(String)\"" + value + "\"";
	}

	public int typeID() {
		return T_String;
	}

}
