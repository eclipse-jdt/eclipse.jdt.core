package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;

public interface IHierarchyRequestor {
/**
 * Connect the supplied type to its superclass & superinterfaces.
 * The superclass & superinterfaces are the identical binary or source types as
 * supplied by the name environment.
 */

public void connect(IGenericType suppliedType, IGenericType superclass, IGenericType[] superinterfaces);
}
