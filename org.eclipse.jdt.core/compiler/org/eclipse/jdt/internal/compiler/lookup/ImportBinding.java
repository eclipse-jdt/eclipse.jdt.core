package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.util.*;

public class ImportBinding extends Binding {
	public char[][] compoundName;
	public boolean onDemand;

	Binding resolvedImport; // must ensure the import is resolved
public ImportBinding(char[][] compoundName, boolean isOnDemand, Binding binding) {
	this.compoundName = compoundName;
	this.onDemand = isOnDemand;
	this.resolvedImport = binding;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return IMPORT;
}
public char[] readableName() {
	if (onDemand)
		return CharOperation.concat(CharOperation.concatWith(compoundName, '.'), ".*"/*nonNLS*/.toCharArray());
	else
		return CharOperation.concatWith(compoundName, '.');
}
public String toString() {
	return "import : "/*nonNLS*/ + new String(readableName());
}
}
