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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.sun.mirror.apt.Filer;

public class ReconcileProcessorEnv extends CompilationProcessorEnv implements EclipseAnnotationProcessorEnvironment{
	
	/**
	 * Create a reconcile environment from the given context. 
	 * @param reconcileContext
	 * @return the reconcile environment or null if creation failed.
	 */
	static ReconcileProcessorEnv newEnv(ReconcileContext reconcileContext)
    {	
		CompilationUnit compilationUnit = null;
		try{
			compilationUnit = reconcileContext.getAST(AST.JLS3, true);
		}catch( JavaModelException e){
			// TODO: log error
			compilationUnit = EMPTY_AST_UNIT;
		}
		
		final ICompilationUnit workingCopy = reconcileContext.getWorkingCopy();
		IJavaProject javaProject = workingCopy.getJavaProject();
		final IFile file = (IFile)workingCopy.getResource();
       	return new ReconcileProcessorEnv(compilationUnit, file, javaProject);
    }
	
	private ReconcileProcessorEnv(
			CompilationUnit astCompilationUnit,
		    IFile file,
		    IJavaProject javaProj)
	{
		super(astCompilationUnit, file, javaProj, Phase.RECONCILE);
	}
	
	void addMessage(
			IFile resource, 
		    int start, 
			int end,
            Severity severity, 
            String msg, 
            int line,
            String[] arguments)
	{
		checkValid();
		
		if( resource == null )
			resource = getFile();
		
		assert resource != null : "don't know about the current resource"; //$NON-NLS-1$
		
		// not going to post any markers to resource outside of the one we are currently 
		// processing during reconcile phase.
		if( resource != null && !resource.equals( getFile() ) )
			return;
		
		_problems.add(createProblem(resource, start, end, severity, msg, line, arguments));
	}
	
	public CompilationUnit getASTFrom(final IFile file){
		if( _file.equals(file) )
			return _astRoot;
		else 
			return null;
	}
	
	public void addTypeDependency(String fullyQualifiedTypeName) {
		// do not sotre type dependency during reconcile.
		return;
	}
	
	public Filer getFiler(){ 
    	return new NoOpFiler();
    }
	
	private static final class NoOpFiler implements Filer{
		
		private static final OutputStream NO_OP_STREAM = new OutputStream(){
			public void write(int b) throws IOException {
				return;
			}
		};
		
		private static final PrintWriter NO_OP_WRITER = new PrintWriter(new NoOpWriter());
		
		public OutputStream createBinaryFile(Filer.Location loc, String pkg, File relPath)
			throws IOException {
			return NO_OP_STREAM;
		}
		public OutputStream createClassFile(String name) throws IOException {
			return NO_OP_STREAM;
		}
		public PrintWriter createSourceFile(String typeName) throws IOException  {
			return NO_OP_WRITER;
		}
		public PrintWriter createTextFile(Filer.Location loc, String pkg, File relPath, String charsetName) 
			throws IOException {
			return NO_OP_WRITER;
		}
	}
	
	private static final class NoOpWriter extends Writer{
		public void write(char[] cbuf, int off, int len) 
			throws IOException {
			return;
		}
		public void flush() throws IOException {
			return;
		}		
		public void close() throws IOException {
			return;
		}
	}
}
