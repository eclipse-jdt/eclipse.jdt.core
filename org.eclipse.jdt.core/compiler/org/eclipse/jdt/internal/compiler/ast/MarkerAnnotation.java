/*
 * Created on 2004-03-11
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class MarkerAnnotation extends Annotation {
	
	public MarkerAnnotation(TypeReference type, int sourceStart) {
		this.type = type;
		this.sourceStart = sourceStart;
		this.sourceEnd = type.sourceEnd;
	}
	
	TypeBinding internalResolveType(TypeBinding annotationType, Scope scope) {
		
		if (super.internalResolveType(annotationType, scope) == null)
			return null;
		
		checkMemberValues(NoValuePairs, scope);
		return this.resolvedType;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, CompilationUnitScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
