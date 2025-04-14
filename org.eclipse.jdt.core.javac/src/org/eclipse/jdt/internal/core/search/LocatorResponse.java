package org.eclipse.jdt.internal.core.search;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 *
 */
public record LocatorResponse(int level, boolean replacementNodeFound,
		ASTNode replacement, boolean added, boolean canVisitChildren) {
}