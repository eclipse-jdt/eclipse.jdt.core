package org.eclipse.jdt.core.tests.compiler.parser;

public class SourceInitializer extends SourceField {
public SourceInitializer(
	int declarationStart, 
	int modifiers) {
	super(declarationStart, modifiers, null, null, -1, -1, null);
}

public void setDeclarationSourceEnd(int declarationSourceEnd) {
	this.declarationEnd = declarationSourceEnd;
}

public String toString(int tab) {
	if (modifiers == AccStatic) {
		return tabString(tab) + "static {}";
	}
	return tabString(tab) + "{}";
}
}
