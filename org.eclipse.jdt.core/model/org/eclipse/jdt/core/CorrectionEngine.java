package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.*;

/**
 * This class is the entry point for source corrections.
 * 
 * @since 2.0 
 */
public class CorrectionEngine implements ProblemReasons {
	
	protected int correctionStart;
	protected int correctionEnd;
	protected int prefixLength;
	protected ICompilationUnit unit;
	protected ICorrectionRequestor requestor;
	
	protected static final int CLASSES = 0x00000001;
	protected static final int INTERFACES = 0x00000002;
	protected static final int IMPORT = 0x00000004;
	protected static final int METHOD = 0x00000008;
	protected static final int FIELD = 0x00000010;
	protected static final int LOCAL = 0x00000020;
	
	protected int filter;
		
	/**
	 * The CorrectionEngine is responsible for computing problem corrections.
	 *
	 *  @param setting java.util.Map
	 *		set of options used to configure the code correction engine.
	 * 		CURRENTLY THERE IS NO CORRECTION SPECIFIC SETTINGS.
	 */
	public CorrectionEngine(Map setting) {
		
	}
	
	/**
	 * Performs code correction for the given marker,
	 * reporting results to the given correction requestor.
	 * 
	 * @return void
	 *      correction results are answered through a requestor.
	 * 
	 * @param marker
	 * 		the marker which describe the problem to correct.
	 * 
	 * @param targetUnit
	 * 		replace the compilation unit given by the marker. Ignored if null.
	 * 
	 * @param positionOffset
	 * 		the offset of position given by the marker.
	 *
	 * @exception IllegalArgumentException if <code>requestor</code> is <code>null</code>
	 * @since 2.0 
	 */
	public void computeCorrections(IMarker marker, ICompilationUnit targetUnit, int positionOffset, ICorrectionRequestor requestor) throws JavaModelException {
		
		IJavaElement element = targetUnit == null ? JavaCore.create(marker.getResource()) : targetUnit;
		
		if(!(element instanceof ICompilationUnit))
			return;
			
		ICompilationUnit unit = (ICompilationUnit) element;
		
		int id = marker.getAttribute(IJavaModelMarker.ID, -1);
		String[] args = Util.getProblemArgumentsFromMarker(marker.getAttribute(IJavaModelMarker.ARGUMENTS, "")); //$NON-NLS-1$
		int start = marker.getAttribute(IMarker.CHAR_START, -1);
		int end = marker.getAttribute(IMarker.CHAR_END, -1);
		
		computeCorrections(unit, id, start + positionOffset, end + positionOffset, args, requestor);
	}
	
	/**
	 * Performs code correction for the given IProblem,
	 * reporting results to the given correction requestor.
	 * 
	 * @return void
	 *      correction results are answered through a requestor.
	 * 
	 * @param problem
	 * 		the problem which describe the problem to correct.
	 * 
	 * @param targetUnit
	 * 		denote the compilation unit in which correction occurs. Cannot be null.
	 * 
	 * @exception IllegalArgumentException if <code>targetUnit</code> or <code>requestor</code> is <code>null</code>
	 * @since 2.0 
	 */
	public void computeCorrections(IProblem problem, ICompilationUnit targetUnit, ICorrectionRequestor requestor) throws JavaModelException {
		if (requestor == null) {
			throw new IllegalArgumentException(Util.bind("correction.nullUnit")); //$NON-NLS-1$
		}
		this.computeCorrections(
			targetUnit, problem.getID(), 
			problem.getSourceStart(), 
			problem.getSourceEnd(), 
			problem.getArguments(),
			requestor);
	}

	/**
	 * Ask the engine to compute a correction for the specified problem
	 * of the given compilation unit.
	 *
	 *  @return void
	 *      correction results are answered through a requestor.
	 *
	 *  @param unit org.eclipse.jdt.internal.core.ICompilationUnit
	 *      the compilation unit.
	 *  
	 * 	@param id int
	 * 		the id of the problem.
	 * 
	 * 	@param start int
	 * 		a position in the source where the error begin.
	 *
	 *  @param end int
	 *      a position in the source where the error finish. 
	 * 
	 * 	@param arguments String[]
	 * 		arguments of the problem.
	 * 
	 * @exception IllegalArgumentException if <code>requestor</code> is <code>null</code>
	 * @since 2.0
	 */
	private void computeCorrections(ICompilationUnit unit, int id, int start, int end, String[] arguments, ICorrectionRequestor requestor) throws JavaModelException{

		if(id == -1 || arguments == null || start == -1 || end == -1)
			return;		
		if (requestor == null) {
			throw new IllegalArgumentException(Util.bind("correction.nullRequestor")); //$NON-NLS-1$
		}
		
		this.requestor = requestor;
		this.correctionStart = start;
		this.correctionEnd = end;
		this.unit = unit;
		
		String argument = null;
		try {
			switch (id) {
				// Type correction
				case IProblem.FieldTypeNotFound :
				case IProblem.ArgumentTypeNotFound :
					filter = CLASSES | INTERFACES;
					argument = arguments[2];
					break;
				case IProblem.SuperclassNotFound :
					filter = CLASSES;
					argument = arguments[0];
					break;
				case IProblem.InterfaceNotFound :
					filter = INTERFACES;
					argument = arguments[0];
					break;
				case IProblem.ExceptionTypeNotFound :
					filter = CLASSES;
					argument = arguments[1];
					break;
				case IProblem.ReturnTypeNotFound :
					filter = CLASSES | INTERFACES;
					argument = arguments[1];
					break;
				case IProblem.ImportNotFound :
					filter = IMPORT;
					argument = arguments[0];
					break;
				case IProblem.UndefinedType :
					filter = CLASSES | INTERFACES;
					argument = arguments[0];
					break;
					
				// Method correction
				case IProblem.UndefinedMethod :
					filter = METHOD;
					argument = arguments[1];
					break;
					
				// Field and local variable correction
				case IProblem.UndefinedField :
					filter = FIELD;
					argument = arguments[0];
					break;
				case IProblem.UndefinedName :
					filter = FIELD | LOCAL;
					argument = arguments[0];
					break;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return;
		}
		if(argument != null) {
			correct(argument.toCharArray());
		}
	}

	private void correct(char[] argument) throws JavaModelException {
		try {
			String source = unit.getSource();
			Scanner scanner = new Scanner();
			scanner.setSourceBuffer(source.toCharArray());
			
			scanner.resetTo(correctionStart, correctionEnd);
			int token = 0;
			char[] argumentSource = new char[0];
			
			// search last segment position
			while(true) {
				token = scanner.getNextToken();
				if (token == TerminalSymbols.TokenNameEOF) return;
				
				char[] tokenSource = scanner.getCurrentTokenSource();
				
				argumentSource = CharOperation.concat(argumentSource, tokenSource);
				if(!CharOperation.startsWith(argument, argumentSource))
					return;
				
				if(CharOperation.equals(argument, argumentSource)) {
					correctionStart = scanner.startPosition;
					correctionEnd = scanner.currentPosition;
					prefixLength = CharOperation.lastIndexOf('.', argument) + 1;
					break;
				}
				
			}
		
			// search completion position
			int completionPosition = correctionStart;
			scanner.resetTo(completionPosition, correctionEnd);
			int position = completionPosition;
			
			for (int i = 0; i < 4; i++) {
				if(scanner.getNextCharAsJavaIdentifierPart()) {
					completionPosition = position;
					position = scanner.currentPosition;
				} else {
					break;
				}
			}
			
			unit.codeComplete(
				completionPosition,
				completionRequestor
			);
		} catch (JavaModelException e) {
			return;
		} catch (InvalidInputException e) {
			return;
		}
	}

	protected ICompletionRequestor completionRequestor = new ICompletionRequestor() {
		public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd) {}
		public void acceptClass(char[] packageName,char[] className,char[] completionName,int modifiers,int completionStart,int completionEnd) {
			if((filter & (CLASSES | INTERFACES)) != 0) {
				requestor.acceptClass(
					packageName,
					className,
					CharOperation.subarray(completionName, prefixLength, completionName.length),
					modifiers,
					correctionStart,
					correctionEnd);
			} else if((filter & IMPORT) != 0) {
				char[] fullName = CharOperation.concat(packageName, className, '.');
				requestor.acceptClass(
					packageName,
					className,
					CharOperation.subarray(fullName, prefixLength, fullName.length),
					modifiers,
					correctionStart,
					correctionEnd);
			}
		}
		public void acceptError(IProblem error) {}
		public void acceptField(char[] declaringTypePackageName,char[] declaringTypeName,char[] name,char[] typePackageName,char[] typeName,char[] completionName,int modifiers,int completionStart,int completionEnd) {
			if((filter & FIELD) != 0) {
				requestor.acceptField(
					declaringTypePackageName,
					declaringTypeName,
					name,
					typePackageName,
					typeName,
					name,
					modifiers,
					correctionStart,
					correctionEnd);
			}
		}
		public void acceptInterface(char[] packageName,char[] interfaceName,char[] completionName,int modifiers,int completionStart,int completionEnd) {
			if((filter & (CLASSES | INTERFACES)) != 0) {
				requestor.acceptInterface(
					packageName,
					interfaceName,
					CharOperation.subarray(completionName, prefixLength, completionName.length),
					modifiers,
					correctionStart,
					correctionEnd);
			} else if((filter & IMPORT) != 0) {
				char[] fullName = CharOperation.concat(packageName, interfaceName, '.');
				requestor.acceptInterface(
					packageName,
					interfaceName,
					CharOperation.subarray(fullName, prefixLength, fullName.length),
					modifiers,
					correctionStart,
					correctionEnd);
			}
		}
		public void acceptKeyword(char[] keywordName,int completionStart,int completionEnd) {}
		public void acceptLabel(char[] labelName,int completionStart,int completionEnd) {}
		public void acceptLocalVariable(char[] name,char[] typePackageName,char[] typeName,int modifiers,int completionStart,int completionEnd) {
			if((filter & LOCAL) != 0) {
				requestor.acceptLocalVariable(
					name,
					typePackageName,
					typeName,
					modifiers,
					correctionStart,
					correctionEnd);
			}
		}
		public void acceptMethod(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd) {
			if((filter & METHOD) != 0) {
				requestor.acceptMethod(
					declaringTypePackageName,
					declaringTypeName,
					selector,
					parameterPackageNames,
					parameterTypeNames,
					parameterNames,
					returnTypePackageName,
					returnTypeName,
					selector,
					modifiers,
					correctionStart,
					correctionEnd);
			}
		}
		public void acceptMethodDeclaration(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd) {}
		public void acceptModifier(char[] modifierName,int completionStart,int completionEnd) {}
		public void acceptPackage(char[] packageName,char[] completionName,int completionStart,int completionEnd) {
			if((filter & (CLASSES | INTERFACES | IMPORT)) != 0) {
				requestor.acceptPackage(
					packageName,
					CharOperation.subarray(packageName, prefixLength, packageName.length),
					correctionStart,
					correctionEnd);
			}
		}
		public void acceptType(char[] packageName,char[] typeName,char[] completionName,int completionStart,int completionEnd) {}
		public void acceptVariableName(char[] typePackageName,char[] typeName,char[] name,char[] completionName,int completionStart,int completionEnd) {}
	};
}