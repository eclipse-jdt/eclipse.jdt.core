package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.env.ISourceField;

public class SourceField implements ISourceField, IConstants {
	protected int modifiers;
	protected char[] typeName;
	protected char[] name;
	protected int declarationStart;
	protected int declarationEnd;
	protected int nameSourceStart;
	protected int nameSourceEnd;
	protected char[] source;
public SourceField(
	int declarationStart,
	int modifiers,
	char[] typeName,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] source) {

	this.declarationStart = declarationStart;
	this.modifiers = modifiers;
	this.typeName = typeName;
	this.name = name;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
	this.source = source;
}
public String displayModifiers(int modifiers) {
	StringBuffer buffer = new StringBuffer();

	if (modifiers == 0)
		return null;
	if ((modifiers & AccPublic) != 0)
		buffer.append("public ");
	if ((modifiers & AccProtected) != 0)
		buffer.append("protected ");
	if ((modifiers & AccPrivate) != 0)
		buffer.append("private ");
	if ((modifiers & AccFinal) != 0)
		buffer.append("final ");
	if ((modifiers & AccStatic) != 0)
		buffer.append("static ");
	if ((modifiers & AccAbstract) != 0)
		buffer.append("abstract ");
	if ((modifiers & AccNative) != 0)
		buffer.append("native ");
	if ((modifiers & AccSynchronized) != 0)
		buffer.append("synchronized ");
	return buffer.toString();
}
public String getActualName() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(source, nameSourceStart, nameSourceEnd - nameSourceStart + 1);
	return buffer.toString();
}
public int getDeclarationSourceEnd() {
	return declarationEnd;
}
public int getDeclarationSourceStart() {
	return declarationStart;
}
public int getModifiers() {
	return modifiers;
}
public char[] getName() {
	return name;
}
public int getNameSourceEnd() {
	return nameSourceEnd;
}
public int getNameSourceStart() {
	return nameSourceStart;
}
public char[] getTypeName() {
	return typeName;
}
protected void setDeclarationSourceEnd(int position) {
	declarationEnd = position;
}
public String tabString(int tab) {
	/*slow code*/

	String s = "";
	for (int i = tab; i > 0; i--)
		s = s + "\t";
	return s;
}
public String toString() {
	return toString(0);
}
public String toString(int tab) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(tabString(tab));
	if (displayModifiers(modifiers) != null) {
		buffer.append(displayModifiers(modifiers));
	}
	buffer.append(typeName).append(" ").append(name);
	buffer.append(";");
	return buffer.toString();
}
}
