/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
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
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

class APTProblem extends CategorizedProblem 
{	
	private static final String[] NO_ARGS = new String[0];
	private final Severity _severity;
	private int _startingOffset;
	private int _endingOffset;
	private int _line;
	private IFile _resource;
	private final String _message;
	private final String _markerType;
	
	// May be null
	private final String[] _arguments;
	
	APTProblem(final String msg, 
			   final Severity severity, 
			   final IFile file, 
			   final int startingOffset,
			   final int endingOffset,
			   final int line,
			   final String[] arguments,
			   boolean isNonReconcile){
		_message = msg;
		_severity = severity;
		_startingOffset = startingOffset;
		_endingOffset = endingOffset;
		_line = line;
		_resource = file;
		_arguments = arguments;
		_markerType = isNonReconcile ? AptPlugin.APT_NONRECONCILE_COMPILATION_PROBLEM_MARKER : AptPlugin.APT_COMPILATION_PROBLEM_MARKER;
	}

	public int getID() {
		// If we have arguments, then we're quick-fixable
		if (_arguments != null) {
			return EclipseMessager.APT_QUICK_FIX_PROBLEM_ID;
		}
		else {
			return EclipseMessager.APT_PROBLEM_ID;
		}
	}
	
	public String[] getArguments() {	
		return _arguments == null ? NO_ARGS : (String[])_arguments.clone();
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
		return _severity == Severity.ERROR;
	}
	
	public boolean isWarning() {
		return _severity == Severity.WARNING;
	}
	
	public String toString()
	{
		return _message == null ? "<null message>" : _message ;  //$NON-NLS-1$
	}
	
	@Override
	public int getCategoryID() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getMarkerType() {
		return _markerType;
	}
}
