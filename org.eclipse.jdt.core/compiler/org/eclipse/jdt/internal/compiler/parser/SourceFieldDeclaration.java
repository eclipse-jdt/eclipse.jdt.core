package org.eclipse.jdt.internal.compiler.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class SourceFieldDeclaration extends FieldDeclaration {
	public int fieldEndPosition;
public SourceFieldDeclaration(
	Expression initialization, 
	char[] name, 
	int sourceStart, 
	int sourceEnd) {
	super(initialization, name, sourceStart, sourceEnd);
}
}
