package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.core.search.*;

import java.util.*;

public class OrNameCombiner implements IIndexSearchRequestor {

	IIndexSearchRequestor targetRequestor;
	HashtableOfObject acceptedAnswers = new HashtableOfObject(5);
		
public OrNameCombiner(IIndexSearchRequestor targetRequestor){
	this.targetRequestor = targetRequestor;
}
public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName){

	if (!this.acceptedAnswers.containsKey(CharOperation.concat(packageName, simpleTypeName, '.'))){
		this.targetRequestor.acceptClassDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
	}
}
public void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount) {}
public void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount) {}
public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {}
public void acceptFieldReference(String resourcePath, char[] fieldName) {}
public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {}
public void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount) {}
public void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount) {}
public void acceptPackageReference(String resourcePath, char[] packageName) {}
public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers){
}
public void acceptTypeReference(String resourcePath, char[] typeName) {}
}
