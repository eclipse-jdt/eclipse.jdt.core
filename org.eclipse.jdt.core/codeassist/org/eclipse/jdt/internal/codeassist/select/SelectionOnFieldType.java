package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionOnFieldType extends FieldDeclaration {
	public SelectionOnFieldType(TypeReference type) {
		super();
		this.sourceStart = type.sourceStart;
		this.sourceEnd = type.sourceEnd;
		this.type = type;
		this.name = NoChar;
	}
	public String toString(int tab) {
		return type.toString(tab);
	}
}
