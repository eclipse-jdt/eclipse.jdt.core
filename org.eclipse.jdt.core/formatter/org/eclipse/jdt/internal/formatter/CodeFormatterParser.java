/*
 * Created on Jun 12, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * @author oliviert
 */
public class CodeFormatterParser extends Parser {

	public CodeFormatterParser(
			ProblemReporter problemReporter,
			boolean optimizeStringLiterals,
			long sourceLevel) {
		super(problemReporter, optimizeStringLiterals, sourceLevel);
	}

	public Expression parseExpression(char[] source, CompilationUnitDeclaration unit) {
	
		initialize();
		goForExpression();
		nestedMethod[nestedType]++;
	
		// TODO check if it works fine
		referenceContext = unit;
		compilationUnit = unit;
	
		scanner.setSource(source);
		scanner.resetTo(0, source.length-1);
		try {
			parse();
		} catch (AbortCompilation ex) {
			lastAct = ERROR_ACTION;
		} finally {
			nestedMethod[nestedType]--;
		}
	
		if (lastAct == ERROR_ACTION) {
			return null;
		}
	
		return expressionStack[expressionPtr];
	}
	
	public AstNode[] parseClassBodyDeclarations(char[] source, CompilationUnitDeclaration unit) {
		/* automaton initialization */
		initialize();
		goForClassBodyDeclarations();
		/* scanner initialization */
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);

		/* unit creation */
		referenceContext = unit;
		compilationUnit = unit;

		/* run automaton */
		try {
			parse();
		} catch (AbortCompilation ex) {
			lastAct = ERROR_ACTION;
		}
	
		if (lastAct == ERROR_ACTION) {
			return null;
		}
		int length;
		if ((length = astLengthStack[astLengthPtr--]) != 0) {
			AstNode[] result = new AstNode[length];
			astPtr -= length;
			System.arraycopy(astStack, astPtr + 1, result, 0, length);
			return result;
		} else {
			return null;
		}
	}	
}
