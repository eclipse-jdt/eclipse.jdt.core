package org.eclipse.jdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;

public class NLSLine {

	private int fLineNumber;
	private List fElements;

	public NLSLine(int lineNumber) {
		fLineNumber= lineNumber;
		fElements= new ArrayList();
	}
	
	/**
	 * Adds a NLS element to this line.
	 */
	public void add(StringLiteral element) {
		fElements.add(element);
	}
	
	/**
	 * returns an Iterator over NLSElements
	 */
	public Iterator iterator() {
		return fElements.iterator();
	}
	
	public StringLiteral get(int index) {
		return (StringLiteral) fElements.get(index);
	}
	
	public void set(int index, StringLiteral literal) {
		fElements.set(index, literal);
	}
	
	public boolean exists(int index) {
		return index >= 0 && index < fElements.size();
	}
	
	public int size(){
		return fElements.size();
	}
	
	public String toString() {
		StringBuffer result= new StringBuffer();
		result.append("Line: " + fLineNumber + "\n"); //$NON-NLS-2$ //$NON-NLS-1$
		for (Iterator iter= iterator(); iter.hasNext(); ) {
			result.append("\t"); //$NON-NLS-1$
			result.append(iter.next().toString());
			result.append("\n"); //$NON-NLS-1$
		}
		return result.toString();
	}
}