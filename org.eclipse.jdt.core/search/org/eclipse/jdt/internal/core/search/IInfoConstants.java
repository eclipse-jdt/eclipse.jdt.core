package org.eclipse.jdt.internal.core.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

public interface IInfoConstants {
	/* granularity of search results */
	int NameInfo = 1;
	int PathInfo = 2;
	int PositionInfo = 4;
	int DeclarationInfo = 8;
}
