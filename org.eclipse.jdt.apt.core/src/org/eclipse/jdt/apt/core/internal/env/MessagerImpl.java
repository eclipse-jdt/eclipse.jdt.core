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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl.Phase;
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
    
    public void printError(SourcePosition pos, String msg)
    {
    	if( pos == null )
    		printError(msg);
    	else if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, IMarker.SEVERITY_ERROR, msg);
    	else
    		print(pos, IMarker.SEVERITY_ERROR, msg);
    }
	
	public void printError(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null");
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		addMarker(_env.getFile(), start, node.getLength() + start, IMarker.SEVERITY_ERROR, msg, line );
	}

    public void printError(String msg)
    {
        print(IMarker.SEVERITY_ERROR, msg);
    }

    public void printNotice(SourcePosition pos, String msg)
    {
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, IMarker.SEVERITY_INFO, msg);
		else if (pos == null )
			printNotice(msg);
		else
    		print(pos, IMarker.SEVERITY_INFO, msg);
    }
	
	public void printNotice(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null");
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		addMarker(_env.getFile(), start, node.getLength() + start, IMarker.SEVERITY_INFO, msg, line );
	}

    public void printNotice(String msg)
    {
       print(IMarker.SEVERITY_INFO, msg);
    }

    public void printWarning(SourcePosition pos, String msg)
    {		
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, IMarker.SEVERITY_WARNING, msg);
		else if (pos == null )
			printWarning(msg); 
		else
    		print(pos, IMarker.SEVERITY_WARNING, msg);
    }
	
	public void printWarning(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null");
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		addMarker(_env.getFile(), start, node.getLength() + start, IMarker.SEVERITY_WARNING, msg, line );
	}

    public void printWarning(String msg)
    {
        print(IMarker.SEVERITY_WARNING, msg);
    }    
  
    private void print(SourcePositionImpl pos,
                       int severity,
                       String msg)
    {

        final int start = pos.getStartingOffset();
        final int end   = pos.getEndingOffset();
        final IResource resource = pos.getResource();
        if( resource == null ){
			throw new IllegalStateException("missing resource");            
        }
        else{
           addMarker(resource, 
				   	 pos.getStartingOffset(), 
				   	 pos.getEndingOffset(), 
				     severity, 
				     msg, 
				     pos.line());
        }
    }
    
    private void print(SourcePosition pos,
    				   int severity,
    				   String msg)
    {    	
    	final java.io.File file = pos.file();
    	IResource resource = null;
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
    	 
    	final IResource currentResource = _env.getFile();
    	int offset = 0;    	
    	if( currentResource.equals(resource) ){
    		final CompilationUnit unit = _env.getAstCompilationUnit();
    		//TODO: waiting on new API Bugzilla #97766
    		//offset = unit.getPosition(pos.line(), pos.column() );
    		offset = 0;
    	}    	

    	addMarker(resource, offset, -1, severity, msg, pos.line());
    }

    private void print(int severity, String msg)
    {
		addMarker(null, 0, 1, severity, msg, 1);	
    }

    /**
     * 
     * @param resource null to indicate current resource
     * @param start the starting offset of the marker
     * @param end -1 to indicate unknow ending offset.
     * @param severity the severity of the marker
     * @param msg the message on the marker
     * @param line the line number of where the marker should be
     */
    private void addMarker(final IResource resource, final int start, final int end,
                            final int severity, String msg, final int line)
    {         
    	final IResource currentResource = _env.getFile();
    	// not going to post any markers to resource outside of the one we are currently 
    	// processing during reconcile phase.
    	if( resource != null && !currentResource.equals(resource) && _env.getPhase() == Phase.RECONCILE )
    		return;
		if( msg == null )
			msg = "<no message>";		
        Map<String, Object> map = new HashMap<String, Object>(8); // entries = 6, loadFactory = 0.75 thus, capacity = 8
        map.put( IMarker.CHAR_START, start );
        if(end >= 0)
        	map.put( IMarker.CHAR_END, end );
        map.put( IMarker.SEVERITY, severity );
        map.put( IMarker.MESSAGE, msg );
		map.put( IMarker.LINE_NUMBER, line);
		map.put( IMarker.LOCATION, "line: " + line);        
        _env.addMarker(resource, map);
    }
}
