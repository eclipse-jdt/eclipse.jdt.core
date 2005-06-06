/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.compiler.IProblem;

public class APTProblem implements IProblem 
{
	public static enum Severity{ Error, Warning, Info};
	public static final String[] NO_ARGS = new String[0];
	private final int _id;
	private final Severity _severity;
	private int _startingOffset;
	private int _endingOffset;
	private int _line;
	private IFile _resource;
	private final String _message;
	
	APTProblem(final int id, 
			   final String msg, 
			   final Severity severity, 
			   final IFile file, 
			   final int startingOffset,
			   final int endingOffset,
			   final int line){
		_id = id;
		_message = msg;
		_severity = severity;
		_startingOffset = startingOffset;
		_endingOffset = endingOffset;
		_line = line;
		_resource = file;
	}	
   
	public int getID() {
		return _id;
	}
	public String[] getArguments() {	
		return NO_ARGS;
	}
	
	public String getMessage() {	
		return _message;
	}
	
	public char[] getOriginatingFileName() {		
		return _resource.getName().toCharArray();
	}
	
	public int getSourceStart() {
		return _startingOffset;
	}
	
	public int getSourceEnd() {	
		return _endingOffset;
	}
	
	public int getSourceLineNumber() {		
		return _line;
	}
	
	public void setSourceStart(int sourceStart) {
		_startingOffset = sourceStart;
	}	
	
	public void setSourceEnd(int sourceEnd) {
		_endingOffset = sourceEnd;
	}
	
	public void setSourceLineNumber(int lineNumber) {
		_line = lineNumber;		
	}
	
	public boolean isError() {
		return _severity == Severity.Error;
	}
	
	public boolean isWarning() {
		return _severity == Severity.Warning;
	}
}
