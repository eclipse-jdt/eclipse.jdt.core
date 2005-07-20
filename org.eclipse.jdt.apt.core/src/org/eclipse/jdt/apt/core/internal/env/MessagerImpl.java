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

import com.sun.mirror.apt.Messager;
import com.sun.mirror.util.SourcePosition;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class MessagerImpl implements Messager, EclipseMessager
{
    private final ProcessorEnvImpl _env;

    MessagerImpl(ProcessorEnvImpl env){
        _env = env;
    }
    
    public void printError(SourcePosition pos, String msg, String[] arguments)
    {
    	if( pos == null )
    		printError(msg);
    	else if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, APTProblem.Severity.ERROR, msg, arguments);
    	else
    		print(pos, APTProblem.Severity.ERROR, msg, arguments);
    }
	
	public void printError(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		_env.addProblem(_env.getFile(), start, node.getLength() + start, APTProblem.Severity.ERROR, msg, line, null );
	}

    public void printError(String msg)
    {
        print(APTProblem.Severity.ERROR, msg, null);
    }

    public void printNotice(SourcePosition pos, String msg, String[] arguments)
    {
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, APTProblem.Severity.INFO, msg, arguments);
		else if (pos == null )
			printNotice(msg);
		else
    		print(pos, APTProblem.Severity.INFO, msg, arguments);
    }
	
	public void printNotice(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		_env.addProblem(_env.getFile(), start, node.getLength() + start, APTProblem.Severity.INFO, msg, line, null );
	}

    public void printNotice(String msg)
    {
       print(APTProblem.Severity.INFO, msg, null);
    }

    public void printWarning(SourcePosition pos, String msg, String[] arguments)
    {		
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, APTProblem.Severity.WARNING, msg, arguments);
		else if (pos == null )
			printWarning(msg); 
		else
    		print(pos, APTProblem.Severity.WARNING, msg, arguments);
    }
	
	public void printWarning(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		_env.addProblem(_env.getFile(), start, node.getLength() + start, APTProblem.Severity.WARNING, msg, line, null);
	}

    public void printWarning(String msg)
    {
        print(APTProblem.Severity.WARNING, msg, null);
    }
    
    public void printError(SourcePosition pos, String msg) {
		printError(pos, msg, null);
	}

	public void printWarning(SourcePosition pos, String msg) {
		printWarning(pos, msg, null);
	}

	public void printNotice(SourcePosition pos, String msg) {
		printNotice(pos, msg, null);
	}
	
	public void printFixableError(SourcePosition pos, String msg, String... arguments) {
		if (arguments == null) {
			throw new IllegalArgumentException("Arguments cannot be null"); //$NON-NLS-1$
		}
		printError(pos, msg, arguments);
	}
	
	public void printFixableWarning(SourcePosition pos, String msg, String... arguments) {
		if (arguments == null) {
			throw new IllegalArgumentException("Arguments cannot be null"); //$NON-NLS-1$
		}
		printWarning(pos, msg, arguments);
	}
	
	public void printFixableNotice(SourcePosition pos, String msg, String... arguments) {
		if (arguments == null) {
			throw new IllegalArgumentException("Arguments cannot be null"); //$NON-NLS-1$
		}
		printNotice(pos, msg, arguments);
	}
	
	public void printFixableError(String msg, String... arguments) {
		if (arguments == null) {
			throw new IllegalArgumentException("Arguments cannot be null"); //$NON-NLS-1$
		}
		print(APTProblem.Severity.ERROR, msg, arguments);
	}
	
	public void printFixableWarning(String msg, String... arguments) {
		if (arguments == null) {
			throw new IllegalArgumentException("Arguments cannot be null"); //$NON-NLS-1$
		}
		print(APTProblem.Severity.WARNING, msg, arguments);
	}
	
	public void printFixableNotice(String msg, String... arguments) {
		if (arguments == null) {
			throw new IllegalArgumentException("Arguments cannot be null"); //$NON-NLS-1$
		}
		print(APTProblem.Severity.INFO, msg, arguments);
	}
  
    private void print(SourcePositionImpl pos,
    				   APTProblem.Severity severity,
                       String msg,
                       String[] arguments)
    {
        final IFile resource = pos.getResource();
        if( resource == null ){
			throw new IllegalStateException("missing resource"); //$NON-NLS-1$            
        }
        else{          
          _env.addProblem(resource, pos.getStartingOffset(), pos.getEndingOffset(), 
						  severity, msg, pos.line(), arguments);
        }
    }
    
    private void print(SourcePosition pos,
    				   APTProblem.Severity severity,
    				   String msg,
    				   String[] arguments)
    {    	
    	final java.io.File file = pos.file();
    	IFile resource = null;
    	if( file != null ){    		
    		final String projAbsPath = _env.getProject().getLocation().toOSString();
    		final String fileAbsPath = file.getAbsolutePath();
    		final String fileRelPath = fileAbsPath.substring(projAbsPath.length());    			
    		resource = _env.getProject().getFile(fileRelPath);
    		if( !resource.exists() )
    			resource = null;
    	}
    	else
    		resource = null;
    	 
    	final IFile currentResource = _env.getFile();
    	int offset = 0;    	
    	if( currentResource.equals(resource) ){
    		final CompilationUnit unit = _env.getAstCompilationUnit();    		
    		offset = unit.getPosition(pos.line(), pos.column() );
    	}    	
    	_env.addProblem(resource, offset, -1, severity, msg, pos.line(), arguments );   
    }

    private void print(APTProblem.Severity severity, String msg, String[] arguments)
    {
    	_env.addProblem(null, 0, -1, severity, msg, 1, arguments );  
		
    }
  
}
