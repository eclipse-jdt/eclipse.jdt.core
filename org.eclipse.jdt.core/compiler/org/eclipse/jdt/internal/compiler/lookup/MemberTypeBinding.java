package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.util.*;

public final class MemberTypeBinding extends NestedTypeBinding {
public MemberTypeBinding(char[][] compoundName, ClassScope scope, SourceTypeBinding enclosingType) {
	super(compoundName, scope, enclosingType);
	this.tagBits |= MemberTypeMask;
}
void checkSyntheticArgsAndFields() {
	if (this.isStatic()) return;
	if (this.isInterface()) return;
	this.addSyntheticArgumentAndField(this.enclosingType);
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] constantPoolName() /* java/lang/Object */ {
	if (constantPoolName != null)
		return constantPoolName;

	return constantPoolName = CharOperation.concat(enclosingType().constantPoolName(), sourceName, '$');
}
public String toString() {
	return "Member type : "/*nonNLS*/ + new String(sourceName()) + " "/*nonNLS*/ + super.toString();
}
}
