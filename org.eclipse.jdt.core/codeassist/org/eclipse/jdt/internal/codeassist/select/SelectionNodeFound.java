package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionNodeFound extends RuntimeException {
	public Binding binding;
	public SelectionNodeFound() {
		this(null); // we found a problem in the selection node
	}

	public SelectionNodeFound(Binding binding) {
		this.binding = binding;
	}

}
