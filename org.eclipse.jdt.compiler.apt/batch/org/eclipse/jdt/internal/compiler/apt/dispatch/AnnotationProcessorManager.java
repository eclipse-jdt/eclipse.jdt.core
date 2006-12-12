package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

public class AnnotationProcessorManager extends AbstractAnnotationProcessorManager {
	List addedUnits;
	
	public void configure(org.eclipse.jdt.internal.compiler.Compiler compiler, String[] commandLineArguments) {
		// do nothing
	}

	private AnnotationProcessorManager() {
		this.addedUnits = new ArrayList();
	}

	public void processAnnotations(CompilationUnitDeclaration[] units, boolean isLastRound) {
		// do nothing
	}
	
	public void addNewUnit(ICompilationUnit unit) {
		this.addedUnits.add(unit);
	}

	public List getNewUnits() {
		return this.addedUnits;
	}
	
	public void reset() {
		this.addedUnits.clear();
	}

	public void setErr(PrintWriter err) {
		// do nothing
	}

	public void setOut(PrintWriter out) {
		// do nothing
	}
}
